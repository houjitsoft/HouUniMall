package com.houjiang.order.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @ClassName AdjustPriceDTO
 * @Description
 * @Author Amos
 * @Date 2023/8/15 16:00
 * @Version 1.0
 * @author Amos
 */
@Data
public class AdjustPriceDTO {

    /**
     * 订单编号
     */
    private String orderId;

    /**
     * 存储改价之后的金额
     */
    private BigDecimal calculateAfterMoney;
}
