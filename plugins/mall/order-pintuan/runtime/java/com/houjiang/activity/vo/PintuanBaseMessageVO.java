package com.houjiang.activity.vo;

import lombok.Data;

/**
 * @author Amos
 */
@Data
public class PintuanBaseMessageVO {

	/**
	 * 拼团id
	 */
	private Integer pintuanId;
	/**
	 * 拼团详情id
	 */
	private Integer detailId;
	/**
	 * 几人成团
	 */
	private Integer personNum;
	/**
	 * 商品id
	 */
	private Integer goodsId;

}
