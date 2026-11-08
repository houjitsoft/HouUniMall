package com.houjiang.order.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 订单换货表(OrderBarter)表实体类
 *
 * @author Amos
 * @since 2025-05-07 09:36:29
 */
@Data
@TableName("hou_order_barter")
public class OrderBarterEntity implements Serializable {

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
    
    /**
     * 换货id
     */
    @TableId(type = IdType.AUTO)
    private Integer orderBarterId;

    /**
     * 商家id
     */
    private Integer shopId;

    /**
     * 换货物流单号
     */
    private String barterShippingCode;
    
    /**
     * 换货公司id
     */
    private Integer barterPostCode;
    /**
     * 换货公司名称
     */
    private String barterPostName;

    /**
     * 换货手机号
     */
    private String barterMobile;
    
    /**
     * 订单退款id
     */
    private Integer orderReturnId;
    
    /**
     * 订单id
     */
    private String orderId;
    
    /**
     * 退款编号
     */
    private String returnCode;
    
    /**
     * 创建时间
     */
    private Date createTime;
    
    /**
     * 换货状态 0发出换货物流 1用户确认收货 
     */
    private Integer barterStatus;
    
    /**
     * 收货时间
     */
    private Date deliveryTime;

}
