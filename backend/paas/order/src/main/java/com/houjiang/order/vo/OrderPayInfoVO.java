package com.houjiang.order.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Amos
 * @date 2021-05-3114:24
 * @Description: 订单信息
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderPayInfoVO {
    /**
     * 订单ID
     */
    private String orderId;

    /**
     * 支付单号
     */
    private String paymentNumber;

    /**
     * 订单类型
     */
    private Integer orderType;
}
