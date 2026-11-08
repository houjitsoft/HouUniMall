package com.houjiang.activity.dto;



import com.houjiang.commons.util.querypage.PageHelper;
import lombok.Data;

import java.util.List;

/**
 * @author Amos
 */
@Data
public class ReductionPolicyListQueryDTO extends PageHelper {

	/**
	 * 满减赠折活动名称
	 */
	private String promotionName; 
	/**
	 *活动状态，1-正常;0-已结束;2-管理员已关闭
	 */
	private Integer promotionStatus; 
	/**
	 *活动ID
	 */
	private Integer promotionId; 
	/**
	 *店铺ID
	 */
	private Integer shopId;
	/**
	 *店铺名称
	 */
	private String shopName;
	/**
	 *折扣类型
	 */
	private Integer discountType;
	/**
	 *满减赠折活动id
	 */
	private List<Integer> promotionIds;


}
