package com.houjiang.payment.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @ClassName StripeOrder
 * @Author Amos
 * @Date 2024/12/18 15:19
 * @Version 1.0
 * @author Amos
 */
@Data
public class StripeOrder {
    /**
     * 主题
     */
    private String subject;

    /**
     * 正文
     */
    private String body;
    /**
     * 价格
     */
    private BigDecimal price;
    /**
     * 货币
     */
    private String currencyCode;
    /**
     * 订单号
     */
    private String orderId;
}
