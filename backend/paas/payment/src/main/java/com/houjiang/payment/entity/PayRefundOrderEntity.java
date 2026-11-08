package com.houjiang.payment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Amos
 */
@Data
@TableName("hou_pay_refund_order")
public class PayRefundOrderEntity implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String refundNo;
    private String cashierNo;
    private String mallOrderNo;
    private BigDecimal amount;
    private String reason;
    private String status;
    private String channelRefundNo;
    private String failureReason;
    private Date successTime;
    private Date createTime;
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
