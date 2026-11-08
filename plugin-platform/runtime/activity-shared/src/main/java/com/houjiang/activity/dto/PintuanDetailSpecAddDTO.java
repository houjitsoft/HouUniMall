package com.houjiang.activity.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
public class PintuanDetailSpecAddDTO {

	/**
	 * 拼团id
	 */
	private Integer pintuanId;

	/**
	 * 拼团详情id
	 */
	private Integer detailId;
	/**
	 *原价格
	 */
	private BigDecimal priceOri; 
	/**
	 *团购价格
	 */
	private BigDecimal price; 
	/**
	 * 单独购买价格
	 */
	private BigDecimal priceOne;
	/**
	 *商品类别id
	 */
	private Integer cid; 
	/**
	 *参加拼团的商品ID
	 */
	private Integer goodsId;
	/**
	 *商品ID
	 */
	private Integer commonId;


}
