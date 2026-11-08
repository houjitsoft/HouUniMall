package com.houjiang.thirdparty.dto.message;

public record IdempotentMessageSendResponse(
        boolean success,
        String messageId,
        boolean idempotentReplay,
        String errorCode,
        String errorMessage) {
}
