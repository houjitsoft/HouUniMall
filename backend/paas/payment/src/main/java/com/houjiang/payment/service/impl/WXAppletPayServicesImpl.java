package com.houjiang.payment.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.kit.StrKit;
import com.jfinal.weixin.sdk.api.PaymentApi;
import com.jfinal.weixin.sdk.kit.PaymentKit;
import com.jfinal.weixin.sdk.utils.JsonUtils;
import com.houjiang.commons.config.WXPayConfig;
import com.houjiang.commons.config.WXPayConfigShop;
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
import com.houjiang.payment.mapper.PayConsumeTradeMapper;
import com.houjiang.payment.service.PayConsumeTradeService;
import com.houjiang.payment.service.PayStrategyHandleService;
import com.houjiang.payment.vo.CallBackVO;
import com.houjiang.payment.vo.RecordVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Amos
 * @date 2023/4/13 15:16
 */
@Service
public class WXAppletPayServicesImpl extends BasePayServiceImpl implements PayStrategyHandleService {

    private static PayConfigMapper payConfigMapper;

    private static PayConsumeTradeMapper payConsumeTradeMapper;

    private static PayConsumeRecordMapper payConsumeRecordMapper;

    private static UsermsServiceClient usermsServiceClient;

    private static PayConsumeTradeService payConsumeTradeService;

    private static  RestTemplate restTemplate;

    private static OrderServiceClient orderServiceClient;

    /** 注入的时候，给类的 service 注入 */
    @Autowired
    public void setUsermsServiceClient(UsermsServiceClient usermsServiceClient, PayConsumeTradeService payConsumeTradeService, RestTemplate restTemplate) {

        WXAppletPayServicesImpl.usermsServiceClient = usermsServiceClient;
        WXAppletPayServicesImpl.payConsumeTradeService=payConsumeTradeService;
        WXAppletPayServicesImpl.restTemplate=restTemplate;
    }
    @Autowired
    public void setOrderServiceClient(OrderServiceClient orderServiceClient) {
        WXAppletPayServicesImpl.orderServiceClient = orderServiceClient;
    }
    /** 注入的时候，给类的 service 注入 */
    @Autowired
    public void setMapper(PayConfigMapper payConfigMapper, PayConsumeTradeMapper payConsumeTradeMapper,
                          PayConsumeRecordMapper payConsumeRecordMapper) {
        WXAppletPayServicesImpl.payConfigMapper = payConfigMapper;
        WXAppletPayServicesImpl.payConsumeTradeMapper = payConsumeTradeMapper;
        WXAppletPayServicesImpl.payConsumeRecordMapper = payConsumeRecordMapper;
    }

    final static Logger log = LoggerFactory.getLogger(WXAppletPayServicesImpl.class);

    /**
     * 初始化微信支付配置。
     */
    public void initWXPayConfig() {
        // 获取微信PC H5的支付配置
        this.initWeChatPayConfig();

        //微信小程序
        // 根据支付方式代码查询支付配置信息
        String paymentMode2 = "02";
        // 通过支付方式代码查询支付配置，并将结果存储在一个Map中
        Map<String, Object> outMap2 = payConfigMapper.selectPayConfig(paymentMode2);
        // 从查询结果中提取小程序的APPID，并赋值给WXPayConfig的相应静态字段
        WXPayConfig.Applet_APP_ID = outMap2.get("appID").toString();
        // 从查询结果中提取小程序的APPSECRET，并赋值给WXPayConfig的相应静态字段
        WXPayConfig.Applet_APP_SECRET = outMap2.get("appSecret").toString();

        // 获取微信公众号的支付配置
        String paymentMode1 = "04";
        Map<String, Object> outMap1 = payConfigMapper.selectPayConfig(paymentMode1);
        WXPayConfig.GZ_APP_ID = outMap1.get("appID").toString();
        WXPayConfig.GZ_APP_SECRET = outMap1.get("appSecret").toString();
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
        WXPayConfigShop.Applet_APP_ID = stringStringMap.get("Applet_APP_ID");
        // 设置微信小程序的APP SECRET
        WXPayConfigShop.Applet_APP_SECRET = stringStringMap.get("Applet_APP_SECRET");
        // 设置API密钥
        WXPayConfigShop.API_KEY = stringStringMap.get("API_KEY");
        // 设置商户号
        WXPayConfigShop.MCH_ID = stringStringMap.get("MCH_ID");
        // 设置微信H5支付的跳转路径
        WXPayConfigShop.weChatWapPath = stringStringMap.get("weChatWapPath");
        // 设置支付通知URL
        WXPayConfigShop.NOTIFY_URL = stringStringMap.get("NOTIFY_URL");
    }


    @Override
    public ResponseResult userPayEntrance(Map<Object, Object> map, HttpServletRequest request, HttpServletResponse response) {
        System.out.println(map);
        if (map.get("shopId") != null && map.get("shopId") != ""){
            //初始化品牌店铺支付参数
            initWXShopPayConfig(Integer.valueOf(map.get("shopId").toString()));
        }else{
            //查询支付配置信息
            this.initWXPayConfig();
        }

        String outTradeNo = (String) map.get("orderId");
        // 判断订单是否支付过
        ResponseResult responseResult = basePayServiceImpl.isPayOrder(outTradeNo);
        if(responseResult.getCode() != BizCodeEnume.ORDER_NOT_EXIST.getCode()){
            return responseResult;
        }
        String code = (String) map.get("code");
        String openId = (String) map.get("openId");
        String token = (String) map.get("token");
        Map<String,String> param = new HashMap<>();
        param.put("code",code);
        param.put("token",token);
        if(StringUtils.isEmpty(openId)){
            //小程序账号密码登陆获取openid
            try {
                String strUrl = String.format(WXPayConfig.getCODE_SESSION(), code);
                String resultStr = restTemplate.getForObject(strUrl, String.class);
                resultStr = new String(resultStr.getBytes("ISO-8859-1"), "UTF-8");
                JSONObject result1 = JSONObject.parseObject(resultStr);
                openId = result1.getString("openid");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
        BigDecimal totalPrice = new BigDecimal(0);
        //积分商品 0  是  1 否
        Integer integralGoods = 1;
        Integer amountAotalIntegral = null;
        String goodsName = "订单编号" + outTradeNo;
        try{
            UserInfoVO ui = ParseTokenUtils.getUserInfoByToken(token);
            String userId = ui.getUserId();
            if (openId == null) {
                openId = ui.getBindOpenid();
            }
            List<CallBackVO> callBackParaList = payConsumeTradeService.treasureOrder(token, outTradeNo);

            // 查询商品名与商品价格
            if (PayToolUtil.isInteger(outTradeNo)) {
                GoodsDTO queryGoodsPara = payConsumeTradeMapper.queryGoodsPriceAndName(outTradeNo);
                // 总价
                totalPrice = queryGoodsPara.getTradePaymentAmount();

                RedisUtil.set(HouConfig.HOU_TREASUREPARA + outTradeNo, JSON.toJSONString(callBackParaList),
                        21600);

                //获取积分商品订单的积分
                Integer totalIntegral = queryGoodsPara.getTotalIntegral();
                //积分商品 0  是  1 否
                integralGoods = queryGoodsPara.getIntegralGoods();
                amountAotalIntegral = totalIntegral;
            }else{
                List<RecordVO> rp = payConsumeRecordMapper.queryRecordParaList(outTradeNo);
                for (RecordVO recordVO : rp) {
                    BigDecimal price = recordVO.getRecordMoney();
                    totalPrice = totalPrice.add(price);
                }
                RedisUtil.set(HouConfig.HOU_TREASUREPARA + outTradeNo, JSON.toJSONString(callBackParaList),
                        21600);
                // 获取积分商品的积分
                String totalIntegral = RedisUtil
                        .get(HouConfig.HOU_USERINTEGRALYPAID + "--" + token + "--" + ui.getUserId());
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
            // 处理自营店铺
            if (!StringUtils.isEmpty(RedisUtil.get(HouConfig.HOU_ORDER_USER_TOKEN + outTradeNo))) {
                RedisUtil.remove(HouConfig.HOU_ORDER_USER_TOKEN + outTradeNo);
            }
            RedisUtil.set(HouConfig.HOU_ORDER_USER_TOKEN + outTradeNo, token, 86400);

            // 判断是否京东订单 且 是否当前支付是京东余额支付 且 余额是否够支付
            ResponseResult responseResult1 = orderServiceClient.queryJDBalanceIsEnough(outTradeNo, totalPrice);
            if(responseResult1.getCode() ==  CommonStatusEnum.SUCCESS.getCode() && responseResult1.getData().toString().equals("false")){
                // 京东账户余额不足！
                return ResponseResult.fail("京东账户余额不足！");
            }

            String payAmount = String.valueOf(totalPrice);
            String price = PayToolUtil.getMoney(payAmount);
            Map<String, String> params = new HashMap<String, String>();
            if (map.get("shopId") != null && map.get("shopId") != ""){
                //品牌店铺
                // 设置支付参数
                // 初始化参数映射，用于构建微信支付请求
                params.put("appid", WXPayConfigShop.Applet_APP_ID);
                params.put("mch_id", WXPayConfigShop.MCH_ID);
                params.put("nonce_str", PayToolUtil.create_nonce_str());
                // 设置商品描述
                params.put("body", goodsName);
                // 设置商户订单号
                params.put("out_trade_no", outTradeNo);
                // 设置订单金额
                params.put("total_fee", price);

                // 设置终端IP
                params.put("spbill_create_ip", PayToolUtil.getIpAddr(request));
                // 设置通知地址
                params.put("notify_url", WXPayConfigShop.NOTIFY_URL);
                // 设置交易类型为JSAPI
                params.put("trade_type", "JSAPI");
                // 设置openid
                params.put("openid", openId);

                // 生成签名
                String sign = PaymentKit.createSign(params, WXPayConfigShop.API_KEY);
                // 将签名添加到参数中
                params.put("sign", sign);


            }else{
                // 设置微信支付的参数
                params.put("appid", WXPayConfig.Applet_APP_ID);
                params.put("mch_id", WXPayConfig.MCH_ID);
                params.put("nonce_str", PayToolUtil.create_nonce_str());
                // 商品描述
                params.put("body", goodsName);
                // 商户订单号
                params.put("out_trade_no", outTradeNo);
                // 订单金额
                params.put("total_fee", price);

                // 用户IP
                params.put("spbill_create_ip", PayToolUtil.getIpAddr(request));
                // 支付结果通知URL
                params.put("notify_url", WXPayConfig.NOTIFY_URL);
                // 交易类型，设置为JSAPI表示微信JSAPI支付
                params.put("trade_type", "JSAPI");
                // 用户的openid
                params.put("openid", openId);

                // 生成签名
                String sign = PaymentKit.createSign(params, WXPayConfig.API_KEY);
                params.put("sign", sign);

            }

            // 调用支付接口，推送订单信息，返回XML格式的结果
            String xmlResult = PaymentApi.pushOrder(params);
            // 将XML格式的结果转换为Map格式，方便处理
            Map<String, String> result = PaymentKit.xmlToMap(xmlResult);

            // 获取返回码
            String return_code = result.get("return_code");
            // 获取返回信息
            String return_msg = result.get("return_msg");
            // 检查返回码是否为空或非SUCCESS，如果是，则返回失败结果
            if (StrKit.isBlank(return_code) || !"SUCCESS".equals(return_code)) {
                return ResponseResult.fail(return_msg);
            }
            // 获取业务结果码
            String result_code = result.get("result_code");
            // 检查业务结果码是否为空或非SUCCESS，如果是，则返回失败结果
            if (StrKit.isBlank(result_code) || !"SUCCESS".equals(result_code)) {
                return ResponseResult.fail(return_msg);
            }

            // 从结果中获取预支付订单ID，用于后续的支付流程
            String prepay_id = result.get("prepay_id");


            Map<String, String> packageParams = new HashMap<String, String>();
            // 判断是否包含shopId，用于区分小程序支付还是H5支付
            if ((map.get("shopId") != null && map.get("shopId") != "")){
                // 小程序支付场景，使用特定的appId和API密钥
                packageParams.put("appId", WXPayConfigShop.Applet_APP_ID);
                packageParams.put("timeStamp", System.currentTimeMillis() / 1000 + "");
                packageParams.put("nonceStr", System.currentTimeMillis() + "");
                packageParams.put("package", "prepay_id=" + prepay_id);
                packageParams.put("signType", "MD5");
                String packageSign = PaymentKit.createSign(packageParams, WXPayConfigShop.API_KEY);
                packageParams.put("paySign", packageSign);
                String jsonStr = JsonUtils.toJson(packageParams);
                return ResponseResult.success(jsonStr);
            }else{
                // H5支付场景，使用通用的appId和API密钥
                packageParams.put("appId", WXPayConfig.Applet_APP_ID);
                packageParams.put("timeStamp", System.currentTimeMillis() / 1000 + "");
                packageParams.put("nonceStr", System.currentTimeMillis() + "");
                packageParams.put("package", "prepay_id=" + prepay_id);
                packageParams.put("signType", "MD5");
                String packageSign = PaymentKit.createSign(packageParams, WXPayConfig.API_KEY);
                packageParams.put("paySign", packageSign);
                String jsonStr = JsonUtils.toJson(packageParams);
                return ResponseResult.success(jsonStr);
            }

        }catch (Exception e){
            return ResponseResult.fail(e.getMessage());
        }
    }
}
