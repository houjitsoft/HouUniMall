package com.houjiang.commons.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
@Component
public class WXPayConfigShop {
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
	public static  String PLATFORM_URL = null ;



/**
 * public static String NOTIFY_URL = "https://your-domain.example.com/ShoppingPayment/WXPay/notifyWeiXinPay";
 * 公众账号appid（改为自己实际的）
 */
	public static String APP_ID = "wx4e4622171870b7d5";
	public static String APP_SECRET = "ae108237be5f779411c372c1111232bb";


	public static String REDIRECT_URL = "https://unimall.houjit.com'/h5/#/member/orderlist/orderlist";


	/**
	 * 小程序
	 * appid（改为自己实际的）
	 */
	public static String Applet_APP_ID ;
	public static String Applet_APP_SECRET ;

	/** 商户号（改为自己实际的） */
	public static String MCH_ID ;
	public static String API_KEY ;

	public static String weChatWapPath ;

	/** 回调 */
	public static String NOTIFY_URL ;

	/** 退款url */
	public static String REFUND_URL = "https://api.mch.weixin.qq.com/secapi/pay/refund";

	/**
	 * 直播微信回调
	 */
	public static  String VIDEO_PAY_NOTIFY ;

	/**
	 * 单笔最低转账金额0.3元
	 */
	public static final BigDecimal MIN_AMOUNT = new BigDecimal("0.3");

	public static final String BODY_DESC = "交易";


}
