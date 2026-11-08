package com.houjiang.payment.dto;

import lombok.Data;

/**
 * @author Amos
 */
@Data
public class CheckRechargeCardDTO {
	/**
	 * 令牌
	 */
	private String token;

	/**
	 * 总价
	 */
	private String totalPrice;

	/**
	 * 起始卡片
	 */
	private String startCard;

	/**
	 * 结束卡片
	 */
	private String finishCard;

	/**
	 * 结束时间
	 */
	private String finishTime;

	/**
	 * 开始时间
	 */
	private String startTime;

	/**
	 * 卡片描述
	 */
	private String cardDesc;

	/**
	 * 时间类型
	 */
	private Integer timeType;
}

