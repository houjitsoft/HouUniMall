package com.houjiang.payment.security;

import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.payment.mapper.PaymentUserOrganizationMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class PaymentOrganizationResolverTest {
    private PaymentUserOrganizationMapper mapper;
    private PaymentOrganizationResolver resolver;
    private MockHttpServletRequest request;

    @BeforeEach
    void setup() {
        mapper = mock(PaymentUserOrganizationMapper.class);
        resolver = new PaymentOrganizationResolver(mapper);
        request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        UserInfoVO user = new UserInfoVO();
        user.setUserId("account-b");
        user.setOrgId("999");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, List.of()));
        when(mapper.selectOrganizationsByUserId("account-b"))
                .thenReturn(List.of(Map.of("org_id", "2", "is_primary", 1)));
    }

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void ignoresStaleClientTenantAndUsesAuthoritativePrimaryOrganization() {
        assertEquals(2L, resolver.requireCurrentTenantId());
    }

    @Test
    void rejectsForgedOrganizationHeaderInsteadOfFallingBack() {
        request.addHeader("X-Org-Id", "999");
        assertThrows(AccessDeniedException.class, resolver::requireCurrentTenantId);
    }

    @Test
    void missingAuthoritativeMembershipFailsClosed() {
        when(mapper.selectOrganizationsByUserId("account-b")).thenReturn(List.of());
        assertThrows(AccessDeniedException.class, resolver::requireCurrentTenantId);
    }

    @Test
    void anonymousRequestFailsBeforeMembershipLookup() {
        SecurityContextHolder.clearContext();
        assertThrows(AccessDeniedException.class, resolver::requireCurrentTenantId);
        verifyNoInteractions(mapper);
    }
}
