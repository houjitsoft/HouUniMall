package com.houjiang.merchant.vo.mall;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
public class MallSettlementRecordListedVo implements Serializable {
    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "创建时间")
    private String createTime;

    @Schema(description = "修改时间")
    private String updateTime;

    @Schema(description = "店铺id")
    private Integer shopId;

    @Schema(description = "结算id")
    private Integer settlementId;

    @Schema(description = "流水号")
    private String sn;

    @Schema(description = "来源单号")
    private String fromSn;

    @Schema(description = "类型 1订单收入 2订单售后退款")
    private Integer type;

    @Schema(description = "类型 1订单收入 2订单售后退款")
    private String typeText;

    @Schema(description = "订单金额")
    private BigDecimal orderMoney;

    @Schema(description = "订单退款金额")
    private BigDecimal orderRefundMoney;

    @Schema(description = "服务费")
    private BigDecimal serviceMoney;

    @Schema(description = "服务费率")
    private BigDecimal serviceRatio;

    @Schema(description = "结算入账金额")
    private BigDecimal money;

    @Schema(description = "分销金额")
    private BigDecimal distributionMoney;
}
