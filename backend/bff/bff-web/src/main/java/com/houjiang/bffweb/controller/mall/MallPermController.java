package com.houjiang.bffweb.controller.mall;

import com.houjiang.bffweb.feign.MallServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Mall 平台后台权限管理 BFF
/**
 * 路由 /web/mall/perms/* → mall 服务 /mall/perms/*（hou_mall_* 表）
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/web/mall/perms")
@RequiredArgsConstructor
public class MallPermController {

    private final MallServiceFeign mallServiceFeign;

    /** ================= 菜单 ================= */

    @PostMapping("/menu/list")
    public ResponseResult menuList(@RequestHeader(value = "Authorization", required = false) String token,
                                   @RequestBody(required = false) Map<String, Object> params) {
        return mallServiceFeign.mallPermMenuList(token, params == null ? Map.of() : params);
    }

    @PostMapping("/menu/all")
    public ResponseResult menuAll(@RequestHeader(value = "Authorization", required = false) String token,
                                  @RequestBody(required = false) Map<String, Object> params) {
        return mallServiceFeign.mallPermMenuAll(token, params == null ? Map.of() : params);
    }

    @PostMapping("/menu/tree")
    public ResponseResult menuTree(@RequestHeader(value = "Authorization", required = false) String token,
                                   @RequestBody(required = false) Map<String, Object> params) {
        return mallServiceFeign.mallPermMenuTree(token, params == null ? Map.of() : params);
    }

    @GetMapping("/menu/detail")
    public ResponseResult menuDetail(@RequestHeader(value = "Authorization", required = false) String token,
                                     @RequestParam("id") Integer id) {
        return mallServiceFeign.mallPermMenuDetail(token, id);
    }

    @PostMapping("/menu/add")
    public ResponseResult menuAdd(@RequestHeader(value = "Authorization", required = false) String token,
                                  @RequestBody Map<String, Object> params) {
        return mallServiceFeign.mallPermMenuAdd(token, params);
    }

    @PostMapping("/menu/edit")
    public ResponseResult menuEdit(@RequestHeader(value = "Authorization", required = false) String token,
                                   @RequestBody Map<String, Object> params) {
        return mallServiceFeign.mallPermMenuEdit(token, params);
    }

    @PostMapping("/menu/delete")
    public ResponseResult menuDelete(@RequestHeader(value = "Authorization", required = false) String token,
                                     @RequestBody Map<String, Object> params) {
        return mallServiceFeign.mallPermMenuDelete(token, params);
    }

    /** ================= 角色 ================= */

    @GetMapping("/role/all")
    public ResponseResult roleAll(@RequestHeader(value = "Authorization", required = false) String token) {
        return mallServiceFeign.mallPermRoleAll(token);
    }

    @GetMapping("/role/lists")
    public ResponseResult roleLists(@RequestHeader(value = "Authorization", required = false) String token,
                                    @RequestParam Map<String, Object> params) {
        return mallServiceFeign.mallPermRoleLists(token, params);
    }

    @GetMapping("/role/detail")
    public ResponseResult roleDetail(@RequestHeader(value = "Authorization", required = false) String token,
                                     @RequestParam("id") Integer id) {
        return mallServiceFeign.mallPermRoleDetail(token, id);
    }

    @PostMapping("/role/add")
    public ResponseResult roleAdd(@RequestHeader(value = "Authorization", required = false) String token,
                                  @RequestBody Map<String, Object> params) {
        return mallServiceFeign.mallPermRoleAdd(token, params);
    }

    @PostMapping("/role/edit")
    public ResponseResult roleEdit(@RequestHeader(value = "Authorization", required = false) String token,
                                   @RequestBody Map<String, Object> params) {
        return mallServiceFeign.mallPermRoleEdit(token, params);
    }

    @PostMapping("/role/delete")
    public ResponseResult roleDelete(@RequestHeader(value = "Authorization", required = false) String token,
                                     @RequestBody Map<String, Object> params) {
        return mallServiceFeign.mallPermRoleDelete(token, params);
    }

    /** ================= 管理员 ================= */

    @GetMapping("/admin/all")
    public ResponseResult adminAll(@RequestHeader(value = "Authorization", required = false) String token) {
        return mallServiceFeign.mallPermAdminAll(token);
    }

    @GetMapping("/admin/lists")
    public ResponseResult adminLists(@RequestHeader(value = "Authorization", required = false) String token,
                                     @RequestParam Map<String, Object> params) {
        return mallServiceFeign.mallPermAdminLists(token, params);
    }

    @GetMapping("/admin/detail")
    public ResponseResult adminDetail(@RequestHeader(value = "Authorization", required = false) String token,
                                      @RequestParam("id") Integer id) {
        return mallServiceFeign.mallPermAdminDetail(token, id);
    }

    @PostMapping("/admin/add")
    public ResponseResult adminAdd(@RequestHeader(value = "Authorization", required = false) String token,
                                   @RequestBody Map<String, Object> params) {
        return mallServiceFeign.mallPermAdminAdd(token, params);
    }

    @PostMapping("/admin/edit")
    public ResponseResult adminEdit(@RequestHeader(value = "Authorization", required = false) String token,
                                    @RequestBody Map<String, Object> params) {
        return mallServiceFeign.mallPermAdminEdit(token, params);
    }

    @PostMapping("/admin/delete")
    public ResponseResult adminDelete(@RequestHeader(value = "Authorization", required = false) String token,
                                      @RequestBody Map<String, Object> params) {
        return mallServiceFeign.mallPermAdminDelete(token, params);
    }
}
