package com.houjiang.userms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.houjiang.commons.vo.activity.RedTemplateEntityVO;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * plus会员权益表
 * @TableName user_plus_equity
 * @author Amos
 */
@TableName(value ="hou_user_plus_equity")
@Data
public class UserPlusEquityEntity implements Serializable {
    /**
     *
     */
    @TableId(type = IdType.AUTO)
    private Integer equityId;

    /**
     * 权益名称
     */
    private String equityName;

    /**
     * 1 PLUS专属折扣, 2 无门槛红包,3 红包券,4 送积分,5 积分倍率
     */
    private Integer equityType;

    /**
     * 权益值
     */
    private String equityValue;

    /**
     * 权益添加时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date equityAddtime;

    /**
     * 权益状态 0 关闭  1 开启
     */
    private Integer equityStatus;

    /**
     * 无门槛红包有效时间
     */
    private Integer redEffective;

    /**
     * 红包发放时间  几号
     */
    private Integer redProvide;

    /**
     * 每月最多可抵扣额度
     */
    private BigDecimal discountLimit;

    /**
     * 红包模板
     */
    @TableField(select = false)
    private List<RedTemplateEntityVO> redList;

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
        UserPlusEquityEntity other = (UserPlusEquityEntity) that;
        return (this.getEquityId() == null ? other.getEquityId() == null : this.getEquityId().equals(other.getEquityId()))
            && (this.getEquityName() == null ? other.getEquityName() == null : this.getEquityName().equals(other.getEquityName()))
            && (this.getEquityType() == null ? other.getEquityType() == null : this.getEquityType().equals(other.getEquityType()))
            && (this.getEquityValue() == null ? other.getEquityValue() == null : this.getEquityValue().equals(other.getEquityValue()))
            && (this.getEquityAddtime() == null ? other.getEquityAddtime() == null : this.getEquityAddtime().equals(other.getEquityAddtime()))
            && (this.getEquityStatus() == null ? other.getEquityStatus() == null : this.getEquityStatus().equals(other.getEquityStatus()))
            && (this.getRedEffective() == null ? other.getRedEffective() == null : this.getRedEffective().equals(other.getRedEffective()))
            && (this.getRedProvide() == null ? other.getRedProvide() == null : this.getRedProvide().equals(other.getRedProvide()))
            && (this.getDiscountLimit() == null ? other.getDiscountLimit() == null : this.getDiscountLimit().equals(other.getDiscountLimit()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getEquityId() == null) ? 0 : getEquityId().hashCode());
        result = prime * result + ((getEquityName() == null) ? 0 : getEquityName().hashCode());
        result = prime * result + ((getEquityType() == null) ? 0 : getEquityType().hashCode());
        result = prime * result + ((getEquityValue() == null) ? 0 : getEquityValue().hashCode());
        result = prime * result + ((getEquityAddtime() == null) ? 0 : getEquityAddtime().hashCode());
        result = prime * result + ((getEquityStatus() == null) ? 0 : getEquityStatus().hashCode());
        result = prime * result + ((getRedEffective() == null) ? 0 : getRedEffective().hashCode());
        result = prime * result + ((getRedProvide() == null) ? 0 : getRedProvide().hashCode());
        result = prime * result + ((getDiscountLimit() == null) ? 0 : getDiscountLimit().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", equityId=").append(equityId);
        sb.append(", equityName=").append(equityName);
        sb.append(", equityType=").append(equityType);
        sb.append(", equityValue=").append(equityValue);
        sb.append(", equityAddtime=").append(equityAddtime);
        sb.append(", equityStatus=").append(equityStatus);
        sb.append(", redEffective=").append(redEffective);
        sb.append(", redProvide=").append(redProvide);
        sb.append(", discountLimit=").append(discountLimit);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}
