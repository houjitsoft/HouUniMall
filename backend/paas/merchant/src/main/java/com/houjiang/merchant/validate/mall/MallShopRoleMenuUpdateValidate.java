package com.houjiang.merchant.validate.mall;

import com.houjiang.merchant.common.validator.annotation.IDMust;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 商家角色权限参数
 * @author Amos
 */
@Data
@Schema(description = "商家角色权限更新参数")
public class MallShopRoleMenuUpdateValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @IDMust(message = "id参数必传且需大于0")
    @Schema(description = "ID")
    private Integer id;

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
