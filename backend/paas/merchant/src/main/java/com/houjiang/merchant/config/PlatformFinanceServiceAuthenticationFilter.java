package com.houjiang.merchant.config;

import com.houjiang.commons.util.PlatformServiceSignature;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.function.Predicate;

@Component
public class PlatformFinanceServiceAuthenticationFilter extends OncePerRequestFilter {

    static final int MAX_BODY_BYTES = 64 * 1024;
    private static final String PLATFORM_FINANCE_PREFIX = "/merchant/platform-finance/";
    private final String serviceSecret;
    private final Clock clock;
    private final Predicate<String> nonceClaimer;

    @Autowired
    public PlatformFinanceServiceAuthenticationFilter(
            @Value("${mall.platform-finance.service-secret}") String serviceSecret,
            RedisTemplate<String, Object> redisTemplate) {
        this(serviceSecret, Clock.systemUTC(), nonce -> Boolean.TRUE.equals(
                redisTemplate.opsForValue().setIfAbsent(
                        "MALL_PLATFORM_FINANCE_NONCE:" + nonce, Boolean.TRUE, Duration.ofMinutes(2))));
    }

    PlatformFinanceServiceAuthenticationFilter(String serviceSecret, Clock clock, Predicate<String> nonceClaimer) {
        if (serviceSecret == null || serviceSecret.isBlank()) {
            throw new IllegalStateException("mall.platform-finance.service-secret 未配置");
        }
        this.serviceSecret = serviceSecret;
        this.clock = clock;
        this.nonceClaimer = nonceClaimer;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith(PLATFORM_FINANCE_PREFIX);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (request.getContentLengthLong() > MAX_BODY_BYTES) {
            rejectOversized(response);
            return;
        }
        byte[] body = request.getInputStream().readNBytes(MAX_BODY_BYTES + 1);
        if (body.length > MAX_BODY_BYTES) {
            rejectOversized(response);
            return;
        }
        CachedBodyRequest wrappedRequest = new CachedBodyRequest(request, body);
        String authorization = wrappedRequest.getHeader("Authorization");
        String nonce = wrappedRequest.getHeader(PlatformServiceSignature.NONCE_HEADER);
        boolean trusted = PlatformServiceSignature.verify(
                serviceSecret,
                clock.instant().getEpochSecond(),
                wrappedRequest.getHeader(PlatformServiceSignature.TIMESTAMP_HEADER),
                nonce,
                authorization,
                wrappedRequest.getMethod(),
                wrappedRequest.getRequestURI(),
                body,
                wrappedRequest.getHeader(PlatformServiceSignature.SIGNATURE_HEADER));
        if (trusted) {
            try {
                trusted = nonceClaimer.test(nonce);
            } catch (RuntimeException e) {
                trusted = false;
            }
        }
        Authentication current = SecurityContextHolder.getContext().getAuthentication();
        if (!trusted || current == null || !current.isAuthenticated()) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":403,\"message\":\"平台财务服务身份校验失败\"}");
            return;
        }

        var authorities = new ArrayList<GrantedAuthority>(current.getAuthorities());
        authorities.add(new SimpleGrantedAuthority("ROLE_MALL_PLATFORM_FINANCE"));
        UsernamePasswordAuthenticationToken elevated = new UsernamePasswordAuthenticationToken(
                current.getPrincipal(), current.getCredentials(), authorities);
        elevated.setDetails(current.getDetails());
        SecurityContextHolder.getContext().setAuthentication(elevated);
        chain.doFilter(wrappedRequest, response);
    }

    private static void rejectOversized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":413,\"message\":\"平台财务请求正文过大\"}");
    }

    private static final class CachedBodyRequest extends HttpServletRequestWrapper {
        private final byte[] body;

        private CachedBodyRequest(HttpServletRequest request, byte[] body) {
            super(request);
            this.body = body;
        }

        @Override
        public ServletInputStream getInputStream() {
            ByteArrayInputStream input = new ByteArrayInputStream(body);
            return new ServletInputStream() {
                @Override public boolean isFinished() { return input.available() == 0; }
                @Override public boolean isReady() { return true; }
                @Override public void setReadListener(ReadListener readListener) { }
                @Override public int read() { return input.read(); }
                @Override public int read(byte[] bytes, int offset, int length) {
                    return input.read(bytes, offset, length);
                }
            };
        }

        @Override
        public BufferedReader getReader() {
            return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
        }
    }
}
