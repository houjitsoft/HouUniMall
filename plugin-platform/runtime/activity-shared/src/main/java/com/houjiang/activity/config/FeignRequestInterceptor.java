package com.houjiang.activity.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
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
public class FeignRequestInterceptor implements RequestInterceptor {
/*
 * 说明：本类不再标注 @Component。
 * activity-shared 是给插件用的共享层，宿主需 compile 依赖以满足 PF4J 的类加载；
 * 带 @Component 会被宿主组件扫描注册成 bean，与宿主自己的同名 bean 冲突。
 * 13 个活动插件中无人使用本类（cms 另有自己的同名实现），去掉注解无功能影响。
 */


    @Override
    public void apply(RequestTemplate template) {
        System.out.println("FeignRequestInterceptor.apply() called for URL: " + template.url());
        
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
