package com.houjiang.payment.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 支付宝回调参数用于对比
 *
 * @author Amos
 */
@Data
public class PayConsumeRecordDTO {

    /**
     * 订单id
     */
    private String orderId;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 用户昵称
     */
    private String userNickname;

    /**
     * 金额
     */
    private BigDecimal recordMoney;



    /**
     * 1-用家，2-批发
     */
    private Integer customerType;

}
