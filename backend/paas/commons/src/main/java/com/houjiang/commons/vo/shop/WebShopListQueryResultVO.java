package com.houjiang.commons.vo.shop;


import com.houjiang.commons.dto.shop.MerchantShopNavQueryDTO;
import com.houjiang.commons.dto.shop.ShopPercentDTO;
import com.houjiang.commons.vo.goods.GdsListQueryResultVO;
import lombok.Data;

import java.util.List;

/**
 * 不能随意修改
 * 
 * @author Amos
 *
 */
@Data
public class WebShopListQueryResultVO {

	private Integer shopId;
	private String shopName;
	/** 店铺粉丝数 */
	private Integer shopCollect;
	/**
	 * 店铺轮播图（PC端）
	 */
	private String shopSlide;
	/** 店铺logo */
	private String wapShopLogo;
	/** 店铺logo */
	private String shopLogo;
	/** 区域id */
	private Integer districtId;
	private String shopAddress;
	private List<GdsListQueryResultVO> commonList;
	private Integer shopGoodsSum;
	/** 店铺导航 */
	private List<MerchantShopNavQueryDTO> shopNav;
	/** 自营店铺 */
	private String shopSelfSupport;

	/**  */
	private ShopPercentDTO shopPercent;
	/**  */
	private ShopEvaluationVO ShopEvaluationEntity;

	/**
	 * 店铺等级id
	 */
	private Integer shopLevelId;

}
