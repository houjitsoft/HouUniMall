package com.houjiang.base.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
public class WapCommonBaseUserFootVO {

	private String goodsId;

	private Integer commonId;

	/** 商品名称 */
	private String commonName;

	/** 商品价格 */
	private BigDecimal commonPrice;

	private BigDecimal commonMarketPrice;

	/** 商品主图 */
	private String commonImage;

	/** 销量 */
	private Integer commonSalenum;
	/**
	 * 商品状态 0下架，1正常，10违规（禁售）
	 */
	private Integer commonState;

	/** 商品广告词 */
	private String commonPromotionTips;

}
