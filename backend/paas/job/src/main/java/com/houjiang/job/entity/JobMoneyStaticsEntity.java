package com.houjiang.job.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 平台充值卡统计表
 * @TableName hou_job_money_statics
 * @author Amos
 */
@TableName(value ="hou_job_money_statics")
@Data
public class JobMoneyStaticsEntity implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 不是联营商用户余额
     */
    private BigDecimal moneyNoBusiness;

    /**
     * 不是联营商充值卡余额
     */
    private BigDecimal rechargeCardNoBusiness;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 联营商用户余额
     */
    private BigDecimal moneyBusiness;

    /**
     * 联营商充值卡余额
     */
    private BigDecimal rechargeCardBusiness;

    /**
     * 已充值金额
     */
    private BigDecimal rechargedAmount;

    /**
     * 激活未充值金额
     */
    private BigDecimal unrechargeAmount;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}