package com.houjiang.thirdparty.service.message.sender;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import com.houjiang.commons.http.OutboundHttpClientFactory;
import okhttp3.*;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 企业微信消息发送器
 * 支持：
 * 1. 企业微信机器人 Webhook
 * 2. 企业微信应用消息
 * 
 * @author Amos
 * @since 2025-12-26
 */
@Slf4j
@Component
public class WecomSender implements MessageSender {
    
    private static final OkHttpClient httpClient = OutboundHttpClientFactory.okHttpExternal();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    
    /** access_token 缓存 */
    private static final ConcurrentHashMap<String, TokenCache> tokenCache = new ConcurrentHashMap<>();
    
    @Override
    public String getChannelCode() {
        return "WECOM";
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
            log.error("企业微信消息发送异常: errorType={}", e.getClass().getSimpleName(), e);
            return SendResult.fail("SYSTEM_ERROR", "系统异常: " + e.getMessage());
        }
    }
    
    /**
     * 通过 Webhook 发送消息（机器人）
     */
    private SendResult sendByWebhook(String receiver, String title, String content, 
                                      Map<String, Object> config, Map<String, Object> params) throws Exception {
        String webhook = getConfigValue(config, "webhook", "");
        
        if (webhook.isEmpty()) {
            return SendResult.fail("CONFIG_ERROR", "企业微信配置不完整，缺少Webhook地址");
        }
        
        // 构建消息体
        Map<String, Object> msgBody = new HashMap<>();
        
        String msgType = params != null ? getConfigValue(params, "msgType", "text") : "text";
        msgBody.put("msgtype", msgType);
        
        if ("text".equals(msgType)) {
            Map<String, Object> text = new HashMap<>();
            text.put("content", title + "\n" + content);
            if (receiver != null && !receiver.isEmpty()) {
                text.put("mentioned_mobile_list", receiver.split(","));
            }
            msgBody.put("text", text);
        } else if ("markdown".equals(msgType)) {
            Map<String, Object> markdown = new HashMap<>();
            markdown.put("content", "### " + title + "\n" + content);
            msgBody.put("markdown", markdown);
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
            
            Integer errcode = (Integer) result.get("errcode");
            if (errcode != null && errcode == 0) {
                String messageId = UUID.randomUUID().toString();
                log.info("企业微信Webhook消息发送成功: messageId={}", messageId);
                return SendResult.success(messageId);
            } else {
                String errmsg = (String) result.get("errmsg");
                log.error("企业微信Webhook消息发送失败: errcode={}, errmsg={}", errcode, errmsg);
                return SendResult.fail(String.valueOf(errcode), errmsg);
            }
        }
    }
    
    /**
     * 通过应用发送消息
     */
    private SendResult sendByApp(String receiver, String title, String content, 
                                  Map<String, Object> config, Map<String, Object> params) throws Exception {
        String corpId = getConfigValue(config, "corpId", "");
        String corpSecret = getConfigValue(config, "corpSecret", "");
        String agentId = getConfigValue(config, "agentId", "");
        
        if (corpId.isEmpty() || corpSecret.isEmpty() || agentId.isEmpty()) {
            return SendResult.fail("CONFIG_ERROR", "企业微信配置不完整，缺少CorpId/CorpSecret/AgentId");
        }
        
        // 获取 access_token
        String accessToken = getAccessToken(corpId, corpSecret);
        if (accessToken == null) {
            return SendResult.fail("AUTH_ERROR", "获取企业微信access_token失败");
        }
        
        // 发送应用消息
        String url = "https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token=" + accessToken;
        
        Map<String, Object> msgBody = new HashMap<>();
        // 多个用户用 | 分隔
        String msgType = params != null ? getConfigValue(params, "msgType", "text") : "text";
        msgBody.put("touser", receiver);
        msgBody.put("msgtype", msgType);
        msgBody.put("agentid", Integer.parseInt(agentId));

        if ("markdown".equals(msgType)) {
            Map<String, Object> markdown = new HashMap<>();
            String mdTitle = title == null ? "" : title.trim();
            String mdBody = content == null ? "" : content;
            markdown.put("content", mdTitle.isEmpty() ? mdBody : ("### " + mdTitle + "\n" + mdBody));
            msgBody.put("markdown", markdown);
        } else {
            Map<String, Object> text = new HashMap<>();
            text.put("content", (title == null ? "" : title) + "\n" + (content == null ? "" : content));
            msgBody.put("text", text);
        }
        
        String jsonBody = objectMapper.writeValueAsString(msgBody);
        RequestBody body = RequestBody.create(jsonBody, JSON);
        Request request = new Request.Builder()
            .url(url)
            .post(body)
            .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            String respBody = response.body() != null ? response.body().string() : "";
            Map<String, Object> result = objectMapper.readValue(respBody, Map.class);
            
            Integer errcode = (Integer) result.get("errcode");
            if (errcode != null && errcode == 0) {
                Object msgId = result.get("msgid");
                String messageId = msgId != null ? msgId.toString() : UUID.randomUUID().toString();
                log.info("企业微信应用消息发送成功: msgId={}", messageId);
                return SendResult.success(messageId);
            } else {
                String errmsg = (String) result.get("errmsg");
                log.error("企业微信应用消息发送失败: errcode={}, errmsg={}", errcode, errmsg);
                return SendResult.fail(String.valueOf(errcode), errmsg);
            }
        }
    }
    
    /**
     * 获取企业微信 access_token (带缓存)
     */
    private String getAccessToken(String corpId, String corpSecret) {
        String cacheKey = corpId + "_" + corpSecret;
        TokenCache cache = tokenCache.get(cacheKey);
        
        // 检查缓存是否有效
        if (cache != null && cache.isValid()) {
            return cache.token;
        }
        
        try {
            String url = "https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=" + corpId + "&corpsecret=" + corpSecret;
            Request request = new Request.Builder().url(url).get().build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                String respBody = response.body() != null ? response.body().string() : "";
                Map<String, Object> result = objectMapper.readValue(respBody, Map.class);
                
                Integer errcode = (Integer) result.get("errcode");
                if (errcode != null && errcode == 0) {
                    String token = (String) result.get("access_token");
                    Integer expiresIn = (Integer) result.get("expires_in");
                    // 缓存token，提前5分钟过期
                    tokenCache.put(cacheKey, new TokenCache(token, expiresIn - 300));
                    return token;
                }
            }
        } catch (Exception e) {
            log.error("获取企业微信access_token失败: {}", e.getMessage(), e);
        }
        return null;
    }
    
    @Override
    public boolean validateConfig(Map<String, Object> config) {
        if (config == null) return false;
        String type = getConfigValue(config, "type", "webhook");
        
        if ("webhook".equals(type)) {
            return !getConfigValue(config, "webhook", "").isEmpty();
        } else if ("app".equals(type)) {
            return !getConfigValue(config, "corpId", "").isEmpty() 
                && !getConfigValue(config, "corpSecret", "").isEmpty()
                && !getConfigValue(config, "agentId", "").isEmpty();
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
