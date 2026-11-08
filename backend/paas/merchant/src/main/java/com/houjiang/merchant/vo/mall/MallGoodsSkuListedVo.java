package com.houjiang.merchant.vo.mall;

import com.alibaba.fastjson2.JSONArray;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.houjiang.merchant.vo.ms.GoodsMsVo;
import com.houjiang.merchant.vo.pt.GoodsPtVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author Amos
 */
@Data
public class MallGoodsSkuListedVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "创建时间")
    @JsonProperty("create_time")
    private String createTime;

    @Schema(description = "修改时间")
    @JsonProperty("update_time")
    private String updateTime;

    @Schema(description = "删除时间")
    @JsonProperty("delete_time")
    private Long deleteTime;

    @Schema(description = "店铺id")
    @JsonProperty("shop_id")
    private Integer shopId;

    @Schema(description = "商品ID")
    @JsonProperty("goods_id")
    private Integer goodsId;

    @Schema(description = "规格图片")
    private String image;

    @Schema(description = "sku值 ID集合")
    @JsonProperty("sku_value_ids")
    private JSONArray skuValueIds;

    @Schema(description = "sku值 值集合")
    @JsonProperty("sku_value_arr")
    private JSONArray skuValueArr;

    @Schema(description = "销售价")
    private BigDecimal price;

    @Schema(description = "划线价")
    @JsonProperty("line_price")
    private BigDecimal linePrice;

    @Schema(description = "市场价")
    @JsonProperty("market_price")
    private BigDecimal marketPrice;

    @Schema(description = "库存")
    private Integer stock;

    @Schema(description = "重量")
    private BigDecimal weight;

    @Schema(description = "体积")
    private BigDecimal volume;

    @Schema(description = "条形码")
    private String code;
}
