package com.houjiang.merchant.validate.system;

import com.houjiang.merchant.common.validator.annotation.IDMust;
import com.houjiang.merchant.common.validator.annotation.IntegerContains;
import com.houjiang.merchant.common.validator.annotation.StringContains;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class SystemMenuUpdateValidate implements Serializable {

    private static final long serialVersionUID = 1L;

    @IDMust(message = "id参数必传且需大于0")
    @Schema(description = "id", required = true)
    private Integer id;

    @NotNull(message = "上级菜单不能为空")
    @DecimalMin(value = "0", message = "上级菜单值不能少于0")
    @Schema(description = "上级ID", required = true)
    private Integer pid;

    @NotNull(message = "menuType参数缺失")
    @StringContains(values = {"M", "C", "A"}, message = "菜单类型不是合法值(M,C,A)")
    @Schema(description = "菜单类型", required = true)
    private String type;

    @NotNull(message = "缺少参数menuName")
    @Length(min = 1, max = 30, message = "菜单名称必须在1~30个字符内")
    @Schema(description = "菜单名称", required = true)
    private String name;

    @Length(max = 100, message = "图标名称不能超过100个字符")
    @Schema(description = "菜单图标")
    private String icon;

    @NotNull(message = "排序号不能为空")
    @DecimalMin(value = "0", message = "排序号值不能少于0")
    @Schema(description = "菜单排序")
    private Integer sort;

    @Length(max = 100, message = "权限字符不能超过100个字符")
    @Schema(description = "权限字符")
    private String perms;

    @Length(max = 200, message = "路由地址不能超过200个字符")
    @Schema(description = "路由地址")
    private String paths;

    @Length(max = 200, message = "前端组件不能超过200个字符")
    @Schema(description = "前端组件")
    private String component;

    @Length(max = 200, message = "选中菜单路径不能超过200个字符")
    @Schema(description = "菜单路径")
    private String selected;

    @Length(max = 200, message = "路由参数不能超过200个字符")
    @Schema(description = "路由参数")
    private String params;

    @NotNull(message = "请选择缓存状态")
    @IntegerContains(values = {0, 1})
    @Schema(description = "缓存状态", required = true)
    private Integer isCache;

    @NotNull(message = "请选择显示状态")
    @IntegerContains(values = {0, 1})
    @Schema(description = "显示状态", required = true)
    private Integer isShow;

    @NotNull(message = "请选择菜单状态")
    @IntegerContains(values = {0, 1})
    @Schema(description = "菜单状态", required = true)
    private Integer isDisable;

}
