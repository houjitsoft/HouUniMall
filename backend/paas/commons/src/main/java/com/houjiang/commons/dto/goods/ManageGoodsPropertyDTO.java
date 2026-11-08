package com.houjiang.commons.dto.goods;

import lombok.Data;

/**
 * 商品属性
 * @author Amos
 */
@Data
public class ManageGoodsPropertyDTO {

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
