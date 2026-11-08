package com.houjiang.base.dto;

import com.houjiang.commons.util.querypage.PageHelper;
import lombok.Data;

import java.math.BigDecimal;

/**
 * AI服务配置DTO
 *
 * @description AI服务配置查询和操作参数
 * @author Amos
 * @since 2026-01-07
 */
@Data
public class BaseAiConfigDTO extends PageHelper {

    private Integer id;
    private String providerCode;
    private String providerName;
    private String providerNameEn;
    private String modelName;
    private String modelVersion;
    private String apiKey;
    private String apiSecret;
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

    /** 查询参数 */
    private String keyword;
}
