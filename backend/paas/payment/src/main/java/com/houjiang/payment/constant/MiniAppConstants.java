package com.houjiang.payment.constant;

/**
 * 小程序相关常量
 * 
 * @author Amos
 */
public class MiniAppConstants {
    
    /**
     * 通企付微信小程序
     */
    public static class WechatMiniApp {
        /** 小程序原始ID */
        public static final String ORIGINAL_ID = "gh_4fa331e708fb";
        /** 小程序AppId */
        public static final String APP_ID = "wxe48aedff31caaf2d";
        /** 小程序路径 */
        public static final String PATH = "pages/index/index";
    }
    
    /**
     * 通企付支付宝小程序
     */
    public static class AlipayMiniApp {
        /** 小程序AppId */
        public static final String APP_ID = "2021004162658199";
        /** 小程序路径 */
        public static final String PATH = "pages/index/index";
        /** H5跳转scheme前缀 */
        public static final String SCHEME_PREFIX = "https://ds.alipay.com/?scheme=";
        /** scheme内容前缀 */
        public static final String SCHEME_CONTENT = "alipays://platformapi/startApp?appId=2021004162658199&page=pages/index/index&query=payinfo=";
    }
    
    /**
     * 支付方式代码
     */
    public static class PayType {
        /** 微信 */
        public static final String WX_PAY = "WX_PAY";
        /** 支付宝 */
        public static final String ALI_PAY = "ALI_PAY";
        /** 云闪付 */
        public static final String YSF_PAY = "YSF_PAY";
        /** 个人银行卡 */
        public static final String C_PAY = "C_PAY";
        /** 企业网银 */
        public static final String B_PAY = "B_PAY";
        /** 白条分期 */
        public static final String JD_PAY = "JD_PAY";
        /** 花呗分期 */
        public static final String ALI_CREDIT_PAY = "ALI_CREDIT_PAY";
        /** 富民e贷 */
        public static final String FME_PAY = "FME_PAY";
        /** 转账汇款 */
        public static final String ZZHK_PAY = "ZZHK_PAY";
        /** 找好友付款 */
        public static final String SHARE_PAY = "SHARE_PAY";
    }
    
    /**
     * 直接支付方式
     */
    public static class DirectPayType {
        /** 微信小程序支付 */
        public static final String WX_LITE = "WX_LITE";
        /** 通联汇入金 */
        public static final String SFT_HRJ = "SFT_HRJ";
        /** 支付宝支付（微信小程序收银台） */
        public static final String ALI_QR = "ALI_QR";
        /** 支付宝JS支付（支付宝小程序收银台） */
        public static final String ALI_JSAPI = "ALI_JSAPI";
        /** 花呗分期 */
        public static final String ALI_CREDIT = "ALI_CREDIT";
    }
    
    /**
     * 支付结果码
     */
    public static class ResultCode {
        /** 支付成功 */
        public static final String SUCCESS = "success";
        /** 支付中 */
        public static final String PENDING = "pending";
        /** 取消支付 */
        public static final String CANCEL = "cancel";
        /** 支付失败 */
        public static final String FAIL = "fail";
    }
    
    /**
     * 小程序场景值
     */
    public static class Scene {
        /** 从小程序返回 */
        public static final String FROM_MINI_APP = "1038";
    }
}
