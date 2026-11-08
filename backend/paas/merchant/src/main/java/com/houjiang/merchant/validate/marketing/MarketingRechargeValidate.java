package com.houjiang.merchant.validate.marketing;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
public class MarketingRechargeValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "status参数缺失")
    @Schema(description = "是否开启充值: 0=否,1=是")
    private Integer status;

    @NotNull(message = "minRechargeMoney参数缺失")
    @Schema(description = "最低充值金额")
    private BigDecimal minAmount;

}
