package com.houjiang.goods.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 商品类型与品牌对应表
 * @TableName goods_type_brand
 * @author Amos
 */
@TableName(value ="goods_type_brand")
@Data
public class GoodsTypeBrandEntity implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer typeBrandId;

    /**
     * 类型id
     */
    private Integer typeId;

    /**
     * 规格id
     */
    private Integer brandId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}