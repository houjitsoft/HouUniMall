package com.houjiang.commons.config;

import com.github.wxpay.sdk.WXPayConfig;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * 微信支付配置(单例)
 * @author Amos
 */
public class WXConfigUtil implements WXPayConfig {

    private byte[] certData;
    private static WXConfigUtil INSTANCE;


    /** 应用AppID */
    public static  String APP_ID = "wxb9bd435f7dd4f077";
    /** 商户密钥 */
    public static  String KEY = "1a0sh2jtW47wbnxzemTC1ZUQHiKKhJEL";
    /** 商户号 */
    public static  String MCH_ID = "1564151221";
    public static String certPath = "/www/server/p12/wap_apiclient_cert.p12";
    public static String NOTIFY_URL_APP = "https://java-b2b.unimall.houjit.com'/ShoppingPayment/WXPay/notifyWeiXinPay";
    /** 余额充值回调地址 */
    public static  String BALANCE_RECHARGE_NOTIFY_URL = null ;
    public static String APP_SECRET = "517b52157f308b580cf63c89b8738a34";
    public WXConfigUtil() throws Exception {
        File file = new File(certPath);
        InputStream certStream = new FileInputStream(file);
        this.certData = new byte[(int) file.length()];
        certStream.read(this.certData);
        certStream.close();
    }

    /** 双重检查加锁 */
    public static WXConfigUtil getInstance() throws Exception {
        if (INSTANCE == null) {
            synchronized (WXConfigUtil.class) {
                if (INSTANCE == null) {
                    INSTANCE = new WXConfigUtil();
                }
            }
        }
        return INSTANCE;
    }


    @Override
    public String getAppID() {
        return APP_ID;
    }

    /** parnerid，商户号 */
    @Override
    public String getMchID() {
        return MCH_ID;
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public InputStream getCertStream() {
        ByteArrayInputStream certBis = new ByteArrayInputStream(this.certData);
        return certBis;
    }

    @Override
    public int getHttpConnectTimeoutMs() {
        return 8000;
    }

    @Override
    public int getHttpReadTimeoutMs() {
        return 10000;
    }
}
