package com.houjiang.base.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI服务配置实体类
 *
 * @description AI服务提供商配置信息
 * @author Amos
 * @since 2026-01-07
 */
@Data
@TableName("hou_base_ai_config")
public class BaseAiConfigEntity implements Serializable {
    
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("provider_code")
    private String providerCode;

    @TableField("provider_name")
    private String providerName;

    @TableField("provider_name_en")
    private String providerNameEn;

    @TableField("model_name")
    private String modelName;

    @TableField("model_version")
    private String modelVersion;

    @TableField("api_key")
    private String apiKey;

    @TableField("api_secret")
    private String apiSecret;

    @TableField("api_endpoint")
    private String apiEndpoint;

    @TableField("app_id")
    private String appId;

    @TableField("region")
    private String region;

    @TableField("max_tokens")
    private Integer maxTokens;

    @TableField("temperature")
    private BigDecimal temperature;

    @TableField("top_p")
    private BigDecimal topP;

    @TableField("frequency_penalty")
    private BigDecimal frequencyPenalty;

    @TableField("presence_penalty")
    private BigDecimal presencePenalty;

    @TableField("is_enabled")
    private Integer isEnabled;

    @TableField("is_default")
    private Integer isDefault;

    @TableField("priority")
    private Integer priority;

    @TableField("rate_limit")
    private Integer rateLimit;

    @TableField("timeout")
    private Integer timeout;

    @TableField("retry_times")
    private Integer retryTimes;

    @TableField("support_stream")
    private Integer supportStream;

    @TableField("support_function")
    private Integer supportFunction;

    @TableField("support_vision")
    private Integer supportVision;

    @TableField("cost_per_1k_input")
    private BigDecimal costPer1kInput;

    @TableField("cost_per_1k_output")
    private BigDecimal costPer1kOutput;

    @TableField("description")
    private String description;

    @TableField("config_json")
    private String configJson;

    @TableField("status")
    private Integer status;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;
}
