package com.houjiang.commons.dto.goods;


import com.houjiang.commons.dto.PageUserTokenBeanDTO;

import java.util.List;

/**
 * @author Amos
 */
public class ManjianzengzheListQueryDTO extends PageUserTokenBeanDTO {

	/** 满减赠折活动名称 */
	private String promotionName;

	/** 活动状态，1-正常;0-已结束;2-管理员已关闭 */
	private Integer promotionStatus;

	/** 活动ID */
	private Integer promotionId;

	/** 店铺ID */
	private Integer shopId;

	/** 商品公共ID */
	private List<Integer> commonIds;

	/** 商品名称 */
	private String commonName;

	/** 折扣类型 */
	private Integer discountType;

	private List<Integer> promotionIds;


}
