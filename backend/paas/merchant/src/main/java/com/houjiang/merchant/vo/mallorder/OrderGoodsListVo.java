package com.houjiang.merchant.vo.mallorder;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
public class OrderGoodsListVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "商品ID")
    private Integer goodsId;

    @Schema(description = "SKU_ID")
    private Integer goodsSkuId;

    @Schema(description = "商品名称")
    private String goodsName;

    @Schema(description = "商品货号")
    private String goodsCode;

    @Schema(description = "SKU值名")
    private String goodsSkuValue;

    @Schema(description = "商品图片")
    private String goodsImage;

    @Schema(description = "购买数量")
    private Integer goodsNum;

    @Schema(description = "商品原价")
    private BigDecimal goodsOriginalPrice;

    @Schema(description = "商品单价")
    private BigDecimal goodsPrice;

    @Schema(description = "商品总额")
    private BigDecimal goodsMoney;

    @Schema(description = "订单总额")
    private BigDecimal money;

    @Schema(description = "应付金额")
    private BigDecimal needPayMoney;

    @Schema(description = "运费金额")
    private BigDecimal expressMoney;

    @Schema(description = "优惠金额")
    private BigDecimal couponMoney;

    @Schema(description = "售后状态: [1=无售后, 2=售后中, 3=售后完成]")
    private Integer afterSale;

    @Schema(description = "售后状态描述")
    private String afterMsg;

    @Schema(description = "核销时间")
    private Long verificationTime;
    @Schema(description = "核销状态")
    private Integer verificationStatus;
}
