package com.houjiang.merchant.vo.mall;

import com.alibaba.fastjson2.JSONArray;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author Amos
 */
@Data
public class MallGoodsDistributionSkuVo implements Serializable {

    private static final long serialVersionUID = 1L;


    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "创建时间")
    private String createTime;

    @Schema(description = "修改时间")
    private String updateTime;

    @Schema(description = "删除时间")
    private String deleteTime;

    @Schema(description = "店铺id")
    private Integer shopId;

    @Schema(description = "商品ID")
    private Integer goodsId;

    @Schema(description = "规格图片")
    private String image;

    @Schema(description = "sku值 ID集合")
    private JSONArray skuValueIds;

    @Schema(description = "sku值 值集合")
    private JSONArray skuValueArr;

    @Schema(description = "销售价")
    private String price;

    @Schema(description = "划线价")
    private String linePrice;

    @Schema(description = "市场价")
    private String marketPrice;

    @Schema(description = "库存")
    private Integer stock;

    @Schema(description = "重量")
    private String weight;

    @Schema(description = "体积")
    private String volume;

    @Schema(description = "条形码")
    private String code;

}
