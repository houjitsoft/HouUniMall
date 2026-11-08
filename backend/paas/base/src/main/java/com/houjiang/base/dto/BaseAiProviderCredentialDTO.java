package com.houjiang.base.dto;

import lombok.Data;

import java.util.List;

/**
 * AI 品牌凭证批量下发 DTO
 *
 * @description 按「品牌」（API 端点）聚合的共享凭证，保存时把凭证下发到该端点下的全部模型模板。
 * @author Amos
 * @since 2026-01-07
 */
@Data
public class BaseAiProviderCredentialDTO {

    /** 代表服务商代码 */
    private String providerCode;
    /** 品牌 API 端点 */
    private String apiEndpoint;
    /** 共享 API 密钥（留空表示保持原值） */
    private String apiKey;
    /** 共享 API 密钥2/Secret（留空表示保持原值） */
    private String apiSecret;
    /** 区域（数字人等） */
    private String region;
    /** 应用 ID */
    private String appId;
    /** 品牌级扩展配置 JSON */
    private String configJson;
    /** 启用状态：1 启用 0 停用 */
    private Integer isEnabled;
    /** 该品牌下的全部模型模板 */
    private List<BaseAiConfigDTO> models;
}
