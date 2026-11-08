package com.houjiang.goods.controller;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.goods.service.StatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 数据统计控制器
 * 
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/goods/statistics")
public class StatisticsController {
    
    @Autowired
    private StatisticsService statisticsService;
    
    // =============== 统计报表 ===============
    
    /**
     * 获取统计概览
     */
    @PostMapping("/report/overview")
    public ResponseResult getReportOverview(@RequestBody Map<String, Object> param) {
        return statisticsService.getReportOverview(param);
    }
    
    /**
     * 获取统计明细
     */
    @PostMapping("/report/detail")
    public ResponseResult getReportDetail(@RequestBody Map<String, Object> param) {
        return statisticsService.getReportDetail(param);
    }
    
    /**
     * 获取销售趋势
     */
    @PostMapping("/report/trend")
    public ResponseResult getSalesTrend(@RequestBody Map<String, Object> param) {
        return statisticsService.getSalesTrend(param);
    }
    
    /**
     * 导出统计报表
     */
    @PostMapping("/report/export")
    public ResponseResult exportReport(@RequestBody Map<String, Object> param) {
        return statisticsService.exportReport(param);
    }
    
    // =============== 销售统计 ===============
    
    /**
     * 获取销售统计
     */
    @PostMapping("/sales/overview")
    public ResponseResult getSalesStatistics(@RequestBody Map<String, Object> param) {
        return statisticsService.getSalesStatistics(param);
    }
    
    /**
     * 获取销售趋势详情
     */
    @PostMapping("/sales/trend")
    public ResponseResult getSalesTrendDetail(@RequestBody Map<String, Object> param) {
        return statisticsService.getSalesTrendDetail(param);
    }
    
    // =============== 财务统计 ===============
    
    /**
     * 获取财务统计
     */
    @PostMapping("/finance/overview")
    public ResponseResult getFinanceStatistics(@RequestBody Map<String, Object> param) {
        return statisticsService.getFinanceStatistics(param);
    }
    
    // =============== 库存统计 ===============
    
    /**
     * 获取库存统计概览
     */
    @PostMapping("/inventory/overview")
    public ResponseResult getInventoryStatisticsOverview(@RequestBody Map<String, Object> param) {
        return statisticsService.getInventoryStatisticsOverview(param);
    }
    
    // =============== 客户统计 ===============
    
    /**
     * 获取客户统计
     */
    @PostMapping("/customer/overview")
    public ResponseResult getCustomerStatistics(@RequestBody Map<String, Object> param) {
        return statisticsService.getCustomerStatistics(param);
    }
    
    // =============== 商品统计 ===============
    
    /**
     * 获取商品统计概览
     */
    @PostMapping("/goods/overview")
    public ResponseResult getGoodsStatisticsOverview(@RequestBody Map<String, Object> param) {
        return statisticsService.getGoodsStatisticsOverview(param);
    }
}
