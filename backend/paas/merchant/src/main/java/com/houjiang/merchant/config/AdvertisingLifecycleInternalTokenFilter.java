package com.houjiang.merchant.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Set;

/** Protects only the three mall-advertising bcenter menu lifecycle callbacks. */
public final class AdvertisingLifecycleInternalTokenFilter extends OncePerRequestFilter {

    private static final String HEADER = "X-Internal-Job-Token";
    private static final Set<String> PATHS = Set.of(
            "/merchant/internal/advertising/menu/sync",
            "/merchant/internal/advertising/menu/disable",
            "/merchant/internal/advertising/menu/remove");

    private final String expectedToken;

    public AdvertisingLifecycleInternalTokenFilter(String expectedToken) {
        this.expectedToken = expectedToken == null ? "" : expectedToken.trim();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !"POST".equals(request.getMethod()) || !PATHS.contains(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String provided = request.getHeader(HEADER);
        if (expectedToken.isBlank() || !StringUtils.hasText(provided)
                || !MessageDigest.isEqual(provided.trim().getBytes(StandardCharsets.UTF_8),
                expectedToken.getBytes(StandardCharsets.UTF_8))) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":403,\"message\":\"广告插件生命周期身份校验失败\"}");
            return;
        }
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "mall-advertising-lifecycle", null,
                List.of(new SimpleGrantedAuthority("ROLE_ADVERTISING_LIFECYCLE"))));
        chain.doFilter(request, response);
    }
}
