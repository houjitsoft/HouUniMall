package com.houjiang.goods.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 话题分类表
 * @TableName goods_tag_class_info
 * @author Amos
 */
@TableName(value ="goods_tag_class_info")
@Data
public class GoodsTagClassInfoEntity implements Serializable {
    /**
     * 分类ID
     */
    @TableId(type = IdType.AUTO)
    private Integer classId;

    /**
     * 分类名称
     */
    private String className;

    /**
     * 是否启用:1-开启,0-关闭
     */
    private Integer isActive;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 
     */
    private Date lastUpdatedTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}