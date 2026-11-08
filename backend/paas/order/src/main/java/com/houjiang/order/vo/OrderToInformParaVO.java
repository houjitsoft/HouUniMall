package com.houjiang.order.vo;

import lombok.Data;

/**
 * 下单通知所需参数
 * 
 * @author Amos
 *
 */
@Data
public class OrderToInformParaVO {
	/**
	 * 订单ID
	 */
	private String orderId;

	/**
	 * 卖家用户ID
	 */
	private String sellerUserId;

	/**
	 * 卖家用户名
	 */
	private String sellerUserName;

	/**
	 * 店铺ID
	 */
	private String shopId;

	/**
	 * 买家用户ID
	 */
	private String buyerUserId;

	/**
	 * 买家用户名
	 */
	private String buyerUserName;

}
