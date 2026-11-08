package com.houjiang.base.controller;

import com.houjiang.base.dto.OrganizationDTO;
import com.houjiang.base.security.BasePermissionGuard;
import com.houjiang.base.service.OrganizationService;
import com.houjiang.commons.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @ClassName OrganizationController
 * @Description 组织信息管理控制器
 * @Author Amos
 * @Date 2024-12-19
 * @Version 1.0
 * @author Amos
 */
@RestController
@RequestMapping("/organization")
public class OrganizationController {

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private BasePermissionGuard permissionGuard;

    /**
     * 分页查询组织信息
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    @PostMapping("/queryOrganizationPage")
    public ResponseResult queryOrganizationPage(@RequestBody OrganizationDTO dto) {
        ResponseResult<?> denied = permissionGuard.require("shell:organization:view");
        if (denied != null) return denied;
        if (dto == null) dto = new OrganizationDTO();
        permissionGuard.applyOrganizationScope(dto);
        return organizationService.queryOrganizationPage(dto);
    }

    /**
     * 查询组织树形结构
     *
     * @param dto 查询条件
     * @return 组织树
     */
    @PostMapping("/queryOrganizationTree")
    public ResponseResult queryOrganizationTree(@RequestBody OrganizationDTO dto) {
        ResponseResult<?> denied = permissionGuard.require("shell:organization:view");
        if (denied != null) return denied;
        if (dto == null) dto = new OrganizationDTO();
        permissionGuard.applyOrganizationScope(dto);
        return organizationService.queryOrganizationTree(dto);
    }

    /**
     * 根据ID查询组织信息
     *
     * @param orgId 组织ID
     * @return 组织信息
     */
    @GetMapping("/queryOrganizationById")
    public ResponseResult queryOrganizationById(@RequestParam String orgId) {
        ResponseResult<?> denied = permissionGuard.requireTarget("shell:organization:view", orgId);
        if (denied != null) return denied;
        return organizationService.queryOrganizationById(orgId);
    }

    /**
     * 新增组织
     *
     * @param dto 组织信息
     * @return 操作结果
     */
    @PostMapping("/addOrganization")
    public ResponseResult addOrganization(@RequestBody OrganizationDTO dto) {
        ResponseResult<?> denied = permissionGuard.require("shell:organization:add");
        if (denied != null) return denied;
        if (dto != null && dto.getParentOrgId() != null && !dto.getParentOrgId().isBlank()) {
            denied = permissionGuard.requireTarget("shell:organization:view", dto.getParentOrgId());
            if (denied != null) return denied;
        }
        return organizationService.addOrganization(dto);
    }

    /**
     * 更新组织信息
     *
     * @param dto 组织信息
     * @return 操作结果
     */
    @PostMapping("/updateOrganization")
    public ResponseResult updateOrganization(@RequestBody OrganizationDTO dto) {
        ResponseResult<?> denied = permissionGuard.requireTarget("shell:organization:edit",
                dto == null ? null : dto.getOrgId());
        if (denied != null) return denied;
        return organizationService.updateOrganization(dto);
    }

    /**
     * 删除组织
     *
     * @param orgId 组织ID
     * @return 操作结果
     */
    @PostMapping("/deleteOrganization")
    public ResponseResult deleteOrganization(@RequestParam String orgId) {
        ResponseResult<?> denied = permissionGuard.requireTarget("shell:organization:delete", orgId);
        if (denied != null) return denied;
        return organizationService.deleteOrganization(orgId);
    }

    /**
     * 批量删除组织
     *
     * @param orgIds 组织ID列表
     * @return 操作结果
     */
    @PostMapping("/batchDeleteOrganization")
    public ResponseResult batchDeleteOrganization(@RequestBody List<String> orgIds) {
        ResponseResult<?> denied = permissionGuard.require("shell:organization:batch-delete");
        if (denied != null) return denied;
        if (orgIds == null || orgIds.isEmpty()) return ResponseResult.fail(400, "组织ID列表不能为空");
        for (String orgId : orgIds) {
            denied = permissionGuard.requireTarget("shell:organization:batch-delete", orgId);
            if (denied != null) return denied;
        }
        return organizationService.batchDeleteOrganization(orgIds);
    }

    /**
     * 启用/禁用组织
     *
     * @param orgId  组织ID
     * @param status 状态：1-启用，0-禁用
     * @return 操作结果
     */
    @PostMapping("/updateOrganizationStatus")
    public ResponseResult updateOrganizationStatus(@RequestParam String orgId, @RequestParam Integer status) {
        String permission = Integer.valueOf(1).equals(status)
                ? "shell:organization:enable" : "shell:organization:disable";
        ResponseResult<?> denied = permissionGuard.requireTarget(permission, orgId);
        if (denied != null) return denied;
        return organizationService.updateOrganizationStatus(orgId, status);
    }

    /**
     * 检查组织编码是否存在
     *
     * @param orgCode 组织编码
     * @param orgId   组织ID（排除自己）
     * @return 是否存在
     */
    @GetMapping("/checkOrgCodeExists")
    public ResponseResult checkOrgCodeExists(@RequestParam String orgCode, 
                                           @RequestParam(required = false) String orgId) {
        ResponseResult<?> denied = permissionGuard.requireAny("shell:organization:add", "shell:organization:edit");
        if (denied != null) return denied;
        if (orgId != null && !orgId.isBlank()) {
            denied = permissionGuard.requireTarget("shell:organization:edit", orgId);
            if (denied != null) return denied;
        }
        return organizationService.checkOrgCodeExists(orgCode, orgId);
    }

    /**
     * 检查组织名称是否存在
     *
     * @param orgName 组织名称
     * @param orgId   组织ID（排除自己）
     * @return 是否存在
     */
    @GetMapping("/checkOrgNameExists")
    public ResponseResult checkOrgNameExists(@RequestParam String orgName, 
                                           @RequestParam(required = false) String orgId) {
        ResponseResult<?> denied = permissionGuard.requireAny("shell:organization:add", "shell:organization:edit");
        if (denied != null) return denied;
        if (orgId != null && !orgId.isBlank()) {
            denied = permissionGuard.requireTarget("shell:organization:edit", orgId);
            if (denied != null) return denied;
        }
        return organizationService.checkOrgNameExists(orgName, orgId);
    }

    /**
     * 提交公司登记申请
     * @param companyData 公司登记信息
     * @return 提交结果
     */
    @PostMapping("/register")
    public ResponseResult submitCompanyRegistration(@RequestBody Map<String, Object> companyData) {
        return organizationService.submitCompanyRegistration(companyData);
    }

    /**
     * 检查用户是否已经提交过公司登记申请
     * @param userId 用户ID
     * @return 检查结果
     */
    @GetMapping("/checkUserRegistration")
    public ResponseResult checkUserCompanyRegistration(@RequestParam("userId") String userId,
                                                       jakarta.servlet.http.HttpServletRequest request) {
        // 只允许本人或服务间内部调用：否则可枚举任意用户是否已登记公司
        ResponseResult<?> denied = permissionGuard.requireInternalOrSelf(userId, request);
        if (denied != null) return denied;
        return organizationService.checkUserCompanyRegistration(userId);
    }

    /**
     * 创建用户组织关联关系
     * @param userOrgData 用户组织关联数据
     * @return 创建结果
     */
    @PostMapping("/createUserOrg")
    public ResponseResult createUserOrganization(@RequestBody Map<String, Object> userOrgData,
                                                 jakarta.servlet.http.HttpServletRequest request) {
        // 🔴 2026-10-27：按 userId 建组织，必须校验「本人」或内部令牌
        String targetUserId = userOrgData == null ? null
                : (userOrgData.get("userId") == null ? null : String.valueOf(userOrgData.get("userId")));
        ResponseResult<?> denied = permissionGuard.requireInternalOrSelf(targetUserId, request);
        if (denied != null) return denied;
        return organizationService.createUserOrganization(userOrgData);
    }

    /**
     * 保障用户拥有「个人空间」组织（幂等）。
     *
     * <p>仅供 userms 在注册成功后通过 Feign 内部调用：注册请求没有登录态，因此这里
     * 不做权限校验，必须显式传 userId。<b>BFF 不得转发此接口</b>，否则任何人都能
     * 按 userId 建组织。</p>
     */
    @PostMapping("/ensurePersonalOrganization")
    public ResponseResult ensurePersonalOrganization(@RequestBody Map<String, Object> data,
                                                     jakarta.servlet.http.HttpServletRequest request) {
        // 🔴 2026-10-27：注册链路（userms Feign 内部调用）靠内部令牌证明身份；
        // 缺令牌时只允许本人给自己建个人空间，防止 BFF 被滥用转发后按任意 userId 建组织。
        String targetUserId = data == null ? null
                : (data.get("userId") == null ? null : String.valueOf(data.get("userId")));
        ResponseResult<?> denied = permissionGuard.requireInternalOrSelf(targetUserId, request);
        if (denied != null) return denied;
        return organizationService.ensurePersonalOrganization(data);
    }

}
