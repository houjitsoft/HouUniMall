package com.houjiang.order.dto;

import lombok.Data;

/**
 * 订单商品折扣
 * @author Amos
 */
@Data
public class OrderGoodsDiscountDTO {

    /**
     * 商品价格
     */
    private Double goodsPrice;

    /**
     * 订单商品数量
     */
    private Double orderGoodsNum;

    /**
     * 红包
     */
    private Double orderRptPrice;

    /**
     * 代金券支付
     */
    private Double voucherPrice;

}
