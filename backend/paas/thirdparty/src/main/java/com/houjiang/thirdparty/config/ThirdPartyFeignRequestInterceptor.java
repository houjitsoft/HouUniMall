package com.houjiang.thirdparty.config;

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
public class ThirdPartyFeignRequestInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String authorization = request.getHeader("Authorization");
            if (authorization != null && !authorization.trim().isEmpty()) {
                template.header("Authorization", authorization);
            }
            // 不再输出日志，因为未登录用户调用是正常情况（如发送验证码）
        }
    }
}