package com.houjiang.bffweb.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Enumeration;

/**
 * Feign请求拦截器
/**
 * 
 * 【重要说明】Token传递机制：
 * 1. 前端请求必须携带Authorization头（格式：Bearer {token}）
 * 2. BFF层（bff-web）不进行JWT验证，只负责转发请求和传递token
 * 3. 本拦截器从RequestContextHolder获取Authorization头，并传递给下游PAAS服务
 * 4. 下游PAAS服务（如oa、pm等）统一使用commons的JwtAuthenticationTokenFilter进行验证
 * 5. PAAS服务的JWT过滤器会从Redis中验证token并获取用户信息
 * 
 * 【关键点】：
 * - 所有前端请求必须携带token（登录接口除外）
 * - BFF层只做转发，不验证token
 * - PAAS层统一使用commons的JWT过滤器验证
 * - 如果出现"用户未登录"错误，检查：
 *   1. 前端是否正确携带Authorization头
 *   2. FeignRequestInterceptor是否正确传递token
 *   3. PAAS服务的SecurityConfig是否正确配置JWT过滤器
 *   4. Redis中是否存在对应的token（可能已过期）
 * 
 * @author Amos
 * @since 2025-01-07
 */
@Slf4j
@Component
public class FeignRequestInterceptor implements RequestInterceptor {

    /** 标记请求来自 bff-web，供 paas 操作日志切面防双写 */
    public static final String GATEWAY_HEADER = "X-Hou-Gateway";
    public static final String GATEWAY_VALUE = "bff-web";

    @Override
    public void apply(RequestTemplate template) {
        String url = template.url();
        log.debug("FeignRequestInterceptor - 处理请求: {}", url);

        // 始终标记网关来源（含认证接口），paas SysLogAspect 据此跳过自动落库
        template.header(GATEWAY_HEADER, GATEWAY_VALUE);
        
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            log.error("【严重错误】无法获取ServletRequestAttributes，无法传递Authorization头！" +
                "\n请求URL: {}" +
                "\n可能原因：Feign请求在异步线程中执行，RequestContextHolder丢失" +
                "\n解决方案：确保Feign请求在同一线程中同步执行",
                url);
            return;
        }
        
        HttpServletRequest request = attributes.getRequest();
        
        // 获取Authorization头（支持多种格式）
        String authorization = request.getHeader("Authorization");
        
        // 如果没有Authorization头，尝试从其他可能的头部获取
        if (authorization == null || authorization.isEmpty()) {
            authorization = request.getHeader("authorization");
        }

        // 老 uni-app 客户端（H5/小程序）不带 Authorization，只带自定义头 Token（原始 JWT 串）。
        // 它是全站请求的默认头，但下游 PAAS 统一按 Authorization: Bearer 校验，缺这一步时
        // 结算算价 / 提交订单等非白名单路径一律 30802「请先登录后再操作」。
        if (authorization == null || authorization.isEmpty()) {
            String legacyToken = request.getHeader("Token");
            if (legacyToken == null || legacyToken.isBlank()) {
                legacyToken = request.getHeader("token");
            }
            if (legacyToken != null && !legacyToken.isBlank()
                    && !"undefined".equals(legacyToken) && !"null".equals(legacyToken)) {
                authorization = legacyToken.startsWith("Bearer ") ? legacyToken : "Bearer " + legacyToken.trim();
            }
        }
        
        // 对于认证相关的接口，不传递Authorization头
        if (url != null && (url.contains("/auth/") || url.contains("/user/login") ||
            url.contains("/user/codelogin") || url.contains("/register/") ||
            url.contains("/platform/userinfo/getVerify") || url.contains("/shop/getShopVerify") ||
            url.contains("/shop/getSupplierVerify") || url.endsWith("/getVerify") ||
            isCaptchaGenerationEndpoint(url) || url.contains("/verifyGenerateCaptcha"))) {
            log.debug("跳过传递Authorization头，认证相关接口: {}", url);
            return;
        }
        
        // 传递Authorization头到下游服务
        if (authorization != null && !authorization.isEmpty() && 
            !"undefined".equals(authorization) && !"null".equals(authorization)) {
            template.header("Authorization", authorization);
            log.debug("已传递Authorization头到下游服务: {}", url);
        } else {
            log.warn("请求中没有有效的Authorization头，URL: {}", url);
        }

        String orgId = request.getHeader("X-Org-Id");
        if (orgId != null && !orgId.isBlank()) {
            template.header("X-Org-Id", orgId.trim());
        }

        // 转发公网域名信息，供下游服务生成对外 URL（如日历 ICS 订阅链接）
        forwardProxyHeaders(template, request);
    }

    /**
     * The auth service exposes its anonymous slide-captcha endpoint as
     * {@code /generate}. Do not use a generic suffix check here: GEO/AI/OA
     * business endpoints also contain paths ending in {@code /generate} and
     * still require the caller's JWT.
     */
    private boolean isCaptchaGenerationEndpoint(String url) {
        return "/generate".equals(url)
            || url.endsWith("/auth/generate")
            || url.endsWith("/web/auth/generate");
    }

    /**
     * 将 BFF 收到的公网访问信息转发给下游 PAAS，避免下游只看到 127.0.0.1:端口。
     */
    private void forwardProxyHeaders(RequestTemplate template, HttpServletRequest request) {
        String forwardedHost = request.getHeader("X-Forwarded-Host");
        if (forwardedHost == null || forwardedHost.isBlank()) {
            forwardedHost = request.getHeader("Host");
        }
        if (forwardedHost != null && !forwardedHost.isBlank()) {
            template.header("X-Forwarded-Host", forwardedHost.split(",")[0].trim());
        }

        String forwardedProto = request.getHeader("X-Forwarded-Proto");
        if (forwardedProto == null || forwardedProto.isBlank()) {
            forwardedProto = request.getScheme();
        }
        if (forwardedProto != null && !forwardedProto.isBlank()) {
            template.header("X-Forwarded-Proto", forwardedProto.split(",")[0].trim());
        }
    }
    
    /**
     * 获取所有请求头（用于调试）
     */
    private String getRequestHeaders(HttpServletRequest request) {
        StringBuilder headers = new StringBuilder();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            headers.append(headerName).append(": ").append(headerValue).append("; ");
        }
        return headers.toString();
    }
}
