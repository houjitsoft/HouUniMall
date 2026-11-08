package com.houjiang.merchant.validate.mall;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class MallShopRoleMenuCreateValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "shopId参数缺失")
    @Schema(description = "店铺id")
    private Integer shopId;

    @NotNull(message = "roleId参数缺失")
    @Schema(description = "角色id")
    private Integer roleId;

    @NotNull(message = "menuId参数缺失")
    @Schema(description = "菜单id")
    private Integer menuId;

}
