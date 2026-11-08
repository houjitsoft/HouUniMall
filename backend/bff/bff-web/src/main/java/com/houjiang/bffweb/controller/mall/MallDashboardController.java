package com.houjiang.bffweb.controller.mall;

import com.houjiang.bffweb.feign.MallServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 商城Dashboard控制器
/**
 * 通过MallServiceFeign调用mall服务聚合数据接口
 *
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/web/mall/dashboard")
@RequiredArgsConstructor
public class MallDashboardController {

    private final MallServiceFeign mallServiceFeign;

    /**
     * 获取商城概览数据
/**
     * 聚合店铺、分销、佣金、库存预警等核心指标
     *
     * @return ResponseResult 包含概览数据
     */
    @PostMapping("/overview")
    public ResponseResult getOverview() {
        return mallServiceFeign.getDashboardOverview();
    }

    @PostMapping("/workbench")
    public ResponseResult getWorkbench() {
        return mallServiceFeign.getDashboardWorkbench();
    }

    @PostMapping("/check-version")
    public ResponseResult checkVersion(@RequestBody(required = false) Map<String, Object> params) {
        return mallServiceFeign.getDashboardCheckVersion(params != null ? params : Map.of());
    }

    @PostMapping("/check-legal")
    public ResponseResult checkLegal(@RequestBody(required = false) Map<String, Object> params) {
        return mallServiceFeign.getDashboardCheckLegal(params != null ? params : Map.of());
    }

    /**
     * 获取实时监控数据
/**
     * 包含待处理计数、最近订单流等
     *
     * @return ResponseResult 包含实时数据
     */
    @PostMapping("/realtime")
    public ResponseResult getRealtimeData() {
        return mallServiceFeign.getDashboardRealtime();
    }

    /**
     * 获取销售趋势数据
/**
     * 支持按日(day)、周(week)、月(month)查询
     *
     * @param params 请求参数，包含timeRange字段
     * @return ResponseResult 包含趋势图表数据
     */
    @PostMapping("/sales-trend")
    public ResponseResult getSalesTrend(@RequestBody Map<String, Object> params) {
        return mallServiceFeign.getDashboardSalesTrend(params);
    }

    /**
     * 获取商品佣金排行
/**
     *
     * @param params 请求参数，包含limit字段（默认10）
     * @return ResponseResult 包含排行列表
     */
    @PostMapping("/goods-rank")
    public ResponseResult getGoodsRank(@RequestBody Map<String, Object> params) {
        return mallServiceFeign.getDashboardGoodsRank(params);
    }

    /**
     * 获取快捷操作待处理数据
/**
     * 返回各类待处理计数
     *
     * @return ResponseResult 包含快捷操作数据
     */
    @PostMapping("/quick-actions")
    public ResponseResult getQuickActions() {
        return mallServiceFeign.getDashboardQuickActions();
    }
}
