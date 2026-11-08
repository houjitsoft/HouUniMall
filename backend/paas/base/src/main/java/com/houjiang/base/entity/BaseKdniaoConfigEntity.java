package com.houjiang.base.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 店铺批量打印快递鸟信息表
 * @TableName base_kdniao_config
 * @author Amos
 */
@TableName(value ="hou_base_kdniao_config")
@Data
public class BaseKdniaoConfigEntity implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 快递鸟Id
     */
    private String ebussinessid;

    /**
     * 快递鸟密钥
     */
    private String appkey;

    /**
     * 快递鸟接口指令 购买套餐不一样 指令不一样
     */
    private Integer instructCode;

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
        BaseKdniaoConfigEntity other = (BaseKdniaoConfigEntity) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getEbussinessid() == null ? other.getEbussinessid() == null : this.getEbussinessid().equals(other.getEbussinessid()))
            && (this.getAppkey() == null ? other.getAppkey() == null : this.getAppkey().equals(other.getAppkey()))
            && (this.getInstructCode() == null ? other.getInstructCode() == null : this.getInstructCode().equals(other.getInstructCode()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getEbussinessid() == null) ? 0 : getEbussinessid().hashCode());
        result = prime * result + ((getAppkey() == null) ? 0 : getAppkey().hashCode());
        result = prime * result + ((getInstructCode() == null) ? 0 : getInstructCode().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", ebussinessid=").append(ebussinessid);
        sb.append(", appkey=").append(appkey);
        sb.append(", instructCode=").append(instructCode);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}