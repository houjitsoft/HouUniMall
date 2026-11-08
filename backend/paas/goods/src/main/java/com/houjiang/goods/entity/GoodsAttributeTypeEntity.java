package com.houjiang.goods.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 属性类型表
 * @TableName goods_attribute_type
 * @author Amos
 */
@TableName(value ="goods_attribute_type")
@Data
@EqualsAndHashCode(callSuper = false)
public class GoodsAttributeTypeEntity implements Serializable {
    /**
     * 属性类型ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 属性类型名称
     */
    private String name;

    /**
     * 属性类型备注
     */
    private String remark;

    /**
     * 属性值数量
     */
    private Integer valueCount;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 属性值列表（非数据库字段）
     */
    @TableField(exist = false)
    private List<GoodsAttributeTypeValueEntity> values;

    private static final long serialVersionUID = 1L;
}
