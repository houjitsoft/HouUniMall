package com.houjiang.permission.controller;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.permission.security.PermissionIdentity;
import com.houjiang.permission.service.PagePermissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

/**
 * 页面权限验证控制器
 * 专门用于前端页面权限验证，包括页面访问权限和页面内操作按钮权限
 * 
 * @author Amos
 * @since 2025-01-12
 */
@Slf4j
@RestController
@RequestMapping("/permission/page")
public class PagePermissionController {
    
    @Autowired
    private PagePermissionService pagePermissionService;

    @Autowired
    private PermissionIdentity permissionIdentity;
    
    /**
     * 验证用户是否有访问指定页面的权限
     * @param request 请求参数：{userId, pagePath}
     * @return 是否有权限访问页面
     */
    @PostMapping("/access/check")
    public ResponseResult<Boolean> checkPageAccess(@RequestBody Map<String, Object> request) {
        Long userId = permissionIdentity.requireSelf(request.get("userId"));
        try {
            String pagePath = (String) request.get("pagePath");
            if (pagePath == null) {
                return ResponseResult.fail(400, "页面路径不能为空");
            }
            
            boolean hasAccess = pagePermissionService.checkPageAccess(userId, pagePath);
            log.info("页面访问权限检查: userId={}, pagePath={}, hasAccess={}", userId, pagePath, hasAccess);
            
            return ResponseResult.success(hasAccess);
        } catch (Exception e) {
            propagateResponseStatus(e);
            log.error("检查页面访问权限失败", e);
            return ResponseResult.fail(500, "检查页面访问权限失败");
        }
    }
    
    /**
     * 获取用户在指定页面的操作按钮权限列表
     * @param request 请求参数：{userId, pagePath}
     * @return 用户在该页面的按钮权限列表
     */
    @PostMapping("/buttons")
    public ResponseResult<List<String>> getPageButtonPermissions(@RequestBody Map<String, Object> request) {
        Long userId = permissionIdentity.requireSelf(request.get("userId"));
        try {
            String pagePath = (String) request.get("pagePath");
            if (pagePath == null) {
                return ResponseResult.fail(400, "页面路径不能为空");
            }
            
            List<String> buttonPermissions = pagePermissionService.getPageButtonPermissions(userId, pagePath);
            log.info("获取页面按钮权限: userId={}, pagePath={}, permissions={}", userId, pagePath, buttonPermissions);
            
            return ResponseResult.success(buttonPermissions);
        } catch (Exception e) {
            propagateResponseStatus(e);
            log.error("获取页面按钮权限失败", e);
            return ResponseResult.fail(500, "获取页面按钮权限失败");
        }
    }
    
    /**
     * 批量检查用户的按钮权限
     * @param request 请求参数：{userId, permissions[]}
     * @return 权限检查结果映射
     */
    @PostMapping("/buttons/batch-check")
    public ResponseResult<Map<String, Boolean>> batchCheckButtonPermissions(@RequestBody Map<String, Object> request) {
        Long userId = permissionIdentity.requireSelf(request.get("userId"));
        try {
            @SuppressWarnings("unchecked")
            List<String> permissions = (List<String>) request.get("permissions");
            if (permissions == null || permissions.isEmpty()) {
                return ResponseResult.fail(400, "权限列表不能为空");
            }
            
            Map<String, Boolean> result = pagePermissionService.batchCheckButtonPermissions(userId, permissions);
            log.info("批量检查按钮权限: userId={}, permissions={}, result={}", userId, permissions, result);
            
            return ResponseResult.success(result);
        } catch (Exception e) {
            propagateResponseStatus(e);
            log.error("批量检查按钮权限失败", e);
            return ResponseResult.fail(500, "批量检查按钮权限失败");
        }
    }

    private static void propagateResponseStatus(Exception exception) {
        if (exception instanceof ResponseStatusException responseStatusException) {
            throw responseStatusException;
        }
    }
}
