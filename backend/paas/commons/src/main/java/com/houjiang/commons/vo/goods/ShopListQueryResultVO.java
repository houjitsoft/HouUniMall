package com.houjiang.commons.vo.goods;

import lombok.Data;

import java.util.List;

/**
 * @author Amos
 */
@Data
public class ShopListQueryResultVO {

	private Integer shopId;

	private String shopName;

	private String shopSelfSupport;
	/**
	 * 店铺粉丝数
	 */
	private Integer shopCollect;
	/**
	 * 轮播图
	 */
	private String shopSlide;
	/**
	 * 店铺logo
	 */
	private String wapShopLogo;
	/**
	 * 店铺logo
	 */
	private String shopLogo;
	/**
	 * 店铺所在地址
	 */
	private String shopCompanyAddress;
	/**
	 * 商品
	 */
	private List goods;

}
