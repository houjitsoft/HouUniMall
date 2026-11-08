package com.houjiang.merchant.vo.mall;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author Amos
 */
@Data
public class MallGoodsSkuVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "id")
    private Integer id;

    @Schema(description = "ids")
    private String ids;

    @Schema(description = "商品ID")
    private Integer goodsId;

    @Schema(description = "规格图片")
    private String image;

    @Schema(description = "sku值(ID集合)")
    private List<String> skuValueIds;

    @Schema(description = "sku值(值集合)")
    private List<String> skuValueArr;

    @Schema(description = "销售价")
    private BigDecimal price;

    @Schema(description = "划线价")
    private BigDecimal linePrice = new BigDecimal(0);

    @Schema(description = "市场价")
    private BigDecimal marketPrice = new BigDecimal(0);

    @Schema(description = "库存")
    private Integer stock = 0;

    @Schema(description = "重量")
    private BigDecimal weight = new BigDecimal(0);

    @Schema(description = "体积")
    private BigDecimal volume = new BigDecimal(0);

    @Schema(description = "条形码")
    private String code;

}
