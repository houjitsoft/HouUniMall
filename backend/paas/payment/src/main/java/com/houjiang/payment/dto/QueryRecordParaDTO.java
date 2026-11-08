package com.houjiang.payment.dto;

import lombok.Data;

/**
 * @author Amos
 */
@Data
public class QueryRecordParaDTO {
	/**
	 * 订单id
	 */
	private String orderId;
	/**
	 * 购买人
	 */
	private String buyerId;
	/**
	 * 备注
	 */
	private String tradeDesc;
	/**
	 * 订单状态
	 */
	private Integer orderStateId;
	/**
	 * 支付金额
	 */
	private String tradePaymentAmount;
}
