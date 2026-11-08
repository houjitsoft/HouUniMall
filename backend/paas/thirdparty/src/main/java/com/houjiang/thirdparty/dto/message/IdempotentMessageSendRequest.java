package com.houjiang.thirdparty.dto.message;

import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

public record IdempotentMessageSendRequest(
        String idempotencyKey,
        String channelCode,
        String receiver,
        String receiverName,
        String title,
        String content,
        Map<String, Object> params) {

    public IdempotentMessageSendRequest {
        idempotencyKey = requireText(idempotencyKey, "idempotencyKey");
        channelCode = requireText(channelCode, "channelCode").toUpperCase();
        receiver = requireText(receiver, "receiver");
        receiverName = receiverName == null ? null : receiverName.trim();
        title = title == null ? "" : title;
        content = content == null ? "" : content;
        params = params == null
                ? Map.of()
                : Collections.unmodifiableMap(new HashMap<>(params));
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " 不能为空");
        }
        return value.trim();
    }
}
