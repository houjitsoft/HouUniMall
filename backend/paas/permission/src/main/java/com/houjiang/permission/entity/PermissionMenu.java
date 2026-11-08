package com.houjiang.permission.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 权限菜单实体类
 * 
 * @author Amos
 * @since 2025-01-09
 */
@Data
@TableName("hou_permission_menu")
public class PermissionMenu {
    
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    /**
     * 上级菜单
     */
    @TableField("pid")
    private Integer pid;
    
    /**
     * 权限类型: M=目录，C=菜单，A=按钮
     */
    private String type;
    
    /**
     * 菜单名称
     */
    private String name;
    
    /**
     * 菜单图标
     */
    private String icon;
    
    /**
     * 菜单排序
     */
    private Integer sort;
    
    /**
     * 权限标识
     */
    private String perms;
    
    /**
     * 路由地址
     */
    private String paths;
    
    /**
     * 前端组件
     */
    private String component;
    
    /**
     * 选中路径
     */
    private String selected;
    
    /**
     * 路由参数
     */
    private String params;
    
    /**
     * 是否缓存: 0=否, 1=是
     */
    @TableField("is_cache")
    private Integer isCache;
    
    /**
     * 是否显示: 0=否, 1=是
     */
    @TableField("is_show")
    private Integer isShow;
    
    /**
     * 是否禁用: 0=否, 1=是
     */
    @TableField("is_disable")
    private Integer isDisable;
    
    /**
     * 一级菜单显示位置: left=左侧竖栏, plugin=右上角插件分类, hidden=不展示
     */
    @TableField("display_pos")
    private String displayPos;

    /** 菜单所属插件；插件分类由 base 维护。 */
    @TableField("plugin_code")
    private String pluginCode;

    /**
     * 插件分类 ID（display_pos=plugin 时生效，对应 hou_addon_cate.id，0=未分类）
     */
    @TableField("cate_id")
    private Integer cateId;

    /**
     * 创建时间
     */
    private Integer createTime;
    
    /**
     * 更新时间
     */
    private Integer updateTime;
}
