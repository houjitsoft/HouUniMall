package com.houjiang.goods.vo;


import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
public class IntegralGoodsResultVO {

	private Integer commonId;

	private String goodsId;

	/**
	 * 商品ID（第一个）
	 */
	private Integer goodsIdOne;

	private String commonName;
	/**
	 * 副标题，即商品广告词
	 */
	private String commonPromotionTips;
	/**
	 * 商品价格
	 */
	private BigDecimal commonPrice;
	/**
	 * 销售量
	 */
	private Integer commonSalenum;
	/**
	 * 市场价
	 */
	private BigDecimal commonMarketPrice;

	private String commonImage;
	/**
	 * 商品状态 0下架，1正常，10违规（禁售）
	 */
	private Integer commonState;
	/**
	 * 评论数
	 */
	private Integer commonEvaluate;
	/**
	 * 收藏量
	 */
	private Integer commonCollect;

	private String shopName;

	private Integer shopId;

	private Integer commonType;
	/**
	 * 小宝价格
	 */
	private BigDecimal commonBabyPrice;
	/**
	 * 好评数
	 */
	private Integer qty;
	/**
	 * 平均发货时间
	 */
	private String avgShippingTime;
	/**
	 * 商品是否参加促销0不参加,1砍价,2秒杀,3限时折扣
	 */
	private Integer commonIsXian;

	private Integer activityStatus;
	/**
	 * 角标图片
	 */
	private String cornerUrl;
	/**
	 * 是否为虚拟商品0是1否
	 */
	private Integer virtualGoods;
	/**
	 * 虚拟商品有效期
	 */
	private String periodOfValidity;
	/**
	 * 过期退款0是1否
	 */
	private Integer pastDue;
	/**
	 * 是否是积分商品0是1否
	 */
	private Integer integralGoods;
	/**
	 * 购买所需积分
	 */
	private Integer needIntegral;

}
