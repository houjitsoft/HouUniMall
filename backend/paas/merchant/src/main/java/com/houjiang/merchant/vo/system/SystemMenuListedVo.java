package com.houjiang.merchant.vo.system;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class SystemMenuListedVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Integer id;

    @Schema(description = "上级菜单")
    private Integer pid;

    @Schema(description = "权限类型: [M=目录, C=菜单, A=按钮]")
    @JsonProperty("menuType")
    private String type;

    @Schema(description = "菜单名称")
    @JsonProperty("menuName")
    private String name;

    @Schema(description = "菜单图标")
    @JsonProperty("menuIcon")
    private String icon;

    @Schema(description = "菜单排序")
    @JsonProperty("menuSort")
    private Integer sort;

    @Schema(description = "权限标识")
    private String perms;

    @Schema(description = "路由地址")
    private String paths;

    @Schema(description = "前端组件")
    private String component;

    @Schema(description = "选中路径")
    private String selected;

    @Schema(description = "路由参数")
    private String params;

    @Schema(description = "是否缓存: [0=否, 1=是]")
    private Integer isCache;

    @Schema(description = "是否显示: [0=否, 1=是]")
    private Integer isShow;

    @Schema(description = "是否禁用: [0=否, 1=是]")
    private Integer isDisable;

    @Schema(description = "创建时间")
    private String createTime;

    @Schema(description = "更新时间")
    private String updateTime;

}
