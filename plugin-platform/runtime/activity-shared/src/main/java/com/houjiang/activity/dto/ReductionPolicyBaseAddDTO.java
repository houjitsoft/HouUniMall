package com.houjiang.activity.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.houjiang.commons.dto.userms.UserTokenBeanDTO;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Amos
 */
@Data
public class ReductionPolicyBaseAddDTO extends UserTokenBeanDTO {
	/**
	 * 活动ID
	 */
	private Integer promotionId;
	/**
	 * 活动名称
	 */
	private String promotionName;
	/**
	 * 店铺ID
	 */
	private String shopId;
	/**
	 * 商品ID
	 */
	private List<Map<String,Integer>> goodsList;
	/**
	 * 商品公共表ID
	 */
	private List<Integer> commonIds;
	/**
	 * 活动状态
	 */
	private Integer promotionStatus;
	/**
	 * 开始时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date startTime;
	/**
	 * 结束时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date endTime;
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
	/**
	 * 满送规则
	 */
	private List<ReductionPolicyRuleAddDTO> discountRules;
	/**
	 * 备注
	 */
	private String promotionDesc;


}
