package com.houjiang.commons.dto.goods;

import lombok.Data;

import java.util.List;

/**
 * 商品
 * @author Amos
 */
@Data
public class MobConfigGoodsDTO {

    /**
     * 商品id集合
     */
    private List<Integer> goodsIdList;

    /**
     * 商品数量
     */
    private Integer goodsNum;

    /**
     * 分类id集合
     */
    private List<Integer> catIds;


}
