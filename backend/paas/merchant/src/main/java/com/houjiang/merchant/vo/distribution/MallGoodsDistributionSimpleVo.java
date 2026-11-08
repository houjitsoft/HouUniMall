package com.houjiang.merchant.vo.distribution;

import com.alibaba.fastjson2.JSONArray;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class MallGoodsDistributionSimpleVo implements Serializable {

    private static final long serialVersionUID = 1L;


    @Schema(description = "商品id")
    private Integer goodsId;

    @Schema(description = "商品sku id")
    private Integer goodsSkuId;

    @Schema(description = "分销等级id")
    private Integer distributionLevelId;

    @Schema(description = "单独佣金比例 自购比例")
    private String aloneSelfRatio;

    @Schema(description = "单独佣金比例 一级比例")
    private String aloneOneRatio;

    @Schema(description = "单独佣金比例 二级比例")
    private String aloneTwoRatio;

    @Schema(description = "单独佣金金额 自购佣金")
    private String aloneSelfMoney;

    @Schema(description = "单独佣金金额 一级佣金")
    private String aloneOneMoney;

    @Schema(description = "单独佣金金额 二级佣金")
    private String aloneTwoMoney;

    private String price;

    private JSONArray skuValueArr;

}