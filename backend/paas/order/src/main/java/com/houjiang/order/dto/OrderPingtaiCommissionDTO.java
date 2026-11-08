package com.houjiang.order.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @ClassName OrderPingtaiCommissionDTO
 * @Description
 * @Author Amos
 * @Date 2023/7/26 9:50
 * @Version 1.0
 * @author Amos
 */
@Data
public class OrderPingtaiCommissionDTO {

    /**
     * 商品分类ID
     */
    private Integer catId;

    /**
     * 商品退货数量
     */
    private Integer goodsReturnNum;

    /**
     * 商品数量
     */
    private Integer goodsNum;

    /**
     * 订单商品佣金
     */
    private BigDecimal orderGoodsCommission;

    /**
     * 商品会员折扣
     */
    private BigDecimal goodsMemberDiscount;

    /**
     * 是否使用直邮服务（0表示不使用，1表示使用）
     */
    private Integer dropShipping;

    /**
     * 发货店铺id
     */
    private Integer deliverShopId;

}
