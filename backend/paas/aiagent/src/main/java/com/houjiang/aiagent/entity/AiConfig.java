package com.houjiang.aiagent.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 配置项（key-value）。前端「AI设置」按扁平 map 读写，
 * 因此 {@code config_key} 必须与前端 payload 的键完全一致（如 default_model、timeout）。
 */
@Data
@TableName("hou_ai_config")
public class AiConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String configKey;

    private String configValue;

    /**
     * 配置分组（gateway / model / default ...）。
     * 表列由 27-aiagent-schema-fix.sql 补齐，初建表脚本 25-aiagent.sql 漏了该列。
     */
    private String configGroup;

    /**
     * 备注。⚠️ 表列名为 {@code config_desc}（与实体字段名不一致），
     * 必须显式 @TableField 映射，否则查询会报 Unknown column 'remark'。
     */
    @TableField("config_desc")
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
