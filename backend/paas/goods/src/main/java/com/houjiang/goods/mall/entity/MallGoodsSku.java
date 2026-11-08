package com.houjiang.goods.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Mall 商品 SKU 实体
 * 表：hou_mall_goods_sku
 * @author Amos
 */
@Data
@TableName("hou_mall_goods_sku")
public class MallGoodsSku implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer createTime;
    private Integer updateTime;
    private Integer deleteTime;

    private Integer shopId;
    private Integer goodsId;
    private String image;
    private String skuValueIds;
    private String skuValueArr;
    private BigDecimal price;
    private BigDecimal linePrice;
    private BigDecimal marketPrice;
    private Integer stock;
    private BigDecimal weight;
    private BigDecimal volume;
    private String code;
}
