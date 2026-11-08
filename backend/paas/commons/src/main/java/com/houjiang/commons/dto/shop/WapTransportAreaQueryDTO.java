package com.houjiang.commons.dto.shop;

import lombok.Data;

import java.util.List;

/**
 * @author Amos
 */
@Data
public class WapTransportAreaQueryDTO {

	/**
	 * 店铺商品id集合
	 */
	private List<List<Integer>> shopGoodsIds;
	/**
	 * 区域城市id集合
	 */
	private List<Integer> areaIds;
	/**
	 * 1 商家  2 供应商
	 */
	private Integer goodsType;
	/**
	 * 国家id
	 */
	private Integer countryId;
}
