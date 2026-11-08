package com.houjiang.logging.controller;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.logging.dto.LogAnalysisQueryDTO;
import com.houjiang.logging.service.LogAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 日志分析 Controller
 *
 * <p>提供日志统计概览、趋势数据、热门错误排行等数据分析接口，
 * 供前端管理后台数据看板使用。</p>
 *
 * @author Amos
 * @since 2026-04-13
 */
@Slf4j
@RestController
@RequestMapping("/logging/analysis")
@RequiredArgsConstructor
@Tag(name = "日志分析", description = "日志统计、趋势与排行分析接口")
public class LogAnalysisController {

    private final LogAnalysisService analysisService;

    /**
     * 获取日志统计概览
     *
     * <p>返回各类日志总数、今日新增量、错误率等汇总指标。</p>
     *
     * @return 统计概览数据 Map
     */
    @Operation(summary = "日志统计概览")
    @GetMapping("/overview")
    public ResponseResult<Map<String, Object>> overview() {
        return ResponseResult.success(analysisService.getOverview());
    }

    /**
     * 获取日志数量趋势数据
     *
     * <p>按时间范围统计每天的日志数量，支持按模块和日志类型过滤。</p>
     *
     * @param dto 分析查询条件
     * @return 趋势数据列表，每项包含 date 和 count
     */
    @Operation(summary = "日志数量趋势分析")
    @PostMapping("/trend")
    public ResponseResult<List<Map<String, Object>>> trend(@RequestBody LogAnalysisQueryDTO dto) {
        return ResponseResult.success(analysisService.getTrend(dto));
    }

    /**
     * 获取热门错误排行
     *
     * <p>按错误类型分组统计，返回出现次数最多的前 N 条。</p>
     *
     * @param limit 返回最大条数，默认10
     * @return 错误排行列表，每项包含 errorType 和 count
     */
    @Operation(summary = "热门错误排行")
    @GetMapping("/top-errors")
    public ResponseResult<List<Map<String, Object>>> topErrors(
            @Parameter(description = "返回最大条数，默认10")
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseResult.success(analysisService.getTopErrors(limit));
    }
}
