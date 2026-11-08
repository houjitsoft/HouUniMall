package com.houjiang.order.vo;


import lombok.Data;

import java.math.BigDecimal;

/**
 * @ClassName
 * @Description  经销单 经销折扣信息
 * @Author Amos
 * @Date 2024-08-01 09:45:07
 * @Version 1.0
 * @author Amos
 */
@Data
public class OrderDealerGoodsVO {

    /**
     * 订单号
     */
    private String orderId;
    /**
     * 商家skuid
     */
    private Integer goodsId;
    /**
     * 供应商skuid
     */
    private Integer supplierGoodsId;
    /**
     * 含税金额
     */
    private BigDecimal goodsPrice;
    /**
     * 含税金额  *  下单数量
     */
    private BigDecimal totalTaxInclusiveAmount;
    /**
     * 经销折扣
     */
    private BigDecimal dealerDiscount;
    /**
     * 经销折扣金额
     */
    private BigDecimal dealerDiscountAmout;
    /**
     *  店铺id
     */
    private Integer shopId;
    /**
     * 供应商店铺id
     */
    private Integer supplierShopId;
}
