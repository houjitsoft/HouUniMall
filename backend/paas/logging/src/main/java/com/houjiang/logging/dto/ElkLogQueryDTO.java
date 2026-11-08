package com.houjiang.logging.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * ELK 日志查询条件。
 * @author Amos
 */
@Data
@Schema(description = "ELK日志查询条件")
public class ElkLogQueryDTO {

    @Schema(description = "开始时间 yyyy-MM-dd HH:mm:ss")
    private String startTime;

    @Schema(description = "结束时间 yyyy-MM-dd HH:mm:ss")
    private String endTime;

    @Schema(description = "日志级别 DEBUG/INFO/WARN/ERROR")
    private String level;

    @Schema(description = "服务/应用名关键词")
    private String service;

    @Schema(description = "Logger 名称关键词")
    private String logger;

    @Schema(description = "消息关键词")
    private String keyword;

    @Schema(description = "ES search_after 游标（性能优于深分页）")
    private List<Object> searchAfter;
}
