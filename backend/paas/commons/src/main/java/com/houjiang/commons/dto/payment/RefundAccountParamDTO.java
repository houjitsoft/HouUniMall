package com.houjiang.commons.dto.payment;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @ClassName RefundAccountParamDTO
 * @Description
 * @Author Amos
 * @Date 2023/7/26 16:31
 * @Version 1.0
 * @author Amos
 */
@Data
public class RefundAccountParamDTO {

    /**
     * token
     */
    private String token;

    /**
     * 店铺id
     */
    private Integer shopId;

    /**
     * 会计期间ID
     */
    private String accountingPeriodId;

    /**
     * 退款价格
     */
    private BigDecimal refundPrice;
}
