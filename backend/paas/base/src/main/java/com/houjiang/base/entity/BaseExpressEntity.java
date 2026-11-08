package com.houjiang.base.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 快递表
 * @TableName base_express
 * @author Amos
 */
@TableName(value ="hou_base_express")
@Data
public class BaseExpressEntity implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer expressId;

    /**
     * 快递公司
     */
    private String expressName;

    /**
     * 物流
     */
    private String expressPinyin;

    /**
     * 状态0关闭1开启
     */
    private Boolean expressStatus;

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
        BaseExpressEntity other = (BaseExpressEntity) that;
        return (this.getExpressId() == null ? other.getExpressId() == null : this.getExpressId().equals(other.getExpressId()))
            && (this.getExpressName() == null ? other.getExpressName() == null : this.getExpressName().equals(other.getExpressName()))
            && (this.getExpressPinyin() == null ? other.getExpressPinyin() == null : this.getExpressPinyin().equals(other.getExpressPinyin()))
            && (this.getExpressStatus() == null ? other.getExpressStatus() == null : this.getExpressStatus().equals(other.getExpressStatus()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getExpressId() == null) ? 0 : getExpressId().hashCode());
        result = prime * result + ((getExpressName() == null) ? 0 : getExpressName().hashCode());
        result = prime * result + ((getExpressPinyin() == null) ? 0 : getExpressPinyin().hashCode());
        result = prime * result + ((getExpressStatus() == null) ? 0 : getExpressStatus().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", expressId=").append(expressId);
        sb.append(", expressName=").append(expressName);
        sb.append(", expressPinyin=").append(expressPinyin);
        sb.append(", expressStatus=").append(expressStatus);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}