package com.houjiang.order.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author Amos
 * @date 2021-05-2808:38
 * @Description: 经销商品订单金额计算
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SupplyOrderPriceVO {
    /**
     * 订单金额  商品总金额 + 运费 - 等级折扣
     */
    private BigDecimal orderPaymentPrice = BigDecimal.ZERO;

    /**
     * 商品总金额   商品总金额
     */
    private BigDecimal goodsTotalPrice = BigDecimal.ZERO;

    /**
     * 运费总金额
     */
    private BigDecimal totalShippingPrice = BigDecimal.ZERO;

    /**
     * 等级折扣总金额
     */
    private BigDecimal totalDisountPrice = BigDecimal.ZERO;

    /**
     * 订单总金额   商品总金额 + 运费总金额
     */
    private BigDecimal orderTotalPrice = BigDecimal.ZERO;

    public void setTotalShippingPrice(BigDecimal totalShippingPrice) {
        this.totalShippingPrice = null== this.totalShippingPrice?totalShippingPrice:this.totalShippingPrice.add(totalShippingPrice);
    }

    public void setGoodsTotalPrice(BigDecimal goodsTotalPrice) {
        this.goodsTotalPrice = this.goodsTotalPrice.add(goodsTotalPrice);
    }

    public void setTotalDisountPrice(BigDecimal totalDisountPrice) {
        this.totalDisountPrice = this.totalDisountPrice.add(totalDisountPrice);
    }

    public BigDecimal getOrderTotalPrice() {
        return this.goodsTotalPrice.add(this.totalShippingPrice);
    }

    public BigDecimal getOrderPaymentPrice() {
        return this.goodsTotalPrice.add(this.totalShippingPrice).subtract(this.totalDisountPrice);
    }
}
