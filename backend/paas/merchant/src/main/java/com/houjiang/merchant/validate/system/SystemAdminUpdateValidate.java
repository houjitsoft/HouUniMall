package com.houjiang.merchant.validate.system;

import com.houjiang.merchant.common.validator.annotation.IDMust;
import com.houjiang.merchant.common.validator.annotation.IntegerContains;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * @author Amos
 */
@Data
public class SystemAdminUpdateValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @IDMust(message = "id参数必传且需大于0")
    @Schema(description = "ID", required = true)
    private Integer id;

    @NotEmpty(message = "账号不能为空")
    @Length(min = 2, max = 20, message = "账号必须在2~20个字符内")
    @Schema(description = "登录账号", required = true)
    private String account;

    @NotEmpty(message = "昵称不能为空")
    @Length(min = 2, max = 30, message = "昵称必须在2~30个字符内")
    @Schema(description = "用户昵称", required = true)
    private String name;

    @Schema(description = "登录密码")
    private String password;

    @NotNull(message = "请选择是否禁用")
    @IntegerContains(values = {0, 1})
    @Schema(description = "是否禁用", required = true)
    private Integer disable;

    @NotNull(message = "请选择是否支持多端登录")
    @IntegerContains(values = {0, 1})
    @Schema(description = "支持多端", required = true)
    private Integer multipointLogin;

    @NotNull(message = "排序号不能为空")
    @DecimalMin(value = "0", message = "排序号值不能少于0")
    @Schema(description = "排序", required = true)
    private Integer sort = 0;

    @Length(max = 200, message = "头像不能超出200个字符")
    @Schema(description = "头像")
    private String avatar = "";

/** @NotNull(message = "请选择角色") */
    @Schema(description = "角色ID")
    private List<Integer> roleId;

    @Schema(description = "部门ID")
    private List<Integer> deptId;

    @Schema(description = "岗位ID")
    private List<Integer> jobsId;

    private Integer root;

}
