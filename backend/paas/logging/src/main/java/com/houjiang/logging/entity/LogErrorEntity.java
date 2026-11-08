package com.houjiang.logging.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 错误日志实体类
 *
 * <p>映射 hou_log_error 表，记录系统运行时产生的异常信息，
 * 便于问题追踪与排查。</p>
 *
 * @author Amos
 * @since 2026-04-13
 */
@Data
@TableName("hou_log_error")
@Schema(description = "错误日志实体")
public class LogErrorEntity {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "所属模块（如pm、oa、crm）")
    private String module;

    @Schema(description = "异常类名（全限定名）")
    private String errorType;

    @Schema(description = "错误消息内容")
    private String errorMessage;

    @Schema(description = "异常堆栈信息")
    private String stackTrace;

    @Schema(description = "触发异常的请求URL")
    private String requestUrl;

    @Schema(description = "HTTP请求方法")
    private String requestMethod;

    @Schema(description = "请求参数")
    private String requestParams;

    @Schema(description = "客户端IP地址")
    private String ip;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "用户名")
    private String username;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
