package com.houjiang.goods.dto;

import com.houjiang.commons.util.querypage.PageHelper;
import lombok.Data;

/**
 * @author Amos
 */
@Data
public class WebIndexRecommendQueryParamDTO extends PageHelper {
	/**
	 * 1新品，2收藏量3销售量4价格
	 */
	private Integer opType;
	/**
	 * 店铺id
	 */
	private Integer shopId;

	/**
	 * 商品id
	 */
	private Integer goodsId;

}
