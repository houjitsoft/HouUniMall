package com.houjiang.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
public class GoodsAllByidVO {
	/**
	 *商品主图
	 */
	private String goodsImage;

	/**
	 *商品名称
	 */
	private String goodsName;

	/**
	 *价格
	 */
	private double goodsPrice;

	/**
	 *数量
	 */
	private Integer orderGoodsNum;

	/**
	 *退货状态
	 */
	private Integer goodsRefundStatus;

	/**
	 *退款状态
	 */
	private Integer goodsReturnStatus;

	/**
	 *是否存在退货
	 */
	private Integer isNoretrun;

	/**
	 *
	 */
	private String orderSpecChineseValue;

	/**
	 *
	 */
	private String orderGoodsReturnnum;

	/**
	 *手工调整价格
	 */
	private double orderGoodsAdjustFee;

	/**
	 * 商品实付价
	 */
	private BigDecimal orderGoodsPaymentAmount;

	/**
	 * 运费
	 */
	private BigDecimal shippingFee;
	/**
	 * 红包优惠
	 */
	private BigDecimal orderRptPrice;
	/**
	 * 优惠券优惠
	 */
	private BigDecimal voucherPrice;
	/**
	 * 会员折扣
	 */
	private BigDecimal goodsMemberDiscount;
	/**
	 *  plus会员折扣金额
	 */
	private BigDecimal goodsPlusDiscount;

	/**
	 * 满减优惠金额
	 */
	private BigDecimal manjianDiscount;
	/**
	 * 满折优惠金额
	 */
	private BigDecimal manzheDiscount;
	/**
	 * 活动id
	 */
	private Integer promotionId;
	/**
	 * 活动单价
	 */
	private BigDecimal activityUnitPrice;
}
