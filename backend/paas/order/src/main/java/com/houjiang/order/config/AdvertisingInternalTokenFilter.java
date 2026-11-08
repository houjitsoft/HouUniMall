package com.houjiang.order.config;

import com.houjiang.plugin.engine.core.PluginDispatcher;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

/** Authenticates only the stable slot and merchant adapter paths of mall-advertising. */
public final class AdvertisingInternalTokenFilter extends OncePerRequestFilter {

    static final String HEADER_INTERNAL_TOKEN = "X-Internal-Job-Token";
    static final String PUBLIC_SLOT_PATTERN = "/plugin/mall-advertising/public/slots/*";
    static final String MERCHANT_PATTERN = "/plugin/mall-advertising/merchant/**";

    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final String expectedToken;

    public AdvertisingInternalTokenFilter(String expectedToken) {
        this.expectedToken = expectedToken == null ? "" : expectedToken.trim();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        boolean publicSlot = "GET".equals(request.getMethod())
                && pathMatcher.match(PUBLIC_SLOT_PATTERN, request.getRequestURI());
        boolean merchant = "POST".equals(request.getMethod())
                && pathMatcher.match(MERCHANT_PATTERN, request.getRequestURI());
        if ((publicSlot || merchant) && !expectedToken.isBlank()) {
            String provided = request.getHeader(HEADER_INTERNAL_TOKEN);
            if (StringUtils.hasText(provided) && matches(provided.trim(), expectedToken)) {
                SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                        "advertising-slot-reader", null,
                        List.of(new SimpleGrantedAuthority("ROLE_INTERNAL_JOB"),
                                new SimpleGrantedAuthority(publicSlot
                                        ? "mall-advertising:slot:read"
                                        : "mall-advertising:merchant:application"))));
                PluginDispatcher.markCaller(request, PluginDispatcher.CALLER_INTERNAL);
            }
        }
        chain.doFilter(request, response);
    }

    private static boolean matches(String provided, String expected) {
        return MessageDigest.isEqual(provided.getBytes(StandardCharsets.UTF_8),
                expected.getBytes(StandardCharsets.UTF_8));
    }
}
