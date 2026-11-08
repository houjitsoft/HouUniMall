package com.houjiang.userms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.userms.entity.UserOrganizationEntity;
import com.houjiang.commons.util.ResponseResult;

import java.util.Map;

/**
 * @ClassName UserOrganizationService
 * @Description 用户组织关联服务接口
 * @Author Amos
 * @Date 2024-12-19
 * @Version 1.0
 * @author Amos
 */
public interface UserOrganizationService extends IService<UserOrganizationEntity> {

    /**
     * 创建用户组织关联关系
     * @param userOrgData 用户组织关联数据
     * @return 创建结果
     */
    ResponseResult createUserOrganization(Map<String, Object> userOrgData);

    /**
     * 根据用户ID查询用户的所有组织
     * @param userId 用户ID
     * @return 组织列表
     */
    ResponseResult getUserOrganizations(String userId);

    /**
     * 检查用户是否已关联指定组织
     * @param userId 用户ID
     * @param orgId 组织ID
     * @return 是否已关联
     */
    ResponseResult checkUserOrgExists(String userId, String orgId);
}
