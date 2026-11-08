package com.houjiang.permission.service;

import java.util.List;

/**
 * 权限服务接口
 * 
 * @author Amos
 * @since 2025-01-07
 */
public interface PermissionService {
    
    /**
     * 检查用户是否有指定权限
     * @param userId 用户ID
     * @param permission 权限标识
     * @return 是否有权限
     */
    boolean hasPermission(Long userId, String permission);
    
    /**
     * 获取用户所有权限
     * @param userId 用户ID
     * @return 权限列表
     */
    List<String> getUserPermissions(Long userId);
}
