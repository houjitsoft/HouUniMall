package com.houjiang.goods.dto;

import com.houjiang.commons.util.querypage.PageHelper;
import lombok.Data;

/**
 * @author Amos
 */
@Data
public class WepGuessLikeGoodsParamDTO extends PageHelper {

	/**
	 * 商品id
	 */
	private Integer goodsId;

	/**
	 * 分类id
	 */
	private Integer catId;
	/**
	 * 1普通商品   2供应商商品',
	 */
	private Integer goodsType;
	/**
	 * 是否是积分商品0是1否
	 */
	private Integer integralGoods;

}
