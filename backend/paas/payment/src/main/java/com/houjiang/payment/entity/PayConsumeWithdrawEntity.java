package com.houjiang.payment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 提现申请表
 * @TableName ${database.prefix.payment}hou_pay_consume_withdraw
 * @author Amos
 */
@TableName(value ="hou_pay_consume_withdraw")
@Data
public class PayConsumeWithdrawEntity implements Serializable {
    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 会员支付ID
     */
    private String payUid;

    /**
     * 交易明细ID
     */
    private String orderid;

    /**
     * 提现金额
     */
    private BigDecimal amount;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date addTime;

    /**
     * 管理员
     */
    private String censor;

    /**
     * 操作时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date checkTime;

    /**
     * 是否成功"0" => ''待审核'',"1" => ''进行中'',"2" => ''打款完成'',"3" => ''通过'',"4" => ''不通过'',',
     */
    private Integer isSucceed;

    /**
     * 银行流水账号
     */
    private String bankflow;

    /**
     * 描述
     */
    private String con;

    /**
     * 银行名称
     */
    private String bank;

    /**
     * 银行详细地址
     */
    private String bankAddress;

    /**
     * 银行卡号
     */
    private String cardno;

    /**
     * 开户人姓名
     */
    private String cardname;

    /**
     * 手续费
     */
    private Double fee;

    /**
     * 平台审核备注
     */
    private String remark;

    /**
     * 店铺名
     */
    private String shopName;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 结算单号
     */
    private String settlementOrderId;

    /**
     * 付款金额
     */
    private BigDecimal payAmount;

    /**
     * 结算订单类型 1-普通订单 2-商家一键上架供应商销售订单商家结算单  3商家一键上架供应商销售订单供应商结算单 4直播分佣结算
     */
    private Boolean withdrawType;

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
        PayConsumeWithdrawEntity other = (PayConsumeWithdrawEntity) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getPayUid() == null ? other.getPayUid() == null : this.getPayUid().equals(other.getPayUid()))
            && (this.getOrderid() == null ? other.getOrderid() == null : this.getOrderid().equals(other.getOrderid()))
            && (this.getAmount() == null ? other.getAmount() == null : this.getAmount().equals(other.getAmount()))
            && (this.getAddTime() == null ? other.getAddTime() == null : this.getAddTime().equals(other.getAddTime()))
            && (this.getCensor() == null ? other.getCensor() == null : this.getCensor().equals(other.getCensor()))
            && (this.getCheckTime() == null ? other.getCheckTime() == null : this.getCheckTime().equals(other.getCheckTime()))
            && (this.getIsSucceed() == null ? other.getIsSucceed() == null : this.getIsSucceed().equals(other.getIsSucceed()))
            && (this.getBankflow() == null ? other.getBankflow() == null : this.getBankflow().equals(other.getBankflow()))
            && (this.getCon() == null ? other.getCon() == null : this.getCon().equals(other.getCon()))
            && (this.getBank() == null ? other.getBank() == null : this.getBank().equals(other.getBank()))
            && (this.getBankAddress() == null ? other.getBankAddress() == null : this.getBankAddress().equals(other.getBankAddress()))
            && (this.getCardno() == null ? other.getCardno() == null : this.getCardno().equals(other.getCardno()))
            && (this.getCardname() == null ? other.getCardname() == null : this.getCardname().equals(other.getCardname()))
            && (this.getFee() == null ? other.getFee() == null : this.getFee().equals(other.getFee()))
            && (this.getRemark() == null ? other.getRemark() == null : this.getRemark().equals(other.getRemark()))
            && (this.getShopName() == null ? other.getShopName() == null : this.getShopName().equals(other.getShopName()))
            && (this.getUserName() == null ? other.getUserName() == null : this.getUserName().equals(other.getUserName()))
            && (this.getSettlementOrderId() == null ? other.getSettlementOrderId() == null : this.getSettlementOrderId().equals(other.getSettlementOrderId()))
            && (this.getPayAmount() == null ? other.getPayAmount() == null : this.getPayAmount().equals(other.getPayAmount()))
            && (this.getWithdrawType() == null ? other.getWithdrawType() == null : this.getWithdrawType().equals(other.getWithdrawType()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getPayUid() == null) ? 0 : getPayUid().hashCode());
        result = prime * result + ((getOrderid() == null) ? 0 : getOrderid().hashCode());
        result = prime * result + ((getAmount() == null) ? 0 : getAmount().hashCode());
        result = prime * result + ((getAddTime() == null) ? 0 : getAddTime().hashCode());
        result = prime * result + ((getCensor() == null) ? 0 : getCensor().hashCode());
        result = prime * result + ((getCheckTime() == null) ? 0 : getCheckTime().hashCode());
        result = prime * result + ((getIsSucceed() == null) ? 0 : getIsSucceed().hashCode());
        result = prime * result + ((getBankflow() == null) ? 0 : getBankflow().hashCode());
        result = prime * result + ((getCon() == null) ? 0 : getCon().hashCode());
        result = prime * result + ((getBank() == null) ? 0 : getBank().hashCode());
        result = prime * result + ((getBankAddress() == null) ? 0 : getBankAddress().hashCode());
        result = prime * result + ((getCardno() == null) ? 0 : getCardno().hashCode());
        result = prime * result + ((getCardname() == null) ? 0 : getCardname().hashCode());
        result = prime * result + ((getFee() == null) ? 0 : getFee().hashCode());
        result = prime * result + ((getRemark() == null) ? 0 : getRemark().hashCode());
        result = prime * result + ((getShopName() == null) ? 0 : getShopName().hashCode());
        result = prime * result + ((getUserName() == null) ? 0 : getUserName().hashCode());
        result = prime * result + ((getSettlementOrderId() == null) ? 0 : getSettlementOrderId().hashCode());
        result = prime * result + ((getPayAmount() == null) ? 0 : getPayAmount().hashCode());
        result = prime * result + ((getWithdrawType() == null) ? 0 : getWithdrawType().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", payUid=").append(payUid);
        sb.append(", orderid=").append(orderid);
        sb.append(", amount=").append(amount);
        sb.append(", addTime=").append(addTime);
        sb.append(", censor=").append(censor);
        sb.append(", checkTime=").append(checkTime);
        sb.append(", isSucceed=").append(isSucceed);
        sb.append(", bankflow=").append(bankflow);
        sb.append(", con=").append(con);
        sb.append(", bank=").append(bank);
        sb.append(", bankAddress=").append(bankAddress);
        sb.append(", cardno=").append(cardno);
        sb.append(", cardname=").append(cardname);
        sb.append(", fee=").append(fee);
        sb.append(", remark=").append(remark);
        sb.append(", shopName=").append(shopName);
        sb.append(", userName=").append(userName);
        sb.append(", settlementOrderId=").append(settlementOrderId);
        sb.append(", payAmount=").append(payAmount);
        sb.append(", withdrawType=").append(withdrawType);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}