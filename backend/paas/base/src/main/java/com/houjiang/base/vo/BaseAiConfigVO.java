package com.houjiang.base.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI服务配置VO
 *
 * @description AI服务配置返回对象
 * @author Amos
 * @since 2026-01-07
 */
@Data
public class BaseAiConfigVO {

    private Integer id;
    private String providerCode;
    private String providerName;
    private String providerNameEn;
    private String modelName;
    private String modelVersion;
    private String apiKey;
    private Boolean apiKeyConfigured;
    private Boolean apiSecretConfigured;
    private Boolean chatLicenseConfigured;
    private String apiEndpoint;
    private String appId;
    private String region;
    private Integer maxTokens;
    private BigDecimal temperature;
    private BigDecimal topP;
    private BigDecimal frequencyPenalty;
    private BigDecimal presencePenalty;
    private Integer isEnabled;
    private Integer isDefault;
    private Integer priority;
    private Integer rateLimit;
    private Integer timeout;
    private Integer retryTimes;
    private Integer supportStream;
    private Integer supportFunction;
    private Integer supportVision;
    private BigDecimal costPer1kInput;
    private BigDecimal costPer1kOutput;
    private String description;
    private String configJson;
    private Integer status;
    private Integer sortOrder;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
