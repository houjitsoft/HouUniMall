package com.houjiang.commons.vo.goods;

import lombok.Data;

/**
 * @author Amos
 */
@Data
public class ManageShopBaseQueryResultVO {

	private Integer districtId;
	/**
	 * 店铺名
	 */
	private String shopName;

	private String shopSelfSupport;

	private Integer shopStatus;



}
