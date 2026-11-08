package com.houjiang.aiagent.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 中台 - 智能体调用记录。
 *
 * <p>与 {@code hou_ai_token_usage} 的分工：本表记「哪个智能体在什么业务上被调用、结果如何」，
 * 偏业务可解释；token 用量表记计量与成本。一次调用可能只写用量表，但排障要看本表。
 */
@Data
@TableName("hou_ai_agent_invoke_log")
public class AiAgentInvokeLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** hou_ai_agent.id */
    private Long agentId;

    /** 冗余编码，便于按场景统计 */
    private String agentCode;

    private String scene;

    /** tenant / user / guest */
    private String subjectType;

    private String subjectKey;

    /** 业务单据号（订单号、商品 ID 等） */
    private String bizId;

    private String modelName;

    private String inputSummary;

    private String outputSummary;

    private Integer promptTokens;

    private Integer completionTokens;

    private Integer totalTokens;

    private Integer latencyMs;

    /** 1 成功 / 0 失败 */
    private Integer status;

    private String errorMessage;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
