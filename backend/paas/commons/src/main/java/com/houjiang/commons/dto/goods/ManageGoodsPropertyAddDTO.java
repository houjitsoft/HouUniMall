package com.houjiang.commons.dto.goods;

import lombok.Data;

/**
 * @ClassName ManageGoodsPropertyAddDTO
 * @Description 商品属性
 * @Author Amos
 * @Date 2023/6/26 15:08
 * @Version 1.0
 * @author Amos
 */
@Data
public class ManageGoodsPropertyAddDTO {

    /**
     * 商品id
     */
    private Integer commonId;

    /**
     * 属性id
     */
    private Integer propertyId;

    /**
     * 属性值
     */
    private String propertyValue;

    /**
     * 属性名称
     */
    private String propertyName;
}
