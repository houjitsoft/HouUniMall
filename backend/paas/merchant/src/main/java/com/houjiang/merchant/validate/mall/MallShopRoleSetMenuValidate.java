package com.houjiang.merchant.validate.mall;

import com.houjiang.merchant.common.validator.annotation.IDMust;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 商家角色权限参数
 * @author Amos
 */
@Data
@Schema(description = "商家角色修改菜单参数")
public class MallShopRoleSetMenuValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @IDMust(message = "id参数必传且需大于0")
    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "店铺ID")
    private Integer shopId;

    @NotNull(message = "请选择菜单")
    @Schema(description = "菜单权限")
    private List<Integer> menu;

}
