package com.houjiang.payment.service.impl;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.CertAlipayRequest;
import com.alipay.api.DefaultAlipayClient;
import com.houjiang.commons.config.ALiPayConfig;
import com.houjiang.commons.config.WXConfigUtil;
import com.houjiang.commons.config.WXPayConfig;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.exception.BizCodeEnume;
import com.houjiang.commons.util.RedisUtil;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.StringUtils;
import com.houjiang.payment.feign.OrderServiceClient;
import com.houjiang.payment.feign.UsermsServiceClient;
import com.houjiang.payment.mapper.PayConfigMapper;
import com.houjiang.payment.mapper.PayConsumeRecordMapper;
import com.houjiang.payment.mapper.PayConsumeTradeMapper;
import com.houjiang.payment.service.WeChatPayServices;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName PayBaseServiceImpl
 * @Description 基础支付服务实现公共类
 * @Author Amos
 * @Date 2024/5/10 11:06
 * @Version 1.0
 * @author Amos
 */
@Service
public class BasePayServiceImpl {

    @Value("${domainName}")
    public String domainName;

    @Resource
    protected PayConfigMapper payConfigMapper;

    @Resource
    protected PayConsumeTradeMapper payConsumeTradeMapper;

    @Resource
    protected PayConsumeRecordMapper payConsumeRecordMapper;

    @Resource
    protected UsermsServiceClient usermsServiceClient;

    @Resource
    private OrderServiceClient orderServiceClient;

    @Resource
    protected WeChatPayServices weChatPayServices;

    /**
     * 静态初使化当前类
     */
    public static BasePayServiceImpl basePayServiceImpl;

    /**
     * 该方法在类的实例化后立即执行，用于初始化操作。
     * 本方法中，将当前实例赋值给basePayServiceImpl变量，实现对自身的引用。
     *
     * /@PostConstruct 注解表明该方法是一个初始化方法，会在依赖注入完成后执行。
     */
    @PostConstruct
    public void init(){
        basePayServiceImpl = this;
    }

    /**
     * 初始化 阿里云支付参数
     * 该方法用于从Redis或数据库中获取支付配置，并初始化阿里云支付相关的配置参数。
     * 1. 首先尝试从Redis中获取支付配置信息；
     * 2. 如果Redis中不存在，则从数据库中查询支付配置；
     * 3. 将查询到的支付配置信息存储到Redis中以供后续使用；
     * 4. 并初始化阿里云支付配置类的各个参数。
     */
    public void initAliPayConfig() {
        //从redis中查询
        String payStr = RedisUtil.get(HouConfig.ALI_PAY_CONFIG);
        Map<String, Object> aliPayConfig;
        if (StringUtils.isEmpty(payStr)) {
            aliPayConfig = basePayServiceImpl.payConfigMapper.selectPayConfig("03");
        } else {
            aliPayConfig = JSON.parseObject(payStr, Map.class);
            //将支付配置存入Redis
            RedisUtil.set(HouConfig.ALI_PAY_CONFIG, JSON.toJSON(aliPayConfig).toString());
        }
        if (null != aliPayConfig) {
            // 初始化支付宝配置参数
            // 从配置文件中获取并设置支付宝网关URL
            ALiPayConfig.URL = aliPayConfig.get("gatewayURL").toString();
            // 从配置文件中获取并设置应用ID
            ALiPayConfig.APPID = aliPayConfig.get("appID").toString();
            // 从配置文件中获取并设置卖家ID
            ALiPayConfig.sellerId = aliPayConfig.get("appMCHId").toString();
            // 从配置文件中获取并设置应用私钥
            ALiPayConfig.RSA_PRIVATE_KEY = aliPayConfig.get("appAPIKey").toString();
            // 从配置文件中获取并设置证书路径
            ALiPayConfig.CERT_PATH = aliPayConfig.get("cert_public_key_res2").toString();
            // 从配置文件中获取并设置根证书路径
            ALiPayConfig.ROOT_CERT_PATH = aliPayConfig.get("cert_root_key").toString();
            // 从配置文件中获取并设置支付宝公钥路径
            ALiPayConfig.ALIPAY_PUBLIC_CERT_PATH = aliPayConfig.get("cert_app_public_key").toString();
            // 从配置文件中获取并设置交易成功的通知URL
            ALiPayConfig.shopping_notify_url = aliPayConfig.get("shoppingNotifyUrl").toString();
            // 从配置文件中获取并设置H5支付成功的通知URL
            ALiPayConfig.shopping_notify_url_h5 = aliPayConfig.get("rechargeNotifyUrl2").toString();
            // 从配置文件中获取并设置PC端返回URL
            ALiPayConfig.shopping_return_url_pc = aliPayConfig.get("shoppingReturnUrlPc").toString();
            // 从配置文件中获取并设置PLUS支付通知URL
            ALiPayConfig.PLUS_NOTIFY_URL = aliPayConfig.get("plusNotifyUrl").toString();
            // 从配置文件中获取并设置余额充值支付通知URL
            ALiPayConfig.BALANCE_RECHARGE_NOTIFY_URL = aliPayConfig.get("balanceRechargeNotifyUrl").toString();

            // 余额充值 页面跳转 地址
            ALiPayConfig.BALANCE_RECHARGE_NOTIFY_URL_PC = domainName + "/ucenter/index.html#/property/myBalance" ;
            // 余额充值 页面跳转 地址
            ALiPayConfig.BALANCE_RECHARGE_NOTIFY_URL_H5 = domainName + "/h5/#/wallet/index/index" ;
        }
    }

    /**
     * 获取支付宝请求客户端
     * 该方法用于初始化并返回一个用于与支付宝进行通信的客户端实例。
     * 这个客户端配置了应用的认证信息和通信参数。
     *
     * @return AlipayClient 支付宝客户端实例，用于发起支付宝接口请求。
     * @throws AlipayApiException 如果在初始化客户端时发生错误，抛出此异常。
     */
    public AlipayClient getAlipayClient() throws AlipayApiException {
        CertAlipayRequest certAlipayRequest = new CertAlipayRequest();
        // 初始化支付宝请求配置
        // 设置支付宝网关URL
        certAlipayRequest.setServerUrl(ALiPayConfig.URL);
        // 设置应用ID
        certAlipayRequest.setAppId(ALiPayConfig.APPID);
        // 设置应用私钥
        certAlipayRequest.setPrivateKey(ALiPayConfig.RSA_PRIVATE_KEY);
        // 设置应用公钥证书路径
        certAlipayRequest.setCertPath(ALiPayConfig.ALIPAY_PUBLIC_CERT_PATH);
        // 设置支付宝公钥证书路径
        certAlipayRequest.setAlipayPublicCertPath(ALiPayConfig.CERT_PATH);
        // 设置支付宝根证书路径
        certAlipayRequest.setRootCertPath(ALiPayConfig.ROOT_CERT_PATH);
        // 设置签名类型
        certAlipayRequest.setSignType(ALiPayConfig.SIGNTYPE);
        // 设置返回格式
        certAlipayRequest.setFormat(ALiPayConfig.FORMAT);
        // 设置编码方式
        certAlipayRequest.setCharset(ALiPayConfig.CHARSET);
        System.out.println("ALiPayConfig.APPID======"+ALiPayConfig.APPID);
        // 创建并返回支付宝客户端实例
        return new DefaultAlipayClient(certAlipayRequest);
    }

    /**
     * 初始化微信支付参数。
     * 该方法用于设置微信支付所需的配置参数，包括appAPIKey、appSecret、appMCHId、appID和notifyUrl。
     * 这些参数是从一个未明示的方法getWeChatMap中获取，并赋值给全局配置类WXConfigUtil的相应字段。
     */
    public void initWeChatPayConfig() {
        // 从getWeChatMap方法获取微信支付配置信息
        Map<String, Object> outMap = getWeChatMap();

        // 设置微信支付的APP相关参数
        WXConfigUtil.KEY = outMap.get("appAPIKey").toString();
        WXConfigUtil.APP_SECRET = outMap.get("appSecret").toString();
        WXConfigUtil.MCH_ID = outMap.get("appMCHId").toString();
        WXConfigUtil.APP_ID = outMap.get("appID").toString();
        // 设置微信支付通知URL
        WXConfigUtil.NOTIFY_URL_APP = outMap.get("notifyUrl").toString();
        WXConfigUtil.BALANCE_RECHARGE_NOTIFY_URL = outMap.get("balanceRechargeNotifyUrl").toString();
        // 如果配置中包含微信PC端路径，则设置该路径
        if(outMap.containsKey("weChatPcPath")){
            WXConfigUtil.certPath = outMap.get("weChatPcPath").toString();
        }
    }


    /**
     * 获取微信PC端H5支付所需的参数配置
     * 该方法不接受任何参数，返回一个包含微信支付配置信息的Map对象。
     * 配置信息包括：API密钥、应用密钥、商户号、应用ID、通知URL、重定向URL以及可选的微信H5支付路径。
     *
     * @return 包含微信支付参数配置的Map对象
     */
    public Map<String, Object> getWeChatMap() {
        // 初始化支付模式代码为"00"
        String paymentMode = "00";
        // 从支付配置中获取相应的支付参数
        Map<String, Object> outMap = basePayServiceImpl.payConfigMapper.selectPayConfig(paymentMode);

        // 设置微信支付配置的API密钥、应用密钥、商户号、应用ID、通知URL和重定向URL
        WXPayConfig.API_KEY = outMap.get("appAPIKey").toString();
        WXPayConfig.APP_SECRET = outMap.get("appSecret").toString();
        WXPayConfig.MCH_ID = outMap.get("appMCHId").toString();
        WXPayConfig.APP_ID = outMap.get("appID").toString();
        WXPayConfig.NOTIFY_URL = outMap.get("notifyUrl").toString();
        WXPayConfig.REDIRECT_URL = outMap.get("redirectUrl").toString();
        WXPayConfig.PLUS_NOTIFY_URL = outMap.get("plusNotifyUrl").toString();
        WXPayConfig.BALANCE_RECHARGE_NOTIFY_URL = outMap.get("balanceRechargeNotifyUrl").toString();

        // 如果配置中包含微信PC端路径，则设置该路径
        if(outMap.containsKey("weChatPcPath")){
            WXPayConfig.weChatWapPath=outMap.get("weChatPcPath").toString();
        }
        // 获取微信公众号的支付配置
        String paymentMode1 = "04";
        Map<String, Object> outMap1 = basePayServiceImpl.payConfigMapper.selectPayConfig(paymentMode1);
        WXPayConfig.GZ_APP_ID = outMap1.get("appID").toString();
        WXPayConfig.GZ_APP_SECRET = outMap1.get("appSecret").toString();

        return outMap;
    }

    /**
     * 判断订单是否支付过
     */
    public ResponseResult isPayOrder(String orderId){
        // 判断订单是否微信支付过
        ResponseResult responseResult = isWxPayOrder(orderId);
        if(responseResult.getCode() == BizCodeEnume.ORDER_NOT_EXIST.getCode()){
            // 判断订单是否支付宝支付过
            responseResult = isAliPayOrder(orderId);
            if(responseResult.getCode() != BizCodeEnume.ORDER_NOT_EXIST.getCode()){
                return responseResult;
            }else {
                return ResponseResult.fail(BizCodeEnume.ORDER_NOT_EXIST);
            }
        }else {
            return responseResult;
        }
    }

    public ResponseResult isWxPayOrder(String orderId){
        // 判断订单是否支付
        Map<String, Object> weChatMap = new HashMap<>();
        List<Map<String, Object>> baseMapList = basePayServiceImpl.orderServiceClient.queryOrderIdAndPaymentNumber(orderId);
        for (Map<String, Object> baseMap : baseMapList) {
            weChatMap.put("orderId", baseMap.get("paymentNumber"));
            ResponseResult responseResult = basePayServiceImpl.weChatPayServices.queryWxPay(weChatMap);
            if(responseResult.getCode() == BizCodeEnume.ORDER_NOT_EXIST.getCode()){
                weChatMap.put("orderId", baseMap.get("orderId"));
                responseResult = basePayServiceImpl.weChatPayServices.queryWxPay(weChatMap);
                if(responseResult.getCode() != BizCodeEnume.ORDER_NOT_EXIST.getCode()){
                    return responseResult;
                }else {
                    return ResponseResult.fail(BizCodeEnume.ORDER_NOT_EXIST);
                }
            }else {
                return responseResult;
            }
        }
        return ResponseResult.fail(BizCodeEnume.ORDER_NOT_EXIST);
    }

    public ResponseResult isAliPayOrder(String orderId){
        // 判断订单是否支付
        Map<String, Object> aliMap = new HashMap<>();
        List<Map<String, Object>> baseMapList = basePayServiceImpl.orderServiceClient.queryOrderIdAndPaymentNumber(orderId);
        for (Map<String, Object> baseMap : baseMapList) {
            aliMap.put("orderId", baseMap.get("paymentNumber"));
            ALiPayServiceImpl aLiPayService = new ALiPayServiceImpl();
            ResponseResult responseResult = aLiPayService.alipayQuery(aliMap);
            if(responseResult.getCode() == BizCodeEnume.ORDER_NOT_EXIST.getCode()){
                aliMap.put("orderId", baseMap.get("orderId"));
                responseResult = aLiPayService.alipayQuery(aliMap);
                if(responseResult.getCode() != BizCodeEnume.ORDER_NOT_EXIST.getCode()){
                    return responseResult;
                }else {
                    return ResponseResult.fail(BizCodeEnume.ORDER_NOT_EXIST);
                }
            }else {
                return responseResult;
            }
        }
        return ResponseResult.fail(BizCodeEnume.ORDER_NOT_EXIST);
    }
    /**
     * 判断充值订单是否支付过
     */
    public ResponseResult isPayBalanceRechargeOrder(String orderId){
        Map<String, Object> map = new HashMap<>();
        map.put("orderId",orderId);
        // 判断订单是否微信支付过
        ResponseResult responseResult = basePayServiceImpl.weChatPayServices.queryWxPay(map);
        if(responseResult.getCode() == BizCodeEnume.ORDER_NOT_EXIST.getCode()){
            // 判断订单是否支付宝支付过、
            ALiPayServiceImpl aLiPayService = new ALiPayServiceImpl();
            responseResult =  aLiPayService.alipayQuery(map);;
            if(responseResult.getCode() != BizCodeEnume.ORDER_NOT_EXIST.getCode()){
                return responseResult;
            }else {
                return ResponseResult.fail(BizCodeEnume.ORDER_NOT_EXIST);
            }
        }else {
            return responseResult;
        }
    }
}
