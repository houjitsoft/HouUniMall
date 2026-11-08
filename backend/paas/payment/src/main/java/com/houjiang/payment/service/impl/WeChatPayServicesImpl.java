package com.houjiang.payment.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.wxpay.sdk.WXPayUtil;
import com.houjiang.commons.config.WXConfigUtil;
import com.houjiang.commons.config.WXPayConfig;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.constant.CommonType;
import com.houjiang.commons.dto.base.MemberMessageListIntegralDTO;
import com.houjiang.commons.dto.oos.OssUploadUpdateParamDTO;
import com.houjiang.commons.dto.order.WapGoodsSaleSumDTO;
import com.houjiang.commons.enums.CustomerTypeEnum;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.*;
import com.houjiang.commons.util.json.JsonUtils;
import com.houjiang.commons.vo.order.OrderBalanceRechargeVO;
import com.houjiang.commons.vo.order.OrderBaseVO;
import com.houjiang.commons.vo.third.ConsumePointsReqVO;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.payment.dto.BalanceWithdrawDTO;
import com.houjiang.payment.dto.PayConsumeRecordDTO;
import com.houjiang.payment.dto.QueryGoodsParaDTO;
import com.houjiang.payment.dto.QueryRecordParaDTO;
import com.houjiang.payment.feign.*;
import com.houjiang.payment.mapper.*;
import com.houjiang.payment.service.WeChatPayServices;
import com.houjiang.payment.utils.WeChatUtil;
import io.seata.core.context.RootContext;
import io.seata.core.exception.TransactionException;
import io.seata.spring.annotation.GlobalTransactional;
import io.seata.tm.api.GlobalTransactionContext;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Amos
 */
@Service
public class WeChatPayServicesImpl implements WeChatPayServices {

    final static Logger log = LoggerFactory.getLogger(WeChatPayServicesImpl.class);

    @Resource
    private PayConfigMapper payConfigMapper;

    @Autowired
    private OrderServiceClient orderServiceClient;

    @Autowired
    private ActivityServiceClient activityServiceClient;

    @Resource
    private PayUserResourceMapper payUserResourceMapper;

    @Autowired
    private GoodServiceClient goodServiceClient;

    @Autowired
    private UsermsServiceClient usermsServiceClient;

    @Autowired
    private ThirdPartyServiceClient thirdPartyServiceClient;

    @Resource
    private PayConsumeTradeMapper payConsumeTradeMapper;

    @Resource
    private PayConsumeRecordMapper payConsumeRecordMapper;

    @Resource
    private PayFxAccountServiceImpl payFxAccountService;

    @Resource
    private PayAssetDetailsMapper payAssetDetailsMapper;

    @Resource
    private PayUserBaseMapper payUserBaseMapper;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 初始化 阿里云支付参数
     */
    public void initWXPayConfig() {
        //微信 pc  h5
        // 初始化支付方式标识
        String paymentMode = "00";

        // 通过支付方式标识查询支付配置信息
        Map<String, Object> outMap = payConfigMapper.selectPayConfig(paymentMode);

        // 设置微信支付的API密钥
        WXPayConfig.API_KEY = outMap.get("appAPIKey").toString();
        // 设置微信支付的App Secret
        WXPayConfig.APP_SECRET = outMap.get("appSecret").toString();
        // 设置微信支付的商户号
        WXPayConfig.MCH_ID = outMap.get("appMCHId").toString();
        // 设置微信支付的App ID
        WXPayConfig.APP_ID = outMap.get("appID").toString();
        // 设置微信支付的异步通知URL
        WXPayConfig.NOTIFY_URL = outMap.get("notifyUrl").toString();
        // 设置微信支付的回调URL
        WXPayConfig.REDIRECT_URL = outMap.get("redirectUrl").toString();
        // 设置微信支付的PC端扫码支付路径
        WXPayConfig.weChatWapPath = outMap.get("weChatPcPath").toString();


        //微信app
        // 从outMap中获取配置信息，并初始化WXConfigUtil的静态变量
        // 这里将appAPIKey、appSecret、appMCHId、appID和notifyUrl的值分别赋给WXConfigUtil的对应静态变量
        // 这些配置信息用于微信支付和其他微信相关功能的集成
        WXConfigUtil.KEY = outMap.get("appAPIKey").toString();
        WXConfigUtil.APP_SECRET = outMap.get("appSecret").toString();
        WXConfigUtil.MCH_ID = outMap.get("appMCHId").toString();
        WXConfigUtil.APP_ID = outMap.get("appID").toString();
        WXConfigUtil.NOTIFY_URL_APP = outMap.get("notifyUrl").toString();

        //微信小程序
        // 根据支付方式代码查询支付配置信息
        String paymentMode2 = "02";
        // 通过支付方式代码查询支付配置，并将结果存储在一个Map中
        Map<String, Object> outMap2 = payConfigMapper.selectPayConfig(paymentMode2);
        // 从查询结果中提取小程序的APPID，并赋值给WXPayConfig的相应静态字段
        WXPayConfig.Applet_APP_ID = outMap2.get("appID").toString();
        // 从查询结果中提取小程序的APPSECRET，并赋值给WXPayConfig的相应静态字段
        WXPayConfig.Applet_APP_SECRET = outMap2.get("appSecret").toString();
    }
    /**
     * 初始化微信小程序支付配置信息。
     * 通过传入的店铺ID，从数据库中查询并设置相应的支付配置参数，用于后续的微信支付操作。
     *
     * @param shopId 店铺ID，用于查询该店铺的支付配置信息。
     */
    /**
     * 初始化 品牌店铺支付信息
     */
    public void initWXShopPayConfig(Integer shopId) {
        // 根据店铺ID查询支付配置信息
        Map<String, String> stringStringMap = payConfigMapper.selectYfPayConfigShop(shopId);
        // 设置微信小程序的APP ID
        WXPayConfig.Applet_APP_ID = stringStringMap.get("Applet_APP_ID");
        // 设置微信小程序的APP SECRET
        WXPayConfig.Applet_APP_SECRET = stringStringMap.get("Applet_APP_SECRET");
        // 设置API密钥
        WXPayConfig.API_KEY = stringStringMap.get("API_KEY");
        // 设置商户号
        WXPayConfig.MCH_ID = stringStringMap.get("MCH_ID");
        // 设置微信H5支付的跳转路径
        WXPayConfig.weChatWapPath = stringStringMap.get("weChatWapPath");
        // 设置支付通知URL
        WXPayConfig.NOTIFY_URL = stringStringMap.get("NOTIFY_URL");
    }

    @Override
    @GlobalTransactional(rollbackFor = Exception.class)
    public String notifyWeiXinPay(HttpServletRequest request, HttpServletResponse response) {
        log.info("------支付成功，进入回调notifyWeiXinPay------");
        try {
            InputStream inStream = request.getInputStream();
            ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = inStream.read(buffer)) != -1) {
                outSteam.write(buffer, 0, len);
            }
            String resultxml = outSteam.toString("utf-8");
            log.info("resultxml:" + resultxml);
            @SuppressWarnings("unchecked")
            Map<String, String> params = PayCommonUtil.doXMLParse(resultxml);
            outSteam.close();
            inStream.close();
            // 根据支付单号 查询当前订单 店铺是否是品牌店铺 如果是用品牌店铺 微信值参数验签
            // 如果是是品牌店铺的 一个支付单号 只会对应一个店铺id
            // 查询当前店铺是否是品牌 店铺  如果是返回shopId
            Integer shopId = payUserResourceMapper.queryshopIdByOutTradeNo(params.get("out_trade_no"));
            if (shopId != null) {
                //初始化品牌店铺支付参数
                initWXShopPayConfig(shopId);
            } else {
                //查询支付配置信息
                initWXPayConfig();
            }

            Map<String, String> return_data = new HashMap<String, String>();
            if (!PayCommonUtil.isTenpaySign(params)) {
                // 支付失败
                log.info("------支付失败------");
                return_data.put("return_code", "FAIL");
                return_data.put("return_msg", "return_code不正确");
                log.info("return_data：" + StringUtils.GetMapToXML(return_data));
                return StringUtils.GetMapToXML(return_data);
            } else {
                log.info("===============付款成功==============");
                String out_trade_no = params.get("out_trade_no");
                log.info("-----out_trade_no-----" + out_trade_no);
                // 获取wx传来的订单价格
                String total_fee = params.get("total_fee");
                log.info("-----total_fee-----" + total_fee);
                // 微信支付分配的商户号
                String mch_id = params.get("mch_id");
                log.info("-----mch_id-----" + mch_id);
                // 微信支付订单号
                String transaction_id = params.get("transaction_id");
                log.info("-----transaction_id-----" + transaction_id);
                usermsServiceClient.setVKInfo(out_trade_no);
                // 更改销量
                List<WapGoodsSaleSumDTO> wapGood = orderServiceClient.queryGoodsByOrderId(out_trade_no);
                goodServiceClient.updateGoodsSalenum(wapGood);
                Map<String, String> orderIdIsNum = new HashMap<>();
                //虚拟订单 0是 1否
                Integer virtualGoods = null;
                //门店订单 1 是 2否
                Integer orderIsShopTake = null;
                // 是否同城配送2-否 1-是
                Integer isDelivery = null;
                // 判断纯数字订单，实际支付订单
                if (PayToolUtil.isInteger(out_trade_no)) {
                    QueryRecordParaDTO queryGoodsPara = payConsumeTradeMapper.queryRecordPriceAndName(out_trade_no);

                    if (null != queryGoodsPara && !queryGoodsPara.getOrderStateId().equals(6)) {
                        String orderId = queryGoodsPara.getOrderId();
                        String recordMoney = PayToolUtil.getMoney(queryGoodsPara.getTradePaymentAmount());
                        if (out_trade_no.equals(orderId) && total_fee.equals(recordMoney)
                                && mch_id.equals(WXPayConfig.MCH_ID)) {
                            log.info("----判断成功----");
                            payConsumeTradeMapper.updateConsumeTradePaymentMoney(out_trade_no, 3);
                            orderIdIsNum.put("orderId", out_trade_no);
                            //订单号是数字
                            orderIdIsNum.put("isNum", "0");

                            //获取非数字订单
                            String recordPayorder = orderServiceClient.queryAssetInfo(out_trade_no, "").getFirst().getPaymentNumber();
                            //修改拼团的支付状态
                            Map<String, Object> pinTuanMap = new HashMap<>();
                            pinTuanMap.put("orderId", recordPayorder);
                            activityServiceClient.updatePinTuanPayStart(pinTuanMap);

                            //判断是否是积分商品订单 扣除积分
                            updateUserJIfen(out_trade_no);
                            //判断是否是虚拟订单
                            Map<String, Integer> judgeOrderMap = this.virtualWxExchangeCode(orderIdIsNum);
                            virtualGoods = judgeOrderMap.get("virtualGoods");
                            isDelivery = judgeOrderMap.get("isDelivery");
                            Map<Object, Object> map = new HashMap<Object, Object>();
                            Integer payCode = 3;
                            // 待发货
                            Integer orderStatus = 3;
                            //门店订单 0 是 1 否 且不是同城配送
                            if (null != orderIsShopTake && CommonType.orderType.IS_SHOP_TAKE.getCode().equals(orderIsShopTake) && CommonType.DeliveryOrderStart.not_is_delivery.getCode().equals(isDelivery)) {
                                //如果是门店订单就将订单状态修改为 已发货
                                orderStatus = 4;
                            }
                            //虚拟订单 0 是 1 否
                            if (null != virtualGoods && 0 == virtualGoods) {
                                //如果是虚拟订单就将订单状态修改为 已发货
                                orderStatus = 4;
                            }
                            // 支付方式
                            Integer paymentId = 1;
                            // 支付方式名称
                            String paymentName = "在线支付";
                            // 支付时间
                            String paymentTime = DateUtils.getTime();
                            // 第三方支付号
                            String paymentOtherNumber = transaction_id;
                            String orderFrom = RedisUtil.get(HouConfig.ORDERFORM + out_trade_no);
                            map.put("payCode", payCode);
                            map.put("orderFrom", orderFrom);
                            map.put("paymentId", paymentId);
                            // 商户订单号
                            map.put("paymentNumber", out_trade_no);
                            map.put("orderStatus", orderStatus);
                            map.put("paymentName", paymentName);
                            map.put("paymentTime", paymentTime);
                            map.put("paymentOtherNumber", paymentOtherNumber);
                            boolean a = orderServiceClient.HFiveUpdateOrderBaseByPayment(map);
                            // 更改支付表状态ConsumeRecord
                            Integer recordStatus = 3;
                            Map<String, Object> recordMap = new HashMap<>();
                            recordMap.put("recordStatus", recordStatus);
                            recordMap.put("paymentTime", paymentTime);
                            recordMap.put("outTradeNo", out_trade_no);
                            boolean updateConsumeRecord = payConsumeRecordMapper.updateComsumeRecordByPureDigital(recordMap);
                            // 更改ConsumeTrade
                            Integer orderStateId = 3;
                            // 支付方式
                            Integer paymentChannelId = 4;
                            String closeTime = DateUtils.getTime();
                            boolean updateConsumeTrade = payConsumeTradeMapper.updateConsumeTradeByPureDigital(orderStateId,
                                    paymentTime, closeTime, out_trade_no, paymentChannelId);

                            // 发送feign,提交下单通知
                            Map<String, Object> messageMap = new HashMap<String, Object>();
                            // 支付号
                            messageMap.put("orderId", out_trade_no);
                            messageMap.put("paymentTime", paymentTime);
                            // 发送下单通知
                            orderServiceClient.orderToInform(messageMap);
                            return_data.put("return_code", "SUCCESS");
                            return_data.put("return_msg", "OK");
                            return StringUtils.GetMapToXML(return_data);
                        } else {
                            log.info("订单号或订单金额不符！");
                            return_data.put("return_code", "FAIL");
                            return_data.put("return_msg", "订单号或订单金额不符！");
                            return StringUtils.GetMapToXML(return_data);
                        }
                    } else {
                        return_data.put("return_code", "SUCCESS");
                        return_data.put("return_msg", "OK");
                        return StringUtils.GetMapToXML(return_data);
                    }
                } else {
                    orderIdIsNum.put("orderId", out_trade_no);
                    //订单号不是数字
                    orderIdIsNum.put("isNum", "1");
                    //判断是否是虚拟订单
                    Map<String, Integer> judgeOrderMap = this.virtualWxExchangeCode(orderIdIsNum);
                    virtualGoods = judgeOrderMap.get("virtualGoods");
                    // 支付订单号
                    orderIsShopTake = judgeOrderMap.get("orderIsShopTake");
                    List<PayConsumeRecordDTO> payConsumeRecord = payConsumeRecordMapper.queryOrderNo(out_trade_no);
                    List<Integer> allRecordStatus = payConsumeRecordMapper.queryAllRecordStatus(out_trade_no);
                    BigDecimal money = new BigDecimal(0);

                    for (int i = 0; i < payConsumeRecord.size(); i++) {
                        money = money.add(payConsumeRecord.get(i).getRecordMoney());

                        PayConsumeRecordDTO dto = payConsumeRecord.get(i);
                    }
                    try {
                        OrderBaseVO orderBaseVO = orderServiceClient.queryOrderInfoFegin(out_trade_no);

                        // 消费嘉币
                        if (CustomerTypeEnum.isYongjia(orderBaseVO.getCustomerType())) {
                            ConsumePointsReqVO req = new ConsumePointsReqVO();

                            req.setUserId(Integer.valueOf(orderBaseVO.getBuyerUserId()));
                            req.setShopId(orderBaseVO.getOutShopId());
                            req.setCurrencyId(1);
                            req.setCurrencyName("嘉币");
                            req.setOrderid(orderBaseVO.getOrderId());
                            req.setBusinessUuid(UUIDUtil.getUUID());
                            req.setConsumeTitle("订单兑换");
                            usermsServiceClient.consumePoints(req);
                        }
                        //消费劲豆
                        if (CustomerTypeEnum.isPifa(orderBaseVO.getCustomerType())) {
                            ConsumePointsReqVO req = new ConsumePointsReqVO();

                            req.setUserId(Integer.valueOf(orderBaseVO.getBuyerUserId()));
                            req.setShopId(orderBaseVO.getShopId());
                            req.setCurrencyId(2);
                            req.setCurrencyName("劲豆");
                            req.setOrderid(orderBaseVO.getOrderId());
                            req.setBusinessUuid(UUIDUtil.getUUID());
                            req.setConsumeTitle("订单兑换");
                            usermsServiceClient.consumePoints(req);
                        }
                    } catch (Exception e) {
                        log.info("嘉币、劲豆支付失败！", e);
                    }

                    if (allRecordStatus.contains(1)) {
                        String orderId = payConsumeRecord.get(0).getOrderId();
                        String userName = payConsumeRecord.get(0).getUserNickname();
                        String recordMoney = PayToolUtil.getMoney(money.toString());
                        if (out_trade_no.equals(orderId) && total_fee.equals(recordMoney)
                                && mch_id.equals(WXPayConfig.MCH_ID)) {
                            payConsumeTradeMapper.updateConsumeTradePaymentMoney(out_trade_no, 3);
                            Map<Object, Object> map = new HashMap<Object, Object>();
                            Integer payCode = 3;
                            // 待发货
                            Integer orderStatus = 3;

                            //修改拼团的支付状态
                            Map<String, Object> pinTuanMap = new HashMap<>();
                            pinTuanMap.put("orderId", out_trade_no);
                            activityServiceClient.updatePinTuanPayStart(pinTuanMap);

                            //判断是否是积分商品订单 扣除积分
                            updateUserJIfen(out_trade_no);
                            //门店订单 0 是 1 否
                            if (null != orderIsShopTake && 1 == orderIsShopTake) {
                                //如果是门店订单就将订单状态修改为 已发货
                                orderStatus = 4;
                            }
                            //虚拟订单 0 是 1 否
                            if (null != virtualGoods && 0 == virtualGoods) {
                                //如果是虚拟订单就将订单状态修改为 已发货
                                orderStatus = 4;
                            }
                            // 支付方式
                            Integer paymentId = 1;
                            // 支付方式名称
                            String paymentName = "在线支付";
                            // 支付时间
                            String paymentTime = DateUtils.getTime();
                            // 第三方支付号
                            String paymentOtherNumber = transaction_id;
                            String orderFrom = RedisUtil.get(HouConfig.ORDERFORM + out_trade_no);
                            map.put("payCode", payCode);
                            map.put("orderFrom", orderFrom);
                            map.put("paymentId", paymentId);
                            // 商户订单号
                            map.put("paymentNumber", out_trade_no);
                            map.put("orderStatus", orderStatus);
                            map.put("paymentName", paymentName);
                            map.put("paymentTime", paymentTime);
                            map.put("paymentOtherNumber", paymentOtherNumber);
                            boolean updatePayment = orderServiceClient.HFiveUpdateOrderBaseByPayment(map);
                            // 更改支付表状态ConsumeRecord
                            Integer recordStatus = 3;
                            Map<String, Object> recordMap = new HashMap<>();
                            recordMap.put("recordStatus", recordStatus);
                            recordMap.put("paymentTime", paymentTime);
                            recordMap.put("outTradeNo", out_trade_no);
                            boolean updateConsumeRecord = payConsumeRecordMapper.updateComsumeRecord(recordMap);
                            // 更改ConsumeTrade
                            Integer orderStateId = 3;
                            // 支付方式
                            Integer paymentChannelId = 4;
                            String closeTime = DateUtils.getTime();
                            boolean updateConsumeTrade = payConsumeTradeMapper.updateConsumeTrade(orderStateId, paymentTime,
                                    closeTime, out_trade_no, paymentChannelId);

                            // 库存传值
                            List<String> orderIdList = payConsumeRecordMapper.queryPayOrderId(orderId);
                            String orderList = Url.listToString2(orderIdList);
                            // 发送feign,提交下单通知
                            Map<String, Object> messageMap = new HashMap<String, Object>();
                            // 支付号
                            messageMap.put("orderId", out_trade_no);
                            messageMap.put("paymentTime", paymentTime);
                            // 发送下单通知
                            orderServiceClient.orderToInform(messageMap);
                            return_data.put("return_code", "SUCCESS");
                            return_data.put("return_msg", "OK");
                            return StringUtils.GetMapToXML(return_data);
                        } else {
                            log.info("订单号或订单金额不符！");
                            return_data.put("return_code", "FAIL");
                            return_data.put("return_msg", "订单号或订单金额不符！");
                            return StringUtils.GetMapToXML(return_data);
                        }
                    } else {
                        return_data.put("return_code", "SUCCESS");
                        return_data.put("return_msg", "OK");
                        return StringUtils.GetMapToXML(return_data);
                    }
                }
            }
        } catch (Exception e) {
            log.info("notifyWeiXinPayErrorResults:" + e.getMessage());
            // 回滚
            try {
                GlobalTransactionContext.reload(RootContext.getXID()).rollback();
            } catch (TransactionException e1) {
                return e1.getMessage().toString();
            }
            return e.getMessage().toString();
        }
    }

    /**
     * 获取微信网页授权的code链接。
     * 该方法用于构建微信授权页面的URL，引导用户授权后重定向到指定的redirect_uri，并在URL中附带授权code。
     *
     * @param paraMap 包含请求参数的映射，其中必须包含redirect_uri参数。
     * @return 返回一个映射，其中包含code、data和desc三个字段。
     * - code字段固定为"1"，表示操作成功。
     * - data字段为构建的授权页面URL。
     * - desc字段为空，保留字段，用于未来可能的描述信息。
     */
    @Override
    public Map<String, Object> getCode(Map<String, String> paraMap) {
        // 从请求参数中获取重定向URI
        String redirect_uri = paraMap.get("redirect_uri");

        // 初始化支付方式为04，用于从配置中获取微信公众号的APPID和APPSECRET
        String paymentMode1 = "04";
        // 从配置数据库中查询支付方式为04的配置信息
        Map<String, Object> outMap1 = payConfigMapper.selectPayConfig(paymentMode1);
        // 从查询结果中提取APPID和APPSECRET，并设置到全局配置中
        WXPayConfig.GZ_APP_ID = outMap1.get("appID").toString();
        WXPayConfig.GZ_APP_SECRET = outMap1.get("appSecret").toString();

        // 初始化返回结果的映射
        Map<String, Object> result = new HashMap<String, Object>();
        // 构建微信授权页面的URL
        // 第一步：用户同意授权，获取code
        String strUrl = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + WXPayConfig.GZ_APP_ID +
                "&redirect_uri=" + redirect_uri +
                "&response_type=code" +
                "&scope=snsapi_base" +
                "&state=" + UUID.randomUUID() + "#wechat_redirect";
        // 设置返回结果的code、data和desc字段
        result.put("code", "1");
        result.put("data", strUrl);
        result.put("desc", "");
        // 返回构建好的结果映射
        return result;
    }


    /**
     * 获取微信网页OpenId
     */
    @Override
    public Map<String, Object> getOpenId(Map<String, String> paraMap) {
        String code = paraMap.get("code");
        //获取配置
        String paymentMode1 = "04";
        Map<String, Object> outMap1 = payConfigMapper.selectPayConfig(paymentMode1);
        WXPayConfig.GZ_APP_ID = outMap1.get("appID").toString();
        WXPayConfig.GZ_APP_SECRET = outMap1.get("appSecret").toString();
        Map<String, Object> result = new HashMap<String, Object>();
        // 第一步：用户同意授权，获取code
        String strUrl = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=" + WXPayConfig.GZ_APP_ID +
                "&secret=" + WXPayConfig.GZ_APP_SECRET + "&code=" + code + "&grant_type=authorization_code";
        // 从Redis中获取字符串结果
        String resultStr = restTemplate.getForObject(strUrl, String.class);

        // 将从Redis获取的字符串从ISO_8859_1编码转换为UTF-8编码，以确保正确解码
        resultStr = new String(resultStr.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);

        // 将字符串解析为JSONObject，以便访问JSON中的特定字段
        JSONObject result1 = JSONObject.parseObject(resultStr);
        log.info("result1==={}", result1);

        // 从JSONObject中提取openid和session_key
        String openid = result1.getString("openid");
        String session_key = result1.getString("session_key");

        // 将openid和session_key添加到结果Map中，以供后续使用
        result.put("openid", openid);
        result.put("session_key", session_key);

        // 设置结果Map的其他字段，如code、data和desc
        result.put("code", "1");
        result.put("data", openid);
        result.put("desc", "");

        // 返回结果Map
        return result;

    }

    /**
     * 如果是积分订单就 修改用户积分
     */
    public void updateUserJIfen(String orderId) {
        Integer amountAotalIntegral = null;
        //积分商品 0  是  1 否
        Integer integralGoods = 1;
        //是纯数字
        QueryRecordParaDTO queryGoodsPara = null;
        if (PayToolUtil.isInteger(orderId)) {
            queryGoodsPara = payConsumeTradeMapper.queryRecordPriceAndName(orderId);
        } else {
            //不是纯数字
            queryGoodsPara = payConsumeTradeMapper.queryRecordPriceAndName(orderId);
        }
        if (null == queryGoodsPara) {
            return;
        }
        QueryGoodsParaDTO queryGoodsParaDTO2 = payConsumeTradeMapper.queryGoodsOrderAndUser(queryGoodsPara.getOrderId(), queryGoodsPara.getBuyerId());
        if (null == queryGoodsParaDTO2 && null != queryGoodsParaDTO2.getTotalIntegral()) {
            return;
        }
        //获取积分商品订单的积分
        Integer totalIntegral = queryGoodsParaDTO2.getTotalIntegral();
        //积分商品 0  是  1 否
        integralGoods = queryGoodsParaDTO2.getIntegralGoods();
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
                updateMemberMessageListIntegral.setType(0);
                usermsServiceClient.updateMemberIntegral(updateMemberMessageListIntegral);
                //添加积分修改记录
                Map<String, Object> pointParam = new HashMap<>();
                pointParam.put("PointsLogType", CommonType.pointsLogType.CONSUME.getCode());
                pointParam.put("ClassId", CommonType.pointType.POINTS_EXCHANGE_VOUCHER.getCode());
                pointParam.put("PointsLogTime", DateUtils.getTime());
                pointParam.put("PointsLogDesc", CommonType.pointType.POINTS_EXCHANGE_GOODS.getDesc());
                pointParam.put("PointsLogFlag", "reg");
                pointParam.put("UserId", queryGoodsPara.getBuyerId());
                pointParam.put("PointsLogPoints", amountAotalIntegral);
                pointParam.put("PointsLogOperate", CommonType.pointType.POINTS_EXCHANGE_GOODS.getDesc());
                pointParam.put("OrderId", orderId);
                usermsServiceClient.insertPointLogByMap(pointParam);
            }
        }
    }

    /**
     * 微信小程序支付回调判断该订单是否是 虚拟 / 自提 订单 如果是 生成兑换码
     *
     * @param orderIdIsNum
     */
    public Map<String, Integer> virtualWxExchangeCode(Map<String, String> orderIdIsNum) {
        Integer virtualGoods = 1;
        String orderId = null;
        String redeemCodeUrl = null;
        String code = null;
        Integer orderIsShopTake = 2;
        Map<String, Integer> judgeOrderMap = new HashMap<>();
        try {
            ResponseResult res = orderServiceClient.isVirtualOrder(orderIdIsNum);
            if (null != res.getData()) {
                Map<String, Object> map = (Map<String, Object>) res.getData();
                virtualGoods = (Integer) map.get("virtualGoods");
                orderId = (String) map.get("orderId");
                orderIsShopTake = (Integer) map.get("orderIsShopTake");
            }
            //虚拟订单 0 是 1 否
            if (null != virtualGoods && 0 == virtualGoods || null != orderIsShopTake && 1 == orderIsShopTake) {
                // 创建一个SimpleDateFormat实例，用于格式化日期时间为"ddHHmmss"格式
                SimpleDateFormat df = new SimpleDateFormat("ddHHmmss");

                // 获取当前日期时间的"ddHHmmss"格式字符串
                String timeStamp = df.format(new Date());

                // 生成一个1位随机整数（0或1）乘以1000的字符串表示的随机数
                String temp = String.valueOf(Math.round((Math.random() + 1) * 1000));

                // 将随机数和时间戳拼接成唯一的code
                code = temp + timeStamp;

                // 调用QrCodeUtil的GenerateQRCode方法生成二维码，传入code作为参数
                ResponseResult resultResp = QrCodeUtil.GenerateQRCode(code);

                // 将生成的二维码上传到OSS，并获取二维码的URL
                redeemCodeUrl = String.valueOf(thirdPartyServiceClient.uploadToOssBase64((OssUploadUpdateParamDTO) resultResp.getData()).getData());

                // 初始化一个Map，用于存储订单号、兑换码和兑换码URL
                Map<String, Object> param = new HashMap<>();

                // 将订单号、兑换码和兑换码URL添加到param Map中
                param.put("order", orderId);
                param.put("redeemCode", code);
                param.put("redeemCodeUrl", redeemCodeUrl);

                //为虚拟拟订单添加兑换码
                orderServiceClient.addRedeemCode(param);
            }

            judgeOrderMap.put("virtualGoods", virtualGoods);
            judgeOrderMap.put("orderIsShopTake", orderIsShopTake);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return judgeOrderMap;
    }

    /**
     * 处理微信支付回调通知的接口方法。
     * 该方法用于接收微信支付完成后的回调通知，验证支付结果，并根据支付结果进行相应的业务处理。
     *
     * @param request  用于接收微信支付回调通知的HttpServletRequest对象。
     * @param response 用于发送响应到微信支付的HttpServletResponse对象。
     * @return 返回处理结果的字符串，微信支付根据该结果确定后续操作。
     */
    @Override
    public String plusNotifyWeiXinPay(HttpServletRequest request, HttpServletResponse response) {
        // 日志记录回调开始
        log.info("------支付成功，进入回调lx------");
        log.info("------支付成功，进入回调notifyWeiXinPay------");
        try {
            // 读取请求的输入流，用于接收微信支付的通知数据
            InputStream inStream = request.getInputStream();
            ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = inStream.read(buffer)) != -1) {
                outSteam.write(buffer, 0, len);
            }
            // 将接收的数据转换为字符串
            String resultxml = new String(outSteam.toByteArray(), "utf-8");
            log.info("resultxml:" + resultxml);
            /** 解析XML格式的通知数据 */
            @SuppressWarnings("unchecked")
            Map<String, String> params = PayCommonUtil.doXMLParse(resultxml);
            // 关闭流资源
            outSteam.close();
            inStream.close();
            Map<String, String> return_data = new HashMap<String, String>();
            // 验证通知数据的签名，确保数据的完整性和真实性
            if (!PayCommonUtil.isTenpaySign(params)) {
                // 签名验证失败，表示支付失败
                log.info("------支付失败------");
                // 设置返回数据，表示支付失败
                return_data.put("return_code", "FAIL");
                return_data.put("return_msg", "return_code不正确");
                log.info("return_data：" + StringUtils.GetMapToXML(return_data));
                return StringUtils.GetMapToXML(return_data);
            } else {
                // 签名验证成功，表示支付成功
                log.info("===============付款成功==============");
                // 从通知数据中获取订单相关的信息
                String out_trade_no = params.get("out_trade_no");
                String attach = params.get("attach");
                Map map1 = JsonUtils.string2Obj(attach, Map.class);
                String total_fee = params.get("total_fee");
                String mch_id = params.get("mch_id");
                String transaction_id = params.get("transaction_id");
                // 插入plus会员信息
                map1.put("plusOrder", out_trade_no);
                map1.put("type", CommonType.plusPayType.PLUS_WX_PAY.getCode());
                ResponseResult plusmemberopen = usermsServiceClient.plusmemberopen(map1);
                if (plusmemberopen.getCode() == 1 || plusmemberopen.getCode() == 3) {
                    // 业务处理成功，返回给微信支付成功的通知
                    return_data.put("return_code", "SUCCESS");
                    return_data.put("return_msg", "OK");
                    return StringUtils.GetMapToXML(return_data);
                } else {
                    // 业务处理失败，返回给微信支付失败的通知
                    return_data.put("return_code", "FAIL");
                    return_data.put("return_msg", "开通失败");
                    return StringUtils.GetMapToXML(return_data);
                }
            }
        } catch (Exception e) {
            // 记录异常信息
            log.info("notifyWeiXinPayErrorResults:" + e.getMessage());
        }
        // 如果出现异常或其他情况，返回"OK"，表示接收通知成功，但处理结果未知
        return "OK";
    }

    @Override
    public String balanceRechargeNotifyWeiXinPay(HttpServletRequest request, HttpServletResponse response) {
        // 日志记录回调开始
        log.info("------支付成功，进入回调------");
        log.info("------支付成功，进入回调balanceRechargeNotifyWeiXinPay------");
        try {
            // 读取请求的输入流，用于接收微信支付的通知数据
            InputStream inStream = request.getInputStream();
            ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = inStream.read(buffer)) != -1) {
                outSteam.write(buffer, 0, len);
            }
            // 将接收的数据转换为字符串
            String resultxml = new String(outSteam.toByteArray(), "utf-8");
            log.info("resultxml:" + resultxml);
            /** 解析XML格式的通知数据 */
            @SuppressWarnings("unchecked")
            Map<String, String> params = PayCommonUtil.doXMLParse(resultxml);
            // 关闭流资源
            outSteam.close();
            inStream.close();
            Map<String, String> return_data = new HashMap<String, String>();
            // 验证通知数据的签名，确保数据的完整性和真实性
            if (!PayCommonUtil.isTenpaySign(params)) {
                // 签名验证失败，表示支付失败
                log.info("------支付失败------");
                // 设置返回数据，表示支付失败
                return_data.put("return_code", "FAIL");
                return_data.put("return_msg", "return_code不正确");
                log.info("return_data：" + StringUtils.GetMapToXML(return_data));
                return StringUtils.GetMapToXML(return_data);
            } else {
                // 签名验证成功，表示支付成功
                log.info("===============付款成功==============");
                // 从通知数据中获取订单相关的信息
                String out_trade_no = params.get("out_trade_no");
                String transaction_id = params.get("transaction_id");
                Map<Object, Object> map = new HashMap<>();
                // 插入plus会员信息
                map.put("rechargeOrderId", out_trade_no);
                map.put("rechargeOtherNumber", transaction_id);
                map.put("payCode", CommonType.balanceRechargeOrderStart.WX_PAY.getCode());
                map.put("rechargeOrderStart", CommonType.balanceRechargeOrderStart.FINISH.getCode());

                //插入记录信息
                ResponseResult recharge = orderServiceClient.updateBalanceRechargeOrder(map);


                if (recharge.getCode() == 1 || recharge.getCode() == 3) {
                    // 业务处理成功，返回给微信支付成功的通知
                    return_data.put("return_code", "SUCCESS");
                    return_data.put("return_msg", "OK");
                    return StringUtils.GetMapToXML(return_data);
                } else {
                    // 业务处理失败，返回给微信支付失败的通知
                    return_data.put("return_code", "FAIL");
                    return_data.put("return_msg", "支付失败");
                    return StringUtils.GetMapToXML(return_data);
                }
            }
        } catch (Exception e) {
            // 记录异常信息
            log.info("balanceRechargeNotifyWeiXinPay:" + e.getMessage());
        }
        // 如果出现异常或其他情况，返回"OK"，表示接收通知成功，但处理结果未知
        return "OK";
    }

    /**
     * 根据传入的参数查询微信支付状态。
     *
     * @param map 包含查询所需信息的键值对，其中必须包含token和可选的orderId。
     * @return 返回查询结果，成功或失败时包含相应的状态码和消息。
     */
    @Override
    public ResponseResult queryWxPayStatus(Map<Object, Object> map) {
        ResponseResult rr = new ResponseResult();
        // 从参数map中获取token
        String token = (String) map.get("token");
        // 检查token是否存在于Redis中
        boolean isExistToken = RedisUtil.exists(HouConfig.HOU_USER_TOKEN_USER + token);
        if (isExistToken) {
            try {
                // 使用token获取用户信息
                UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(token);
                String userId = ui.getUserId();
                String orderId = (String) map.get("orderId");
                // 如果订单号为空，查询PLUS会员的支付状态
                //订单号为空 是查询的plus 会员
                if (StringUtils.isEmpty(orderId)) {
                    // 根据用户ID查询PLUS会员支付状态
                    Integer status = usermsServiceClient.queryStatusByPlus(userId);
                    // 如果状态为1（支付成功），返回成功结果
                    if (null != status && status == 1) {
                        //状态更改成功！
                        return ResponseResult.success(BizCodeEnume.UPDATE_SUCCEEDED);
                    } else {
                        // 状态不是支付成功，返回失败结果
                        //状态未更改成功！
                        return ResponseResult.fail(BizCodeEnume.UPDATE_ERROR);
                    }
                }
                // 如果订单号是整数，查询该订单的支付状态
                if (PayToolUtil.isInteger(orderId)) {
                    // 根据订单号和用户ID查询支付状态
                    Integer status = payConsumeTradeMapper.queryStatusByInteger(orderId, userId);
                    // 如果状态为1（支付成功），返回失败结果
                    if (null != status && status == 1) {
                        //状态未更改成功！
                        return ResponseResult.fail(BizCodeEnume.UPDATE_ERROR);
                    } else {
                        // 状态不是支付成功，返回成功结果
                        //状态更改成功！
                        return ResponseResult.success(BizCodeEnume.UPDATE_SUCCEEDED);
                    }
                } else {
                    // 查询非整数形式的订单支付状态
                    List<Integer> status = payConsumeRecordMapper.queryStatus(orderId, userId);
                    // 如果查询到状态列表不为空
                    if (!CollectionUtils.isEmpty(status)) {
                        // 遍历状态列表，如果存在状态为1（支付成功），返回失败结果
                        for (Integer integer : status) {
                            if (integer == 1) {
                                //状态未更改成功！
                                return ResponseResult.fail(BizCodeEnume.UPDATE_ERROR);
                            } else {
                                // 状态不是支付成功，返回成功结果
                                //状态更改成功！
                                return ResponseResult.success(BizCodeEnume.UPDATE_SUCCEEDED);
                            }
                        }
                    }
                    // 如果查询结果为空，返回未获取到信息的失败结果
                    return ResponseResult.fail(BizCodeEnume.NO_INFORMATION_OBTAINED);
                }
            } catch (Exception e) {
                // 查询过程中发生异常，返回失败结果并附带异常信息
                return ResponseResult.fail(e.getMessage());
            }
        } else {
            // token无效，返回失败结果
            return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
        }
    }

    @Override
    public ResponseResult queryWxPayplus(Map<Object, Object> map) {
        // 初始化响应结果对象
        ResponseResult rr = new ResponseResult();
        // 从请求参数中获取token
        String token = (String) map.get("token");
        // 检查token在Redis中是否存在
        boolean isExistToken = RedisUtil.exists(HouConfig.HOU_USER_TOKEN_USER + token);
        if (isExistToken) {
            // 通过token获取用户信息
            UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(token);
            // 获取用户ID
            String userId = ui.getUserId();
            // 从请求参数中获取plusOrder，转换为字符串类型
            String plusOrder = String.valueOf(map.get("plusOrder"));
            // 根据用户ID和plusOrder查询支付状态
            Integer queryplusorder = usermsServiceClient.queryplusorder(userId, plusOrder);
            // 如果支付状态为1，表示支付成功，返回相应的成功消息
            if (queryplusorder == 1) {
                return ResponseResult.success(BizCodeEnume.PAYMENT_SUCCESSFUL.getMsg());
            } else {
                // 如果支付状态不为1，表示支付失败，返回相应的失败消息
                return ResponseResult.success(BizCodeEnume.PAYMENT_FAILED.getMsg());
            }
        }
        // 如果token不存在，返回token无效的错误消息
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);

    }

    /**
     * 轮询 余额充值 微信支付后状态
     *
     * @param map
     * @return
     */
    @Override
    public ResponseResult queryWxPayBalanceRecharge(Map<Object, Object> map) {
        // 初始化响应结果对象
        ResponseResult rr = new ResponseResult();
        // 从请求参数中获取token
        String token = (String) map.get("token");
        // 检查token在Redis中是否存在
        boolean isExistToken = RedisUtil.exists(HouConfig.HOU_USER_TOKEN_USER + token);
        if (isExistToken) {
            // 通过token获取用户信息
            UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(token);
            // 获取用户ID
            String userId = ui.getUserId();
            String rechargeOrderId = String.valueOf(map.get("rechargeOrderId"));
            OrderBalanceRechargeVO orderBalanceRechargeVO = orderServiceClient.queryRechargeOrderById(rechargeOrderId);
            // 如果支付状态为1，表示支付成功，返回相应的成功消息
            if (null != orderBalanceRechargeVO && CommonType.balanceRechargeOrderStart.FINISH.getCode().equals(orderBalanceRechargeVO.getRechargeOrderStart())) {
                return ResponseResult.success(BizCodeEnume.PAYMENT_SUCCESSFUL.getMsg(), 1);
            } else {
                // 如果支付状态不为1，表示支付失败，返回相应的失败消息
                return ResponseResult.success(BizCodeEnume.PAYMENT_FAILED.getMsg(), 2);
            }
        }
        // 如果token不存在，返回token无效的错误消息
        return ResponseResult.fail(BizCodeEnume.TOKEN_IS_INVALID);
    }

    /**
     * 查询微信支付订单状态
     *
     * @param param 查询参数，包含订单号(orderId)
     * @return 返回支付结果，如果支付成功，则包含订单相关信息；如果支付失败，则返回失败信息。
     */
    @Override
    public ResponseResult queryWxPay(Map<String, Object> param) {
        try {
            WXAppletPayServicesImpl basePayServiceImpl = new WXAppletPayServicesImpl();
            basePayServiceImpl.initWXPayConfig();

            // 构建查询订单的参数Map
            Map<String, String> paraMap = new HashMap<String, String>();
            // 设置必填参数
            paraMap.put("appid", WXPayConfig.APP_ID);
            paraMap.put("mch_id", WXPayConfig.MCH_ID);
            paraMap.put("nonce_str", PayToolUtil.create_nonce_str());
            //订单号
            paraMap.put("out_trade_no", param.get("orderId").toString());
            // 生成签名
            String sign = WXPayUtil.generateSignature(paraMap, WXPayConfig.API_KEY);
            paraMap.put("sign", sign);

            // 将参数Map转换为XML格式，用于微信支付接口请求
            //将所有参数(map)转xml格式
            String xml = WXPayUtil.mapToXml(paraMap);

            // 发送查询订单的POST请求
            //发送post请求"统一下单接口"返回预支付id:prepay_id
            String xmlStr = HttpsUtil.sendPost(WXPayConfig.QUERY_ORDER_START_URL, xml);

            // 将返回的XML数据转换为Map
            Map<String, String> map = WXPayUtil.xmlToMap(xmlStr);
            System.out.println(map);
            log.info("----map-----" + map);

            // 判断支付状态
            if (("订单不存在".equalsIgnoreCase(map.get("err_code_des"))) || "NOTPAY".equalsIgnoreCase(map.get("trade_state"))) {
                log.info("----微信支付订单查询 订单不存在或未支付----");
                return ResponseResult.fail(BizCodeEnume.ORDER_NOT_EXIST);
            }
            if (map != null && "SUCCESS".equalsIgnoreCase(map.get("trade_state"))) {
                log.info("----微信支付订单查询 支付成功----");
                // 构建支付成功的结果Map
                // 订单支付成功 封装参数返回
                Map<String, Object> result = new HashMap<>();
                result.put("outTradeNo", map.get("out_trade_no"));
                result.put("totalFee", map.get("total_fee"));
                result.put("mchId", map.get("mch_id"));
                result.put("transactionId", map.get("transaction_id"));
                // 返回支付成功的响应
                return ResponseResult.success(result);
            } else {
                log.info("----微信支付订单查询 支付失败----");
                // 返回支付失败的响应
                return ResponseResult.fail(BizCodeEnume.ORDER_NOT_EXIST);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.fail(BizCodeEnume.ORDER_NOT_EXIST);
        }
    }

    @Override
    public ResponseResult wxPayCompensate(Map<String, Object> params) {
        try {
            String out_trade_no = params.get("outTradeNo").toString();
            // 获取wx传来的订单价格
            String total_fee = params.get("totalFee").toString();
            // 微信支付分配的商户号
            String mch_id = params.get("mchId").toString();
            // 微信支付订单号
            String transaction_id = params.get("transactionId").toString();
            Map<String, String> return_data = new HashMap<String, String>();

            // 更改销量
            List<WapGoodsSaleSumDTO> wapGood = orderServiceClient.queryGoodsByOrderId(out_trade_no);
            goodServiceClient.updateGoodsSalenum(wapGood);
            Map<String, String> orderIdIsNum = new HashMap<>();
            //虚拟订单 0是 1否
            Integer virtualGoods = null;
            //门店订单 1 是 2否
            Integer orderIsShopTake = null;
            // 判断纯数字订单，实际支付订单
            if (PayToolUtil.isInteger(out_trade_no)) {
                QueryRecordParaDTO queryGoodsPara = payConsumeTradeMapper.queryRecordPriceAndName(out_trade_no);

                if (null != queryGoodsPara && !queryGoodsPara.getOrderStateId().equals(6)) {
                    String orderId = queryGoodsPara.getOrderId();
                    String recordMoney = PayToolUtil.getMoney(queryGoodsPara.getTradePaymentAmount());
                    if (out_trade_no.equals(orderId) && total_fee.equals(recordMoney)
                            && mch_id.equals(WXPayConfig.MCH_ID)) {
                        log.info("----判断成功----");
                        payConsumeTradeMapper.updateConsumeTradePaymentMoney(out_trade_no, 3);
                        orderIdIsNum.put("orderId", out_trade_no);
                        //订单号是数字
                        orderIdIsNum.put("isNum", "0");
                        //判断是否是积分商品订单 扣除积分
                        updateUserJIfen(out_trade_no);
                        //判断是否是虚拟订单
                        Map<String, Integer> judgeOrderMap = this.virtualWxExchangeCode(orderIdIsNum);
                        virtualGoods = judgeOrderMap.get("virtualGoods");
                        orderIsShopTake = judgeOrderMap.get("orderIsShopTake");
                        Map<Object, Object> map = new HashMap<Object, Object>();
                        Integer payCode = 3;
                        // 待发货
                        Integer orderStatus = 3;
                        //门店订单 0 是 1 否
                        if (null != orderIsShopTake && 1 == orderIsShopTake) {
                            //如果是门店订单就将订单状态修改为 已发货
                            orderStatus = 4;
                        }
                        //虚拟订单 0 是 1 否
                        if (null != virtualGoods && 0 == virtualGoods) {
                            //如果是虚拟订单就将订单状态修改为 已发货
                            orderStatus = 4;
                        }
                        // 支付方式
                        Integer paymentId = 1;
                        // 支付方式名称
                        String paymentName = "在线支付";
                        // 支付时间
                        String paymentTime = DateUtils.getTime();
                        // 第三方支付号
                        String paymentOtherNumber = transaction_id;
                        String orderFrom = RedisUtil.get(HouConfig.ORDERFORM + out_trade_no);
                        map.put("payCode", payCode);
                        map.put("orderFrom", orderFrom);
                        map.put("paymentId", paymentId);
                        // 商户订单号
                        map.put("paymentNumber", out_trade_no);
                        map.put("orderStatus", orderStatus);
                        map.put("paymentName", paymentName);
                        map.put("paymentTime", paymentTime);
                        map.put("paymentOtherNumber", paymentOtherNumber);
                        boolean a = orderServiceClient.HFiveUpdateOrderBaseByPayment(map);
                        // 更改支付表状态ConsumeRecord
                        Integer recordStatus = 3;
                        Map<String, Object> recordMap = new HashMap<>();
                        recordMap.put("recordStatus", recordStatus);
                        recordMap.put("paymentTime", paymentTime);
                        recordMap.put("outTradeNo", out_trade_no);
                        boolean updateConsumeRecord = payConsumeRecordMapper.updateComsumeRecordByPureDigital(recordMap);
                        // 更改ConsumeTrade
                        Integer orderStateId = 3;
                        // 支付方式
                        Integer paymentChannelId = 4;
                        String closeTime = DateUtils.getTime();
                        boolean updateConsumeTrade = payConsumeTradeMapper.updateConsumeTradeByPureDigital(orderStateId,
                                paymentTime, closeTime, out_trade_no, paymentChannelId);

                        // 发送feign,提交下单通知
                        Map<String, Object> messageMap = new HashMap<String, Object>();
                        // 支付号
                        messageMap.put("orderId", out_trade_no);
                        messageMap.put("paymentTime", paymentTime);
                        // 发送下单通知
                        orderServiceClient.orderToInform(messageMap);
                        return_data.put("return_code", "SUCCESS");
                        return_data.put("return_msg", "OK");
                        return ResponseResult.success(return_data);
                    } else {
                        log.info("订单号或订单金额不符！");
                        return_data.put("return_code", "FAIL");
                        return_data.put("return_msg", "订单号或订单金额不符！");
                        return ResponseResult.fail(return_data);
                    }
                } else {
                    return_data.put("return_code", "SUCCESS");
                    return_data.put("return_msg", "OK");
                    return ResponseResult.success(return_data);
                }
            } else {
                orderIdIsNum.put("orderId", out_trade_no);
                //订单号不是数字
                orderIdIsNum.put("isNum", "1");
                //判断是否是虚拟订单
                Map<String, Integer> judgeOrderMap = this.virtualWxExchangeCode(orderIdIsNum);
                virtualGoods = judgeOrderMap.get("virtualGoods");
                // 支付订单号
                orderIsShopTake = judgeOrderMap.get("orderIsShopTake");
                List<PayConsumeRecordDTO> payConsumeRecord = payConsumeRecordMapper.queryOrderNo(out_trade_no);
                List<Integer> allRecordStatus = payConsumeRecordMapper.queryAllRecordStatus(out_trade_no);
                BigDecimal money = new BigDecimal(0);
                for (int i = 0; i < payConsumeRecord.size(); i++) {
                    money = money.add(payConsumeRecord.get(i).getRecordMoney());
                }
                if (allRecordStatus.contains(1)) {
                    String orderId = payConsumeRecord.get(0).getOrderId();
                    String userName = payConsumeRecord.get(0).getUserNickname();
                    String recordMoney = PayToolUtil.getMoney(money.toString());
                    if (out_trade_no.equals(orderId) && total_fee.equals(recordMoney)
                            && mch_id.equals(WXPayConfig.MCH_ID)) {
                        payConsumeTradeMapper.updateConsumeTradePaymentMoney(out_trade_no, 3);
                        Map<Object, Object> map = new HashMap<Object, Object>();
                        Integer payCode = 3;
                        // 待发货
                        Integer orderStatus = 3;
                        //判断是否是积分商品订单 扣除积分
                        updateUserJIfen(out_trade_no);
                        //门店订单 0 是 1 否
                        if (null != orderIsShopTake && 1 == orderIsShopTake) {
                            //如果是门店订单就将订单状态修改为 已发货
                            orderStatus = 4;
                        }
                        //虚拟订单 0 是 1 否
                        if (null != virtualGoods && 0 == virtualGoods) {
                            //如果是虚拟订单就将订单状态修改为 已发货
                            orderStatus = 4;
                        }
                        // 支付方式
                        Integer paymentId = 1;
                        // 支付方式名称
                        String paymentName = "在线支付";
                        // 支付时间
                        String paymentTime = DateUtils.getTime();
                        // 第三方支付号
                        String paymentOtherNumber = transaction_id;
                        String orderFrom = RedisUtil.get(HouConfig.ORDERFORM + out_trade_no);
                        map.put("payCode", payCode);
                        map.put("orderFrom", orderFrom);
                        map.put("paymentId", paymentId);
                        // 商户订单号
                        map.put("paymentNumber", out_trade_no);
                        map.put("orderStatus", orderStatus);
                        map.put("paymentName", paymentName);
                        map.put("paymentTime", paymentTime);
                        map.put("paymentOtherNumber", paymentOtherNumber);
                        boolean updatePayment = orderServiceClient.HFiveUpdateOrderBaseByPayment(map);
                        // 更改支付表状态ConsumeRecord
                        Integer recordStatus = 3;
                        Map<String, Object> recordMap = new HashMap<>();
                        recordMap.put("recordStatus", recordStatus);
                        recordMap.put("paymentTime", paymentTime);
                        recordMap.put("outTradeNo", out_trade_no);
                        boolean updateConsumeRecord = payConsumeRecordMapper.updateComsumeRecord(recordMap);
                        // 更改ConsumeTrade
                        Integer orderStateId = 3;
                        // 支付方式
                        Integer paymentChannelId = 4;
                        String closeTime = DateUtils.getTime();
                        boolean updateConsumeTrade = payConsumeTradeMapper.updateConsumeTrade(orderStateId, paymentTime,
                                closeTime, out_trade_no, paymentChannelId);

                        // 库存传值
                        List<String> orderIdList = payConsumeRecordMapper.queryPayOrderId(orderId);
                        String orderList = Url.listToString2(orderIdList);
                        // 发送feign,提交下单通知
                        Map<String, Object> messageMap = new HashMap<String, Object>();
                        // 支付号
                        messageMap.put("orderId", out_trade_no);
                        messageMap.put("paymentTime", paymentTime);
                        // 发送下单通知
                        orderServiceClient.orderToInform(messageMap);
                        return_data.put("return_code", "SUCCESS");
                        return_data.put("return_msg", "OK");
                        return ResponseResult.success(return_data);
                    } else {
                        log.info("订单号或订单金额不符！");
                        return_data.put("return_code", "FAIL");
                        return_data.put("return_msg", "订单号或订单金额不符！");
                        return ResponseResult.fail(return_data);
                    }
                } else {
                    return_data.put("return_code", "SUCCESS");
                    return_data.put("return_msg", "OK");
                    return ResponseResult.success(return_data);
                }
            }
        } catch (Exception e) {
            log.info("notifyWeiXinPayErrorResults:" + e.getMessage());
            // 回滚
            try {
                GlobalTransactionContext.reload(RootContext.getXID()).rollback();
            } catch (TransactionException e1) {
                return ResponseResult.fail();
            }
            return ResponseResult.fail();
        }
    }

    @Override
    public String wechatPost(String requestBody) throws Exception {
        WXAppletPayServicesImpl basePayServiceImpl = new WXAppletPayServicesImpl();
        basePayServiceImpl.initWXPayConfig();

        // 解析XML
        Map<String, String> requestMap = parseXmlToMap(requestBody);

        // 处理关注事件
        if ("subscribe".equals(requestMap.get("Event"))) {
            String unionId = null;
            String openId = requestMap.get("FromUserName");
            System.out.println(WXPayConfig.GZ_APP_ID);
            System.out.println(WXPayConfig.GZ_APP_SECRET);
            String accessToken = WeChatUtil.getAccessToken(WXPayConfig.GZ_APP_ID, WXPayConfig.GZ_APP_SECRET);
            System.out.println(accessToken);
            if (accessToken != null) {
                System.out.println("accessToken: " + accessToken);
                System.out.println("openId: " + openId);
                String userInfoResponse = WeChatUtil.sendHttpRequest(accessToken, openId);
                unionId = parseUnionId(userInfoResponse);
                System.out.println("UnionId: " + unionId);
            }
            // 保存OpenId到数据库
            usermsServiceClient.insertOpenIdByUnionId(openId, unionId);

            // 返回欢迎消息
            return "<xml>" +
                    "<ToUserName><![CDATA[" + requestMap.get("FromUserName") + "]]></ToUserName>" +
                    "<FromUserName><![CDATA[" + requestMap.get("ToUserName") + "]]></FromUserName>" +
                    "<CreateTime>" + System.currentTimeMillis() / 1000 + "</CreateTime>" +
                    "<MsgType><![CDATA[text]]></MsgType>" +
                    "<Content><![CDATA[欢迎关注！]]></Content>" +
                    "</xml>";
        }

        // 其他事件处理...
        return "";
    }

    public Map<String, String> parseXmlToMap(String xmlString) throws Exception {
        Map<String, String> map = new HashMap<>();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(xmlString)));

        document.getDocumentElement().normalize();
        Element root = document.getDocumentElement();
        NodeList nodeList = root.getChildNodes();

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                map.put(element.getNodeName(), element.getTextContent());
            }
        }

        return map;
    }

    private static String parseUnionId(String response) {
        System.out.println("");
        System.out.println(response);
        System.out.println("");
        JSONObject result = JSONObject.parseObject(response);
        // 解析获取unionId的响应
        // 在实际开发中，可以使用JSON库来解析
        // 示例中直接手动解析，实际中应该更加严谨
        return result.getString("unionid");
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
        // 生成充值单号
        String orderNo = "W_WX_" + DateUtils.getTimeStamp() + DateUtils.randomLengthByTwo();
        // 标记提现是否开始
        Boolean tiXianStart = false;
        // 提现结果字符串
        String resStr = null;
        // 创建提现用Map集合
        Map<String, Object> tixianMap = new HashMap<>();
        // 设置提现金额（转换为分）
        tixianMap.put("transferAmount", balanceWithdrawDTO.getMoney().multiply(new BigDecimal(100)));
        // 设置用户OpenId
        tixianMap.put("openId", balanceWithdrawDTO.getOpenId());
        // 设置提现描述
        tixianMap.put("desc", "余额提现");
        // 调用微信转账接口
        Map<String, Object> map = payFxAccountService.weixinTransferBat(tixianMap);
        log.info("map+++++++:" + map);
//         检查微信转账结果
        if (null != map) {
            // 提现是否开始标记
            tiXianStart = (Boolean) map.get("start");
            // 提现结果字符串
            resStr = (String) map.get("resStr");
        }
        // 提现成功后更新用户余额及交易记录
        if (tiXianStart) {
            // 修改用户余额
            payUserResourceMapper.updateMoney(userInfo.getUserId(), balanceWithdrawDTO.getMoney());
            //插入我的余额明细
            Map<Object, Object> assetDetailMap = new HashMap<>();
            assetDetailMap.put("assetDirection", CommonType.balanceRechargeOrderStart.WX_PAY.getDesc() + ":" + userInfo.getUserName());
            assetDetailMap.put("payType", CommonType.payType.PAY_TYPE_BALANCE.getCode());
            assetDetailMap.put("userId", userInfo.getUserId());
            assetDetailMap.put("detailType", CommonType.detailType.WITHDRAWAL_OF_BALANCE.getCode());
            assetDetailMap.put("assetType", CommonType.assetType.EXPENDITURE.getCode());
            assetDetailMap.put("assetAmount", balanceWithdrawDTO.getMoney());
            assetDetailMap.put("orderId", orderNo);
            payAssetDetailsMapper.insertDetail(assetDetailMap);
            return ResponseResult.success();
        } else {
            return ResponseResult.fail(resStr);
        }
    }
}
