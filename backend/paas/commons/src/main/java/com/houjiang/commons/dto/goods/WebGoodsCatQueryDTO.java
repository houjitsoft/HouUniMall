package com.houjiang.commons.dto.goods;

import lombok.Data;

import java.util.List;

/**
 * @ClassName WebGoodsCatQueryDTO
 * @Description
 * @Author Amos
 * @Date 2023-05-22 16:51:47
 * @Version 1.0
 * @author Amos
 */
@Data
public class WebGoodsCatQueryDTO {

	/**
	 * 分类id
	 */
	private Integer catId;
	/**
	 * 标注该分类是否是 积分商品的分类 1 是 2 否
	 */
	private Integer isIntegralGoods;

	/**
	 * 分类id
	 */
	private List<Integer> catIdList;


}
