package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.feign.GeoServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * GEO 独立权限管理 BFF
/**
 * 路由 /web/geo/perms/* → geo 服务 /geo/perms/*（hou_geo_* 表）
 * @author Amos
 */
@RestController
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "houcloud.legacy-bff.enabled", havingValue = "true")
@RequestMapping("/web/geo/perms")
@RequiredArgsConstructor
public class WebGeoPermController {

    private final GeoServiceFeign geoServiceFeign;

    @PostMapping("/menu/list")
    public ResponseResult menuList(@RequestBody(required = false) Map<String, Object> params) {
        return geoServiceFeign.geoPermMenuList(params != null ? params : Map.of());
    }

    @PostMapping("/menu/all")
    public ResponseResult menuAll(@RequestBody(required = false) Map<String, Object> params) {
        return geoServiceFeign.geoPermMenuAll(params != null ? params : Map.of());
    }

    @PostMapping("/menu/tree")
    public ResponseResult menuTree(@RequestBody(required = false) Map<String, Object> params) {
        return geoServiceFeign.geoPermMenuTree(params != null ? params : Map.of());
    }

    @GetMapping("/menu/detail")
    public ResponseResult menuDetail(@RequestParam("id") Integer id) {
        return geoServiceFeign.geoPermMenuDetail(id);
    }

    @PostMapping("/menu/add")
    public ResponseResult menuAdd(@RequestBody Map<String, Object> params) {
        return geoServiceFeign.geoPermMenuAdd(params);
    }

    @PostMapping("/menu/edit")
    public ResponseResult menuEdit(@RequestBody Map<String, Object> params) {
        return geoServiceFeign.geoPermMenuEdit(params);
    }

    @PostMapping("/menu/delete")
    public ResponseResult menuDelete(@RequestBody Map<String, Object> params) {
        return geoServiceFeign.geoPermMenuDelete(params);
    }

    @PostMapping("/role/all")
    public ResponseResult roleAll(@RequestBody(required = false) Map<String, Object> params) {
        return geoServiceFeign.geoPermRoleAll(params != null ? params : Map.of());
    }

    @PostMapping("/role/lists")
    public ResponseResult roleLists(@RequestBody(required = false) Map<String, Object> params) {
        return geoServiceFeign.geoPermRoleLists(params != null ? params : Map.of());
    }

    @GetMapping("/role/detail")
    public ResponseResult roleDetail(@RequestParam("id") Integer id) {
        return geoServiceFeign.geoPermRoleDetail(id);
    }

    @PostMapping("/role/add")
    public ResponseResult roleAdd(@RequestBody Map<String, Object> params) {
        return geoServiceFeign.geoPermRoleAdd(params);
    }

    @PostMapping("/role/edit")
    public ResponseResult roleEdit(@RequestBody Map<String, Object> params) {
        return geoServiceFeign.geoPermRoleEdit(params);
    }

    @PostMapping("/role/delete")
    public ResponseResult roleDelete(@RequestBody Map<String, Object> params) {
        return geoServiceFeign.geoPermRoleDelete(params);
    }

    @PostMapping("/user/lists")
    public ResponseResult userLists(@RequestBody(required = false) Map<String, Object> params) {
        return geoServiceFeign.geoPermUserLists(params != null ? params : Map.of());
    }

    @GetMapping("/user/roles")
    public ResponseResult userRoles(@RequestParam("user_id") Long userId) {
        return geoServiceFeign.geoPermUserRoles(userId);
    }

    @PostMapping("/user/bind")
    public ResponseResult bindUserRoles(@RequestBody Map<String, Object> params) {
        return geoServiceFeign.geoPermUserBind(params);
    }
}
