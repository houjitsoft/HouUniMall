package com.houjiang.permission.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 角色菜单关系实体类
 * 
 * @author Amos
 * @since 2025-01-10
 */
@Data
@TableName("hou_permission_role_menu")
public class RoleMenu {
    
    /**
     * 角色ID
     */
    @TableField("role_id")
    private Integer roleId;
    
    /**
     * 菜单ID
     */
    @TableField("menu_id")
    private Integer menuId;
}
