package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 权限服务Feign客户端
/**
 * 
 * @author Amos
 * @since 2025-01-07
 */
@FeignClient(
        name = "permission",
        url = "${feign.permission.url:http://127.0.0.1:8300}",
        fallback = PermissionServiceFeignFallback.class
)
@Component
public interface PermissionServiceFeign {
    
    /**
     * 获取用户权限菜单
     */
    @PostMapping("/permission/user/menus")
    ResponseResult<List<Map<String, Object>>> getUserMenus(@RequestBody Map<String, Object> request);
    
    /**
     * 获取子菜单树
     */
    @PostMapping("/permission/submenu/tree")
    ResponseResult<List<Map<String, Object>>> getSubMenuTree(@RequestBody Map<String, Object> request);
    
    /**
     * 检查用户权限
     */
    @PostMapping("/permission/user/check")
    ResponseResult<Boolean> checkUserPermission(@RequestBody Map<String, Object> request);
    
    /**
     * 获取用户权限列表
     */
    @PostMapping("/permission/user/permissions")
    ResponseResult<List<String>> getUserPermissions(@RequestBody Map<String, Object> request);

    /**
     * 检查当前用户是否可以访问页面
     */
    @PostMapping("/permission/page/access/check")
    ResponseResult<Boolean> checkPageAccess(@RequestBody Map<String, Object> request);

    /**
     * 获取当前用户的页面按钮权限
     */
    @PostMapping("/permission/page/buttons")
    ResponseResult<List<String>> getPageButtonPermissions(@RequestBody Map<String, Object> request);

    /**
     * 批量检查当前用户的按钮权限
     */
    @PostMapping("/permission/page/buttons/batch-check")
    ResponseResult<Map<String, Boolean>> batchCheckButtonPermissions(@RequestBody Map<String, Object> request);
    
    /**
     * 获取所有菜单（用于菜单管理）
     */
    @PostMapping("/permission/menu/list")
    ResponseResult getAllMenus();

    /**
     * 获取菜单权限树（用于角色权限管理）
     */
    @PostMapping("/permission/menu/tree")
    ResponseResult getMenuTreeForRole();
    
    /**
     * 根据角色获取菜单
     */
    @GetMapping("/permission/menu/role/{roleId}")
    ResponseResult getMenusByRole(@PathVariable("roleId") Integer roleId);
    
    /**
     * 新增菜单
     */
    @PostMapping("/permission/menu/add")
    ResponseResult addMenu(@RequestBody Map<String, Object> request);
    
    /**
     * 编辑菜单
     */
    @PostMapping("/permission/menu/edit")
    ResponseResult editMenu(@RequestBody Map<String, Object> request);
    
    /**
     * 删除菜单
     */
    @PostMapping("/permission/menu/delete")
    ResponseResult deleteMenu(@RequestBody Map<String, Object> request);
    
    /**
     * 获取菜单详情
     */
    @GetMapping("/permission/menu/detail/{id}")
    ResponseResult getMenuDetail(@PathVariable("id") Integer id);

    /**
     * 更新菜单显示位置（左侧竖栏 / 右上角插件分类 / 隐藏）
     */
    @PostMapping("/permission/menu/display/update")
    ResponseResult updateMenuDisplay(@RequestBody Map<String, Object> request);

    /**
     * 获取插件中心分类列表（顶栏右上角按分类聚合插件菜单）
     */
    @GetMapping("/permission/addon/cate/list")
    ResponseResult getAddonCateList();
    
    // ==================== 角色管理接口 ====================
    
    /**
     * 获取所有角色
     */
    @GetMapping("/permission/role/all")
    ResponseResult getAllRoles();
    
    /**
     * 分页获取角色列表
     */
    @PostMapping("/permission/role/list")
    ResponseResult getRoleList(@RequestBody Map<String, Object> request);
    
    /**
     * 获取角色详情
     */
    @GetMapping("/permission/role/detail/{id}")
    ResponseResult getRoleDetail(@PathVariable("id") Integer id);
    
    /**
     * 创建角色
     */
    @PostMapping("/permission/role/add")
    ResponseResult createRole(@RequestBody Map<String, Object> request);
    
    /**
     * 更新角色
     */
    @PostMapping("/permission/role/edit")
    ResponseResult updateRole(@RequestBody Map<String, Object> request);
    
    /**
     * 删除角色
     */
    @PostMapping("/permission/role/delete")
    ResponseResult deleteRole(@RequestBody Map<String, Object> request);
    
    /**
     * 根据用户ID获取角色名称列表
     */
    @GetMapping("/permission/role/user/{userId}")
    ResponseResult getRoleNamesByUserId(@PathVariable("userId") String userId);
    
    // ==================== 用户管理接口 ====================
    
    /**
     * 分页获取用户列表
     */
    @PostMapping("/permission/user/list")
    ResponseResult getUserList(@RequestBody Map<String, Object> request);
    
    /**
     * 获取用户详情
     */
    @GetMapping("/permission/user/detail/{id}")
    ResponseResult getUserDetail(@PathVariable("id") String id);
    
    /**
     * 创建用户
     */
    @PostMapping("/permission/user/add")
    ResponseResult createUser(@RequestBody Map<String, Object> request);
    
    /**
     * 更新用户
     */
    @PostMapping("/permission/user/edit")
    ResponseResult updateUser(@RequestBody Map<String, Object> request);
    
    /**
     * 删除用户
     */
    @PostMapping("/permission/user/delete")
    ResponseResult deleteUser(@RequestBody Map<String, Object> request);
    
    /**
     * 重置用户密码
     */
    @PostMapping("/permission/user/reset-password")
    ResponseResult resetPassword(@RequestBody Map<String, Object> request);
    
    /**
     * 更新用户状态
     */
    @PostMapping("/permission/user/update-status")
    ResponseResult updateUserStatus(@RequestBody Map<String, Object> request);
    
    // ==================== 用户角色管理接口 ====================
    
    /**
     * 获取用户的角色列表
     */
    @PostMapping("/permission/user/roles")
    ResponseResult<List<Integer>> getUserRoles(@RequestBody Map<String, Object> request);
    
    /**
     * 设置用户角色
     */
    @PostMapping("/permission/user/roles/set")
    ResponseResult<Void> setUserRoles(@RequestBody Map<String, Object> request);
    
    /**
     * 获取角色的用户列表
     */
    @PostMapping("/permission/role/users")
    ResponseResult<List<Integer>> getRoleUsers(@RequestBody Map<String, Object> request);
    
    /**
     * 批量设置角色菜单关联
     */
    @PostMapping("/permission/role/menu/batch-set")
    ResponseResult<Void> batchSetRoleMenus(@RequestBody Map<String, Object> request);
    
    /**
     * 修复管理员权限
     */
    @PostMapping("/permission/role/menu/fix-admin-permissions")
    ResponseResult<Void> fixAdminPermissions(@RequestBody Map<String, Object> request);
    
    /**
     * 清除所有用户的菜单缓存
     */
    @PostMapping("/permission/menu/clear-cache")
    ResponseResult<Void> clearMenuCache();

    /**
     * 通用清缓存（按类型清除 Redis 缓存）
/**
     * @param params type: menu/data/all
     */
    @PostMapping("/permission/cache/clear")
    ResponseResult<Void> clearCache(@RequestBody Map<String, Object> params);
}
