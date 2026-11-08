package com.houjiang.userms.ucenter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.houjiang.userms.ucenter.mapper.UcenterPermissionAuditMapper;
import com.houjiang.userms.ucenter.mapper.UcenterSpaceMemberMapper;
import com.houjiang.userms.ucenter.model.UcenterPermissionAuditEntity;
import com.houjiang.userms.ucenter.model.UcenterPermissionEvent;
import com.houjiang.userms.ucenter.model.UcenterRole;
import com.houjiang.userms.ucenter.model.UcenterSpaceMemberEntity;
import com.houjiang.userms.ucenter.service.UcenterAuditService;
import com.houjiang.userms.ucenter.service.UcenterIdentityService;
import com.houjiang.userms.ucenter.vo.UcenterAuditVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Set;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UcenterAuditServiceTest {
    private final UcenterPermissionAuditMapper audits = mock(UcenterPermissionAuditMapper.class);
    private final UcenterIdentityService identity = mock(UcenterIdentityService.class);
    private final UcenterSpaceMemberMapper members = mock(UcenterSpaceMemberMapper.class);
    private final UcenterAuditService service = new UcenterAuditService(
            audits, identity, members, new ObjectMapper());

    @AfterEach
    void clearRequestContext() {
        RequestContextHolder.resetRequestAttributes();
        MDC.clear();
    }

    @Test
    void recordsTraceAndSanitizedClientContextWithoutSensitiveValues() {
        when(identity.requireUserId()).thenReturn(42L);
        MDC.put("traceId", "trace-ucenter-42");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "203.0.113.9, 10.0.0.2");
        request.addHeader("User-Agent", "Browser/1.0\r\nInjected: value");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        service.recordSuccess(UcenterPermissionEvent.MENUS_REPLACED, 10L, 30L,
                Set.of("ucenter:order:list", "password=do-not-store", "13800138000"),
                Set.of("ucenter:member:profile", "token:raw-secret"));

        ArgumentCaptor<UcenterPermissionAuditEntity> row =
                ArgumentCaptor.forClass(UcenterPermissionAuditEntity.class);
        verify(audits).insert(row.capture());
        UcenterPermissionAuditEntity saved = row.getValue();
        assertEquals(42L, saved.getActorUserId());
        assertEquals("trace-ucenter-42", saved.getCorrelationId());
        assertEquals("203.0.113.9", saved.getClientIp());
        assertFalse(saved.getUserAgent().contains("\r"));
        assertFalse(saved.getUserAgent().contains("\n"));
        assertTrue(saved.getBeforePermissions().contains("ucenter:order:list"));
        assertTrue(saved.getAfterPermissions().contains("ucenter:member:profile"));
        String persisted = saved.getBeforePermissions() + saved.getAfterPermissions();
        assertFalse(persisted.contains("do-not-store"));
        assertFalse(persisted.contains("13800138000"));
        assertFalse(persisted.contains("raw-secret"));
        assertNotNull(saved.getCreateTime());
    }

    @Test
    void ownerAuditViewContainsSafeTraceAndClientContext() {
        when(identity.requireUserId()).thenReturn(42L);
        UcenterSpaceMemberEntity owner = new UcenterSpaceMemberEntity();
        owner.setSpaceId(10L);
        owner.setRole(UcenterRole.OWNER);
        when(members.findActiveByUserId(42L)).thenReturn(owner);
        UcenterPermissionAuditEntity row = new UcenterPermissionAuditEntity();
        row.setId(1L);
        row.setSpaceId(10L);
        row.setActorUserId(42L);
        row.setEventType(UcenterPermissionEvent.MENUS_REPLACED);
        row.setBeforePermissions("[]");
        row.setAfterPermissions("[\"ucenter:order:list\"]");
        row.setCorrelationId("trace-1");
        row.setResult("SUCCESS");
        row.setClientIp("203.0.113.9");
        row.setUserAgent("Browser/1.0");
        row.setCreateTime(java.time.LocalDateTime.of(2026, 9, 28, 8, 0));
        when(audits.findRecent(10L, 25)).thenReturn(List.of(row));

        UcenterAuditVO result = service.recentForOwner(25).getFirst();

        assertEquals("trace-1", result.correlationId());
        assertEquals("203.0.113.9", result.clientIp());
        assertEquals("Browser/1.0", result.userAgent());
    }
}
