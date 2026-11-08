package com.houjiang.merchant.validate.mall;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class MallShopRoleSearchValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "店铺id")
    private Integer shopId;

    @Schema(description = "角色名")
    private String name;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "描述")
    private String desc;

    @Schema(description = "状态")
    private Integer status;

}
