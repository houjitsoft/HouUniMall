package com.houjiang.permission.service;

import java.util.List;

/**
 * 角色菜单服务接口
 * 
 * @author Amos
 * @since 2025-01-10
 */
public interface RoleMenuService {
    
    /**
     * 根据角色ID获取菜单ID列表
     * 
     * @param roleId 角色ID
     * @return 菜单ID列表
     */
    List<Integer> getMenuIdsByRoleId(Integer roleId);
    
    /**
     * 根据角色ID列表获取菜单ID列表
     * 
     * @param roleIds 角色ID列表
     * @return 菜单ID列表
     */
    List<Integer> getMenuIdsByRoleIds(List<Integer> roleIds);
    
    /**
     * 批量保存角色菜单关系
     * 
     * @param roleId 角色ID
     * @param menuIds 菜单ID列表
     */
    void batchSaveByMenuIds(Integer roleId, List<Integer> menuIds);

    /**
     * 只补充角色尚未拥有的菜单，不删除或替换已有授权。
     *
     * @param roleId 角色ID
     * @param menuIds 待补菜单ID列表
     */
    void addMissingMenuIds(Integer roleId, List<Integer> menuIds);
    
    /**
     * 根据角色ID批量删除角色菜单关系
     * 
     * @param roleId 角色ID
     */
    void batchDeleteByRoleId(Integer roleId);
    
    /**
     * 根据菜单ID批量删除角色菜单关系
     * 
     * @param menuId 菜单ID
     */
    void batchDeleteByMenuId(Integer menuId);
}
