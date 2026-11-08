package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 权限服务Feign客户端降级处理
/**
 * 
 * @author Amos
 * @since 2025-01-07
 */
@Component
public class PermissionServiceFeignFallback implements PermissionServiceFeign {

    @Override
    public ResponseResult<List<Map<String, Object>>> getUserMenus(Map<String, Object> request) {
        return ResponseResult.fail(503, "权限服务暂时不可用");
    }

    @Override
    public ResponseResult<List<Map<String, Object>>> getSubMenuTree(Map<String, Object> request) {
        return ResponseResult.fail(503, "权限服务暂时不可用");
    }

    @Override
    public ResponseResult<Boolean> checkUserPermission(Map<String, Object> request) {
        return ResponseResult.fail(503, "权限服务暂时不可用");
    }

    @Override
    public ResponseResult<List<String>> getUserPermissions(Map<String, Object> request) {
        return ResponseResult.fail(503, "权限服务暂时不可用");
    }

    @Override
    public ResponseResult<Boolean> checkPageAccess(Map<String, Object> request) {
        return ResponseResult.fail(503, "权限服务暂时不可用");
    }

    @Override
    public ResponseResult<List<String>> getPageButtonPermissions(Map<String, Object> request) {
        return ResponseResult.fail(503, "权限服务暂时不可用");
    }

    @Override
    public ResponseResult<Map<String, Boolean>> batchCheckButtonPermissions(Map<String, Object> request) {
        return ResponseResult.fail(503, "权限服务暂时不可用");
    }

    @Override
    public ResponseResult getAllMenus() {
        return ResponseResult.fail(503, "权限服务暂时不可用");
    }

    @Override
    public ResponseResult getMenuTreeForRole() {
        return ResponseResult.fail(503, "权限服务暂时不可用");
    }

    @Override
    public ResponseResult getMenusByRole(Integer roleId) {
        return ResponseResult.fail(503, "权限服务暂时不可用");
    }

    @Override
    public ResponseResult addMenu(Map<String, Object> request) {
        return ResponseResult.fail(503, "权限服务暂时不可用");
    }

    @Override
    public ResponseResult editMenu(Map<String, Object> request) {
        return ResponseResult.fail(503, "权限服务暂时不可用");
    }

    @Override
    public ResponseResult deleteMenu(Map<String, Object> request) {
        return ResponseResult.fail(503, "权限服务暂时不可用");
    }

    @Override
    public ResponseResult getMenuDetail(Integer id) {
        return ResponseResult.fail(503, "权限服务暂时不可用");
    }

    @Override
    public ResponseResult updateMenuDisplay(Map<String, Object> request) {
        return ResponseResult.fail(503, "权限服务暂时不可用");
    }

    @Override
    public ResponseResult getAddonCateList() {
        return ResponseResult.fail(503, "权限服务暂时不可用");
    }
    
    /** ==================== 角色管理接口降级处理 ==================== */
    
    @Override
    public ResponseResult getAllRoles() {
        return ResponseResult.fail(503, "权限服务暂时不可用");
    }
    
    @Override
    public ResponseResult getRoleList(Map<String, Object> request) {
        return ResponseResult.fail(503, "权限服务暂时不可用");
    }
    
    @Override
    public ResponseResult getRoleDetail(Integer id) {
        return ResponseResult.fail(503, "权限服务暂时不可用");
    }
    
    @Override
    public ResponseResult createRole(Map<String, Object> request) {
        return ResponseResult.fail(503, "权限服务暂时不可用");
    }
    
    @Override
    public ResponseResult updateRole(Map<String, Object> request) {
        return ResponseResult.fail(503, "权限服务暂时不可用");
    }
    
    @Override
    public ResponseResult deleteRole(Map<String, Object> request) {
        return ResponseResult.fail(503, "权限服务暂时不可用");
    }
    
    @Override
    public ResponseResult getRoleNamesByUserId(String userId) {
        return ResponseResult.fail(503, "权限服务暂时不可用");
    }
    
    /** ==================== 用户管理接口降级处理 ==================== */
    
    @Override
    public ResponseResult getUserList(Map<String, Object> request) {
        return ResponseResult.fail(503, "权限服务暂时不可用");
    }
    
    @Override
    public ResponseResult getUserDetail(String id) {
        return ResponseResult.fail(503, "权限服务暂时不可用");
    }
    
    @Override
    public ResponseResult createUser(Map<String, Object> request) {
        return ResponseResult.fail(503, "权限服务暂时不可用");
    }
    
    @Override
    public ResponseResult updateUser(Map<String, Object> request) {
        return ResponseResult.fail(503, "权限服务暂时不可用");
    }
    
    @Override
    public ResponseResult deleteUser(Map<String, Object> request) {
        return ResponseResult.fail(503, "权限服务暂时不可用");
    }
    
    @Override
    public ResponseResult resetPassword(Map<String, Object> request) {
        return ResponseResult.fail(503, "权限服务暂时不可用");
    }
    
    @Override
    public ResponseResult updateUserStatus(Map<String, Object> request) {
        return ResponseResult.fail(503, "权限服务暂时不可用");
    }
    
    /** ==================== 用户角色管理接口 ==================== */
    
    @Override
    public ResponseResult<List<Integer>> getUserRoles(Map<String, Object> request) {
        return ResponseResult.fail(503, "权限服务暂时不可用");
    }
    
    @Override
    public ResponseResult<Void> setUserRoles(Map<String, Object> request) {
        return ResponseResult.fail(503, "权限服务暂时不可用");
    }
    
    @Override
    public ResponseResult<List<Integer>> getRoleUsers(Map<String, Object> request) {
        return ResponseResult.fail(503, "权限服务暂时不可用");
    }
    
    @Override
    public ResponseResult<Void> batchSetRoleMenus(Map<String, Object> request) {
        return ResponseResult.fail(503, "权限服务暂时不可用");
    }
    
    @Override
    public ResponseResult<Void> fixAdminPermissions(Map<String, Object> request) {
        return ResponseResult.fail(503, "权限服务暂时不可用");
    }
    
    @Override
    public ResponseResult<Void> clearMenuCache() {
        return ResponseResult.fail(503, "权限服务暂时不可用");
    }

    @Override
    public ResponseResult<Void> clearCache(Map<String, Object> params) {
        return ResponseResult.fail(503, "权限服务暂时不可用");
    }
}
