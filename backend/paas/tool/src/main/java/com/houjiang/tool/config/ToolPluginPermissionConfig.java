package com.houjiang.tool.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.houjiang.plugin.engine.security.PluginPermissionEvaluator;
import com.houjiang.plugin.engine.security.SpringSecurityPluginPermissionEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.List;

/**
 * OCR 管理查询的权限判定。
 *
 * <p>两类授权来源缺一不可，任一命中即放行，都没有则拒绝：
 * <ol>
 *   <li><b>当前请求的 Security 上下文</b>（{@code JwtAuthenticationTokenFilter} 写入）：超级管理员
 *       这类账号在 Redis 会话里的 permissions 就是 {@code ["*"]}，且没有任何 RBAC 角色菜单，
 *       权限服务 {@code /permission/user/permissions} 只会返回空数组。只按 RBAC 实时精确比对
 *       会把超管直接 403（2026-10-27 线上复核：user_id=1 调 {@code /web/plugin/ocr/stat} 报
 *       “无权访问插件接口”，而带完整 RBAC 的账号正常）。</li>
 *   <li><b>权限服务的当前 RBAC 授权</b>：菜单与子权限上线后，登录时间在之前的会话缓存里没有
 *       新增的子菜单权限（如 {@code shell:ocr:overview}），实时查询才能拿到最新授权。</li>
 * </ol>
 *
 * <p>入口权限 {@code shell:ocr:apps} 不自动等价于全部子权限：只有显式通配授权才放宽，
 * 细粒度子权限仍以实时 RBAC 为准；未登录、空权限、权限服务不可用一律拒绝（fail-closed）。
 */
@Configuration
public class ToolPluginPermissionConfig {

    @Bean
    public PluginPermissionEvaluator toolPluginPermissionEvaluator(
            @Value("${houcloud.tool.permission-url:http://127.0.0.1:8300}") String serviceUrl) {
        var factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(5000);
        return new CurrentPermissionEvaluator(new RestTemplate(factory), serviceUrl);
    }

    public static final class CurrentPermissionEvaluator implements PluginPermissionEvaluator {
        private static final Logger log = LoggerFactory.getLogger(CurrentPermissionEvaluator.class);
        private final SpringSecurityPluginPermissionEvaluator delegate = new SpringSecurityPluginPermissionEvaluator();
        private final RestTemplate rest;
        private final String url;

        public CurrentPermissionEvaluator(RestTemplate rest, String serviceUrl) {
            this.rest = rest;
            this.url = serviceUrl.replaceAll("/+$", "") + "/permission/user/permissions";
        }

        @Override
        public boolean isAuthenticated() {
            return delegate.isAuthenticated();
        }

        @Override
        public boolean hasPermission(String permission) {
            if (!isAuthenticated() || permission == null || permission.isBlank()) {
                return false;
            }
            // 仅对本宿主 OCR 管理权限走实时 RBAC，其他插件保持既有判定。
            if (!permission.startsWith("shell:ocr:")) {
                return delegate.hasPermission(permission);
            }
            // ① 会话自身已持有该权限，或持有显式通配授权（超管 "*"）→ 直接放行，无需再查权限服务。
            if (grants(contextPermissions(), permission)) {
                return true;
            }
            if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)) {
                log.warn("OCR 管理权限 {} 校验失败：缺少请求上下文（线程内无 HTTP 请求）", permission);
                return false;
            }
            var request = attributes.getRequest();
            HttpHeaders headers = new HttpHeaders();
            for (String name : new String[]{"Authorization", "token", "tenant-id"}) {
                String value = request.getHeader(name);
                if (value != null && !value.isBlank()) {
                    headers.set(name, value);
                }
            }
            if (!headers.containsKey("Authorization") && !headers.containsKey("token")) {
                log.warn("OCR 管理权限 {} 校验失败：请求未携带登录凭据", permission);
                return false;
            }
            headers.setContentType(MediaType.APPLICATION_JSON);
            // 不传 userId：权限服务必须从转发的真实登录身份读取本人权限。
            List<String> granted;
            try {
                JsonNode response = rest.exchange(url, HttpMethod.POST,
                        new HttpEntity<>(java.util.Map.of(), headers), JsonNode.class).getBody();
                if (response == null || response.path("code").asInt(-1) != 1 || !response.path("data").isArray()) {
                    log.warn("OCR 管理权限 {} 校验失败：权限服务返回异常响应", permission);
                    return false;
                }
                granted = new ArrayList<>();
                for (JsonNode node : response.path("data")) {
                    granted.add(node.asText());
                }
            } catch (RuntimeException unavailable) {
                // 权限服务失败必须拒绝，不回落到旧会话父权限、不泄露凭据。
                log.warn("OCR 管理权限 {} 校验失败：权限服务不可用（{}）", permission, unavailable.toString());
                return false;
            }
            if (!grants(granted, permission)) {
                log.warn("OCR 管理权限 {} 校验失败：当前账号既无会话授权也无 RBAC 授权", permission);
                return false;
            }
            return true;
        }

        private static List<String> contextPermissions() {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null) {
                return List.of();
            }
            List<String> permissions = new ArrayList<>();
            for (GrantedAuthority authority : authentication.getAuthorities()) {
                permissions.add(authority.getAuthority());
            }
            return permissions;
        }

        /** 精确匹配加显式通配（{@code *}、{@code shell:ocr:*}）；入口父权限不等价于子权限。 */
        private static boolean grants(List<String> granted, String permission) {
            for (String value : granted) {
                if (value == null) {
                    continue;
                }
                String candidate = value.trim();
                if (candidate.isEmpty()) {
                    continue;
                }
                if (candidate.equals(permission) || candidate.equals("*") || candidate.equals("*:*:*")) {
                    return true;
                }
                if (candidate.endsWith(":*")
                        && permission.startsWith(candidate.substring(0, candidate.length() - 1))) {
                    return true;
                }
            }
            return false;
        }
    }
}
