package com.houjiang.order.vo;

import lombok.Data;

/**
 * @ClassName YiYaTongOrderVO
 * @Description
 * @Author Amos
 * @Date 2024/12/17 10:31
 * @Version 1.0
 * @author Amos
 */
@Data
public class YiYaTongOrderVO {

    /**
     * 订单ID
     */
    private String orderId;

    /**
     * 订单状态（例如：已创建、已支付、已发货等）
     */
    private Integer orderStatus;

    /**
     * 买家用户ID
     */
    private String buyerUserId;

    /**
     * 买家用户名
     */
    private String buyerUserName;

    /**
     * 收件人姓名
     */
    private String orderReceiverName;
    /**
     * 订单收货人联系方式
     */
    private String orderReceiverContact;

    /**
     * 订单收货地址
     */
    private String orderReceiverAddress;

    /**
     * 运费
     */
    private String shippingFee;

    /**
     * 订单商品支付金额
     */
    private String orderGoodsPaymentAmount;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 商品价格
     */
    private String goodsPrice;

    /**
     * 商品建议零售价
     */
    private String msrp;

    /**
     * 订单中商品的数量
     */
    private String orderGoodsNum;

    /**
     * 订单商品折扣费用
     */
    private String orderGoodsDiscountFee;

    /**
     * 订单创建时间
     */
    private String orderCreateTime;

    /**
     * 支付时间
     */
    private String paymentTime;

    /**
     * 订单完成时间
     */
    private String orderFinishedTime;

    /**
     * 退货方式类型
     */
    private String returnMethodType;

    /**
     * 退货申请创建时间
     */
    private String returnCreateTime;

    /**
     * 退货处理完成时间
     */
    private String returnFinishTime;

}
