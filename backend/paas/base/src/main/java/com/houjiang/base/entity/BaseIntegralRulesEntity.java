package com.houjiang.base.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 积分规则表
 * @TableName base_integral_rules
 * @author Amos
 */
@TableName(value ="hou_base_integral_rules")
@Data
public class BaseIntegralRulesEntity implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 登录赠送积分
     */
    private Integer ruleLogin;

    /**
     * 注册赠送积分
     */
    private Integer ruleRegister;

    /**
     * 
消费与赠送积分比例
     */
    private Integer ruleConsume;

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
        BaseIntegralRulesEntity other = (BaseIntegralRulesEntity) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getRuleLogin() == null ? other.getRuleLogin() == null : this.getRuleLogin().equals(other.getRuleLogin()))
            && (this.getRuleRegister() == null ? other.getRuleRegister() == null : this.getRuleRegister().equals(other.getRuleRegister()))
            && (this.getRuleConsume() == null ? other.getRuleConsume() == null : this.getRuleConsume().equals(other.getRuleConsume()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getRuleLogin() == null) ? 0 : getRuleLogin().hashCode());
        result = prime * result + ((getRuleRegister() == null) ? 0 : getRuleRegister().hashCode());
        result = prime * result + ((getRuleConsume() == null) ? 0 : getRuleConsume().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", ruleLogin=").append(ruleLogin);
        sb.append(", ruleRegister=").append(ruleRegister);
        sb.append(", ruleConsume=").append(ruleConsume);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}