package com.houjiang.logging.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 错误日志查询条件 DTO
 *
 * <p>用于接收前端传入的错误日志分页查询条件，所有字段均为可选，
 * 服务层根据非空字段动态构建查询条件。</p>
 *
 * @author Amos
 * @since 2026-04-13
 */
@Data
@Schema(description = "错误日志查询条件")
public class LogErrorQueryDTO {

    @Schema(description = "所属模块（如pm、oa、crm）")
    private String module;

    @Schema(description = "异常类型（全限定类名，模糊匹配）")
    private String errorType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "关键词（模糊匹配错误消息内容）")
    private String keyword;
}
