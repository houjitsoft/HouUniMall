package com.houjiang.goods.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 商品属性组绑定属性表(MerchantGoodsGroupBindAttribute)表实体类
 *
 * @author Amos
 * @since 2024-10-31 16:03:54
 */
@Data
@TableName("goods_group_bind_attribute")
public class GoodsGroupBindAttributeEntity implements Serializable {

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
    
    /**
     * 商品属性组ID
     */
    @TableId(type = IdType.AUTO)
    private Integer bindId;
    
    /**
     * 商品属性组ID
     */
    private Integer groupId;
    
    /**
     * 关联商品属性id
     */
    private Integer attributeId;
    
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
