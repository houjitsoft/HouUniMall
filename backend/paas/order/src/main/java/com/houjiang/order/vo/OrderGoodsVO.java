package com.houjiang.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @ClassName OrderGoodsVo
 * @Description TODO
 * @Author Amos
 * @Date 2024/8/15 9:10
 * @Version 1.0
 * @author Amos
 */
@Data
public class OrderGoodsVO {
    /**
     * 订单号
     */
    private String orderId;
    /**
     * 分类佣金
     */
    private BigDecimal orderCommissionFee;
    /**
     * 商品下单数量
     */
    private Integer orderGoodsNum;
    /**
     * 商品退单数量
     */
    private Integer  orderGoodsReturnNum;
    /**
     * 订单商品佣金
     */
    private BigDecimal orderGoodsCommission;
    /**
     * 会员折扣
     */
    private BigDecimal goodsMemberDiscount;
}
