package com.houjiang.order.vo;

import lombok.Data;

import java.util.List;

/**
 * @ClassName OrderInfo
 * @Description
 * @Author Amos
 * @Date 2022/7/22 17:55
 * @Version 1.0
 * @author Amos
 */
@Data
public class OrderInfoVO {
    /**
     * 支付单号
     */
    private String paymentNumber;

    /**
     * 订单ID列表
     */
    private List<String> orderIdList;

    /**
     * 订单取消时间，以Unix时间戳形式表示
     */
    private Long orderCancelTime;

    /**
     * 是否纯积分订单
     */
    private Integer isIntegral;

    /**
     * 支付时间
     */
    private String paymentDate;
}
