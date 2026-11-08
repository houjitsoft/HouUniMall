package com.houjiang.activity.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Amos
 */
@Data
public class DiscountGoodsListQueryResultVO {

	/**
	 * 商品id
	 */
	private Integer commonId;
	/**
	 * 商品ID
	 */
	private Integer goodsId;
	/**
	 * 是否plus商品
	 */
	private Integer plusGoods;
	/**
	 * 商品名称
	 */
	private String commonName;
	/**
	 * 商品图片
	 */
	private String commonImage;
	/**
	 * 商品价格
	 */
	private BigDecimal commonPrice;
	/**
	 * 限时折扣价格
	 */
	private BigDecimal discountPrice;

	/**
	 *
	 */
	private BigDecimal discountMember;
	/**
	 * 库存
	 */
	private String commonSalenum;
	/**
	 * 广告词
	 */
	private String commonPromotionTips;

	/**
	 * 开始时间
	 */
	private Date goodsStartTime;

	/**
	 * 结束时间
	 */
	private Date goodsEndTime;

	/**
	 * 预热开始时间
	 */
	private Date preStartTime;

	/**
	 * 商品评论数
	 */
	private Integer commonEvaluate;

	/**
	 * 限时编号
	 */
	private Integer discountId;

	/**
	 * 1-已开始；2-已结束；3-已失效；0-未开始；4-预热中
	 */
	private Integer discountState;

	/**
	 * 1开始 2未开始
	 */
	private Integer activityStartStatus;

	/**
	 * 剩余时间
	 */
	private Long overplusTime;

	/**
	 * 角标图片
	 */
	private String cornerUrl;




}
