package com.houjiang.thirdparty.dingtalk.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 钉钉开放平台 HTTP 客户端（获取 access_token 等）
 * @author Amos
 */
@Slf4j
@Component
public class DingtalkApiClient {

    private static final String GET_TOKEN_URL = "https://oapi.dingtalk.com/gettoken";

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 获取 access_token，失败返回 null
     */
    public String getAccessToken(String appKey, String appSecret) {
        if (!StringUtils.hasText(appKey) || !StringUtils.hasText(appSecret)) {
            return null;
        }
        try {
            String url = GET_TOKEN_URL + "?appkey=" + appKey + "&appsecret=" + appSecret;
            Request request = new Request.Builder().url(url).get().build();
            try (Response response = httpClient.newCall(request).execute()) {
                String body = response.body() != null ? response.body().string() : "";
                Map<String, Object> result = objectMapper.readValue(body, new TypeReference<>() {});
                Object errcode = result.get("errcode");
                if (errcode instanceof Number && ((Number) errcode).intValue() == 0) {
                    return (String) result.get("access_token");
                }
                log.warn("钉钉 gettoken 失败: {}", body);
            }
        } catch (Exception e) {
            log.error("钉钉 gettoken 异常: {}", e.getMessage(), e);
        }
        return null;
    }

    /**
     * 测试 AppKey/AppSecret 是否可用
     */
    public boolean testConnection(String appKey, String appSecret) {
        return StringUtils.hasText(getAccessToken(appKey, appSecret));
    }

    /**
     * 健康检查详情
     */
    public Map<String, Object> healthCheck(String appKey, String appSecret) {
        Map<String, Object> health = new HashMap<>();
        if (!StringUtils.hasText(appKey) || !StringUtils.hasText(appSecret)) {
            health.put("connected", false);
            health.put("message", "AppKey 或 AppSecret 未配置");
            return health;
        }
        String token = getAccessToken(appKey, appSecret);
        health.put("connected", StringUtils.hasText(token));
        health.put("message", StringUtils.hasText(token) ? "连接正常" : "无法获取 access_token，请检查凭证");
        return health;
    }
}
