package com.houjiang.commons.dto.order;

import lombok.Data;

import java.util.List;

/**
 * 店铺结算账户信息
 * @author Amos
 */
@Data
public class SupplierShopSettleAccountInfoDTO {

    /**
     * 商品总金额
     */
    private Double orderGoodsAmount = 0.00;

    /**
     * 红包
     */
    private Double orderRptPrice = 0.00;

    /**
     * 退款金额
     */
    private Double returnCash = 0.00;

    /**
     * 应付金额（商品实际支付金额 + 运费）
     */
    private Double orderPaymentAmount = 0.00;

    /**
     * 运费价格
     */
    private Double orderShippingFee = 0.00;

    /**
     * 售后红包金额
     */
    private Double returnRptCash = 0.00;

    /**
     * 待发货订单 经销商佣金
     */
    private Double dealerCommission = 0.00;

    /**
     * 经销商退还佣金
     */
    private Double dealerReturnCommission = 0.00;

    /**
     * 待发货订单供应商佣金
     */
    private Double supplyCommission = 0.00;

    /**
     * 供应商退还佣金
     */
    private Double supplyReturnCommission = 0.00;

    /**
     * 供货价
     */
    private Double orderSupplyAmount = 0.00;

    /**
     * 佣金金额
     */
    private Double commissionMoney = 0.00;

    /**
     * 佣金
     */
    private Double orderCommissionFee = 0.00;

    /**
     * 退还佣金
     */
    private Double orderReturnCommissionFee = 0.00;

    /**
     * 会员折扣
     */
    private Double baseMemberDiscount = 0.00;

    /**
     * 退还会员折扣
     */
    private Double returnMemberDiscount = 0.00;

    /**
     * 推广佣金
     */
    private Double tgCommission = 0.00;

    /**
     * 分享佣金
     */
    private Double fxCommission = 0.00;

    /**
     * 供应商 折扣等级 的折扣金额
     */
    private Double supperDiscountFee = 0.00;

    /**
     * 退还 供应商折扣等级 的折扣金额
     */
    private Double returnSupperDiscountFee = 0.00;

    /**
     * 供货价 不含运费
     */
    private Double supperPriceSum = 0.00;

    /**
     * 退还 供货价 不含运费
     */
    private Double supperReturnPrice = 0.00;

    /**
     * 退还 运费
     */
    private Double returnShippingFee = 0.00;
    /**
     * 订单id列表
     */
    private List<String> orderIdList;
    /**
     * 退单id列表
     */
    private List<String> returnOrderIdList;

}
