package com.houjiang.commons.vo.order;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Amos
 *
 */
@Data
public class CommonParameterVO {

	private String token;

	private String orderReceiverName;

	private String orderReceiverAddress;

	private String orderReceiverContact;

	private Integer redPacketId;

	private Integer redPacketTId;

	private Integer voucherId;

	private Integer voucherTId;

	private Integer orderReceiverProvinceId;

	private Integer orderReceiverCityId;

	private Integer orderReceiverAreaId;
	/**
	 * 红包价格
	 */
	private Double redPacketPrice;

	private List<StoreInformationVO> storeInformationList;

	private Integer memberClassId;
	/**
	 * 虚拟商品 0 是 1 否
	 */
	private Integer virtualGoods;
	/**
	 * 来源：手机端 1PC 2wap 3安卓 5ios 6微信小程序 7wap浏览器
	 */
	private Integer platForm;
	/**
	 * 积分商品 0是 1否
	 */
	private Integer integralGoods;
	/**
	 * 是否选择门店自提 1是 2否
	 */
	private Integer orderIsShopTake;
	/**
	 * 聚宝店铺id
	 */
	private Integer treasureOrderShopId;
	/**
	 * 门店id
	 */
	private Integer shopGateId;
	/**
	 * 商品分享用户id
	 */
	private String shareUserId;

	/**
	 * 1 商家分享用户申请微客    2微客分享购买获佣
	 */
	private Integer shareType;

	/**
	 * 1正常购买   2爆品购买  3.用户购买供应商商品
	 */
	private Integer orderSource;

	/**
	 * 商品id
	 */
	private Integer goodsId;

	private List<Integer> goodsIds;
	/**
	 * 判断是否是从购物车过来的商品 ture 就删除购物车商品
	 */
	private Boolean goodsCat;
	/**
	 * 支付方式
	 */
	private Integer paymentCode;

	/**
	 * 发票信息对象
	 */
	private InvoiceEntityInfoVO invoiceInfo;

	/**
	 * 直播间id
	 */
	private Integer zbNo;

	/**
	 * 询价单号
	 */
	private String inquiryId;

	/**
	 * 是否开启询价功能
	 */
	private Integer isInquiry;

	/**
	 * 类型
	 */
	private Integer type;

	/**
	 * 是否自动选择优惠券
	 */
	private Integer isAutoChoose;

	/**
	 * 是否同城配送2-否 1-是
	 */
	private Integer isDelivery;

	/**
	 * 最终配送费
	 */
	private BigDecimal fee;

	/**
	 * 配送费
	 */
	private BigDecimal pick;

	/**
	 * 组合商品活动id
	 */
	private String combinationId;

	/**
	 * 组合促销商品数量
	 */
	private String combinationGoodsNum;

	/**
	 * 是否礼包商品
	 */
	private int isGift;

	/**
	 * 是否礼包商品
	 */
	private Integer vkShopId;

	/**
	 * 下单类型:1.普通用户购买,2:经销商购买
	 */
	private Integer buyerType;
	/**
	 * 国家id
	 */
	private Integer countryId;
	/**
	 * 国家名称
	 */
	private String countryName;
	/**
	 * 国际区号
	 */
	private String internationalDialingCode;
	/**
	 * 货币符号
	 */
	private String currencySymbol;
	/**
	 * 汇率
	 */
	private BigDecimal exchangeRate;
	/**
	 * 货币代码
	 */
	private String currencyCode;

	/**
	 * 使用积分抵扣运费：0-否，1-是
	 */
	private Integer usePointsForShipping;
}
