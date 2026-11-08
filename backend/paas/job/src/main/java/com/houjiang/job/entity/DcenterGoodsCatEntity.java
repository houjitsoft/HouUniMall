package com.houjiang.job.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 数据大屏商品分类表
 * @TableName ${database.prefix.datav}hou_datav_goods_cat
 * @author Amos
 */
@TableName(value ="${database.prefix.datav}hou_datav_goods_cat")
@Data
public class DcenterGoodsCatEntity implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer catId;

    /**
     *  分类名称
     */
    private String catName;

    /**
     * 父类
     */
    private Integer catParentId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}