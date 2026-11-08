package com.houjiang.commons.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;

/**
 * @author Amos
 */
@Component
public class ALiPayConfig {

	/** 商户appid */
	public static String APPID = "";
	/** sellerId */
	public static String sellerId = "";
	/** 私钥 pkcs8格式的 */
	public static String RSA_PRIVATE_KEY = "";
	/** 应用公钥证书 */
	public static String CERT_PATH = "";

	public static String ALIPAY_PUBLIC_CERT_PATH = "";
	/** 支付宝根证书 */
	public static String ROOT_CERT_PATH = "";

	/** 服务器异步通知页面路径 https://your-domain.example.com/ShoppingPayment/ALiPay/alipayNotifyUrl */
	public static String shopping_notify_url="";

	/** pc购物回调 https://your-domain.example.com/pc/pages/alipay.html */
	public static String shopping_return_url_pc = "";

	/** 余额充值回调地址 */
	public static  String BALANCE_RECHARGE_NOTIFY_URL = "" ;
	/** 余额充值 页面跳转 地址 */
	public static  String BALANCE_RECHARGE_NOTIFY_URL_PC = "" ;
	/** 余额充值 页面跳转 地址 */
	public static  String BALANCE_RECHARGE_NOTIFY_URL_H5 = "" ;

	/** 移动端 购物回调 https://your-domain.example.com/h5/#/member/orderList/orderList */
	public static String shopping_notify_url_h5 = "";

	public static String plus_notify_url="https://unimall.houjit.com'/ShoppingPayment/ALiPay/alipayNotifyUrl1";
	/** plus 支付回调 */
	public static  String PLUS_NOTIFY_URL = null ;

	/** 请求网关地址 */
	public static String URL = "https://openapi.alipay.com/gateway.do";

	/** 编码 */
	public static String CHARSET = "UTF-8";
	/** 返回格式 */
	public static String FORMAT = "json";
	/** 日志记录目录 */
	public static String log_path = "/log";
	/** RSA2 */
	public static String SIGNTYPE = "RSA2";

	/**
	 * 单笔最低转账金额0.1元
	 */
	public static final BigDecimal MIN_AMOUNT = new BigDecimal("0.1");

	public static String ALIPAY_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAkj6dw+cvChJS0IDyAStTDatNB30MNEEN16lNaxNVrCXCG9AblsRJn8MPeV3KBXXm4C//jIStRheXi9YLPX8tlT/T8a0G009rllNRKMwQ18Kwbyrduxt+L295gCKgM1Fh9gnONFUBzDQ3dejyJOcL5GWf46km1Vx2crtYsa4VBtFJ5RztWR0gdPoswQyBmBOvmW2sm1dbnB4Nh/2aig8KX73AX0Id/3g5mD8jHTWTG6MaNYNFwGhdPHPtze9XSrXOcAHHYnvixf5upvBJIhPEFA2W626Thivx15GC03SmtlU+SX8zBArIMt01uf5c6fWtGqoa4itqJgDX1cFFECQPfwIDAQAB";


}
