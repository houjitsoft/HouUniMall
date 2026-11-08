package com.houjiang.order.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @ClassName LogisticsMessageVO
 * @Description
 * @Author Amos
 * @Date 2023/8/14 9:37
 * @Version 1.0
 * @author Amos
 */
@Data
public class LogisticsMessageVO {

    /**
     * 店铺名称
     */
    private String shippingName;

    /**
     * 商品信息
     */
    private List<ShippingGoodsMessageVO> goodsMessage;

    /**
     * 物流单号
     */
    private String orderShippingCode;

    /**
     * 配送公司ID
     */
    private Integer orderShippingExpressId;

    /**
     * 卖家备注
     */
    private String orderShippingMessage;

    /**
     * 收货人姓名
     */
    private String orderReceiverName;

    /**
     * 收货人地址
     */
    private String orderReceiverAddress;

    /**
     * 收货人联系方式
     */
    private String orderReceiverContact;

    /**
     * 订单商品id
     */
    private Integer orderGoodsId;

    /**
     * 订单发货id
     */
    private Integer shipmentId;

    /**
     * 订单id
     */
    private String orderId;

    /**
     * 订单发货商品id
     */
    private String goodsIds;

    /**
     * 订单发货商品数量
     */
    private String goodsNums;

    /**
     * 物流信息
     */
    private String shippingInfo;

    /**
     * 配送时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date orderShippingTime;
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
     * 发货地址（针对物流、快递）
     */
    private String senderAddress;
}
