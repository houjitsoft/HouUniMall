package com.houjiang.merchant.common.entity.mall;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Amos
 */
@Data
public class MallShopMenu implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value="id", type= IdType.AUTO)
    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "上级菜单")
    private Integer pid;

    @Schema(description = "类型  0目录 1菜单 2按钮")
    private Integer type;

    @Schema(description = "菜单名称")
    private String name;

    @Schema(description = "菜单图标")
    private String icon;

    @Schema(description = "路由地址")
    private String route;

    @Schema(description = "菜单排序")
    private Integer sort;
    @Schema(description = "前端组件")
    private String component;

    @Schema(description = "选中路径")
    private String selected;

    @Schema(description = "路由参数")
    private String routeParams;

    @Schema(description = "权限标识")
    private String access;


    @Schema(description = "是否缓存: [0=否, 1=是]")
    private Integer isCache;

    @Schema(description = "状态 1正常 0禁用")
    private Integer status;
    @Schema(description = "显示状态1 显示 0不显示")
    private Integer showStatus;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "更新时间")
    private Long updateTime;
    @Schema(description = "删除时间")
    private Long deleteTime;

}
