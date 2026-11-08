package com.houjiang.commons.dto.order;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * ConsumeRecord参数
 *
 * @author Amos
 *
 */
@Data
public class ConsumeRecordParaDTO {
	private String userId;
	private String orderId;
	private String userName;
	private String paymentNumber;
	private BigDecimal totalPrice;
	private List<String> goodsName;
	/**
	 * 支付方式
	 */
	private Integer payCode;


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
