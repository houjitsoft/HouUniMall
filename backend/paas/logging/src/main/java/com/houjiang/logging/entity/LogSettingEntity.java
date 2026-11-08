package com.houjiang.logging.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 日志设置实体类
 *
 * <p>映射 hou_log_setting 表，存储日志模块的全局配置项，
 * 采用 KV 键值对结构，支持动态调整日志行为。</p>
 *
 * @author Amos
 * @since 2026-04-13
 */
@Data
@TableName("hou_log_setting")
@Schema(description = "日志设置实体")
public class LogSettingEntity {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Integer id;

    @Schema(description = "配置键名（全局唯一）")
    private String settingKey;

    @Schema(description = "配置值")
    private String settingValue;

    @Schema(description = "配置项说明")
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
