package com.houjiang.logging.service;

import com.houjiang.logging.dto.ElkLogQueryDTO;

import java.util.List;
import java.util.Map;

/**
 * ELK（Elasticsearch）日志查询服务。
 * @author Amos
 */
public interface ElkLogService {

    /**
     * 分页查询运行日志（系统日志 / 运行错误等）。
     *
     * @param dto     查询条件
     * @param current 页码，从 1 开始
     * @param size    每页条数
     * @return records/total/current/size/searchAfter/hasMore/elkEnabled
     */
    Map<String, Object> queryPage(ElkLogQueryDTO dto, long current, long size);

    /**
     * 日志级别分布（聚合，用于分析页）。
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 各级别数量
     */
    Map<String, Long> levelStats(String startTime, String endTime);

    /**
     * 今日 ERROR 数量（聚合）。
     */
    long countTodayErrors();

    /**
     * 今日运行日志总量（聚合）。
     */
    long countTodayTotal();

    /**
     * 热门运行错误（按 message 聚合）。
     */
    List<Map<String, Object>> topRuntimeErrors(int limit, String startTime, String endTime);

    /**
     * TODO 补充方法说明
     * @return TODO
     */
    boolean isEnabled();
}
