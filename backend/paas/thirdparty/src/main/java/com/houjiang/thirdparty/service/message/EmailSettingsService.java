package com.houjiang.thirdparty.service.message;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.houjiang.thirdparty.entity.message.MessageChannel;
import com.houjiang.thirdparty.service.message.sender.SendResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 邮件 SMTP 配置读写与测试发送。
 * @author Amos
 */
@Slf4j
@Service
public class EmailSettingsService {

    private static final String CHANNEL_CODE = "EMAIL";
    private static final String MASK_PASSWORD = "******";

    @Autowired
    private MessageChannelService channelService;

    /**
     * 测试邮件走同步直发。
     *
     * <p>注入 {@link MessageDispatchService} 而不是 {@code MessageSendService} 接口，避免与
     * {@code MessageSendServiceImpl} 形成循环依赖而只能靠 {@code @Lazy} 掩盖。
     */
    @Autowired
    private MessageDispatchService dispatchService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 读取邮件配置（密码脱敏）。
     *
     * @return 邮件配置 Map
     */
    public Map<String, Object> getSettings() {
        MessageChannel channel = channelService.getChannelByCode(CHANNEL_CODE);
        Map<String, Object> config = parseConfig(channel != null ? channel.getConfig() : null);
        Map<String, Object> result = new HashMap<>();
        result.put("provider", getStr(config, "provider", "smtp"));
        result.put("smtpHost", getStr(config, "host", "smtp.qq.com"));
        result.put("smtpPort", getInt(config, "port", 587));
        result.put("encryption", resolveEncryption(config));
        result.put("fromEmail", getStr(config, "from", ""));
        result.put("fromName", getStr(config, "fromName", "HouCloud系统"));
        result.put("username", getStr(config, "username", ""));
        result.put("password", StringUtils.hasText(getStr(config, "password", "")) ? MASK_PASSWORD : "");
        result.put("templateEnabled", getBool(config, "templateEnabled", true));
        result.put("queueEnabled", getBool(config, "queueEnabled", false));
        result.put("channelStatus", channel != null ? channel.getStatus() : 0);
        return result;
    }

    /**
     * 保存邮件配置到消息渠道表。
     *
     * @param body 前端提交的配置
     * @return 是否保存成功
     */
    public boolean saveSettings(Map<String, Object> body) {
        MessageChannel channel = channelService.getChannelByCode(CHANNEL_CODE);
        if (channel == null) {
            channel = new MessageChannel();
            channel.setChannelCode(CHANNEL_CODE);
            channel.setChannelName("邮件");
            channel.setSort(2);
            channel.setStatus(1);
            channelService.createChannel(channel);
            channel = channelService.getChannelByCode(CHANNEL_CODE);
        }

        Map<String, Object> existing = parseConfig(channel.getConfig());
        Map<String, Object> config = new HashMap<>();
        config.put("provider", getStr(body, "provider", "smtp"));
        config.put("host", getStr(body, "smtpHost", getStr(body, "host", "smtp.qq.com")));
        config.put("port", String.valueOf(getInt(body, "smtpPort", getInt(body, "port", 587))));
        String encryption = getStr(body, "encryption", "tls");
        config.put("encryption", encryption);
        config.put("ssl", "ssl".equalsIgnoreCase(encryption) ? "true" : "false");
        config.put("from", getStr(body, "fromEmail", getStr(body, "from", getStr(body, "username", ""))));
        config.put("fromName", getStr(body, "fromName", "HouCloud系统"));
        config.put("username", getStr(body, "username", ""));
        String password = getStr(body, "password", "");
        if (!StringUtils.hasText(password) || MASK_PASSWORD.equals(password)) {
            password = getStr(existing, "password", "");
        }
        config.put("password", password);
        config.put("templateEnabled", getBool(body, "templateEnabled", true));
        config.put("queueEnabled", getBool(body, "queueEnabled", false));

        try {
            channel.setConfig(objectMapper.writeValueAsString(config));
            channel.setStatus(StringUtils.hasText(config.get("username").toString())
                    && StringUtils.hasText(config.get("password").toString()) ? 1 : 0);
            channel.setUpdateTime((int) (System.currentTimeMillis() / 1000));
            return channelService.updateChannel(channel);
        } catch (Exception e) {
            log.error("保存邮件配置失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 发送测试邮件。
     *
     * @param receiver 收件人
     * @return 发送结果
     */
    public SendResult sendTestEmail(String receiver) {
        Map<String, Object> params = new HashMap<>();
        params.put("isHtml", false);
        params.put("sync", true);
        return dispatchService.sendDirectly(
                CHANNEL_CODE,
                receiver,
                "HouCloud 邮件测试",
                "这是一封来自 HouCloud 系统设置页的测试邮件，发送时间：" + System.currentTimeMillis(),
                params
        );
    }

    /**
     * 是否启用邮件模板功能。
     *
     * @return 是否启用
     */
    public boolean isTemplateEnabled() {
        MessageChannel channel = channelService.getChannelByCode(CHANNEL_CODE);
        Map<String, Object> config = parseConfig(channel != null ? channel.getConfig() : null);
        return getBool(config, "templateEnabled", true);
    }

    /**
     * 是否启用邮件队列异步发送。
     *
     * @return 是否启用队列
     */
    public boolean isQueueEnabled() {
        MessageChannel channel = channelService.getChannelByCode(CHANNEL_CODE);
        Map<String, Object> config = parseConfig(channel != null ? channel.getConfig() : null);
        return getBool(config, "queueEnabled", false);
    }

    private Map<String, Object> parseConfig(String json) {
        if (!StringUtils.hasText(json)) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    private String resolveEncryption(Map<String, Object> config) {
        if (config.containsKey("encryption")) {
            return getStr(config, "encryption", "tls");
        }
        return "true".equalsIgnoreCase(getStr(config, "ssl", "true")) ? "ssl" : "tls";
    }

    private String getStr(Map<String, Object> map, String key, String defaultValue) {
        if (map == null || map.get(key) == null) {
            return defaultValue;
        }
        return map.get(key).toString();
    }

    private int getInt(Map<String, Object> map, String key, int defaultValue) {
        if (map == null || map.get(key) == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(map.get(key).toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private boolean getBool(Map<String, Object> map, String key, boolean defaultValue) {
        if (map == null || map.get(key) == null) {
            return defaultValue;
        }
        Object v = map.get(key);
        if (v instanceof Boolean) {
            return (Boolean) v;
        }
        return Boolean.parseBoolean(v.toString());
    }
}
