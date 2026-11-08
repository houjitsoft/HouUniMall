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
 * 属性组表
 * @TableName goods_attribute_group
 * @author Amos
 */
@TableName(value ="goods_attribute_group")
@Data
@EqualsAndHashCode(callSuper = false)
public class GoodsAttributeGroupEntity implements Serializable {
    /**
     * 属性组ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 属性组名称
     */
    private String name;

    /**
     * 属性组备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 关联的属性类型列表（非数据库字段）
     */
    @TableField(exist = false)
    private List<GoodsAttributeGroupRelationEntity> attributes;

    private static final long serialVersionUID = 1L;
}
