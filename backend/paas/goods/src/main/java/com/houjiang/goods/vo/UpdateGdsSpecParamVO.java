package com.houjiang.goods.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
public class UpdateGdsSpecParamVO {

	/**
	 * 规格值
	 */
	private String commonSpecValue;

	/**
	 * 商品id
	 */
	private Integer commonId;
	/**
	 * 第一个商品的规格及规格名称
	 */
	private String goodsId;
	/**
	 * 商品价格
	 */
	private BigDecimal commonPrice;
	/**
	 * 市场价
	 */
	private BigDecimal commonMarketPrice;
	/**
	 * 商品主图
	 */
	private String commonImage;
	/**
	 * 商品库存
	 */
	private Integer commonStock;
	/**
	 * 门店库存总和最大值
	 */
	private Integer commonShopGateStock;
	/**
	 * 商品库存警报线
	 */
	private Integer commonAlarm;
	/**
	 * 商品重量
	 */
	private BigDecimal commonCubage;
	/**
	 * 小宝价格
	 */
	private BigDecimal commonBabyPrice;
	/**
	 * 类型id
	 */
	private String typeId;
	/**
	 * 商品规格
	 */
	private String specParam;


}
