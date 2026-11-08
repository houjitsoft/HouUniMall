package com.houjiang.order.entity;



import com.baomidou.mybatisplus.annotation.TableField;

import com.baomidou.mybatisplus.annotation.TableId;

import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;



import java.io.Serializable;

import java.math.BigDecimal;



/**

 * @TableName order_supplier_base

 * @author Amos

 */

@TableName(value ="hou_order_supplier_base")

@Data

public class OrderSupplierBaseEntity implements Serializable {



    /**

     * id

     */

    @TableId

    private Integer id;



    /**

     * 订单号

     */

    private String orderId;



    /**

     * 供应商店铺id

     */

    private Integer supplierShopId;



    /**

     * 商品id

     */

    private Integer goodsId;



    /**

     * 商品id

     */

    private Integer commonId;



    /**

     * 供应商 的商品价格

     */

    private BigDecimal goodsPrice;



    /**

     * 购买数量

     */

    private Integer goodsNum;



    /**

     * 订单价 商品价格乘购买数量 减运费 

     */

    private BigDecimal goodsPriceSum;



    /**

     * 供应商实付金额 含运费

     */

    private BigDecimal goodsPaymentAmount;



    /**

     * 退款金额

     */

    private BigDecimal orderRefundPrice;



    /**

     * 退货金额

     */

    private BigDecimal orderReturnPrice;



    /**

     * 退还的经销折扣 全退 退经销折扣 半退不退

     */

    private BigDecimal orderReturnDiscount;



    /**

     * 平台抽供应商佣金

     */

    private BigDecimal supperCommission;



    /**

     * 平台抽供应商退还佣金

     */

    private BigDecimal supperReturnCommission;



    @TableField(exist = false)

    private static final long serialVersionUID = 1L;



    @Override

    public boolean equals(Object that) {

        if (this == that) {

            return true;

        }

        if (that == null) {

            return false;

        }

        if (getClass() != that.getClass()) {

            return false;

        }

        OrderSupplierBaseEntity other = (OrderSupplierBaseEntity) that;

        return (this.getOrderId() == null ? other.getOrderId() == null : this.getOrderId().equals(other.getOrderId()))

            && (this.getSupplierShopId() == null ? other.getSupplierShopId() == null : this.getSupplierShopId().equals(other.getSupplierShopId()))

            && (this.getGoodsId() == null ? other.getGoodsId() == null : this.getGoodsId().equals(other.getGoodsId()))

            && (this.getCommonId() == null ? other.getCommonId() == null : this.getCommonId().equals(other.getCommonId()))

            && (this.getGoodsPrice() == null ? other.getGoodsPrice() == null : this.getGoodsPrice().equals(other.getGoodsPrice()))

            && (this.getGoodsNum() == null ? other.getGoodsNum() == null : this.getGoodsNum().equals(other.getGoodsNum()))

            && (this.getGoodsPriceSum() == null ? other.getGoodsPriceSum() == null : this.getGoodsPriceSum().equals(other.getGoodsPriceSum()))

            && (this.getGoodsPaymentAmount() == null ? other.getGoodsPaymentAmount() == null : this.getGoodsPaymentAmount().equals(other.getGoodsPaymentAmount()))

            && (this.getOrderRefundPrice() == null ? other.getOrderRefundPrice() == null : this.getOrderRefundPrice().equals(other.getOrderRefundPrice()))

            && (this.getOrderReturnPrice() == null ? other.getOrderReturnPrice() == null : this.getOrderReturnPrice().equals(other.getOrderReturnPrice()))

            && (this.getOrderReturnDiscount() == null ? other.getOrderReturnDiscount() == null : this.getOrderReturnDiscount().equals(other.getOrderReturnDiscount()))

            && (this.getSupperCommission() == null ? other.getSupperCommission() == null : this.getSupperCommission().equals(other.getSupperCommission()))

            && (this.getSupperReturnCommission() == null ? other.getSupperReturnCommission() == null : this.getSupperReturnCommission().equals(other.getSupperReturnCommission()));

    }



    @Override

    public int hashCode() {

        final int prime = 31;

        int result = 1;

        result = prime * result + ((getOrderId() == null) ? 0 : getOrderId().hashCode());

        result = prime * result + ((getSupplierShopId() == null) ? 0 : getSupplierShopId().hashCode());

        result = prime * result + ((getGoodsId() == null) ? 0 : getGoodsId().hashCode());

        result = prime * result + ((getCommonId() == null) ? 0 : getCommonId().hashCode());

        result = prime * result + ((getGoodsPrice() == null) ? 0 : getGoodsPrice().hashCode());

        result = prime * result + ((getGoodsNum() == null) ? 0 : getGoodsNum().hashCode());

        result = prime * result + ((getGoodsPriceSum() == null) ? 0 : getGoodsPriceSum().hashCode());

        result = prime * result + ((getGoodsPaymentAmount() == null) ? 0 : getGoodsPaymentAmount().hashCode());

        result = prime * result + ((getOrderRefundPrice() == null) ? 0 : getOrderRefundPrice().hashCode());

        result = prime * result + ((getOrderReturnPrice() == null) ? 0 : getOrderReturnPrice().hashCode());

        result = prime * result + ((getOrderReturnDiscount() == null) ? 0 : getOrderReturnDiscount().hashCode());

        result = prime * result + ((getSupperCommission() == null) ? 0 : getSupperCommission().hashCode());

        result = prime * result + ((getSupperReturnCommission() == null) ? 0 : getSupperReturnCommission().hashCode());

        return result;

    }



    @Override

    public String toString() {

        StringBuilder sb = new StringBuilder();

        sb.append(getClass().getSimpleName());

        sb.append(" [");

        sb.append("Hash = ").append(hashCode());

        sb.append(", orderId=").append(orderId);

        sb.append(", supplierShopId=").append(supplierShopId);

        sb.append(", goodsId=").append(goodsId);

        sb.append(", commonId=").append(commonId);

        sb.append(", goodsPrice=").append(goodsPrice);

        sb.append(", goodsNum=").append(goodsNum);

        sb.append(", goodsPriceSum=").append(goodsPriceSum);

        sb.append(", goodsPaymentAmount=").append(goodsPaymentAmount);

        sb.append(", orderRefundPrice=").append(orderRefundPrice);

        sb.append(", orderReturnPrice=").append(orderReturnPrice);

        sb.append(", orderReturnDiscount=").append(orderReturnDiscount);

        sb.append(", supperCommission=").append(supperCommission);

        sb.append(", supperReturnCommission=").append(supperReturnCommission);

        sb.append(", serialVersionUID=").append(serialVersionUID);

        sb.append("]");

        return sb.toString();

    }

}