package com.houjiang.merchant.validate.mall;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class MallSettlementRecordSearchValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "店铺id")
    private Integer shopId;

    @Schema(description = "来源单号")
    private String from_sn;

    @Schema(description = "类型 1订单收入 2订单售后退款")
    private Integer type;
    @Schema(description = "结算id")
    private Integer settlementId;
}
