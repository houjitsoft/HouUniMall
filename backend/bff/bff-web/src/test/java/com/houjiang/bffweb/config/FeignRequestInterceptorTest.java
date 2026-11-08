package com.houjiang.bffweb.config;

import feign.RequestTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class FeignRequestInterceptorTest {

    @AfterEach
    void clearRequestContext() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void keepsJwtForGeoGenerateBusinessEndpoint() {
        MockHttpServletRequest request = requestWithAuthorization();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        RequestTemplate template = new RequestTemplate();
        template.target("http://geo");
        template.uri("/geo/content/ai-studio/generate");

        new FeignRequestInterceptor().apply(template);

        assertEquals(List.of("Bearer token"), List.copyOf(template.headers().get("Authorization")));
    }

    @Test
    void omitsJwtForAnonymousCaptchaGenerationEndpoint() {
        MockHttpServletRequest request = requestWithAuthorization();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        RequestTemplate template = new RequestTemplate();
        template.target("http://auth");
        template.uri("/generate");

        new FeignRequestInterceptor().apply(template);

        assertFalse(template.headers().containsKey("Authorization"));
    }

    private static MockHttpServletRequest requestWithAuthorization() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token");
        return request;
    }
}
