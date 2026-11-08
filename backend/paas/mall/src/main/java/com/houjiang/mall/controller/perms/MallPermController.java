package com.houjiang.mall.controller.perms;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.service.auth.MallAdminTokenService;
import com.houjiang.mall.service.perms.MallPermAdminService;
import com.houjiang.mall.service.perms.MallPermMenuService;
import com.houjiang.mall.service.perms.MallPermRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Mall 平台后台权限管理（hou_mall_* 表，独立于云后台 permission 服务）
 * @author Amos
 */
@RestController
@RequestMapping("/mall/perms")
@RequiredArgsConstructor
public class MallPermController {

    private static final String MENU_READ =
            "hasAnyAuthority('ROLE_MALL_ADMIN','*','shell:setting:permission','shell:setting:permission:menu')";
    private static final String MENU_ADD =
            "hasAnyAuthority('ROLE_MALL_ADMIN','*','shell:setting:permission','shell:setting:permission:menu:add')";
    private static final String MENU_EDIT =
            "hasAnyAuthority('ROLE_MALL_ADMIN','*','shell:setting:permission','shell:setting:permission:menu:edit')";
    private static final String MENU_DELETE =
            "hasAnyAuthority('ROLE_MALL_ADMIN','*','shell:setting:permission','shell:setting:permission:menu:delete')";
    private static final String ROLE_READ =
            "hasAnyAuthority('ROLE_MALL_ADMIN','*','shell:setting:permission','shell:setting:permission:role')";
    private static final String ROLE_ADD =
            "hasAnyAuthority('ROLE_MALL_ADMIN','*','shell:setting:permission','shell:setting:permission:role:add')";
    private static final String ROLE_EDIT =
            "hasAnyAuthority('ROLE_MALL_ADMIN','*','shell:setting:permission','shell:setting:permission:role:edit')";
    private static final String ROLE_DELETE =
            "hasAnyAuthority('ROLE_MALL_ADMIN','*','shell:setting:permission','shell:setting:permission:role:delete')";
    private static final String USER_READ =
            "hasAnyAuthority('ROLE_MALL_ADMIN','*','shell:setting:permission','shell:setting:permission:user')";
    private static final String USER_ADD =
            "hasAnyAuthority('ROLE_MALL_ADMIN','*','shell:setting:permission','shell:setting:permission:user:add')";
    private static final String USER_EDIT =
            "hasAnyAuthority('ROLE_MALL_ADMIN','*','shell:setting:permission','shell:setting:permission:user:edit')";
    private static final String USER_DELETE =
            "hasAnyAuthority('ROLE_MALL_ADMIN','*','shell:setting:permission','shell:setting:permission:user:delete')";

    private final MallAdminTokenService mallAdminTokenService;
    private final MallPermMenuService mallPermMenuService;
    private final MallPermRoleService mallPermRoleService;
    private final MallPermAdminService mallPermAdminService;

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

    /** ================= 菜单 ================= */

    @PostMapping("/menu/list")
    @PreAuthorize(MENU_READ)
    public ResponseResult menuList(@RequestHeader(value = "Authorization", required = false) String token,
                                   @RequestBody(required = false) Map<String, Object> params) {
        ResponseResult auth = requireLogin(token);
        if (auth != null) {
            return auth;
        }
        return mallPermMenuService.list();
    }

    @PostMapping("/menu/all")
    @PreAuthorize(MENU_READ)
    public ResponseResult menuAll(@RequestHeader(value = "Authorization", required = false) String token,
                                  @RequestBody(required = false) Map<String, Object> params) {
        ResponseResult auth = requireLogin(token);
        if (auth != null) {
            return auth;
        }
        return mallPermMenuService.all();
    }

    @PostMapping("/menu/tree")
    @PreAuthorize(MENU_READ)
    public ResponseResult menuTree(@RequestHeader(value = "Authorization", required = false) String token,
                                   @RequestBody(required = false) Map<String, Object> params) {
        ResponseResult auth = requireLogin(token);
        if (auth != null) {
            return auth;
        }
        return mallPermMenuService.tree();
    }

    @GetMapping("/menu/detail")
    @PreAuthorize(MENU_READ)
    public ResponseResult menuDetail(@RequestHeader(value = "Authorization", required = false) String token,
                                     @RequestParam("id") Integer id) {
        ResponseResult auth = requireLogin(token);
        if (auth != null) {
            return auth;
        }
        return mallPermMenuService.detail(id);
    }

    @PostMapping("/menu/add")
    @PreAuthorize(MENU_ADD)
    public ResponseResult menuAdd(@RequestHeader(value = "Authorization", required = false) String token,
                                  @RequestBody Map<String, Object> params) {
        ResponseResult auth = requireLogin(token);
        if (auth != null) {
            return auth;
        }
        return mallPermMenuService.add(params);
    }

    @PostMapping("/menu/edit")
    @PreAuthorize(MENU_EDIT)
    public ResponseResult menuEdit(@RequestHeader(value = "Authorization", required = false) String token,
                                   @RequestBody Map<String, Object> params) {
        ResponseResult auth = requireLogin(token);
        if (auth != null) {
            return auth;
        }
        return mallPermMenuService.edit(params);
    }

    @PostMapping("/menu/delete")
    @PreAuthorize(MENU_DELETE)
    public ResponseResult menuDelete(@RequestHeader(value = "Authorization", required = false) String token,
                                     @RequestBody Map<String, Object> params) {
        ResponseResult auth = requireLogin(token);
        if (auth != null) {
            return auth;
        }
        return mallPermMenuService.delete(params);
    }

    /** ================= 角色 ================= */

    @GetMapping("/role/all")
    @PreAuthorize(ROLE_READ)
    public ResponseResult roleAll(@RequestHeader(value = "Authorization", required = false) String token) {
        ResponseResult auth = requireLogin(token);
        if (auth != null) {
            return auth;
        }
        return mallPermRoleService.all();
    }

    @GetMapping("/role/lists")
    @PreAuthorize(ROLE_READ)
    public ResponseResult roleLists(@RequestHeader(value = "Authorization", required = false) String token,
                                    @RequestParam Map<String, Object> params) {
        ResponseResult auth = requireLogin(token);
        if (auth != null) {
            return auth;
        }
        return mallPermRoleService.lists(params);
    }

    @GetMapping("/role/detail")
    @PreAuthorize(ROLE_READ)
    public ResponseResult roleDetail(@RequestHeader(value = "Authorization", required = false) String token,
                                     @RequestParam("id") Integer id) {
        ResponseResult auth = requireLogin(token);
        if (auth != null) {
            return auth;
        }
        return mallPermRoleService.detail(id);
    }

    @PostMapping("/role/add")
    @PreAuthorize(ROLE_ADD)
    public ResponseResult roleAdd(@RequestHeader(value = "Authorization", required = false) String token,
                                  @RequestBody Map<String, Object> params) {
        ResponseResult auth = requireLogin(token);
        if (auth != null) {
            return auth;
        }
        return mallPermRoleService.add(params);
    }

    @PostMapping("/role/edit")
    @PreAuthorize(ROLE_EDIT)
    public ResponseResult roleEdit(@RequestHeader(value = "Authorization", required = false) String token,
                                   @RequestBody Map<String, Object> params) {
        ResponseResult auth = requireLogin(token);
        if (auth != null) {
            return auth;
        }
        return mallPermRoleService.edit(params);
    }

    @PostMapping("/role/delete")
    @PreAuthorize(ROLE_DELETE)
    public ResponseResult roleDelete(@RequestHeader(value = "Authorization", required = false) String token,
                                     @RequestBody Map<String, Object> params) {
        ResponseResult auth = requireLogin(token);
        if (auth != null) {
            return auth;
        }
        return mallPermRoleService.delete(params);
    }

    /** ================= 管理员 ================= */

    @GetMapping("/admin/all")
    @PreAuthorize(USER_READ)
    public ResponseResult adminAll(@RequestHeader(value = "Authorization", required = false) String token) {
        ResponseResult auth = requireLogin(token);
        if (auth != null) {
            return auth;
        }
        return mallPermAdminService.all();
    }

    @GetMapping("/admin/lists")
    @PreAuthorize(USER_READ)
    public ResponseResult adminLists(@RequestHeader(value = "Authorization", required = false) String token,
                                     @RequestParam Map<String, Object> params) {
        ResponseResult auth = requireLogin(token);
        if (auth != null) {
            return auth;
        }
        return mallPermAdminService.lists(params);
    }

    @GetMapping("/admin/detail")
    @PreAuthorize(USER_READ)
    public ResponseResult adminDetail(@RequestHeader(value = "Authorization", required = false) String token,
                                      @RequestParam("id") Integer id) {
        ResponseResult auth = requireLogin(token);
        if (auth != null) {
            return auth;
        }
        return mallPermAdminService.detail(id);
    }

    @PostMapping("/admin/add")
    @PreAuthorize(USER_ADD)
    public ResponseResult adminAdd(@RequestHeader(value = "Authorization", required = false) String token,
                                   @RequestBody Map<String, Object> params) {
        ResponseResult auth = requireLogin(token);
        if (auth != null) {
            return auth;
        }
        return mallPermAdminService.add(params);
    }

    @PostMapping("/admin/edit")
    @PreAuthorize(USER_EDIT)
    public ResponseResult adminEdit(@RequestHeader(value = "Authorization", required = false) String token,
                                    @RequestBody Map<String, Object> params) {
        ResponseResult auth = requireLogin(token);
        if (auth != null) {
            return auth;
        }
        return mallPermAdminService.edit(params);
    }

    @PostMapping("/admin/delete")
    @PreAuthorize(USER_DELETE)
    public ResponseResult adminDelete(@RequestHeader(value = "Authorization", required = false) String token,
                                      @RequestBody Map<String, Object> params) {
        ResponseResult auth = requireLogin(token);
        if (auth != null) {
            return auth;
        }
        return mallPermAdminService.delete(params);
    }
}
