package com.houjiang.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 怡亚通订单表
 * @TableName order_yiyatong
 * @author Amos
 */
@TableName(value ="hou_order_yiyatong")
@Data
public class OrderYiyatongEntity implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 订单号
     */
    private String orderId;

    /**
     * 怡亚通订单号
     */
    private String orderSn;

    /**
     * 怡亚通订单批次号batchOrderSn
     */
    private String batchOrderSn;

    /**
     * 订单状态：10为已提交待付款,20为已付款待发货,30为已发货待收货,40为确认收货交易成功,50、60为已取消,70、80、90、100为已关闭
     */
    private Integer orderStatus;

    /**
     * 订单中包含的skuId信息，多个skuId用逗号隔开
     */
    private String skuIds;

    /**
     * 订单总金额(含运费)
     */
    private String payAmount;

    /**
     * 商品总金额(不含运费)
     */
    private String totalAmount;

    /**
     * 订单状态名称
     */
    private String orderStatusName;

    /**
     * 订单退款售后单号
     */
    private String returnSn;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}