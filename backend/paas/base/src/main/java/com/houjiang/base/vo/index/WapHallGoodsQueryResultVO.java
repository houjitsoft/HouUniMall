package com.houjiang.base.vo.index;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
public class WapHallGoodsQueryResultVO {

	private Integer commonId;

	private Integer goodsId;

	private String commonName;

	private String commonPromotionTips;

	private BigDecimal commonPrice;

	private Integer commonSalenum;

	private BigDecimal commonMarketPrice;

	private String commonImage;

	private Integer commonIsXian;
	/**
	 * 商品是否参加促销0不参加,1砍价,2秒杀,3限时折扣
	 */
	private Integer activityStatus;

	/**
	 * 角标图片
	 */
	private String cornerUrl;


}
