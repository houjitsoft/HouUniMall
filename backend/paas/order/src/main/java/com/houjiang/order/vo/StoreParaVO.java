package com.houjiang.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 店铺排行信息
 * 
 * @author Amos
 *
 */
@Data
public class StoreParaVO {
	/**
	 * 商店名称
	 */
	private String shopName;

	/**
	 * 订单总价，使用BigDecimal类型以保持精度
	 */
	private BigDecimal OrderTotalPrice;

}
