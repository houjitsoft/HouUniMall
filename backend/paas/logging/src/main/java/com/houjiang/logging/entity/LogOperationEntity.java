package com.houjiang.logging.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志实体类
 *
 * <p>映射 hou_log_operation 表，记录用户在系统中的操作行为，
 * 包括请求信息、执行结果及耗时。</p>
 *
 * @author Amos
 * @since 2026-04-13
 */
@Data
@TableName("hou_log_operation")
@Schema(description = "操作日志实体")
public class LogOperationEntity {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "所属模块（如pm、oa、crm）")
    private String module;

    @Schema(description = "操作描述")
    private String operation;

    @Schema(description = "类名.方法名")
    private String method;

    @Schema(description = "HTTP请求方法（GET/POST/PUT/DELETE等）")
    private String requestMethod;

    @Schema(description = "请求URL")
    private String requestUrl;

    @Schema(description = "请求参数")
    private String requestParams;

    @Schema(description = "响应结果（超长时截断存储）")
    private String responseResult;

    @Schema(description = "客户端IP地址")
    private String ip;

    @Schema(description = "执行耗时（毫秒）")
    private Integer executionTime;

    @Schema(description = "执行状态：1成功 0失败")
    private Integer status;

    @Schema(description = "失败时的错误信息")
    private String errorMsg;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
