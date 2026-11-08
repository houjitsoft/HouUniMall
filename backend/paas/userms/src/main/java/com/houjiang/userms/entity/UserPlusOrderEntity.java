package com.houjiang.userms.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
/**
 * @ClassName UserPlusOrderEntity
 * @Description plus会员充值订单表
 * @Author Amos
 * @Date 2023-06-29 09:28:32
 * @Version 1.0
 * @author Amos
 */
@TableName(value ="hou_user_plus_order")
@Data
public class UserPlusOrderEntity  implements Serializable {
    /**
     * 支付单号
     */
    @TableId
    private String plusOrderid;
    /**
     * 用户id
     */
    private String userId;
    /**
     * 支付金额
     */
    private BigDecimal payPrice;
    /**
     * 支付方式 1-余额支付，2-购物卡，3-微信支付，4 支付宝 5 平台赠送
     */
    private Integer payType;
    /**
     * 生效时间
     */
    private Date startTime;
    /**
     * 到期时间
     */
    private Date endTime;
    /**
     * 套餐类型 1 月卡 2 季卡 3 年卡
     */
    private Integer thaliType;
    /**
     * 支付时间
     */
    private Date creatTime;
    /**
     * plus权益
     */
    private String plusEquity;

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
        UserPlusOrderEntity other = (UserPlusOrderEntity) that;
        return (this.getPlusOrderid() == null ? other.getPlusOrderid() == null : this.getPlusOrderid().equals(other.getPlusOrderid()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getPayPrice() == null ? other.getPayPrice() == null : this.getPayPrice().equals(other.getPayPrice()))
            && (this.getPayType() == null ? other.getPayType() == null : this.getPayType().equals(other.getPayType()))
            && (this.getStartTime() == null ? other.getStartTime() == null : this.getStartTime().equals(other.getStartTime()))
            && (this.getEndTime() == null ? other.getEndTime() == null : this.getEndTime().equals(other.getEndTime()))
            && (this.getThaliType() == null ? other.getThaliType() == null : this.getThaliType().equals(other.getThaliType()))
            && (this.getCreatTime() == null ? other.getCreatTime() == null : this.getCreatTime().equals(other.getCreatTime()))
            && (this.getPlusEquity() == null ? other.getPlusEquity() == null : this.getPlusEquity().equals(other.getPlusEquity()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getPlusOrderid() == null) ? 0 : getPlusOrderid().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getPayPrice() == null) ? 0 : getPayPrice().hashCode());
        result = prime * result + ((getPayType() == null) ? 0 : getPayType().hashCode());
        result = prime * result + ((getStartTime() == null) ? 0 : getStartTime().hashCode());
        result = prime * result + ((getEndTime() == null) ? 0 : getEndTime().hashCode());
        result = prime * result + ((getThaliType() == null) ? 0 : getThaliType().hashCode());
        result = prime * result + ((getCreatTime() == null) ? 0 : getCreatTime().hashCode());
        result = prime * result + ((getPlusEquity() == null) ? 0 : getPlusEquity().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", plusOrderid=").append(plusOrderid);
        sb.append(", userId=").append(userId);
        sb.append(", payPrice=").append(payPrice);
        sb.append(", payType=").append(payType);
        sb.append(", startTime=").append(startTime);
        sb.append(", endTime=").append(endTime);
        sb.append(", thaliType=").append(thaliType);
        sb.append(", creatTime=").append(creatTime);
        sb.append(", plusEquity=").append(plusEquity);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}
