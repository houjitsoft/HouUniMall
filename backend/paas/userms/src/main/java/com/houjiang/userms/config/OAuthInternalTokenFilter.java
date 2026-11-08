package com.houjiang.userms.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

/** 沿用项目窄路径内部令牌模式；OAuth 专用密钥不能下发浏览器。 */
public class OAuthInternalTokenFilter extends OncePerRequestFilter {
    public static final String PATH = "/user/ms/feign/oauth/get-or-create";
    public static final String HEADER = "X-Internal-OAuth-Token";
    private final String expected;

    public OAuthInternalTokenFilter(String expected) {
        this.expected = expected == null ? "" : expected;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !PATH.equals(request.getServletPath());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String provided = request.getHeader(HEADER);
        if (!"POST".equals(request.getMethod()) || expected.isBlank() || provided == null
                || !MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), provided.getBytes(StandardCharsets.UTF_8))) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":403,\"message\":\"OAuth 内部调用认证失败\"}");
            return;
        }
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "auth-oauth", null, List.of(new SimpleGrantedAuthority("ROLE_INTERNAL_OAUTH"))));
        chain.doFilter(request, response);
    }
}
