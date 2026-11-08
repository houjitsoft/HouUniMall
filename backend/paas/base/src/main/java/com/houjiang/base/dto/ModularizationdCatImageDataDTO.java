package com.houjiang.base.dto;

import lombok.Data;

/**
 * @ClassName ModularizationdCatImageData
 * @Description TODO
 * @Author Amos
 * @Date 2023/6/8 16:28
 * @Version 1.0
 * @author Amos
 */
@Data
public class ModularizationdCatImageDataDTO {
    /**
     * 分类名称
     */
    private String catName;
    /**
     * 分类图片
     */
    private String catImage;
    /**
     * 分类图片跳转类型 1 商品 2 分类
     */
    private Integer catImageType;
    /**
     * 商品id
     */
    private Integer goodsId;
    /**
     * 商品id
     */
    private Integer commonId;
    /**
     * 分类id
     */
    private Integer catId;
}
