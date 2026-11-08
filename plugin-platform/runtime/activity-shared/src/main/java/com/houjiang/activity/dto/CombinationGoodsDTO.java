package com.houjiang.activity.dto;


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
public class CombinationGoodsDTO {

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
     * 商品名称
     */
    private String goodsSpec;

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

    /**
     * 店铺id
     */
    private Integer shopId;

    /**
     * 商品税率
     */
    private Double taxRate;

    /**
     * 商品图片
     */
    private String goodsImage;

    /**
     * 商品库存
     */
    private Integer goodsStock;

    /**
     * 商品spuId
     */
    private Integer commonId;

    /**
     * 商品订货编码
     */
    private String goodsCode;

}
