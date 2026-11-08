package com.houjiang.payment.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 查询商品价格商品名
 *
 * @author Amos
 */
@Data
public class QueryGoodsParaDTO {

	/**
	 * 备注
	 */
	private String tradeDesc;
	/**
	 * 支付金额
	 */
	private BigDecimal tradePaymentAmount;
	/**
	 * 积分商品所需的积分
	 */
	private Integer totalIntegral;
	/**
	 * 积分商品 0是 1 否
	 */
	private Integer integralGoods;


}
