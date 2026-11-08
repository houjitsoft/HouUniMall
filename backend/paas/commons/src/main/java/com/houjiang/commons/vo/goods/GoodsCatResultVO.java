package com.houjiang.commons.vo.goods;

import lombok.Data;

import java.util.List;

/**
 * @author Amos
 */
@Data
public class GoodsCatResultVO {

	/**
	 * 分类id
	 */
	private Integer catId;

	/**
	 * 分类名称
	 */
	private String catName;

	/**
	 * 父类
	 */
	private Integer catParentId;

	/**
	 * 分类图片
	 */
	private String catPic;

	/**
	 * 二级分类  / 在商品搜索接口时为1级分类
	 */
	private List<SecordNameListVO> secordNameList;

	/**
	 * 分类级别
	 */
	private Integer catLevel;

	/**
	 * 排序
	 */
	private Integer catDisplayorder;

}
