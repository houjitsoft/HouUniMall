package com.houjiang.order.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @ClassName OrderSettlementVO
 * @Description
 * @Author Amos
 * @Date 2023/7/17 14:09
 * @Version 1.0
 * @author Amos
 */
@Data
public class OrderSettlementVO {

    /**
     * 订单ID
     */
    private String osId;

    /**
     * 订单开始日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date osStartDate;

    /**
     * 订单结束日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date osEndDate;

    /**
     * 订单金额
     */
    private String osOrderAmount;

    /**
     * 运费金额
     */
    private String osShippingAmount;

    /**
     * 订单退款金额
     */
    private String osOrderReturnAmount;

    /**
     * 佣金金额
     */
    private String osCommisAmount;

    /**
     * 佣金退款金额
     */
    private String osCommisReturnAmount;

    /**
     * 店铺成本金额
     */
    private String osShopCostAmount;

    /**
     * 平台支付金额
     */
    private String osPlatformBearAmount;

    /**
     * 平台支付退款金额
     */
    private String osPlatformBearReturnAmount;

    /**
     * 总金额
     */
    private String osAmount;

    /**
     * 订单日期时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date osDatetime;

    /**
     * 订单日期
     */
    private String osDate;

    /**
     * 订单状态
     */
    private String osState;

    /**
     * 支付日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date osPayDate;

    /**
     * 支付内容
     */
    private String osPayContent;

    /**
     * 店铺ID
     */
    private String shopId;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 订单类型
     */
    private String osOrderType;

    /**
     * 红包金额
     */
    private String osRedpacketAmount;

    /**
     * 红包退款金额
     */
    private String osRedpacketReturnAmount;

    /**
     * 区域ID
     */
    private String districtId;

    /**
     * 直销商金额
     */
    private String osDirectsellerAmount;

    /**
     * 订单优惠金额
     */
    private String osOrderRanderectAmount;

    /**
     * 订单退款优惠金额
     */
    private String osOrderReturnRanderectAmount;

    /**
     * 订单砍价金额
     */
    private String osOrderBargainAmount;

    /**
     * 订单金额
     */
    private String osMoney;

    /**
     * 罚款金额
     */
    private String osFineAmount = "0.00";

    /**
     * 经销商佣金
     */
    private String osDealerCommission;

    /**
     * 经销商退款佣金
     */
    private String osDealerReturnCommission;

    /**
     * 供应商佣金
     */
    private String osSupplyCommission;

    /**
     * 供应商退款佣金
     */
    private String osSupplyReturnCommission;

    /**
     * 基础会员折扣
     */
    private String osBaseMemberDiscount = "0.00";

    /**
     * 退款会员折扣
     */
    private String osReturnMemberDiscount = "0.00";

    /**
     * 推广佣金
     */
    private String osTgCommission = "0.00";

    /**
     * 分销佣金
     */
    private String osFxCommission = "0.00";

    /**
     * 超级折扣费用
     */
    private String osSupperDiscountFee = "0.00";

    /**
     * 退款超级折扣费用
     */
    private String osReturnSupperDiscountFee = "0.00";

    /**
     * 供应商价格
     */
    private String osSupplyPrice = "0.00";

    /**
     * 退款供应商价格
     */
    private String osReturnSupplyPrice = "0.00";
    /**
     * 退还推广佣金
     */
    private String osReturnTgCommission = "0.00";
    /**
     * 退还分享佣金
     */
    private String osReturnFxCommission = "0.00";

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
    private Double orderGoodsPaymentAmount;

    /**
     * 自销商品佣金
     */
    private Double selfSellingCommission = 0.00;

    /**
     * 自销商品运费
     */
    private Double selfSellingShippingFee = 0.00;

    /**
     * 自销退单金额
     */
    private Double selfSellingReturnCash = 0.00;

    /**
     * 自销退还佣金
     */
    private Double selfSellingReturnCommission = 0.00;

    /**
     * 经销订单金额
     */
    private Double sellOnAmount = 0.00;

    /**
     * 经销商品佣金
     */
    private Double sellOnCommission = 0.00;

    /**
     * 经销退单金额
     */
    private Double sellOnReturnCash = 0.00;

    /**
     * 经销退还佣金
     */
    private Double sellOnReturnCommission = 0.00;

}
