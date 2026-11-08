package com.houjiang.commons.vo.shop;

import lombok.Data;

import java.util.List;

/**
 * @author Amos
 */
@Data
public class ShopAllBaseDistrictVO {

	/**
	 * 限时活动编号
	 */
	private Integer districtId;
	/**
	 * 活动名称
	 */
	private String districtName;

	/**
	 * 地区父id
	 */
	private Integer districtParentId;

	/**
	 * 地区层级
	 */
	private Integer districtIsLevel;

	/**
	 * 检查全部
	 */
	private Boolean checkAll;

	/**
	 * 不确定
	 */
	private Boolean isIndeterminate;

	/**
	 * 选中的子菜单
	 */
	private List<?> checkedSubMenus;

	/**
	 * 分区子级
	 */
	private List<ShopAllBaseDistrictVO> childDistrict;

}
