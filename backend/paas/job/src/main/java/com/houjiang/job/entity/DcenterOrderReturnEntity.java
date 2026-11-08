package com.houjiang.job.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 数据大屏售后记录表
 * @TableName ${database.prefix.datav}hou_datav_order_return
 * @author Amos
 */
@TableName(value ="${database.prefix.datav}hou_datav_order_return")
@Data
public class DcenterOrderReturnEntity implements Serializable {
    /**
     * 订单id
     */
    @TableId
    private String orderId;

    /**
     * 退款理由id
     */
    private Integer returnReasonId;

    /**
     * 退款金额
     */
    private BigDecimal returnCash;

    /**
     * 添加时间
     */
    private Date returnAddTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}