package com.houjiang.bffweb.controller.mall;

import com.houjiang.bffweb.feign.MallServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Mall 平台后台组织管理 BFF
/**
 * 路由 /web/mall/org/* → mall 服务 /mall/org/*
 * @author Amos
 */
@RestController
@RequestMapping("/web/mall/org")
@RequiredArgsConstructor
public class MallOrgController {

    private final MallServiceFeign mallServiceFeign;

    @GetMapping("/dept/all")
    public ResponseResult deptAll(@RequestHeader(value = "Authorization", required = false) String token) {
        return mallServiceFeign.mallOrgDeptAll(token);
    }

    @GetMapping("/dept/lists")
    public ResponseResult deptLists(@RequestHeader(value = "Authorization", required = false) String token,
                                    @RequestParam Map<String, Object> params) {
        return mallServiceFeign.mallOrgDeptLists(token, params);
    }

    @GetMapping("/dept/detail")
    public ResponseResult deptDetail(@RequestHeader(value = "Authorization", required = false) String token,
                                     @RequestParam("id") Integer id) {
        return mallServiceFeign.mallOrgDeptDetail(token, id);
    }

    @PostMapping("/dept/add")
    public ResponseResult deptAdd(@RequestHeader(value = "Authorization", required = false) String token,
                                  @RequestBody Map<String, Object> params) {
        return mallServiceFeign.mallOrgDeptAdd(token, params);
    }

    @PostMapping("/dept/edit")
    public ResponseResult deptEdit(@RequestHeader(value = "Authorization", required = false) String token,
                                   @RequestBody Map<String, Object> params) {
        return mallServiceFeign.mallOrgDeptEdit(token, params);
    }

    @PostMapping("/dept/delete")
    public ResponseResult deptDelete(@RequestHeader(value = "Authorization", required = false) String token,
                                     @RequestBody Map<String, Object> params) {
        return mallServiceFeign.mallOrgDeptDelete(token, params);
    }

    @GetMapping("/jobs/all")
    public ResponseResult jobsAll(@RequestHeader(value = "Authorization", required = false) String token,
                                  @RequestParam(required = false) Map<String, Object> params) {
        return mallServiceFeign.mallOrgJobsAll(token, params != null ? params : Map.of());
    }

    @GetMapping("/jobs/lists")
    public ResponseResult jobsLists(@RequestHeader(value = "Authorization", required = false) String token,
                                    @RequestParam Map<String, Object> params) {
        return mallServiceFeign.mallOrgJobsLists(token, params);
    }

    @GetMapping("/jobs/detail")
    public ResponseResult jobsDetail(@RequestHeader(value = "Authorization", required = false) String token,
                                     @RequestParam("id") Integer id) {
        return mallServiceFeign.mallOrgJobsDetail(token, id);
    }

    @PostMapping("/jobs/add")
    public ResponseResult jobsAdd(@RequestHeader(value = "Authorization", required = false) String token,
                                  @RequestBody Map<String, Object> params) {
        return mallServiceFeign.mallOrgJobsAdd(token, params);
    }

    @PostMapping("/jobs/edit")
    public ResponseResult jobsEdit(@RequestHeader(value = "Authorization", required = false) String token,
                                   @RequestBody Map<String, Object> params) {
        return mallServiceFeign.mallOrgJobsEdit(token, params);
    }

    @PostMapping("/jobs/delete")
    public ResponseResult jobsDelete(@RequestHeader(value = "Authorization", required = false) String token,
                                     @RequestBody Map<String, Object> params) {
        return mallServiceFeign.mallOrgJobsDelete(token, params);
    }
}
