package com.houjiang.thirdparty.service.message.sender;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import com.houjiang.commons.http.OutboundHttpClientFactory;
import okhttp3.*;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 钉钉消息发送器
 * 支持：
 * 1. 钉钉机器人 Webhook
 * 2. 钉钉工作通知 (需要企业内部应用)
 * 
 * @author Amos
 * @since 2025-12-26
 */
@Slf4j
@Component
public class DingtalkSender implements MessageSender {
    
    private static final OkHttpClient httpClient = OutboundHttpClientFactory.okHttpExternal();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    
    @Override
    public String getChannelCode() {
        return "DINGTALK";
    }
    
    @Override
    public SendResult send(String receiver, String title, String content, 
                           Map<String, Object> config, Map<String, Object> params) {
        try {
            String type = getConfigValue(config, "type", "webhook");
            
            if ("webhook".equals(type)) {
                return sendByWebhook(receiver, title, content, config, params);
            } else if ("work_notice".equals(type)) {
                return sendByWorkNotice(receiver, title, content, config, params);
            } else {
                return SendResult.fail("CONFIG_ERROR", "不支持的发送类型: " + type);
            }
            
        } catch (Exception e) {
            log.error("钉钉消息发送异常: errorType={}", e.getClass().getSimpleName(), e);
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
            return SendResult.fail("CONFIG_ERROR", "钉钉配置不完整，缺少Webhook地址");
        }
        
        // 如果有签名密钥，需要加签
        if (!secret.isEmpty()) {
            Long timestamp = System.currentTimeMillis();
            String sign = generateSign(timestamp, secret);
            webhook = webhook + "&timestamp=" + timestamp + "&sign=" + sign;
        }
        
        // 构建消息体
        Map<String, Object> msgBody = new HashMap<>();
        
        // 判断消息类型
        String msgType = params != null ? getConfigValue(params, "msgType", "text") : "text";
        msgBody.put("msgtype", msgType);
        
        if ("text".equals(msgType)) {
            Map<String, Object> text = new HashMap<>();
            text.put("content", title + "\n" + content);
            msgBody.put("text", text);
            
            // 如果需要@某人
            if (receiver != null && !receiver.isEmpty()) {
                Map<String, Object> at = new HashMap<>();
                at.put("atMobiles", receiver.split(","));
                msgBody.put("at", at);
            }
        } else if ("markdown".equals(msgType)) {
            Map<String, Object> markdown = new HashMap<>();
            markdown.put("title", title);
            markdown.put("text", content);
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
                log.info("钉钉Webhook消息发送成功: messageId={}", messageId);
                return SendResult.success(messageId);
            } else {
                String errmsg = (String) result.get("errmsg");
                log.error("钉钉Webhook消息发送失败: errcode={}, errmsg={}", errcode, errmsg);
                return SendResult.fail(String.valueOf(errcode), errmsg);
            }
        }
    }
    
    /**
     * 通过工作通知发送消息（需要企业内部应用）
     */
    private SendResult sendByWorkNotice(String receiver, String title, String content, 
                                         Map<String, Object> config, Map<String, Object> params) throws Exception {
        String appKey = getConfigValue(config, "appKey", "");
        String appSecret = getConfigValue(config, "appSecret", "");
        String agentId = getConfigValue(config, "agentId", "");
        
        if (appKey.isEmpty() || appSecret.isEmpty() || agentId.isEmpty()) {
            return SendResult.fail("CONFIG_ERROR", "钉钉配置不完整，缺少AppKey/AppSecret/AgentId");
        }
        
        // 1. 获取 access_token
        String accessToken = getAccessToken(appKey, appSecret);
        if (accessToken == null) {
            return SendResult.fail("AUTH_ERROR", "获取钉钉access_token失败");
        }
        
        // 2. 发送工作通知
        String url = "https://oapi.dingtalk.com/topapi/message/corpconversation/asyncsend_v2?access_token=" + accessToken;
        
        Map<String, Object> msgBody = new HashMap<>();
        msgBody.put("agent_id", Long.parseLong(agentId));
        // 多个用户用逗号分隔
        msgBody.put("userid_list", receiver);
        
        // 消息内容
        Map<String, Object> msg = new HashMap<>();
        msg.put("msgtype", "text");
        Map<String, Object> text = new HashMap<>();
        text.put("content", title + "\n" + content);
        msg.put("text", text);
        msgBody.put("msg", msg);
        
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
                Object taskId = result.get("task_id");
                String messageId = taskId != null ? taskId.toString() : UUID.randomUUID().toString();
                log.info("钉钉工作通知发送成功: taskId={}", messageId);
                return SendResult.success(messageId);
            } else {
                String errmsg = (String) result.get("errmsg");
                log.error("钉钉工作通知发送失败: errcode={}, errmsg={}", errcode, errmsg);
                return SendResult.fail(String.valueOf(errcode), errmsg);
            }
        }
    }
    
    /**
     * 获取钉钉 access_token
     */
    private String getAccessToken(String appKey, String appSecret) {
        try {
            String url = "https://oapi.dingtalk.com/gettoken?appkey=" + appKey + "&appsecret=" + appSecret;
            Request request = new Request.Builder().url(url).get().build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                String respBody = response.body() != null ? response.body().string() : "";
                Map<String, Object> result = objectMapper.readValue(respBody, Map.class);
                
                Integer errcode = (Integer) result.get("errcode");
                if (errcode != null && errcode == 0) {
                    return (String) result.get("access_token");
                }
            }
        } catch (Exception e) {
            log.error("获取钉钉access_token失败: {}", e.getMessage(), e);
        }
        return null;
    }
    
    /**
     * 生成签名
     */
    private String generateSign(Long timestamp, String secret) throws Exception {
        String stringToSign = timestamp + "\n" + secret;
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
        return URLEncoder.encode(Base64.getEncoder().encodeToString(signData), StandardCharsets.UTF_8);
    }
    
    @Override
    public boolean validateConfig(Map<String, Object> config) {
        if (config == null) return false;
        String type = getConfigValue(config, "type", "webhook");
        
        if ("webhook".equals(type)) {
            return !getConfigValue(config, "webhook", "").isEmpty();
        } else if ("work_notice".equals(type)) {
            return !getConfigValue(config, "appKey", "").isEmpty() 
                && !getConfigValue(config, "appSecret", "").isEmpty()
                && !getConfigValue(config, "agentId", "").isEmpty();
        }
        return false;
    }
    
    private String getConfigValue(Map<String, Object> config, String key, String defaultValue) {
        if (config == null) return defaultValue;
        Object value = config.get(key);
        return value != null ? value.toString() : defaultValue;
    }
}
