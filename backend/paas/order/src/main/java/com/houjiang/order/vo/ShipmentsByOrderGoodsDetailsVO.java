package com.houjiang.order.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 发货订单商品详情
 *
 * @author Amos
 *
 */
@Data
public class ShipmentsByOrderGoodsDetailsVO {

	/**
	 * 商品名
	 */
	private String goodsName;

	/**
	 * 商品id
	 */
	private Integer goodsId;
	/**
	 * 商品图片
	 */
	private String goodsImage;
	/**
	 * 商品价格
	 */
	private BigDecimal goodsPrice;

	/**
	 * 订单商品数量
	 */
	private Integer orderGoodsNum;

	/**
	 * 商品加减值（未使用）
	 */
	private Integer goodsPlusDis;

	/**
	 * 订单商品描述的中文值
	 */
	private String orderSpecChineseValue;

	/**
	 * 订单商品应付金额，double类型表示精度更高，存储更准确
	 */
	private double orderGoodsPaymentAmount;
	/**
	 * 供应商商品订单总价 （不含运费）
	 */
	private BigDecimal supperOrderAmount;
	/**
	 * 供应商商品订单实付价 （含运费）
	 */
	private BigDecimal supperPaymentAmount;
	/**
	 * 供应商商品价格
	 */
	private BigDecimal supperGoodsPrice;

	/**
	 * 发货数量
	 */
	private Integer shipmentQuantity;

	/**
	 * 发货状态 0待发货 1部分发货 2发货完成
	 */
	private Integer shipmentStatus;
	/**
	 * 发货时效
	 */
	private Integer deliveryTime;
	/**
	 * 最晚发货时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date latestDeliveryTime;
	/**
	 * 支付时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date paymentTime;
}
