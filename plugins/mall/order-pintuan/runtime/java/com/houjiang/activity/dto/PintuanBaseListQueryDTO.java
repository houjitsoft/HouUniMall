package com.houjiang.activity.dto;

import com.houjiang.commons.util.querypage.PageHelper;
import lombok.Data;

/**
 * @author Amos
 */
@Data
public class PintuanBaseListQueryDTO extends PageHelper {
	/**
	 * 拼团活动名称
	 */
	private String name;
	/**
	 * 是否可用，1为可用,0不可用
	 */
	private Integer status;
	/**
	 * 店铺名称
	 */
	private String shopName;
	/**
	 *拼团id
	 */
	private Integer pinTuanId;
	/**
	 *用户id
	 */
	private String userId;

	/**
	 *店铺id
	 */
	private Integer shopId;

}
