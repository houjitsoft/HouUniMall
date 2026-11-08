package com.houjiang.commons.vo.activity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * (ActivityCombinationGoods)表实体类
 *
 * @author Amos
 * @since 2024-04-16 11:27:10
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CombinationGoodsInfoVO {

    /**
     * 组合商品id
     */
    private Integer combinationGoodsId;


    /**
     * 组合活动id
     */
    private Integer combinationId;

    /**
     * 组合活动类型 1固定组合 2搭配组合
     */
    private Integer combinationType;

    /**
     * 是否为主商品 0否 1是
     */
    private Integer isMainProduct;

    /**
     * 商品id
     */
    private Integer goodsId;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 商品价格
     */
    private BigDecimal goodsPrice;

    /**
     * 商品数量
     */
    private Integer goodsNum;

    /**
     * 搭配价格
     */
    private BigDecimal matchingPrices;

}
