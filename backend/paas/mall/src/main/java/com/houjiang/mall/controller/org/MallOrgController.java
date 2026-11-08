package com.houjiang.mall.controller.org;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.service.auth.MallAdminTokenService;
import com.houjiang.mall.service.org.MallOrgDeptService;
import com.houjiang.mall.service.org.MallOrgJobsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Mall 平台后台组织管理（hou_mall_dept / hou_mall_jobs）
 * @author Amos
 */
@RestController
@RequestMapping("/mall/org")
@RequiredArgsConstructor
public class MallOrgController {

    private static final String ORG_READ =
            "hasAnyAuthority('ROLE_MALL_ADMIN','*','shell:organization','shell:organization:view')";
    private static final String ORG_ADD =
            "hasAnyAuthority('ROLE_MALL_ADMIN','*','shell:organization','shell:organization:add')";
    private static final String ORG_EDIT =
            "hasAnyAuthority('ROLE_MALL_ADMIN','*','shell:organization','shell:organization:edit')";
    private static final String ORG_DELETE =
            "hasAnyAuthority('ROLE_MALL_ADMIN','*','shell:organization','shell:organization:delete')";

    private final MallAdminTokenService mallAdminTokenService;
    private final MallOrgDeptService mallOrgDeptService;
    private final MallOrgJobsService mallOrgJobsService;

    /**
     * 与 {@code MallAdminTokenAuthenticationFilter} 保持一致：商城管理员 token 与
     * Cloud 内嵌商城的云后台 token 均视为已登录。
     */
    private ResponseResult requireLogin(String token) {
        if (!mallAdminTokenService.isAuthenticated(token)) {
            return ResponseResult.fail(401, "登录已过期");
        }
        return null;
    }

    /** ================= 部门 ================= */

    @GetMapping("/dept/all")
    @PreAuthorize(ORG_READ)
    public ResponseResult deptAll(@RequestHeader(value = "Authorization", required = false) String token) {
        ResponseResult auth = requireLogin(token);
        if (auth != null) {
            return auth;
        }
        return mallOrgDeptService.all();
    }

    @GetMapping("/dept/lists")
    @PreAuthorize(ORG_READ)
    public ResponseResult deptLists(@RequestHeader(value = "Authorization", required = false) String token,
                                    @RequestParam Map<String, Object> params) {
        ResponseResult auth = requireLogin(token);
        if (auth != null) {
            return auth;
        }
        return mallOrgDeptService.lists(params);
    }

    @GetMapping("/dept/detail")
    @PreAuthorize(ORG_READ)
    public ResponseResult deptDetail(@RequestHeader(value = "Authorization", required = false) String token,
                                     @RequestParam("id") Integer id) {
        ResponseResult auth = requireLogin(token);
        if (auth != null) {
            return auth;
        }
        return mallOrgDeptService.detail(id);
    }

    @PostMapping("/dept/add")
    @PreAuthorize(ORG_ADD)
    public ResponseResult deptAdd(@RequestHeader(value = "Authorization", required = false) String token,
                                  @RequestBody Map<String, Object> params) {
        ResponseResult auth = requireLogin(token);
        if (auth != null) {
            return auth;
        }
        return mallOrgDeptService.add(params);
    }

    @PostMapping("/dept/edit")
    @PreAuthorize(ORG_EDIT)
    public ResponseResult deptEdit(@RequestHeader(value = "Authorization", required = false) String token,
                                    @RequestBody Map<String, Object> params) {
        ResponseResult auth = requireLogin(token);
        if (auth != null) {
            return auth;
        }
        return mallOrgDeptService.edit(params);
    }

    @PostMapping("/dept/delete")
    @PreAuthorize(ORG_DELETE)
    public ResponseResult deptDelete(@RequestHeader(value = "Authorization", required = false) String token,
                                     @RequestBody Map<String, Object> params) {
        ResponseResult auth = requireLogin(token);
        if (auth != null) {
            return auth;
        }
        return mallOrgDeptService.delete(params);
    }

    /** ================= 岗位 ================= */

    @GetMapping("/jobs/all")
    @PreAuthorize(ORG_READ)
    public ResponseResult jobsAll(@RequestHeader(value = "Authorization", required = false) String token,
                                  @RequestParam(required = false) Map<String, Object> params) {
        ResponseResult auth = requireLogin(token);
        if (auth != null) {
            return auth;
        }
        return mallOrgJobsService.all(params != null ? params : Map.of());
    }

    @GetMapping("/jobs/lists")
    @PreAuthorize(ORG_READ)
    public ResponseResult jobsLists(@RequestHeader(value = "Authorization", required = false) String token,
                                    @RequestParam Map<String, Object> params) {
        ResponseResult auth = requireLogin(token);
        if (auth != null) {
            return auth;
        }
        return mallOrgJobsService.lists(params);
    }

    @GetMapping("/jobs/detail")
    @PreAuthorize(ORG_READ)
    public ResponseResult jobsDetail(@RequestHeader(value = "Authorization", required = false) String token,
                                     @RequestParam("id") Integer id) {
        ResponseResult auth = requireLogin(token);
        if (auth != null) {
            return auth;
        }
        return mallOrgJobsService.detail(id);
    }

    @PostMapping("/jobs/add")
    @PreAuthorize(ORG_ADD)
    public ResponseResult jobsAdd(@RequestHeader(value = "Authorization", required = false) String token,
                                  @RequestBody Map<String, Object> params) {
        ResponseResult auth = requireLogin(token);
        if (auth != null) {
            return auth;
        }
        return mallOrgJobsService.add(params);
    }

    @PostMapping("/jobs/edit")
    @PreAuthorize(ORG_EDIT)
    public ResponseResult jobsEdit(@RequestHeader(value = "Authorization", required = false) String token,
                                   @RequestBody Map<String, Object> params) {
        ResponseResult auth = requireLogin(token);
        if (auth != null) {
            return auth;
        }
        return mallOrgJobsService.edit(params);
    }

    @PostMapping("/jobs/delete")
    @PreAuthorize(ORG_DELETE)
    public ResponseResult jobsDelete(@RequestHeader(value = "Authorization", required = false) String token,
                                     @RequestBody Map<String, Object> params) {
        ResponseResult auth = requireLogin(token);
        if (auth != null) {
            return auth;
        }
        return mallOrgJobsService.delete(params);
    }
}
