package com.houjiang.commons.vo.goods;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
public class SelfSupportIndexGoodsVO {
	/**
	 * 商品id
	 */
	private Integer commonId;
	/**
	 * 商品id
	 */
	private String goodsId;

	/**
	 * 商品名称
	 */
	private String commonName; 
	/**
	 *商品价格
	 */
	private BigDecimal commonPrice; 
	/**
	 * 市场价
	 */
	private BigDecimal commonMarketPrice;
	/**
	 *商品主图
	 */
	private String commonImage; 
	/**
	 * 商品是否参加促销0不参加,1砍价,2秒杀,3限时折扣,4拼团,5满减赠折
	 */
	private Integer commonIsXian;
	/**
	 *角标图片
	 */
	private String cornerUrl;



}
