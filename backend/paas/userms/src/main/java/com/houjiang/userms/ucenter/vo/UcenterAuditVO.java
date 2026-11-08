package com.houjiang.userms.ucenter.vo;

import com.houjiang.userms.ucenter.model.UcenterPermissionAuditEntity;
import com.houjiang.userms.ucenter.model.UcenterPermissionEvent;

import java.time.LocalDateTime;

public record UcenterAuditVO(
        Long id,
        Long actorUserId,
        Long targetMemberId,
        UcenterPermissionEvent eventType,
        String beforePermissions,
        String afterPermissions,
        String correlationId,
        String result,
        String clientIp,
        String userAgent,
        LocalDateTime createTime
) {
    public static UcenterAuditVO from(UcenterPermissionAuditEntity row) {
        return new UcenterAuditVO(row.getId(), row.getActorUserId(), row.getTargetMemberId(),
                row.getEventType(), row.getBeforePermissions(), row.getAfterPermissions(),
                row.getCorrelationId(), row.getResult(), row.getClientIp(), row.getUserAgent(),
                row.getCreateTime());
    }
}
