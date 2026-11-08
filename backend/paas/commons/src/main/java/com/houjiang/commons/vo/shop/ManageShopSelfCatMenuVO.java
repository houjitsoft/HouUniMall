package com.houjiang.commons.vo.shop;

import lombok.Data;

import java.util.List;

/**
 * 店铺自定义商品分类表
 *
 * @author Amos
 *
 */
@Data
public class ManageShopSelfCatMenuVO {
	/**
	 * 物品类目id
	 */
	private Integer shopGoodsCatId;
	/**
	 * 物品种类
	 */
	private String shopGoodsCatName;
	/**
	 * 分类图片
	 */
	private String shopGoodsCatImg;
	/**
	 * 类目父id
	 */
	private Integer parentId;
	/**
	 * 类目状态
	 */
	private Integer shopGoodsCatStatus;
	/**
	 * 排序
	 */
	private Integer shopGoodsCatDisplayorder;

	private List<ManageShopSelfCatMenuVO> children;

	/**
	 * 是否称重分类 0:否 1是
	 */
	private Integer weight;

}
