package com.houjiang.goods.vo;


import com.baomidou.mybatisplus.annotation.TableField;
import com.houjiang.commons.util.querypage.PageHelper;
import com.houjiang.goods.entity.GoodsAttributeValueEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 商品属性类型表(MerchantGoodsAttributeType)表实体类
 *
 * @author Amos
 * @since 2024-10-31 14:07:56
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GoodsAttributeTypeVO extends PageHelper implements Serializable {

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
    
    /**
     * ID
     */
    private Integer attributeId;
    
    /**
     * 属性名称
     */
    private String attributeName;
    
    /**
     * 属性值列
     */
    private String attributeItem;

    /**
     * 属性值数量
     */
    private Integer attributeCount;

    /**
     * 商品属性值
     */
    private List<GoodsAttributeValueEntity> goodsAttributeValueDTOList;

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

    /**
     * 属性备注
     */
    private String attributeNotes;

}
