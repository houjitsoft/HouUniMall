package com.houjiang.permission.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户角色关联实体
 * 
 * @author Amos
 * @since 2025-01-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("hou_permission_user_role")
public class UserRole {
    
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