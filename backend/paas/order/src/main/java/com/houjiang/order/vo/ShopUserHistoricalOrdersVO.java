package com.houjiang.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @ClassName ShopUserHistoricalOrdersVO
 * @Description 店铺会员历史订单
 * @Author Amos
 * @Date 2024/9/5 15:22
 * @Version 1.0
 * @author Amos
 */
@Data
public class ShopUserHistoricalOrdersVO {
    /**
     * 订单编号
     */
    private String orderId;
    /**
     * 下单时间
     */
    private String orderCreateTime;
    /**
     * 订单金额（元）
     */
    private BigDecimal orderPaymentAmount;
    /**
     * 支付单号
     */
    private String paymentNumber;
    /**
     * 支付方式
     */
    private Integer payCode;
    /**
     * 支付时间
     */
    private String paymentTime;
    /**
     * 订单完成时间
     */
    private String orderFinishedTime;
}
