package com.houjiang.payment.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 卡片信息表
 * @TableName ${database.prefix.payment}hou_pay_card_info
 * @author Amos
 */
@TableName(value ="hou_pay_card_info")
@Data
public class PayCardInfoEntity implements Serializable {
    /**
     * 卡片激活码
     */
    @TableId
    private String cardCode;

    /**
     * 卡片密码
     */
    private String cardPassword;

    /**
     * 卡片id
     */
    private Integer cardId;

    /**
     * 领奖时间
     */
    private Date cardFetchTime;

    /**
     * 媒体id,参照base_card_media表
     */
    private Short cardMediaId;

    /**
     * 领卡人的服务器id
     */
    private String serverId;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 领卡人账号
     */
    private String userAccount;

    /**
     * 卡牌生成时间
     */
    private Date cardTime;

    /**
     * 充值卡余额
     */
    private BigDecimal cardMoney;

    /**
     * 卡冻结金额
     */
    private BigDecimal cardFrozeMoney;

    /**
     * 标记1可用2已使用0正常
     */
    private Integer parentId;

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
        PayCardInfoEntity other = (PayCardInfoEntity) that;
        return (this.getCardCode() == null ? other.getCardCode() == null : this.getCardCode().equals(other.getCardCode()))
            && (this.getCardPassword() == null ? other.getCardPassword() == null : this.getCardPassword().equals(other.getCardPassword()))
            && (this.getCardId() == null ? other.getCardId() == null : this.getCardId().equals(other.getCardId()))
            && (this.getCardFetchTime() == null ? other.getCardFetchTime() == null : this.getCardFetchTime().equals(other.getCardFetchTime()))
            && (this.getCardMediaId() == null ? other.getCardMediaId() == null : this.getCardMediaId().equals(other.getCardMediaId()))
            && (this.getServerId() == null ? other.getServerId() == null : this.getServerId().equals(other.getServerId()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getUserAccount() == null ? other.getUserAccount() == null : this.getUserAccount().equals(other.getUserAccount()))
            && (this.getCardTime() == null ? other.getCardTime() == null : this.getCardTime().equals(other.getCardTime()))
            && (this.getCardMoney() == null ? other.getCardMoney() == null : this.getCardMoney().equals(other.getCardMoney()))
            && (this.getCardFrozeMoney() == null ? other.getCardFrozeMoney() == null : this.getCardFrozeMoney().equals(other.getCardFrozeMoney()))
            && (this.getParentId() == null ? other.getParentId() == null : this.getParentId().equals(other.getParentId()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getCardCode() == null) ? 0 : getCardCode().hashCode());
        result = prime * result + ((getCardPassword() == null) ? 0 : getCardPassword().hashCode());
        result = prime * result + ((getCardId() == null) ? 0 : getCardId().hashCode());
        result = prime * result + ((getCardFetchTime() == null) ? 0 : getCardFetchTime().hashCode());
        result = prime * result + ((getCardMediaId() == null) ? 0 : getCardMediaId().hashCode());
        result = prime * result + ((getServerId() == null) ? 0 : getServerId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getUserAccount() == null) ? 0 : getUserAccount().hashCode());
        result = prime * result + ((getCardTime() == null) ? 0 : getCardTime().hashCode());
        result = prime * result + ((getCardMoney() == null) ? 0 : getCardMoney().hashCode());
        result = prime * result + ((getCardFrozeMoney() == null) ? 0 : getCardFrozeMoney().hashCode());
        result = prime * result + ((getParentId() == null) ? 0 : getParentId().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", cardCode=").append(cardCode);
        sb.append(", cardPassword=").append(cardPassword);
        sb.append(", cardId=").append(cardId);
        sb.append(", cardFetchTime=").append(cardFetchTime);
        sb.append(", cardMediaId=").append(cardMediaId);
        sb.append(", serverId=").append(serverId);
        sb.append(", userId=").append(userId);
        sb.append(", userAccount=").append(userAccount);
        sb.append(", cardTime=").append(cardTime);
        sb.append(", cardMoney=").append(cardMoney);
        sb.append(", cardFrozeMoney=").append(cardFrozeMoney);
        sb.append(", parentId=").append(parentId);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}