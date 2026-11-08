package com.houjiang.order.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @ClassName SubclassDTO
 * @Description
 * @Author Amos
 * @Date 2023/9/18 9:48
 * @Version 1.0
 * @author Amos
 */
@Data
public class SubclassDTO {

    /**
     * 支付编号
     */
    String paymentNumber;

    /**
     * 订单编号
     */
    String orderId;

    /**
     * 卖家ID
     */
    String sellerId;

    /**
     * 拼团公共ID
     */
    Integer pinCommonId;

    /**
     * 标记ID
     */
    String markId;

    /**
     * 类型
     */
    Integer type;

    /**
     * 是否标记
     */
    Integer isMark;

    /**
     * 团购商品ID
     */
    Integer groupBookingGoodsId;

    /**
     * A尺寸
     */
    Integer sizeA;

    /**
     * B尺寸
     */
    Integer sizeB;

    /**
     * C尺寸
     */
    Integer sizeC;

    /**
     * 订单供货价
     */
    BigDecimal orderSupplyPrice;

    /**
     * 供货店ID
     */
    Integer supplyShopId;

    /**
     * 供货店名称
     */
    String supplyShopName;

    /**
     * 订单商品佣金总额(不包含商家折扣)
     */
    BigDecimal orderGoodsCommission;

    /**
     * 订单基础佣金总额(包含商家折扣)
     */
    BigDecimal orderBaseCommission;

    /**
     * 商品会员折扣总额(不包含多多进宝推广费用和分享返佣费用)
     */
    BigDecimal goodsMemberDiscount;

    /**
     * 基础会员折扣总额(包含多多进宝推广费用和分享返佣费用)
     */
    BigDecimal baseMemberDiscount;

    /**
     * 小额订单标识：0->非小额订单; 1->小额订单。和小额订单关系：商家侧看得到的可售量只包含有库存的商品数量，不包含小额订单的商品数量。
     */
    Integer smallOrder;

    /**
     * 订单促销活动ID，如果该订单没有参加促销活动则为0。
     */
    Integer orderPromotionId;

    /**
     * 团购状态：0->未成团；1->已成团；2->已退款；3->已关闭；4->已过期；5->已生效；6->已撤销；7->已支付；8->已拼团成功；9->已拼团失败。
     */
    Integer groupbuyStatus;

    /**
     * 订单购买ID，拼团业务使用。
     */
    String orderBuyId;

    /**
     * 商品数量，
     */
    Integer goodsQuantity;

    /**
     * 红包类型
     */
    Integer redpacketTType;

    /**
     * 商品店铺ID
     */
    String itemShopId;

    /**
     * 优惠店铺ID
     */
    String allowanceShopId;

    /**
     * 店铺限制信息
     */
    String shopLimit;

    /**
     * 优惠券状态：0->未使用；1->已使用；2->已过期。如果查询不到对应的优惠券状态则值为null。可以通过接口查询优惠券状态获取到该值。
     */
    Integer voucherStatus;


    /**
     * 代金券拆分累加金额
     */
    BigDecimal orderCptPrice; 
    /**
     *goods表实付价格(含运费，去除红包拆分价格)
     */
    BigDecimal paymentRealPrice; 
    /**
     *商品红包拆分累加金额
     */
    BigDecimal orderGoodsRptPrice; 
    /**
     *base表实付金额
     */
    BigDecimal orderBasePaymentAmount; 
    /**
     *记录店铺每次红包拆分金额
     */
    BigDecimal redPacketSumMoney; 
    /**
     *平台优惠券店铺拆分
     */
    BigDecimal platformVoucherShopPrice; 
    /**
     *优惠活动中平台承担的金额
     */
    BigDecimal orderGoodsPlatformBear; 
    /**
     *
     */
    BigDecimal orderGoodsAddPlatformBear;
    /**
     *平台优惠券拆分
     */
    BigDecimal platformVoucherRptPrice; 
    /**
     *
     */
    BigDecimal platformVoucherRptAddPrice;
    /**
     *
     */
    BigDecimal couponPrice;
    /**
     *记录商品每次红包拆分金额
     */
    BigDecimal redPacketGoodsSumMoney; 
    /**
     *发票ID
     */
    Integer invoiceId;
    /**
     * 开票状态:0-未开票;1-审核中;2-已出票;3-未通过
     */
    private Integer invoiceStatus;

    /**
     * 分佣比例
     */
    private BigDecimal catCommission;

}
