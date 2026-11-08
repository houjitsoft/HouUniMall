package com.houjiang.thirdparty.service.message.sender;

import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;

/**
 * 项目群兼容发送器。
 *
 * <p>群平台及凭证只从本次请求读取，本类不缓存、不持久化，也不记录这些值。</p>
 * @author Amos
 */
@Component
public class WecomGroupSender implements MessageSender {

    private final WecomSender wecomSender;
    private final DingtalkSender dingtalkSender;
    private final FeishuSender feishuSender;

    public WecomGroupSender(
            WecomSender wecomSender,
            DingtalkSender dingtalkSender,
            FeishuSender feishuSender) {
        this.wecomSender = wecomSender;
        this.dingtalkSender = dingtalkSender;
        this.feishuSender = feishuSender;
    }

    @Override
    public String getChannelCode() {
        return "WECOM_GROUP";
    }

    @Override
    public SendResult send(
            String receiver,
            String title,
            String content,
            Map<String, Object> config,
            Map<String, Object> params) {
        String platform = value(params, "groupPlatform");
        if (platform == null || platform.isBlank()) {
            return SendResult.fail("GROUP_PLATFORM_REQUIRED", "群平台不能为空");
        }
        if (!validateConfig(config)) {
            return SendResult.fail("CHANNEL_CONFIG_EMPTY", "群渠道配置为空");
        }

        MessageSender delegate = switch (platform.trim().toLowerCase(Locale.ROOT)) {
            case "wecom", "wechat-work" -> wecomSender;
            case "dingtalk" -> dingtalkSender;
            case "feishu" -> feishuSender;
            default -> null;
        };
        if (delegate == null) {
            return SendResult.fail("GROUP_PLATFORM_UNSUPPORTED", "不支持的群平台");
        }
        return delegate.send(receiver, title, content, config, params);
    }

    @Override
    public boolean validateConfig(Map<String, Object> config) {
        return config != null && !config.isEmpty();
    }

    private String value(Map<String, Object> params, String key) {
        if (params == null) {
            return null;
        }
        Object value = params.get(key);
        return value == null ? null : value.toString();
    }
}
