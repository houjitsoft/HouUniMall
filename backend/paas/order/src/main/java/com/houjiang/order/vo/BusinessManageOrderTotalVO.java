package com.houjiang.order.vo;

import lombok.Data;

/**
 * @ClassName BusinessManageOrderTotalVO
 * @Description 商城首页订单总计
 * @Author Amos
 * @Date 2023/6/25 13:37
 * @Version 1.0
 * @author Amos
 */
@Data
public class BusinessManageOrderTotalVO {

    /**
     * 待付款
     */
    private Integer WaitingForPayment;

    /**
     * 代发货
     */
    private Integer WaitForTheDelivery;

    /**
     * 已发货
     */
    private Integer Shipped;

    /**
     * 已完成
     */
    private Integer OffTheStocks;

    /**
     * 退货订单
     */
    private Integer SalesReturn;

    /**
     * 退款订单
     */
    private Integer Refund;

    /**
     * 售后订单
     */
    private Integer afterSales;

    /**
     * 评价订单
     */
    private Integer evaluate;
}
