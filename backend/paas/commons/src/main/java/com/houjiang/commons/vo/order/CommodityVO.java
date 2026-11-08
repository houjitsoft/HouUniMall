package com.houjiang.commons.vo.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Amos
 *请求商品信息的类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommodityVO {
	/**
	 * 商品名称
	 */
	private String GoodsName;

	/**
	 * 商品数量
	 */
	private String Goodsquantity;

	/**
	 * 商品价格
	 */
	private Double GoodsPrice;





}
