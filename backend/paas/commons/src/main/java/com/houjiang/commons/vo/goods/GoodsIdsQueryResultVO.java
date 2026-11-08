package com.houjiang.commons.vo.goods;

import lombok.Data;

/**
 * @ClassName
 * @Description
 * @Author Amos
 * @Date 2023-06-13 09:05:20
 * @Version 1.0
 * @author Amos
 */
@Data
public class GoodsIdsQueryResultVO {
	/**
	 * 商品id
	 */
	private Integer commonId;

	/**
	 * 商品id
	 */
	private Integer goodsId;

	/**
	 * 是否经销商品  0普通商品   1经销商品(商家一键上架供应商商品)  2购买经销商品（商家通过立即购买供应商上架的商品）
	 */
	private Integer dealerGoodsFlag;

}
