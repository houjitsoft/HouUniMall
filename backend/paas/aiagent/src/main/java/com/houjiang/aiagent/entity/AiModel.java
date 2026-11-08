package com.houjiang.aiagent.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI 模型池。
 *
 * <p>{@code api_key} 只允许存放 AI 网关签发的档案密钥（{@code sk-cp-…}），
 * 禁止存任何厂商密钥——厂商密钥只存在于 aigateway（见方案 D.8 第 4 条）。
 */
@Data
@TableName("hou_ai_model")
public class AiModel {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    /**
     * 模型编码，唯一。GEO 迁入的 12 个平台沿用原 code（chatgpt / deepseek …）。
     */
    private String code;

    /** 厂商：OpenAI / DeepSeek / Baidu / Alibaba … */
    private String vendor;

    /** domestic 国内 / global 海外 */
    private String region;

    /**
     * chat / text / image / code 为「可被中台调用的模型」；
     * geo_target 为「GEO 要分析的 AI 问答平台」（被分析对象，不可调用）。
     */
    private String type;

    private String apiUrl;

    private String apiKey;

    private BigDecimal temperature;

    private Integer maxTokens;

    /** 1 启用 0 禁用 */
    private Integer status;

    private String description;

    private Long usageCount;

    private Integer sort;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic(value = "0", delval = "1")
    private Integer deleteFlag;
}
