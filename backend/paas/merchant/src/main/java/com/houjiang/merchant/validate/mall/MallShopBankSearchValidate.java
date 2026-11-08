package com.houjiang.merchant.validate.mall;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class MallShopBankSearchValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "店铺id")
    private Integer shopId;

    @Schema(description = "银行名称")
    private String name;

    @Schema(description = "支行名称")
    private String branch;

    @Schema(description = "是否默认")
    private Integer isDefault;

    @Schema(description = "开户名")
    private String username;

    @Schema(description = "卡号")
    private String cardNumber;

}
