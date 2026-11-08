package com.houjiang.logging.service;

import com.houjiang.logging.dto.LogAnalysisQueryDTO;

import java.util.List;
import java.util.Map;

/**
 * 日志分析 Service 接口
 *
 * <p>提供日志统计概览、趋势分析、热门错误等分析功能。</p>
 *
 * @author Amos
 * @since 2026-04-13
 */
public interface LogAnalysisService {

    /**
     * 获取日志统计概览
     *
     * <p>返回各类日志总数、今日新增量、错误率等汇总指标。</p>
     *
     * @return 包含统计指标的 Map，key 为指标名，value 为对应数值
     */
    Map<String, Object> getOverview();

    /**
     * 获取日志数量趋势数据
     *
     * <p>按时间范围统计每天（或每小时）的日志数量，用于前端绘制趋势图。</p>
     *
     * @param dto 分析查询条件，包含时间范围、模块、日志类型等过滤项
     * @return 趋势数据列表，每项包含 date（日期/时间）和 count（数量）
     */
    List<Map<String, Object>> getTrend(LogAnalysisQueryDTO dto);

    /**
     * 获取热门错误排行
     *
     * <p>按错误类型分组统计，返回出现次数最多的错误类型及其数量。</p>
     *
     * @param limit 返回的最大条数
     * @return 错误排行列表，每项包含 errorType（错误类型）和 count（出现次数）
     */
    List<Map<String, Object>> getTopErrors(int limit);
}
