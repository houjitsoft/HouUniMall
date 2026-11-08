package com.houjiang.activity.dto;

import lombok.Data;

/**
 * @author Amos
 */
@Data
public class AvaliableRpGoodsListDTO {

	/**
	 * 商品ID
	 */
	private Integer goodsId;
	/**
	 * 商品价格
	 */
	private Double goodsPrice;
	/**
	 * 商品数量
	 */
	private Integer goodsNumber;


}
