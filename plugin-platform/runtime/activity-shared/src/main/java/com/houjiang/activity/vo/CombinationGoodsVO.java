package com.houjiang.activity.vo;


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
public class CombinationGoodsVO {
        
    /**
    * 商品id
    */
    private Integer goodsId;
        
    /**
    * 商品名称
    */
    private String goodsName;

    /**
     * 商品名称
     */
    private String goodsSpec;

    /**
    * 商品价格
    */
    private BigDecimal goodsPrice;

}
