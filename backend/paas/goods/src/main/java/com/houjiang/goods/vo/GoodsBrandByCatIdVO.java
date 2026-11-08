package com.houjiang.goods.vo;

import lombok.Data;

/**
 * 商品品牌
 * @author Amos
 */
@Data
public class GoodsBrandByCatIdVO {

    /**
     * 品牌id
     */
    private Integer brandId;

    /**
     * 品牌名称
     */
    private String brandName;

    /**
     * 是否推荐 ： 0. 否； 1.是
     */
    private Integer brandRecommend;

    /**
     * 品牌类型id
     */
    private Integer typeBrandId;

}
