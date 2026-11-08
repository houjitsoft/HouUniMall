package com.houjiang.goods.vo;

import lombok.Data;

import java.util.List;

/**
 * @author Amos
 */
@Data
public class GoodsBrandCatParamVO {
    private Integer id;
    /**
     * 分类id
     */
    private Integer catId;
    /**
     * 品牌id
     */
    private Integer brandId;
    /**
     * 品牌名称
     */
    private String brandName;
    /**
     * pc图片
     */
    private String brandPic;
    /**
     * h5
     */
    private String brandHf;

    /**
     * 品牌id
     */
    private List<Integer> brandIdList;


}
