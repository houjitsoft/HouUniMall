package com.houjiang.thirdparty.dto.message;

public record MessageRecordListItemResponse(
        Long id,
        String bizType,
        String bizId,
        String eventType,
        String channel,
        String receiverName,
        String title,
        Integer sendStatus,
        Integer sendTime,
        String errorCode,
        Integer retryCount,
        Integer createTime) {
}
