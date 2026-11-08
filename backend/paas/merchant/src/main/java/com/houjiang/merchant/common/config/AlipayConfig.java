package com.houjiang.merchant.common.config;

/**
 * @author Amos
 */
public class AlipayConfig
{

    /** 支付宝网关 */
    public static String GATEWAY_URL = "https://openapi.alipay.com/gateway.do";
    public static String GATEWAY_URL_DEBUG = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";
    /** 应用ID,您的APPID，收款账号既是您的APPID对应支付宝账号 */
    public static String APP_ID = "";
    /** 商户私钥，您的PKCS8格式RSA2私钥 */
    public static String MERCHANT_PRIVATE_KEY = "";
    /** https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。 */
    public static String ALIPAY_PUBLIC_KEY = "";
    /** 服务器异步通知页面路径  需http://格式的完整路径，由自己系统开发实现 */
    public static String NOTIFY_URL = "";

    /** 页面跳转同步通知页面路径 需http://格式的完整路径，由自己系统开发实现 */
    public static String RETURN_URL = "";

    /** 签名方式 */
    public static String SIGN_TYPE = "RSA2";

    /** 字符编码格式 */
    public static String CHARSET = "utf-8";
}
