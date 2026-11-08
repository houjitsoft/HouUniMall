package com.houjiang.mall.entity.auth;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;

/**
 * Mall 系统菜单实体类
 * 对应表: hou_mall_system_menu
 *
 * @author Amos
 */
@Data
@TableName("hou_mall_system_menu")
public class MallSystemMenu implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 菜单ID */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 父菜单ID (0=顶级) */
    private Integer pid;

    /** 菜单类型: M=目录, C=菜单, A=按钮（对齐 like-admin ls_system_menu） */
    private String type;

    /** 菜单名称 */
    private String name;

    /** 菜单图标 */
    private String icon;

    /** 排序 */
    private Integer sort;

    /** 权限标识 */
    private String perms;

    /** 路由路径 */
    private String paths;

    /** 组件路径 */
    private String component;

    /** 选中路径 */
    private String selected;

    /** 路由参数 */
    private String params;

    /** 是否缓存: 0=否, 1=是 */
    private Integer isCache;

    /** 是否显示: 0=显示, 1=隐藏 */
    private Integer isShow;

    /** 是否禁用: 0=正常, 1=禁用 */
    private Integer isDisable;

    /** 创建时间（时间戳） */
    private Integer createTime;

    /** 更新时间（时间戳） */
    private Integer updateTime;
}
