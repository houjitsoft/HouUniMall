package com.houjiang.order.config;

import com.houjiang.plugin.engine.core.PluginDispatcher;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdvertisingInternalTokenFilterTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void validTokenMarksOnlyAdvertisingPublicSlotAsTrustedInternal() throws Exception {
        AdvertisingInternalTokenFilter filter = new AdvertisingInternalTokenFilter("service-secret");
        MockHttpServletRequest request = new MockHttpServletRequest(
                "GET", "/plugin/mall-advertising/public/slots/pc-login-left");
        request.addHeader("X-Internal-Job-Token", "service-secret");

        filter.doFilter(request, new MockHttpServletResponse(), assertingChain(request));

        assertEquals("internal", request.getAttribute(PluginDispatcher.ATTRIBUTE_TRUSTED_PLUGIN_CALLER));
        assertTrue(SecurityContextHolder.getContext().getAuthentication().isAuthenticated());
        assertTrue(SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(authority -> "mall-advertising:slot:read".equals(authority.getAuthority())));
    }

    @Test
    void wrongTokenOrUnrelatedRouteNeverGetsInternalIdentity() throws Exception {
        AdvertisingInternalTokenFilter filter = new AdvertisingInternalTokenFilter("service-secret");
        MockHttpServletRequest wrongToken = new MockHttpServletRequest(
                "GET", "/plugin/mall-advertising/public/slots/pc-login-left");
        wrongToken.addHeader("X-Internal-Job-Token", "wrong");
        filter.doFilter(wrongToken, new MockHttpServletResponse(), assertingChain(wrongToken));
        assertNull(wrongToken.getAttribute(PluginDispatcher.ATTRIBUTE_TRUSTED_PLUGIN_CALLER));

        MockHttpServletRequest unrelated = new MockHttpServletRequest(
                "GET", "/plugin/mall-advertising/admin/positions");
        unrelated.addHeader("X-Internal-Job-Token", "service-secret");
        filter.doFilter(unrelated, new MockHttpServletResponse(), assertingChain(unrelated));
        assertNull(unrelated.getAttribute(PluginDispatcher.ATTRIBUTE_TRUSTED_PLUGIN_CALLER));
    }

    @Test
    void validTokenGrantsOnlyMerchantAdvertisingPermissionOnMerchantRoutes() throws Exception {
        AdvertisingInternalTokenFilter filter = new AdvertisingInternalTokenFilter("service-secret");
        MockHttpServletRequest request = new MockHttpServletRequest(
                "POST", "/plugin/mall-advertising/merchant/applications/save");
        request.addHeader("X-Internal-Job-Token", "service-secret");

        filter.doFilter(request, new MockHttpServletResponse(), assertingChain(request));

        assertEquals("internal", request.getAttribute(PluginDispatcher.ATTRIBUTE_TRUSTED_PLUGIN_CALLER));
        assertTrue(SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(authority -> "mall-advertising:merchant:application".equals(authority.getAuthority())));
        org.junit.jupiter.api.Assertions.assertFalse(SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .anyMatch(authority -> "mall-advertising:slot:read".equals(authority.getAuthority())));
    }

    private static FilterChain assertingChain(MockHttpServletRequest expected) {
        return (request, response) -> assertEquals(expected, request);
    }
}
