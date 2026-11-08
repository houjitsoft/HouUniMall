package com.houjiang.merchant.vo.mall;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
public class MallSettlementCountSettlementVo implements Serializable {

    @Schema(description = "商家入账金额")
    private BigDecimal allMoney;

    @Schema(description = "已结算营业额")
    private BigDecimal allOrderMoney;

    @Schema(description = "已结算订单数")
    private Long allOrderNum;
    @Schema(description = "已结算交易服务费")
    private BigDecimal allServiceMoney;

}
