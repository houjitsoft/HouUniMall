package com.houjiang.merchant.validate.mall;

import com.houjiang.merchant.common.validator.annotation.IDMust;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 商家银行卡参数
 * @author Amos
 */
@Data
@Schema(description = "商家银行卡更新参数")
public class MallShopBankUpdateValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @IDMust(message = "id参数必传且需大于0")
    @Schema(description = "ID")
    private Integer id;
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
