package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.feign.PermissionServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Web端权限管理BFF控制器 - 仅做适配和路由
/**
 * 
 * @author Amos
 * @since 2025-01-07
 */
@RestController
@RequestMapping("/web/permission")
public class WebPermissionController {
    
    @Autowired
    private PermissionServiceFeign permissionServiceFeign;

    private <T> ResponseEntity<ResponseResult<T>> selfServiceResponse(
            Supplier<ResponseResult<T>> downstreamCall) {
        try {
            ResponseResult<T> response = downstreamCall.get();
            int code = response.getCode();
            if (isSelfServiceStatus(code)) {
                return ResponseEntity.status(code).body(response);
            }
            return ResponseEntity.ok(response);
        } catch (FeignException cause) {
            int status = cause.status();
            if (!isSelfServiceStatus(status)) {
                throw cause;
            }
            return ResponseEntity.status(status)
                    .body(ResponseResult.fail(status, permissionFailureMessage(status)));
        }
    }

    private boolean isSelfServiceStatus(int status) {
        return status == 400 || status == 401 || status == 403 || status == 503;
    }

    private String permissionFailureMessage(int status) {
        return switch (status) {
            case 400 -> "权限请求参数错误";
            case 401 -> "用户未登录或登录已过期";
            case 403 -> "无权执行此权限操作";
            default -> "权限服务暂时不可用";
        };
    }
    
    /**
     * 获取用户权限菜单 - 直接转发到权限服�?
     */
    @PostMapping("/menus")
    public ResponseResult<List<Map<String, Object>>> getUserMenus(@RequestBody Map<String, Object> request) {
        return permissionServiceFeign.getUserMenus(request);
    }
    
    /**
     * 获取用户权限菜单 - 兼容前端调用路径
     */
    @PostMapping("/user/menus")
    public ResponseEntity<ResponseResult<List<Map<String, Object>>>> getUserMenusByUser(
            @RequestBody Map<String, Object> request) {
        return selfServiceResponse(() -> permissionServiceFeign.getUserMenus(request));
    }
    
    /**
     * 获取子菜单树 - 新增接口
     */
    @PostMapping("/submenu/tree")
    public ResponseEntity<ResponseResult<List<Map<String, Object>>>> getSubMenuTree(
            @RequestBody Map<String, Object> request) {
        return selfServiceResponse(() -> permissionServiceFeign.getSubMenuTree(request));
    }
    
    /**
     * 修复管理员权限 - 新增接口
     */
    @PostMapping("/role/menu/fix-admin-permissions")
    public ResponseResult<Void> fixAdminPermissions(@RequestBody Map<String, Object> request) {
        return permissionServiceFeign.fixAdminPermissions(request);
    }
    
    /**
     * 检查用户权限 - 直接转发到权限服务
     */
    @PostMapping("/check")
    public ResponseResult<Boolean> checkUserPermission(@RequestBody Map<String, Object> request) {
        return permissionServiceFeign.checkUserPermission(request);
    }
    
    /**
     * 检查用户权限 - 兼容前端调用路径
     */
    @PostMapping("/user/check")
    public ResponseEntity<ResponseResult<Boolean>> checkUserPermissionByUser(
            @RequestBody Map<String, Object> request) {
        return selfServiceResponse(() -> permissionServiceFeign.checkUserPermission(request));
    }
    
    /**
     * 获取用户权限列表 - 直接转发到权限服务
     */
    @PostMapping("/permissions")
    public ResponseResult<List<String>> getUserPermissions(@RequestBody Map<String, Object> request) {
        return permissionServiceFeign.getUserPermissions(request);
    }
    
    /**
     * 获取用户权限列表 - 兼容前端调用路径
     */
    @PostMapping("/user/permissions")
    public ResponseEntity<ResponseResult<List<String>>> getUserPermissionsByUser(
            @RequestBody Map<String, Object> request) {
        return selfServiceResponse(() -> permissionServiceFeign.getUserPermissions(request));
    }

    /**
     * 检查当前用户页面访问权限
     */
    @PostMapping("/page/access/check")
    public ResponseEntity<ResponseResult<Boolean>> checkPageAccess(@RequestBody Map<String, Object> request) {
        return selfServiceResponse(() -> permissionServiceFeign.checkPageAccess(request));
    }

    /**
     * 获取当前用户页面按钮权限
     */
    @PostMapping("/page/buttons")
    public ResponseEntity<ResponseResult<List<String>>> getPageButtonPermissions(
            @RequestBody Map<String, Object> request) {
        return selfServiceResponse(() -> permissionServiceFeign.getPageButtonPermissions(request));
    }

    /**
     * 批量检查当前用户按钮权限
     */
    @PostMapping("/page/buttons/batch-check")
    public ResponseEntity<ResponseResult<Map<String, Boolean>>> batchCheckButtonPermissions(
            @RequestBody Map<String, Object> request) {
        return selfServiceResponse(() -> permissionServiceFeign.batchCheckButtonPermissions(request));
    }
    
    /**
     * 批量检查用户权限 - 直接转发到权限服务
     */
    @PostMapping("/batch-check")
    public ResponseEntity<ResponseResult<Map<String, Boolean>>> batchCheckUserPermission(
            @RequestBody Map<String, Object> request) {
        return selfServiceResponse(() -> permissionServiceFeign.batchCheckButtonPermissions(request));
    }
    
    /**
     * 获取所有菜�?- 直接转发到权限服�?
     */
    @PostMapping("/menu/list")
    public ResponseResult getAllMenus() {
        return permissionServiceFeign.getAllMenus();
    }

    /**
     * 获取菜单权限树（用于角色权限管理�? 直接转发到权限服�?
     */
    @PostMapping("/menu/tree")
    public ResponseResult getMenuTreeForRole() {
        return permissionServiceFeign.getMenuTreeForRole();
    }
    
    /**
     * 根据角色获取菜单 - 直接转发到权限服�?
     */
    @PostMapping("/menu/role")
    public ResponseResult getMenusByRole(@RequestBody Map<String, Object> request) {
        return permissionServiceFeign.getMenusByRole((Integer) request.get("roleId"));
    }
    
    /**
     * 新增菜单 - 直接转发到权限服�?
     */
    @PostMapping("/menu/add")
    public ResponseResult addMenu(@RequestBody Map<String, Object> request) {
        return permissionServiceFeign.addMenu(request);
    }
    
    /**
     * 编辑菜单 - 直接转发到权限服�?
     */
    @PostMapping("/menu/edit")
    public ResponseResult editMenu(@RequestBody Map<String, Object> request) {
        return permissionServiceFeign.editMenu(request);
    }
    
    /**
     * 删除菜单 - 直接转发到权限服�?
     */
    @PostMapping("/menu/delete")
    public ResponseResult deleteMenu(@RequestBody Map<String, Object> request) {
        return permissionServiceFeign.deleteMenu(request);
    }
    
    /**
     * 获取菜单详情 - 直接转发到权限服�?
     */
    @GetMapping("/menu/detail/{id}")
    public ResponseResult getMenuDetail(@PathVariable Integer id) {
        return permissionServiceFeign.getMenuDetail(id);
    }
    
    /**
     * 更新菜单显示位置（左侧竖栏 / 右上角插件分类 / 隐藏）
     */
    @PostMapping("/menu/display/update")
    public ResponseResult updateMenuDisplay(@RequestBody Map<String, Object> request) {
        return permissionServiceFeign.updateMenuDisplay(request);
    }

    /**
     * 获取插件中心分类列表（顶栏右上角按分类聚合插件菜单）
     */
    @GetMapping("/addon/cate/list")
    public ResponseResult getAddonCateList() {
        return permissionServiceFeign.getAddonCateList();
    }
    
    // ==================== 角色管理接口 ====================
    
    /**
     * 获取所有角�?- 直接转发到权限服�?
     */
    @GetMapping("/role/all")
    public ResponseResult getAllRoles() {
        return permissionServiceFeign.getAllRoles();
    }
    
    /**
     * 分页获取角色列表 - 直接转发到权限服�?
     */
    @PostMapping("/role/list")
    public ResponseResult getRoleList(@RequestBody Map<String, Object> request) {
        return permissionServiceFeign.getRoleList(request);
    }
    
    /**
     * 获取角色详情 - 直接转发到权限服�?
     */
    @GetMapping("/role/detail/{id}")
    public ResponseResult getRoleDetail(@PathVariable Integer id) {
        return permissionServiceFeign.getRoleDetail(id);
    }
    
    /**
     * 创建角色 - 直接转发到权限服�?
     */
    @PostMapping("/role/add")
    public ResponseResult createRole(@RequestBody Map<String, Object> request) {
        return permissionServiceFeign.createRole(request);
    }
    
    /**
     * 更新角色 - 直接转发到权限服�?
     */
    @PostMapping("/role/edit")
    public ResponseResult updateRole(@RequestBody Map<String, Object> request) {
        return permissionServiceFeign.updateRole(request);
    }
    
    /**
     * 删除角色 - 直接转发到权限服�?
     */
    @PostMapping("/role/delete")
    public ResponseResult deleteRole(@RequestBody Map<String, Object> request) {
        return permissionServiceFeign.deleteRole(request);
    }
    
    /**
     * 根据用户ID获取角色名称列表 - 直接转发到权限服�?
     */
    @GetMapping("/role/user/{userId}")
    public ResponseResult getRoleNamesByUserId(@PathVariable String userId) {
        return permissionServiceFeign.getRoleNamesByUserId(userId);
    }
    
    // ==================== 用户管理接口 ====================
    
    /**
     * 分页获取用户列表 - 直接转发到权限服�?
     */
    @PostMapping("/user/list")
    public ResponseResult getUserList(@RequestBody Map<String, Object> request) {
        return permissionServiceFeign.getUserList(request);
    }
    
    /**
     * 获取用户详情 - 直接转发到权限服�?
     */
    @GetMapping("/user/detail/{id}")
    public ResponseResult getUserDetail(@PathVariable String id) {
        return permissionServiceFeign.getUserDetail(id);
    }
    
    /**
     * 创建用户 - 直接转发到权限服�?
     */
    @PostMapping("/user/add")
    public ResponseResult createUser(@RequestBody Map<String, Object> request) {
        return permissionServiceFeign.createUser(request);
    }
    
    /**
     * 更新用户 - 直接转发到权限服�?
     */
    @PostMapping("/user/edit")
    public ResponseResult updateUser(@RequestBody Map<String, Object> request) {
        return permissionServiceFeign.updateUser(request);
    }
    
    /**
     * 删除用户 - 直接转发到权限服�?
     */
    @PostMapping("/user/delete")
    public ResponseResult deleteUser(@RequestBody Map<String, Object> request) {
        return permissionServiceFeign.deleteUser(request);
    }
    
    /**
     * 重置用户密码 - 直接转发到权限服�?
     */
    @PostMapping("/user/reset-password")
    public ResponseResult resetPassword(@RequestBody Map<String, Object> request) {
        return permissionServiceFeign.resetPassword(request);
    }
    
    /**
     * 更新用户状�?- 直接转发到权限服�?
     */
    @PostMapping("/user/update-status")
    public ResponseResult updateUserStatus(@RequestBody Map<String, Object> request) {
        return permissionServiceFeign.updateUserStatus(request);
    }
    
    // ==================== 用户角色管理接口 ====================
    
    /**
     * 获取用户的角色列�?
     */
    @PostMapping("/user/roles")
    public ResponseResult<List<Integer>> getUserRoles(@RequestBody Map<String, Object> request) {
        return permissionServiceFeign.getUserRoles(request);
    }
    
    /**
     * 设置用户角色
     */
    @PostMapping("/user/roles/set")
    public ResponseResult<Void> setUserRoles(@RequestBody Map<String, Object> request) {
        return permissionServiceFeign.setUserRoles(request);
    }
    
    /**
     * 获取角色的用户列�?
     */
    @PostMapping("/role/users")
    public ResponseResult<List<Integer>> getRoleUsers(@RequestBody Map<String, Object> request) {
        return permissionServiceFeign.getRoleUsers(request);
    }
    
    /**
     * 批量设置角色菜单关联
     */
    @PostMapping("/role/menu/batch-set")
    public ResponseResult<Void> batchSetRoleMenus(@RequestBody Map<String, Object> request) {
        return permissionServiceFeign.batchSetRoleMenus(request);
    }
    
    /**
     * 清除所有用户的菜单缓存
     */
    @PostMapping("/menu/clear-cache")
    public ResponseResult<Void> clearMenuCache() {
        return permissionServiceFeign.clearMenuCache();
    }

    /**
     * 通用清缓存（按类型清除 Redis 缓存）
/**
     * @param params type: menu=菜单权限缓存, data=业务数据缓存, all=全部缓存
     */
    @PostMapping("/cache/clear")
    public ResponseResult<Void> clearCache(@RequestBody Map<String, Object> params) {
        return permissionServiceFeign.clearCache(params);
    }
}
