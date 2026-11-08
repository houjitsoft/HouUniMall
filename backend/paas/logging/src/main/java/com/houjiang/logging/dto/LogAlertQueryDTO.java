package com.houjiang.logging.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 运行错误告警查询条件 DTO
 *
 * <p>所有字段均为可选，服务层根据非空字段动态构建查询条件。
 * status 不传时默认只查未处理（后台告警中心的主视角）。</p>
 *
 * @author Amos
 * @since 2026-09-25
 */
@Data
@Schema(description = "运行错误告警查询条件")
public class LogAlertQueryDTO {

    @Schema(description = "处理状态：0未处理 1已处理 2已忽略；不传默认查未处理")
    private Integer status;

    @Schema(description = "所属模块（如pm、oa、crm）")
    private String module;

    @Schema(description = "异常类型（全限定类名，模糊匹配）")
    private String errorType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "开始时间（按最近出现时间过滤）")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "结束时间（按最近出现时间过滤）")
    private LocalDateTime endTime;

    @Schema(description = "关键词（模糊匹配错误消息摘要）")
    private String keyword;
}
