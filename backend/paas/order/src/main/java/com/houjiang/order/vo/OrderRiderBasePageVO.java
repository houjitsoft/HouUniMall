package com.houjiang.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @ClassName OrderRiderBasePageVO
 * @Description 骑手订单分页出参
 * @Author Amos
 * @Date 2024/4/29 15:44
 * @Version 1.0
 * @author Amos
 */
@Data
public class OrderRiderBasePageVO {

    /**
     * 订单ID
     */
    private String orderId;

    /**
     * 应付金额（商品实际支付金额 + 运费）-订单金额
     */
    private BigDecimal orderPaymentAmount;

    /**
     * 支付(付款)时间
     */
    private String paymentTime;

    /**
     * 卖家店铺名称
     */
    private String shopName;

    /**
     * 发货人的地址-取货地址
     */
    private String orderSellerAddress;

    /**
     * 买家姓名-买家账号
     */
    private String buyerUserName;

    /**
     * 收货人
     */
    private String orderReceiverName;

    /**
     * 收货人的联系方式-买家手机号
     */
    private String orderReceiverContact;

    /**
     * 收货人的详细地址-收货人地址
     */
    private String orderReceiverAddress;

    /**
     * 提货编号
     */
    private String pickupNum;

    /**
     * 骑手id
     */
    private String distributorId;

    /**
     * 0 待取货 1待配送
     */
    private Integer pickup;

    /**
     * 骑手名称
     */
    private String riderName;

    /**
     * 骑手电话
     */
    private String riderTel;

    /**
     * 订单状态 2待配货 3待发货 4已发货 6已完成
     */
    private Integer orderStatus;

    /**
     * 是否评价骑手0-否 1-是  已评价或未评价
     */
    private Integer distributorEvaluation;

    /**
     * 是否骑手删除0-否 1-是 ,默认传0
     */
    private Integer isDeliveryDel;

    /**
     * 店铺地址
     */
    private String shopAddress;

    /**
     * 店铺电话
     */
    private String shopTel;

    /**
     * 经度
     */
    private String longitude;

    /**
     * 纬度
     */
    private String latitude;

    /**
     * 配送时间
     */
    private String orderShippingTime;

    /**
     * 订单完成时间
     */
    private String orderFinishedTime;

    /**
     * 门店名称
     */
    private String shopGateName;

    /**
     * 省
     */
    private String province;
    /**
     * 市
     */
    private String city;
    /**
     * 区/县
     */
    private String county;
    /**
     * 门店地址
     */
    private String detailAddress;

    /**
     * 买家手机号
     */
    private String userMobile;

    /**
     * 买家昵称
     */
    private String userName;

    /**
     * 骑手接单时间
     */
    private String deliveryTime;

    /**
     * 加配送费
     */
    private String deliveryCharge;

    /**
     * 骑手完成时间
     */
    private String  deliveryEndTime;

    /**
     * 配送距离
     */
    private String distance;
}
