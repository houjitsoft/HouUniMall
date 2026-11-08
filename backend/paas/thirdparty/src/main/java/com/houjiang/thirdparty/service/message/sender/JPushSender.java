package com.houjiang.thirdparty.service.message.sender;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import com.houjiang.commons.http.OutboundHttpClientFactory;
import okhttp3.*;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 极光推送消息发送器
 * <p>
 * 支持：
 * 1. 通知推送（notification）
 * 2. 自定义消息推送（message）
 * 3. 单推/群推/广播
 * <p>
 * 极光 REST API v3 文档：https://docs.jiguang.cn/jpush/server/push/rest_api_v3_push
 *
 * @author Amos
 * @since 2025-07-02
 */
@Slf4j
@Component
public class JPushSender implements MessageSender {

    private static final OkHttpClient httpClient = OutboundHttpClientFactory.okHttpDefault();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final MediaType JSON_TYPE = MediaType.get("application/json; charset=utf-8");
    private static final String JPUSH_API_URL = "https://api.jpush.cn/v3/push";

    @Override
    public String getChannelCode() {
        return "JPUSH";
    }

    @Override
    public SendResult send(String receiver, String title, String content,
                           Map<String, Object> config, Map<String, Object> params) {
        try {
            String appKey = getConfigValue(config, "appKey", "");
            String masterSecret = getConfigValue(config, "masterSecret", "");

            if (appKey.isEmpty() || masterSecret.isEmpty()) {
                return SendResult.fail("CONFIG_ERROR", "极光推送配置不完整，缺少AppKey或MasterSecret");
            }

            // 构建推送请求体
            Map<String, Object> pushPayload = buildPushPayload(receiver, title, content, params);

            // 发送请求
            String credentials = Credentials.basic(appKey, masterSecret);
            String jsonBody = objectMapper.writeValueAsString(pushPayload);
            RequestBody body = RequestBody.create(jsonBody, JSON_TYPE);
            Request request = new Request.Builder()
                    .url(JPUSH_API_URL)
                    .header("Authorization", credentials)
                    .post(body)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                String respBody = response.body() != null ? response.body().string() : "";
                int code = response.code();

                if (code == 200) {
                    Map<String, Object> result = objectMapper.readValue(respBody, Map.class);
                    String sendId = result.get("sendno") != null ? result.get("sendno").toString() : UUID.randomUUID().toString();
                    log.info("极光推送成功: sendId={}", sendId);
                    return SendResult.success(sendId);
                } else {
                    log.error("极光推送失败: code={}, body={}", code, respBody);
                    return SendResult.fail(String.valueOf(code), "推送失败: " + respBody);
                }
            }

        } catch (Exception e) {
            log.error("极光推送异常: errorType={}", e.getClass().getSimpleName(), e);
            return SendResult.fail("SYSTEM_ERROR", "系统异常: " + e.getMessage());
        }
    }

    /**
     * 构建极光推送请求体
     */
    private Map<String, Object> buildPushPayload(String receiver, String title, String content,
                                                  Map<String, Object> params) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("platform", "all");

        // 构建 audience（推送目标）
        Map<String, Object> audience = new HashMap<>();
        String targetType = params != null ? getConfigValue(params, "targetType", "all") : "all";

        if ("all".equals(targetType)) {
            audience.put("all", true);
        } else if ("registration_id".equals(targetType) && receiver != null && !receiver.isEmpty()) {
            audience.put("registration_id", Arrays.asList(receiver.split(",")));
        } else if ("alias".equals(targetType) && receiver != null && !receiver.isEmpty()) {
            audience.put("alias", Arrays.asList(receiver.split(",")));
        } else if ("tag".equals(targetType) && receiver != null && !receiver.isEmpty()) {
            audience.put("tag", Arrays.asList(receiver.split(",")));
        } else {
            audience.put("all", true);
        }
        payload.put("audience", audience);

        // 消息类型
        String msgType = params != null ? getConfigValue(params, "msgType", "notification") : "notification";

        if ("notification".equals(msgType)) {
            // 通知
            Map<String, Object> notification = new HashMap<>();
            Map<String, Object> android = new HashMap<>();
            android.put("alert", content);
            android.put("title", title);
            notification.put("android", android);

            Map<String, Object> ios = new HashMap<>();
            ios.put("alert", content);
            ios.put("title", title);
            ios.put("sound", "default");
            notification.put("ios", ios);

            payload.put("notification", notification);
        } else {
            // 自定义消息
            Map<String, Object> message = new HashMap<>();
            message.put("msg_content", content);
            message.put("title", title);
            payload.put("message", message);
        }

        // 扩展字段
        if (params != null && params.containsKey("extras")) {
            payload.put("extras", params.get("extras"));
        }

        // options
        Map<String, Object> options = new HashMap<>();
        options.put("apns_production", false);
        if (params != null && params.containsKey("apnsProduction")) {
            options.put("apns_production", Boolean.parseBoolean(params.get("apnsProduction").toString()));
        }
        payload.put("options", options);

        return payload;
    }

    @Override
    public boolean validateConfig(Map<String, Object> config) {
        if (config == null) return false;
        return !getConfigValue(config, "appKey", "").isEmpty()
                && !getConfigValue(config, "masterSecret", "").isEmpty();
    }

    private String getConfigValue(Map<String, Object> config, String key, String defaultValue) {
        if (config == null) return defaultValue;
        Object value = config.get(key);
        return value != null ? value.toString() : defaultValue;
    }
}
