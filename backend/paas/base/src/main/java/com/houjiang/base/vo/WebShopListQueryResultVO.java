package com.houjiang.base.vo;


import com.houjiang.commons.vo.goods.GdsListQueryResultVO;
import lombok.Data;

import java.util.List;

/**
 * 供应商店铺列表
 *
 * @author Amos
 *
 */
@Data
public class WebShopListQueryResultVO {

	private Integer shopId;

	private String shopName;
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
	/***
	 * 店铺logo
	 */
	private String shopLogo;
	/**
	 * 区域id
	 */
	private Integer districtId;

	private String shopAddress;

	private List<GdsListQueryResultVO> commonList;

	private Integer shopGoodsSum;
	/**
	 * 自营店铺
	 */
	private String shopSelfSupport;





}
