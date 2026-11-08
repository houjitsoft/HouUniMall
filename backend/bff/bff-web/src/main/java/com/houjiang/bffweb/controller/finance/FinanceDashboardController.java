package com.houjiang.bffweb.controller.finance;

import com.houjiang.bffweb.feign.FinanceServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 财务Dashboard控制器
/**
 * @author Amos
 */
@Slf4j
@RestController
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "houcloud.legacy-bff.enabled", havingValue = "true")
@RequestMapping("/web/finance/dashboard")
@Tag(name = "财务Dashboard", description = "财务中心Dashboard相关接口")
public class FinanceDashboardController {

    @Autowired
    private FinanceServiceFeign financeServiceFeign;

    @GetMapping("/statistics")
    @Operation(summary = "获取财务统计数据")
    public ResponseResult getStatistics(@RequestParam(required = false) Integer year) {
        return financeServiceFeign.getDashboardStatistics(year);
    }

    @GetMapping("/cashflow")
    @Operation(summary = "获取资金流水")
    public ResponseResult getCashFlow(@RequestParam(required = false) Map<String, Object> param) {
        return financeServiceFeign.getDashboardCashFlow(param != null ? param : Map.of());
    }

    @GetMapping("/data")
    @Operation(summary = "获取Dashboard完整数据")
    public ResponseResult getDashboardData() {
        return financeServiceFeign.getDashboardData();
    }

    @GetMapping("/statistics/period")
    @Operation(summary = "获取按时间段的统计数据")
    public ResponseResult getStatisticsByPeriod() {
        return financeServiceFeign.getStatisticsByPeriod();
    }

    @GetMapping("/statistics/category")
    @Operation(summary = "获取按类别的统计数据")
    public ResponseResult getStatisticsByCategory() {
        return financeServiceFeign.getStatisticsByCategory();
    }

    @GetMapping("/statistics/status")
    @Operation(summary = "获取按状态的统计数据")
    public ResponseResult getStatisticsByStatus() {
        return financeServiceFeign.getStatisticsByStatus();
    }

    @GetMapping("/statistics/yearly-monthly")
    @Operation(summary = "获取历年每月收支统计")
    public ResponseResult getYearlyMonthlyStatistics(@RequestParam(required = false) String years) {
        return financeServiceFeign.getYearlyMonthlyStatistics(years);
    }
}
