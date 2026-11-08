package com.houjiang.goods.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * Mall 商品 SKU 规格名实体
 * 表：hou_mall_goods_sku_name
 * @author Amos
 */
@Data
@TableName("hou_mall_goods_sku_name")
public class MallGoodsSkuName implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer createTime;
    private Integer updateTime;
    private Integer deleteTime;

    private Integer goodsId;
    private String name;
}
