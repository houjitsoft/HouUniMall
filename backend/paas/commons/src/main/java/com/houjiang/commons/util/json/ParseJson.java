package com.houjiang.commons.util.json;

import lombok.Data;

/**
 * @author Amos
 */
@Data
public class ParseJson {

	private Object specId;
	private Object specValue;
	/**
	 * 商品id
	 */
	private Integer goodsId;
	/**
	 * 商品库存
	 */
	private Integer goodsStock;



}
