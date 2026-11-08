package com.houjiang.payment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 卡片基础信息表
 * @TableName ${database.prefix.payment}hou_pay_card_base
 * @author Amos
 */
@TableName(value ="hou_pay_card_base")
@Data
public class PayCardBaseEntity implements Serializable {
    /**
     * 卡的id
     */
    @TableId(type = IdType.AUTO)
    private Integer cardId;

    /**
     * app id  ： 9999 通用
     */
    private Integer appId;

    /**
     * 卡名称
     */
    private String cardName;

    /**
     * 卡片里面的奖品
     */
    private String cardPrize;

    /**
     * 卡片描述
     */
    private String cardDesc;

    /**
     * 卡的有效开始时间
     */
    private Date cardStartTime;

    /**
     * 卡的有效结束时间
     */
    private Date cardEndTime;

    /**
     * 卡片数量
     */
    private Integer cardNum;

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
        PayCardBaseEntity other = (PayCardBaseEntity) that;
        return (this.getCardId() == null ? other.getCardId() == null : this.getCardId().equals(other.getCardId()))
            && (this.getAppId() == null ? other.getAppId() == null : this.getAppId().equals(other.getAppId()))
            && (this.getCardName() == null ? other.getCardName() == null : this.getCardName().equals(other.getCardName()))
            && (this.getCardPrize() == null ? other.getCardPrize() == null : this.getCardPrize().equals(other.getCardPrize()))
            && (this.getCardDesc() == null ? other.getCardDesc() == null : this.getCardDesc().equals(other.getCardDesc()))
            && (this.getCardStartTime() == null ? other.getCardStartTime() == null : this.getCardStartTime().equals(other.getCardStartTime()))
            && (this.getCardEndTime() == null ? other.getCardEndTime() == null : this.getCardEndTime().equals(other.getCardEndTime()))
            && (this.getCardNum() == null ? other.getCardNum() == null : this.getCardNum().equals(other.getCardNum()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getCardId() == null) ? 0 : getCardId().hashCode());
        result = prime * result + ((getAppId() == null) ? 0 : getAppId().hashCode());
        result = prime * result + ((getCardName() == null) ? 0 : getCardName().hashCode());
        result = prime * result + ((getCardPrize() == null) ? 0 : getCardPrize().hashCode());
        result = prime * result + ((getCardDesc() == null) ? 0 : getCardDesc().hashCode());
        result = prime * result + ((getCardStartTime() == null) ? 0 : getCardStartTime().hashCode());
        result = prime * result + ((getCardEndTime() == null) ? 0 : getCardEndTime().hashCode());
        result = prime * result + ((getCardNum() == null) ? 0 : getCardNum().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", cardId=").append(cardId);
        sb.append(", appId=").append(appId);
        sb.append(", cardName=").append(cardName);
        sb.append(", cardPrize=").append(cardPrize);
        sb.append(", cardDesc=").append(cardDesc);
        sb.append(", cardStartTime=").append(cardStartTime);
        sb.append(", cardEndTime=").append(cardEndTime);
        sb.append(", cardNum=").append(cardNum);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}