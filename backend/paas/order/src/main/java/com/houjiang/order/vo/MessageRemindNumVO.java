package com.houjiang.order.vo;

import lombok.Data;

/**
 * 消息计数
 *
 * @author Amos
 */
@Data
public class MessageRemindNumVO {

    /**
     * 待付款
     */
    private Integer waitingPayment;

    /**
     * 待发货
     */
    private Integer waitingSendGoods;

    /**
     * 待收货
     */
    private Integer waitForReceivingGoods;

    /**
     * 待评价
     */
    private Integer waitingEvaluation;

    /**
     * 退款及售后
     */
    private Integer refundAndAfterSale;

    /**
     * 已取消的订单
     */
    private Integer cancelOrder;


}
