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
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.util.PayToolUtil;
import com.houjiang.commons.util.RedisUtil;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.order.OrderBalanceRechargeVO;
import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.payment.dto.GoodsDTO;
import com.houjiang.payment.feign.OrderServiceClient;
import com.houjiang.payment.feign.UsermsServiceClient;
import com.houjiang.payment.mapper.PayConfigMapper;
import com.houjiang.payment.mapper.PayConsumeRecordMapper;
import com.houjiang.payment.mapper.PayConsumeTradeBackMapper;
import com.houjiang.payment.mapper.PayConsumeTradeMapper;
import com.houjiang.payment.service.PayConsumeTradeService;
import com.houjiang.payment.service.PayStrategyHandleService;
import com.houjiang.payment.vo.CallBackVO;
import com.houjiang.payment.vo.RecordVO;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 余额充值 - 微信小程序支付
 * @author Amos
 * @date 2023/4/13 15:16
 */
@Service
public class WXAppletBalanceRechargePayServicesImpl extends BasePayServiceImpl implements PayStrategyHandleService {

    private static PayConfigMapper payConfigMapper;

    private static  RestTemplate restTemplate;

    private static OrderServiceClient orderServiceClient;

    /** 注入的时候，给类的 service 注入 */
    @Autowired
    public void setUsermsServiceClient(OrderServiceClient orderServiceClient,  RestTemplate restTemplate) {
        WXAppletBalanceRechargePayServicesImpl.restTemplate = restTemplate;
        WXAppletBalanceRechargePayServicesImpl.orderServiceClient = orderServiceClient;
    }

    /** 注入的时候，给类的 service 注入 */
    @Autowired
    public void setMapper(PayConfigMapper payConfigMapper) {
        WXAppletBalanceRechargePayServicesImpl.payConfigMapper = payConfigMapper;
    }

    final static Logger log = LoggerFactory.getLogger(WXAppletBalanceRechargePayServicesImpl.class);

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

    }


    @Override
    public ResponseResult userPayEntrance(Map<Object, Object> map, HttpServletRequest request, HttpServletResponse response) {

        //查询支付配置信息
        this.initWXPayConfig();
        String outTradeNo = (String) map.get("rechargeOrderId");
        // 判断订单是否支付过
        ResponseResult responseResult2 = basePayServiceImpl.isPayBalanceRechargeOrder(outTradeNo);
        if(responseResult2.getCode() != BizCodeEnume.ORDER_NOT_EXIST.getCode()){
            return responseResult2;
        }
        String openId = (String) map.get("openId");

        OrderBalanceRechargeVO orderBalanceRechargeVO = orderServiceClient.queryRechargeOrderById(outTradeNo);
        // 将订单总金额转换为字符串格式，用于后续支付接口调用
        String payAmount = String.valueOf(orderBalanceRechargeVO.getRechargeAmount());
        String goodsName = "订单编号" + outTradeNo;
        String price = PayToolUtil.getMoney(payAmount);
        try{
            Map<String, String> params = new HashMap<String, String>();
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
            params.put("notify_url", WXPayConfig.BALANCE_RECHARGE_NOTIFY_URL);
            // 交易类型，设置为JSAPI表示微信JSAPI支付
            params.put("trade_type", "JSAPI");
            // 用户的openid
            params.put("openid", openId);
            // 生成签名
            String sign = PaymentKit.createSign(params, WXPayConfig.API_KEY);
            params.put("sign", sign);
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
            packageParams.put("appId", WXPayConfig.Applet_APP_ID);
            packageParams.put("timeStamp", System.currentTimeMillis() / 1000 + "");
            packageParams.put("nonceStr", System.currentTimeMillis() + "");
            packageParams.put("package", "prepay_id=" + prepay_id);
            packageParams.put("signType", "MD5");
            String packageSign = PaymentKit.createSign(packageParams, WXPayConfig.API_KEY);
            packageParams.put("paySign", packageSign);
            String jsonStr = JsonUtils.toJson(packageParams);
            return ResponseResult.success(jsonStr);
        }catch (Exception e){
            return ResponseResult.fail(e.getMessage());
        }
    }
}
