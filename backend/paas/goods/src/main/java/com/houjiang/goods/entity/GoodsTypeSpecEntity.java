package com.houjiang.goods.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 商品类型与规格对应表
 * @TableName goods_type_spec
 * @author Amos
 */
@TableName(value ="goods_type_spec")
@Data
public class GoodsTypeSpecEntity implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer typeSpecId;

    /**
     * 类型id
     */
    private Integer typeId;

    /**
     * 规格id
     */
    private Integer specId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}