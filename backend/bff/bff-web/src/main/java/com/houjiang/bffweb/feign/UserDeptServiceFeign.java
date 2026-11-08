package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户部门服务 Feign 客户端
/**
 *
 * @description 专用于调用 userms 的 UserDeptController，其 @RequestMapping 为 /user/dept，
 *              不属于 UserInfoController 的 /user/ms 前缀。因此单独设置 path = /user，
 *              以免与 UserServiceFeign 的 /user/ms 前缀冲突导致 404。
 * @author Amos
 * @date 2026-05-12
 */
@FeignClient(
        name = "userms-dept",
        url = "${feign.userms.url:http://127.0.0.1:8195}",
        path = "/user",
        fallback = UserDeptServiceFeignFallback.class
)
public interface UserDeptServiceFeign {

    /**
     * 获取当前登录用户的部门列表
     */
    @GetMapping("/dept/list")
    ResponseResult getDeptListByCurrentUser();

    /**
     * 根据组织ID获取部门列表
     */
    @GetMapping("/dept/list/{orgId}")
    ResponseResult getDeptListByOrgId(@PathVariable("orgId") Long orgId);

    /**
     * 获取当前登录用户的部门树结构
     */
    @GetMapping("/dept/tree")
    ResponseResult getDeptTreeByCurrentUser();

    /**
     * 根据组织ID获取部门树结构
     */
    @GetMapping("/dept/tree/{orgId}")
    ResponseResult getDeptTreeByOrgId(@PathVariable("orgId") Long orgId);

    /**
     * 根据部门ID获取部门详细信息
     */
    @GetMapping("/dept/detail/{deptId}")
    ResponseResult getDeptDetailById(@PathVariable("deptId") Long deptId);

    /**
     * Feign 接口：根据组织ID获取部门列表（POST）
     */
    @PostMapping("/dept/feign/list")
    ResponseResult getDeptListByOrgIdFeign(@RequestBody Map<String, Object> params);

    /**
     * Feign 接口：根据组织ID获取部门树结构（POST）
     */
    @PostMapping("/dept/feign/tree")
    ResponseResult getDeptTreeByOrgIdFeign(@RequestBody Map<String, Object> params);
}
