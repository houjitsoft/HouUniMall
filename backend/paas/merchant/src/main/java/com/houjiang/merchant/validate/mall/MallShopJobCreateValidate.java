package com.houjiang.merchant.validate.mall;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class MallShopJobCreateValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "店铺id")
    private Integer shopId;

    @NotNull(message = "code参数缺失")
    @Schema(description = "岗位编码")
    private String code;

    @NotNull(message = "name参数缺失")
    @Schema(description = "岗位名称")
    private String name;

    @NotNull(message = "sort参数缺失")
    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "备注")
    private String remark;

    @NotNull(message = "status参数缺失")
    @Schema(description = "状态 1正常 0 禁用")
    private Integer status;

}
