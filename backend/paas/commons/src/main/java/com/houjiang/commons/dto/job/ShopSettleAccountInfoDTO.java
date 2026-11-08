package com.houjiang.commons.dto.job;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @ClassName ShopSettleAccountInfoDTO
 * @Description 店铺结算账户信息
 * @Author Amos
 * @Date 2023/5/17 16:31
 * @Version 1.0
 * @author Amos
 */
@Data
public class ShopSettleAccountInfoDTO {

    /**
     * 商品实付
     */
    private BigDecimal orderGoodsAmount = new BigDecimal(0.00);

    /**
     * 红包
     */
    private BigDecimal orderRptPrice = new BigDecimal(0.00);

    /**
     * 优惠券
     */
    private BigDecimal orderVoucherPrice = new BigDecimal(0.00);

    /**
     * 退款金额
     */
    private BigDecimal returnCash = new BigDecimal(0.00);

    /**
     * 总付款额度
     */
    private BigDecimal orderPaymentAmount = new BigDecimal(0.00);

    /**
     * 运费
     */
    private BigDecimal orderShippingFee = new BigDecimal(0.00);

    /**
     * 售后红包金额
     */
    private BigDecimal returnRptCash = new BigDecimal(0.00);

    /**
     * 退还优惠券
     */
    private BigDecimal returnVoucherPrice = new BigDecimal(0.00);

    /**
     * 供货价
     */
    private BigDecimal orderSupplyAmount = new BigDecimal(0.00);

    /**
     * 经销佣金
     */
    private BigDecimal orderDealerCommission = new BigDecimal(0.00);

    /**
     * 供应商佣金
     */
    private BigDecimal orderSupplyCommission = new BigDecimal(0.00);

    /**
     * 经销退还佣金
     */
    private BigDecimal returnDealerCommission = new BigDecimal(0.00);

    /**
     * 供应商退还佣金
     */
    private BigDecimal returnSupplyCommission = new BigDecimal(0.00);

    /**
     * 分佣金额
     */
    private BigDecimal commissionMoney = new BigDecimal(0.00);

    /**
     * Bonus积分
     */
    private Integer bonusPoints = 0;

    /**
     * 佣金
     */
    private BigDecimal orderCommissionFee = new BigDecimal(0.00);

    /**
     * 退还佣金
     */
    private BigDecimal orderReturnCommissionFee = new BigDecimal(0.00);

    /**
     * 会员折扣
     */
    private BigDecimal baseMemberDiscount = new BigDecimal(0.00);

    /**
     * 返回会员折扣
     */
    private BigDecimal returnMemberDiscount = new BigDecimal(0.00);

    /**
     * 推广佣金
     */
    private BigDecimal tgCommission = new BigDecimal(0.00);

    /**
     * 分享佣金
     */
    private BigDecimal fxCommission = new BigDecimal(0.00);

    /**
     * 推广佣金
     */
    private BigDecimal returnTgCommission = new BigDecimal(0.00);

    /**
     * 分享佣金
     */
    private BigDecimal returnFxCommission = new BigDecimal(0.00);

    /**
     * 经销折扣
     */
    private BigDecimal supperDiscountFee = new BigDecimal(0.00);

    /**
     * 退还经销折扣
     */
    private BigDecimal returnSupperDiscountFee = new BigDecimal(0.00);

    /**
     * 供货价
     */
    private BigDecimal supperPriceSum = new BigDecimal(0.00);

    /**
     * 退还经供货价
     */
    private BigDecimal supperReturnPrice = new BigDecimal(0.00);

    /**
     * 退还经供货价
     */
    private BigDecimal returnShippingFee = new BigDecimal(0.00);

    /**
     * 经销商佣金
     */
    private BigDecimal dealerCommission = new BigDecimal(0.00);

    /**
     * 经销商退还佣金
     */
    private BigDecimal dealerReturnCommission = new BigDecimal(0.00);

    /**
     * 待发货订单供应商佣金
     */
    private BigDecimal supplyCommission = new BigDecimal(0.00);

    /**
     * 待发货订单供应商退还佣金
     */
    private BigDecimal supplyReturnCommission = new BigDecimal(0.00);

    /**
     * webPos 现金订单佣金
     */
    private BigDecimal orderXjCommissionFee = new BigDecimal(0.00);

    /**
     * webPos 现金订单退还佣金
     */
    private BigDecimal orderReturnXjCommissionFee = new BigDecimal(0.00);

    /**
     * webPos 现金订单会员折扣
     */
    private BigDecimal baseMemberXjDiscount = new BigDecimal(0.00);

    /**
     * webPos 现金订单退还会员折扣
     */
    private BigDecimal returnMemberXjDiscount = new BigDecimal(0.00);

    /**
     * webPos 现金订单总付款额度
     */
    private BigDecimal orderXjPaymentAmount = new BigDecimal(0.00);

    /**
     * 订单商品价格
     */
    private BigDecimal orderBasePaymentAmount = new BigDecimal(0.00);

    /**
     * 订单会员折扣
     */
    private BigDecimal orderBaseMemberDiscount = new BigDecimal(0.00);

    /**
     * 订单佣金
     */
    private BigDecimal orderBaseCommissionFee = new BigDecimal(0.00);

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
     * 经销订单金额
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
     * 经销订单协议价金额
     */
    private BigDecimal sellAgreementAmount = new BigDecimal(0.00);

    /**
     * 经销退单协议价金额
     */
    private BigDecimal sellAgreementReturnAmount = new BigDecimal(0.00);
    /**
     * 经销退单金额
     */
    private BigDecimal returnSellOnAmount = new BigDecimal(0.00);
    /**
     * 订单列表
     */
    private List<String> orderIdList;
    /**
     * 退单列表
     */
    private List<String> returnOrderIdList;
}
