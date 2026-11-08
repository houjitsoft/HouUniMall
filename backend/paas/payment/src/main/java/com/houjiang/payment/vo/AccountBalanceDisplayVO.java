package com.houjiang.payment.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 账户余额展示
 *
 * @author Amos
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountBalanceDisplayVO {

    /**
     * 待结算余额
     */
    private BigDecimal UserMoneyPendingSettlement;

    /**
     * 用户资金预付款充值、转账、结算后的款项等等
     */
    private BigDecimal UserMoney;

    /**
     * 用户充值套餐金额
     */
    private BigDecimal UserPackageMoney;

    /**
     * 冻结余额
     */
    private BigDecimal UserMoneyFrozen;

    /**
     * 充值卡余额
     */
    private BigDecimal UserRechargeCard;

    /**
     * 充值卡冻结余额
     */
    private BigDecimal UserRechargeCardFrozen;

    /**
     * 蜂币
     */
    private BigDecimal UserFixedRechargeCard;

    /**
     * 大宝账户
     */
    private BigDecimal UserBigTreasure;

    /**
     * 大宝冻结账户
     */
    private BigDecimal UserBigTreasureFrozen;

    /**
     * 小宝账户
     */
    private BigDecimal UserSmallTreasure;

    /**
     * 用户总资产
     */
    private BigDecimal TotalUserAssets;

    /**
     * 用户积分
     */
    private BigDecimal UserPoints;

    /**
     * 冻结积分
     */
    private BigDecimal UserPointsFrozen;

    /**
     * 信用额度
     */
    private BigDecimal UserCreditLimit;

    /**
     * 可用额度
     */
    private BigDecimal UserCreditAvailability;

    /**
     * 已还信用额度
     */
    private BigDecimal UserCreditReturn;

    /**
     * 白条还款周期
     */
    private Integer UserCreditCycle;

    /**
     * 成长值
     */
    private Integer UserGrowth;

    /**
     * 会员积分
     */
    private Integer UserIntegral;
    /**
     * 冻结积分
     */
    private Integer frozenIntegra;

    /**
     * 是否限额
     */
    private Integer accountIsLimit;

    /**
     * 可用金额
     */
    private BigDecimal availableAmount;

    /**
     * 是否授权账期 0，未授权 1，已授权 2，已冻结
     */
    private Integer isAccount;


}
