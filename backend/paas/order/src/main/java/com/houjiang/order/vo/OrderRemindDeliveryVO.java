package com.houjiang.order.vo;

import lombok.Data;

/**
 * @author Amos
 */
@Data
public class OrderRemindDeliveryVO {

    /**
     * 订单ID
     */
    private String orderId;

    /**
     * 订单收货人姓名
     */
    private String orderReceiverName;

    /**
     * 买家用户名
     */
    private String buyerUserName;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 付款时间
     */
    private String paymentTime;

    /**
     * 时差
     */
    private int timeDiff;

    /**
     * 订单卖家联系方式
     */
    private String orderSellerContact;

    /**
     * 店铺ID
     */
    private String shopId;

}
