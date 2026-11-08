package com.houjiang.userms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.userms.entity.UserDeptEntity;

import java.util.List;
import java.util.Map;

/**
 * 部门管理服务接口
 * 
 * @author Amos
 * @since 2025-01-22
 */
public interface UserDeptService extends IService<UserDeptEntity> {

    /**
     * 根据当前登录用户的org_id获取部门列表
     * 
     * @return 部门列表
     */
    ResponseResult getDeptListByCurrentUser();

    /**
     * 根据指定org_id获取部门列表
     * 
     * @param orgId 组织ID
     * @return 部门列表
     */
    ResponseResult getDeptListByOrgId(Long orgId);

    /**
     * 根据当前登录用户的org_id获取部门树结构
     * 包含部门详细信息，通过关联hou_base_department表获取完整信息
     * 
     * @return 部门树结构
     */
    ResponseResult getDeptTreeByCurrentUser();

    /**
     * 根据指定org_id获取部门树结构
     * 
     * @param orgId 组织ID
     * @return 部门树结构
     */
    ResponseResult getDeptTreeByOrgId(Long orgId);

    /**
     * 根据部门ID获取部门详细信息
     * 
     * @param deptId 部门ID
     * @return 部门详细信息
     */
    ResponseResult getDeptDetailById(Long deptId);

    /**
     * 构建部门树结构
     * 
     * @param deptList 部门列表
     * @return 树形结构的部门列表
     */
    List<Map<String, Object>> buildDeptTree(List<Map<String, Object>> deptList);
}
