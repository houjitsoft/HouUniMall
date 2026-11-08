package com.houjiang.userms.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Amos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberMessageListVO {
	/**
	 * 用户id
	 */
	private String userId;
	/**
	 *用户名
	 */
	private String userName;
	/**
	 *手机号
	 */
	private String userMobile;
	/**
	 *性别
	 */
	private String userGender;
	/**
	 *真实姓名
	 */
	private String userTruename;
	/**
	 *生日
	 */
	private String userBirth;
	/**
	 *注册时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private String userRegTime;

	/**
	 *上次登陆时间
	 */
	private String userLastloginTime;
	/**
	 *
	 */
	private Integer orderCount;
	/**
	 *
	 */
	private BigDecimal sumAmount = new BigDecimal(0);
	/**
	 *绑定类型  1:sinaweibo  2:qq   3:weixin 4.：支付宝  11:email  12:mobile
	 */
	private String bindType;
	/**
	 *用户积分
	 */
	private Integer userIntegral;
	/**
	 *会员等级id
	 */
	private Integer memberClassId;
	/**
	 *会员等级
	 */
	private String  memberClass;
	/**
	 *会员标签id
	 */
	private String tagId;
	/**
	 *
	 */
	private List<String> tagList;
	/**
	 *是否是商家
	 */
	private Integer isBusiness;
	/**
	 *套餐类型
	 */
	private Integer thaliType;
	/**
	 *plus会员价格
	 */
	private String plusPrice;
	/**
	 * 0:个人入驻 1:企业入驻 3:供应商  4  品牌店铺
	 */
	private Integer shopBusiness;
	/**
	 * 是否自营
	 */
	private Boolean shopSelfSupport;

	/**
	 * 剩余货币数量
	 */
	private BigDecimal balance;
	/**
	 * 货币类型id
	 */
	private Integer currencyId;
	/**
	 * 货币类型名称
	 */
	private String currencyName;

	/**
	 * 客户类型名称
	 * 1、用家--嘉币，2、批发--劲豆，
	 */
	private String customerTypeName;

	/**
	 * 店铺id
	 */
	private Integer outShopId;


	/**
	 * 店铺名称
	 */
	private String outShopName;
	/**
	 * 店铺类型，1-餐厅/饭馆（用家）；2-批发/贸易商（批发）；3-超市/便利店（批发）；4-学校/工厂（批发）；5-电商（批发）
	 */
	private Integer outShopType;

	/**
	 * 智能云为客户编码
	 */
	private String outShopCode;

	/**
	 * 大区
	 */
	private String regionName;
	/**
	 * 办事处
	 */
	private String officeName;
	/**
	 * 片区
	 */
	private String zoneName;
	/**
	 * 省
	 */
	private String province;
	/**
	 * 市
	 */
	private String city;

	/**
	 * 区
	 */
	private String area;

	/**
	 * 销售队员
	 */
	private String relationSaleName;
	/**
	 * scrm店铺编码
	 */
	private String shopCode;
	/**
	 * 详细地址
	 */
	private String shopDetailAddress;


	/**
	 * 营业执照编号
	 */
	private String businessLicenseRegNum;

	/**
	 * scrm店铺编码
	 */
	private String bizShopCode;

	/**
	 * 状态(0:未激活,1:未认证,2:已认证,3:锁定4.多账号)
	 */
	private String userState;

	/**
	 * 0-不是；1-是（黑名单的用户登录不了小程序）
	 */
	private Integer isBlack;

	/**
	 * 消耗积分冻结状态，0-冻结；1-正常
	 */
	private Integer placeOrderStatus;
}

