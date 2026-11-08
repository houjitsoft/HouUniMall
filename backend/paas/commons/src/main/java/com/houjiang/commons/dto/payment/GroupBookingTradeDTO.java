package com.houjiang.commons.dto.payment;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @ClassName GroupBookingTradeDTO
 * @Description
 * @Author Amos
 * @Date 2023/7/26 16:07
 * @Version 1.0
 * @author Amos
 */
@Data
public class GroupBookingTradeDTO {

    /**
     * 支付渠道
     */
    private Integer paymentChannelId;

    /**
     * 第三方支付金额
     */
    private BigDecimal tradeThirdPartyAmount;

    /**
     * 充值卡余额支付
     */
    private BigDecimal tradePaymentRechargeCard;
}
