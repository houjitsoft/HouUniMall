package com.houjiang.order.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.houjiang.commons.util.DateUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 订单发货表(OrderShipment)表实体类
 *
 * @author Amos
 * @since 2024-01-30 15:46:44
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("hou_order_shipment")
public class OrderShipmentEntity implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
    *发货id
    */
    @TableId(type = IdType.AUTO)
    private Integer shipmentId;


    /**
    *订单id
    */
    private String orderId;

    /**
    *配送商品
    */
    private String goodsIds;

    /**
    *配送数量
    */
    private String goodsNums;

    /**
    *配送时间
    */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date orderShippingTime;

    /**
    *配送公司ID
    */
    private Integer orderShippingExpressId;

    /**
    *物流单号
    */
    private String orderShippingCode;

    /**
    *卖家备注
    */
    private String orderShippingMessage;

    /**
    *创建时间
    */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    /**
     * 1 物流配送 2 自主配送
     */
    private Integer shipmentType;
    /**
     * 配送人
     */
    private String deliveryPerson;
    /**
     * 配送联系方式
     */
    private String deliveryTelephone;
    /**
     * 配送凭证
     */
    private String deliveryCertificate;
    /**
     * 仓库id
     */
    private Integer storageId;
    /**
     * 发货地址（针对物流、快递类）
     */
    private String senderAddress;

    public OrderShipmentEntity(String orderId, String goodsIds, String goodsNums, Date orderShippingTime,
                               Integer orderShippingExpressId, String orderShippingCode, String orderShippingMessage,
                               Integer shipmentType,String deliveryPerson, String deliveryTelephone, String deliveryCertificate,
                               Integer storageId) {
        this.orderId = orderId;
        this.goodsIds = goodsIds;
        this.goodsNums = goodsNums;
        this.orderShippingTime = orderShippingTime;
        this.orderShippingExpressId = orderShippingExpressId;
        this.orderShippingCode = orderShippingCode;
        this.orderShippingMessage = orderShippingMessage;
        this.createTime = DateUtils.getNowDateToDf();
        this.shipmentType = shipmentType;
        this.deliveryPerson = deliveryPerson;
        this.deliveryTelephone = deliveryTelephone;
        this.deliveryCertificate = deliveryCertificate;
        this.storageId = storageId;
    }

    public OrderShipmentEntity(String orderId, String goodsIds, String goodsNums, Date orderShippingTime,
                               Integer orderShippingExpressId, String orderShippingCode, String orderShippingMessage,Integer shipmentType,
                               String deliveryPerson, String deliveryTelephone, String deliveryCertificate, Integer storageId,String senderAddress) {
        this.orderId = orderId;
        this.goodsIds = goodsIds;
        this.goodsNums = goodsNums;
        this.orderShippingTime = orderShippingTime;
        this.orderShippingExpressId = orderShippingExpressId;
        this.orderShippingCode = orderShippingCode;
        this.orderShippingMessage = orderShippingMessage;
        this.createTime = DateUtils.getNowDateToDf();
        this.shipmentType = shipmentType;
        this.deliveryPerson = deliveryPerson;
        this.deliveryTelephone = deliveryTelephone;
        this.deliveryCertificate = deliveryCertificate;
        this.storageId = storageId;
        this.senderAddress = senderAddress;
    }

}
