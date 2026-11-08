package com.houjiang.commons.vo.goodscat;

import lombok.Data;

import java.util.List;

/**
 * @ClassName WebCatMenuVO
 * @Description
 * @Author Amos
 * @Date 2023/4/12 14:50
 * @Version 1.0
 * @author Amos
 */
@Data
public class WebCatMenuVO {

	/**
	 * 分类id
	 */
	private Integer catId;

	/**
	 * 分类名称
	 */
	private String catName;

	/**
	 * 父级分类id
	 */
	private Integer catParentId;

	/**
	 * 分类级别
	 */
	private Integer catLevel;

	/**
	 * 分佣比例
	 */
	private Float catCommission;

	/**
	 * 所有子分类id
	 */
	private List<Integer> allChildCatIds;

	/**
	 * 所有子分类
	 */
	private List<WebCatMenuVO> allChildCats;



}
