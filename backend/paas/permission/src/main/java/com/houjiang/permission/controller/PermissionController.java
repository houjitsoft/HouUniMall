package com.houjiang.permission.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.permission.service.PermissionService;
import com.houjiang.permission.service.MenuService;
import com.houjiang.permission.service.RoleService;
import com.houjiang.permission.service.UserService;
import com.houjiang.permission.service.UserRoleService;
import com.houjiang.permission.security.PermissionIdentity;
import com.houjiang.permission.security.PermissionGuard;
import com.houjiang.permission.util.PermissionUserIdParser;
import com.houjiang.permission.service.RoleMenuService;
import com.houjiang.permission.vo.MenuVO;
import com.houjiang.permission.vo.RoleVO;
import com.houjiang.permission.vo.PageResult;
import com.houjiang.permission.dto.PageRequest;
import com.houjiang.permission.dto.RoleCreateRequest;
import com.houjiang.permission.dto.RoleUpdateRequest;
import com.houjiang.permission.dto.UserCreateRequest;
import com.houjiang.permission.dto.UserUpdateRequest;
import com.houjiang.permission.dto.UserSearchRequest;
import com.houjiang.commons.config.RedisCache;
import com.houjiang.permission.vo.UserVO;
import com.houjiang.permission.entity.PermissionMenu;
import com.houjiang.permission.mapper.PermissionMenuMapper;
import com.houjiang.permission.mapper.RoleMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

/**
 * 权限管理控制器
 * 
 * @author Amos
 * @since 2025-01-07
 */
@Slf4j
@RestController
@RequestMapping("/permission")
public class PermissionController {
    
    @Autowired
    private PermissionService permissionService;
    
    @Autowired
    private MenuService menuService;
    
    @Autowired
    private RoleService roleService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserRoleService userRoleService;
    
    @Autowired
    private RoleMenuService roleMenuService;
    
    @Autowired
    private PermissionMenuMapper permissionMenuMapper;

    @Autowired
    private PermissionIdentity permissionIdentity;

    @Autowired
    private PermissionGuard permissionGuard;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired(required = false)
    private RedisCache redisCache;
    
    /**
     * 获取用户权限菜单
     */
    @PostMapping("/user/menus")
    public ResponseResult<List<MenuVO>> getUserMenus(@RequestBody Map<String, Object> params) {
        Long userId = permissionIdentity.requireSelf(params.get("userId"));
        long startTime = System.currentTimeMillis();
        try {
            List<MenuVO> menus = menuService.getUserMenus(userId);
            long queryTime = System.currentTimeMillis() - startTime;
            log.debug("获取用户菜单完成，userId={}, 菜单数量={}, 耗时={}ms", userId, 
                menus != null ? menus.size() : 0, queryTime);
            return ResponseResult.success(menus);
        } catch (Exception e) {
            propagateResponseStatus(e);
            long errorTime = System.currentTimeMillis() - startTime;
            log.error("获取用户菜单失败，耗时={}ms", errorTime, e);
            return ResponseResult.fail(500, "获取用户菜单失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/submenu/tree")
    public ResponseResult<List<MenuVO>> getSubMenuTree(@RequestBody Map<String, Object> params) {
        Long userId = permissionIdentity.requireSelf(params.get("userId"));
        try {
            // 获取一级菜单ID
            Object parentMenuIdObj = params.get("parentMenuId");
            if (parentMenuIdObj == null) {
                return ResponseResult.fail(400, "一级菜单ID不能为空");
            }
            Integer parentMenuId = Integer.valueOf(parentMenuIdObj.toString());
            
            log.info("获取子菜单树，用户ID: {}, 一级菜单ID: {}", userId, parentMenuId);
            
            List<MenuVO> subMenus = menuService.getSubMenuTree(userId, parentMenuId);
            return ResponseResult.success(subMenus);
        } catch (Exception e) {
            propagateResponseStatus(e);
            log.error("获取子菜单树失败", e);
            return ResponseResult.fail(500, "获取子菜单树失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查用户权限
     */
    @PostMapping("/user/check")
    public ResponseResult<Boolean> checkUserPermission(@RequestBody Map<String, Object> params) {
        Long userId = permissionIdentity.requireSelf(params.get("userId"));
        try {
            String permission = (String) params.get("permission");
            if (permission == null) {
                return ResponseResult.fail(400, "权限标识不能为空");
            }

            boolean hasPermission = permissionService.hasPermission(userId, permission);
            return ResponseResult.success(hasPermission);
        } catch (Exception e) {
            propagateResponseStatus(e);
            log.error("检查用户权限失败", e);
            return ResponseResult.fail(500, "检查用户权限失败");
        }
    }
    
    /**
     * 获取用户权限列表
     */
    @PostMapping("/user/permissions")
    public ResponseResult<List<String>> getUserPermissions(@RequestBody Map<String, Object> params) {
        Long userId = permissionIdentity.requireSelf(params.get("userId"));
        try {
            List<String> permissions = permissionService.getUserPermissions(userId);
            return ResponseResult.success(permissions);
        } catch (Exception e) {
            propagateResponseStatus(e);
            log.error("获取用户权限失败", e);
            return ResponseResult.fail(500, "获取用户权限失败");
        }
    }
    
    /**
     * 获取所有菜单（用于菜单管理）
     */
    @PostMapping("/menu/list")
    public ResponseResult<List<MenuVO>> getAllMenus() {
        permissionGuard.require("shell:setting:permission:menu", PermissionGuard.Risk.READ);
        try {
            List<MenuVO> menus = menuService.getAllMenus();
            return ResponseResult.success(menus);
        } catch (Exception e) {
            propagateResponseStatus(e);
            log.error("获取菜单列表失败", e);
            return ResponseResult.fail(500, "获取菜单列表失败");
        }
    }

    /**
     * 获取菜单权限树（用于角色权限管理）
     */
    @PostMapping("/menu/tree")
    public ResponseResult<List<MenuVO>> getMenuTreeForRole() {
        permissionGuard.require("shell:setting:permission:menu", PermissionGuard.Risk.READ);
        try {
            List<MenuVO> menus = menuService.getMenuTreeForRole();
            return ResponseResult.success(menus);
        } catch (Exception e) {
            propagateResponseStatus(e);
            log.error("获取菜单权限树失败", e);
            return ResponseResult.fail(500, "获取菜单权限树失败");
        }
    }

    
    /**
     * 根据角色获取菜单
     */
    @GetMapping("/menu/role/{roleId}")
    public ResponseResult<List<MenuVO>> getMenusByRole(@PathVariable Integer roleId) {
        permissionGuard.require("shell:setting:permission:menu", PermissionGuard.Risk.READ);
        try {
            List<MenuVO> menus = menuService.getMenusByRole(roleId);
            return ResponseResult.success(menus);
        } catch (Exception e) {
            propagateResponseStatus(e);
            log.error("根据角色获取菜单失败", e);
            return ResponseResult.fail(500, "根据角色获取菜单失败");
        }
    }
    
    /**
     * 新增菜单
     */
    @PostMapping("/menu/add")
    public ResponseResult<String> addMenu(@RequestBody MenuCreateRequest request) {
        permissionGuard.require("shell:setting:permission:menu:add", PermissionGuard.Risk.WRITE);
        try {
            menuService.addMenu(request);
            return ResponseResult.success("新增菜单成功");
        } catch (Exception e) {
            propagateResponseStatus(e);
            log.error("新增菜单失败", e);
            return ResponseResult.fail(500, "新增菜单失败: " + e.getMessage());
        }
    }
    
    /**
     * 编辑菜单
     */
    @PostMapping("/menu/edit")
    public ResponseResult<String> editMenu(@RequestBody MenuUpdateRequest request) {
        permissionGuard.require("shell:setting:permission:menu:edit", PermissionGuard.Risk.WRITE);
        if (request.getIsDisable() != null && isMenuStatusChange(request)) {
            String action = request.getIsDisable() == 0 ? "enable" : "disable";
            permissionGuard.require("shell:setting:permission:menu:" + action,
                    PermissionGuard.Risk.STATUS_CHANGE);
        }
        try {
            menuService.editMenu(request);
            return ResponseResult.success("编辑菜单成功");
        } catch (Exception e) {
            propagateResponseStatus(e);
            log.error("编辑菜单失败", e);
            return ResponseResult.fail(500, "编辑菜单失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除菜单
     */
    @PostMapping("/menu/delete")
    public ResponseResult<String> deleteMenu(@RequestBody MenuDeleteRequest request) {
        permissionGuard.require("shell:setting:permission:menu:delete", PermissionGuard.Risk.DELETE);
        try {
            menuService.deleteMenu(request.getId());
            return ResponseResult.success("删除菜单成功");
        } catch (Exception e) {
            propagateResponseStatus(e);
            log.error("删除菜单失败", e);
            return ResponseResult.fail(500, "删除菜单失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取菜单详情
     */
    @GetMapping("/menu/detail/{id}")
    public ResponseResult<MenuVO> getMenuDetail(@PathVariable Integer id) {
        permissionGuard.require("shell:setting:permission:menu", PermissionGuard.Risk.READ);
        try {
            MenuVO menu = menuService.getMenuDetail(id);
            return ResponseResult.success(menu);
        } catch (Exception e) {
            propagateResponseStatus(e);
            log.error("获取菜单详情失败", e);
            return ResponseResult.fail(500, "获取菜单详情失败");
        }
    }
    
    /**
     * 获取插件中心分类列表（顶栏右上角按分类聚合插件菜单）
     */
    @GetMapping("/addon/cate/list")
    public ResponseResult<List<Map<String, Object>>> getAddonCateList() {
        try {
            return ResponseResult.success(menuService.getAddonCateList());
        } catch (Exception e) {
            propagateResponseStatus(e);
            log.error("获取插件分类列表失败", e);
            return ResponseResult.fail(500, "获取插件分类列表失败");
        }
    }

    /**
     * 更新菜单显示位置（左侧竖栏 / 右上角插件分类 / 隐藏）
     * body: { id, displayPos, cateId }
     */
    @PostMapping("/menu/display/update")
    public ResponseResult<String> updateMenuDisplay(@RequestBody Map<String, Object> request) {
        permissionGuard.require("shell:setting:permission:menu:edit", PermissionGuard.Risk.WRITE);
        try {
            Object idObj = request.get("id");
            if (idObj == null) {
                return ResponseResult.fail(400, "菜单ID不能为空");
            }
            Integer id = Integer.valueOf(String.valueOf(idObj).trim());
            Object posObj = request.get("displayPos");
            Integer cateId = null;
            Object cateObj = request.get("cateId");
            if (cateObj != null && !String.valueOf(cateObj).trim().isEmpty()) {
                cateId = Integer.valueOf(String.valueOf(cateObj).trim());
            }
            menuService.updateMenuDisplay(id, posObj == null ? null : String.valueOf(posObj), cateId);
            return ResponseResult.success("菜单显示位置已更新");
        } catch (NumberFormatException e) {
            return ResponseResult.fail(400, "参数格式错误: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseResult.fail(400, e.getMessage());
        } catch (Exception e) {
            propagateResponseStatus(e);
            log.error("更新菜单显示位置失败", e);
            return ResponseResult.fail(500, "更新菜单显示位置失败: " + e.getMessage());
        }
    }

    /**
     * 用户菜单请求对象
     */
    public static class UserMenuRequest {
        private String userId;
        private String orgId;
        private String serviceName;
        
        /** getters and setters */
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getOrgId() { return orgId; }
        public void setOrgId(String orgId) { this.orgId = orgId; }
        public String getServiceName() { return serviceName; }
        public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    }
    
    /**
     * 权限检查请求对象
     */
    public static class PermissionCheckRequest {
        private String userId;
        private String orgId;
        private String resourceCode;
        private String permissionType;
        
        /** getters and setters */
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getOrgId() { return orgId; }
        public void setOrgId(String orgId) { this.orgId = orgId; }
        public String getResourceCode() { return resourceCode; }
        public void setResourceCode(String resourceCode) { this.resourceCode = resourceCode; }
        public String getPermissionType() { return permissionType; }
        public void setPermissionType(String permissionType) { this.permissionType = permissionType; }
    }
    
    /**
     * 菜单创建请求对象
     */
    public static class MenuCreateRequest {
        private String type;
        private Integer parentId;
        private String name;
        private String icon;
        private String paths;
        private String component;
        private String selected;
        private String perms;
        private String params;
        private Integer isCache;
        private Integer isShow;
        private Integer isDisable;
        private Integer sort;
        
        /** getters and setters */
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public Integer getParentId() { return parentId; }
        public void setParentId(Integer parentId) { this.parentId = parentId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getIcon() { return icon; }
        public void setIcon(String icon) { this.icon = icon; }
        public String getPaths() { return paths; }
        public void setPaths(String paths) { this.paths = paths; }
        public String getComponent() { return component; }
        public void setComponent(String component) { this.component = component; }
        public String getSelected() { return selected; }
        public void setSelected(String selected) { this.selected = selected; }
        public String getPerms() { return perms; }
        public void setPerms(String perms) { this.perms = perms; }
        public String getParams() { return params; }
        public void setParams(String params) { this.params = params; }
        public Integer getIsCache() { return isCache; }
        public void setIsCache(Integer isCache) { this.isCache = isCache; }
        public Integer getIsShow() { return isShow; }
        public void setIsShow(Integer isShow) { this.isShow = isShow; }
        public Integer getIsDisable() { return isDisable; }
        public void setIsDisable(Integer isDisable) { this.isDisable = isDisable; }
        public Integer getSort() { return sort; }
        public void setSort(Integer sort) { this.sort = sort; }
    }
    
    /**
     * 菜单更新请求对象
     */
    public static class MenuUpdateRequest {
        private Integer id;
        private String type;
        private Integer parentId;
        private String name;
        private String icon;
        private String paths;
        private String component;
        private String selected;
        private String perms;
        private String params;
        private Integer isCache;
        private Integer isShow;
        private Integer isDisable;
        private Integer sort;
        
        /** getters and setters */
        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public Integer getParentId() { return parentId; }
        public void setParentId(Integer parentId) { this.parentId = parentId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getIcon() { return icon; }
        public void setIcon(String icon) { this.icon = icon; }
        public String getPaths() { return paths; }
        public void setPaths(String paths) { this.paths = paths; }
        public String getComponent() { return component; }
        public void setComponent(String component) { this.component = component; }
        public String getSelected() { return selected; }
        public void setSelected(String selected) { this.selected = selected; }
        public String getPerms() { return perms; }
        public void setPerms(String perms) { this.perms = perms; }
        public String getParams() { return params; }
        public void setParams(String params) { this.params = params; }
        public Integer getIsCache() { return isCache; }
        public void setIsCache(Integer isCache) { this.isCache = isCache; }
        public Integer getIsShow() { return isShow; }
        public void setIsShow(Integer isShow) { this.isShow = isShow; }
        public Integer getIsDisable() { return isDisable; }
        public void setIsDisable(Integer isDisable) { this.isDisable = isDisable; }
        public Integer getSort() { return sort; }
        public void setSort(Integer sort) { this.sort = sort; }
    }
    
    /**
     * 菜单删除请求对象
     */
    public static class MenuDeleteRequest {
        private Integer id;
        
        /** getters and setters */
        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
    }
    
    // ==================== 角色管理接口 ====================
    
    /**
     * 获取所有角色
     */
    @GetMapping("/role/all")
    public ResponseResult<List<RoleVO>> getAllRoles() {
        permissionGuard.require("shell:setting:permission:role", PermissionGuard.Risk.READ);
        try {
            List<RoleVO> roles = roleService.getAllRoles();
            return ResponseResult.success(roles);
        } catch (Exception e) {
            propagateResponseStatus(e);
            log.error("获取所有角色失败", e);
            return ResponseResult.fail(500, "获取所有角色失败");
        }
    }
    
    /**
     * 分页获取角色列表
     */
    @PostMapping("/role/list")
    public ResponseResult<PageResult<RoleVO>> getRoleList(@RequestBody Map<String, Object> params) {
        permissionGuard.require("shell:setting:permission:role", PermissionGuard.Risk.READ);
        try {
            PageRequest pageRequest = new PageRequest();
            pageRequest.setPageNo(((Number) params.getOrDefault("page", 1)).intValue());
            pageRequest.setPageSize(((Number) params.getOrDefault("limit", 10)).intValue());
            PageResult<RoleVO> result = roleService.getRoleList(pageRequest);
            return ResponseResult.success(result);
        } catch (Exception e) {
            propagateResponseStatus(e);
            log.error("获取角色列表失败", e);
            return ResponseResult.fail(500, "获取角色列表失败");
        }
    }
    
    /**
     * 获取角色详情
     */
    @GetMapping("/role/detail/{id}")
    public ResponseResult<RoleVO> getRoleDetail(@PathVariable Integer id) {
        permissionGuard.require("shell:setting:permission:role", PermissionGuard.Risk.READ);
        try {
            RoleVO role = roleService.getRoleDetail(id);
            return ResponseResult.success(role);
        } catch (Exception e) {
            propagateResponseStatus(e);
            log.error("获取角色详情失败", e);
            return ResponseResult.fail(500, "获取角色详情失败");
        }
    }
    
    /**
     * 创建角色
     */
    @PostMapping("/role/add")
    public ResponseResult<String> createRole(@RequestBody RoleCreateRequest request) {
        permissionGuard.require("shell:setting:permission:role:add", PermissionGuard.Risk.WRITE);
        if (request.getMenuIds() != null) {
            permissionGuard.require("shell:setting:permission:role:assign", PermissionGuard.Risk.ASSIGN);
        }
        try {
            roleService.createRole(request);
            return ResponseResult.success("创建角色成功");
        } catch (Exception e) {
            propagateResponseStatus(e);
            log.error("创建角色失败", e);
            return ResponseResult.fail(500, "创建角色失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新角色
     */
    @PostMapping("/role/edit")
    public ResponseResult<String> updateRole(@RequestBody RoleUpdateRequest request) {
        permissionGuard.require("shell:setting:permission:role:edit", PermissionGuard.Risk.WRITE);
        if (request.getMenuIds() != null) {
            permissionGuard.require("shell:setting:permission:role:assign", PermissionGuard.Risk.ASSIGN);
        }
        try {
            roleService.updateRole(request);
            return ResponseResult.success("更新角色成功");
        } catch (Exception e) {
            propagateResponseStatus(e);
            log.error("更新角色失败", e);
            return ResponseResult.fail(500, "更新角色失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除角色
     */
    @PostMapping("/role/delete")
    public ResponseResult<String> deleteRole(@RequestBody RoleDeleteRequest request) {
        permissionGuard.require("shell:setting:permission:role:delete", PermissionGuard.Risk.DELETE);
        try {
            roleService.deleteRole(request.getId());
            return ResponseResult.success("删除角色成功");
        } catch (Exception e) {
            propagateResponseStatus(e);
            log.error("删除角色失败", e);
            return ResponseResult.fail(500, "删除角色失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据用户ID获取角色名称列表
     */
    @GetMapping("/role/user/{userId}")
    public ResponseResult<List<String>> getRoleNamesByUserId(@PathVariable String userId) {
        permissionGuard.require("shell:setting:permission:user", PermissionGuard.Risk.READ);
        try {
            List<String> roleNames = roleService.getRoleNamesByUserId(userId);
            return ResponseResult.success(roleNames);
        } catch (Exception e) {
            propagateResponseStatus(e);
            log.error("获取用户角色失败", e);
            return ResponseResult.fail(500, "获取用户角色失败");
        }
    }
    
    /**
     * 角色删除请求对象
     */
    public static class RoleDeleteRequest {
        private Integer id;
        
        /** getters and setters */
        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
    }
    
    // ==================== 用户管理接口 ====================
    
    /**
     * 分页获取用户列表
     */
    @PostMapping("/user/list")
    public ResponseResult<PageResult<UserVO>> getUserList(@RequestBody UserSearchRequest request) {
        permissionGuard.require("shell:setting:permission:user", PermissionGuard.Risk.READ);
        try {
            PageResult<UserVO> result = userService.getUserList(request);
            return ResponseResult.success(result);
        } catch (Exception e) {
            propagateResponseStatus(e);
            log.error("获取用户列表失败", e);
            return ResponseResult.fail(500, "获取用户列表失败");
        }
    }
    
    /**
     * 获取用户详情
     */
    @GetMapping("/user/detail/{id}")
    public ResponseResult<UserVO> getUserDetail(@PathVariable String id) {
        permissionGuard.require("shell:setting:permission:user", PermissionGuard.Risk.READ);
        try {
            UserVO user = userService.getUserDetail(id);
            return ResponseResult.success(user);
        } catch (Exception e) {
            propagateResponseStatus(e);
            log.error("获取用户详情失败", e);
            return ResponseResult.fail(500, "获取用户详情失败");
        }
    }
    
    /**
     * 创建用户
     */
    @PostMapping("/user/add")
    public ResponseResult<String> createUser(@RequestBody UserCreateRequest request) {
        permissionGuard.require("shell:setting:permission:user:add", PermissionGuard.Risk.WRITE);
        if (request.getRoleIds() != null) {
            permissionGuard.require("shell:setting:permission:user:assign", PermissionGuard.Risk.ASSIGN);
        }
        try {
            userService.createUser(request);
            return ResponseResult.success("创建用户成功");
        } catch (Exception e) {
            propagateResponseStatus(e);
            log.error("创建用户失败", e);
            return ResponseResult.fail(500, "创建用户失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新用户
     */
    @PostMapping("/user/edit")
    public ResponseResult<String> updateUser(@RequestBody UserUpdateRequest request) {
        permissionGuard.require("shell:setting:permission:user:edit", PermissionGuard.Risk.WRITE);
        if (request.getRoleIds() != null) {
            permissionGuard.require("shell:setting:permission:user:assign", PermissionGuard.Risk.ASSIGN);
        }
        Integer targetStatus = request.getStatus() != null ? request.getStatus() : request.getUserState();
        if (targetStatus != null && isUserStatusChange(request.getId(), targetStatus)) {
            String action = targetStatus == 0 ? "disable" : "enable";
            permissionGuard.require("shell:setting:permission:user:" + action,
                    PermissionGuard.Risk.STATUS_CHANGE);
            if (request.getStatus() == null) {
                request.setStatus(targetStatus);
            }
        }
        try {
            userService.updateUser(request);
            return ResponseResult.success("更新用户成功");
        } catch (Exception e) {
            propagateResponseStatus(e);
            log.error("更新用户失败", e);
            return ResponseResult.fail(500, "更新用户失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除用户
     */
    @PostMapping("/user/delete")
    public ResponseResult<String> deleteUser(@RequestBody UserDeleteRequest request) {
        permissionGuard.require("shell:setting:permission:user:delete", PermissionGuard.Risk.DELETE);
        try {
            userService.deleteUser(request.getId());
            return ResponseResult.success("删除用户成功");
        } catch (Exception e) {
            propagateResponseStatus(e);
            log.error("删除用户失败", e);
            return ResponseResult.fail(500, "删除用户失败: " + e.getMessage());
        }
    }
    
    /**
     * 重置用户密码
     */
    @PostMapping("/user/reset-password")
    public ResponseResult<String> resetPassword(@RequestBody UserResetPasswordRequest request) {
        permissionGuard.require("shell:setting:permission:user:reset", PermissionGuard.Risk.RESET_PASSWORD);
        try {
            userService.resetPassword(request.getId(), request.getNewPassword());
            return ResponseResult.success("密码重置成功");
        } catch (Exception e) {
            propagateResponseStatus(e);
            log.error("重置密码失败", e);
            return ResponseResult.fail(500, "重置密码失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新用户状态
     */
    @PostMapping("/user/update-status")
    public ResponseResult<String> updateUserStatus(@RequestBody UserUpdateStatusRequest request) {
        String action = request.getStatus() != null && request.getStatus() == 0 ? "disable" : "enable";
        permissionGuard.require("shell:setting:permission:user:" + action,
                PermissionGuard.Risk.STATUS_CHANGE);
        try {
            userService.updateUserStatus(request.getId(), request.getStatus());
            return ResponseResult.success("状态更新成功");
        } catch (Exception e) {
            propagateResponseStatus(e);
            log.error("更新用户状态失败", e);
            return ResponseResult.fail(500, "更新用户状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 用户删除请求对象
     */
    public static class UserDeleteRequest {
        private String id;
        
        /** getters and setters */
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
    }
    
    /**
     * 用户重置密码请求对象
     */
    public static class UserResetPasswordRequest {
        private String id;
        private String newPassword;
        
        /** getters and setters */
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }
    
    /**
     * 用户更新状态请求对象
     */
    public static class UserUpdateStatusRequest {
        private String id;
        private Integer status;
        
        /** getters and setters */
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public Integer getStatus() { return status; }
        public void setStatus(Integer status) { this.status = status; }
    }
    
    // ==================== 用户角色管理接口 ====================
    
    /**
     * 获取用户的角色列表
     */
    @PostMapping("/user/roles")
    public ResponseResult<List<Integer>> getUserRoles(@RequestBody Map<String, Object> params) {
        permissionGuard.require("shell:setting:permission:user:assign", PermissionGuard.Risk.ASSIGN);
        try {
            Object userIdObj = params.get("userId");
            if (userIdObj == null) {
                return ResponseResult.fail(400, "用户ID不能为空");
            }
            Long userId = PermissionUserIdParser.parse(userIdObj);
            if (userId == null) {
                return ResponseResult.fail(400, "用户ID不能为空");
            }
            List<Integer> roleIds = userRoleService.getRoleIdsByUserId(userId);
            return ResponseResult.success(roleIds);
        } catch (Exception e) {
            propagateResponseStatus(e);
            log.error("获取用户角色失败", e);
            return ResponseResult.fail(500, "获取用户角色失败");
        }
    }
    
    /**
     * 设置用户角色
     */
    @PostMapping("/user/roles/set")
    public ResponseResult<Void> setUserRoles(@RequestBody Map<String, Object> params) {
        permissionGuard.require("shell:setting:permission:user:assign", PermissionGuard.Risk.ASSIGN);
        try {
            Object userIdObj = params.get("userId");
            List<Integer> roleIds = parseRoleIdsParam(params.get("roleIds"));
            if (userIdObj == null) {
                return ResponseResult.fail(400, "用户ID不能为空");
            }
            Long userId = PermissionUserIdParser.parse(userIdObj);
            if (userId == null) {
                return ResponseResult.fail(400, "用户ID不能为空");
            }
            userRoleService.batchSetUserRoles(userId, roleIds);
            return ResponseResult.success();
        } catch (Exception e) {
            propagateResponseStatus(e);
            log.error("设置用户角色失败", e);
            return ResponseResult.fail(500, "设置用户角色失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取角色的用户列表
     */
    @PostMapping("/role/users")
    public ResponseResult<List<Long>> getRoleUsers(@RequestBody Map<String, Object> params) {
        permissionGuard.require("shell:setting:permission:role:assign", PermissionGuard.Risk.ASSIGN);
        try {
            Integer roleId = (Integer) params.get("roleId");
            if (roleId == null) {
                return ResponseResult.fail(400, "角色ID不能为空");
            }
            
            log.info("获取角色用户: roleId={}", roleId);
            List<Long> userIds = userRoleService.getUserIdsByRoleId(roleId);
            log.info("获取到的用户ID列表: {}", userIds);
            return ResponseResult.success(userIds);
        } catch (Exception e) {
            propagateResponseStatus(e);
            log.error("获取角色用户失败", e);
            return ResponseResult.fail(500, "获取角色用户失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量设置角色菜单关联
     */
    @PostMapping("/role/menu/fix-admin-permissions")
    public ResponseResult<Void> fixAdminPermissions(@RequestBody Map<String, Object> params) {
        permissionGuard.require("shell:setting:permission:role:assign", PermissionGuard.Risk.ASSIGN);
        permissionGuard.requireSuperAdmin();
        try {
            log.info("开始修复管理员权限...");
            
            QueryWrapper<PermissionMenu> permissionCenterQuery = new QueryWrapper<>();
            permissionCenterQuery.eq("is_disable", 0)
                    .isNotNull("perms")
                    .and(scope -> scope.eq("perms", "shell:setting:permission")
                            .or()
                            .likeRight("perms", "shell:setting:permission:"));
            List<Integer> permissionCenterMenuIds = permissionMenuMapper.selectList(permissionCenterQuery).stream()
                .map(PermissionMenu::getId)
                .collect(Collectors.toList());

            log.info("找到权限中心菜单数量: {}", permissionCenterMenuIds.size());
            
            QueryWrapper<com.houjiang.permission.entity.Role> roleQuery = new QueryWrapper<>();
            roleQuery.apply("LOWER(TRIM(name)) IN ('admin','super_admin','管理员','超级管理员')")
                    .and(query -> query.isNull("delete_time").or().eq("delete_time", 0));
            List<Integer> adminRoleIds = roleMapper.selectList(roleQuery).stream()
                    .map(com.houjiang.permission.entity.Role::getId)
                    .collect(Collectors.toList());
            if (adminRoleIds.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "未找到标准管理员角色");
            }

            for (Integer adminRoleId : adminRoleIds) {
                roleMenuService.addMissingMenuIds(adminRoleId, permissionCenterMenuIds);
            }
            log.info("已为标准管理员角色幂等补齐权限中心权限，roleIds={}, 菜单数量={}",
                    adminRoleIds, permissionCenterMenuIds.size());
            
            // 清除所有用户的菜单缓存，确保新菜单立即生效
            menuService.clearAllUserMenusCache();
            log.info("已清除所有用户菜单缓存");
            
            return ResponseResult.success();
        } catch (Exception e) {
            propagateResponseStatus(e);
            log.error("修复管理员权限失败", e);
            return ResponseResult.fail(500, "修复管理员权限失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/role/menu/batch-set")
    public ResponseResult<Void> batchSetRoleMenus(@RequestBody Map<String, Object> params) {
        permissionGuard.require("shell:setting:permission:role:assign", PermissionGuard.Risk.ASSIGN);
        try {
            Object roleIdObj = params.get("roleId");
            @SuppressWarnings("unchecked")
            List<Integer> menuIds = (List<Integer>) params.get("menuIds");
            
            if (roleIdObj == null) {
                return ResponseResult.fail(400, "角色ID不能为空");
            }
            
            Integer roleId;
            if (roleIdObj instanceof Integer) {
                roleId = (Integer) roleIdObj;
            } else {
                roleId = Integer.valueOf(roleIdObj.toString());
            }
            
            // 先删除角色的所有菜单关联
            roleMenuService.batchDeleteByRoleId(roleId);
            
            // 批量插入新的菜单关联
            if (menuIds != null && !menuIds.isEmpty()) {
                roleMenuService.batchSaveByMenuIds(roleId, menuIds);
            }
            
            // 清除所有用户的菜单缓存
            menuService.clearAllUserMenusCache();
            
            log.info("批量设置角色菜单关联成功: roleId={}, menuIds={}", roleId, menuIds);
            return ResponseResult.success();
        } catch (Exception e) {
            propagateResponseStatus(e);
            log.error("批量设置角色菜单关联失败", e);
            return ResponseResult.fail(500, "批量设置角色菜单关联失败: " + e.getMessage());
        }
    }
    
    /**
     * 清除所有用户的菜单缓存
     * 当菜单权限发生变化后，可以调用此接口清除缓存，使新菜单立即生效
     */
    @PostMapping("/menu/clear-cache")
    public ResponseResult<Void> clearMenuCache() {
        permissionGuard.require("shell:setting:permission:menu:edit", PermissionGuard.Risk.CACHE_CLEAR);
        try {
            log.info("开始清除所有用户菜单缓存...");
            menuService.clearAllUserMenusCache();
            log.info("清除所有用户菜单缓存成功");
            return ResponseResult.success();
        } catch (Exception e) {
            propagateResponseStatus(e);
            log.error("清除用户菜单缓存失败", e);
            return ResponseResult.fail(500, "清除用户菜单缓存失败: " + e.getMessage());
        }
    }

    /**
     * 通用清缓存接口（支持按类型清除不同范围的 Redis 缓存）
     * @param params type: menu=菜单权限缓存, data=业务数据缓存, all=全部缓存
     */
    @PostMapping("/cache/clear")
    public ResponseResult<Void> clearCache(@RequestBody Map<String, Object> params) {
        permissionGuard.require("shell:setting:permission:cache:clear", PermissionGuard.Risk.CACHE_CLEAR);
        String requestedType = params.get("type") != null ? String.valueOf(params.get("type")) : "menu";
        if ("all".equals(requestedType)
                && !"CLEAR_ALL_CACHE".equals(String.valueOf(params.get("confirm")))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "清除全部缓存必须确认 CLEAR_ALL_CACHE");
        }
        try {
            String type = requestedType;
            int clearedCount = 0;

            if (redisCache == null) {
                return ResponseResult.fail(500, "Redis 不可用");
            }

            switch (type) {
                case "menu":
                    // 菜单 + 权限缓存
                    menuService.clearAllUserMenusCache();
                    log.info("已清除菜单/权限缓存");
                    break;

                case "data":
                    // 业务数据缓存（不清除 token/session）
                    clearedCount += clearKeysByPattern("user:menus:*");
                    clearedCount += clearKeysByPattern("user:permissions:*");
                    clearedCount += clearKeysByPattern("merchant:config");
                    clearedCount += clearKeysByPattern("salary:*");
                    clearedCount += clearKeysByPattern("attendance:*");
                    clearedCount += clearKeysByPattern("oa:*");
                    clearedCount += clearKeysByPattern("goods:*");
                    clearedCount += clearKeysByPattern("order:*");
                    clearedCount += clearKeysByPattern("finance:*");
                    clearedCount += clearKeysByPattern("crm:*");
                    clearedCount += clearKeysByPattern("cms:*");
                    clearedCount += clearKeysByPattern("geo:*");
                    clearedCount += clearKeysByPattern("pm:*");
                    clearedCount += clearKeysByPattern("ai:*");
                    clearedCount += clearKeysByPattern("config:*");
                    log.info("已清除业务数据缓存，共 {} 个键", clearedCount);
                    break;

                case "all":
                    // 全部缓存（包括 token，会导致所有用户登出）
                    Collection<String> allKeys = redisCache.keys("*");
                    if (allKeys != null && !allKeys.isEmpty()) {
                        redisCache.deleteObject(allKeys);
                        clearedCount = allKeys.size();
                    }
                    log.info("已清除全部 Redis 缓存，共 {} 个键", clearedCount);
                    break;

                default:
                    return ResponseResult.fail(400, "不支持的缓存类型: " + type);
            }

            return ResponseResult.success();
        } catch (Exception e) {
            propagateResponseStatus(e);
            log.error("清除缓存失败", e);
            return ResponseResult.fail(500, "清除缓存失败: " + e.getMessage());
        }
    }

    /**
     * 按模式清除 Redis 键，返回清除数量
     */
    private int clearKeysByPattern(String pattern) {
        try {
            Collection<String> keys = redisCache.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisCache.deleteObject(keys);
                return keys.size();
            }
        } catch (Exception e) {
            log.warn("清除缓存模式 {} 失败: {}", pattern, e.getMessage());
        }
        return 0;
    }

    /** 解析 roleIds（兼容 Integer / Long / String） */
    private static List<Integer> parseRoleIdsParam(Object raw) {
        List<Integer> roleIds = new java.util.ArrayList<>();
        if (!(raw instanceof List<?> list)) {
            return roleIds;
        }
        for (Object item : list) {
            if (item == null) {
                continue;
            }
            if (item instanceof Number n) {
                roleIds.add(n.intValue());
            } else {
                roleIds.add(Integer.parseInt(item.toString().trim()));
            }
        }
        return roleIds;
    }

    private boolean isMenuStatusChange(MenuUpdateRequest request) {
        if (request.getId() == null) {
            return true;
        }
        try {
            MenuVO current = menuService.getMenuDetail(request.getId());
            return current == null || !Objects.equals(current.getIsDisable(), request.getIsDisable());
        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE, "无法确认菜单原状态", exception);
        }
    }

    private boolean isUserStatusChange(String userId, Integer targetStatus) {
        if (userId == null || userId.isBlank()) {
            return true;
        }
        try {
            UserVO current = userService.getUserDetail(userId);
            return current == null || !Objects.equals(current.getStatus(), targetStatus);
        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE, "无法确认用户原状态", exception);
        }
    }

    private static void propagateResponseStatus(Exception exception) {
        if (exception instanceof ResponseStatusException responseStatusException) {
            throw responseStatusException;
        }
    }
}
