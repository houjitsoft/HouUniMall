package com.houjiang.goods.vo;

import lombok.Data;

/**
 * 店铺按月分组
 * 
 * @author Amos
 *
 */
@Data
public class ShopGroupingStatisticsVO {
	/**
	 * 时间
	 */
	private String time;
	/**
	 * 数量
	 */
	private Integer num;

}
