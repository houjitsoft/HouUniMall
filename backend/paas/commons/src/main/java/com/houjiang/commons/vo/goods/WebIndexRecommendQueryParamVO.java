package com.houjiang.commons.vo.goods;

import lombok.Data;

/**
 * @author Amos
 */
@Data
public class WebIndexRecommendQueryParamVO {
	/**
	 * 1新品，2收藏量3销售量4价格
	 */
	private Integer opType;

	/**
	 * 商店ID，用于标识不同的商店
	 */
	private Integer shopId;

	/**
	 * 限制数量，表示每次操作的最大数量限制
	 */
	private Integer limitSize;

	/**
	 * 商品ID，用于标识特定的商品
	 */
	private Integer goodsId;

	/**
	 * 当前页码，用于分页查询时指定当前查看的页面
	 */
	private Integer currentPage;

}
