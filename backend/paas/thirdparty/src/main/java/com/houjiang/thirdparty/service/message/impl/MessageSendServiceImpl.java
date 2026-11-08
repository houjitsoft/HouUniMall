package com.houjiang.thirdparty.service.message.impl;

import com.houjiang.thirdparty.dto.message.IdempotentMessageSendRequest;
import com.houjiang.thirdparty.dto.message.IdempotentMessageSendResponse;
import com.houjiang.thirdparty.entity.message.MessageTemplate;
import com.houjiang.thirdparty.service.message.EmailQueueService;
import com.houjiang.thirdparty.service.message.EmailSettingsService;
import com.houjiang.thirdparty.service.message.MessageChannelService;
import com.houjiang.thirdparty.service.message.MessageDispatchService;
import com.houjiang.thirdparty.service.message.MessageSendService;
import com.houjiang.thirdparty.service.message.MessageTemplateService;
import com.houjiang.thirdparty.service.message.sender.SendResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 统一消息发送服务实现
 *
 * <p>本类只保留"编排"职责：模板渲染、邮件队列分流，其余的幂等记录与渠道投递分别下沉到
 * {@link MessageDispatchService} 与 {@link MessageDeliveryService}，以此断开与
 * {@link EmailQueueService} / {@link EmailSettingsService} 的循环依赖。
 *
 * @author Amos
 * @since 2025-12-26
 */
@Slf4j
@Service
public class MessageSendServiceImpl implements MessageSendService {

    @Autowired
    private MessageChannelService channelService;

    @Autowired
    private MessageTemplateService templateService;

    @Autowired
    private EmailSettingsService emailSettingsService;

    @Autowired
    private EmailQueueService emailQueueService;

    @Autowired
    private MessageDispatchService dispatchService;

    /** 模板变量模式 */
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{(\\w+)\\}");

    @Override
    public IdempotentMessageSendResponse sendIdempotently(IdempotentMessageSendRequest request) {
        return dispatchService.dispatch(request);
    }

    @Override
    public IdempotentMessageSendResponse retryExisting(IdempotentMessageSendRequest request) {
        return dispatchService.retry(request);
    }

    @Override
    public List<SendResult> sendByTemplate(String templateCode, String receiver, Map<String, Object> params) {
        List<SendResult> results = new ArrayList<>();

        // 1. 获取模板
        MessageTemplate template = templateService.getTemplateByCode(templateCode);
        if (template == null) {
            log.error("模板不存在: {}", templateCode);
            results.add(SendResult.fail("TEMPLATE_NOT_FOUND", "模板不存在: " + templateCode));
            return results;
        }

        if (template.getStatus() != 1) {
            log.error("模板未启用: {}", templateCode);
            results.add(SendResult.fail("TEMPLATE_DISABLED", "模板未启用"));
            return results;
        }

        // 2. 解析模板支持的渠道
        String channelsStr = template.getChannels();
        if (channelsStr == null || channelsStr.isEmpty()) {
            log.error("模板未配置发送渠道: {}", templateCode);
            results.add(SendResult.fail("NO_CHANNEL", "模板未配置发送渠道"));
            return results;
        }

        String[] channelCodes = channelsStr.split(",");

        // 3. 渲染模板内容
        String title = renderTemplate(template.getTitleTemplate(), params);
        String content = renderTemplate(template.getContentTemplate(), params);
        Map<String, Object> templateParams = params == null
                ? new HashMap<>()
                : new HashMap<>(params);
        templateParams.putIfAbsent("templateCode", template.getTemplateCode());
        templateParams.putIfAbsent("bizType", template.getBizType());

        // 4. 遍历渠道发送
        for (String channelCode : channelCodes) {
            channelCode = channelCode.trim();
            if ("EMAIL".equalsIgnoreCase(channelCode) && !emailSettingsService.isTemplateEnabled()) {
                log.warn("邮件模板功能未启用，跳过 EMAIL 渠道: template={}", templateCode);
                results.add(SendResult.fail("EMAIL_TEMPLATE_DISABLED", "邮件模板功能未启用"));
                continue;
            }
            Map<String, Object> channelParams = channelParams(
                    templateParams, channelCodes.length, channelCode);
            SendResult result = sendByChannel(channelCode, receiver, title, content, channelParams);
            result.setExtra(Map.of("channelCode", channelCode, "templateCode", templateCode));
            results.add(result);
        }

        return results;
    }

    @Override
    public SendResult sendByChannel(String channelCode, String receiver, String title, String content, Map<String, Object> params) {
        boolean sync = params != null && Boolean.TRUE.equals(params.get("sync"));
        if (!sync && "EMAIL".equalsIgnoreCase(channelCode) && emailSettingsService.isQueueEnabled()) {
            emailQueueService.enqueue(channelCode, receiver, title, content, params);
            return SendResult.success("queued");
        }
        return dispatchService.sendDirectly(channelCode, receiver, title, content, params);
    }

    @Override
    public List<SendResult> batchSend(String templateCode, List<String> receivers, Map<String, Object> params) {
        List<SendResult> results = new ArrayList<>();
        for (String receiver : receivers) {
            List<SendResult> singleResults = sendByTemplate(templateCode, receiver, params);
            results.addAll(singleResults);
        }
        return results;
    }

    @Override
    public SendResult testChannel(String channelCode, String testReceiver) {
        String title = "测试消息";
        String content = "这是一条测试消息，发送时间：" + new Date();
        return sendByChannel(channelCode, testReceiver, title, content, null);
    }

    /**
     * 多渠道发送时给每个渠道派生独立的幂等键，避免首渠道发完后其余渠道被当成重放。
     *
     * @param params        模板参数
     * @param channelCount  本次发送涉及的渠道数
     * @param channelCode   当前渠道编码
     * @return 带上渠道后缀幂等键的参数副本
     */
    private Map<String, Object> channelParams(
            Map<String, Object> params, int channelCount, String channelCode) {
        if (params == null || params.isEmpty() || channelCount <= 1) {
            return params;
        }
        String key = params.get("idempotencyKey") == null
                ? null
                : params.get("idempotencyKey").toString();
        if (key == null || key.isBlank()) {
            return params;
        }
        Map<String, Object> result = new HashMap<>(params);
        result.put("idempotencyKey", key + ":" + channelCode.toUpperCase(Locale.ROOT));
        return result;
    }

    /**
     * 渲染模板（替换变量）
     */
    private String renderTemplate(String template, Map<String, Object> params) {
        if (template == null || params == null) {
            return template;
        }

        StringBuffer result = new StringBuffer();
        Matcher matcher = VARIABLE_PATTERN.matcher(template);

        while (matcher.find()) {
            String varName = matcher.group(1);
            Object value = params.get(varName);
            String replacement = value != null ? value.toString() : "";
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);

        return result.toString();
    }

}
