package com.houjiang.logging.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 业务日志查询条件 DTO
 *
 * <p>用于接收前端传入的业务日志分页查询条件，所有字段均为可选，
 * 服务层根据非空字段动态构建查询条件。</p>
 *
 * @author Amos
 * @since 2026-04-13
 */
@Data
@Schema(description = "业务日志查询条件")
public class LogBusinessQueryDTO {

    @Schema(description = "用户名（模糊匹配）")
    private String username;

    @Schema(description = "所属模块（如pm、oa、crm）")
    private String module;

    @Schema(description = "操作动作（create/update/delete/approve）")
    private String action;

    @Schema(description = "目标对象类型（如project、task、contract）")
    private String targetType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "关键词（模糊匹配操作详情描述）")
    private String keyword;
}
