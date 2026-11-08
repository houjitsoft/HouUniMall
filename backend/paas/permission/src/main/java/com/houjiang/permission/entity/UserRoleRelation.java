package com.houjiang.permission.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 用户角色关系实体类
 * 
 * @author Amos
 * @since 2025-01-10
 */
@Data
@TableName("hou_permission_user_role")
public class UserRoleRelation {
    
    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;
    
    /**
     * 角色ID
     */
    @TableField("role_id")
    private Integer roleId;
}
