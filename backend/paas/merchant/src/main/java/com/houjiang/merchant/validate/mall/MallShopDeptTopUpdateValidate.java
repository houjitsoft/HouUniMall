package com.houjiang.merchant.validate.mall;

import com.houjiang.merchant.common.validator.annotation.IDMust;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 顶级部门编辑参数（pid 固定为 0）
 */
@Data
@Schema(description = "顶级部门更新参数")
public class MallShopDeptTopUpdateValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @IDMust(message = "id参数必传且需大于0")
    @Schema(description = "ID")
    private Integer id;

    @NotNull(message = "name参数缺失")
    @Schema(description = "部门名称")
    private String name;

    @NotNull(message = "status参数缺失")
    @Schema(description = "状态 1正常 0禁用")
    private Integer status;

    @NotNull(message = "sort参数缺失")
    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "负责人姓名")
    private String contact;

    @Schema(description = "负责人电话")
    private String mobile;
}
