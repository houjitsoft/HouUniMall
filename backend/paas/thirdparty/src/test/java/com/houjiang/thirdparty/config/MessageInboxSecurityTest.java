package com.houjiang.thirdparty.config;

import com.houjiang.commons.config.RedisCache;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.HandlerExceptionResolver;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class MessageInboxSecurityTest {

    @Test
    void inboxRequiresJwtWhileInternalSendEndpointKeepsServiceCompatibility() throws Exception {
        RedisCache redisCache = mock(RedisCache.class);
        HandlerExceptionResolver resolver = mock(HandlerExceptionResolver.class);
        SecurityConfig config = new SecurityConfig(redisCache, resolver);
        var filter = config.jwtAuthenticationTokenFilter();

        FilterChain inboxChain = mock(FilterChain.class);
        MockHttpServletRequest inbox = new MockHttpServletRequest("GET", "/message/inbox/list");
        filter.doFilter(inbox, new MockHttpServletResponse(), inboxChain);

        verify(inboxChain, never()).doFilter(any(), any());
        verify(resolver).resolveException(any(), any(), any(), any());

        FilterChain sendChain = mock(FilterChain.class);
        MockHttpServletRequest send = new MockHttpServletRequest("POST", "/message/send/by-channel");
        filter.doFilter(send, new MockHttpServletResponse(), sendChain);

        verify(sendChain).doFilter(any(), any());
    }
}
