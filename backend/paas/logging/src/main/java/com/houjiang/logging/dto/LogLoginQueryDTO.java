package com.houjiang.logging.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 登录日志查询条件 DTO
 *
 * <p>用于接收前端传入的登录日志分页查询条件，所有字段均为可选，
 * 服务层根据非空字段动态构建查询条件。</p>
 *
 * @author Amos
 * @since 2026-04-13
 */
@Data
@Schema(description = "登录日志查询条件")
public class LogLoginQueryDTO {

    @Schema(description = "用户名（模糊匹配）")
    private String username;

    @Schema(description = "登录方式（password/sms/wechat/third）")
    private String loginType;

    @Schema(description = "登录状态：1成功 0失败")
    private Integer loginStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "客户端IP地址（精确匹配）")
    private String ip;
}
