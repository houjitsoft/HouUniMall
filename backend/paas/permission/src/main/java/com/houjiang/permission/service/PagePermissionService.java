package com.houjiang.permission.service;

import java.util.List;
import java.util.Map;

/**
 * 页面权限验证服务接口
 * 
 * @author Amos
 * @date 2024-09-26
 */
public interface PagePermissionService {
    
    /**
     * 检查用户是否有页面访问权限
     * 
     * @param userId 用户ID
     * @param pagePath 页面路径（如：/oa/attendance/list）
     * @return 是否有权限
     */
    boolean checkPageAccess(Long userId, String pagePath);
    
    /**
     * 获取用户在指定页面的按钮权限列表
     * 
     * @param userId 用户ID
     * @param pagePath 页面路径
     * @return 按钮权限列表（权限编码列表）
     */
    List<String> getPageButtonPermissions(Long userId, String pagePath);
    
    /**
     * 批量检查用户的按钮权限
     * 
     * @param userId 用户ID
     * @param buttonCodes 需要检查的按钮权限编码列表
     * @return 权限检查结果 Map<按钮编码, 是否有权限>
     */
    Map<String, Boolean> batchCheckButtonPermissions(Long userId, List<String> buttonCodes);
    
    /**
     * 检查单个按钮权限
     * 
     * @param userId 用户ID
     * @param buttonCode 按钮权限编码（如：oa:attendance:add）
     * @return 是否有权限
     */
    boolean checkButtonPermission(Long userId, String buttonCode);
    
    /**
     * 获取用户所有页面权限
     * 
     * @param userId 用户ID
     * @return 页面权限列表
     */
    List<String> getAllPagePermissions(Long userId);
    
    /**
     * 获取用户所有按钮权限
     * 
     * @param userId 用户ID
     * @return 按钮权限列表
     */
    List<String> getAllButtonPermissions(Long userId);
}