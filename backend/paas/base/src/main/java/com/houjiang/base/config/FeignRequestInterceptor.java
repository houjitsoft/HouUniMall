package com.houjiang.base.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Feign请求拦截器
 * 用于传递Authorization头到下游服务
 * 
 * @author Amos
 * @since 2025-01-07
 */
@Component
public class FeignRequestInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        System.out.println("FeignRequestInterceptor.apply() called for URL: " + template.url());

        // Internal lifecycle clients authenticate with their own service token. If their
        // client-specific interceptor ran first, never reattach the browser credential.
        if (template.headers().containsKey("X-Internal-Job-Token")) {
            template.removeHeader("Authorization");
            return;
        }
        
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String authorization = request.getHeader("Authorization");
            System.out.println("Authorization header: " + authorization);
            if (authorization != null) {
                template.header("Authorization", authorization);
                System.out.println("Authorization header added to Feign request");
            } else {
                System.out.println("No Authorization header found in request");
            }
            String orgId = request.getHeader("X-Org-Id");
            if (orgId != null && !orgId.isBlank()) {
                template.header("X-Org-Id", orgId.trim());
            }
        } else {
            System.out.println("No ServletRequestAttributes found");
        }
    }
}
