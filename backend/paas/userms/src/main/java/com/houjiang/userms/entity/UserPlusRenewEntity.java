package com.houjiang.userms.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
/**
 * @ClassName
 * @Description
 * @Author Amos
 * @Date 2023-06-28 18:14:55
 * @Version 1.0
 * @author Amos
 */
@TableName(value ="hou_user_plus_renew")
@Data
public class UserPlusRenewEntity  implements Serializable {

    @TableId(type = IdType.AUTO)
    private Integer renewId;
    /**
     * 套餐类型 1 月卡 2 季卡 3 年卡
     */
    private Integer thaliType;
    /**
     * 用户id
     */
    private String userId;
    /**
     * 续费开始时间
     */
    private Date renewStarttime;
    /**
     * 续费结束时间
     */
    private Date renewEndtime;
    /**
     * 创建时间
     */
    private Date creatTime;
    /**
     * 权益id
     */
    private String plusEquity;
    /**
     * 状态 0 未生效 1 已生效
     */
    private Integer renewStatus;
    /**
     * 充值订单
     */
    private String plusOrderid;

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
        UserPlusRenewEntity other = (UserPlusRenewEntity) that;
        return (this.getRenewId() == null ? other.getRenewId() == null : this.getRenewId().equals(other.getRenewId()))
            && (this.getThaliType() == null ? other.getThaliType() == null : this.getThaliType().equals(other.getThaliType()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getRenewStarttime() == null ? other.getRenewStarttime() == null : this.getRenewStarttime().equals(other.getRenewStarttime()))
            && (this.getRenewEndtime() == null ? other.getRenewEndtime() == null : this.getRenewEndtime().equals(other.getRenewEndtime()))
            && (this.getCreatTime() == null ? other.getCreatTime() == null : this.getCreatTime().equals(other.getCreatTime()))
            && (this.getPlusEquity() == null ? other.getPlusEquity() == null : this.getPlusEquity().equals(other.getPlusEquity()))
            && (this.getRenewStatus() == null ? other.getRenewStatus() == null : this.getRenewStatus().equals(other.getRenewStatus()))
            && (this.getPlusOrderid() == null ? other.getPlusOrderid() == null : this.getPlusOrderid().equals(other.getPlusOrderid()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getRenewId() == null) ? 0 : getRenewId().hashCode());
        result = prime * result + ((getThaliType() == null) ? 0 : getThaliType().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getRenewStarttime() == null) ? 0 : getRenewStarttime().hashCode());
        result = prime * result + ((getRenewEndtime() == null) ? 0 : getRenewEndtime().hashCode());
        result = prime * result + ((getCreatTime() == null) ? 0 : getCreatTime().hashCode());
        result = prime * result + ((getPlusEquity() == null) ? 0 : getPlusEquity().hashCode());
        result = prime * result + ((getRenewStatus() == null) ? 0 : getRenewStatus().hashCode());
        result = prime * result + ((getPlusOrderid() == null) ? 0 : getPlusOrderid().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", renewId=").append(renewId);
        sb.append(", thaliType=").append(thaliType);
        sb.append(", userId=").append(userId);
        sb.append(", renewStarttime=").append(renewStarttime);
        sb.append(", renewEndtime=").append(renewEndtime);
        sb.append(", creatTime=").append(creatTime);
        sb.append(", plusEquity=").append(plusEquity);
        sb.append(", renewStatus=").append(renewStatus);
        sb.append(", plusOrderid=").append(plusOrderid);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}
