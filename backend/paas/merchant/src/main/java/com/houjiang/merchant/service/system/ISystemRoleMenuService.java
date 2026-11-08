package com.houjiang.merchant.service.system;


import java.util.List;

/**
 * 角色菜单关系接口类
 */
public interface ISystemRoleMenuService {


    /**
     * 根据角色ID获取菜单ID
     *
     * @param roleIds 角色ID
     * @return List<Integer>
     */
    List<Integer> selectMenuIdsByRoleId(List<Integer> roleIds);

    /**
     * 批量写入角色菜单
     *
     * @author Amos
     * @param roleId 角色ID
     * @param menuIds 菜单ID组
     */
    void batchSaveByMenuIds(Integer roleId, List<Integer> menuIds);

    /**
     * 根据角色ID批量删除角色菜单
     *
     * @author Amos
     * @param roleId 角色ID
     */
    void batchDeleteByRoleId(Integer roleId);

    /**
     * 根据菜单ID批量删除角色菜单
     *
     * @author Amos
     * @param menuId 菜单ID
     */
    void batchDeleteByMenuId(Integer menuId);
}
