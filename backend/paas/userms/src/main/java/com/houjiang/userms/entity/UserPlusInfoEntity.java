package com.houjiang.userms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @TableName user_plus_info
 * @author Amos
 */
@TableName(value ="hou_user_plus_info")
@Data
public class UserPlusInfoEntity implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer plusId;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 套餐类型 1 月卡 2 季卡 3 年卡
     */
    private Integer thaliType;

    /**
     * 套餐开始时间
     */
    private Date thaliStarttime;

    /**
     * 套餐结束时间
     */
    private Date thaliEndtime;

    /**
     * 当月已用额度
     */
    private BigDecimal usedAmount;

    /**
     * 已优惠总额度
     */
    private BigDecimal totalAmount;

    /**
     * 创建时间
     */
    private Date creatTime;

    /**
     * 获取积分
     */
    private Long earnPoints;

    /**
     * 会员状态 0 过期 1 正常
     */
    private Integer thaliStatus;

    /**
     * 是否显示续费按钮 0 不显示 1 显示(暂时废弃)
     */
    private Integer renewalDisplay;

    /**
     * 剩余红包发放次数
     */
    private Integer redGrantnum;

    /**
     * 权益id
     */
    private String plusEquity;

    /**
     * 上次红包发放月份
     */
    private Integer redGrantmonth;

    /**
     * 续费短信是否发送 0 未发送 1 已发送
     */
    private Integer expirationNotice;

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
        UserPlusInfoEntity other = (UserPlusInfoEntity) that;
        return (this.getPlusId() == null ? other.getPlusId() == null : this.getPlusId().equals(other.getPlusId()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getThaliType() == null ? other.getThaliType() == null : this.getThaliType().equals(other.getThaliType()))
            && (this.getThaliStarttime() == null ? other.getThaliStarttime() == null : this.getThaliStarttime().equals(other.getThaliStarttime()))
            && (this.getThaliEndtime() == null ? other.getThaliEndtime() == null : this.getThaliEndtime().equals(other.getThaliEndtime()))
            && (this.getUsedAmount() == null ? other.getUsedAmount() == null : this.getUsedAmount().equals(other.getUsedAmount()))
            && (this.getTotalAmount() == null ? other.getTotalAmount() == null : this.getTotalAmount().equals(other.getTotalAmount()))
            && (this.getCreatTime() == null ? other.getCreatTime() == null : this.getCreatTime().equals(other.getCreatTime()))
            && (this.getEarnPoints() == null ? other.getEarnPoints() == null : this.getEarnPoints().equals(other.getEarnPoints()))
            && (this.getThaliStatus() == null ? other.getThaliStatus() == null : this.getThaliStatus().equals(other.getThaliStatus()))
            && (this.getRenewalDisplay() == null ? other.getRenewalDisplay() == null : this.getRenewalDisplay().equals(other.getRenewalDisplay()))
            && (this.getRedGrantnum() == null ? other.getRedGrantnum() == null : this.getRedGrantnum().equals(other.getRedGrantnum()))
            && (this.getPlusEquity() == null ? other.getPlusEquity() == null : this.getPlusEquity().equals(other.getPlusEquity()))
            && (this.getRedGrantmonth() == null ? other.getRedGrantmonth() == null : this.getRedGrantmonth().equals(other.getRedGrantmonth()))
            && (this.getExpirationNotice() == null ? other.getExpirationNotice() == null : this.getExpirationNotice().equals(other.getExpirationNotice()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getPlusId() == null) ? 0 : getPlusId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getThaliType() == null) ? 0 : getThaliType().hashCode());
        result = prime * result + ((getThaliStarttime() == null) ? 0 : getThaliStarttime().hashCode());
        result = prime * result + ((getThaliEndtime() == null) ? 0 : getThaliEndtime().hashCode());
        result = prime * result + ((getUsedAmount() == null) ? 0 : getUsedAmount().hashCode());
        result = prime * result + ((getTotalAmount() == null) ? 0 : getTotalAmount().hashCode());
        result = prime * result + ((getCreatTime() == null) ? 0 : getCreatTime().hashCode());
        result = prime * result + ((getEarnPoints() == null) ? 0 : getEarnPoints().hashCode());
        result = prime * result + ((getThaliStatus() == null) ? 0 : getThaliStatus().hashCode());
        result = prime * result + ((getRenewalDisplay() == null) ? 0 : getRenewalDisplay().hashCode());
        result = prime * result + ((getRedGrantnum() == null) ? 0 : getRedGrantnum().hashCode());
        result = prime * result + ((getPlusEquity() == null) ? 0 : getPlusEquity().hashCode());
        result = prime * result + ((getRedGrantmonth() == null) ? 0 : getRedGrantmonth().hashCode());
        result = prime * result + ((getExpirationNotice() == null) ? 0 : getExpirationNotice().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", plusId=").append(plusId);
        sb.append(", userId=").append(userId);
        sb.append(", thaliType=").append(thaliType);
        sb.append(", thaliStarttime=").append(thaliStarttime);
        sb.append(", thaliEndtime=").append(thaliEndtime);
        sb.append(", usedAmount=").append(usedAmount);
        sb.append(", totalAmount=").append(totalAmount);
        sb.append(", creatTime=").append(creatTime);
        sb.append(", earnPoints=").append(earnPoints);
        sb.append(", thaliStatus=").append(thaliStatus);
        sb.append(", renewalDisplay=").append(renewalDisplay);
        sb.append(", redGrantnum=").append(redGrantnum);
        sb.append(", plusEquity=").append(plusEquity);
        sb.append(", redGrantmonth=").append(redGrantmonth);
        sb.append(", expirationNotice=").append(expirationNotice);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}