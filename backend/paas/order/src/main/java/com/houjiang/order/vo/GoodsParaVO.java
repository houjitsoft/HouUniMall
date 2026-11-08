package com.houjiang.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品排行信息
 * 
 * @author Amos
 *
 */
@Data
public class GoodsParaVO {
	/**
	 * 商品名
	 */
	private String goodsName;
	/**
	 * 订单总金额
	 */
	private BigDecimal OrderTotalPrice;

}
