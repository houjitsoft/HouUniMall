package com.houjiang.userms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户行为日志表
 * @TableName user_log_action
 * @author Amos
 */
@TableName(value ="hou_user_log_action")
@Data
public class UserLogActionEntity implements Serializable {
    /**
     * 日志id
     */
    @TableId(type = IdType.AUTO)
    private Integer logId;

    /**
     * 玩家Id
     */
    private String userId;

    /**
     * 角色名称
     */
    private String userName;

    /**
     * 请求方法
     */
    private String method;

    /**
     * 请求的参数
     */
    private String logParam;

    /**
     * 请求描述
     */
    private String operation;

    /**
     * ip地址
     */
    private String logIp;

    /**
     * 日志日期
     */
    private Date logDate;

    /**
     * 记录时间
     */
    private Date logTime;

    /**
     * 时间戳
     */
    private Date ts;



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
        UserLogActionEntity other = (UserLogActionEntity) that;
        return (this.getLogId() == null ? other.getLogId() == null : this.getLogId().equals(other.getLogId()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getUserName() == null ? other.getUserName() == null : this.getUserName().equals(other.getUserName()))
            && (this.getMethod() == null ? other.getMethod() == null : this.getMethod().equals(other.getMethod()))
            && (this.getLogParam() == null ? other.getLogParam() == null : this.getLogParam().equals(other.getLogParam()))
            && (this.getOperation() == null ? other.getOperation() == null : this.getOperation().equals(other.getOperation()))
            && (this.getLogIp() == null ? other.getLogIp() == null : this.getLogIp().equals(other.getLogIp()))
            && (this.getLogDate() == null ? other.getLogDate() == null : this.getLogDate().equals(other.getLogDate()))
            && (this.getLogTime() == null ? other.getLogTime() == null : this.getLogTime().equals(other.getLogTime()))
            && (this.getTs() == null ? other.getTs() == null : this.getTs().equals(other.getTs()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getLogId() == null) ? 0 : getLogId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getUserName() == null) ? 0 : getUserName().hashCode());
        result = prime * result + ((getMethod() == null) ? 0 : getMethod().hashCode());
        result = prime * result + ((getLogParam() == null) ? 0 : getLogParam().hashCode());
        result = prime * result + ((getOperation() == null) ? 0 : getOperation().hashCode());
        result = prime * result + ((getLogIp() == null) ? 0 : getLogIp().hashCode());
        result = prime * result + ((getLogDate() == null) ? 0 : getLogDate().hashCode());
        result = prime * result + ((getLogTime() == null) ? 0 : getLogTime().hashCode());
        result = prime * result + ((getTs() == null) ? 0 : getTs().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", logId=").append(logId);
        sb.append(", userId=").append(userId);
        sb.append(", userName=").append(userName);
        sb.append(", method=").append(method);
        sb.append(", logParam=").append(logParam);
        sb.append(", operation=").append(operation);
        sb.append(", logIp=").append(logIp);
        sb.append(", logDate=").append(logDate);
        sb.append(", logTime=").append(logTime);
        sb.append(", ts=").append(ts);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}