package com.houjiang.thirdparty.dto.message;

public record MessageRecordFilter(
        String bizType,
        String eventType,
        String channel,
        Integer sendStatus,
        String receiver,
        Integer startTime,
        Integer endTime) {
}
