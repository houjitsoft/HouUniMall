package com.houjiang.base.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.houjiang.base.dto.OrganizationDTO;
import com.houjiang.base.entity.OrganizationEntity;
import com.houjiang.base.mapper.OrganizationMapper;
import com.houjiang.base.service.OrganizationService;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.SnowflakeIdWorker;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import com.houjiang.commons.util.RequestTokenUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * @ClassName OrganizationServiceImpl
 * @Description 组织信息服务实现类
 * @Author Amos
 * @Date 2024-12-19
 * @Version 1.0
 * @author Amos
 */
@Slf4j
@Service
public class OrganizationServiceImpl extends ServiceImpl<OrganizationMapper, OrganizationEntity> implements OrganizationService {

    /**
     * 组织类型：个人空间。与公司(1)/部门(2)/小组(3)区分，注册用户默认归属。
     */
    private static final int ORG_TYPE_PERSONAL = 4;

    /**
     * 个人空间组织编码前缀，按 userId 幂等，重复调用不会重复建组织。
     */
    private static final String PERSONAL_ORG_CODE_PREFIX = "PERSONAL_";

    @Autowired
    private OrganizationMapper organizationMapper;

    @Override
    public ResponseResult queryOrganizationPage(OrganizationDTO dto) {
        try {
            Page<OrganizationEntity> page = new Page<>(dto.getPageNum(), dto.getPageSize());
            IPage<OrganizationEntity> result = organizationMapper.selectOrganizationPage(page, dto);
            return ResponseResult.success(result);
        } catch (Exception e) {
            return ResponseResult.error("查询组织信息失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseResult queryOrganizationTree(OrganizationDTO dto) {
        try {
            List<OrganizationEntity> list = organizationMapper.selectOrganizationTree(dto);
            return ResponseResult.success(list);
        } catch (Exception e) {
            return ResponseResult.error("查询组织树失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseResult queryOrganizationById(String orgId) {
        try {
            if (StrUtil.isBlank(orgId)) {
                return ResponseResult.error("组织ID不能为空");
            }
            
            LambdaQueryWrapper<OrganizationEntity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(OrganizationEntity::getOrgId, orgId);
            
            OrganizationEntity entity = this.getOne(wrapper);
            if (entity == null) {
                return ResponseResult.error("组织信息不存在");
            }
            
            return ResponseResult.success(entity);
        } catch (Exception e) {
            return ResponseResult.error("查询组织信息失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult addOrganization(OrganizationDTO dto) {
        try {
            // 参数校验
            if (StrUtil.isBlank(dto.getOrgName())) {
                return ResponseResult.error("组织名称不能为空");
            }
            if (StrUtil.isBlank(dto.getOrgCode())) {
                return ResponseResult.error("组织编码不能为空");
            }
            
            // 检查组织编码是否已存在
            int codeCount = organizationMapper.checkOrgCodeExists(dto.getOrgCode(), null);
            if (codeCount > 0) {
                return ResponseResult.error("组织编码已存在");
            }
            
            // 检查组织名称是否已存在
            int nameCount = organizationMapper.checkOrgNameExists(dto.getOrgName(), null);
            if (nameCount > 0) {
                return ResponseResult.error("组织名称已存在");
            }
            
            // 构建实体
            OrganizationEntity entity = new OrganizationEntity();
            BeanUtils.copyProperties(dto, entity);
            
            // 设置默认值（orgId使用自增，不需要手动设置）
            entity.setOrgStatus(dto.getOrgStatus() != null ? dto.getOrgStatus() : 1);
            entity.setCreateTime(new Date());
            entity.setUpdateTime(new Date());
            
            // 保存
            boolean success = this.save(entity);
            if (success) {
                return ResponseResult.success("新增组织成功");
            } else {
                return ResponseResult.error("新增组织失败");
            }
        } catch (Exception e) {
            return ResponseResult.error("新增组织失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult updateOrganization(OrganizationDTO dto) {
        try {
            if (StrUtil.isBlank(dto.getOrgId())) {
                return ResponseResult.error("组织ID不能为空");
            }
            
            // 检查组织是否存在
            LambdaQueryWrapper<OrganizationEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(OrganizationEntity::getOrgId, dto.getOrgId());
            OrganizationEntity existEntity = this.getOne(queryWrapper);
            if (existEntity == null) {
                return ResponseResult.error("组织信息不存在");
            }
            
            // 检查组织编码是否已存在（排除自己）
            if (StrUtil.isNotBlank(dto.getOrgCode())) {
                int codeCount = organizationMapper.checkOrgCodeExists(dto.getOrgCode(), dto.getOrgId());
                if (codeCount > 0) {
                    return ResponseResult.error("组织编码已存在");
                }
            }
            
            // 检查组织名称是否已存在（排除自己）
            if (StrUtil.isNotBlank(dto.getOrgName())) {
                int nameCount = organizationMapper.checkOrgNameExists(dto.getOrgName(), dto.getOrgId());
                if (nameCount > 0) {
                    return ResponseResult.error("组织名称已存在");
                }
            }
            
            // 更新实体
            OrganizationEntity entity = new OrganizationEntity();
            BeanUtils.copyProperties(dto, entity);
            entity.setOrgId(existEntity.getOrgId());
            entity.setUpdateTime(new Date());
            
            // 更新
            boolean success = this.updateById(entity);
            if (success) {
                return ResponseResult.success("更新组织成功");
            } else {
                return ResponseResult.error("更新组织失败");
            }
        } catch (Exception e) {
            return ResponseResult.error("更新组织失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult deleteOrganization(String orgId) {
        try {
            if (StrUtil.isBlank(orgId)) {
                return ResponseResult.error("组织ID不能为空");
            }
            
            // 检查是否有子组织
            List<OrganizationEntity> children = organizationMapper.selectChildrenByParentId(orgId);
            if (!children.isEmpty()) {
                return ResponseResult.error("该组织下存在子组织，无法删除");
            }
            
            // 硬删除
            LambdaQueryWrapper<OrganizationEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(OrganizationEntity::getOrgId, orgId);
            
            boolean success = this.remove(queryWrapper);
            if (success) {
                return ResponseResult.success("删除组织成功");
            } else {
                return ResponseResult.error("删除组织失败");
            }
        } catch (Exception e) {
            return ResponseResult.error("删除组织失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult batchDeleteOrganization(List<String> orgIds) {
        try {
            if (orgIds == null || orgIds.isEmpty()) {
                return ResponseResult.error("组织ID列表不能为空");
            }
            
            // 检查是否有子组织
            for (String orgId : orgIds) {
                List<OrganizationEntity> children = organizationMapper.selectChildrenByParentId(orgId);
                if (!children.isEmpty()) {
                    return ResponseResult.error("组织ID " + orgId + " 下存在子组织，无法删除");
                }
            }
            
            // 批量硬删除
            LambdaQueryWrapper<OrganizationEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(OrganizationEntity::getOrgId, orgIds);
            
            boolean success = this.remove(queryWrapper);
            if (success) {
                return ResponseResult.success("批量删除组织成功");
            } else {
                return ResponseResult.error("批量删除组织失败");
            }
        } catch (Exception e) {
            return ResponseResult.error("批量删除组织失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult updateOrganizationStatus(String orgId, Integer status) {
        try {
            if (StrUtil.isBlank(orgId)) {
                return ResponseResult.error("组织ID不能为空");
            }
            if (status == null || (status != 0 && status != 1)) {
                return ResponseResult.error("状态值无效");
            }
            
            LambdaUpdateWrapper<OrganizationEntity> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(OrganizationEntity::getOrgId, orgId)
                        .set(OrganizationEntity::getOrgStatus, status)
                        .set(OrganizationEntity::getUpdateTime, new Date());
            
            boolean success = this.update(updateWrapper);
            if (success) {
                String statusText = status == 1 ? "启用" : "禁用";
                return ResponseResult.success(statusText + "组织成功");
            } else {
                return ResponseResult.error("更新组织状态失败");
            }
        } catch (Exception e) {
            return ResponseResult.error("更新组织状态失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseResult checkOrgCodeExists(String orgCode, String orgId) {
        try {
            if (StrUtil.isBlank(orgCode)) {
                return ResponseResult.error("组织编码不能为空");
            }
            
            int count = organizationMapper.checkOrgCodeExists(orgCode, orgId);
            return ResponseResult.success(count > 0);
        } catch (Exception e) {
            return ResponseResult.error("检查组织编码失败：" + e.getMessage());
        }
    }

    @Override
    public ResponseResult checkOrgNameExists(String orgName, String orgId) {
        try {
            if (StrUtil.isBlank(orgName)) {
                return ResponseResult.error("组织名称不能为空");
            }
            
            int count = organizationMapper.checkOrgNameExists(orgName, orgId);
            return ResponseResult.success(count > 0);
        } catch (Exception e) {
            return ResponseResult.error("检查组织名称失败：" + e.getMessage());
        }
    }


    /**
     * 计算组织层级
     *
     * @param parentOrgId 父组织ID
     * @return 组织层级
     */

    @Override
    public ResponseResult submitCompanyRegistration(Map<String, Object> companyData) {
        try {
            log.info("收到公司登记数据: {}", companyData);
            log.info("组织编码字段值 - orgCode: {}, org_code: {}", companyData.get("orgCode"), companyData.get("org_code"));
            
            // 从token中获取用户ID
            String userId = RequestTokenUtils.getUserIdFromRequest();
            if (userId == null) {
                return ResponseResult.error("用户未登录");
            }
            
            String companyName = (String) companyData.get("companyName");
            if (companyName == null || companyName.trim().isEmpty()) {
                return ResponseResult.error("公司名称不能为空");
            }
            
            // 检查组织名称是否已存在
            OrganizationEntity existingOrg = this.lambdaQuery()
                    .eq(OrganizationEntity::getOrgName, companyName.trim())
                    .one();
            
            Integer orgId;
            boolean isNewOrg = false;
            
            if (existingOrg != null) {
                // 组织已存在，更新组织信息
                log.info("组织名称已存在，更新组织信息，orgId: {}, orgName: {}", existingOrg.getOrgId(), companyName);
                orgId = existingOrg.getOrgId();
                
                // 更新组织信息（组织编码、联系电话等）
                boolean needUpdate = false;
                
                // 如果前端传入了组织编码，更新组织编码
                String orgCode = (String) companyData.get("orgCode");
                if (orgCode == null || orgCode.trim().isEmpty()) {
                    orgCode = (String) companyData.get("org_code");
                }
                if (orgCode != null && !orgCode.trim().isEmpty() && !orgCode.trim().equals(existingOrg.getOrgCode())) {
                    existingOrg.setOrgCode(orgCode.trim());
                    needUpdate = true;
                    log.info("更新组织编码: {}", orgCode);
                }
                
                // 更新联系电话
                Object contactPhoneObj = companyData.get("contactPhone");
                String contactPhone = null;
                if (contactPhoneObj != null) {
                    if (contactPhoneObj instanceof String) {
                        contactPhone = (String) contactPhoneObj;
                    } else if (contactPhoneObj instanceof List) {
                        List<?> phoneList = (List<?>) contactPhoneObj;
                        if (!phoneList.isEmpty()) {
                            contactPhone = phoneList.get(0).toString();
                        }
                    } else {
                        contactPhone = contactPhoneObj.toString();
                    }
                }
                if (contactPhone != null && !contactPhone.equals(existingOrg.getContactPhone())) {
                    existingOrg.setContactPhone(contactPhone);
                    needUpdate = true;
                    log.info("更新联系电话: {}", contactPhone);
                }
                
                // 如果组织状态为禁用，更新为启用
                if (existingOrg.getOrgStatus() != null && existingOrg.getOrgStatus() == 0) {
                    existingOrg.setOrgStatus(1);
                    needUpdate = true;
                    log.info("更新组织状态为启用");
                }
                
                // 如果有更新，保存到数据库
                if (needUpdate) {
                    existingOrg.setUpdateTime(new Date());
                    boolean updateSuccess = this.updateById(existingOrg);
                    if (updateSuccess) {
                        log.info("组织信息更新成功，orgId: {}", orgId);
                    } else {
                        log.warn("组织信息更新失败，orgId: {}", orgId);
                    }
                }
            } else {
                // 组织不存在，创建新组织
                isNewOrg = true;
                log.info("组织名称不存在，创建新组织，orgName: {}", companyName);
                
                // 创建组织实体
                OrganizationEntity entity = new OrganizationEntity();
                // orgId 使用自增，不需要手动设置
                entity.setOrgName(companyName.trim());
                
                // 如果前端传入了组织编码，使用前端的；否则使用默认编码（保存后会自动生成）
                // 支持 orgCode 和 org_code 两种字段名
                String orgCode = (String) companyData.get("orgCode");
                if (orgCode == null || orgCode.trim().isEmpty()) {
                    orgCode = (String) companyData.get("org_code");
                }
                if (orgCode != null && !orgCode.trim().isEmpty()) {
                    entity.setOrgCode(orgCode.trim());
                    log.info("使用传入的组织编码: {}", orgCode);
                } else {
                    // 临时设置一个编码，保存后会自动生成正确的编码
                    entity.setOrgCode("TEMP");
                    log.info("未传入组织编码，使用临时编码TEMP");
                }
                
                // 处理联系电话，确保是字符串类型
                Object contactPhoneObj = companyData.get("contactPhone");
                String contactPhone = null;
                if (contactPhoneObj != null) {
                    if (contactPhoneObj instanceof String) {
                        contactPhone = (String) contactPhoneObj;
                    } else if (contactPhoneObj instanceof List) {
                        List<?> phoneList = (List<?>) contactPhoneObj;
                        if (!phoneList.isEmpty()) {
                            contactPhone = phoneList.get(0).toString();
                        }
                    } else {
                        contactPhone = contactPhoneObj.toString();
                    }
                }
                entity.setContactPhone(contactPhone);
                // 1-公司
                entity.setOrgType(1);
                // 1-已启用（创建后立即可用）
                entity.setOrgStatus(1);
                
                // 保存到数据库（orgId会自动生成）
                log.info("保存前 - 组织编码: {}", entity.getOrgCode());
                boolean success = this.save(entity);
                
                if (success) {
                    orgId = entity.getOrgId();
                    log.info("保存后 - 组织ID: {}, 组织编码: {}", orgId, entity.getOrgCode());
                    // 如果组织编码是临时值，更新为基于orgId的编码
                    if ("TEMP".equals(entity.getOrgCode())) {
                        entity.setOrgCode("ORG_" + orgId);
                        this.updateById(entity);
                        log.info("更新组织编码为: {}", entity.getOrgCode());
                    } else {
                        log.info("使用传入的组织编码，不更新: {}", entity.getOrgCode());
                    }
                } else {
                    log.error("用户{}提交公司登记申请失败", userId);
                    return ResponseResult.error("提交失败，请重试");
                }
            }
            
            // 返回组织ID供后续使用（无论是新创建还是已存在的组织）
            Map<String, Object> resultData = new HashMap<>();
            resultData.put("orgId", String.valueOf(orgId));
            resultData.put("isNewOrg", isNewOrg);
            String message = isNewOrg ? "公司登记申请提交成功，请等待审核" : "组织信息已更新，用户关联成功";
            log.info("用户{}提交公司登记申请成功，公司名称：{}，组织ID：{}，是否新组织：{}", userId, companyName, orgId, isNewOrg);
            return ResponseResult.success(message, resultData);
            
        } catch (Exception e) {
            log.error("提交公司登记申请异常", e);
            return ResponseResult.error("系统异常，请稍后重试");
        }
    }

    @Override
    public ResponseResult checkUserCompanyRegistration(String userId) {
        try {
            // 由于数据库表中没有用户ID字段，暂时返回未提交状态
            // 实际业务中应该通过其他方式（如用户-组织关联表）来检查
            return ResponseResult.success("用户未提交过公司登记申请", null);
            
        } catch (Exception e) {
            log.error("检查用户公司登记申请异常", e);
            return ResponseResult.error("系统异常，请稍后重试");
        }
    }

    @Override
    public ResponseResult createUserOrganization(Map<String, Object> userOrgData) {
        try {
            // base服务中不直接操作用户组织关联表
            // 这个接口应该由userms服务提供，base服务通过Feign调用
            // 但为了兼容性，这里保留接口，实际应该重定向到userms服务
            log.warn("base服务的createUserOrganization方法被调用，应该通过userms服务调用");
            return ResponseResult.error("请通过userms服务创建用户组织关联关系");
        } catch (Exception e) {
            log.error("创建用户组织关联关系异常", e);
            return ResponseResult.error("系统异常，请稍后重试");
        }
    }

    @Override
    public ResponseResult ensurePersonalOrganization(Map<String, Object> data) {
        String userId = data == null ? null : text(data.get("userId"));
        if (StrUtil.isBlank(userId)) {
            return ResponseResult.error("用户ID不能为空");
        }
        userId = userId.trim();
        String orgCode = PERSONAL_ORG_CODE_PREFIX + userId;
        try {
            OrganizationEntity existing = this.lambdaQuery()
                    .eq(OrganizationEntity::getOrgCode, orgCode)
                    .one();
            boolean isNewOrg;
            Integer orgId;
            if (existing != null) {
                orgId = existing.getOrgId();
                isNewOrg = false;
                log.info("个人空间已存在，userId={}, orgId={}", userId, orgId);
            } else {
                OrganizationEntity entity = new OrganizationEntity();
                entity.setOrgName(personalOrgName(text(data.get("displayName")), userId));
                entity.setOrgCode(orgCode);
                // 4-个人空间
                entity.setOrgType(ORG_TYPE_PERSONAL);
                // 1-已启用
                entity.setOrgStatus(1);
                if (!this.save(entity)) {
                    log.error("创建个人空间组织失败，userId={}", userId);
                    return ResponseResult.error("创建个人空间失败，请重试");
                }
                orgId = entity.getOrgId();
                isNewOrg = true;
                log.info("已创建个人空间组织，userId={}, orgId={}, orgCode={}", userId, orgId, orgCode);
            }
            Map<String, Object> resultData = new HashMap<>();
            resultData.put("orgId", String.valueOf(orgId));
            resultData.put("orgCode", orgCode);
            resultData.put("isNewOrg", isNewOrg);
            return ResponseResult.success(resultData);
        } catch (Exception e) {
            log.error("保障个人空间组织异常，userId={}", userId, e);
            return ResponseResult.error("创建个人空间失败：" + e.getMessage());
        }
    }

    private String personalOrgName(String displayName, String userId) {
        String base = StrUtil.isBlank(displayName) ? ("用户" + userId) : displayName;
        String name = base + "的个人空间";
        // hou_base_organization.org_name 为 varchar(100)
        return name.length() > 100 ? name.substring(0, 100) : name;
    }

    private static String text(Object value) {
        return value == null ? null : String.valueOf(value);
    }

}
