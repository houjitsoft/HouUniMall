package com.houjiang.payment.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 支付参数
 *
 * @author Amos
 */
@Data
public class RecordVO {

    /**
     * 订单id
     */
    private String orderId;

    /**
     * 描述
     */
    private String recordDesc;

    /**
     * 实际支付单号
     */
    private String recordPayorder;

    /**
     * 记录总额
     */
    private BigDecimal recordMoney;

}
