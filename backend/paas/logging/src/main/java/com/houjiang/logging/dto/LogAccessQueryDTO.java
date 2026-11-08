package com.houjiang.logging.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 访问日志查询条件 DTO
 *
 * <p>用于接收前端传入的访问日志分页查询条件，所有字段均为可选，
 * 服务层根据非空字段动态构建查询条件。</p>
 *
 * @author Amos
 * @since 2026-04-13
 */
@Data
@Schema(description = "访问日志查询条件")
public class LogAccessQueryDTO {

    @Schema(description = "用户名（模糊匹配）")
    private String username;

    @Schema(description = "HTTP请求方法（GET/POST/PUT/DELETE等）")
    private String method;

    @Schema(description = "请求路径（模糊匹配）")
    private String path;

    @Schema(description = "HTTP响应状态码")
    private Integer statusCode;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "客户端IP地址（精确匹配）")
    private String ip;
}
