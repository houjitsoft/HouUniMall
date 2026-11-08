package com.houjiang.goods.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 怡亚通商品代发记录
 * @TableName supplier_goods_yiyatong
 * @author Amos
 */
@TableName(value ="supplier_goods_yiyatong")
@Data
public class SupplierGoodsYiyatongEntity implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 商品commonId
     */
    private Integer commonId;

    /**
     * 贻亚通商品spuId
     */
    private String spuId;

    /**
     * 商品goodsId
     */
    private Integer goodsId;

    /**
     * 贻亚通商品skuId
     */
    private String skuId;

    /**
     * 分类id
     */
    private String catId;

    /**
     * 代发店铺id
     */
    private Integer shopId;

    /**
     * 是否删除0:否,1:是
     */
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}