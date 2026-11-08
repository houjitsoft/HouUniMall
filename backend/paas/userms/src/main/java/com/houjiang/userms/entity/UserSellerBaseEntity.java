package com.houjiang.userms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 卖家用户表
 * @TableName user_seller_base
 * @author Amos
 */
@TableName(value ="hou_user_seller_base")
@Data
public class UserSellerBaseEntity implements Serializable {
    /**
     * 卖家id
     */
    @TableId(type = IdType.AUTO)
    private String sellerId;

    /**
     * 卖家用户名
     */
    private String sellerName;

    /**
     * 店铺ID
     */
    private Integer shopId;

    /**
     * 会员ID
     */
    private String userId;

    /**
     * 卖家组ID
     */
    private Integer rightsGroupId;

    /**
     * 是否管理员(0-不是 1-是)
     */
    private Byte sellerIsAdmin;

    /**
     * 最后登录时间
     */
    private Date sellerLoginTime;

    /**
     * 卖家组ID
     */
    private Integer sellerGroupId;

    /**
     * 1卖家店铺 2供应商店铺 3 品牌店铺
     */
    private Integer shopType;

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
        UserSellerBaseEntity other = (UserSellerBaseEntity) that;
        return (this.getSellerId() == null ? other.getSellerId() == null : this.getSellerId().equals(other.getSellerId()))
            && (this.getSellerName() == null ? other.getSellerName() == null : this.getSellerName().equals(other.getSellerName()))
            && (this.getShopId() == null ? other.getShopId() == null : this.getShopId().equals(other.getShopId()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getRightsGroupId() == null ? other.getRightsGroupId() == null : this.getRightsGroupId().equals(other.getRightsGroupId()))
            && (this.getSellerIsAdmin() == null ? other.getSellerIsAdmin() == null : this.getSellerIsAdmin().equals(other.getSellerIsAdmin()))
            && (this.getSellerLoginTime() == null ? other.getSellerLoginTime() == null : this.getSellerLoginTime().equals(other.getSellerLoginTime()))
            && (this.getSellerGroupId() == null ? other.getSellerGroupId() == null : this.getSellerGroupId().equals(other.getSellerGroupId()))
            && (this.getShopType() == null ? other.getShopType() == null : this.getShopType().equals(other.getShopType()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getSellerId() == null) ? 0 : getSellerId().hashCode());
        result = prime * result + ((getSellerName() == null) ? 0 : getSellerName().hashCode());
        result = prime * result + ((getShopId() == null) ? 0 : getShopId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getRightsGroupId() == null) ? 0 : getRightsGroupId().hashCode());
        result = prime * result + ((getSellerIsAdmin() == null) ? 0 : getSellerIsAdmin().hashCode());
        result = prime * result + ((getSellerLoginTime() == null) ? 0 : getSellerLoginTime().hashCode());
        result = prime * result + ((getSellerGroupId() == null) ? 0 : getSellerGroupId().hashCode());
        result = prime * result + ((getShopType() == null) ? 0 : getShopType().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", sellerId=").append(sellerId);
        sb.append(", sellerName=").append(sellerName);
        sb.append(", shopId=").append(shopId);
        sb.append(", userId=").append(userId);
        sb.append(", rightsGroupId=").append(rightsGroupId);
        sb.append(", sellerIsAdmin=").append(sellerIsAdmin);
        sb.append(", sellerLoginTime=").append(sellerLoginTime);
        sb.append(", sellerGroupId=").append(sellerGroupId);
        sb.append(", shopType=").append(shopType);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}