package com.houjiang.payment.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayUtil;
import com.jfinal.weixin.sdk.api.PaymentApi;
import com.houjiang.commons.config.WXConfigUtil;
import com.houjiang.commons.config.WXPayConfig;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.constant.CommonStatusEnum;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.PayToolUtil;
import com.houjiang.commons.util.RedisUtil;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.payment.dto.GoodsDTO;
import com.houjiang.payment.feign.OrderServiceClient;
import com.houjiang.payment.feign.UsermsServiceClient;
import com.houjiang.payment.mapper.PayConfigMapper;
import com.houjiang.payment.mapper.PayConsumeRecordMapper;
import com.houjiang.payment.mapper.PayConsumeTradeBackMapper;
import com.houjiang.payment.mapper.PayConsumeTradeMapper;
import com.houjiang.payment.service.PayStrategyHandleService;
import com.houjiang.payment.vo.RecordVO;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Amos
 * @date 2023/4/13 15:16
 */
@Service
public class WXPayAPPServicesImpl extends BasePayServiceImpl implements PayStrategyHandleService {

    private static PayConfigMapper payConfigMapper;

    private static PayConsumeTradeMapper payConsumeTradeMapper;

    private static PayConsumeTradeBackMapper payConsumeTradeBackMapper;

    private static PayConsumeRecordMapper payConsumeRecordMapper;

    private static UsermsServiceClient usermsServiceClient;

    private static OrderServiceClient orderServiceClient;

    /** 注入的时候，给类的 service 注入 */
    @Autowired
    public void setUsermsServiceClient(UsermsServiceClient usermsServiceClient) {

        WXPayAPPServicesImpl.usermsServiceClient = usermsServiceClient;
    }
    @Autowired
    public void setOrderServiceClient(OrderServiceClient orderServiceClient) {
        WXPayAPPServicesImpl.orderServiceClient = orderServiceClient;
    }
    /** 注入的时候，给类的 service 注入 */
    @Autowired
    public void setMapper(PayConfigMapper payConfigMapper, PayConsumeTradeMapper payConsumeTradeMapper,
                          PayConsumeTradeBackMapper payConsumeTradeBackMapper, PayConsumeRecordMapper payConsumeRecordMapper) {
        WXPayAPPServicesImpl.payConfigMapper = payConfigMapper;
        WXPayAPPServicesImpl.payConsumeTradeMapper = payConsumeTradeMapper;
        WXPayAPPServicesImpl.payConsumeTradeBackMapper = payConsumeTradeBackMapper;
        WXPayAPPServicesImpl.payConsumeRecordMapper = payConsumeRecordMapper;
    }

    final static Logger log = LoggerFactory.getLogger(WXPayAPPServicesImpl.class);

    @Override
    public ResponseResult userPayEntrance(Map<Object, Object> map, HttpServletRequest request, HttpServletResponse response) {
        //查询支付配置信息
        this.initWeChatPayConfig();
        String outTradeNo = (String) map.get("orderId");
        String token = (String) map.get("token");
        BigDecimal totalPrice = new BigDecimal(0);
        // 判断订单是否支付过
        ResponseResult responseResult = basePayServiceImpl.isPayOrder(outTradeNo);
        if(responseResult.getCode() != BizCodeEnume.ORDER_NOT_EXIST.getCode()){
            return responseResult;
        }
        // 来源
        Integer orderFromNum = map.get("platForm") == null ? 0 : Integer.parseInt(map.get("platForm").toString());
        // 订单来源存值
        RedisUtil.set(HouConfig.HOU_ORDERFORM + outTradeNo, orderFromNum.toString(), 86400);

        // 处理自营店铺
        if (!StringUtils.isEmpty(RedisUtil.get(HouConfig.HOU_ORDER_USER_TOKEN + outTradeNo))) {
            RedisUtil.remove(HouConfig.HOU_ORDER_USER_TOKEN + outTradeNo);
        }
        RedisUtil.set(HouConfig.HOU_ORDER_USER_TOKEN + outTradeNo, token, 86400);

        String goodsName = "订单编号" + outTradeNo;

        Integer amountAotalIntegral = null;
        //积分商品 0  是  1 否
        Integer integralGoods = 1;
        UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(token);
        String userId = ui.getUserId();

        // 查询商品名与商品价格
        if (PayToolUtil.isInteger(outTradeNo)) {
            GoodsDTO queryGoodsPara = payConsumeTradeMapper.queryGoodsPriceAndName(outTradeNo);
            // 总价
            totalPrice = queryGoodsPara.getTradePaymentAmount();

            GoodsDTO queryGoodsPara2 = payConsumeTradeBackMapper.queryGoodsPriceAndName(outTradeNo, userId);
            //获取积分商品订单的积分
            Integer totalIntegral = queryGoodsPara2.getTotalIntegral();
            //积分商品 0  是  1 否
            integralGoods = queryGoodsPara2.getIntegralGoods();
            amountAotalIntegral = totalIntegral;
        } else {
            List<RecordVO> rp = payConsumeRecordMapper.queryRecordParaList(outTradeNo);
            for (RecordVO recordVO : rp) {
                BigDecimal price = recordVO.getRecordMoney();
                totalPrice = totalPrice.add(price);
            }
            // 获取积分商品的积分
            String totalIntegral = RedisUtil
                    .get(HouConfig.HOU_USERINTEGRALYPAID + "--" + token + "--" + userId);
            if (null != totalIntegral) {
                //amountAotalIntegral 不为null 证明是积分订单
                amountAotalIntegral = Integer.valueOf(totalIntegral);
                //积分商品 0  是  1 否
                integralGoods = 0;
            }
        }
        if (null != integralGoods && integralGoods == 0) {
            //获取用户积分
            Integer userIntegral = usermsServiceClient.getMemberIntegralByUserId(userId);
            Integer frozenIntegra = usermsServiceClient.getMemberFrozenIntegraIntegralByUserId(userId);
            userIntegral = userIntegral - frozenIntegra;
            if(userIntegral < 0){
                userIntegral = 0;
            }
            if (null != userIntegral && amountAotalIntegral > userIntegral) {
                //账户积分不足
                return ResponseResult.fail(BizCodeEnume.PAY_INTEGRAL_ERROR);
            }
        }
        // 判断是否京东订单 且 是否当前支付是京东余额支付 且 余额是否够支付
        ResponseResult responseResult1 = orderServiceClient.queryJDBalanceIsEnough(outTradeNo, totalPrice);
        if(responseResult1.getCode() ==  CommonStatusEnum.SUCCESS.getCode() && responseResult1.getData().toString().equals("false")){
            // 京东账户余额不足！
            return ResponseResult.fail("京东账户余额不足！");
        }
        Map<String, String> params = new HashMap<String, String>();
        String sign = null;
        Map<String, String> returnMap = new HashMap<>();
        try {
            // 初始化微信支付配置工具类
            WXConfigUtil config = new WXConfigUtil();

            // 将总价转换为字符串格式，用于支付金额的处理
            String payAmount = String.valueOf(totalPrice);
            // 调用支付工具类处理支付金额，获取格式化后的支付金额
            String price = PayToolUtil.getMoney(payAmount);

            // 设置微信支付应用ID
            params.put("appid", config.getAppID());
            // 设置商户号
            params.put("mch_id", config.getMchID());
            // 生成随机字符串，用于签名
            params.put("nonce_str", PayToolUtil.create_nonce_str());
            // 设置商品描述
            params.put("body", goodsName);
            // 设置订单号
            params.put("out_trade_no", outTradeNo);
            // 设置订单总金额
            params.put("total_fee", price);

            // 设置终端IP地址
            params.put("spbill_create_ip", PayToolUtil.getIpAddr(request));
            // 设置通知URL
            params.put("notify_url", config.NOTIFY_URL_APP);
            // 设置交易类型为APP
            params.put("trade_type", "APP");

            // 生成签名
            sign = WXPayUtil.generateSignature(params, config.getKey());
            // 将签名添加到参数中
            params.put("sign", sign);

            // 记录支付参数信息
            log.info("param{}", JSON.toJSONString(params));
            // 调用微信支付API发起支付请求
//            String xmlResult = PaymentApi.pushOrder(params);
            // 初始化微信支付工具类
            WXPay wxpay = new WXPay(config);
            //使用官方API请求预付订单
            Map<String, String> wxResponse = wxpay.unifiedOrder(params);
            log.info("wxResponse{}", JSON.toJSONString(wxResponse));
            //获取返回码
            String returnCode = wxResponse.get("return_code");
            //若返回码为SUCCESS，则会返回一个result_code,再对该result_code进行判断
            if (returnCode.equals("SUCCESS")) {
                //主要返回以下5个参数(必须按照顺序，否则APP报错：-1)
                String resultCode = wxResponse.get("result_code");
                returnMap.put("appid", wxResponse.get("appid"));
                returnMap.put("noncestr", wxResponse.get("nonce_str"));
                returnMap.put("returnMsg", wxResponse.get("return_msg"));
                returnMap.put("errCodees", wxResponse.get("err_code_des"));
                //resultCode 为SUCCESS，才会返回prepay_id和trade_type
                if ("SUCCESS".equals(resultCode)) {
                    returnMap.put("package","Sign=WXPay");
                    returnMap.put("partnerid", wxResponse.get("mch_id"));
                    returnMap.put("prepayid", wxResponse.get("prepay_id"));
                    //单位为秒
                    returnMap.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));
                    Map<String, String> signMap = new HashMap<>();
                    signMap.put("appid", wxResponse.get("appid"));
                    signMap.put("noncestr", wxResponse.get("nonce_str"));
                    signMap.put("package","Sign=WXPay");
                    signMap.put("partnerid", wxResponse.get("mch_id"));
                    signMap.put("prepayid", wxResponse.get("prepay_id"));
                    signMap.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));
                    // 生成支付签名
                    String paySign = WXPayUtil.generateSignature(signMap, config.getKey());
                    returnMap.put("paySign",paySign);
                    //签名
                    returnMap.put("sign",sign);
                    //获取预支付交易回话标志
                    returnMap.put("trade_type", wxResponse.get("trade_type"));
                    return ResponseResult.success(returnMap);
                } else {
                    //此时返回没有预付订单的数据
                    return ResponseResult.success(returnMap);
                }
            } else {
                return ResponseResult.success(returnMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseResult.fail(BizCodeEnume.SYSTEM_ERROR.getCode(),BizCodeEnume.SYSTEM_ERROR.getMsg());
    }
}
