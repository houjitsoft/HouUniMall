package com.houjiang.goods.dto;

import lombok.Data;

import java.util.List;

/**
 * @author Amos
 */
@Data
public class GoodsAttributeDTO {

    /**
     * 商品属性组ID
     */
    private Integer bindId;

    /**
     * 所属类型id
     */
    private Integer attributeId;

    /**
     * 所属类型id
     */
    private Integer attributeName;

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
