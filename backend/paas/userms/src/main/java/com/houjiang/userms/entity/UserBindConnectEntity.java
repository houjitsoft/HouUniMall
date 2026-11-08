package com.houjiang.userms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户绑定表
 * @TableName user_bind_connect
 * @author Amos
 */
@TableName(value ="hou_user_bind_connect")
@Data
public class UserBindConnectEntity implements Serializable {
    /**
     * 绑定标记：mobie/微信/支付宝openid  绑定类型+openid
     */
    @TableId
    private String bindId;

    /**
     * 绑定类型  1 :mobile 2:weixin
     */
    private Byte bindType;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 绑定时间
     */
    private Date bindTime;

    /**
     * 第三方用户昵称
     */
    private String bindNickname;

    /**
     * 第三方用户头像地址
     */
    private String bindAvator;

    /**
     * 第三方用户身份认证编号
     */
    private String bindOpenid;

    /**
     *
     */
    private String bindUnionid;


    /**
     * 创建者
     */
    private String createUser;

    /**
     * 微信小程序是否自动登录 1是 2 否
     */
    private Integer isAutoLogin;


    /**
     * 时间戳
     */
    private Date ts;
    /**
     * 公众号openid
      */
    private String officialAccountOpenid;

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
        UserBindConnectEntity other = (UserBindConnectEntity) that;
        return (this.getBindId() == null ? other.getBindId() == null : this.getBindId().equals(other.getBindId()))
            && (this.getBindType() == null ? other.getBindType() == null : this.getBindType().equals(other.getBindType()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getBindTime() == null ? other.getBindTime() == null : this.getBindTime().equals(other.getBindTime()))
            && (this.getBindNickname() == null ? other.getBindNickname() == null : this.getBindNickname().equals(other.getBindNickname()))
            && (this.getBindAvator() == null ? other.getBindAvator() == null : this.getBindAvator().equals(other.getBindAvator()))
            && (this.getBindOpenid() == null ? other.getBindOpenid() == null : this.getBindOpenid().equals(other.getBindOpenid()))
            && (this.getBindUnionid() == null ? other.getBindUnionid() == null : this.getBindUnionid().equals(other.getBindUnionid()))
            && (this.getCreateUser() == null ? other.getCreateUser() == null : this.getCreateUser().equals(other.getCreateUser()))
            && (this.getIsAutoLogin() == null ? other.getIsAutoLogin() == null : this.getIsAutoLogin().equals(other.getIsAutoLogin()))
            && (this.getTs() == null ? other.getTs() == null : this.getTs().equals(other.getTs()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getBindId() == null) ? 0 : getBindId().hashCode());
        result = prime * result + ((getBindType() == null) ? 0 : getBindType().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getBindTime() == null) ? 0 : getBindTime().hashCode());
        result = prime * result + ((getBindNickname() == null) ? 0 : getBindNickname().hashCode());
        result = prime * result + ((getBindAvator() == null) ? 0 : getBindAvator().hashCode());
        result = prime * result + ((getBindOpenid() == null) ? 0 : getBindOpenid().hashCode());
        result = prime * result + ((getBindUnionid() == null) ? 0 : getBindUnionid().hashCode());
        result = prime * result + ((getCreateUser() == null) ? 0 : getCreateUser().hashCode());
        result = prime * result + ((getIsAutoLogin() == null) ? 0 : getIsAutoLogin().hashCode());
        result = prime * result + ((getTs() == null) ? 0 : getTs().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", bindId=").append(bindId);
        sb.append(", bindType=").append(bindType);
        sb.append(", userId=").append(userId);
        sb.append(", bindTime=").append(bindTime);
        sb.append(", bindNickname=").append(bindNickname);
        sb.append(", bindAvator=").append(bindAvator);
        sb.append(", bindOpenid=").append(bindOpenid);
        sb.append(", bindUnionid=").append(bindUnionid);
        sb.append(", createUser=").append(createUser);
        sb.append(", isAutoLogin=").append(isAutoLogin);
        sb.append(", ts=").append(ts);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}
