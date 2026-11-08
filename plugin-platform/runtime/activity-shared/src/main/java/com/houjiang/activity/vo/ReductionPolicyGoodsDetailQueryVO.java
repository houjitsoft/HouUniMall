package com.houjiang.activity.vo;

import lombok.Data;

/**
 * @author Amos
 */
@Data
public class ReductionPolicyGoodsDetailQueryVO {
	/**
	 * 活动ID
	 */
	private Integer promotionId;
	/**
	 * 商品id
	 */
	private Integer commonId;
	/**
	 * 商品id
	 */
	private Integer goodsId;
	/**
	 * 店铺id
	 */
	private Integer shopId;
	/**
	 * 商品名称
	 */
	private String commonName;
	/**
	 * 商品图片
	 */
	private String commonImage;

}
