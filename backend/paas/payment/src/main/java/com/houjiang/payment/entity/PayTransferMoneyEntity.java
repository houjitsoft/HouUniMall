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
 * 转帐，红包
 * @TableName ${database.prefix.payment}hou_pay_transfer_money
 * @author Amos
 */
@TableName(value ="hou_pay_transfer_money")
@Data
public class PayTransferMoneyEntity implements Serializable {
    /**
     * 转账编号
     */
    @TableId(type = IdType.AUTO)
    private Integer transferId;

    /**
     * 发起转帐或红包的人
     */
    private String fromUser;

    /**
     * 接收人
     */
    private String toUser;

    /**
     * 发送时间
     */
    private Date sendTime;

    /**
     * 收到时间
     */
    private Date receiveTime;

    /**
     * 转账金额
     */
    private BigDecimal transferMoney;

    /**
     * 状态：0为处理中,1为已收到，2为过期
     */
    private Byte transferStatus;

    /**
     * 转账说明
     */
    private String transferTxt;

    /**
     * 交易类型： 1 转帐 2红包
     */
    private Boolean transferType;

    /**
     * 交易单号
     */
    private String transactionNumber;

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
        PayTransferMoneyEntity other = (PayTransferMoneyEntity) that;
        return (this.getTransferId() == null ? other.getTransferId() == null : this.getTransferId().equals(other.getTransferId()))
            && (this.getFromUser() == null ? other.getFromUser() == null : this.getFromUser().equals(other.getFromUser()))
            && (this.getToUser() == null ? other.getToUser() == null : this.getToUser().equals(other.getToUser()))
            && (this.getSendTime() == null ? other.getSendTime() == null : this.getSendTime().equals(other.getSendTime()))
            && (this.getReceiveTime() == null ? other.getReceiveTime() == null : this.getReceiveTime().equals(other.getReceiveTime()))
            && (this.getTransferMoney() == null ? other.getTransferMoney() == null : this.getTransferMoney().equals(other.getTransferMoney()))
            && (this.getTransferStatus() == null ? other.getTransferStatus() == null : this.getTransferStatus().equals(other.getTransferStatus()))
            && (this.getTransferTxt() == null ? other.getTransferTxt() == null : this.getTransferTxt().equals(other.getTransferTxt()))
            && (this.getTransferType() == null ? other.getTransferType() == null : this.getTransferType().equals(other.getTransferType()))
            && (this.getTransactionNumber() == null ? other.getTransactionNumber() == null : this.getTransactionNumber().equals(other.getTransactionNumber()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getTransferId() == null) ? 0 : getTransferId().hashCode());
        result = prime * result + ((getFromUser() == null) ? 0 : getFromUser().hashCode());
        result = prime * result + ((getToUser() == null) ? 0 : getToUser().hashCode());
        result = prime * result + ((getSendTime() == null) ? 0 : getSendTime().hashCode());
        result = prime * result + ((getReceiveTime() == null) ? 0 : getReceiveTime().hashCode());
        result = prime * result + ((getTransferMoney() == null) ? 0 : getTransferMoney().hashCode());
        result = prime * result + ((getTransferStatus() == null) ? 0 : getTransferStatus().hashCode());
        result = prime * result + ((getTransferTxt() == null) ? 0 : getTransferTxt().hashCode());
        result = prime * result + ((getTransferType() == null) ? 0 : getTransferType().hashCode());
        result = prime * result + ((getTransactionNumber() == null) ? 0 : getTransactionNumber().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", transferId=").append(transferId);
        sb.append(", fromUser=").append(fromUser);
        sb.append(", toUser=").append(toUser);
        sb.append(", sendTime=").append(sendTime);
        sb.append(", receiveTime=").append(receiveTime);
        sb.append(", transferMoney=").append(transferMoney);
        sb.append(", transferStatus=").append(transferStatus);
        sb.append(", transferTxt=").append(transferTxt);
        sb.append(", transferType=").append(transferType);
        sb.append(", transactionNumber=").append(transactionNumber);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}