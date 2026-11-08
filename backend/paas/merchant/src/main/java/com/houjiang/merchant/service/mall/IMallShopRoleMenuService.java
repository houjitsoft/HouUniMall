package com.houjiang.merchant.service.mall;

import java.util.List;

/**
 * 商家角色权限服务接口类
 * @author Amos
 */
public interface IMallShopRoleMenuService {

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
    void batchSaveByMenuIds(Integer roleId, List<Integer> menuIds, Integer shopId);

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
