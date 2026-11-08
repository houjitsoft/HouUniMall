package com.houjiang.activity.dto;

import lombok.Data;

import java.util.List;

/**
 * @author Amos
 */
@Data
public class GetShopInfoDTO {

	/**
	 * 店铺id
	 */
	private Integer shopId;

	/**
	 * 优惠券id
	 */
	private Integer voucherTId;

	/**
	 * 商品信息
	 */
	private List<AvaliableRpGoodsListDTO> goodsList;


}
