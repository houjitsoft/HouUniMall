package com.houjiang.logging.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 访问日志实体类
 *
 * <p>映射 hou_log_access 表，记录所有 HTTP 请求的访问信息，
 * 用于流量分析和安全审计。</p>
 *
 * @author Amos
 * @since 2026-04-13
 */
@Data
@TableName("hou_log_access")
@Schema(description = "访问日志实体")
public class LogAccessEntity {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "用户ID（未登录时为空）")
    private String userId;

    @Schema(description = "用户名（未登录时为空）")
    private String username;

    @Schema(description = "HTTP请求方法（GET/POST/PUT/DELETE等）")
    private String method;

    @Schema(description = "请求路径")
    private String path;

    @Schema(description = "查询字符串参数")
    private String queryString;

    @Schema(description = "客户端IP地址")
    private String ip;

    @Schema(description = "浏览器User-Agent")
    private String userAgent;

    @Schema(description = "HTTP响应状态码")
    private Integer statusCode;

    @Schema(description = "响应耗时（毫秒）")
    private Integer responseTime;

    @Schema(description = "来源页面URL")
    private String referer;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
