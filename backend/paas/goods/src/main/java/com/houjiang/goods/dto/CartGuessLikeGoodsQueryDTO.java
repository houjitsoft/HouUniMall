package com.houjiang.goods.dto;

import lombok.Data;

import java.util.List;
/**
 * @ClassName CartGuessLikeGoodsQueryDTO
 * @Description 查询购物车底部猜你喜欢
 * @Author Amos
 * @Date 2023/5/25 9:47
 * @Version 1.0
 * @author Amos
 */
@Data
public class CartGuessLikeGoodsQueryDTO extends UserTokenBeanDTO {

	private Integer limitSize;
	private List<Integer> catIds;


}
