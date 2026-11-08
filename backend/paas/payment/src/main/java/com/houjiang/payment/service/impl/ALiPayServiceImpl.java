package com.houjiang.payment.service.impl;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayFundTransUniTransferModel;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.domain.Participant;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayFundTransUniTransferRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayFundTransUniTransferResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.houjiang.commons.config.ALiPayConfig;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.dto.base.MemberMessageListIntegralDTO;
import com.houjiang.commons.dto.oos.OssUploadUpdateParamDTO;
import com.houjiang.commons.dto.order.WapGoodsSaleSumDTO;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.*;
import com.houjiang.commons.util.json.JsonUtils;
import com.houjiang.commons.util.secverifyapiutil.Base64Utils;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.payment.dto.BalanceWithdrawDTO;
import com.houjiang.payment.dto.GoodsDTO;
import com.houjiang.payment.dto.RecordDTO;
import com.houjiang.payment.feign.*;
import com.houjiang.payment.mapper.*;
import com.houjiang.payment.service.ALiPayService;
import com.houjiang.payment.vo.CallBackVO;
import com.houjiang.payment.vo.ComsumeRecordVO;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Amos
 */
@Service
public class ALiPayServiceImpl extends BasePayServiceImpl implements ALiPayService {

    final static Logger log = LoggerFactory.getLogger(ALiPayServiceImpl.class);

    @Resource
    private PayConsumeTradeBackMapper payConsumeTradeBackMapper;

    @Resource
    private PayConsumeTradeMapper payConsumeTradeMapper;

    @Resource
    private PayConsumeRecordMapper payConsumeRecordMapper;

    @Autowired
    private UsermsServiceClient usermsServiceClient;

    @Autowired
    private ThirdPartyServiceClient thirdPartyServiceClient;

    @Autowired
    private OrderServiceClient orderServiceClient;

    @Autowired
    private ActivityServiceClient activityServiceClient;

    @Resource
    private PayUserResourceMapper payUserResourceMapper;

    @Autowired
    private GoodServiceClient goodServiceClient;

    @Resource
    private PayAssetDetailsMapper payAssetDetailsMapper;

    @Resource
    private PayUserBaseMapper payUserBaseMapper;

    @Override
    public void alipayNotifyUrl(HttpServletRequest request, HttpServletResponse response) {
        log.info("----进入异步回调----");
        // 获取支付宝POST过来反馈信息
        Map<String, String> params = new HashMap<String, String>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext(); ) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            params.put(name, valueStr);
        }

        try {
            log.info("-----进入开始验签------");
            boolean signVerified = AlipaySignature.rsaCertCheckV1(params, ALiPayConfig.CERT_PATH, ALiPayConfig.CHARSET, ALiPayConfig.SIGNTYPE);
            PrintWriter out = response.getWriter();
            /**
             * 实际验证过程建议商户务必添加以下校验：
             * 1、需要验证该通知数据中的out_trade_no是否为商户系统中创建的订单号，
             * 2、判断total_amount是否确实为该订单的实际金额（即商户订单创建时的金额），
             * 3、校验通知中的seller_id（或者seller_email)是否为out_trade_no这笔单据的对应的操作方（有的时候，一个商户可能有多个seller_id/seller_email）
             * 4、验证app_id是否为该商户本身。
             */
            if (signVerified) {
                log.info("-----验签成功----");
                // 商户订单号
                String out_trade_no = new String(request.getParameter("out_trade_no").getBytes("ISO-8859-1"), "UTF-8");
                log.info("-----out_trade_no-----" + out_trade_no);
                // 支付宝交易号
                String trade_no = new String(request.getParameter("trade_no").getBytes("ISO-8859-1"), "UTF-8");
                log.info("-----trade_no-----" + trade_no);
                // 交易状态
                String trade_status = new String(request.getParameter("trade_status").getBytes("ISO-8859-1"), "UTF-8");
                log.info("-----trade_status-----" + trade_status);
                // 交易订单金额
                String total_amount = new String(request.getParameter("total_amount").getBytes("ISO-8859-1"), "UTF-8");
                log.info("-----total_amount-----" + total_amount);
                // 卖家支付宝用户号
                String seller_id = new String(request.getParameter("seller_id").getBytes("ISO-8859-1"), "UTF-8");
                log.info("-----seller_id-----" + seller_id);

                // 判断状态
                // 如果状态为TRADE_FINISHED,则交易结束，不可退款
                if (trade_status.equals("TRADE_FINISHED")) {
                    // 判断该笔订单是否在商户网站中已经做过处理
                    // 如果没有做过处理，根据订单号（out_trade_no）在商户网站的订单系统中查到该笔订单的详细，并执行商户的业务程序
                    // 请务必判断请求时的total_fee、seller_id与通知时获取的total_fee、seller_id为一致的
                    // 如果有做过处理，不执行商户的业务程序

                    // 注意：
                    // 如果签约的是可退款协议，退款日期超过可退款期限后（如三个月可退款），支付宝系统发送该交易状态通知
                    // 如果没有签约可退款协议，那么付款完成后，支付宝系统发送该交易状态通知。
                    out.println("success");
                    out.close();
                } else if (trade_status.equals("WAIT_BUYER_PAY")) {
                    out.println("success");
                    out.close();
                } else if (trade_status.equals("TRADE_CLOSED")) {
                    out.println("success");
                    out.close();
                } else if (trade_status.equals("TRADE_SUCCESS")) {
                    usermsServiceClient.setVKInfo(out_trade_no);
                    // 更改销量
                    List<WapGoodsSaleSumDTO> wapGood = orderServiceClient.queryGoodsByOrderId(out_trade_no);
                    goodServiceClient.updateGoodsSalenum(wapGood);
                    // 订单号是纯数字
                    if (MathUtil.isInteger(out_trade_no)) {
                        //进入回调纯数字订单逻辑处理
                        orderIsInteger(out_trade_no, total_amount, seller_id, trade_no);
                    } else {
                        //进入非纯数字订单逻辑处理
                        orderIsNotInteger(out_trade_no, total_amount, seller_id, trade_no);
                    }
                    out.println("success");
                    out.close();
                }
            } else {
                log.info("验签失败！----- Verify signature error -----------------");
                out.println("fail");
                out.close();
            }
        } catch (Exception e) {
            log.info("alipayNotifyUrlErrorResultsTwo2:" + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void alipayPlusNotifyUrl(HttpServletRequest request, HttpServletResponse response) {
        log.info("----进入异步回调----");
        Map<String, String> params = new HashMap<String, String>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext(); ) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            try {
                params.put(name, valueStr);
            } catch (Exception e) {
                log.info("alipayNotifyUrlErrorResultsOne:" + e.getMessage());
            }
        }

        try {
            log.info("-----进入开始验签------");
            boolean signVerified = AlipaySignature.rsaCertCheckV1(params, ALiPayConfig.CERT_PATH, ALiPayConfig.CHARSET, ALiPayConfig.SIGNTYPE);
            log.info("----------------" + signVerified + "==" + ALiPayConfig.CERT_PATH);
            PrintWriter out = response.getWriter();
            /**
             * 实际验证过程建议商户务必添加以下校验： 1、需要验证该通知数据中的out_trade_no是否为商户系统中创建的订单号，
             * 2、判断total_amount是否确实为该订单的实际金额（即商户订单创建时的金额）， 3、校验通知中的seller_id（或者seller_email)
             * 是否为out_trade_no这笔单据的对应的操作方（有的时候，一个商户可能有多个seller_id/seller_email）
             * 4、验证app_id是否为该商户本身。
             */
            if (signVerified) {
                log.info("-----验签成功----");
                // 商户订单号
                String out_trade_no = new String(request.getParameter("out_trade_no").getBytes("ISO-8859-1"), "UTF-8");
                log.info("-----out_trade_no-----" + out_trade_no);
                // 支付宝交易号
                String trade_no = new String(request.getParameter("trade_no").getBytes("ISO-8859-1"), "UTF-8");
                log.info("-----trade_no-----" + trade_no);
                // 交易状态
                String trade_status = new String(request.getParameter("trade_status").getBytes("ISO-8859-1"), "UTF-8");
                log.info("-----trade_status-----" + trade_status);
                // 交易订单金额
                String total_amount = new String(request.getParameter("total_amount").getBytes("ISO-8859-1"), "UTF-8");
                log.info("-----total_amount-----" + total_amount);
                // 卖家支付宝用户号
                String seller_id = new String(request.getParameter("seller_id").getBytes("ISO-8859-1"), "UTF-8");
                log.info("-----seller_id-----" + seller_id);

                String passbackParams = "";
                Object obj = request.getParameter("passback_params");
                Map map = new HashMap();
                if (null != obj) {
                    // 获取存放值
                    passbackParams = URLDecoder.decode(
                            new String(request.getParameter("passback_params").getBytes("ISO-8859-1"), "UTF-8"), "UTF-8");
                    System.out.println("-----passbackParams-----" + passbackParams);
                    map = JsonUtils.string2Obj(passbackParams, Map.class);
                    System.out.println("-----plus的map-----" + map.get("token"));
                    map.put("plusOrder", map.get("out_trade_no"));
                }

                if (trade_status.equals("TRADE_FINISHED")) {
                    // 判断该笔订单是否在商户网站中已经做过处理
                    // 如果没有做过处理，根据订单号（out_trade_no）在商户网站的订单系统中查到该笔订单的详细，并执行商户的业务程序
                    // 请务必判断请求时的total_fee、seller_id与通知时获取的total_fee、seller_id为一致的
                    // 如果有做过处理，不执行商户的业务程序

                    // 注意：
                    // 如果签约的是可退款协议，退款日期超过可退款期限后（如三个月可退款），支付宝系统发送该交易状态通知
                    // 如果没有签约可退款协议，那么付款完成后，支付宝系统发送该交易状态通知。
                    out.println("success");
                    out.close();
                } else if (trade_status.equals("WAIT_BUYER_PAY")) {
                    out.println("success");
                    out.close();
                } else if (trade_status.equals("TRADE_CLOSED")) {
                    out.println("success");
                    out.close();
                } else if (trade_status.equals("TRADE_SUCCESS")) {
                    log.info("----TRADE_SUCCESS---");
                    //插入plus会员信息
                    map.put("type", CommonType.plusPayType.PLUS_ALI_PAY.getCode());
                    ResponseResult plusmemberopen = usermsServiceClient.plusmemberopen(map);
                    if (plusmemberopen.getCode() == 1 || plusmemberopen.getCode() == 3) {
                        // 结果已处理，返回success
                        out.println("success");
                        out.close();
                    } else {
                        out.println("fail");
                        out.close();
                    }
                }
            } else {
                log.info("验签失败！----- Verify signature error -----------------");
                out.println("fail");
                out.close();
            }
        } catch (Exception e) {
            log.info("alipayNotifyUrlErrorResultsTwo2:" + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void alipayBalanceRechargeNotifyUrl(HttpServletRequest request, HttpServletResponse response) {
        log.info("----进入异步回调----");
        Map<String, String> params = new HashMap<String, String>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext(); ) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            try {
                params.put(name, valueStr);
            } catch (Exception e) {
                log.info("alipayBalanceRechargeNotifyUrlErrorResultsOne:" + e.getMessage());
            }
        }

        try {
            log.info("-----进入开始验签------");
            boolean signVerified = AlipaySignature.rsaCertCheckV1(params, ALiPayConfig.CERT_PATH, ALiPayConfig.CHARSET, ALiPayConfig.SIGNTYPE);
            log.info("----------------" + signVerified + "==" + ALiPayConfig.CERT_PATH);
            PrintWriter out = response.getWriter();
            /**
             * 实际验证过程建议商户务必添加以下校验： 1、需要验证该通知数据中的out_trade_no是否为商户系统中创建的订单号，
             * 2、判断total_amount是否确实为该订单的实际金额（即商户订单创建时的金额）， 3、校验通知中的seller_id（或者seller_email)
             * 是否为out_trade_no这笔单据的对应的操作方（有的时候，一个商户可能有多个seller_id/seller_email）
             * 4、验证app_id是否为该商户本身。
             */
            if (signVerified) {
                log.info("-----验签成功----");
                // 商户订单号
                String out_trade_no = new String(request.getParameter("out_trade_no").getBytes("ISO-8859-1"), "UTF-8");
                log.info("-----out_trade_no-----" + out_trade_no);
                // 支付宝交易号
                String trade_no = new String(request.getParameter("trade_no").getBytes("ISO-8859-1"), "UTF-8");
                log.info("-----trade_no-----" + trade_no);
                // 交易状态
                String trade_status = new String(request.getParameter("trade_status").getBytes("ISO-8859-1"), "UTF-8");
                log.info("-----trade_status-----" + trade_status);
                // 交易订单金额
                String total_amount = new String(request.getParameter("total_amount").getBytes("ISO-8859-1"), "UTF-8");
                log.info("-----total_amount-----" + total_amount);
                // 卖家支付宝用户号
                String seller_id = new String(request.getParameter("seller_id").getBytes("ISO-8859-1"), "UTF-8");
                log.info("-----seller_id-----" + seller_id);
                if (trade_status.equals("TRADE_FINISHED")) {
                    // 判断该笔订单是否在商户网站中已经做过处理
                    // 如果没有做过处理，根据订单号（out_trade_no）在商户网站的订单系统中查到该笔订单的详细，并执行商户的业务程序
                    // 请务必判断请求时的total_fee、seller_id与通知时获取的total_fee、seller_id为一致的
                    // 如果有做过处理，不执行商户的业务程序

                    // 注意：
                    // 如果签约的是可退款协议，退款日期超过可退款期限后（如三个月可退款），支付宝系统发送该交易状态通知
                    // 如果没有签约可退款协议，那么付款完成后，支付宝系统发送该交易状态通知。
                    out.println("success");
                    out.close();
                } else if (trade_status.equals("WAIT_BUYER_PAY")) {
                    out.println("success");
                    out.close();
                } else if (trade_status.equals("TRADE_CLOSED")) {
                    out.println("success");
                    out.close();
                } else if (trade_status.equals("TRADE_SUCCESS")) {
                    log.info("----TRADE_SUCCESS---");
                    Map<Object, Object> map = new HashMap<>();
                    // 插入plus会员信息
                    map.put("rechargeOrderId", out_trade_no);
                    map.put("rechargeOtherNumber", trade_no);
                    map.put("payCode", CommonType.balanceRechargeOrderStart.ALI_PAY.getCode());
                    map.put("rechargeOrderStart", CommonType.balanceRechargeOrderStart.FINISH.getCode());

                    //插入记录信息
                    ResponseResult recharge = orderServiceClient.updateBalanceRechargeOrder(map);
                    if (recharge.getCode() == 1 || recharge.getCode() == 3) {
                        // 结果已处理，返回success
                        out.println("success");
                        out.close();
                    } else {
                        out.println("fail");
                        out.close();
                    }
                }
            } else {
                log.info("验签失败！----- Verify signature error -----------------");
                out.println("fail");
                out.close();
            }
        } catch (Exception e) {
            log.info("alipayBalanceRechargeNotifyUrlErrorResultsTwo2:" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 当支付单号为纯数字订单
     *
     * @param outTradeNo
     * @param totalAmount
     * @param sellerId
     * @param tradeNo
     */
    public void orderIsInteger(String outTradeNo, String totalAmount, String sellerId, String tradeNo) {
        Map<String, String> orderIdIsNum = new HashMap<>();
        //虚拟订单 0是 1否
        Integer virtualGoods = null;
        //门店订单 1 是 2否
        Integer orderIsShopTake = null;
        // 是否同城配送2-否 1-是
        Integer isDelivery = null;
        RecordDTO queryGoodsPara = payConsumeTradeBackMapper.queryRecordPriceAndName(outTradeNo);
        //判断订单是否已支付成功
        if (!queryGoodsPara.getOrderStateId().equals(6)) {
            String orderId = queryGoodsPara.getOrderId();
            String recordMoney = queryGoodsPara.getTradePaymentAmount();
            if (outTradeNo.equals(orderId) && totalAmount.equals(recordMoney)
                    && sellerId.equals(ALiPayConfig.sellerId)) {
                payConsumeTradeMapper.updateConsumeTradePaymentMoney(outTradeNo, 3);
                orderIdIsNum.put("orderId", outTradeNo);
                //订单号是数字
                orderIdIsNum.put("isNum", "0");

                //获取非数字订单
                String recordPayorder = orderServiceClient.queryAssetInfo(outTradeNo, "").getFirst().getPaymentNumber();
                //修改拼团的支付状态
                Map<String, Object> pinTuanMap = new HashMap<>();
                pinTuanMap.put("orderId", recordPayorder);
                activityServiceClient.updatePinTuanPayStart(pinTuanMap);

                //判断是否是积分商品订单 并修改用户积分
                updateUserJIfen(outTradeNo);
                //判断是否是虚拟订单
                Map<String, Integer> judgeOrderMap = this.virtualWxExchangeCode(orderIdIsNum);
                virtualGoods = judgeOrderMap.get("virtualGoods");
                orderIsShopTake = judgeOrderMap.get("orderIsShopTake");
                isDelivery = judgeOrderMap.get("isDelivery");
                Map<Object, Object> map = new HashMap<Object, Object>();
                // 支付方式
                Integer payCode = CommonType.payType.PAY_TYPE_ZFB.getCode();
                // 待发货
                Integer orderStatus = CommonType.orderStatus.TO_BE_SHIPPED.getCode();
                //虚拟订单 0 是 1 否
                if (null != virtualGoods && virtualGoods.equals(CommonType.orderType.IS_VIRTUAL.getCode())) {
                    //如果是虚拟订单就将订单状态修改为 已发货
                    orderStatus = CommonType.orderStatus.SHIPPED.getCode();
                }
                //门店订单 1 是 2 否 且不是同城配送
                if (null != orderIsShopTake && orderIsShopTake.equals(CommonType.orderType.IS_SHOP_TAKE.getCode()) && CommonType.DeliveryOrderStart.not_is_delivery.getCode().equals(isDelivery)) {
                    //如果是门店订单就将订单状态修改为 已发货
                    orderStatus = CommonType.orderStatus.SHIPPED.getCode();
                }
                // 支付方式
                Integer paymentId = CommonType.payType.ONLINE_PAY.getCode();
                // 支付方式名称
                String paymentName = "在线支付";
                // 支付时间
                String paymentTime = DateUtils.getNowDateToString();
                // 第三方支付号
                String paymentOtherNumber = tradeNo;
                // 支付方式
                map.put("payCode", payCode);
                map.put("paymentId", paymentId);
                // 商户订单号
                map.put("paymentNumber", outTradeNo);
                map.put("orderStatus", orderStatus);
                map.put("paymentName", paymentName);
                map.put("paymentTime", paymentTime);
                map.put("paymentOtherNumber", paymentOtherNumber);
                orderServiceClient.HFiveUpdateOrderBaseByPayment(map);
                // 更改支付表状态ConsumeRecord
                Integer recordStatus = 3;
                Map<String, Object> mapPureDigital = new HashMap<>();
                mapPureDigital.put("recordStatus", recordStatus);
                mapPureDigital.put("paymentTime", paymentTime);
                mapPureDigital.put("outTradeNo", outTradeNo);
                payConsumeRecordMapper.updateComsumeRecordByPureDigital(mapPureDigital);
                // 发送feign,提交下单通知
                Map<String, Object> messageMap = new HashMap<String, Object>();
                // 支付号
                messageMap.put("orderId", outTradeNo);
                messageMap.put("paymentTime", paymentTime);
                // 发送下单通知
                orderServiceClient.orderToInform(messageMap);
                // 更改ConsumeTrade
                Integer orderStateId = 3;
                String closeTime = DateUtils.getNowDateToString();

                Map<String, Object> mapDigital = new HashMap<>();
                mapDigital.put("orderStateId", orderStateId);
                mapDigital.put("paymentTime", paymentTime);
                mapDigital.put("closeTime", closeTime);
                mapDigital.put("outTradeNo", outTradeNo);
                payConsumeTradeBackMapper.updateConsumeTradeByPureDigital(mapDigital);

            } else {
                log.info("订单号或订单金额不符！");
            }
        }
    }

    /**
     * 当支付单号不是纯数字订单
     *
     * @param outTradeNo
     * @param totalAmount
     * @param sellerId
     * @param tradeNo
     */
    public void orderIsNotInteger(String outTradeNo, String totalAmount, String sellerId, String tradeNo) {
        Map<String, String> orderIdIsNum = new HashMap<>();
        //虚拟订单 0是 1否
        Integer virtualGoods = null;
        //门店订单 1 是 2否
        Integer orderIsShopTake = null;
        // 订单号不是纯数字Record表查询
        List<Integer> recodeStatus = payConsumeRecordMapper.queryRecodeStatus(outTradeNo);
        orderIdIsNum.put("orderId", outTradeNo);
        //订单号不是是数字
        orderIdIsNum.put("isNum", "1");

        //修改拼团的支付状态
        Map<String, Object> pinTuanMap = new HashMap<>();
        pinTuanMap.put("orderId", outTradeNo);
        activityServiceClient.updatePinTuanPayStart(pinTuanMap);

        //判断是否是积分商品订单 并修改用户积分
        updateUserJIfen(outTradeNo);
        //判断是否是虚拟订单
        Map<String, Integer> judgeOrderMap = this.virtualWxExchangeCode(orderIdIsNum);
        virtualGoods = judgeOrderMap.get("virtualGoods");
        orderIsShopTake = judgeOrderMap.get("orderIsShopTake");
        // 判断订单状态是否改变
        for (int i = 0; i < recodeStatus.size(); i++) {
            String orderId = null;
            if (recodeStatus.get(i) == 1) {
                List<ComsumeRecordVO> payConsumeRecord = payConsumeRecordMapper.queryorderNo(outTradeNo);
                BigDecimal money = new BigDecimal(0);
                for (int j = 0; j < payConsumeRecord.size(); j++) {
                    money = money.add(payConsumeRecord.get(j).getRecordMoney());
                }
                orderId = payConsumeRecord.get(0).getOrderId();
                String recordMoney = money.toString();
                if (outTradeNo.equals(orderId) && totalAmount.equals(recordMoney)
                        && sellerId.equals(ALiPayConfig.sellerId)) {
                    Map<Object, Object> map = new HashMap<Object, Object>();
                    // 支付方式
                    Integer payCode = 2;
                    // 待发货
                    Integer orderStatus = 3;
                    //虚拟订单 0 是 1 否
                    if (null != virtualGoods && 0 == virtualGoods) {
                        //如果是虚拟订单就将订单状态修改为 已发货
                        orderStatus = 4;
                    }
                    //门店订单 0 是 1 否
                    if (null != orderIsShopTake && 0 == orderIsShopTake) {
                        //如果是门店订单就将订单状态修改为 已发货
                        orderStatus = 4;
                    }
                    // 支付方式
                    Integer paymentId = 1;
                    // 支付方式名称
                    String paymentName = "在线支付";
                    // 支付时间
                    String paymentTime = DateUtils.getNowDateToString();
                    // 第三方支付号
                    String paymentOtherNumber = tradeNo;
                    // 支付方式
                    map.put("payCode", payCode);
                    map.put("paymentId", paymentId);
                    // 商户订单号
                    map.put("paymentNumber", outTradeNo);
                    map.put("orderStatus", orderStatus);
                    map.put("paymentName", paymentName);
                    map.put("paymentTime", paymentTime);
                    map.put("paymentOtherNumber", paymentOtherNumber);
                    orderServiceClient.HFiveUpdateOrderBaseByPayment(map);
                    // 更改支付表状态ConsumeRecord
                    Integer recordStatus = 3;
                    Map<String, Object> mapPureDigital = new HashMap<>();
                    mapPureDigital.put("recordStatus", recordStatus);
                    mapPureDigital.put("paymentTime", paymentTime);
                    mapPureDigital.put("outTradeNo", outTradeNo);
                    payConsumeRecordMapper.updateComsumeRecord(mapPureDigital);
                    // 更改ConsumeTrade
                    Integer orderStateId = 3;
                    String closeTime = DateUtils.getNowDateToString();
                    Map<String, Object> mapDigital = new HashMap<>();
                    mapDigital.put("orderStateId", orderStateId);
                    mapDigital.put("paymentTime", paymentTime);
                    mapDigital.put("closeTime", closeTime);
                    mapDigital.put("outTradeNo", outTradeNo);
                    payConsumeTradeBackMapper.updateConsumeTrade(mapDigital);


                    // 发送feign,提交下单通知
                    Map<String, Object> messageMap = new HashMap<String, Object>();
                    // 支付号
                    messageMap.put("orderId", outTradeNo);
                    messageMap.put("paymentTime", paymentTime);
                    // 发送下单通知
                    orderServiceClient.orderToInform(messageMap);
                } else {
                    log.info("订单号或订单金额不符");
                }
            }
        }
    }

    /**
     * 如果是积分订单就 修改用户积分
     *
     * @param orderId
     */
    public void updateUserJIfen(String orderId) {
        Integer amountAotalIntegral = null;
        //积分商品 0  是  1 否
        Integer integralGoods = 1;
        //是纯数字
        RecordDTO queryGoodsPara = null;
        if (MathUtil.isInteger(orderId)) {
            queryGoodsPara = payConsumeTradeBackMapper.queryRecordPriceAndNameByTradeId(orderId);
        //不是纯数字
        } else {
            queryGoodsPara = payConsumeTradeBackMapper.queryRecordPriceAndName(orderId);
        }
        if (null == queryGoodsPara) {
            return;
        }
        GoodsDTO queryGoodsPara2 = payConsumeTradeBackMapper.queryGoodsPriceAndName(queryGoodsPara.getOrderId(), queryGoodsPara.getBuyerId());
        if (null == queryGoodsPara2 && null != queryGoodsPara2.getTotalIntegral()) {
            return;
        }
        //获取积分商品订单的积分
        Integer totalIntegral = queryGoodsPara2.getTotalIntegral();
        //积分商品 0  是  1 否
        integralGoods = queryGoodsPara2.getIntegralGoods();
        amountAotalIntegral = totalIntegral;
        if (null != integralGoods && integralGoods == 0) {
            //获取用户积分
            Integer userIntegral = usermsServiceClient.getMemberIntegralByUserId(queryGoodsPara.getBuyerId());
            if (null != userIntegral && amountAotalIntegral <= userIntegral) {
                //扣除积分
                //修改会员积分和会员等级
                MemberMessageListIntegralDTO updateMemberMessageListIntegral = new MemberMessageListIntegralDTO();
                ArrayList<String> list = new ArrayList<>();
                list.add(queryGoodsPara.getBuyerId());
                updateMemberMessageListIntegral.setUserIdList(list);
                updateMemberMessageListIntegral.setIntegral(amountAotalIntegral);
                //1是增加积分 0 是扣除积分
                updateMemberMessageListIntegral.setType(CommonType.commonStatus.NO.getCode());
                usermsServiceClient.updateMemberIntegral(updateMemberMessageListIntegral);
                //添加积分修改记录
                Map<String, Object> pointParam = new HashMap<>();
                pointParam.put("PointsLogType", CommonType.pointsLogType.CONSUME.getCode());
                pointParam.put("ClassId", CommonType.pointType.POINTS_EXCHANGE_GOODS.getCode());
                pointParam.put("PointsLogTime", new Date());
                pointParam.put("PointsLogDesc", CommonType.pointType.POINTS_EXCHANGE_GOODS.getDesc());
                pointParam.put("PointsLogFlag", "reg");
                pointParam.put("UserId", queryGoodsPara.getBuyerId());
                pointParam.put("PointsLogPoints", amountAotalIntegral);
                pointParam.put("PointsLogOperate", CommonType.pointType.POINTS_EXCHANGE_GOODS.getDesc());
                usermsServiceClient.insertPointLogByMap(pointParam);
            }
        }
    }

    /**
     * 判断微信小程序支付的订单是否为虚拟订单或自提订单，并为虚拟订单生成兑换码。
     *
     * @param orderIdIsNum 包含订单ID和其他信息的映射表。
     * @return 返回一个映射表，其中包含虚拟商品标识、订单提取方式和配送方式等信息。
     */
    public Map<String, Integer> virtualWxExchangeCode(Map<String, String> orderIdIsNum) {
        // 初始化变量，用于存储虚拟商品标识、订单ID、兑换码URL、兑换码、订单提取方式和配送方式等信息。
        Integer virtualGoods = 1;
        String orderId = null;
        String redeemCodeUrl = null;
        String code = null;
        Integer orderIsShopTake = 2;
        // 是否同城配送2-否 1-是
        Integer isDelivery = null;
        // 创建一个映射表，用于存储判断结果。
        Map<String, Integer> judgeOrderMap = new HashMap<>();
        try {
            // 调用服务检查订单是否为虚拟订单。
            ResponseResult res = orderServiceClient.isVirtualOrder(orderIdIsNum);
            if (null != res.getData()) {
                // 从返回的数据中提取订单信息。
                Map<String, Object> map = (Map<String, Object>) res.getData();
                virtualGoods = Integer.valueOf(map.get("virtualGoods").toString());
                orderId = (String) map.get("orderId");
                orderIsShopTake = Integer.valueOf(map.get("orderIsShopTake").toString());
                isDelivery = Integer.valueOf(map.get("isDelivery").toString());
            }

            // 如果订单是虚拟订单或自提订单，则生成兑换码。
            // 虚拟订单 0 是 1 否
            if (null != virtualGoods && 0 == virtualGoods || null != orderIsShopTake && 1 == orderIsShopTake) {
                // 生成兑换码的唯一标识。
                SimpleDateFormat df = new SimpleDateFormat("ddHHmmss");
                String timeStamp = df.format(new Date());
                String temp = String.valueOf(Math.round((Math.random() + 1) * 1000));
                code = temp + timeStamp;
                // 生成兑换码的二维码并上传至OSS。
                byte[] b = QrCodeUtil.createQRCode(150, 150, code);
                String encode = Base64Utils.encode2(b);
                OssUploadUpdateParamDTO param = new OssUploadUpdateParamDTO();
                param.setBase64Str(encode);
                param.setStreamName(".png");
                ResponseResult responseResult = thirdPartyServiceClient.uploadToOssBase64(param);
                // 保存兑换码信息到数据库。
                redeemCodeUrl = (String) responseResult.getData();
                Map<String, Object> param2 = new HashMap<>();
                param2.put("order", orderId);
                param2.put("redeemCode", code);
                param2.put("redeemCodeUrl", redeemCodeUrl);
                orderServiceClient.addRedeemCode(param2);
            }

            // 将判断结果存入映射表中返回。
            judgeOrderMap.put("virtualGoods", virtualGoods);
            judgeOrderMap.put("orderIsShopTake", orderIsShopTake);
            judgeOrderMap.put("isDelivery", isDelivery);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return judgeOrderMap;
    }


    @Override
    public ResponseResult alipayQuery(Map<String, Object> param) {
        try {
            //加载支付配置
            this.initAliPayConfig();
            String orderId = (String) param.get("orderId");
            // 创建支付宝客户端
            AlipayClient alipayClient = this.getAlipayClient();

            // 构造请求参数以调用接口
            AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
            AlipayTradeQueryModel model = new AlipayTradeQueryModel();

            // 设置订单支付时传入的商户订单号
            model.setOutTradeNo(orderId);
            // 设置查询选项
            List<String> queryOptions = new ArrayList<String>();
            queryOptions.add("trade_settle_info");
            model.setQueryOptions(queryOptions);
            request.setBizModel(model);
            AlipayTradeQueryResponse response = alipayClient.certificateExecute(request);
            System.out.println(response.getBody());

            if (response.isSuccess()) {
                //支付宝订单状态 查询成功 解析返回的参数
                String body = response.getBody();
                Map<String, Object> map = JSON.parseObject(body, Map.class);
                Map<String, Object> alipayTradeQueryResponse = JSON.parseObject(map.get("alipay_trade_query_response").toString(), Map.class);
                if ((alipayTradeQueryResponse.get("sub_msg") != null && "交易不存在".equals(alipayTradeQueryResponse.get("sub_msg").toString())) ||
                        (alipayTradeQueryResponse.get("trade_status") != null && "WAIT_BUYER_PAY".equals(alipayTradeQueryResponse.get("trade_status").toString()))) {
                    log.info("----支付宝支付订单查询 订单不存在或未支付----");
                    return ResponseResult.fail(BizCodeEnume.ORDER_NOT_EXIST);
                } else if ("TRADE_SUCCESS".equals(alipayTradeQueryResponse.get("trade_status"))) {
                    log.info("----支付宝支付订单查询 支付成功----");
                    //订单支付成功 封装参数返回
                    Map<String, Object> result = new HashMap<>();
                    // 商户订单号
                    result.put("outTradeNo", alipayTradeQueryResponse.get("out_trade_no"));
                    // 支付宝交易号
                    result.put("tradeNo", alipayTradeQueryResponse.get("trade_no"));
                    // 交易订单金额
                    result.put("totalAmount", alipayTradeQueryResponse.get("total_amount"));
                    // 卖家支付宝用户号
                    result.put("sellerId", ALiPayConfig.sellerId);
                    return ResponseResult.success(result);
                } else {
                    log.info("----支付宝支付订单查询 支付失败----");
                    return ResponseResult.fail(BizCodeEnume.ORDER_NOT_EXIST);
                }
            } else {
                log.info("----支付宝支付订单查询 调用失败----");
                return ResponseResult.fail(BizCodeEnume.ORDER_NOT_EXIST);
            }
        } catch (AlipayApiException e) {
            log.info("e-------" + e);
            return ResponseResult.fail(BizCodeEnume.ORDER_NOT_EXIST);
        }
    }

    @Override
    public ResponseResult alipayCompensate(Map<String, Object> param) {
        // 从参数映射中获取订单号，并转换为字符串格式
        String outTradeNo = param.get("outTradeNo").toString();
        // 从参数映射中获取交易号，并转换为字符串格式
        String tradeNo = param.get("tradeNo").toString();
        // 从参数映射中获取总金额，并转换为字符串格式
        String totalAmount = param.get("totalAmount").toString();
        // 从参数映射中获取卖家ID，并转换为字符串格式
        String sellerId = param.get("sellerId").toString();


        // 更改销量
        List<WapGoodsSaleSumDTO> wapGood = orderServiceClient.queryGoodsByOrderId(outTradeNo);
        goodServiceClient.updateGoodsSalenum(wapGood);
        // 订单号是纯数字
        if (MathUtil.isInteger(outTradeNo)) {
            //进入回调纯数字订单逻辑处理
            orderIsInteger(outTradeNo, totalAmount, sellerId, tradeNo);
        } else {
            //进入非纯数字订单逻辑处理
            orderIsNotInteger(outTradeNo, totalAmount, sellerId, tradeNo);
        }
        return ResponseResult.success();
    }

    @Override
    public ResponseResult balanceWithdraw(BalanceWithdrawDTO balanceWithdrawDTO) {
        // 通过token获取用户信息
        UserInfoVO userInfo = ParseTokenUtils.getUserInfoByToken(balanceWithdrawDTO.getToken());
        // 如果用户信息为空，表示token非法，返回失败结果
        if (userInfo == null) {
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_ILLEGAL);
        }
        // 校验用户余额是否满足提现
        BigDecimal userBalanceMoney = payUserResourceMapper.queryBalanceMoney(userInfo.getUserId());
        if (userBalanceMoney.compareTo(balanceWithdrawDTO.getMoney()) < 0) {
            return ResponseResult.fail(BizCodeEnume.INSUFFICIENT_ACCOUNT_BALANCE.getCode(), BizCodeEnume.INSUFFICIENT_ACCOUNT_BALANCE.getMsg());
        }
        // 验证支付密码
        String password = payUserBaseMapper.isPasswd(userInfo.getUserId());
        if (!PasswordUtils.BCryptPasswordMatches(balanceWithdrawDTO.getPasswd(), password)) {
            return ResponseResult.fail(BizCodeEnume.PAYMENT_PASSWORD_ERROR);
        }
        //加载支付配置
        this.initAliPayConfig();
        // 创建支付宝客户端
        try {
            AlipayClient alipayClient = this.getAlipayClient();
            // 构造请求参数以调用接口
            AlipayFundTransUniTransferRequest request = new AlipayFundTransUniTransferRequest();
            AlipayFundTransUniTransferModel model = new AlipayFundTransUniTransferModel();
            // 生成充值单号
            String orderNo = "W_ALI_" + DateUtils.getTimeStamp() + DateUtils.randomLengthByTwo();
            // 设置商家侧唯一订单号
            model.setOutBizNo(orderNo);
            // 设置订单总金额
            model.setTransAmount(balanceWithdrawDTO.getMoney().toString());
            // 设置描述特定的业务场景
            model.setBizScene("DIRECT_TRANSFER");
            // 设置业务产品码
            model.setProductCode("TRANS_ACCOUNT_NO_PWD");
            // 设置转账业务的标题
            model.setOrderTitle("余额提现");
            // 设置收款方信息
            Participant payeeInfo = new Participant();
            payeeInfo.setName(balanceWithdrawDTO.getName());
            payeeInfo.setIdentity(balanceWithdrawDTO.getUserMobile());
            payeeInfo.setIdentityType("ALIPAY_LOGON_ID");
            model.setPayeeInfo(payeeInfo);
            // 设置业务备注
            model.setRemark("余额提现");
            request.setBizModel(model);
            AlipayFundTransUniTransferResponse response = alipayClient.certificateExecute(request);
            System.out.println(response.getBody());
            if (response.isSuccess()) {
                // 修改用户余额
                payUserResourceMapper.updateMoney(userInfo.getUserId(), balanceWithdrawDTO.getMoney());
                System.out.println("调用成功");
                //插入我的余额明细
                Map<Object, Object> assetDetailMap = new HashMap<>();
                assetDetailMap.put("assetDirection", CommonType.balanceRechargeOrderStart.ALI_PAY.getDesc() + "：" + balanceWithdrawDTO.getName());
                assetDetailMap.put("payType", CommonType.payType.PAY_TYPE_BALANCE.getCode());
                assetDetailMap.put("userId", userInfo.getUserId());
                assetDetailMap.put("detailType", CommonType.detailType.WITHDRAWAL_OF_BALANCE.getCode());
                assetDetailMap.put("assetType", CommonType.assetType.EXPENDITURE.getCode());
                assetDetailMap.put("assetAmount", balanceWithdrawDTO.getMoney());
                assetDetailMap.put("orderId", orderNo);
                payAssetDetailsMapper.insertDetail(assetDetailMap);

                return ResponseResult.success();
            } else {
                String body = response.getBody();
                Map<String, Object> map = JSON.parseObject(body, Map.class);
                Map<String, Object> alipayTradeQueryResponse = JSON.parseObject(map.get("alipay_fund_trans_uni_transfer_response").toString(), Map.class);

                System.out.println("调用失败");
                return ResponseResult.fail(alipayTradeQueryResponse.get("sub_msg").toString(), " ");
            }

        } catch (AlipayApiException e) {
            throw new RuntimeException(e);
        }
    }
}
