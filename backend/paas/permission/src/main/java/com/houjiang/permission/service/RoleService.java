package com.houjiang.permission.service;

import com.houjiang.permission.dto.PageRequest;
import com.houjiang.permission.dto.RoleCreateRequest;
import com.houjiang.permission.dto.RoleUpdateRequest;
import com.houjiang.permission.vo.PageResult;
import com.houjiang.permission.vo.RoleVO;

import java.util.List;

/**
 * 角色服务接口
 * 
 * @author Amos
 * @since 2025-01-10
 */
public interface RoleService {
    
    /**
     * 获取所有角色
     * 
     * @return 角色列表
     */
    List<RoleVO> getAllRoles();
    
    /**
     * 分页获取角色列表
     * 
     * @param pageRequest 分页请求
     * @return 分页结果
     */
    PageResult<RoleVO> getRoleList(PageRequest pageRequest);
    
    /**
     * 获取角色详情
     * 
     * @param id 角色ID
     * @return 角色详情
     */
    RoleVO getRoleDetail(Integer id);
    
    /**
     * 创建角色
     * 
     * @param request 创建请求
     */
    void createRole(RoleCreateRequest request);
    
    /**
     * 更新角色
     * 
     * @param request 更新请求
     */
    void updateRole(RoleUpdateRequest request);
    
    /**
     * 删除角色
     * 
     * @param id 角色ID
     */
    void deleteRole(Integer id);
    
    /**
     * 根据用户ID获取角色名称列表
     * 
     * @param userId 用户ID
     * @return 角色名称列表
     */
    List<String> getRoleNamesByUserId(String userId);
}
