package com.houjiang.merchant.vo.mall;

import com.alibaba.fastjson2.JSONArray;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
public class MallGoodsUserLevelDiscountVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "商品ID")
    private Integer goodsId;

    @Schema(description = "商品sku id")
    private Integer goodsSkuId;

/**
 * @Schema(description = "等级折扣 1参与等级折扣 0无等级折扣")
 * private Integer isDiscount;
 */

    @Schema(description = "折扣金额")
    private BigDecimal discountPrice;

    @Schema(description = "折扣率")
    private BigDecimal discountRatio;

/**
 * @Schema(description = "会员等级")
 * private String name;
 */

    @Schema(description = "单价")
    private BigDecimal price;

    @Schema(description = "会员等级ID")
    private Integer userLevelId;

    @Schema(description = "sku值 值集合")
    @JsonProperty("sku_value_arr")
    private JSONArray skuValueArr;

    @Schema(description = "单独折扣")
    private BigDecimal aloneDiscount;

    @Schema(description = "单独价格")
    private BigDecimal alonePrice;
}
