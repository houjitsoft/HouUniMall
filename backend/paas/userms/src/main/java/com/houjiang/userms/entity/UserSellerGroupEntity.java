package com.houjiang.userms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 卖家用户组表
 * @TableName user_seller_group
 * @author Amos
 */
@TableName(value ="hou_user_seller_group")
@Data
public class UserSellerGroupEntity implements Serializable {
    /**
     * 卖家组编号
     */
    @TableId(type = IdType.AUTO)
    private Integer groupId;

    /**
     * 组名
     */
    private String groupName;

    /**
     * 权限
     */
    private String limits;

    /**
     * 消息权限范围
     */
    private String smtLimits;

    /**
     * 店铺编号
     */
    private Integer shopId;

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
        UserSellerGroupEntity other = (UserSellerGroupEntity) that;
        return (this.getGroupId() == null ? other.getGroupId() == null : this.getGroupId().equals(other.getGroupId()))
            && (this.getGroupName() == null ? other.getGroupName() == null : this.getGroupName().equals(other.getGroupName()))
            && (this.getLimits() == null ? other.getLimits() == null : this.getLimits().equals(other.getLimits()))
            && (this.getSmtLimits() == null ? other.getSmtLimits() == null : this.getSmtLimits().equals(other.getSmtLimits()))
            && (this.getShopId() == null ? other.getShopId() == null : this.getShopId().equals(other.getShopId()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getGroupId() == null) ? 0 : getGroupId().hashCode());
        result = prime * result + ((getGroupName() == null) ? 0 : getGroupName().hashCode());
        result = prime * result + ((getLimits() == null) ? 0 : getLimits().hashCode());
        result = prime * result + ((getSmtLimits() == null) ? 0 : getSmtLimits().hashCode());
        result = prime * result + ((getShopId() == null) ? 0 : getShopId().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", groupId=").append(groupId);
        sb.append(", groupName=").append(groupName);
        sb.append(", limits=").append(limits);
        sb.append(", smtLimits=").append(smtLimits);
        sb.append(", shopId=").append(shopId);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}