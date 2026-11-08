package com.houjiang.merchant.vo.mall;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class MallSettlementCountVo implements Serializable {

    @Schema(description = "已结算")
    private MallSettlementCountSettlementVo settlement;

    @Schema(description = "已结算")
    private MallSettlementCountWaitVo wait;
}
