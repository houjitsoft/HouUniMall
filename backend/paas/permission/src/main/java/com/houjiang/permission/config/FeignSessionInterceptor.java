package com.houjiang.permission.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/** 只透传已有会话，不生成或记录身份凭据。 */
@Component
public class FeignSessionInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate template) {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)) return;
        var request = attributes.getRequest();
        String token = request.getHeader("Authorization");
        if (token == null || token.isBlank()) token = request.getHeader("token");
        if (token != null && !token.isBlank()) template.header("Authorization", token);
    }
}
