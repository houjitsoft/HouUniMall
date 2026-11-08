package com.houjiang.commons.dto.goods;


import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 商品设置的类目属性表(GoodsCategoryAttributes)表实体类
 *
 * @author Amos
 * @since 2024-11-01 10:17:55
 */
@Data
public class GoodsCategoryAttributesDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 商品属性值ID
     */
    private Integer id;

    /**
     * 商品ID
     */
    private Integer commonId;

    /**
     * 商品属性id
     */
    private Integer attributeId;

    /**
     * 商品属性名称
     */
    private String attributeName;
    
    /**
     * 商品属性值名称
     */
    private String attributeValueName;
    
    /**
     * 是否被搜索。0为不搜索、1为搜索
     */
    private Integer isSearch;
    
    /**
     * 是否必填。0为不必填、1为必填
     */
    private Integer isRequired;
    
    /**
     * 排序
     */
    private Integer displayOrder;

}
