package com.houjiang.commons.vo.goods;

import lombok.Data;

/**
 * 品牌
 * @author Amos
 */
@Data
public class GoodsBrandVO {

    /**
     * 品牌id
     */
    private Integer brandId;

    /**
     * 品牌名称
     */
    private String brandName;

    /**
     * 分类id
     */
    private Integer catId;

    /**
     * 分类名称
     */
    private String catName;

    /**
     * 品牌图片
     */
    private String brandPic;

    /**
     * 是否启用 ： 0. 否； 1.是
     */
    private Integer brandEnable;

    /**
     * 品牌key
     */
    private String brandKey;

}
