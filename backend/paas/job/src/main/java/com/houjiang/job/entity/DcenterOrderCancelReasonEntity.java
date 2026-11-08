package com.houjiang.job.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 数据大屏订单取消原因表
 * @TableName ${database.prefix.datav}hou_datav_order_cancel_reason
 * @author Amos
 */
@TableName(value ="${database.prefix.datav}hou_datav_order_cancel_reason")
@Data
public class DcenterOrderCancelReasonEntity implements Serializable {
    /**
     * 
     */
    @TableId
    private Integer cancelReasonId;

    /**
     * 取消订单的原因
     */
    private String cancelReasonContent;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}