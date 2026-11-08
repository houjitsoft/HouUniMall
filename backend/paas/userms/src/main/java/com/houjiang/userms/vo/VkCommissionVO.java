package com.houjiang.userms.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 微客佣金信息
 * @author Amos
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VkCommissionVO {

    /**
     * 用户id
     */
    private String userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 可提现佣金
     */
    private BigDecimal canWithdrawCommission;

    /**
     * 今日结算
     */
    private BigDecimal todaySettlement;

    /**
     * 累计结算
     */
    private BigDecimal totalSettlement;

    /**
     * 累计提现
     */
    private BigDecimal totalWithdraw;

    /**
     * 今日预估收益
     */
    private BigDecimal todayEstimatedIncome;

    /**
     * 累计预估收益
     */
    private BigDecimal totalEstimatedIncome;

}
