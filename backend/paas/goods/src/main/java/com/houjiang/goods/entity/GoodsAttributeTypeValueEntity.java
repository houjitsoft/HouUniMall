package com.houjiang.goods.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 属性类型值表
 * @TableName goods_attribute_type_value
 * @author Amos
 */
@TableName(value ="goods_attribute_type_value")
@Data
@EqualsAndHashCode(callSuper = false)
public class GoodsAttributeTypeValueEntity implements Serializable {
    /**
     * 属性值ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 属性类型ID
     */
    private Integer attributeTypeId;

    /**
     * 属性值名称
     */
    private String valueName;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 创建时间
     */
    private Date createTime;

    private static final long serialVersionUID = 1L;
}
