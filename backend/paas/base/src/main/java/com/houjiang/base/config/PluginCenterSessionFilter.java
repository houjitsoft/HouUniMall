package com.houjiang.base.config;

import com.alibaba.fastjson.JSON;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.dto.userms.UserInfoDTO;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

/** Authenticates plugin-center requests against server-issued platform sessions. */
public final class PluginCenterSessionFilter extends OncePerRequestFilter {
    private final RedisTemplate<String, Object> redis;
    public PluginCenterSessionFilter(RedisTemplate<String, Object> redis) { this.redis = redis; }

    static boolean isCredentialEndpoint(HttpServletRequest request) {
        String path = request.getRequestURI();
        return ("GET".equals(request.getMethod()) && path.equals("/base/plugin-center/artifact/raw"))
                || ("POST".equals(request.getMethod())
                && path.matches("/base/plugin-center/market/pay/notify/[A-Za-z0-9_-]+"))
                // 插件下载次数：站点上报（X-Platform-Token 在 controller 内 fail-closed 校验）
                // 与真实次数读取（只返回聚合值）。两者都没有平台登录态，
                // 别的安装人在自己站点里调用时也不会带本平台 token。
                // 2026-10-29 追加平台对外目录：商品目录是公开信息（与官网市场页同口径），
                // 开源包本地没有商品快照，站点市场页实时从这里读。
                || ("POST".equals(request.getMethod())
                && (path.equals("/base/plugin-center/market/download/report")
                || path.equals("/base/plugin-center/market/download/stats")
                || path.equals("/base/plugin-center/market/remote/catalog")
                // 2026-10-30 站点换票安装包：X-Platform-Token 在 controller 内 fail-closed 校验，
                // 与下载上报同一把钥匙；站点（别的安装人）没有本平台登录态。
                // 2026-10-30 站点购买授权上报：站点发证后异步上报授权副本（同一道令牌门），
                // 平台按副本放行付费插件 acquire——「站点购买→平台派发」闭环的同步腿。
                || path.equals("/base/plugin-center/market/remote/acquire")
                || path.equals("/base/plugin-center/market/remote/license/report")));
    }

    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                             FilterChain chain) throws ServletException, IOException {
        if (isCredentialEndpoint(request)) { chain.doFilter(request, response); return; }
        String token = request.getHeader("Authorization");
        if (token == null || token.isBlank()) token = request.getHeader("token");
        if (token != null && token.regionMatches(true, 0, "Bearer ", 0, 7)) token = token.substring(7);
        if (token == null || token.isBlank()) { reject(response, 401, "未登录或登录已过期"); return; }
        try {
            token = token.trim();
            var authorities = new ArrayList<SimpleGrantedAuthority>();
            Object principal;
            Object admin = redis.opsForValue().get("MALL_ADMIN_TOKEN:" + token);
            if (admin instanceof Map<?, ?> map && map.get("id") != null
                    && !"1".equals(String.valueOf(map.get("disable")))) {
                // Preserve the existing mall-{id} purchase identity; no Mall service dependency.
                principal = map;
                authorities.add(new SimpleGrantedAuthority("ROLE_MALL_ADMIN"));
            } else {
                Object cached = redis.opsForValue().get(HouConfig.HOU_USER_TOKEN_USER + token);
                UserInfoDTO session = cached instanceof UserInfoDTO dto ? dto
                        : cached == null ? null : JSON.parseObject(cached instanceof String s ? s : JSON.toJSONString(cached), UserInfoDTO.class);
                if (session == null || session.getUser() == null || session.getUser().getUserId() == null) {
                    reject(response, 401, "未登录或登录已过期"); return;
                }
                principal = session.getUser();
                authorities.add(new SimpleGrantedAuthority("ROLE_CLOUD_USER"));
                if (session.getPermissions() != null) session.getPermissions().stream()
                        .filter(p -> p != null && !p.isBlank()).distinct()
                        .map(SimpleGrantedAuthority::new).forEach(authorities::add);
            }
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(principal, null, authorities));
        } catch (RuntimeException unavailable) {
            reject(response, 503, "身份服务暂时不可用"); return;
        }
        chain.doFilter(request, response);
    }

    private static void reject(HttpServletResponse response, int status, String message) throws IOException {
        SecurityContextHolder.clearContext();
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(JSON.toJSONString(Map.of("code", status, "msg", message)));
    }
}
