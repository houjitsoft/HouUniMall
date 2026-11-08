package com.houjiang.commons.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * @author Amos
 */
@Component
public class WXPayConfig {
	@Value("${domainName}")
	public String domainName;

	/**
	 * 域名
	 */
	public static String DOMAIN_NAME_URL;

	/**
	 * 导出路径
	 * 平台后台项目路径
	 */
	public static String PLATFORM_URL;



	public static String NOTIFY_URL = "https://unimall.houjit.com'/ShoppingPayment/WXPay/notifyWeiXinPay";
	/** 公众账号appid（改为自己实际的） */
	public static String APP_ID = "wx4e4622171870b7d5";
	public static String APP_SECRET = "ae108237be5f779411c372c1111232bb";
	/** 商户号（改为自己实际的） */
	public static String MCH_ID = "1564151221";
	public static String API_KEY = "1a0sh2jtW47wbnxzemTC1ZUQHiKKhJEL";
	public static String REDIRECT_URL = "https://unimall.houjit.com'/h5/#/member/orderlist/orderlist";
	public static String weChatWapPath = "/www/server/p12/wap_apiclient_cert.p12";
	/**
	 * 小程序
	 * appid（改为自己实际的）
	 */
	public  static String Applet_APP_ID = "wx8639411a560926cc";
	public  static String Applet_APP_SECRET = "e5b7082e5ebd1c3f116175ebc5108b60";
	/** 商家小程序信息 */
	public static String SHOP_APP_ID;
	public static String SHOP_APP_ID_SECRET;
	/**
	 * 公众号
	 * appid（改为自己实际的）
	 */
	public static String GZ_APP_ID = "wxf7306d2b08726622";
	public static String GZ_APP_SECRET = "ae9f5dba3e5fbc6990a5bdc83ed857ed";

	/** 退款url */
	public static String REFUND_URL = "https://api.mch.weixin.qq.com/secapi/pay/refund";
	/** 查询微信支付订单状态 */
	public static String QUERY_ORDER_START_URL = "https://api.mch.weixin.qq.com/pay/orderquery";
    /** plus 支付回调 */
    public static  String PLUS_NOTIFY_URL = null ;
	/** 余额充值回调地址 */
    public static  String BALANCE_RECHARGE_NOTIFY_URL = null ;


	/**
	 * 直播微信回调
	 */
	public static  String VIDEO_PAY_NOTIFY ;

	/**
	 * 单笔最低转账金额0.3元
	 */
	public static final BigDecimal MIN_AMOUNT = new BigDecimal("0.3");

	public static final String BODY_DESC = "交易";

	public static String getCODE_SESSION(){
		String codeSession = "https://api.weixin.qq.com/sns/jscode2session?appid=" + Applet_APP_ID
				+ "&secret=" + Applet_APP_SECRET + "&js_code=" + "%s" + "&grant_type=authorization_code";
		return codeSession ;
	}

}
