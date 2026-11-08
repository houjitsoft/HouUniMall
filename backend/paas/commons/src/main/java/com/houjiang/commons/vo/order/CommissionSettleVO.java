package com.houjiang.commons.vo.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author Amos
 * @date 2021-06-0716:38
 * @Description: 分佣结算
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommissionSettleVO {
    /**
     * 订单id
     */
    private String orderId;
    /**
     * 订单佣金id
     */
    private Integer orderCommissionId;
    /**
     * 分享佣金
     */
    private BigDecimal fxCommissionMoney;
    /**
     * 一级推广佣金
     */
    private BigDecimal oneTgCommissionMoney;
    /**
     * 二级推广佣金
     */
    private BigDecimal twoTgCommissionMoney;
    /**
     * 总佣金
     */
    private BigDecimal commissionMoney;
    /**
     * 佣金类型
     */
    private Integer commissionType;
    /**
     * 购买用户id
     */
    private String buyUserId;
    /**
     * 申请分享人id
     */
    private String shareUserId;

    /**
     * 申请分享人名称
     */
    private String shareUserName;

    /**
     * 申请分享人电话
     */
    private String shareUserMobile;

    /**
     * 一级推广用户id
     */
    private String tgOneUserId;

    /**
     * 一级推广用户名称
     */
    private String tgOneUserName;

    /**
     * 一级推广用户电话
     */
    private String tgOneUserMobile;

    /**
     * 二级推广用户id
     */
    private String tgTwoUserId;

    /**
     * 二级推广用户名称
     */
    private String tgTwoUserName;

    /**
     * 二级推广用户电话
     */
    private String tgTwoUserMobile;

    /**
     * 累计分享佣金
     */
    private BigDecimal totalShareCommission;

    /**
     * 累计推广佣金
     */
    private BigDecimal totalTgCommission;

    /**
     * 未提现佣金
     */
    private BigDecimal pendingWithdrawnCommission;

}
