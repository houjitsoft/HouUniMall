package com.houjiang.goods.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author Amos
 * @date 2021-06-0317:15
 * @Description: 供应商商品规格
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SupplyGoodsSpecVO {
    /**
     * 商品规格
     */
    private String goodsSpec;

    /**
     * 商品价格
     */
    private BigDecimal goodsPrice;

    /**
     * 商品库存
     */
    private Integer goodsStock;

}
