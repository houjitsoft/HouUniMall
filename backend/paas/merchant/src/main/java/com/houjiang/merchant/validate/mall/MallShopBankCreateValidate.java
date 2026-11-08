package com.houjiang.merchant.validate.mall;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class MallShopBankCreateValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "店铺id")
    private Integer shopId;

    @NotNull(message = "name参数缺失")
    @Schema(description = "银行名称")
    private String name;

    @NotNull(message = "branch参数缺失")
    @Schema(description = "支行名称")
    private String branch;

    @NotNull(message = "isDefault参数缺失")
    @Schema(description = "是否默认")
    private Integer isDefault;

    @NotNull(message = "username参数缺失")
    @Schema(description = "开户名")
    private String username;

    @NotNull(message = "cardNumber参数缺失")
    @Schema(description = "卡号")
    private String cardNumber;

}
