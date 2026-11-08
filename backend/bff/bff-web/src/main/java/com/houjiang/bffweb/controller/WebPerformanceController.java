package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.feign.OaServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * OA 绩效管理兼容入口（历史前端使用 /web/performance，标准路径为 /web/oa/performance）
/**
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/web/performance")
public class WebPerformanceController {

    @Autowired
    private OaServiceFeign oaServiceFeign;

    /**
     * 分页列表（推荐）
     */
    @PostMapping("/page")
    public ResponseResult getPerformancePage(@RequestBody Map<String, Object> params) {
        return oaServiceFeign.getPerformancePage(params);
    }

    /**
     * 浏览器直接访问时的提示（GET /web/performance 无业务含义）
     */
    @GetMapping("")
    public ResponseResult performanceRootGet() {
        return ResponseResult.fail("请使用 POST /web/performance/page 查询绩效列表");
    }

    /**
     * 兼容旧前端：POST /web/performance 且 body 含分页参数时走列表查询
     */
    @PostMapping("")
    public ResponseResult performanceRoot(@RequestBody(required = false) Map<String, Object> body) {
        if (body != null && isPageQuery(body)) {
            return oaServiceFeign.getPerformancePage(body);
        }
        return oaServiceFeign.addPerformance(body != null ? body : Map.of());
    }

    @GetMapping("/{id}")
    public ResponseResult getPerformanceById(@PathVariable Long id) {
        return oaServiceFeign.getPerformanceById(id);
    }

    @PostMapping("/{id}/start")
    public ResponseResult startPerformanceEvaluation(@PathVariable Long id) {
        return oaServiceFeign.startPerformanceEvaluation(id);
    }

    @DeleteMapping("/{id}")
    public ResponseResult deletePerformance(@PathVariable Long id) {
        return oaServiceFeign.deletePerformance(id);
    }

    private boolean isPageQuery(Map<String, Object> body) {
        return body.containsKey("page")
                || body.containsKey("current")
                || body.containsKey("size")
                || body.containsKey("limit")
                || body.containsKey("keyword")
                || body.containsKey("period")
                || body.containsKey("status");
    }
}
