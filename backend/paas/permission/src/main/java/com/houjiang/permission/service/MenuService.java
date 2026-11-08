package com.houjiang.permission.service;

import com.houjiang.permission.controller.PermissionController.MenuCreateRequest;
import com.houjiang.permission.controller.PermissionController.MenuUpdateRequest;
import com.houjiang.permission.vo.MenuVO;

import java.util.List;
import java.util.Map;

/**
 * 菜单服务接口
 * 
 * @author Amos
 * @since 2025-01-09
 */
public interface MenuService {
    
    /**
     * 获取所有菜单（用于菜单管理）
     * @return 菜单列表
     */
    List<MenuVO> getAllMenus();

    /**
     * 获取菜单权限树（用于角色权限管理，只返回菜单级别）
     * @return 菜单权限树
     */
    List<MenuVO> getMenuTreeForRole();
    
    /**
     * 根据用户角色获取菜单
     * @param roleId 角色ID
     * @return 菜单列表
     */
    List<MenuVO> getMenusByRole(Integer roleId);
    
    /**
     * 构建菜单树
     * @param menus 菜单列表
     * @return 菜单树
     */
    List<MenuVO> buildMenuTree(List<MenuVO> menus);
    
    /**
     * 构建菜单树
     * @param menus 菜单列表
     * @return 菜单树
     */
    List<MenuVO> buildMenuAllTree(List<MenuVO> menus);

    /**
     * 获取菜单详情
     * @param id 菜单ID
     * @return 菜单详情
     */
    MenuVO getMenuDetail(Integer id);
    
    /**
     * 新增菜单
     * @param request 菜单创建请求
     */
    void addMenu(MenuCreateRequest request);
    
    /**
     * 编辑菜单
     * @param request 菜单更新请求
     */
    void editMenu(MenuUpdateRequest request);
    
    /**
     * 删除菜单
     * @param id 菜单ID
     */
    void deleteMenu(Integer id);
    
    /**
     * 根据用户ID获取用户导航菜单（不包含A级按钮权限）
     * 此接口为前端导航菜单服务，只返回M级（目录）和C级（菜单）
     * A级按钮权限应通过单独的权限检查接口管理，不在导航菜单中显示
     * @param userId 用户ID
     * @return 用户导航菜单列表（不包含A级按钮）
     */
    List<MenuVO> getUserMenus(Long userId);
    
    /**
     * 获取子服务的所有导航菜单（通过一级菜单ID）
     * 用于微前端子服务获取完整的导航菜单树，不包含A级按钮权限
     * A级按钮权限应通过单独的权限检查接口管理
     * @param userId 用户ID
     * @param parentMenuId 一级菜单ID
     * @return 子导航菜单树（不包含A级按钮）
     */
    List<MenuVO> getSubMenuTree(Long userId, Integer parentMenuId);
    
    /**
     * 清除所有用户的菜单缓存
     * 当菜单数据发生变化时调用（新增、编辑、删除菜单、分配菜单权限等）
     */
    void clearAllUserMenusCache();

    /**
     * 清除指定用户的菜单/权限 Redis 缓存（会员分配角色后需调用，否则侧栏仍为旧菜单）
     */
    void clearUserMenusCache(Long userId);

    /**
     * 更新菜单的显示位置（左侧竖栏 / 右上角插件分类 / 隐藏）。
     *
     * @param id         菜单ID
     * @param displayPos left | plugin | hidden，非法值回落为 left
     * @param cateId     插件分类ID（仅 displayPos=plugin 生效，其余置 0）
     */
    void updateMenuDisplay(Integer id, String displayPos, Integer cateId);

    /**
     * 插件中心分类列表（hou_addon_cate），供顶栏右上角按分类聚合插件菜单。
     *
     * @return [{id, name}]，id 升序
     */
    List<Map<String, Object>> getAddonCateList();
}
