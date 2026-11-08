package com.houjiang.goods.vo;

import lombok.Data;

/**
 * @author Amos
 */
@Data
public class CommonSpecValueVO {

	/**
	 * 商品id
	 */
	private Integer goodsId;
	/**
	 * 商品id
	 */
	private Integer commonId;

	/**
	 * 商品规格id
	 */
	private Integer goodsSpecId;

	/**
	 *
	 */
	private Integer specNameId;

	public CommonSpecValueVO(Integer goodsId, Integer commonId) {
		this.goodsId = goodsId;
		this.commonId = commonId;
	}

	public CommonSpecValueVO() {
	}
}
