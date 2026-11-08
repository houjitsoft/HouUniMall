package com.houjiang.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @ClassName DealerOrderDetailVO
 * @Description
 * @Author Amos
 * @Date 2023/8/10 13:41
 * @Version 1.0
 * @author Amos
 */
@Data
public class DealerOrderDetailVO {

    /**
     * 订单id
     */
    private String orderId;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 商品图片
     */
    private String goodsImage;

    /**
     * 实付金额
     */
    private BigDecimal orderGoodsPaymentAmount;

    /**
     * 商品价格
     */
    private BigDecimal goodsPrice;

    /**
     * 订单创建时间
     */
    private String orderCreateTime;

    /**
     * 发货店铺
     */
    private String deliverShopName;
}
