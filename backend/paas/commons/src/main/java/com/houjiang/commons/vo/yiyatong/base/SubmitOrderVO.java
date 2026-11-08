package com.houjiang.commons.vo.yiyatong.base;

import lombok.Data;

/**
 * @ClassName SubmitOrderVO
 * @Description
 * @Author Amos
 * @Date 2024/11/13 17:45
 * @Version 1.0
 * @author Amos
 */
@Data
public class SubmitOrderVO {

    /**
     * 订单状态名称
     */
    private String orderStatusName;

    /**
     * 商品总金额(不含运费)
     */
    private String totalAmount;

    /**
     * 订单总金额(含运费)
     */
    private String payAmount;

    /**
     * 返回订单编号order_sn
     */
    private String orderSn;

    /**
     * 返回订单批次号batchOrderSn
     */
    private String batchOrderSn;

    /**
     * 订单状态：10为已提交待付款,20为已付款待发货,30为已发货待收货,40为确认收货交易成功,50、60为已取消,70、80、90、100为已关闭
     */
    private String orderStatus;

    /**
     * 商品SKU ID列表，记录订单中包含的商品SKU ID，多个ID以逗号分隔
     */
    private String skuIds;

    private String orderId;
}
