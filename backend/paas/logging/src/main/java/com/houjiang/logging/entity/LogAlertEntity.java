package com.houjiang.logging.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 运行错误告警实体类
 *
 * <p>映射 hou_log_alert 表。错误日志按「指纹」聚合：同一模块 + 同一异常类 +
 * 归一化后的错误消息首行视为同一个问题，只产生一条告警并累计出现次数，
 * 避免同一个 Bug 把后台刷爆。</p>
 *
 * <p>状态流转：0 未处理 → 1 已处理（修复后由运维标记）或 2 已忽略。
 * 已处理的告警若再次出现，会重新打开为未处理并累计次数。</p>
 *
 * @author Amos
 * @since 2026-09-25
 */
@Data
@TableName("hou_log_alert")
@Schema(description = "运行错误告警实体")
public class LogAlertEntity {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "错误指纹（module|异常类|归一化消息首行 的MD5）")
    private String fingerprint;

    @Schema(description = "所属模块（如pm、oa、crm）")
    private String module;

    @Schema(description = "异常类名（全限定名）")
    private String errorType;

    @Schema(description = "错误消息（归一化后的摘要）")
    private String errorMessage;

    @Schema(description = "最近一次的异常堆栈")
    private String stackTrace;

    @Schema(description = "触发错误的请求URL")
    private String requestUrl;

    @Schema(description = "HTTP请求方法")
    private String requestMethod;

    @Schema(description = "最近一次触发的用户ID")
    private String userId;

    @Schema(description = "最近一次触发的用户名")
    private String username;

    @Schema(description = "最近一次触发的IP")
    private String ip;

    @Schema(description = "累计出现次数")
    private Integer hitCount;

    @Schema(description = "处理状态：0未处理 1已处理 2已忽略")
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "首次出现时间")
    private LocalDateTime firstTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "最近出现时间")
    private LocalDateTime lastTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "处理时间")
    private LocalDateTime handleTime;

    @Schema(description = "处理备注（记录修复原因，便于回溯）")
    private String handleRemark;
}
