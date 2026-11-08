package com.houjiang.activity.vo;

import lombok.Data;

import java.util.List;

/**
 * @author Amos
 */
@Data
public class SeckillIndexResultVO {

	/**
	 * 总数
	 */
	private Integer totalCount;

	/**
	 * 商品信息
	 */
	private List<SeckillGoodsListQueryResultVO> promotionGoodsList;

	/**
	 *
	 */
	private Long actOverplusTime;
	/**
	 * 0-未开始;1-已开始
	 */
	private Integer actState;

}
