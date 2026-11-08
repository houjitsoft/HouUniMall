package com.houjiang.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @ClassName AddOrderDetailsVO
 * @Description 新增商品详情
 * @Author Amos
 * @Date 2023/7/6 16:46
 * @Version 1.0
 * @author Amos
 */
@Data
public class AddOrderDetailsVO {
    /**
     * 商品名称
     */
    private String goodsName;
    /**
     * 商品单价
     */
    private BigDecimal goodsPrice;
    /**
     * 商品数量
     */
    private Integer orderGoodsNum;
    /**
     * 优惠券金额
     */
    private BigDecimal voucherPrice;
    /**
     * 红包抵扣金额
     */
    private BigDecimal orderRptPrice;
    /**
     * 优惠类型
     */
    private Integer orderDiscountType;
    /**
     * 折扣金额
     */
    private BigDecimal orderDiscountFee;
    /**
     * 价格
     */
    private BigDecimal orderGoodsAmount;
    /**
     * 规格
     */
    private String orderSpecChineseValue;
    /**
     * 购物津贴抵扣金额
     */
    private BigDecimal platformCouponRptPrice;
    /**
     * 实付价格
     */
    private BigDecimal orderGoodsPaymentAmount;
    /**
     * 商品参见活动类型 0 普通点单 1 砍价（废弃） 2 秒杀 3 限时折扣 4拼团 5 满减增折
     */
    private Integer promotionId;
    private BigDecimal shippingFee;
    /**
     * 商品id
     */
    private Integer goodsId;
    /**
     * 平台抽佣比例
     */
    private BigDecimal chouYongBili;
    /**
     * 供应商订单总价 实付价 （含运费）
     */
    private BigDecimal supperPaymentAmount;
    /**
     * 供应商订单总价 (不含运费)
     */
    private BigDecimal supperOrderAmount;
    /**
     * 用户订单是否供应商代发货 1是  2否
     */
    private Integer dropShipping;
    /**
     *
     */
    /** 供应商订单总价 (不含运费) */
    private BigDecimal supperGoodsPrice;
    /**
     * 运费
     */
    private BigDecimal totalGoodsShippingFee;
    /**
     * 手工调整金额
     */
    private BigDecimal orderGoodsAdjustFee;
    /**
     * 满折活动折扣金额
     */
    private BigDecimal fullFoldingDiscount;
    /**
     * 满减活动折扣金额
     */
    private BigDecimal fullReductionDiscount;
    /**
     * plus会员折扣金额
     */
    private BigDecimal goodsPlusDiscount;
    /**
     * 平台会员折扣
     */
    private BigDecimal goodsMemberDiscount;
    /**
     * 商品佣金
     */
    private BigDecimal orderGoodsCommission;

    /**
     * 积分比例
     */
    private Double pointsRate;


    /**
     * 现金金额
     */
    private BigDecimal cashPrice;


}
