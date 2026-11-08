package com.houjiang.commons.vo.activity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ManjianzengzhePromotionContentQueryResultVO {
	/**
	 * 活动内容
	 */
	private String promotionContent;
	/**
	 * 优惠类型
	 */
	private Integer discountType;
	/**
	 * 是否有赠品
	 */
	private Integer hasGiveaway;
	/**
	 * 优惠内容
	 */
	private String discountContent;
	/**
	 * 满足条件
	 */
	private BigDecimal meetAmount;
	/**
	 * 赠品commonId
	 */
	private Integer giveawayId;
	/**
	 * 优惠类型内容
	 */
	private String discountTypeContent;






}
