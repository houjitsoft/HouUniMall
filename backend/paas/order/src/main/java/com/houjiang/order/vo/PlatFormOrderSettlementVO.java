package com.houjiang.order.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 平台订单结算
 * @author Amos
 */
@Data
public class PlatFormOrderSettlementVO {

    /**
     * 结算单编号(年月店铺ID)
     */
    private String osId;

    /**
     * 开始日期
     */
    private String osStartDate;

    /**
     * 结束日期
     */
    private String osEndDate;

    /**
     * 订单金额
     */
    private String osOrderAmount;

    /**
     * 运费
     */
    private String osShippingAmount;

    /**
     * 退单金额
     */
    private String osOrderReturnAmount;

    /**
     * 佣金金额
     */
    private String osCommisAmount;

    /**
     * 退款佣金金额
     */
    private String osCommisReturnAmount;

    /**
     * 店铺促销活动费用
     */
    private String osShopCostAmount;

    /**
     * 平台承担的店铺折扣金额
     */
    private String osPlatformBearAmount;

    /**
     * 退还的平台承担的折扣金额
     */
    private String osPlatformBearReturnAmount;

    /**
     * 应结金额
     */
    private String osAmount;

    /**
     * 生成结算单日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date osDatetime;

    /**
     * 结算单年月份
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date osDate;

    /**
     * 付款状态
     */
    private String osState;

    /**
     * 付款日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date osPayDate;

    /**
     * 支付备注
     */
    private String osPayContent;

    /**
     * 店铺id
     */
    private String shopId;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 结算订单类型
     * 1-普通订单
     * 2-商家一键上架供应商销售订单商家结算单
     * 3-商家一键上架供应商销售订单供应商结算单
     * 4-直播分佣结算
     */
    private String osOrderType;

    /**
     * 红包金额
     */
    private String osRedpacketAmount;

    /**
     * 退还红包
     */
    private String osRedpacketReturnAmount;

    /**
     * 分区ID
     */
    private String districtId;

    /**
     * 分销佣金总额
     */
    private String osDirectsellerAmount;

    /**
     * 新人随机立减
     */
    private String osOrderRanderectAmount;

    /**
     * 新人随机立减退款
     */
    private String osOrderReturnRanderectAmount;

    /**
     * 砍价金额
     */
    private String osOrderBargainAmount;

    /**
     * 金额
     */
    private String osMoney;

    /**
     * 罚款金额
     */
    private String osFineAmount;

    /**
     * 经销商佣金
     */
    private String osDealerCommission;

    /**
     * 经销商退还佣金
     */
    private String osDealerReturnCommission;

    /**
     * 供应商佣金
     */
    private String osSupplyCommission;

    /**
     * 供应商退还佣金
     */
    private String osSupplyReturnCommission;

    /**
     * 会员折扣
     */
    private String osBaseMemberDiscount = "0.00";

    /**
     * 退还会员折扣
     */
    private String osReturnMemberDiscount = "0.00";

    /**
     * 推广佣金
     */
    private String osTgCommission = "0.00";

    /**
     * 分享佣金
     */
    private String osFxCommission = "0.00";

    /**
     * 供应商经销等级 折扣
     */
    private String osSupperDiscountFee = "0.00";

    /**
     * 供应商退还经销等级折扣
     */
    private String osReturnSupperDiscountFee = "0.00";

    /**
     * 供货价
     */
    private String osSupplyPrice = "0.00";

    /**
     * 退还供货价
     */
    private String osReturnSupplyPrice = "0.00";

    /**
     * 店铺结算周期
     */
    private Integer shopSettlementCycle;

}
