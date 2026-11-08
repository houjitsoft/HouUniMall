package com.houjiang.commons.vo.order;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @ClassName OrderBaseListVO
 * @Description
 * @Author Amos
 * @Date 2023/7/18 10:36
 * @Version 1.0
 * @author Amos
 */
@Data
public class OrderBaseListVO {

    /**
     * 订单编号
     */
    private String orderId;

    /**
     * 订单金额
     */
    private BigDecimal orderPaymentAmount;

    /**
     * 供货价
     */
    private BigDecimal orderSupplyAmount;

    /**
     * 红包金额
     */
    private BigDecimal orderRptPrice;

    /**
     * 运费
     */
    private BigDecimal orderShippingFee;

    /**
     * 佣金
     */
    private BigDecimal orderCommissionFee;

    /**
     * 下单时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date orderCreateTime;

    /**
     * 成交时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date orderFinishedTime;

    /**
     * 买家账号
     */
    private String buyerUserName;

    /**
     * 会员折扣
     */
    private BigDecimal baseMemberDiscount;

    /**
     * 推广佣金
     */
    private BigDecimal tgCommission = new BigDecimal(0.00);

    /**
     * 分享佣金
     */
    private BigDecimal fxCommission = new BigDecimal(0.00);

    /**
     * 经销折扣金额
     */
    private BigDecimal supperDiscountFee = new BigDecimal(0.00);

    /**
     * 经销订单金额
     */
    private BigDecimal supperPriceSum = new BigDecimal(0.00);

    /**
     * 商品id
     */
    private Integer goodsId;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 商品实际支付价格
     */
    private BigDecimal orderGoodsPaymentAmount;

    /**
     * 组合活动类型 1固定组合 2搭配组合
     */
    private Integer combinationType;

    /**
     * 订单商品价格
     */
    private BigDecimal orderBasePaymentAmount;

    /**
     * 订单会员折扣
     */
    private BigDecimal orderBaseMemberDiscount;

    /**
     * 订单佣金
     */
    private BigDecimal orderBaseCommissionFee;

    /**
     * 自销商品佣金
     */
    private BigDecimal selfSellingCommission = new BigDecimal(0.00);

    /**
     * 自销商品运费
     */
    private BigDecimal selfSellingShippingFee = new BigDecimal(0.00);

    /**
     * 自销退单金额
     */
    private BigDecimal selfSellingReturnCash = new BigDecimal(0.00);

    /**
     * 自销退还佣金
     */
    private BigDecimal selfSellingReturnCommission = new BigDecimal(0.00);

    /**
     * 经销商品金额
     */
    private BigDecimal sellOnAmount = new BigDecimal(0.00);

    /**
     * 经销商品佣金
     */
    private BigDecimal sellOnCommission = new BigDecimal(0.00);

    /**
     * 经销退单金额
     */
    private BigDecimal sellOnReturnCash = new BigDecimal(0.00);

    /**
     * 经销退还佣金
     */
    private BigDecimal sellOnReturnCommission = new BigDecimal(0.00);

    /**
     * 是否供应商代发货 1是  2否
     */
    private Integer dropShipping;
}
