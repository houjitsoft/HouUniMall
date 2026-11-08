package com.houjiang.job.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 订单每日统计
 * @TableName job_day_order_statics
 * @author Amos
 */
@TableName(value ="job_day_order_statics")
@Data
public class JobDayOrderStaticsEntity implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer statId;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 总退款金额
     */
    private BigDecimal totalRefundFee;

    /**
     * 总退款数量
     */
    private Integer totalRefundTrade;

    /**
     * 总订单金额
     */
    private BigDecimal totalPaymentAmount;

    /**
     * 总订单数
     */
    private Integer totalOrderTrade;

    /**
     * 已完成订单数
     */
    private Integer completeTrade;

    /**
     * 已完成订单额
     */
    private BigDecimal completeFee;

    /**
     * 已取消的订单数
     */
    private Integer cancleTrade;

    /**
     * 已取消的订单额
     */
    private BigDecimal cancleFee;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}