package com.houjiang.merchant.vo.mall;

import com.alibaba.fastjson2.JSONArray;
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
public class MallGoodsSkuDetailVo implements Serializable {

    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "商品ID")
    private Integer goodsId;

    @Schema(description = "规格图片")
    private String image;

    @Schema(description = "sku值 ID集合")
    private JSONArray skuValueIds;

    @Schema(description = "sku值 值集合")
    private JSONArray skuValueArr;

    @Schema(description = "销售价")
    private BigDecimal price;

    @Schema(description = "划线价")
    private BigDecimal linePrice;

    @Schema(description = "市场价")
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
