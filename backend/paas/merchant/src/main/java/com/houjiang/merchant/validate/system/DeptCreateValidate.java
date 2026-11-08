package com.houjiang.merchant.validate.system;

import com.houjiang.merchant.common.validator.annotation.IntegerContains;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class DeptCreateValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "pid参数缺失")
    @DecimalMin(value = "0", message = "上级值不能少于0")
    @Schema(description = "上级ID", required = true)
    private Integer pid;

    @NotNull(message = "name参数缺失")
    @Length(min = 1, max = 100, message = "部门名称必须在1~100个字符内")
    @Schema(description = "部门名称", required = true)
    private String name;

    @Length(min = 1, max = 30, message = "负责人名称必须在1~30个字符内")
    private String leader = "";

    @Length(min = 11, max = 11, message = "手机号只能为11位")
    @Pattern(regexp = "^[1][3,4,5,6,7,8,9][0-9]{9}$", message = "手机号格式有误")
    @Schema(description = "手机号码")
    private String mobile;

    @NotNull(message = "请选择状态")
    @IntegerContains(values = {0, 1})
    @Schema(description = "状态", required = true)
    private Integer status;

    @NotNull(message = "排序号不能为空")
    @DecimalMin(value = "0", message = "排序号值不能少于0")
    @DecimalMax(value = "9999", message = "排序号值不能大于9999")
    @Schema(description = "排序")
    private Integer sort;

}
