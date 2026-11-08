package com.houjiang.logging.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.houjiang.logging.dto.LogAnalysisQueryDTO;
import com.houjiang.logging.entity.LogErrorEntity;
import com.houjiang.logging.entity.LogOperationEntity;
import com.houjiang.logging.mapper.LogAccessMapper;
import com.houjiang.logging.mapper.LogBusinessMapper;
import com.houjiang.logging.mapper.LogErrorMapper;
import com.houjiang.logging.mapper.LogLoginMapper;
import com.houjiang.logging.mapper.LogOperationMapper;
import com.houjiang.logging.service.ElkLogService;
import com.houjiang.logging.service.LogAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 日志分析 Service 实现类
 *
 * <p>实现日志统计概览、趋势分析、热门错误排行等分析功能。</p>
 *
 * @author Amos
 * @since 2026-04-13
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LogAnalysisServiceImpl implements LogAnalysisService {

    private final LogOperationMapper operationMapper;
    private final LogAccessMapper accessMapper;
    private final LogLoginMapper loginMapper;
    private final LogErrorMapper errorMapper;
    private final LogBusinessMapper businessMapper;
    private final ObjectProvider<ElkLogService> elkLogServiceProvider;

    /**
     * 获取日志统计概览
     *
     * <p>统计各类日志总数、今日新增量以及错误率等汇总指标。</p>
     *
     * @return 包含统计指标的 Map
     */
    @Override
    public Map<String, Object> getOverview() {
        Map<String, Object> result = new LinkedHashMap<>();

        // 各类日志总数
        result.put("operationTotal", operationMapper.selectCount(null));
        result.put("accessTotal", accessMapper.selectCount(null));
        result.put("loginTotal", loginMapper.selectCount(null));
        result.put("errorTotal", errorMapper.selectCount(null));
        result.put("businessTotal", businessMapper.selectCount(null));

        // 今日新增
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        QueryWrapper<LogOperationEntity> opWrapper = new QueryWrapper<LogOperationEntity>()
                .ge("create_time", todayStart);
        result.put("operationToday", operationMapper.selectCount(opWrapper));

        QueryWrapper<LogErrorEntity> errWrapper = new QueryWrapper<LogErrorEntity>()
                .ge("create_time", todayStart);
        long errorToday = errorMapper.selectCount(errWrapper);
        result.put("errorToday", errorToday);

        // 错误率：今日错误数 / 今日操作数（避免除零）
        long opToday = (long) result.get("operationToday");
        double errorRate = opToday > 0 ? (double) errorToday / opToday * 100 : 0.0;
        result.put("errorRate", String.format("%.2f", errorRate) + "%");

        // 审计库汇总（供前端卡片展示）
        long auditTotal = toLong(result.get("operationTotal"))
                + toLong(result.get("accessTotal"))
                + toLong(result.get("loginTotal"))
                + toLong(result.get("errorTotal"))
                + toLong(result.get("businessTotal"));
        result.put("auditTotal", auditTotal);
        result.put("totalLogs", auditTotal);

        // ELK 运行日志统计（可选）
        ElkLogService elk = elkLogServiceProvider.getIfAvailable();
        if (elk != null && elk.isEnabled()) {
            try {
                Map<String, Long> levelStats = elk.levelStats(
                        todayStart.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                long elkError = levelStats.getOrDefault("ERROR", 0L) + levelStats.getOrDefault("FATAL", 0L);
                long elkWarn = levelStats.getOrDefault("WARN", 0L);
                long elkInfo = levelStats.getOrDefault("INFO", 0L);
                long elkToday = elk.countTodayTotal();

                result.put("elkEnabled", true);
                result.put("runtimeTotal", elkToday);
                result.put("runtimeErrorToday", elkError);
                result.put("runtimeWarnToday", elkWarn);
                result.put("runtimeInfoToday", elkInfo);
                result.put("totalLogs", auditTotal + elkToday);
                result.put("errorLogs", errorToday + elkError);
                result.put("warningLogs", elkWarn);
                result.put("infoLogs", elkInfo);
            } catch (Exception e) {
                log.warn("[LogAnalysis] ELK 统计失败: {}", e.getMessage());
                result.put("elkEnabled", false);
                result.put("errorLogs", errorToday);
                result.put("warningLogs", 0);
                result.put("infoLogs", opToday);
            }
        } else {
            result.put("elkEnabled", false);
            result.put("errorLogs", errorToday);
            result.put("warningLogs", 0);
            result.put("infoLogs", opToday);
        }

        return result;
    }

    /**
     * 获取日志数量趋势数据
     *
     * <p>按时间范围（day/week/month）统计每天的日志数量，
     * 根据 dto.logType 决定统计哪类日志。</p>
     *
     * @param dto 分析查询条件
     * @return 趋势数据列表，每项包含 date 和 count
     */
    @Override
    public List<Map<String, Object>> getTrend(LogAnalysisQueryDTO dto) {
        // 确定时间范围
        int days = 7;
        if ("day".equals(dto.getTimeRange())) {
            days = 1;
        } else if ("month".equals(dto.getTimeRange())) {
            days = 30;
        }

        List<Map<String, Object>> trend = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.plusDays(1).atStartOfDay();

            long count = countByTypeAndRange(dto.getLogType(), dto.getModule(), start, end);

            Map<String, Object> item = new HashMap<>();
            item.put("date", date.format(formatter));
            item.put("count", count);
            trend.add(item);
        }

        return trend;
    }

    /**
     * 根据日志类型和模块统计指定时间范围内的日志数量
     *
     * @param logType 日志类型（OPERATION/ACCESS/LOGIN/ERROR/BUSINESS），为空统计操作日志
     * @param module  模块名称，为空则不过滤
     * @param start   开始时间
     * @param end     结束时间
     * @return 日志数量
     */
    private long countByTypeAndRange(String logType, String module, LocalDateTime start, LocalDateTime end) {
        if ("ACCESS".equalsIgnoreCase(logType)) {
            QueryWrapper<com.houjiang.logging.entity.LogAccessEntity> w = new QueryWrapper<>();
            w.ge("create_time", start).lt("create_time", end);
            return accessMapper.selectCount(w);
        } else if ("LOGIN".equalsIgnoreCase(logType)) {
            QueryWrapper<com.houjiang.logging.entity.LogLoginEntity> w = new QueryWrapper<>();
            w.ge("create_time", start).lt("create_time", end);
            return loginMapper.selectCount(w);
        } else if ("ERROR".equalsIgnoreCase(logType)) {
            QueryWrapper<LogErrorEntity> w = new QueryWrapper<>();
            w.ge("create_time", start).lt("create_time", end);
            if (StringUtils.hasText(module)) w.eq("module", module);
            return errorMapper.selectCount(w);
        } else if ("BUSINESS".equalsIgnoreCase(logType)) {
            QueryWrapper<com.houjiang.logging.entity.LogBusinessEntity> w = new QueryWrapper<>();
            w.ge("create_time", start).lt("create_time", end);
            if (StringUtils.hasText(module)) w.eq("module", module);
            return businessMapper.selectCount(w);
        } else {
            // 默认统计操作日志
            QueryWrapper<LogOperationEntity> w = new QueryWrapper<>();
            w.ge("create_time", start).lt("create_time", end);
            if (StringUtils.hasText(module)) w.eq("module", module);
            return operationMapper.selectCount(w);
        }
    }

    /**
     * 获取热门错误排行
     *
     * <p>按 error_type 分组统计，返回出现次数最多的前 limit 条。</p>
     *
     * @param limit 返回最大条数
     * @return 错误排行列表，每项包含 errorType 和 count
     */
    @Override
    public List<Map<String, Object>> getTopErrors(int limit) {
        List<Map<String, Object>> result = new ArrayList<>();

        // 审计库错误
        List<LogErrorEntity> errors = errorMapper.selectList(
                new QueryWrapper<LogErrorEntity>().orderByDesc("create_time").last("LIMIT 5000")
        );
        Map<String, Long> countMap = new LinkedHashMap<>();
        for (LogErrorEntity e : errors) {
            String type = e.getErrorType();
            if (!StringUtils.hasText(type)) type = "Unknown";
            countMap.merge(type, 1L, Long::sum);
        }
        countMap.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .forEach(entry -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("errorType", entry.getKey());
                    item.put("error", entry.getKey());
                    item.put("count", entry.getValue());
                    item.put("source", "audit");
                    result.add(item);
                });

        // 合并 ELK 运行错误（取前 limit 条补充）
        ElkLogService elk = elkLogServiceProvider.getIfAvailable();
        if (elk != null && elk.isEnabled()) {
            try {
                LocalDateTime end = LocalDateTime.now();
                LocalDateTime start = end.minusDays(7);
                List<Map<String, Object>> runtime = elk.topRuntimeErrors(limit,
                        start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                        end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                for (Map<String, Object> r : runtime) {
                    r.put("source", "elk");
                    result.add(r);
                }
            } catch (Exception e) {
                log.warn("[LogAnalysis] ELK top errors 失败: {}", e.getMessage());
            }
        }

        return result.stream().limit(limit).toList();
    }

    private static long toLong(Object v) {
        return v instanceof Number ? ((Number) v).longValue() : 0L;
    }
}
