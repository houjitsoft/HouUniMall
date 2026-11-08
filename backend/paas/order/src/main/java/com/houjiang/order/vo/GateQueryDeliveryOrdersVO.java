package com.houjiang.order.vo;

import lombok.Data;

/**
 * @ClassName GateQueryDeliveryOrdersVO
 * @Description TODO
 * @Author Amos
 * @Date 2024/5/14 17:12
 * @Version 1.0
 * @author Amos
 */
@Data
public class GateQueryDeliveryOrdersVO {
    /**
     * 订单ID
     */
    private String orderId;

    /**
     * 商品名称
     */
    private String goodsName;
    /**
     * 商品ID
     */
    private String goodsId;
    /**
     * 店铺id
     */
    private Integer shopId;

    /**
     * 商品图片
     */
    private String goodsImage;

    /**
     * 商品数量
     */
    private String orderGoodsNum;
    /**
     * 商品单价
     */
    private String goodsPrice;
    /**
     * 实付价格
     */
    private String orderGoodsAmount;
    /**
     * 订单时间
     */
    private String orderGoodsTime;
    /**
     * 实付金额
     */
    private String orderPaymentAmount;
    /**
     * 骑手id
     */
    private String distributorId;
    /**
     * 买家昵称
     */
    private String userName;
    /**
     * 买家手机号
     */
    private String userMobile;
    /**
     * 0 待取货 1，待配送
     */
    private String pickup;
    /**
     * 商家确认,0未确认 ，1已确认
     */
    private String shopAffirm;

    /**
     * 订单金额
     */
    private String orderGoodsPaymentAmount;

    /**
     * 收货人姓名
     */
    private String orderReceiverName;

    /**
     * 收货人的联系方式
     */
    private String orderReceiverContact;

    /**
     * 提货编号
     */
    private String pickupNum;

    /**
     * 订单状态
     */
    private Integer orderStatus;
    /**
     * 门店id
     */
    private Integer orderGateShopId;
    /**
     * 门店名称
     */
    private String shopGateName;
    /**
     * 订单创建时间
     */
    private String orderCreateTime;
    /**
     * 退款状态:0是无退款,1是退款中,2是退款完成,3退款不通过
     */
    private String orderRefundStatus;

}
