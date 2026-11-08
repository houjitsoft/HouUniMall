package com.houjiang.userms.controller;

import com.houjiang.commons.annnotation.SysLog;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.userms.service.UserDeptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 部门管理控制器
 * 
 * @author Amos
 * @since 2025-01-22
 */
@Slf4j
@RestController
@RequestMapping("/user/dept")
public class UserDeptController extends BaseController {

    @Autowired
    private UserDeptService userDeptService;

    /**
     * 获取当前登录用户的部门列表
     * 
     * @return 部门列表
     */
    @GetMapping("/list")
    @SysLog("获取当前用户部门列表")
    public ResponseResult getDeptListByCurrentUser() {
        try {
            log.info("获取当前登录用户的部门列表");
            return userDeptService.getDeptListByCurrentUser();
        } catch (Exception e) {
            log.error("获取当前用户部门列表异常", e);
            return ResponseResult.fail("获取部门列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据组织ID获取部门列表
     * 
     * @param orgId 组织ID
     * @return 部门列表
     */
    @GetMapping("/list/{orgId}")
    @SysLog("根据组织ID获取部门列表")
    public ResponseResult getDeptListByOrgId(@PathVariable("orgId") Long orgId) {
        try {
            log.info("根据组织ID获取部门列表，orgId: {}", orgId);
            return userDeptService.getDeptListByOrgId(orgId);
        } catch (Exception e) {
            log.error("根据组织ID获取部门列表异常，orgId: {}", orgId, e);
            return ResponseResult.fail("获取部门列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前登录用户的部门树结构
     * 
     * @return 部门树结构
     */
    @GetMapping("/tree")
    @SysLog("获取当前用户部门树")
    public ResponseResult getDeptTreeByCurrentUser() {
        try {
            log.info("获取当前登录用户的部门树");
            return userDeptService.getDeptTreeByCurrentUser();
        } catch (Exception e) {
            log.error("获取当前用户部门树异常", e);
            return ResponseResult.fail("获取部门树失败: " + e.getMessage());
        }
    }

    /**
     * 根据组织ID获取部门树结构
     * 
     * @param orgId 组织ID
     * @return 部门树结构
     */
    @GetMapping("/tree/{orgId}")
    @SysLog("根据组织ID获取部门树")
    public ResponseResult getDeptTreeByOrgId(@PathVariable("orgId") Long orgId) {
        try {
            log.info("根据组织ID获取部门树，orgId: {}", orgId);
            return userDeptService.getDeptTreeByOrgId(orgId);
        } catch (Exception e) {
            log.error("根据组织ID获取部门树异常，orgId: {}", orgId, e);
            return ResponseResult.fail("获取部门树失败: " + e.getMessage());
        }
    }

    /**
     * 根据部门ID获取部门详细信息
     * 
     * @param deptId 部门ID
     * @return 部门详细信息
     */
    @GetMapping("/detail/{deptId}")
    @SysLog("获取部门详细信息")
    public ResponseResult getDeptDetailById(@PathVariable("deptId") Long deptId) {
        try {
            log.info("根据部门ID获取部门详细信息，deptId: {}", deptId);
            return userDeptService.getDeptDetailById(deptId);
        } catch (Exception e) {
            log.error("根据部门ID获取部门详细信息异常，deptId: {}", deptId, e);
            return ResponseResult.fail("获取部门详细信息失败: " + e.getMessage());
        }
    }


    /**
     * Feign接口：根据组织ID获取部门列表
     * 供其他服务调用
     * 
     * @param orgId 组织ID
     * @return 部门列表
     */
    @PostMapping("/feign/list")
    public ResponseResult getDeptListByOrgIdFeign(@RequestBody Map<String, Object> params) {
        try {
            Long orgId = Long.valueOf(params.get("orgId").toString());
            log.info("Feign调用：根据组织ID获取部门列表，orgId: {}", orgId);
            return userDeptService.getDeptListByOrgId(orgId);
        } catch (Exception e) {
            log.error("Feign调用：根据组织ID获取部门列表异常", e);
            return ResponseResult.fail("获取部门列表失败: " + e.getMessage());
        }
    }

    /**
     * Feign接口：根据组织ID获取部门树结构
     * 供其他服务调用
     * 
     * @param params 包含orgId的参数
     * @return 部门树结构
     */
    @PostMapping("/feign/tree")
    public ResponseResult getDeptTreeByOrgIdFeign(@RequestBody Map<String, Object> params) {
        try {
            Long orgId = Long.valueOf(params.get("orgId").toString());
            log.info("Feign调用：根据组织ID获取部门树，orgId: {}", orgId);
            return userDeptService.getDeptTreeByOrgId(orgId);
        } catch (Exception e) {
            log.error("Feign调用：根据组织ID获取部门树异常", e);
            return ResponseResult.fail("获取部门树失败: " + e.getMessage());
        }
    }
}
