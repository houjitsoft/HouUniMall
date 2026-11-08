package com.houjiang.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 退回原因 -- 字典表
 * @TableName order_return_reason_yiyatong
 * @author Amos
 */
@TableName(value ="hou_order_return_reason_yiyatong")
@Data
public class OrderReturnReasonYiyatongEntity implements Serializable {
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
    /**
     *
     */
    @TableId(type = IdType.AUTO)
    private Integer orderReturnReasonId;
    /**
     * 投诉理由内容
     */
    private String orderReturnReasonContent;
    /**
     * 投诉理由排序
     */
    private Integer orderReturnReasonSort;
    /**
     * 投诉类型1-退款,2-退货退款,3-换货,4-仅退款,5-部分金额退款
     */
    private Integer orderReturnReasonType;
}