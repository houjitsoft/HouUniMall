package com.houjiang.commons.dto.goods;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Amos
 */
@Data
public class SeckillGoodsListDTO {

	/**
	 * 商品编号
	 */
	private Integer goodsId;
	/**
	 *
	 */
	private Integer commonId;
	/**
	 *店铺编号
	 */
	private Integer shopId;
	/**
	 *商品名称
	 */
	private String goodsName;
	/**
	 *商品原价
	 */
	private BigDecimal goodsPrice;
	/**
	 *限时秒杀价格
	 */
	private BigDecimal seckillPrice;
	/**
	 * 商品图片
	 */
	private String goodsImage;
	/**
	 *秒杀限购上限
	 */
	private Integer goodsUpperLimit;
	/**
	 *秒杀库存
	 */
	private Integer seckillNum;
	/**
	 * sku
	 */
	private List<SeckillGoodsDTO> specLists;
	/**
	 * 秒杀最大价
	 */
	private BigDecimal seckillMaxPrice;
	/**
	 * 秒杀最小价
	 */
	private BigDecimal seckillMinPrice;
	/**
	 * 秒杀总库存
	 */
	private Integer seckillAllNum;
}
