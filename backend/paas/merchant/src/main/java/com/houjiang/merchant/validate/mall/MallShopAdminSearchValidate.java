package com.houjiang.merchant.validate.mall;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class MallShopAdminSearchValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "店铺id")
    private Integer shopId;

    @Schema(description = "管理员名称")
    private String name;

    @Schema(description = "状态 1正常 0停用")
    private Integer status;
    @Schema(description = "登录账号")
    private String account;

    @Schema(description = "管理员角色")
    private Integer role_id;

}
