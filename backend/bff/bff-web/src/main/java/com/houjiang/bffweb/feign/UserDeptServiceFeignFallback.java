package com.houjiang.bffweb.feign;

import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 用户部门服务 Feign 降级处理
/**
 *
 * @author Amos
 * @date 2026-05-12
 */
@Component
public class UserDeptServiceFeignFallback implements UserDeptServiceFeign {

    @Override
    public ResponseResult getDeptListByCurrentUser() {
        return ResponseResult.fail("部门管理服务暂时不可用");
    }

    @Override
    public ResponseResult getDeptListByOrgId(Long orgId) {
        return ResponseResult.fail("部门管理服务暂时不可用");
    }

    @Override
    public ResponseResult getDeptTreeByCurrentUser() {
        return ResponseResult.fail("部门管理服务暂时不可用");
    }

    @Override
    public ResponseResult getDeptTreeByOrgId(Long orgId) {
        return ResponseResult.fail("部门管理服务暂时不可用");
    }

    @Override
    public ResponseResult getDeptDetailById(Long deptId) {
        return ResponseResult.fail("部门管理服务暂时不可用");
    }

    @Override
    public ResponseResult getDeptListByOrgIdFeign(Map<String, Object> params) {
        return ResponseResult.fail("部门管理服务暂时不可用");
    }

    @Override
    public ResponseResult getDeptTreeByOrgIdFeign(Map<String, Object> params) {
        return ResponseResult.fail("部门管理服务暂时不可用");
    }
}
