package com.houjiang.payment.dto;

import lombok.Data;

/**
 * 记录
 * @author Amos
 */
@Data
public class RecordDTO {

    /**
     * 订单id
     */
    private String orderId;

    /**
     * 购买者id
     */
    private String buyerId;

    /**
     * 订单描述
     */
    private String tradeDesc;

    /**
     * 状态id
     */
    private Integer orderStateId;

    /**
     * 贸易付款金额
     */
    private String tradePaymentAmount;

}
