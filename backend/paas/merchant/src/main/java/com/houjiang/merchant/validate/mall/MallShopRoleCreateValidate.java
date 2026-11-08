package com.houjiang.merchant.validate.mall;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class MallShopRoleCreateValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "店铺id")
    private Integer shopId;

    @NotNull(message = "name参数缺失")
    @Schema(description = "角色名")
    private String name;

    @NotNull(message = "sort参数缺失")
    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "描述")
    private String desc;

    @NotNull(message = "status参数缺失")
    @Schema(description = "状态")
    private Integer status;

}
