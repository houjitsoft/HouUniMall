package com.houjiang.merchant.validate.mall;

import com.houjiang.merchant.common.validator.annotation.IDMust;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 商家角色参数
 * @author Amos
 */
@Data
@Schema(description = "商家角色更新参数")
public class MallShopRoleUpdateValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @IDMust(message = "id参数必传且需大于0")
    @Schema(description = "ID")
    private Integer id;

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
