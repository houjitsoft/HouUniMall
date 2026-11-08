package com.houjiang.userms.ucenter.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.houjiang.userms.ucenter.mapper.UcenterPermissionAuditMapper;
import com.houjiang.userms.ucenter.model.UcenterPermissionAuditEntity;
import com.houjiang.userms.ucenter.model.UcenterPermissionEvent;
import com.houjiang.userms.ucenter.model.UcenterRole;
import com.houjiang.userms.ucenter.model.UcenterSpaceMemberEntity;
import com.houjiang.userms.ucenter.vo.UcenterAuditVO;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class UcenterAuditService {
    private static final Pattern SENSITIVE_NAME = Pattern.compile(
            "(?i).*(token|secret|password|passwd|credential|authorization|cookie).*");
    private static final Pattern FULL_MOBILE = Pattern.compile("(?<!\\d)\\+?\\d{6,20}(?!\\d)");
    private final UcenterPermissionAuditMapper auditMapper;
    private final UcenterIdentityService identityService;
    private final com.houjiang.userms.ucenter.mapper.UcenterSpaceMemberMapper memberMapper;
    private final ObjectMapper objectMapper;

    public UcenterAuditService(UcenterPermissionAuditMapper auditMapper,
                               UcenterIdentityService identityService,
                               com.houjiang.userms.ucenter.mapper.UcenterSpaceMemberMapper memberMapper,
                               ObjectMapper objectMapper) {
        this.auditMapper = auditMapper;
        this.identityService = identityService;
        this.memberMapper = memberMapper;
        this.objectMapper = objectMapper;
    }

    public void recordSuccess(UcenterPermissionEvent event, Long spaceId, Long targetMemberId,
                              Set<String> before, Set<String> after) {
        UcenterPermissionAuditEntity row = new UcenterPermissionAuditEntity();
        row.setSpaceId(spaceId);
        row.setActorUserId(identityService.requireUserId());
        row.setTargetMemberId(targetMemberId);
        row.setEventType(event);
        row.setBeforePermissions(json(sanitizeValues(before)));
        row.setAfterPermissions(json(sanitizeValues(after)));
        String correlationId = MDC.get("traceId");
        row.setCorrelationId(correlationId == null || correlationId.isBlank()
                ? UUID.randomUUID().toString() : correlationId.substring(0, Math.min(64, correlationId.length())));
        row.setResult("SUCCESS");
        applyClientContext(row);
        row.setCreateTime(LocalDateTime.now());
        auditMapper.insert(row);
    }

    public List<UcenterAuditVO> recentForOwner(int requestedLimit) {
        Long actorId = identityService.requireUserId();
        UcenterSpaceMemberEntity actor = memberMapper.findActiveByUserId(actorId);
        if (actor == null || actor.getRole() != UcenterRole.OWNER) {
            throw new UcenterDomainException(UcenterDomainException.Kind.FORBIDDEN, "仅空间所有者可查看审计记录");
        }
        int limit = Math.max(1, Math.min(requestedLimit, 200));
        return auditMapper.findRecent(actor.getSpaceId(), limit).stream().map(UcenterAuditVO::from).toList();
    }

    private String json(Set<String> values) {
        try {
            return objectMapper.writeValueAsString(values == null ? Set.of() : new TreeSet<>(values));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to serialize permission audit", exception);
        }
    }

    private static Set<String> sanitizeValues(Set<String> values) {
        if (values == null || values.isEmpty()) return Set.of();
        java.util.TreeSet<String> safe = new java.util.TreeSet<>();
        for (String value : values) {
            if (value == null || value.isBlank() || value.length() > 160) continue;
            if (SENSITIVE_NAME.matcher(value).matches() || FULL_MOBILE.matcher(value).find()) continue;
            safe.add(stripControls(value));
        }
        return Set.copyOf(safe);
    }

    private static void applyClientContext(UcenterPermissionAuditEntity row) {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)) return;
        HttpServletRequest request = attributes.getRequest();
        String forwarded = request.getHeader("X-Forwarded-For");
        String ip = forwarded == null || forwarded.isBlank()
                ? request.getRemoteAddr()
                : forwarded.split(",", 2)[0].trim();
        row.setClientIp(limit(stripControls(ip), 45));
        row.setUserAgent(limit(stripControls(request.getHeader("User-Agent")), 255));
    }

    private static String stripControls(String value) {
        return value == null ? null : value.replaceAll("[\\p{Cntrl}]", "").trim();
    }

    private static String limit(String value, int maxLength) {
        if (value == null || value.isBlank()) return null;
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
