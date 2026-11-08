package com.houjiang.goods.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 商品品牌表
 * @TableName goods_brand
 * @author Amos
 */
@TableName(value ="goods_brand")
@Data
public class GoodsBrandEntity implements Serializable {
    /**
     * 品牌编号
     */
    @TableId(type = IdType.AUTO)
    private Integer brandId;

    /**
     * 品牌名称
     */
    private String brandName;

    /**
     * 首字母
     */
    private String brandInitial;

    /**
     * 品牌图片
     */
    private String brandPic;

    /**
     * 品牌图片h5
     */
    private String brandHf;

    /**
     * 类名称
     */
    @TableField(exist = false)
    private String catName;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}