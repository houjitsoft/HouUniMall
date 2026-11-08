package com.houjiang.aiagent.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用量日结。token 以 AI 网关返回的 usage 为准，这里只按场景/模型做业务归因，
 * 用于成本分摊与配额。日终与网关对账，误差大于 1% 需要排查。
 */
@Data
@TableName("hou_ai_usage_daily")
public class AiUsageDaily {

    @TableId(type = IdType.AUTO)
    private Long id;

    private LocalDate statDate;

    private String scene;

    private String modelName;

    private Long requestCount;

    private Long promptTokens;

    private Long completionTokens;

    private Long totalTokens;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
