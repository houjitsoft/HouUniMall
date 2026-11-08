package com.houjiang.userms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.userms.entity.UserOrganizationEntity;
import com.houjiang.userms.mapper.UserOrganizationMapper;
import com.houjiang.userms.mapper.PmUserSyncMapper;
import com.houjiang.userms.service.UserOrganizationService;
import com.houjiang.commons.util.ResponseResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @ClassName UserOrganizationServiceImpl
 * @Description 用户组织关联服务实现类
 * @Author Amos
 * @Date 2024-12-19
 * @Version 1.0
 * @author Amos
 */
@Service
public class UserOrganizationServiceImpl extends ServiceImpl<UserOrganizationMapper, UserOrganizationEntity> implements UserOrganizationService {

    private static final Logger logger = LoggerFactory.getLogger(UserOrganizationServiceImpl.class);

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private PmUserSyncMapper pmUserSyncMapper;

    @Override
    public ResponseResult createUserOrganization(Map<String, Object> userOrgData) {
        try {
            String userId = (String) userOrgData.get("userId");
            String orgId = (String) userOrgData.get("orgId");
            Integer isPrimary = (Integer) userOrgData.get("isPrimary");
            
            if (userId == null || orgId == null) {
                return ResponseResult.fail("用户ID和组织ID不能为空");
            }
            
            // 检查是否已存在关联关系
            LambdaQueryWrapper<UserOrganizationEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(UserOrganizationEntity::getUserId, userId)
                       .eq(UserOrganizationEntity::getOrgId, orgId);
            UserOrganizationEntity existing = this.getOne(queryWrapper);
            
            if (existing != null) {
                // 即使关系已存在也尝试修复 PM 映射，兼容历史上只写入通用组织表的数据。
                syncPmUser(userId);
                return ResponseResult.fail("用户已关联该组织");
            }
            
            // 如果设置为主组织，需要将其他组织的主组织标记取消
            if (isPrimary != null && isPrimary == 1) {
                LambdaQueryWrapper<UserOrganizationEntity> updateWrapper = new LambdaQueryWrapper<>();
                updateWrapper.eq(UserOrganizationEntity::getUserId, userId)
                            .eq(UserOrganizationEntity::getIsPrimary, 1);
                List<UserOrganizationEntity> primaryOrgs = this.list(updateWrapper);
                for (UserOrganizationEntity org : primaryOrgs) {
                    org.setIsPrimary(0);
                    this.updateById(org);
                }
            }
            
            // 如果用户没有组织，自动设置为主组织
            if (isPrimary == null) {
                LambdaQueryWrapper<UserOrganizationEntity> countWrapper = new LambdaQueryWrapper<>();
                countWrapper.eq(UserOrganizationEntity::getUserId, userId);
                long count = this.count(countWrapper);
                isPrimary = count == 0 ? 1 : 0;
            }
            
            // 创建关联关系
            UserOrganizationEntity entity = new UserOrganizationEntity();
            entity.setUserId(userId);
            entity.setOrgId(orgId);
            entity.setIsPrimary(isPrimary);
            entity.setCreateTime(new Date());
            
            logger.info("准备保存用户组织关联：userId={}, orgId={}, isPrimary={}", userId, orgId, isPrimary);
            boolean success = this.save(entity);
            logger.info("保存用户组织关联结果：success={}", success);
            
            if (success) {
                logger.info("用户组织关联创建成功：userId={}, orgId={}, isPrimary={}, entityId={}", 
                        userId, orgId, isPrimary, entity.getId());
                syncPmUser(userId);
                return ResponseResult.success("组织绑定成功");
            } else {
                logger.error("用户组织关联创建失败：userId={}, orgId={}, isPrimary={}", userId, orgId, isPrimary);
                return ResponseResult.fail("组织绑定失败");
            }
        } catch (Exception e) {
            logger.error("创建用户组织关联关系异常", e);
            return ResponseResult.fail("系统异常，请稍后重试：" + e.getMessage());
        }
    }

    /**
     * 将通用组织成员关系同步到 PM 用户映射。
     * 同步是幂等且尽力而为的：PM 表异常不能影响通用组织绑定结果，
     * PM 请求上下文还会在首次访问时再次执行一次自愈同步。
     */
    private void syncPmUser(String userId) {
        if (pmUserSyncMapper == null || userId == null || userId.isBlank()) {
            return;
        }
        try {
            List<UserOrganizationEntity> memberships = this.list(new LambdaQueryWrapper<UserOrganizationEntity>()
                    .eq(UserOrganizationEntity::getUserId, userId));
            Set<String> orgIds = new LinkedHashSet<>();
            for (UserOrganizationEntity membership : memberships) {
                if (membership.getOrgId() != null && !membership.getOrgId().isBlank()) {
                    orgIds.add(membership.getOrgId().trim());
                }
            }
            if (orgIds.isEmpty()) {
                return;
            }
            String orgIdValue = String.join(",", orgIds);
            Integer pmUserId = pmUserSyncMapper.selectPmUserIdByUserId(userId);
            if (pmUserId == null) {
                pmUserSyncMapper.insertPmUser(userId, orgIdValue);
                logger.info("已创建 PM 用户映射：userId={}, orgIds={}", userId, orgIdValue);
            } else {
                pmUserSyncMapper.updatePmUserOrgIds(userId, orgIdValue);
                logger.info("已同步 PM 用户组织缓存：userId={}, orgIds={}", userId, orgIdValue);
            }
        } catch (Exception e) {
            logger.warn("同步 PM 用户映射失败，不影响组织绑定：userId={}", userId, e);
        }
    }

    @Override
    public ResponseResult getUserOrganizations(String userId) {
        try {
            if (userId == null) {
                return ResponseResult.fail("用户ID不能为空");
            }
            
            LambdaQueryWrapper<UserOrganizationEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(UserOrganizationEntity::getUserId, userId)
                       .orderByDesc(UserOrganizationEntity::getIsPrimary)
                       .orderByDesc(UserOrganizationEntity::getCreateTime);
            
            List<UserOrganizationEntity> orgList = this.list(queryWrapper);
            
            List<Map<String, Object>> result = new ArrayList<>();
            for (UserOrganizationEntity entity : orgList) {
                Map<String, Object> org = new LinkedHashMap<>();
                org.put("org_id", entity.getOrgId());
                org.put("is_primary", entity.getIsPrimary());
                org.put("create_time", entity.getCreateTime());
                result.add(org);
            }
            
            return ResponseResult.success(result);
        } catch (Exception e) {
            logger.error("查询用户组织列表异常", e);
            return ResponseResult.fail("查询失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseResult checkUserOrgExists(String userId, String orgId) {
        try {
            if (userId == null || orgId == null) {
                return ResponseResult.fail("用户ID和组织ID不能为空");
            }
            
            LambdaQueryWrapper<UserOrganizationEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(UserOrganizationEntity::getUserId, userId)
                       .eq(UserOrganizationEntity::getOrgId, orgId);
            
            long count = this.count(queryWrapper);
            return ResponseResult.success(count > 0);
        } catch (Exception e) {
            logger.error("检查用户组织关联关系异常", e);
            return ResponseResult.fail("检查失败：" + e.getMessage());
        }
    }
}
