package com.houjiang.base.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.houjiang.base.dto.OrganizationDTO;
import com.houjiang.base.entity.OrganizationEntity;
import com.houjiang.commons.util.ResponseResult;

import java.util.List;
import java.util.Map;

/**
 * @ClassName OrganizationService
 * @Description 组织信息服务接口
 * @Author Amos
 * @Date 2024-12-19
 * @Version 1.0
 * @author Amos
 */
public interface OrganizationService extends IService<OrganizationEntity> {

    /**
     * 分页查询组织信息
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    ResponseResult queryOrganizationPage(OrganizationDTO dto);

    /**
     * 查询组织树形结构
     *
     * @param dto 查询条件
     * @return 组织树
     */
    ResponseResult queryOrganizationTree(OrganizationDTO dto);

    /**
     * 根据ID查询组织信息
     *
     * @param orgId 组织ID
     * @return 组织信息
     */
    ResponseResult queryOrganizationById(String orgId);

    /**
     * 新增组织
     *
     * @param dto 组织信息
     * @return 操作结果
     */
    ResponseResult addOrganization(OrganizationDTO dto);

    /**
     * 更新组织信息
     *
     * @param dto 组织信息
     * @return 操作结果
     */
    ResponseResult updateOrganization(OrganizationDTO dto);

    /**
     * 删除组织
     *
     * @param orgId 组织ID
     * @return 操作结果
     */
    ResponseResult deleteOrganization(String orgId);

    /**
     * 批量删除组织
     *
     * @param orgIds 组织ID列表
     * @return 操作结果
     */
    ResponseResult batchDeleteOrganization(List<String> orgIds);

    /**
     * 启用/禁用组织
     *
     * @param orgId   组织ID
     * @param status  状态：1-启用，0-禁用
     * @return 操作结果
     */
    ResponseResult updateOrganizationStatus(String orgId, Integer status);

    /**
     * 检查组织编码是否存在
     *
     * @param orgCode 组织编码
     * @param orgId   组织ID（排除自己）
     * @return 是否存在
     */
    ResponseResult checkOrgCodeExists(String orgCode, String orgId);

    /**
     * 检查组织名称是否存在
     *
     * @param orgName 组织名称
     * @param orgId   组织ID（排除自己）
     * @return 是否存在
     */
    ResponseResult checkOrgNameExists(String orgName, String orgId);

    /**
     * 提交公司登记申请
     * @param companyData 公司登记信息
     * @return 提交结果
     */
    ResponseResult submitCompanyRegistration(Map<String, Object> companyData);

    /**
     * 检查用户是否已经提交过公司登记申请
     * @param userId 用户ID
     * @return 检查结果
     */
    ResponseResult checkUserCompanyRegistration(String userId);

    /**
     * 创建用户组织关联关系
     * @param userOrgData 用户组织关联数据
     * @return 创建结果
     */
    ResponseResult createUserOrganization(Map<String, Object> userOrgData);

    /**
     * 保障用户拥有「个人空间」组织。
     *
     * <p>新注册用户不属于任何公司，组织成员关系为空，所有按组织隔离的功能
     * （智能入库、短信配置等）都会因组织未确定而拒绝访问。这里按
     * {@code PERSONAL_<userId>} 编码幂等地创建一个个人组织，由 userms 在注册成功后
     * 调用并完成绑定。</p>
     *
     * <p>仅供内部 Feign 调用：本接口不读取登录态，必须显式传 userId。</p>
     *
     * @param data userId（必填）、displayName（可选，用于组织命名）
     * @return orgId / orgCode / isNewOrg
     */
    ResponseResult ensurePersonalOrganization(Map<String, Object> data);
}
