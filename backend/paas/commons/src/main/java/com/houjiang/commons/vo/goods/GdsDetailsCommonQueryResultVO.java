package com.houjiang.commons.vo.goods;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
public class GdsDetailsCommonQueryResultVO {

	private Integer commonId;
	/**
	 * 商品名称
	 */
	private String commonName;

	private String commonPromotionTips;
	/**
	 * 规格名称id
	 */
	private String commonSpecName;
	/**
	 * 规格值
	 */
	private String commonSpecValue;
	/**
	 * 商品主图
	 */
	private String commonImage;

	private BigDecimal commonPrice;

	private BigDecimal commonMarketPrice;
	/**
	 * 库存
	 */
	private Integer commonStock;
	/**
	 * 销量
	 */
	private Integer commonSalenum;

	private String commonProperty;




	/** 分类id */
	private Integer catId;

	/** 第一个商品的规格及规格名称 */
	private String goodsId;

	/**
	 * 积分支付比例
	 */
	private Double pointsRate;


}
