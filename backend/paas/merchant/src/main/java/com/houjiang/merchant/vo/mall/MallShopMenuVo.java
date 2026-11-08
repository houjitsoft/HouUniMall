package com.houjiang.merchant.vo.mall;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class MallShopMenuVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    private Integer id;

    @Schema(description = "上级菜单")
    private Integer pid;

    @Schema(description = "权限类型: [M=目录, C=菜单, A=按钮]")
    private Integer type;

    @Schema(description = "菜单名称")
    private String name;

    @Schema(description = "菜单图标")
    private String icon;

    @Schema(description = "菜单排序")
    private Integer sort;

    @Schema(description = "路由地址")
    private String route;

    @Schema(description = "路由参数")
    private String routeParams;

    @Schema(description = "权限标识")
    private String access;

    @Schema(description = "前端组件")
    private String component;

    @Schema(description = "选中路径")
    private String selected;

    @Schema(description = "状态 1正常 0禁用")
    @JsonProperty("status")
    private Integer status;

    @Schema(description = "显示状态1 显示 0不显示")
    @JsonProperty("show_status")
    private Integer showStatus;

    @Schema(description = "vue 缓存")
    @JsonProperty("is_cache")
    private Integer isCache;

    @Schema(description = "创建时间")
    @JsonProperty("create_time")
    private String createTime;

    @Schema(description = "更新时间")
    @JsonProperty("update_time")
    private String updateTime;

}
