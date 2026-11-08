package com.houjiang.activity.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Amos
 */
@Data
public class ReductionPolicyRuleAddDTO extends UserTokenBeanDTO {
	/**
	 * 活动ID
	 */
	private Integer promotionId;
	/**
	 * 活动内容
	 */
	private String promotionContent;
	/**
	 * 是否有赠品
	 */
	private Integer hasGiveaway;
	/**
	 * 赠品ID
	 */
	private Integer giveawayId;
	/**
	 * 满足金额
	 */
	private BigDecimal meetAmount;
	/**
	 * 优惠内容
	 */
	private String discountContent;
	/**
	 * 折扣类型
	 */
	private Integer discountType;
	/**
	 * 创建时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date createdTime;
	/**
	 * 更新时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date updateTime;


}
