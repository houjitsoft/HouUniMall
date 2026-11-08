package com.houjiang.merchant.config;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdvertisingLifecycleInternalTokenFilterTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void authenticatesOnlyExactAdvertisingMenuLifecyclePaths() throws Exception {
        AdvertisingLifecycleInternalTokenFilter filter =
                new AdvertisingLifecycleInternalTokenFilter("service-secret");
        MockHttpServletRequest request = request(
                "/merchant/internal/advertising/menu/sync", "service-secret");
        MockHttpServletResponse response = new MockHttpServletResponse();
        boolean[] invoked = {false};

        filter.doFilter(request, response, (ignoredRequest, ignoredResponse) -> invoked[0] = true);

        assertTrue(invoked[0]);
        assertTrue(SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADVERTISING_LIFECYCLE".equals(authority.getAuthority())));

        SecurityContextHolder.clearContext();
        MockHttpServletRequest unrelated = request(
                "/merchant/internal/shop-provision/list", "service-secret");
        filter.doFilter(unrelated, new MockHttpServletResponse(), assertingChain(unrelated));
        assertFalse(SecurityContextHolder.getContext().getAuthentication() != null
                && SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADVERTISING_LIFECYCLE".equals(authority.getAuthority())));
    }

    @Test
    void rejectsMissingOrWrongTokenBeforeController() throws Exception {
        AdvertisingLifecycleInternalTokenFilter filter =
                new AdvertisingLifecycleInternalTokenFilter("service-secret");
        for (String token : new String[]{"", "wrong"}) {
            MockHttpServletRequest request = request(
                    "/merchant/internal/advertising/menu/remove", token);
            MockHttpServletResponse response = new MockHttpServletResponse();
            boolean[] invoked = {false};

            filter.doFilter(request, response, (ignoredRequest, ignoredResponse) -> invoked[0] = true);

            assertFalse(invoked[0]);
            assertEquals(403, response.getStatus());
            assertEquals("application/json;charset=UTF-8", response.getContentType());
            SecurityContextHolder.clearContext();
        }
    }

    private static MockHttpServletRequest request(String uri, String token) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", uri);
        if (!token.isEmpty()) request.addHeader("X-Internal-Job-Token", token);
        return request;
    }

    private static FilterChain assertingChain(MockHttpServletRequest expected) {
        return (request, response) -> assertEquals(expected, request);
    }
}
