package com.houjiang.merchant.vo.mall;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class MallShopMenuDetailVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "菜单名称")
    private String name;

    @Schema(description = "父级ID")
    private Integer pid;

    @Schema(description = "类型  0目录 1菜单 2按钮")
    private Integer type;

    @Schema(description = "图标")
    private String icon;

    @Schema(description = "路由地址")
    private String route;

    @Schema(description = "路由参数")
    private String routeParams;

    @Schema(description = "权限标识")
    private String access;

    @Schema(description = "组件")
    private String component;

    @Schema(description = "选中路径")
    private String selected;

    @Schema(description = "状态 1正常 0禁用")
    private Integer status;

    @Schema(description = "显示状态1 显示 0不显示")
    private Integer showStatus;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "vue 缓存")
    private Integer isCache;


}
