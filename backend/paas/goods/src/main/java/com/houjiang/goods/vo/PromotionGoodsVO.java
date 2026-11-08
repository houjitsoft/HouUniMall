package com.houjiang.goods.vo;

import lombok.Data;

import java.util.List;

/**
 * @author Amos
 */
@Data
public class PromotionGoodsVO {
    /**
     * 商品id
     */
    private List<PromotionGoodsListVO> goodsList;
    /**
     * 总数
     */
    private Integer totalCount;
}
