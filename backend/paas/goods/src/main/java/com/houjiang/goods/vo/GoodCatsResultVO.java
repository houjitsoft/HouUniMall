package com.houjiang.goods.vo;

import com.houjiang.commons.vo.goods.GoodsCatResultVO;
import lombok.Data;

import java.util.List;

/**
 * @author Amos
 */
@Data
public class GoodCatsResultVO {

	/**
	 * 二级分类id
	 */
	private Integer secordCatId;

	/**
	 * 二级分类名
	 */
	private String secordCatName;

	/**
	 * 分类图片
	 */
	private String catPic;

	/**
	 * 分类信息
	 */
	List<GoodsCatResultVO> gdsCats;



}
