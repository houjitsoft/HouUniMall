package com.houjiang.thirdparty.service.message.sender;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.houjiang.commons.http.OutboundHttpClientFactory;
import com.lark.oapi.Client;
import com.lark.oapi.core.utils.Jsons;
import com.lark.oapi.service.im.v1.enums.CreateMessageReceiveIdTypeEnum;
import com.lark.oapi.service.im.v1.model.CreateMessageReq;
import com.lark.oapi.service.im.v1.model.CreateMessageReqBody;
import com.lark.oapi.service.im.v1.model.CreateMessageResp;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 飞书消息发送器
 * 支持：
 * 1. 飞书机器人 Webhook
 * 2. 飞书应用消息
 * 
 * @author Amos
 * @since 2025-12-26
 */
@Slf4j
@Component
public class FeishuSender implements MessageSender {
    
    private static final OkHttpClient httpClient = OutboundHttpClientFactory.okHttpExternal();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    
    /** access_token 缓存 */
    private static final ConcurrentHashMap<String, TokenCache> tokenCache = new ConcurrentHashMap<>();
    
    /** SDK客户端缓存，key为appId */
    private static final ConcurrentHashMap<String, Client> sdkClients = new ConcurrentHashMap<>();
    
    @Override
    public String getChannelCode() {
        return "FEISHU";
    }
    
    @Override
    public SendResult send(String receiver, String title, String content, 
                           Map<String, Object> config, Map<String, Object> params) {
        try {
            String type = getConfigValue(config, "type", "webhook");
            
            if ("webhook".equals(type)) {
                return sendByWebhook(receiver, title, content, config, params);
            } else if ("app".equals(type)) {
                return sendByApp(receiver, title, content, config, params);
            } else {
                return SendResult.fail("CONFIG_ERROR", "不支持的发送类型: " + type);
            }
            
        } catch (Exception e) {
            log.error("飞书消息发送异常: errorType={}", e.getClass().getSimpleName(), e);
            return SendResult.fail("SYSTEM_ERROR", "系统异常: " + e.getMessage());
        }
    }
    
    /**
     * 通过 Webhook 发送消息（机器人）
     */
    private SendResult sendByWebhook(String receiver, String title, String content, 
                                      Map<String, Object> config, Map<String, Object> params) throws Exception {
        String webhook = getConfigValue(config, "webhook", "");
        String secret = getConfigValue(config, "secret", "");
        
        if (webhook.isEmpty()) {
            return SendResult.fail("CONFIG_ERROR", "飞书配置不完整，缺少Webhook地址");
        }
        
        // 构建消息体
        Map<String, Object> msgBody = new HashMap<>();
        
        // 如果有签名密钥
        if (!secret.isEmpty()) {
            long timestamp = System.currentTimeMillis() / 1000;
            String sign = generateSign(timestamp, secret);
            msgBody.put("timestamp", String.valueOf(timestamp));
            msgBody.put("sign", sign);
        }
        
        String msgType = params != null ? getConfigValue(params, "msgType", "text") : "text";
        msgBody.put("msg_type", msgType);
        
        if ("text".equals(msgType)) {
            Map<String, Object> contentMap = new HashMap<>();
            contentMap.put("text", title + "\n" + content);
            msgBody.put("content", contentMap);
        } else if ("post".equals(msgType)) {
            // 富文本消息
            Map<String, Object> postContent = new HashMap<>();
            Map<String, Object> zhCn = new HashMap<>();
            zhCn.put("title", title);
            
            List<List<Map<String, Object>>> contentList = new ArrayList<>();
            List<Map<String, Object>> line = new ArrayList<>();
            Map<String, Object> textItem = new HashMap<>();
            textItem.put("tag", "text");
            textItem.put("text", content);
            line.add(textItem);
            contentList.add(line);
            zhCn.put("content", contentList);
            
            postContent.put("zh_cn", zhCn);
            Map<String, Object> post = new HashMap<>();
            post.put("post", postContent);
            msgBody.put("content", post);
        } else if ("interactive".equals(msgType)) {
            // 卡片消息
            Map<String, Object> card = new HashMap<>();
            Map<String, Object> header = new HashMap<>();
            header.put("title", Map.of("content", title, "tag", "plain_text"));
            card.put("header", header);
            
            List<Map<String, Object>> elements = new ArrayList<>();
            Map<String, Object> element = new HashMap<>();
            element.put("tag", "div");
            element.put("text", Map.of("content", content, "tag", "plain_text"));
            elements.add(element);
            card.put("elements", elements);
            
            msgBody.put("card", card);
            msgBody.put("msg_type", "interactive");
        }
        
        // 发送请求
        String jsonBody = objectMapper.writeValueAsString(msgBody);
        RequestBody body = RequestBody.create(jsonBody, JSON);
        Request request = new Request.Builder()
            .url(webhook)
            .post(body)
            .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            String respBody = response.body() != null ? response.body().string() : "";
            Map<String, Object> result = objectMapper.readValue(respBody, Map.class);
            
            Integer code = (Integer) result.get("code");
            if (code != null && code == 0) {
                String messageId = UUID.randomUUID().toString();
                log.info("飞书Webhook消息发送成功: messageId={}", messageId);
                return SendResult.success(messageId);
            } else {
                String msg = (String) result.get("msg");
                log.error("飞书Webhook消息发送失败: code={}, msg={}", code, msg);
                return SendResult.fail(String.valueOf(code), msg);
            }
        }
    }
    
    /**
     * 通过应用发送消息（使用SDK）
     */
    private SendResult sendByApp(String receiver, String title, String content, 
                                  Map<String, Object> config, Map<String, Object> params) throws Exception {
        String appId = getConfigValue(config, "appId", "");
        String appSecret = getConfigValue(config, "appSecret", "");
        
        if (appId.isEmpty() || appSecret.isEmpty()) {
            return SendResult.fail("CONFIG_ERROR", "飞书配置不完整，缺少AppId/AppSecret");
        }
        
        try {
            // 获取或创建SDK客户端
            Client client = getOrCreateSdkClient(appId, appSecret);
            
            // 构建消息内容（JSON格式）
            String messageContent = title + "\n" + content;
            // 飞书SDK要求content是JSON字符串格式：{"text":"消息内容"}
            Map<String, String> contentMap = new HashMap<>();
            contentMap.put("text", messageContent);
            String contentJson = Jsons.DEFAULT.toJson(contentMap);
            
            // 构建请求
            CreateMessageReq req = CreateMessageReq.newBuilder()
                    .receiveIdType(CreateMessageReceiveIdTypeEnum.USER_ID)
                    .createMessageReqBody(CreateMessageReqBody.newBuilder()
                            .receiveId(receiver)
                            .msgType("text")
                            .content(contentJson)
                            .build())
                    .build();
            
            // 发送消息
            CreateMessageResp resp = client.im().message().create(req);
            
            // 处理响应
            if (resp.success()) {
                String messageId = resp.getData().getMessageId();
                log.info("飞书应用消息发送成功（SDK）: messageId={}", messageId);
                return SendResult.success(messageId);
            } else {
                log.error("飞书应用消息发送失败（SDK）: code={}, msg={}, requestId={}", 
                        resp.getCode(), resp.getMsg(), resp.getRequestId());
                return SendResult.fail(String.valueOf(resp.getCode()), resp.getMsg());
            }
        } catch (Exception e) {
            log.error("使用SDK发送飞书消息异常: error={}", e.getMessage(), e);
            // 如果SDK调用失败，降级到HTTP方式
            return sendByAppHttp(receiver, title, content, config, params);
        }
    }
    
    /**
     * 通过HTTP方式发送应用消息（降级方案）
     */
    private SendResult sendByAppHttp(String receiver, String title, String content, 
                                     Map<String, Object> config, Map<String, Object> params) throws Exception {
        String appId = getConfigValue(config, "appId", "");
        String appSecret = getConfigValue(config, "appSecret", "");
        
        // 获取 tenant_access_token
        String accessToken = getAccessToken(appId, appSecret);
        if (accessToken == null) {
            return SendResult.fail("AUTH_ERROR", "获取飞书access_token失败");
        }
        
        // 发送应用消息
        String url = "https://open.feishu.cn/open-apis/im/v1/messages?receive_id_type=user_id";
        
        Map<String, Object> msgBody = new HashMap<>();
        msgBody.put("receive_id", receiver);
        msgBody.put("msg_type", "text");
        
        Map<String, Object> contentMap = new HashMap<>();
        contentMap.put("text", title + "\n" + content);
        msgBody.put("content", objectMapper.writeValueAsString(contentMap));
        
        String jsonBody = objectMapper.writeValueAsString(msgBody);
        RequestBody body = RequestBody.create(jsonBody, JSON);
        Request request = new Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer " + accessToken)
            .post(body)
            .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            String respBody = response.body() != null ? response.body().string() : "";
            Map<String, Object> result = objectMapper.readValue(respBody, Map.class);
            
            Integer code = (Integer) result.get("code");
            if (code != null && code == 0) {
                Map<String, Object> data = (Map<String, Object>) result.get("data");
                String messageId = data != null ? (String) data.get("message_id") : UUID.randomUUID().toString();
                log.info("飞书应用消息发送成功（HTTP）: messageId={}", messageId);
                return SendResult.success(messageId);
            } else {
                String msg = (String) result.get("msg");
                log.error("飞书应用消息发送失败（HTTP）: code={}, msg={}", code, msg);
                return SendResult.fail(String.valueOf(code), msg);
            }
        }
    }
    
    /**
     * 获取或创建SDK客户端
     */
    private Client getOrCreateSdkClient(String appId, String appSecret) {
        return sdkClients.computeIfAbsent(appId, key -> {
            log.info("创建飞书SDK客户端: appId={}", appId);
            return Client.newBuilder(appId, appSecret)
                    // 生产环境关闭详细日志
                    .logReqAtDebug(false)
                    .build();
        });
    }
    
    /**
     * 获取飞书 tenant_access_token (带缓存)
     */
    private String getAccessToken(String appId, String appSecret) {
        String cacheKey = appId + "_" + appSecret;
        TokenCache cache = tokenCache.get(cacheKey);
        
        if (cache != null && cache.isValid()) {
            return cache.token;
        }
        
        try {
            String url = "https://open.feishu.cn/open-apis/auth/v3/tenant_access_token/internal";
            
            Map<String, String> params = new HashMap<>();
            params.put("app_id", appId);
            params.put("app_secret", appSecret);
            
            String jsonBody = objectMapper.writeValueAsString(params);
            RequestBody body = RequestBody.create(jsonBody, JSON);
            Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                String respBody = response.body() != null ? response.body().string() : "";
                Map<String, Object> result = objectMapper.readValue(respBody, Map.class);
                
                Integer code = (Integer) result.get("code");
                if (code != null && code == 0) {
                    String token = (String) result.get("tenant_access_token");
                    Integer expire = (Integer) result.get("expire");
                    // 缓存token，提前5分钟过期
                    tokenCache.put(cacheKey, new TokenCache(token, expire - 300));
                    return token;
                }
            }
        } catch (Exception e) {
            log.error("获取飞书access_token失败: {}", e.getMessage(), e);
        }
        return null;
    }
    
    /**
     * 生成签名
     */
    private String generateSign(long timestamp, String secret) throws Exception {
        String stringToSign = timestamp + "\n" + secret;
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(new byte[0], "HmacSHA256"));
        byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(signData);
    }
    
    @Override
    public boolean validateConfig(Map<String, Object> config) {
        if (config == null) return false;
        String type = getConfigValue(config, "type", "webhook");
        
        if ("webhook".equals(type)) {
            return !getConfigValue(config, "webhook", "").isEmpty();
        } else if ("app".equals(type)) {
            return !getConfigValue(config, "appId", "").isEmpty() 
                && !getConfigValue(config, "appSecret", "").isEmpty();
        }
        return false;
    }
    
    private String getConfigValue(Map<String, Object> config, String key, String defaultValue) {
        if (config == null) return defaultValue;
        Object value = config.get(key);
        return value != null ? value.toString() : defaultValue;
    }
    
    /**
     * Token缓存类
     */
    private static class TokenCache {
        String token;
        long expireTime;
        
        TokenCache(String token, int expiresIn) {
            this.token = token;
            this.expireTime = System.currentTimeMillis() + expiresIn * 1000L;
        }
        
        boolean isValid() {
            return System.currentTimeMillis() < expireTime;
        }
    }
}
