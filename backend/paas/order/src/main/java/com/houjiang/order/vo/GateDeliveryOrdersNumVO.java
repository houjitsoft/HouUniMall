package com.houjiang.order.vo;

import lombok.Data;

/**
 * @ClassName GateDeliveryOrdersNumVO
 * @Description TODO
 * @Author Amos
 * @Date 2024/5/15 14:26
 * @Version 1.0
 * @author Amos
 */
@Data
public class GateDeliveryOrdersNumVO {
    /**
     * 待付款
     */
    private Integer waitPay;
    /**
     * 待接单
     */
    private Integer waitOrder;
    /**
     * 待取货
     */
    private Integer waitPick;
    /**
     * 待配送
     */
    private Integer waitPickup;
    /**
     * 配送中
     */
    private Integer waitDelivery;
    /**
     * 已完成
     */
    private Integer waitComplete;
    /**
     * 售后
     */
    private Integer waitAfterSale;
}
