package com.houjiang.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @ClassName VirtualGateShopOrderVO
 * @Description
 * @Author Amos
 * @Date 2023/8/3 17:39
 * @Version 1.0
 * @author Amos
 */
@Data
public class VirtualGateShopOrderVO {

    /**
     * 订单号
     */
    private String orderId;

    /**
     * 卖家店铺id
     */
    private Integer shopId;

    /**
     * 卖家门店id
     */
    private Integer orderGateShopId;

    /**
     * 订单生成日期
     */
    private String orderCreateTime;

    /**
     * 收货人的姓名
     */
    private String orderReceiverName;

    /**
     * 收货人联系方式
     */
    private String orderReceiverContact;

    /**
     * 订单状态
     */
    private Integer orderStatus;

    /**
     * 商品id
     */
    private Integer commonId;

    /**
     * 商品goodsid
     */
    private Integer goodsId;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 商品价格
     */
    private BigDecimal goodsPrice;

    /**
     * 订单商品数量
     */
    private Integer orderGoodsNum;

    /**
     * 订单实付金额
     */
    private BigDecimal orderGoodsPaymentAmount;

    /**
     * 商品图片
     */
    private String goodsImage;

    /**
     * 虚拟商品兑换码
     */
    private String redeemCode;

    /**
     * 是否核销0是1否 2已过期
     */
    private String isCancel;

    /**
     * 核销时间
     */
    private String cancelDate;

    /**
     * 商品总价格(不包含运费)
     */
    private BigDecimal orderGoodsAmount;

    /**
     * 商品支付价格(不包含运费)
     */
    private BigDecimal orderPaymentAmount;

    /**
     * 经销折扣
     */
    private BigDecimal distributionDiscount;
}
