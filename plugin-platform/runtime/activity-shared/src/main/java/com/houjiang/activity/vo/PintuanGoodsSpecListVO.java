package com.houjiang.activity.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
public class PintuanGoodsSpecListVO {
	/**
	 * 拼团详情id
	 */
	private Integer detailId;
	/**
	 * 商品id
	 */
	private Integer goodsId;
	/**
	 * 商品id
	 */
	private Integer commonId;
	/**
	 * 商品规格名
	 */
	private String goodsSpecName;
	/**
	 * 商品库存
	 */
	private Integer goodsStock;

	/**
	 * 原价格
	 */
	private BigDecimal priceOri; 
	/**
	 *团购价格
	 */
	private BigDecimal price; 
	/**
	 *单独购买价格
	 */
	private BigDecimal priceOne; 


}
