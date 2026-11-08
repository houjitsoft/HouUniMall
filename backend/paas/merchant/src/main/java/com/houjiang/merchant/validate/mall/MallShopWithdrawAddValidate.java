package com.houjiang.merchant.validate.mall;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Amos
 */
@Data
public class MallShopWithdrawAddValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "店铺id")
    private Integer shopId;

    @Schema(description = "银行ID")
    @NotNull(message = "请选择提现账号")
    private Integer bankId;

    @Schema(description = "提现金额")
    @NotNull(message = "请选择提现金额")
    private BigDecimal money;
}
