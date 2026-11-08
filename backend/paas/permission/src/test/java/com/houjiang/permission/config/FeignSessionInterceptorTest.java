package com.houjiang.permission.config;

import feign.RequestTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import static org.junit.jupiter.api.Assertions.*;

class FeignSessionInterceptorTest {
    @AfterEach void clear() { RequestContextHolder.resetRequestAttributes(); }
    @Test void forwardsExistingTokenWithoutIdentityHeaders() {
        var request = new MockHttpServletRequest(); request.addHeader("token", "session");
        request.addHeader("X-User-Id", "forged-admin");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        var template = new RequestTemplate();
        new FeignSessionInterceptor().apply(template);
        assertEquals(java.util.List.of("session"), java.util.List.copyOf(template.headers().get("Authorization")));
        assertFalse(template.headers().containsKey("X-User-Id"));
    }
}
