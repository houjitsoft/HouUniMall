package com.houjiang.order.vo;

import lombok.Data;

/**
 * @ClassName OrderBaseInfoVO
 * @Description
 * @Author Amos
 * @Date 2023/7/17 14:42
 * @Version 1.0
 * @author Amos
 */
@Data
public class OrderBaseInfoVO {

    /**
     * 订单编号
     */
    private String orderId;

    /**
     * 订单支付金额
     */
    private Double orderPaymentAmount;

    /**
     * 订单供货金额
     */
    private Double orderSupplyAmount;

    /**
     * 订单报告价
     */
    private Double orderRptPrice;

    /**
     * 订单运费
     */
    private Double orderShippingFee;

    /**
     * 订单佣金费用
     */
    private Double orderCommissionFee;

    /**
     * 订单创建时间
     */
    private String orderCreateTime;

    /**
     * 订单完成时间
     */
    private String orderFinishedTime;

    /**
     * 买家用户名
     */
    private String buyerUserName;

    /**
     * 卖家用户ID
     */
    private String sellerUserId;

    /**
     * 供货佣金
     */
    private Double supplyCommission;

    /**
     * 供货返利佣金
     */
    private Double supplyReturnCommission;

    /**
     * 会员折扣基准
     */
    private Double baseMemberDiscount;

    /**
     * 推广佣金
     */
    private Double tgCommission;

    /**
     * 分销佣金
     */
    private Double fxCommission;

    /**
     * 订单折扣费用
     */
    private Double orderDiscountFee;

    /**
     * 商品价格总和
     */
    private Double goodsPriceSum;

    /**
     * 商品id
     */
    private Integer goodsId;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 商品实际支付价格
     */
    private Double orderGoodsPaymentAmount;

}
