package com.houjiang.payment.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 用户资源表
 * @TableName ${database.prefix.payment}hou_pay_user_resource
 * @author Amos
 */
@TableName(value ="hou_pay_user_resource")
@Data
public class PayUserResourceEntity implements Serializable {
    /**
     * 用户id
     */
    @TableId
    private String userId;

    /**
     * 待结算余额(废弃)
     */
    private BigDecimal userMoneyPendingSettlement;

    /**
     * 用户资金-预付款充值、转账、结算后的款项等等
     */
    private BigDecimal userMoney;

    /**
     * 用户充值套餐金额(废弃)
     */
    private BigDecimal userPackageMoney;

    /**
     * 冻结余额
     */
    private BigDecimal userMoneyFrozen;

    /**
     * 充值卡余额-只能从来购物
     */
    private BigDecimal userRechargeCard;

    /**
     * 时间戳
     */
    private Date ts;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        PayUserResourceEntity other = (PayUserResourceEntity) that;
        return (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getUserMoneyPendingSettlement() == null ? other.getUserMoneyPendingSettlement() == null : this.getUserMoneyPendingSettlement().equals(other.getUserMoneyPendingSettlement()))
            && (this.getUserMoney() == null ? other.getUserMoney() == null : this.getUserMoney().equals(other.getUserMoney()))
            && (this.getUserPackageMoney() == null ? other.getUserPackageMoney() == null : this.getUserPackageMoney().equals(other.getUserPackageMoney()))
            && (this.getUserMoneyFrozen() == null ? other.getUserMoneyFrozen() == null : this.getUserMoneyFrozen().equals(other.getUserMoneyFrozen()))
            && (this.getUserRechargeCard() == null ? other.getUserRechargeCard() == null : this.getUserRechargeCard().equals(other.getUserRechargeCard()))
            && (this.getTs() == null ? other.getTs() == null : this.getTs().equals(other.getTs()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getUserMoneyPendingSettlement() == null) ? 0 : getUserMoneyPendingSettlement().hashCode());
        result = prime * result + ((getUserMoney() == null) ? 0 : getUserMoney().hashCode());
        result = prime * result + ((getUserPackageMoney() == null) ? 0 : getUserPackageMoney().hashCode());
        result = prime * result + ((getUserMoneyFrozen() == null) ? 0 : getUserMoneyFrozen().hashCode());
        result = prime * result + ((getUserRechargeCard() == null) ? 0 : getUserRechargeCard().hashCode());
        result = prime * result + ((getTs() == null) ? 0 : getTs().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", userId=").append(userId);
        sb.append(", userMoneyPendingSettlement=").append(userMoneyPendingSettlement);
        sb.append(", userMoney=").append(userMoney);
        sb.append(", userPackageMoney=").append(userPackageMoney);
        sb.append(", userMoneyFrozen=").append(userMoneyFrozen);
        sb.append(", userRechargeCard=").append(userRechargeCard);
        sb.append(", ts=").append(ts);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}