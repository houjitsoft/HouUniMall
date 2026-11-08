package com.houjiang.activity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 组合商品表实体类
 *
 * @author Amos
 * @date 2026-01-04
 */
@Data
@TableName("hou_addon_activity_combination_goods")
public class ActivityCombinationGoods implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 组合商品id
     */
    @TableId(value = "combination_goods_id", type = IdType.AUTO)
    private Long combinationGoodsId;

    /**
     * 组合活动id
     */
    private Long combinationId;

    /**
     * 组合活动类型 1固定组合 2搭配组合
     */
    private String combinationType;

    /**
     * 是否为主商品 0否 1是
     */
    private String isMainProduct;

    /**
     * 商品id
     */
    private Long goodsId;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 商品价格
     */
    private BigDecimal goodsPrice;

    /**
     * 商品数量
     */
    private Integer goodsNum;

    /**
     * 搭配价格
     */
    private BigDecimal matchingPrices;
}
