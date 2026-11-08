package com.houjiang.goods.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 商品属性值表(MerchantGoodsAttributeValue)表实体类
 *
 * @author Amos
 * @since 2024-10-31 14:08:56
 */
@Data
@TableName("goods_attribute_value")
public class GoodsAttributeValueEntity implements Serializable {

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
    
    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Integer attributeValueId;
    
    /**
     * 属性名称
     */
    private String attributeValueName;
    
    /**
     * 所属类型id
     */
    private Integer attributeId;
    
    /**
     * 排序
     */
    private Integer displayOrder;

}
