package com.houjiang.goods.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 贻亚通商品分类表
 * @TableName supplier_yiyatong_cat
 * @author Amos
 */
@TableName(value ="supplier_yiyatong_cat")
@Data
public class SupplierYiyatongCatEntity implements Serializable {
    /**
     * 分类id
     */
    @TableId
    private String id;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 父id
     */
    private String pid;

    /**
     * 等级
     */
    private Integer level;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}