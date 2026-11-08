package com.houjiang.merchant.validate.mall;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
public class MallGoodsSearchValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "店铺id")
    private Integer shopId;
    @Schema(description = "商品名称")
    private String name;
    @Schema(description = "类型商品")
    private String type;
    @Schema(description = "平台分类")
    private Integer category_id;
    @Schema(description = "店铺分类")
    private Integer shop_category_id;

    @Schema(description = "参与折扣")
    private Integer is_discount;

    @Schema(description = "参与分销")
    private Integer is_distribution;

}
