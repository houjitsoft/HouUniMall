package com.houjiang.goods.vo;

import lombok.Data;

/**
 * @author Amos
 */
@Data
public class CommonDetailsDescVO {

	/**
	 * 关联板式id
	 */
	private Integer commonFormatidTop;
	/**
	 * 底部关联板式
	 */
	private Integer commonFormatidBottom;
	/**
	 * 商品属性
	 */
	private String commonProperty;
	/**
	 * 是否支持门店自提 1是 2否
	 */
	private Integer commonIsShopTake;
	/**
	 * 品牌名称
	 */
	private String brandName;


}
