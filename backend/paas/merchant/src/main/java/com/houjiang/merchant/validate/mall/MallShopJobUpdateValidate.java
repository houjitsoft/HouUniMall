package com.houjiang.merchant.validate.mall;

import com.houjiang.merchant.common.validator.annotation.IDMust;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 商家岗位参数
 * @author Amos
 */
@Data
@Schema(description = "商家岗位更新参数")
public class MallShopJobUpdateValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @IDMust(message = "id参数必传且需大于0")
    @Schema(description = "ID")
    private Integer id;

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
