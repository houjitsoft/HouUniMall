package com.houjiang.order.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 应用退货
 * @author Amos
 */
@Data
public class ApplyReturnGoodDTO {

    /**
     * 换货商品id
     */
    private Integer barterGoodsId;

    /**
     * 换货商品数量
     */
    private Integer barterGoodsNum;

    /**
     * 订单商品数量
     */
    private String orderGoodsNum;

    /**
     * 订单商品价格
     */
    private String orderGoodsPrice;

    /**
     * 退货商品编号，0为退款
     */
    private String orderGoodsId;

    /**
     * 订单商品名
     */
    private String orderGoodsName;

    /**
     * 订单商品图
     */
    private String orderGoodsPic;

    /**
     * 商品总金额
     */
    private Double orderGoodsAmount;

    /**
     * 运费
     */
    private Double shippingFee;

    /**
     * 商品总运费
     */
    private Double totalGoodsShippingFee;

    /**
     * 实付金额
     */
    private Double orderGoodsPaymentAmount;

    /**
     * 经销商佣金
     */
    private BigDecimal dealerCommission;

    @Override
    public String toString() {
        return "ApplyReturnGoods [orderGoodsNum=" + orderGoodsNum + ", orderGoodsPrice=" + orderGoodsPrice
                + ", orderGoodsId=" + orderGoodsId + ", orderGoodsName=" + orderGoodsName + ", orderGoodsPic="
                + orderGoodsPic + "]";
    }

    public void setOrderGoodsNum(String orderGoodsNum) {
        if(this.barterGoodsNum != null){
            this.orderGoodsNum = String.valueOf(this.barterGoodsNum);
        }else{
            this.orderGoodsNum = orderGoodsNum;
        }
    }

}
