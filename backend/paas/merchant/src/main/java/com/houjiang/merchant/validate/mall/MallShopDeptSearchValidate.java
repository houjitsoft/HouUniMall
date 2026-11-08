package com.houjiang.merchant.validate.mall;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class MallShopDeptSearchValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "店铺id")
    private Integer shopId;

    @Schema(description = "部门名称")
    private String name;

    @Schema(description = "上级部门")
    private Integer pid;

    @Schema(description = "状态 1正常 0禁用")
    private Integer status;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "负责人姓名")
    private String contact;

    @Schema(description = "负责人电话")
    private String mobile;

}
