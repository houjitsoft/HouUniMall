package com.houjiang.commons.config;

/**
 * @author Amos
 */
public class HouConfig {

	/**
	 * 注册默认头像。
	 * <p>注意：该 URL 为第三方对象存储的<b>带签名临时链接</b>（签名有效期至 2029-01-07），
	 * 其中含对方的 AccessKeyId 与 Signature。替换为自有 OSS/CDN 地址后再公开仓库，
	 * 在此之前请勿删除或改写——改了会导致新用户默认头像加载失败。
	 */
	public final static String TOUXING = "http://fkgou-images.oss-cn-qingdao.aliyuncs.com/file/upload/20190110/20190110094018.png?Expires=1862444433&OSSAccessKeyId=LTAIHv7U1m8yTCrg&Signature=UaFGoQWe9s9r18850JAePkBRwh4%3D";

	public final static int TOKEN_OUTTIME = 86400;

	public final static int IM_TOKEN_OUTTIME = 18000;

	// 统一使用的用户 token key
	public final static String HOU_USER_TOKEN_USER = "hou-user-token-user_";

	public final static String HOU_YIYATONG_TOKEN_USER = "hou-yiyatong-token-user_";

	// 根据code存储用户信息
	public final static String HOU_SHOP_TOKEN_SHOP = "hou-user-token-shop_";

	// 根据code存储用户信息
	public final static String HOU_USER_TOKEN_USERID = "hou-user-token-userid_";

	// 根据code存储注册用户id
	public final static String HOU_USER_LOGON_USERID = "hou-user-logon-userid_";

	// 根据手机号存储手机验证码，默认时间为五分钟
	public final static String HOU_USER_PHONE_VERIFICODE = "hou_user_phone_verificode_";

	// 存放手机验证码对应sessionId
	public final static String HOU_VERIFICODE_CLIENT_SESSION_ID = "hou_VerifiCode_client_session_id";
	// 存放手机验证码对应手机号
	public final static String HOU_VERIFICODE_CLIENT_SESSION_MOBILE = "hou_VerifiCode_client_session_mobile";

	// 根据根据随机数取token
	public final static String RANDOM_TOKEN = "random-token_";

	// 随机数保存的对应微信信息
	public final static String RANDOM_WEIXINJSON = "random-weixinjson";

	// 用户订单状态
	public final static String HOU_ORDERSTATUS = "hou_orderStatus";

	// 退款认证
	public final static String RETURN_VERIFY_PARAM = "return_verify_param_";

	// 已经绑定过手机回调地址
	public final static String ALRIGHT_BIND_URL = "https://unimall.houjit.com'/wap/pages/personal.html?1=";
	// 未绑定手机回调地址
	public final static String NOT_BIND_URL = "https://unimall.houjit.com'/wap/pages/personal.html?1=";

	// 二维码注册页面链接
//	public final static String QR_CODE_REGISTER_SHOPID = "https://www.baidu.com/?shopId=";

	public final static String QR_CODE_REGISTER_USERID = "https://unimall.houjit.com'/wap/pages/shareregister.html?userId=";

	public final static String QR_CODE_REGISTER_SHOPID = "https://unimall.houjit.com'/wap/pages/register.html?shopId=";

	// 微信扫二维码跳转链接
	/** 二维码登录跳转地址。路径中的查询参数为第三方约定的固定标识，改动将导致跳转失效。 */
	public final static String QR_CODE_LOGIN = "https://mobile.baidu.com/item?pid=3363435&&fkgoulogin=";

	// 测试回跳地址
	public final static String TEST_NOT_BIND_URL = "https://unimall.houjit.com'/testwap/pages/autologin.html?";

	// 根据code存储用户信息
	public final static String HOU_USER_OPENID = "hou-user-openid";
	// 根据code存储用户信息
	public final static String HOU_USER_UNIONID = "hou-user-unionid";

	public final static String HOU_USER_RESTRICT = "hou-user-restrict";

	public final static String SELF_ALLIANCE_ADD_TEAM_VERIFICATION = "self_alliance_add_verification";

	// 库存+/-
	public final static String HOU_SALENUM = "hou_saleNum_";

	// 用户实付的积分
	public final static String HOU_USERINTEGRALYPAID = "hou_userintegralpaid";

	// 用户实付金额
	public final static String HOU_USERAMOUNTACTUALLYPAID = "hou_useramountactuallypaid";

	// 存放商家手机号
	public final  static String HOU_SELLERPHONE = "hou_sellerphone";

	// 存放店铺是否自营
	public final  static String HOU_SHOP_IS_SUPPORT = "hou_shop_is_support_";

	// 判断是否支付过
	public final static String DETERMINEWHETHERTOPAY = "determine_whether_to_pay";

	// 支付存值
	public final static String HOU_TREASUREPARA = "hou_treasurePara_";

	// 订单来源存值
	public final static String HOU_ORDERFORM = "hou_orderFrom_";

	// 存放订单对应的用户
	public final  static String HOU_ORDER_USER_TOKEN = "hou_order_user_token_";

	// 用户申请退款,商家拒绝次数
	public final static String  NUMBER_OF_TIMES_MERCHANTS_REFUSE_REFUNDS="number_of_times_merchants_refuse_refunds";

	// 根据code存储用户信息
	public final static String HOU_PLATFORM_TOKEN_USER = "platform_user_";
	// 存储 客服token
	public final static String HOU_KF_USER_TOKEN = "KF_user_token_";
	// 存储 客服 在线状态 及 参与的会话数
	public final static String HOU_KF_ONLINE_START = "KF_user_online_start_";
	public final static String HOU_KF_QUEUE = "KF_user_queue_";
	public final static String HOU_KF_QUEUE_EXPIRE = "KF_user_queue_expire_";
	// 商城用户在线心跳缓存，客服会话列表据此判断用户是否在线
	public final static String SHOP_USER_ONLINE = "USER:ONLINE:";
	public final static int SHOP_USER_ONLINE_TIMEOUT = 5 * 60;
	// 存储 客服 会话状态
	public final static String HOU_KF_CONVERSATION_START = "KF_user_conversation_start_";
	// 存储会会话关闭提示 是否发送过
	public final static String HOU_KF_CONVERSATION_OFF_TIPS_ = "KF_user_conversation_off_tips_";
	// 存储会会话转接提示 是否发送过
	public final static String HOU_KF_CONVERSATION_TRANSFER_TIPS_ = "KF_user_conversation_transfer_tips_";
	// 网易云信账号前缀
	public final static String WY_USER_ACCOUNT = "wy_user_account_";

	// 根据code存储用户信息
	public final static String HOU_PLATFORM_TOKEN_USERID = "platform_userid_";

	// 存放当前用户是否提现请求
	public final  static String HOU_FX_WITHDRAW_ = "hou_fx_withdraw_";

	//定义好获取的域名后缀。如果还有要定义的	请添加 |(\\.域名的后缀) 。
	public final static String regStr="[0-9a-zA-Z]+((\\.com)|(\\.cn)|(\\.org)|(\\.net)|(\\.edu)|(\\.com.cn)|(\\.xyz)|(\\.xin)|(\\.club)|(\\.shop)|(\\.site)|(\\.wang)" +
			"|(\\.top)|(\\.win)|(\\.online)|(\\.tech)|(\\.store)|(\\.bid)|(\\.cc)|(\\.ren)|(\\.lol)|(\\.pro)|(\\.red)|(\\.kim)|(\\.space)|(\\.link)|(\\.click)|(\\.news)|(\\.news)|(\\.ltd)|(\\.website)" +
			"|(\\.biz)|(\\.help)|(\\.mom)|(\\.work)|(\\.date)|(\\.loan)|(\\.mobi)|(\\.live)|(\\.studio)|(\\.info)|(\\.pics)|(\\.photo)|(\\.trade)|(\\.vc)|(\\.party)|(\\.game)|(\\.rocks)|(\\.band)" +
			"|(\\.gift)|(\\.wiki)|(\\.design)|(\\.software)|(\\.social)|(\\.lawyer)|(\\.engineer)|(\\.org)|(\\.net.cn)|(\\.org.cn)|(\\.gov.cn)|(\\.name)|(\\.tv)|(\\.me)|(\\.asia)|(\\.co)|(\\.press)|(\\.video)|(\\.market)" +
			"|(\\.games)|(\\.science)|(\\.中国)|(\\.公司)|(\\.网络)|(\\.pub)" +
			"|(\\.la)|(\\.auction)|(\\.email)|(\\.sex)|(\\.sexy)|(\\.one)|(\\.host)|(\\.rent)|(\\.fans)|(\\.cn.com)|(\\.life)|(\\.cool)|(\\.run)" +
			"|(\\.gold)|(\\.rip)|(\\.ceo)|(\\.sale)|(\\.hk)|(\\.io)|(\\.gg)|(\\.tm)|(\\.com.hk)|(\\.gs)|(\\.us))";

	public final static String IM_CONFIG = "IM_CONFIG";

	public final static String KDINIAO_CONFIG = "KDINIAO_CONFIG";

	public final static String OSS_CONFIG = "OSS_CONFIG";

	public final static String SMS_CONFIG = "SMS_CONFIG";

	public final static String MESSAGE_TEMPLATE_TYPE = "message_template_type_";

	public final static String ALI_PAY_CONFIG = "ALI_PAY_CONFIG";

	public final static String LOGIN = "login:";

	/**
	 * 微信直播 地址
	 */
	public final static String WX_LIVE_HOSTS = "https://api.weixin.qq.com";

	public final static String WX_LIVE_GOODS = "https://api.weixin.qq.com/wxaapi/broadcast/goods/add?access_token=";

	public final static String WX_LIVE_ADDPATH = "/wxaapi/broadcast/role/addrole?access_token=";

	public final  static  String WX_LIVE_DELPATH = "/wxaapi/broadcast/role/deleterole?access_token=";

	//存储userID和商品shopID
	/** 支付密码错误计数。值为 Redis key 前缀，<b>不可修改</b>：改了会丢失存量计数数据。 */
	public final static String HOU_USERID_SHOPID ="fkgou_userid_shopid";

	// 订单来源存值
	/** 订单来源。值为 Redis key 前缀，<b>不可修改</b>：改了支付回调将读不到来源标识。 */
	public final static String ORDERFORM = "fkgou_orderFrom_";

	// 全部咨询分类
	public final static String HOU_INFORMATION_CLASSIFY_ADMIN_ALL = "hou_information_classify_admin_all";

	// 全部咨询分类
	public final static String HOU_INFORMATION_CLASSIFY_PC_ALL = "hou_information_classify_pc_all";

	//敏感词配置
	public final static String HOU_SENSITIVE_CONFIG = "hou_sensitive_config";

	// plus会员 支付订单
	public final static String HOU_PLUS_ORDER = "hou_plus_order_";

	//小程序支付配置 公众号支付配置
	public final static String PAY_CONFIG = "PAY_CONFIG";

	public final static String GOODS_STOCK = "goods_stock_";

	/** 京东VOP accessToken_ */
	public static final String VOP_ACCESS_TOKEN = "vop:accessToken_";

	/** 京东VOP flushToken_ */
	public static final String VOP_REFRESH_TOKEN = "vop:flushToken_";

}
