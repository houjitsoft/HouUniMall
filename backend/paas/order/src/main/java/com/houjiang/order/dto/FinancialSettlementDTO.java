package com.houjiang.order.dto;


import com.houjiang.commons.util.querypage.PageHelper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialSettlementDTO extends PageHelper {

    /**
     * 商店ID，用于唯一标识一个商店。
     */
    private Integer shopId;

    /**
     * 商店名称，用于显示商店的名称。
     */
    private String shopName;

    /**
     * 商店的开始营业时间。
     */
    private String startTime;

    /**
     * 商店的结束营业时间。
     */
    private String endTime;

    /**
     *订单总额
     */
    private BigDecimal osAmount;
    /**
     *退单总额
     */
    private BigDecimal osOrderReturnAmount;
    /**
     *红包总额
     */
    private BigDecimal osRedpacketAmount;
    /**
     *退还红包总额
     */
    private BigDecimal osRedpacketReturnAmount;
    /**
     *罚款总额
     */
    private BigDecimal osFineAmount;
    /**
     *提现总额
     */
    private BigDecimal withdrawalAmount; 
    /**
     *未提现总额
     */
    private BigDecimal undrawnAmount;

    /**
     * 未结算金额字段的声明。
     * <p>
     * 该字段用于存储尚未进行结算的金额，以BigDecimal类型表示，确保计算和存储的精确性。
     * 未结算金额是指在某个特定时刻，还未进行过结算的交易金额总和。
     * 它在财务系统中广泛应用，用于跟踪和管理未结算的财务交易。
     */
    private BigDecimal notSettlementMoney;
}
