package com.houjiang.order.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 订单结算表
 * @TableName order_settlement
 * @author Amos
 */
@TableName(value ="hou_order_settlement")
@Data
public class OrderSettlementEntity implements Serializable {
    /**
     * 结算单编号(年月店铺ID)
     */
    @TableId
    private String osId;

    /**
     * 开始日期
     */
    private Date osStartDate;

    /**
     * 结束日期
     */
    private Date osEndDate;

    /**
     * 订单金额
     */
    private BigDecimal osOrderAmount;

    /**
     * 运费
     */
    private BigDecimal osShippingAmount;

    /**
     * 退单金额
     */
    private BigDecimal osOrderReturnAmount;

    /**
     * 佣金金额
     */
    private BigDecimal osCommisAmount;

    /**
     * 退还佣金
     */
    private BigDecimal osCommisReturnAmount;

    /**
     * 店铺促销活动费用
     */
    private BigDecimal osShopCostAmount;

    /**
     * 平台承担的店铺折扣金额
     */
    private BigDecimal osPlatformBearAmount;

    /**
     * 退还的平台承担的折扣金额
     */
    private BigDecimal osPlatformBearReturnAmount;

    /**
     * 经销商佣金
     */
    private BigDecimal osDealerCommission;

    /**
     * 经销商退还佣金
     */
    private BigDecimal osDealerReturnCommission;

    /**
     * 供应商佣金
     */
    private BigDecimal osSupplyCommission;

    /**
     * 供应商退还佣金
     */
    private BigDecimal osSupplyReturnCommission;

    /**
     * 应结金额
     */
    private BigDecimal osAmount;

    /**
     * 生成结算单日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date osDatetime;

    /**
     * 结算单年月份
     */
    private Date osDate;

    /**
     * 1默认(已出账)2店家已确认3平台已审核4结算完成 5已提现
     */
    private Byte osState;

    /**
     * 付款日期
     */
    private Date osPayDate;

    /**
     * 支付备注
     */
    private String osPayContent;

    /**
     * 店铺ID
     */
    private Integer shopId;

    /**
     * 店铺名
     */
    private String shopName;

    /**
     * 结算订单类型 1-普通订单 2-商家一键上架供应商销售订单商家结算单  3商家一键上架供应商销售订单供应商结算单 4直播分佣结算
     */
    private Integer osOrderType;

    /**
     * 红包金额
     */
    private BigDecimal osRedpacketAmount;

    /**
     * 退还红包
     */
    private BigDecimal osRedpacketReturnAmount;

    /**
     * 地区id,0表示全国
     */
    private Integer districtId;

    /**
     * 结算单是否显示 0不显示 1显示
     */
    private Boolean osIsDisplay;

    /**
     * 会员折扣
     */
    private BigDecimal osBaseMemberDiscount;

    /**
     * 退还会员折扣
     */
    private BigDecimal osReturnMemberDiscount;

    /**
     * 推广佣金
     */
    private BigDecimal osTgCommission;

    /**
     * 分享佣金
     */
    private BigDecimal osFxCommission;

    /**
     * 供应商经销等级 折扣
     */
    private BigDecimal osSupperDiscountFee;

    /**
     * 供应商退还经销等级折扣
     */
    private BigDecimal osReturnSupperDiscountFee;

    /**
     * 供货价
     */
    private BigDecimal osSupplyPrice;

    /**
     * 退还供货价
     */
    private BigDecimal osReturnSupplyPrice;
    /**
     * 退还推广佣金
     */
    private String osReturnTgCommission = "0.00";
    /**
     * 退还分享佣金
     */
    private String osReturnFxCommission = "0.00";

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}