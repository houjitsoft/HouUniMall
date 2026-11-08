package com.houjiang.base.vo;

import lombok.Data;

import java.util.List;
/**
 * @ClassName MobConfigGoodsVO
 * @Description
 * @Author Amos
 * @Date 2023/4/12 14:50
 * @Version 1.0
 * @author Amos
 */
@Data
public class MobConfigGoodsVO {

	private List<Integer> goodsIdList;

	private Integer goodsNum;

	private List<Integer> catIds;

}
