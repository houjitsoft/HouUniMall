package com.houjiang.goods.dto;

import lombok.Data;

/**
 * @author Amos
 */
@Data
public class UserFootprintDTO {
	/**
	 * 商品访问足迹
	 */
	private Integer footprintId;

	/**
	 * 用户id
	 */
	private String userId;

	/**
	 * 商品id
	 */
	private Integer commonId;

	/**
	 * 时间
	 */
	private String footprintTime;

	/**
	 * 足记 - 年月日
	 */
	private String footprintDate;


}
