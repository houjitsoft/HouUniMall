package com.houjiang.commons.vo.order;

import lombok.Data;

/**
 * @Description: 积分支付参数
 * @CreateDate: 2025/7/13 19:52
 * @author Amos
 */
@Data
public class PointsPayReqVO {

    private String orderId;

    /**
     * 支付方式1-余额支付，2-支付宝支付，3-微信支付，4-通联支付,5-健康账户余额支付,6-购物卡支付，7-充值卡+支付宝，8-充值卡+微信,9-蜂币支付，10-蜂币+支付宝，11-蜂币+微信,12-嘉币/劲豆，13-嘉币/劲豆+微信'
     */
    private Integer payCode;

    /**
     * 支付方式名称
     */
    private String paymentName;

    /**
     * 订单状态 1.待付款  3.待发货 4.已发货  6.确认收货 7.取消 8.退款中 9.已退款 11.待自提
     12 虚拟订单已过期
     */
    private Integer orderStatus;
}
