package com.houjiang.goods.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 属性组关联表
 * @TableName goods_attribute_group_relation
 * @author Amos
 */
@TableName(value ="goods_attribute_group_relation")
@Data
@EqualsAndHashCode(callSuper = false)
public class GoodsAttributeGroupRelationEntity implements Serializable {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 属性组ID
     */
    private Integer attributeGroupId;

    /**
     * 属性类型ID
     */
    private Integer attributeTypeId;

    /**
     * 属性类型名称（冗余字段）
     */
    private String attributeTypeName;

    /**
     * 是否设为筛选条件：0-否，1-是
     */
    private Integer isFilter;

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
