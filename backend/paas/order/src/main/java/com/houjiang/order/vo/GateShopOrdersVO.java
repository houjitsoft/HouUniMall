package com.houjiang.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @ClassName GateShopOrdersVO
 * @Description
 * @Author Amos
 * @Date 2023/8/3 17:10
 * @Version 1.0
 * @author Amos
 */
@Data
public class GateShopOrdersVO {

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
     * 订单商品数量
     */
    private Integer orderGoodsNum;

    /**
     * 实付金额
     */
    private BigDecimal orderGoodsPaymentAmount;

    /**
     * 商品图片
     */
    private String goodsImage;
}
