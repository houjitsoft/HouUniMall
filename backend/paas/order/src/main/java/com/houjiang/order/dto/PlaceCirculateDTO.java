package com.houjiang.order.dto;

import com.houjiang.commons.vo.order.WapShopShippingResultVO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @ClassName PlaceCirculateDTO
 * @Description 下单循环参数
 * @Author Amos
 * @Date 2023/7/3 16:44
 * @Version 1.0
 * @author Amos
 */
@Data
public class PlaceCirculateDTO {

    /**
     * 订单的供应价格
     */
    private BigDecimal orderSupplyPrice;

    /**
     * 供应商店铺的ID
     */
    private Integer supplyShopId;

    /**
     * 供应商店铺的名称
     */
    private String supplyShopName;

    /**
     * 订单总会员折扣金额
     */
    private BigDecimal huiYuanZhekou;

    /**
     * 店铺总税费价格
     */
    private BigDecimal totalTaxIncludedPrice;

    /**
     * 平台佣金
     */
    private BigDecimal orderGoodsCommission;

    /**
     * 平台佣金
     */
    private BigDecimal orderBaseCommission;

    /**
     * 会员折扣
     */
    private BigDecimal baseMemberDiscount;

    /**
     * 是否参加活动值
     */
    private Integer orderPromotionId;

    /**
     * 拼团状态值
     */
    private Integer groupbuyStatus;

    /**
     * 订单买家id
     */
    private String orderBuyId;

    /**
     * 每家店铺商品个数
     */
    private Integer goodsQuantity;

    /**
     * 红包装类型
     */
    private Integer redpacketTType;

    /**
     * 单品红包所属shopid
     */
    private String itemShopId;

    /**
     * 跨店满减所属shopid
     */
    private String allowanceShopId;

    /**
     * 获取店铺红包id
     */
    private String shopLimit;

    /**
     * 优惠券使用状态
     */
    private Integer voucherStatus;

    /**
     * 店铺id
     */
    private Integer shopId;

    /**
     * 店铺id变量
     */
    private String shopIdByStr;

    /**
     * 代金券拆分累加金额
     */
    private BigDecimal orderCptPrice;

    /**
     * goods表实付价格(含运费，去除红包拆分价格)
     */
    private BigDecimal paymentRealPrice;

    /**
     * 商品红包拆分累加金额
     */
    private BigDecimal orderGoodsRptPrice;

    /**
     * base表实付金额
     */
    private BigDecimal orderBasePaymentAmount;

    /**
     * 记录店铺每次红包拆分金额
     */
    private BigDecimal redPacketSumMoney;

    /**
     * 平台优惠券店铺拆分
     */
    private BigDecimal platformVoucherShopPrice;

    /**
     * 优惠活动中平台承担的金额
     */
    private BigDecimal orderGoodsPlatformBear;

    /**
     * 优惠活动
     */
    private BigDecimal orderGoodsAddPlatformBear;

    /**
     * 平台优惠券拆分
     */
    private BigDecimal platformVoucherRptPrice;

    /**
     * 平台优惠券
     */
    private BigDecimal platformVoucherRptAddPrice;

    /**
     * 店铺所在地
     */
    private Integer districtId;

    /**
     * 每家店铺使用优惠券价格
     */
    private BigDecimal couponPrice;

    /**
     * 商店名称
     */
    private String shopName;

    /**
     * 买家留言
     */
    private String buyerMessage;

    /**
     * 运费
     */
    private BigDecimal shippingFee;

    /**
     * 订单商品时间
     */
    private String orderGoodsTime;

    /**
     * 标志位
     */
    private Boolean flag;

    /**
     * VK标志位
     */
    private Boolean vkFlag;

    /**
     * 店铺 返回对象
     */
    private List<WapShopShippingResultVO> wssrList;

    /**
     * 总经销商佣金
     */
    private BigDecimal totalDealerCommission;

    /**
     * 总供应商佣金
     */
    private BigDecimal totalSupplyCommission;

    /**
     * 订单日期
     */
    private String orderDate;

    /**
     * 买家评价状态
     */
    private Integer orderBuyerEvaluationStatus;

    /**
     * 订单状态
     */
    private Integer orderStatus;

    /**
     * 总优惠金额
     */
    private BigDecimal orderDiscountFee;

    /**
     * 订单类型
     */
    private Integer orderType;

    /**
     * 商品佣金比例
     */
    private BigDecimal orderGoodsCommissionRate;
}
