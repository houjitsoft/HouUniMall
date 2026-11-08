package com.houjiang.logging.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 日志分析查询条件 DTO
 *
 * <p>用于接收前端传入的日志分析查询条件，支持按时间范围、模块、日志类型进行统计分析。</p>
 *
 * @author Amos
 * @since 2026-04-13
 */
@Data
@Schema(description = "日志分析查询条件")
public class LogAnalysisQueryDTO {

    @Schema(description = "时间范围（day:今天 / week:近7天 / month:近30天）", example = "day")
    private String timeRange;

    @Schema(description = "所属模块（如pm、oa、crm），为空则统计全部模块")
    private String module;

    @Schema(description = "日志类型（OPERATION/ACCESS/LOGIN/ERROR/BUSINESS），为空则统计全部类型")
    private String logType;
}
