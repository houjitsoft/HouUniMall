package com.houjiang.permission.service;

import java.util.List;

/**
 * 用户角色关联服务接口
 * 
 * @author Amos
 * @since 2025-01-09
 */
public interface UserRoleService {
    
    /**
     * 根据用户ID获取角色ID列表
     * 
     * @param userId 用户ID
     * @return 角色ID列表
     */
    List<Integer> getRoleIdsByUserId(Long userId);
    
    /**
     * 根据角色ID获取用户ID列表
     * 
     * @param roleId 角色ID
     * @return 用户ID列表
     */
    List<Long> getUserIdsByRoleId(Integer roleId);
    
    /**
     * 根据角色ID获取用户数量
     * 
     * @param roleId 角色ID
     * @return 用户数量
     */
    Integer getUserCountByRoleId(Integer roleId);
    
    /**
     * 批量设置用户角色
     * 
     * @param userId 用户ID
     * @param roleIds 角色ID列表
     */
    void batchSetUserRoles(Long userId, List<Integer> roleIds);
    
    /**
     * 删除用户的所有角色关联
     * 
     * @param userId 用户ID
     */
    void deleteByUserId(Long userId);
    
    /**
     * 删除角色的所有用户关联
     * 
     * @param roleId 角色ID
     */
    void deleteByRoleId(Integer roleId);
}
