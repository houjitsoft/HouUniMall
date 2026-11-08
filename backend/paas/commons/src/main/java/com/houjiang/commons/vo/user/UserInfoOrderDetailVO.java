package com.houjiang.commons.vo.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 用户信息订单详情
 * @author Amos
 */
@Data
public class UserInfoOrderDetailVO {

    /**
     * 订单号
     */
    private String orderId;

    /**
     * 店铺名
     */
    private String shopName;

    /**
     * 支付时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date paymentTime;

    /**
     * 物流单号
     */
    private String orderShippingCode;

    /**
     * 应付金额
     */
    private Double orderPaymentAmount;

    /**
     * 支付方式
     */
    private int payCode;

    /**
     * 订单状态
     */
    private int orderStatus;

    /**
     * 商品名
     */
    private String goodsName;

    /**
     * 下单时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date orderCreateTime;

    /**
     * 订单完成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date orderFinishedTime;

    /**
     * 卖家姓名
     */
    private String sellerUserName;

    /**
     * 收货人名称
     */
    private String orderReceiverName;

    /**
     * 支付单号
     */
    private String paymentNumber;

    /**
     * 1-实物；2-虚拟；3-门店
     */
    private Integer orderType;
    /**
     * 过期标志
     */
    private Integer pastDue;

    /**
     * 是否取消
     */
    private Integer isCancel;

    /**
     * 虚拟商品
     */
    private Integer virtualGoods;

    /**
     * 订单是否店内取货
     */
    private Integer orderIsShopTake;

    /**
     * 退货标志
     */
    private Integer returnFlag;

    /**
     * 退货状态
     */
    private Integer returnStatus;

    /**
     * 退款状态
     */
    private Integer refundStatus;



}
