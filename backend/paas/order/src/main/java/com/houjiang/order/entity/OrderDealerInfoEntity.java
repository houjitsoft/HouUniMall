package com.houjiang.order.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @ClassName
 * @Description  经销单 经销折扣信息
 * @Author Amos
 * @Date 2024-08-01 09:45:07
 * @Version 1.0
 * @author Amos
 */
@TableName("hou_order_dealer_info")
@Data
public class OrderDealerInfoEntity implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
    private Integer id;
    /**
     * 订单号
     */
    private String orderId;
    /**
     * 商家skuid
     */
    private Integer goodsId;
    /**
     * 供应商skuid
     */
    private Integer supplierGoodsId;
    /**
     * 含税金额
     */
    private BigDecimal taxInclusiveAmount;
    /**
     * 含税金额  *  下单数量
     */
    private BigDecimal totalTaxInclusiveAmount;
    /**
     * 经销折扣
     */
    private BigDecimal dealerDiscount;
    /**
     * 经销折扣金额
     */
    private BigDecimal dealerDiscountAmout = new BigDecimal(0);
    /**
     * 一件代发商品拆分前的原始订单号
     */
    private String originalOrder;
}
