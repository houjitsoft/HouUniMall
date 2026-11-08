package com.houjiang.job.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 数据大屏售后记录表
 * @TableName ${database.prefix.datav}hou_datav_order_return
 * @author Amos
 */

@Data
public class DcenterOrderReturnVO implements Serializable {
    /**
     * 订单id
     */

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