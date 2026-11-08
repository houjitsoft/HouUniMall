package com.houjiang.merchant.common.entity.mall;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
public class MallGoodsDistribution implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value="id", type= IdType.AUTO)
    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "修改时间")
    private Long updateTime;

    @Schema(description = "删除时间")
    private Long deleteTime;

    @Schema(description = "商品id")
    private Integer goodsId;

    @Schema(description = "商品sku id")
    private Integer goodsSkuId;

    @Schema(description = "分销等级id")
    private Integer distributionLevelId;

    @Schema(description = "单独佣金比例 自购比例")
    private BigDecimal aloneSelfRatio;

    @Schema(description = "单独佣金比例 一级比例")
    private BigDecimal aloneOneRatio;

    @Schema(description = "单独佣金比例 二级比例")
    private BigDecimal aloneTwoRatio;

    @Schema(description = "单独佣金金额 自购佣金")
    private BigDecimal aloneSelfMoney;

    @Schema(description = "单独佣金金额 一级佣金")
    private BigDecimal aloneOneMoney;

    @Schema(description = "单独佣金金额 二级佣金")
    private BigDecimal aloneTwoMoney;

}