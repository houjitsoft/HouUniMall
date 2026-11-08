package com.houjiang.aiagent.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI 中台 - 调用计量明细。
 *
 * <p>该表原由 GEO 写入（{@code module_name=geo}，1500+ 行历史数据），
 * 前缀本就合规，故只做所有权划转：aiagent 作为中台统一读写，
 * 后续新链路统一记 {@code module_name=aiagent}。
 */
@Data
@TableName("hou_ai_token_usage")
public class AiTokenUsage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long tenantId;

    private Long projectId;

    /** 来源模块：geo / aiagent / mall … */
    private String moduleName;

    /** 业务类型，如 ai_studio_keyword_expand / support_chat */
    private String bizType;

    private Long bizId;

    private String provider;

    private String modelName;

    private Long modelConfigId;

    private String scene;

    private String seed;

    private Long promptTokens;

    private Long completionTokens;

    private Long totalTokens;

    private BigDecimal totalPrice;

    private String currency;

    private Long latencyMs;

    private String messageId;

    private String conversationId;

    private String menuLevel1;

    private String menuLevel2;

    private String actionName;

    private String sourcePath;

    private Long createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
