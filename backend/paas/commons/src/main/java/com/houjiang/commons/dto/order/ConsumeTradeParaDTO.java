package com.houjiang.commons.dto.order;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * consumeTrade参数
 *
 * @author Amos
 *
 */
@Data
public class ConsumeTradeParaDTO {
	private String userId;
	private String sellerId;
	private String orderId;
	private String paymentNumber;
	private List<String> goodsName;
	private BigDecimal totalPrice;
	private BigDecimal tradeDiscount;
	/** 兑换积分商品所需的积分 */
	private Integer totalIntegral;
	/**
	 * 是否是积分商品0是1否
	 */
	private Integer integralGoods;



	/**
	 * 嘉币支付的现金部分
	 */
	private BigDecimal orderCashPayment;

	/**
	 * 客户类型名称
	 * 1、用家--嘉币，2、批发--劲豆，
	 */
	private Integer customerType;

}
