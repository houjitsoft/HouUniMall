package com.houjiang.thirdparty.service.message;

import com.houjiang.thirdparty.dto.message.IdempotentMessageSendRequest;
import com.houjiang.thirdparty.dto.message.IdempotentMessageSendResponse;
import com.houjiang.thirdparty.service.message.sender.MessageSender;
import com.houjiang.thirdparty.service.message.sender.SendResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 渠道直达投递：挑选发送器、解析渠道配置、调用发送器，不读写任何幂等记录。
 *
 * <p>拆出这个 bean 是为了断开 Bean 循环依赖。原先 {@code MessageRecordService} 通过
 * {@code MessageSendService} 反向调用重投，而 {@code MessageSendService} 的实现又依赖
 * {@code MessageRecordService} 记录发送结果，形成闭环。这条重投链路真正用到的只是
 * "投递 + 结果映射"，因此下沉到本 bean —— 它只依赖 {@link MessageChannelConfigResolver}
 * 和 {@link MessageSender}，属于依赖图的叶子节点，任何方向都不会回指。
 *
 * @author Amos
 */
@Slf4j
@Service
public class MessageDeliveryService {

    @Autowired
    private MessageChannelConfigResolver channelConfigResolver;

    @Autowired
    private List<MessageSender> senders;

    /** 发送器缓存 */
    private final ConcurrentHashMap<String, MessageSender> senderMap = new ConcurrentHashMap<>();

    /**
     * 按渠道执行一次性投递（不查、不写幂等记录）。
     *
     * @param channelCode 渠道编码
     * @param receiver    接收者
     * @param title       标题
     * @param content     内容
     * @param params      发送参数
     * @return 发送结果
     */
    public SendResult deliver(String channelCode, String receiver, String title,
                              String content, Map<String, Object> params) {
        try {
            MessageSender sender = getSender(channelCode);
            if (sender == null) {
                log.error("不支持的渠道: {}", channelCode);
                return SendResult.fail("UNSUPPORTED_CHANNEL", "不支持的渠道: " + channelCode);
            }

            Map<String, Object> config = resolveSendConfig(channelCode, params);
            if (config == null) {
                return SendResult.fail("CHANNEL_DISABLED", "渠道未配置或未启用");
            }
            if (!"INBOX".equalsIgnoreCase(channelCode) && config.isEmpty()) {
                return SendResult.fail("CHANNEL_CONFIG_EMPTY", "渠道配置为空");
            }

            return sender.send(receiver, title, content, config, params);

        } catch (Exception e) {
            log.error("消息发送失败: channel={}, errorType={}", channelCode,
                    e.getClass().getSimpleName());
            return SendResult.fail("SEND_ERROR", "发送失败");
        }
    }

    /**
     * 对已有记录做一次重投：只投递并映射成幂等响应，不改动幂等状态机。
     *
     * @param request 发送快照
     * @return 幂等发送响应
     */
    public IdempotentMessageSendResponse retry(IdempotentMessageSendRequest request) {
        Map<String, Object> deliveryParams = new HashMap<>(request.params());
        deliveryParams.put("idempotencyKey", request.idempotencyKey());
        SendResult result = deliver(
                request.channelCode(),
                request.receiver(),
                request.title(),
                request.content(),
                deliveryParams);
        if (result.isSuccess()) {
            return new IdempotentMessageSendResponse(true, result.getMessageId(), false, null, null);
        }
        String errorCode = hasText(result.getErrorCode()) ? result.getErrorCode() : "SEND_ERROR";
        String errorMessage = hasText(result.getErrorMessage()) ? result.getErrorMessage() : "发送失败";
        return new IdempotentMessageSendResponse(false, null, false, errorCode, errorMessage);
    }

    /**
     * 解析本次投递使用的渠道配置。
     *
     * @param channelCode 渠道编码
     * @param params      发送参数
     * @return 渠道配置；返回 null 表示渠道不可用
     */
    private Map<String, Object> resolveSendConfig(String channelCode, Map<String, Object> params) {
        if ("INBOX".equalsIgnoreCase(channelCode)) {
            return Map.of();
        }
        if ("WECOM_GROUP".equalsIgnoreCase(channelCode)) {
            Object groupConfig = params == null ? null : params.get("groupConfig");
            if (!(groupConfig instanceof Map<?, ?> rawConfig)) {
                return Map.of();
            }
            Map<String, Object> config = new HashMap<>();
            rawConfig.forEach((key, value) -> {
                if (key != null) {
                    config.put(key.toString(), value);
                }
            });
            return config;
        }

        String orgId = extractOrgId(params);
        if (!channelConfigResolver.isChannelAvailable(channelCode, orgId)) {
            return null;
        }
        return channelConfigResolver.resolve(channelCode, orgId);
    }

    /**
     * 从发送参数中提取组织 ID。
     *
     * @param params 发送参数
     * @return 组织 ID，未传时返回 null
     */
    private String extractOrgId(Map<String, Object> params) {
        if (params == null || params.get("orgId") == null) {
            return null;
        }
        return params.get("orgId").toString();
    }

    /**
     * 获取渠道对应的发送器（带缓存）。
     *
     * @param channelCode 渠道编码
     * @return 发送器，未匹配时返回 null
     */
    private MessageSender getSender(String channelCode) {
        MessageSender sender = senderMap.get(channelCode);
        if (sender != null) {
            return sender;
        }

        for (MessageSender s : senders) {
            if (s.getChannelCode().equalsIgnoreCase(channelCode)) {
                senderMap.put(channelCode, s);
                return s;
            }
        }

        return null;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
