package com.houjiang.userms.service.impl;

import com.houjiang.commons.util.RequestTokenUtils;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.userms.feign.BaseServiceClient;
import com.houjiang.userms.service.CompanyRegistrationService;
import com.houjiang.userms.service.SessionOrganizationSync;
import com.houjiang.userms.service.UserOrganizationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 公司登记服务实现类
 * @author Amos
 * @date 2025/01/08
 */
@Slf4j
@Service
public class CompanyRegistrationServiceImpl implements CompanyRegistrationService {
    
    @Autowired
    private BaseServiceClient baseServiceClient;
    
    @Autowired
    private UserOrganizationService userOrganizationService;

    @Autowired
    private SessionOrganizationSync sessionOrganizationSync;
    
    @Override
    public ResponseResult submitCompanyRegistration(Map<String, Object> companyData) {
        try {
            log.info("处理公司登记申请：{}", companyData);
            
            // 从token中获取用户ID
            String userId = RequestTokenUtils.getUserIdFromRequest();
            if (userId == null) {
                log.error("用户未登录，token解析失败");
                return ResponseResult.fail("用户未登录");
            }
            
            // 检查用户已登记的公司数量，最大允许10家
            ResponseResult existingOrgsResult = userOrganizationService.getUserOrganizations(userId);
            int orgCount = 0;
            if (existingOrgsResult.getCode() == 1 && existingOrgsResult.getData() != null) {
                Object data = existingOrgsResult.getData();
                if (data instanceof List) {
                    orgCount = ((List<?>) data).size();
                } else if (data instanceof Map) {
                    orgCount = ((Map<?, ?>) data).size();
                }
            }
            
            if (orgCount >= 10) {
                log.warn("用户{}尝试登记第{}家公司，超过最大允许数量10家", userId, orgCount + 1);
                return ResponseResult.fail("最大允许登记公司数为10家");
            }
            
            log.info("用户{}当前已有{}家公司，可以继续登记", userId, orgCount);
            
            // 1. 先通过Feign调用base服务，检查组织是否存在，如果存在则更新，不存在则创建
            ResponseResult orgResult = baseServiceClient.submitCompanyRegistration(companyData);
            if (orgResult.getCode() != 1) {
                return orgResult;
            }
            
            // 2. 获取返回的org_id
            Map<String, Object> orgData = (Map<String, Object>) orgResult.getData();
            Object orgIdObj = orgData.get("orgId");
            String orgId = null;
            if (orgIdObj != null) {
                // 支持Integer和String类型
                orgId = String.valueOf(orgIdObj);
            }
            if (orgId == null || orgId.isEmpty()) {
                return ResponseResult.fail("创建组织失败，未获取到组织ID");
            }
            
            Boolean isNewOrg = (Boolean) orgData.get("isNewOrg");
            if (isNewOrg == null) {
                // 默认为新组织
                isNewOrg = true;
            }
            log.info("组织处理结果 - orgId: {}, isNewOrg: {}", orgId, isNewOrg);
            
            // 3. 检查用户是否已经关联了该组织
            ResponseResult checkUserOrgResult = userOrganizationService.checkUserOrgExists(userId, orgId);
            boolean userAlreadyLinked = false;
            if (checkUserOrgResult.getCode() == 1 && checkUserOrgResult.getData() != null) {
                Object data = checkUserOrgResult.getData();
                if (data instanceof Boolean) {
                    userAlreadyLinked = (Boolean) data;
                } else {
                    // 如果返回的是其他类型，尝试转换为boolean
                    userAlreadyLinked = Boolean.parseBoolean(String.valueOf(data));
                }
            }
            
            if (userAlreadyLinked) {
                log.warn("用户{}已经关联了组织{}，无需重复关联", userId, orgId);
                // 用户已关联该组织，直接返回成功
                ResponseResult updatedUserOrgResult = userOrganizationService.getUserOrganizations(userId);
                Map<String, Object> resultData = new HashMap<>();
                resultData.put("orgId", orgId);
                resultData.put("orgList", updatedUserOrgResult.getData());
                syncSessionOrgs(userId, orgId, updatedUserOrgResult.getData());
                return ResponseResult.success("您已关联该组织", resultData);
            }
            
            // 4. 将用户和组织的关联关系插入到hou_user_organization表
            // 检查用户是否已有组织，如果没有组织，则第一条登记的组织is_primary=1
            log.info("开始创建用户组织关联，userId: {}, orgId: {}", userId, orgId);
            
            // 查询用户是否已有组织（复用之前查询的结果）
            boolean hasExistingOrgs = (orgCount > 0);
            
            // 如果用户没有组织，则第一条登记的组织is_primary=1；如果已有组织，新组织is_primary=0
            int isPrimary = hasExistingOrgs ? 0 : 1;
            log.info("用户{}已有组织: {}, 设置新组织is_primary={}", userId, hasExistingOrgs, isPrimary);
            
            Map<String, Object> userOrgData = new HashMap<>();
            userOrgData.put("userId", userId);
            userOrgData.put("orgId", orgId);
            userOrgData.put("isPrimary", isPrimary);
            
            log.info("调用createUserOrganization，参数: {}", userOrgData);
            ResponseResult createUserOrgResult = userOrganizationService.createUserOrganization(userOrgData);
            log.info("createUserOrganization返回结果: code={}, message={}, data={}", 
                    createUserOrgResult.getCode(), createUserOrgResult.getMessage(), createUserOrgResult.getData());
            
            if (createUserOrgResult.getCode() != 1) {
                // 如果创建用户组织关联失败，且是新创建的组织，需要回滚组织创建
                if (isNewOrg) {
                    log.error("创建用户组织关联失败，需要回滚组织创建，orgId: {}, 错误信息: {}", 
                            orgId, createUserOrgResult.getMessage());
                    try {
                        baseServiceClient.deleteOrganization(orgId);
                        log.info("已回滚组织创建，删除组织ID: {}", orgId);
                    } catch (Exception e) {
                        log.error("回滚组织创建失败，orgId: {}", orgId, e);
                    }
                } else {
                    log.error("创建用户组织关联失败，但组织已存在，无需回滚，orgId: {}, 错误信息: {}", 
                            orgId, createUserOrgResult.getMessage());
                }
                return ResponseResult.fail("创建用户组织关联失败: " + createUserOrgResult.getMessage());
            }
            
            log.info("用户组织关联创建成功，userId: {}, orgId: {}", userId, orgId);
            
            // 4. 获取更新后的用户组织信息，供前端更新缓存
            ResponseResult updatedUserOrgResult = userOrganizationService.getUserOrganizations(userId);
            Map<String, Object> resultData = new HashMap<>();
            resultData.put("orgId", orgId);
            resultData.put("orgList", updatedUserOrgResult.getData());
            // 同步写回 Redis 会话，避免前端已有组织、后端会话仍为空导致智能入库 422
            syncSessionOrgs(userId, orgId, updatedUserOrgResult.getData());
            
            log.info("用户{}公司登记申请提交成功，组织ID：{}", userId, orgId);
            return ResponseResult.success("公司登记申请提交成功，请等待审核", resultData);
            
        } catch (Exception e) {
            log.error("提交公司登记申请异常", e);
            log.error("异常堆栈信息：", e);
            return ResponseResult.fail("系统异常，请稍后重试：" + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void syncSessionOrgs(String userId, String orgId, Object orgListData) {
        List<Map<String, Object>> orgList = null;
        if (orgListData instanceof List<?> raw) {
            orgList = (List<Map<String, Object>>) raw;
        }
        sessionOrganizationSync.syncCurrentSession(userId, orgId, orgList);
    }
}
