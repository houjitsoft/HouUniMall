package com.houjiang.mall.entity.shop;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 货到付款订单实体类
 * 表名: hou_mall_cash_on_delivery
 * 
 * @author Amos
 */
@Data
@TableName("hou_mall_cash_on_delivery")
public class CashOnDelivery implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 店铺ID
     */
    private Long shopId;

    /**
     * 买家ID
     */
    private Long buyerId;

    /**
     * 订单金额
     */
    private BigDecimal orderAmount;

    /**
     * 配送费
     */
    private BigDecimal deliveryFee;

    /**
     * 应付总额
     */
    private BigDecimal totalAmount;

    /**
     * 收货人
     */
    private String receiverName;

    /**
     * 收货电话
     */
    private String receiverPhone;

    /**
     * 收货地址
     */
    private String receiverAddress;

    /**
     * 配送状态: 0-待发货 1-配送中 2-已送达 3-已签收
     */
    private Integer deliveryStatus;

    /**
     * 支付状态: 0-未支付 1-已支付
     */
    private Integer paymentStatus;

    /**
     * 发货时间
     */
    private LocalDateTime deliveryTime;

    /**
     * 签收时间
     */
    private LocalDateTime receivedTime;

    /**
     * 支付时间
     */
    private LocalDateTime paidTime;

    /**
     * 实付金额
     */
    private BigDecimal paidAmount;

    /**
     * 配送员ID
     */
    private Long courierId;

    /**
     * 配送员姓名
     */
    private String courierName;

    /**
     * 配送员电话
     */
    private String courierPhone;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer deleted;
}
