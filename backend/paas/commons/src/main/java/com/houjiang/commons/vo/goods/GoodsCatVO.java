package com.houjiang.commons.vo.goods;

import lombok.Data;

import java.util.List;

/**
 * @ClassName GoodsCatVO
 * @Description
 * @Author Amos
 * @Date 2023/4/12 14:54
 * @Version 1.0
 * @author Amos
 */
@Data
public class GoodsCatVO {

	private Integer catId;

	private String catName;

	private Integer catParentId;

	private String catPic;

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
