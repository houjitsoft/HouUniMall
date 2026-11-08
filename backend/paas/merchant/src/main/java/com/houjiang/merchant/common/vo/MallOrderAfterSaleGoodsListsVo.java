package com.houjiang.merchant.common.vo;

import com.alibaba.fastjson2.JSONArray;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
public class MallOrderAfterSaleGoodsListsVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    private Integer id;
    @Schema(description = "快递费用")
    private Integer expressMoney;
    @Schema(description = "商品图片")
    private String goodsImage;
    @Schema(description = "商品费用")
    private Integer goodsMoney;
    @Schema(description = "商品名称")
    private String goodsName;
    @Schema(description = "商品数量")
    private Integer goodsNum;
    @Schema(description = "商品单价")
    private BigDecimal goodsPrice;
    @Schema(description = "商品skuID")
    private Object goodsSkuValue;
    @Schema(description = "商品单价")
    private BigDecimal money;
    @Schema(description = "付款")
    private BigDecimal needPayMoney;
    @Schema(description = "订单项ID")
    private Integer orderGoodsId;
    @Schema(description = "订单ID")
    private Integer orderId;
    @Schema(description = "订单状态")
    private Integer orderStatus;
    @Schema(description = "支付费用")
    private BigDecimal payMoney;
    @Schema(description = "userId")
    private Integer userId;
}
