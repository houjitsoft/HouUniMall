package com.houjiang.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 商铺活动
 *
 * @author Amos
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EntityObjectsDTO {

    /**
     * 商户订单id
     */
    private String orderId;

    /**
     * 店铺id
     */
    private Integer shopId;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 订单商品总数
     */
    private Integer goodsNum;

    /**
     * 店铺是否具备发票权限
     */
    private Integer shopInvoices;

    /**
     * 退货商品编号，0为退款
     */
    private Integer orderGoodsId;

    /**
     * 商品总价
     */
    private double goodsTotalPrice;

    /**
     * 有效商品详情
     */
    private List<?> goodsDetailsList;

    /**
     * 订单状态
     */
    private Integer orderGoodsSstatus;

    /**
     * 红包活动
     */
    private List<?> redEnvelopeActivity;

    /**
     * 优惠券活动
     */
    private List<?> discountCouponActicity;

    /**
     * 评价状态
     */
    private Integer orderGoodsEvaluationStatus;

    /**
     * 发票
     */
    private Integer invoiceActive;

    /**
     *plus是否开启 1 开启 0 关闭
     */
    private Integer plusStatus;

    /**
     * 失效商品详情
     */
    private List<?> invalidGoodsDetailsList;
}
