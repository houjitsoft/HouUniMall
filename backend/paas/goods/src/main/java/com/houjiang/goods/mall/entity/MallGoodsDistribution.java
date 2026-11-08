package com.houjiang.goods.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Mall 商品分销关联实体
 * 表：hou_mall_goods_distribution
 * @author Amos
 */
@Data
@TableName("hou_mall_goods_distribution")
public class MallGoodsDistribution implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer createTime;
    private Integer updateTime;
    private Integer deleteTime;

    private Integer goodsId;
    private Integer goodsSkuId;
    private Integer distributionLevelId;
    private BigDecimal aloneSelfRatio;
    private BigDecimal aloneOneRatio;
    private BigDecimal aloneTwoRatio;
    private BigDecimal aloneSelfMoney;
    private BigDecimal aloneOneMoney;
    private BigDecimal aloneTwoMoney;
}
