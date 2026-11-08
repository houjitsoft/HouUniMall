package com.houjiang.merchant.vo.mall;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
public class MallSettlementCountWaitVo implements Serializable {

    @Schema(description = "待结算营业额")
    private BigDecimal allOrderMoney;

    @Schema(description = "待结算订单数")
    private Long allOrderNum;

}
