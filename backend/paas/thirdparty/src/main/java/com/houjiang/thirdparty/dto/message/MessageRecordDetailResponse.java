package com.houjiang.thirdparty.dto.message;

public record MessageRecordDetailResponse(
        Long id,
        String templateCode,
        String bizType,
        String bizId,
        String eventType,
        String channel,
        String receiverName,
        String receiverContact,
        String title,
        Integer sendStatus,
        Integer sendTime,
        String failReason,
        String errorCode,
        String providerMessageId,
        Integer retryCount,
        Integer createTime,
        String idempotencyKeyDigest) {
}
