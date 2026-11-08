package com.houjiang.commons.vo.order;

import lombok.Data;

/**
 * 订单状态信息VO
 * @author Amos
 */
@Data
public class OrderStatusVO {
    /**
     * 订单ID
     */
    private String orderId;

    /**
     * 订单实付金额
     */
    private String orderPaymentAmount;

    /**
     * 订单商品金额
     */
    private String orderGoodsAmount;

    /**
     * 退款金额
     */
    private String returnCash;

    /**
     * 用户手机号
     */
    private String userMobile;

    /**
     * 订单状态
     */
    private Integer orderStatus;

    /**
     * 订单退款状态
     */
    private Integer orderRefundStatus;

    /**
     * 订单退货状态
     */
    private Integer orderReturnStatus;

    /**
     * 买家用户ID
     */
    private String buyerUserId;
}
