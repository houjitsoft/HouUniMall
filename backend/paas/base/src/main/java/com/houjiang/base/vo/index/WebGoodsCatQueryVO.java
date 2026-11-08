package com.houjiang.base.vo.index;

import lombok.Data;

import java.util.List;
/**
 * @ClassName WebArticleBaseNameVO
 * @Description
 * @Author Amos
 * @Date 2023/4/12 14:54
 * @Version 1.0
 * @author Amos
 */
@Data
public class WebGoodsCatQueryVO {

	private Integer catId;
	/**
	 * 标注该分类是否是 积分商品的分类 1 是 2 否
	 */
	private Integer isIntegralGoods;

	private List<Integer> catIdList;


}
