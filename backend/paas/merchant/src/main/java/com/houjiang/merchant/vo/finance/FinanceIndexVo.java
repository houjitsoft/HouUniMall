package com.houjiang.merchant.vo.finance;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
public class FinanceIndexVo implements Serializable {

    private static final long serialVersionUID = 1L;
    @Schema(description = "营业额")
    private BigDecimal saleMoney;
    @Schema(description = "成交订单数")
    private Integer saleOrders;
    @Schema(description = "退款金额")
    private BigDecimal refundMoney;
    @Schema(description = "待退款金额")
    private BigDecimal refundWaitMoney;
    @Schema(description = "已结算订单")
    private Integer settlementOrders;
    @Schema(description = "已结算营业额")
    private BigDecimal settlementOrderMoney;
    @Schema(description = "待结算订单")
    private Integer settlementWaitNum;
    @Schema(description = "待结算营业额")
    private BigDecimal settlementWaitMoney;
    @Schema(description = "已结算入账金额")
    private BigDecimal settlementMoney;
    @Schema(description = "已结算交易服务费")
    private BigDecimal settlementServiceMoney;
    @Schema(description = "可提现金额")
    private BigDecimal canWithdrawMoney;
    @Schema(description = "提现中金额")
    private BigDecimal withdrawMoney;
    @Schema(description = "提现手续费")
    private BigDecimal withdrawServiceMoney;
    @Schema(description = "提现成功金额")
    private BigDecimal withdrawSuccessMoney;

}
