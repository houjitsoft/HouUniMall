package com.houjiang.payment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 充值表-支付回调callback使用-确认付款
 * @TableName ${database.prefix.payment}hou_pay_consume_deposit
 * @author Amos
 */
@TableName(value ="hou_pay_consume_deposit")
@Data
public class PayConsumeDepositEntity implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 交易号
     */
    private String depositTradeNo;

    /**
     * 用户号
     */
    private String depositBuyerId;

    /**
     * 交易金额
     */
    private BigDecimal depositTotalFee;

    /**
     * 充值的付款方式
     */
    private Integer depositPayChannel;

    /**
     * 交易创建时间
     */
    private Date depositGmtCreate;

    /**
     * 交易付款时间
     */
    private Date depositGmtPayment;

    /**
     * 交易关闭时间
     */
    private Date depositGmtClose;

    /**
     * 交易状态
     */
    private Boolean depositTradeStatus;

    /**
     * 充值成功以后的流水
     */
    private String depositReturnTradeNo;

    /**
     * 是否为套餐充值，1-是；0-普通充值
     */
    private Boolean depositIsRechargePackage;

    /**
     * 1是余额 2 是充值卡 
     */
    private Integer depositType;

    /**
     * 
     */
    private String depositOperator;

    /**
     * 后台充值操作人
     */
    private String orderOperator;

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
        PayConsumeDepositEntity other = (PayConsumeDepositEntity) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getDepositTradeNo() == null ? other.getDepositTradeNo() == null : this.getDepositTradeNo().equals(other.getDepositTradeNo()))
            && (this.getDepositBuyerId() == null ? other.getDepositBuyerId() == null : this.getDepositBuyerId().equals(other.getDepositBuyerId()))
            && (this.getDepositTotalFee() == null ? other.getDepositTotalFee() == null : this.getDepositTotalFee().equals(other.getDepositTotalFee()))
            && (this.getDepositPayChannel() == null ? other.getDepositPayChannel() == null : this.getDepositPayChannel().equals(other.getDepositPayChannel()))
            && (this.getDepositGmtCreate() == null ? other.getDepositGmtCreate() == null : this.getDepositGmtCreate().equals(other.getDepositGmtCreate()))
            && (this.getDepositGmtPayment() == null ? other.getDepositGmtPayment() == null : this.getDepositGmtPayment().equals(other.getDepositGmtPayment()))
            && (this.getDepositGmtClose() == null ? other.getDepositGmtClose() == null : this.getDepositGmtClose().equals(other.getDepositGmtClose()))
            && (this.getDepositTradeStatus() == null ? other.getDepositTradeStatus() == null : this.getDepositTradeStatus().equals(other.getDepositTradeStatus()))
            && (this.getDepositReturnTradeNo() == null ? other.getDepositReturnTradeNo() == null : this.getDepositReturnTradeNo().equals(other.getDepositReturnTradeNo()))
            && (this.getDepositIsRechargePackage() == null ? other.getDepositIsRechargePackage() == null : this.getDepositIsRechargePackage().equals(other.getDepositIsRechargePackage()))
            && (this.getDepositType() == null ? other.getDepositType() == null : this.getDepositType().equals(other.getDepositType()))
            && (this.getDepositOperator() == null ? other.getDepositOperator() == null : this.getDepositOperator().equals(other.getDepositOperator()))
            && (this.getOrderOperator() == null ? other.getOrderOperator() == null : this.getOrderOperator().equals(other.getOrderOperator()))
            && (this.getTs() == null ? other.getTs() == null : this.getTs().equals(other.getTs()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getDepositTradeNo() == null) ? 0 : getDepositTradeNo().hashCode());
        result = prime * result + ((getDepositBuyerId() == null) ? 0 : getDepositBuyerId().hashCode());
        result = prime * result + ((getDepositTotalFee() == null) ? 0 : getDepositTotalFee().hashCode());
        result = prime * result + ((getDepositPayChannel() == null) ? 0 : getDepositPayChannel().hashCode());
        result = prime * result + ((getDepositGmtCreate() == null) ? 0 : getDepositGmtCreate().hashCode());
        result = prime * result + ((getDepositGmtPayment() == null) ? 0 : getDepositGmtPayment().hashCode());
        result = prime * result + ((getDepositGmtClose() == null) ? 0 : getDepositGmtClose().hashCode());
        result = prime * result + ((getDepositTradeStatus() == null) ? 0 : getDepositTradeStatus().hashCode());
        result = prime * result + ((getDepositReturnTradeNo() == null) ? 0 : getDepositReturnTradeNo().hashCode());
        result = prime * result + ((getDepositIsRechargePackage() == null) ? 0 : getDepositIsRechargePackage().hashCode());
        result = prime * result + ((getDepositType() == null) ? 0 : getDepositType().hashCode());
        result = prime * result + ((getDepositOperator() == null) ? 0 : getDepositOperator().hashCode());
        result = prime * result + ((getOrderOperator() == null) ? 0 : getOrderOperator().hashCode());
        result = prime * result + ((getTs() == null) ? 0 : getTs().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", depositTradeNo=").append(depositTradeNo);
        sb.append(", depositBuyerId=").append(depositBuyerId);
        sb.append(", depositTotalFee=").append(depositTotalFee);
        sb.append(", depositPayChannel=").append(depositPayChannel);
        sb.append(", depositGmtCreate=").append(depositGmtCreate);
        sb.append(", depositGmtPayment=").append(depositGmtPayment);
        sb.append(", depositGmtClose=").append(depositGmtClose);
        sb.append(", depositTradeStatus=").append(depositTradeStatus);
        sb.append(", depositReturnTradeNo=").append(depositReturnTradeNo);
        sb.append(", depositIsRechargePackage=").append(depositIsRechargePackage);
        sb.append(", depositType=").append(depositType);
        sb.append(", depositOperator=").append(depositOperator);
        sb.append(", orderOperator=").append(orderOperator);
        sb.append(", ts=").append(ts);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}