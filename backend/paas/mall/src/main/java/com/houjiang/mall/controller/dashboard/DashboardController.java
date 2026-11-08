package com.houjiang.mall.controller.dashboard;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.mall.service.dashboard.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Dashboard聚合数据Controller
 * 提供商城概览、实时监控、销售趋势、商品排行、快捷操作等聚合接口
 *
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/mall/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * 获取概览统计数据
     * 聚合店铺、分销、佣金、库存预警等核心指标
     *
     * @return ResponseResult 包含概览数据Map
     */
    @PostMapping("/overview")
    public ResponseResult<Map<String, Object>> getOverview() {
        try {
            return ResponseResult.success(dashboardService.getOverview());
        } catch (Exception e) {
            log.error("获取Dashboard概览失败", e);
            return ResponseResult.fail(500, "获取概览数据失败: " + e.getMessage());
        }
    }

    /** Mall 管理后台工作台（bbc.houjit.com/admin） */
    @PostMapping("/workbench")
    public ResponseResult<Map<String, Object>> getWorkbench() {
        try {
            return ResponseResult.success(dashboardService.getWorkbenchOverview());
        } catch (Exception e) {
            log.error("获取工作台数据失败", e);
            return ResponseResult.fail(500, "获取工作台数据失败: " + e.getMessage());
        }
    }

    @PostMapping("/check-version")
    public ResponseResult<Map<String, Object>> checkVersion(@RequestBody(required = false) Map<String, Object> params) {
        return ResponseResult.success(dashboardService.checkVersion(params != null ? params : Map.of()));
    }

    @PostMapping("/check-legal")
    public ResponseResult<Map<String, Object>> checkLegal(@RequestBody(required = false) Map<String, Object> params) {
        return ResponseResult.success(dashboardService.checkLegal(params != null ? params : Map.of()));
    }

    /**
     * 获取实时监控数据
     * 包含最近订单流、各类待处理计数
     *
     * @return ResponseResult 包含实时数据Map
     */
    @PostMapping("/realtime")
    public ResponseResult<Map<String, Object>> getRealtime() {
        try {
            return ResponseResult.success(dashboardService.getRealtime());
        } catch (Exception e) {
            log.error("获取实时监控数据失败", e);
            return ResponseResult.fail(500, "获取实时数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取销售趋势数据
     * 支持按日(day)、周(week)、月(month)查询
     *
     * @param params 请求参数，包含timeRange字段
     * @return ResponseResult 包含趋势数据Map
     */
    @PostMapping("/sales-trend")
    public ResponseResult<Map<String, Object>> getSalesTrend(@RequestBody Map<String, Object> params) {
        try {
            String timeRange = (String) params.getOrDefault("timeRange", "week");
            return ResponseResult.success(dashboardService.getSalesTrend(timeRange));
        } catch (Exception e) {
            log.error("获取销售趋势失败", e);
            return ResponseResult.fail(500, "获取销售趋势失败: " + e.getMessage());
        }
    }

    /**
     * 获取商品佣金排行
     *
     * @param params 请求参数，包含limit字段（默认10）
     * @return ResponseResult 包含排行列表
     */
    @PostMapping("/goods-rank")
    public ResponseResult<List<Map<String, Object>>> getGoodsRank(@RequestBody Map<String, Object> params) {
        try {
            int limit = params.get("limit") != null ? Integer.parseInt(params.get("limit").toString()) : 10;
            return ResponseResult.success(dashboardService.getGoodsRank(limit));
        } catch (Exception e) {
            log.error("获取商品排行失败", e);
            return ResponseResult.fail(500, "获取商品排行失败: " + e.getMessage());
        }
    }

    /**
     * 获取快捷操作待处理数据
     *
     * @return ResponseResult 包含各类待处理计数
     */
    @PostMapping("/quick-actions")
    public ResponseResult<Map<String, Object>> getQuickActions() {
        try {
            return ResponseResult.success(dashboardService.getQuickActions());
        } catch (Exception e) {
            log.error("获取快捷操作数据失败", e);
            return ResponseResult.fail(500, "获取快捷操作数据失败: " + e.getMessage());
        }
    }
}
