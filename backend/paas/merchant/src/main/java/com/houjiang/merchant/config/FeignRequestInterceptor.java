package com.houjiang.merchant.config;

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
 * @since 2026-04-28
 */
@Component
public class FeignRequestInterceptor implements RequestInterceptor {

    /**
     * 拦截Feign请求，透传Authorization头
     * 
     * @param template Feign请求模板
     */
    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String authorization = request.getHeader("Authorization");
            if (authorization != null) {
                template.header("Authorization", authorization);
            }
            String orgId = request.getHeader("X-Org-Id");
            if (orgId != null && !orgId.isBlank()) {
                template.header("X-Org-Id", orgId.trim());
            }
        }
    }
}
