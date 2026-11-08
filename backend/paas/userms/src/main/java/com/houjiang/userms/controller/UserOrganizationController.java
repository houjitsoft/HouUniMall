package com.houjiang.userms.controller;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.userms.service.UserOrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户组织关联Controller
 * @author Amos
 */
@RestController
@RequestMapping("/user/ms/organization")
public class UserOrganizationController extends BaseController {

    @Autowired
    private UserOrganizationService userOrganizationService;

    /**
     * 创建用户组织关联关系
     * @param userOrgData 用户组织关联数据
     * @return 创建结果
     */
    @PostMapping("/createUserOrg")
    public ResponseResult createUserOrganization(@RequestBody Map<String, Object> userOrgData) {
        return userOrganizationService.createUserOrganization(userOrgData);
    }

    /**
     * 根据用户ID查询用户的所有组织
     * @param userId 用户ID
     * @return 组织列表
     */
    @GetMapping("/getUserOrganizations")
    public ResponseResult getUserOrganizations(@RequestParam("userId") String userId) {
        return userOrganizationService.getUserOrganizations(userId);
    }

    /**
     * 检查用户是否已关联指定组织
     * @param userId 用户ID
     * @param orgId 组织ID
     * @return 是否已关联
     */
    @GetMapping("/checkUserOrgExists")
    public ResponseResult checkUserOrgExists(@RequestParam("userId") String userId,
                                            @RequestParam("orgId") String orgId) {
        return userOrganizationService.checkUserOrgExists(userId, orgId);
    }
}
