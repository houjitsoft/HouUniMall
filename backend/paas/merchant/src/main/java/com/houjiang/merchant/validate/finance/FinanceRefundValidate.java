package com.houjiang.merchant.validate.finance;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class FinanceRefundValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "充值订单id")
    private Integer recharge_id;


}
