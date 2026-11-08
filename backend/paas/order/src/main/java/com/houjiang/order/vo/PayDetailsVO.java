package com.houjiang.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
public class PayDetailsVO {

    /**
     * 订单支付金额
     */
    BigDecimal orderPaymentAmount;

    /**
     * 订单取消时间
     */
    Long orderCancelTime;

    /**
     * 积分
     */
    Integer integral;

}
