package com.houjiang.userms.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class OAuthInternalTokenFilterTest {
    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    void check(String configured, String supplied, String method, boolean allowed) throws Exception {
        var request = new MockHttpServletRequest(method, OAuthInternalTokenFilter.PATH);
        request.setServletPath(OAuthInternalTokenFilter.PATH);
        if (supplied != null) request.addHeader(OAuthInternalTokenFilter.HEADER, supplied);
        var response = new MockHttpServletResponse();
        AtomicBoolean reached = new AtomicBoolean();
        new OAuthInternalTokenFilter(configured).doFilter(request, response, (req, res) -> reached.set(true));
        assertEquals(allowed, reached.get());
        if (!allowed) assertEquals(403, response.getStatus());
    }

    @Test
    void absentOrWrongTokenDeniedEvenForAuthenticatedMember() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("member", null, List.of()));
        check("server-only-secret", null, "POST", false);
        check("server-only-secret", "wrong", "POST", false);
    }

    @Test
    void blankConfigurationFailsClosed() throws Exception {
        check("", "", "POST", false);
        check(null, "anything", "POST", false);
    }

    @Test
    void validInternalTokenGrantsOnlyInternalRole() throws Exception {
        check("server-only-secret", "server-only-secret", "POST", true);
        assertEquals("ROLE_INTERNAL_OAUTH", SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().iterator().next().getAuthority());
    }

    @Test
    void actualSecurityChainRequiresInternalCredential() throws Exception {
        try (var context = new org.springframework.web.context.support.AnnotationConfigWebApplicationContext()) {
            context.setServletContext(new org.springframework.mock.web.MockServletContext());
            context.getEnvironment().getPropertySources().addFirst(new org.springframework.core.env.MapPropertySource(
                    "test", java.util.Map.of("unilogin.internal-token", "server-only-secret")));
            context.register(WebConfig.class);
            context.refresh();
            var mvc = org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup(context)
                    .addFilters(context.getBean("springSecurityFilterChain", jakarta.servlet.Filter.class)).build();
            String body = "{\"channel\":\"wechat\"}";
            mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(PATH())
                    .servletPath(PATH()).contentType("application/json").content(body))
                    .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isForbidden());
            mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(PATH())
                    .servletPath(PATH()).header("Authorization", "Bearer ordinary-member-token")
                    .contentType("application/json").content(body))
                    .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isForbidden());
            mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(PATH())
                    .servletPath(PATH()).header(OAuthInternalTokenFilter.HEADER, "server-only-secret")
                    .contentType("application/json").content(body))
                    .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                    .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.data.userId").value("42"));
        }
    }

    private static String PATH() { return OAuthInternalTokenFilter.PATH; }

    @org.springframework.context.annotation.Configuration
    @org.springframework.web.servlet.config.annotation.EnableWebMvc
    @org.springframework.context.annotation.Import({SecurityConfig.class, com.houjiang.userms.controller.OAuthMemberController.class})
    static class WebConfig {
        @org.springframework.context.annotation.Bean
        org.springframework.data.redis.core.RedisTemplate<Object, Object> redisTemplate() {
            return org.mockito.Mockito.mock(org.springframework.data.redis.core.RedisTemplate.class);
        }

        @org.springframework.context.annotation.Bean
        com.houjiang.commons.config.RedisCache redisCache() {
            return org.mockito.Mockito.mock(com.houjiang.commons.config.RedisCache.class);
        }

        @org.springframework.context.annotation.Bean
        com.houjiang.userms.service.OAuthMemberService memberService() {
            var service = org.mockito.Mockito.mock(com.houjiang.userms.service.OAuthMemberService.class);
            org.mockito.Mockito.when(service.getOrCreate(org.mockito.ArgumentMatchers.anyMap()))
                    .thenReturn(java.util.Map.of("userId", "42", "created", false));
            return service;
        }
    }

    @Test
    void nonPostDenied() throws Exception {
        check("server-only-secret", "server-only-secret", "GET", false);
    }
}
