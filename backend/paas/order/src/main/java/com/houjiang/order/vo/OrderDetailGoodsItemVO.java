package com.houjiang.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @ClassName OrderDetailGoodsItemVO
 * @Description 订单商品详情
 * @Author Amos
 * @Date 2024/4/29 16:25
 * @Version 1.0
 * @author Amos
 */
@Data
public class OrderDetailGoodsItemVO {

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 订单规格中文名称
     */
    private String orderSpecChineseValue;

    /**
     * 商品数量
     */
    private String orderGoodsNum;

    /**
     * 商品单价
     */
    private BigDecimal goodsPrice;

    /**
     * 付款方式-支付方式名称
     */
    private String paymentName;

    /**
     * 实付金额
     */
    private BigDecimal orderGoodsPaymentAmount;
}
