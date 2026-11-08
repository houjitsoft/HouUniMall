package com.houjiang.merchant.vo.mall;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
public class MallSettlementListedVo implements Serializable {

    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "ID")
    private Integer settlementId;

    @Schema(description = "创建时间")
    private String createTime;

    @Schema(description = "修改时间")
    private String updateTime;

    @Schema(description = "店铺id")
    private Integer shopId;

    @Schema(description = "流水号")
    private String sn;

    @Schema(description = "营业额")
    private BigDecimal orderMoney;

    @Schema(description = "订单数")
    private Integer orderNum;

    @Schema(description = "退款订单数")
    private Integer orderRefundNum;

    @Schema(description = "退款金额")
    private BigDecimal orderRefundMoney;

    @Schema(description = "服务费比率")
    private BigDecimal serviceRatio;

    private BigDecimal distributionMoney;

    @Schema(description = "服务费")
    private BigDecimal serviceMoney;

    @Schema(description = "结算入账金额")
    private BigDecimal money;

}
