package com.houjiang.plugin.service;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Objects;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Asks the base control plane whether a product is enabled for the caller.
 * Brief base/auth blips must not become hard 503s for every OA/ERP request:
 * retry a few times before letting {@link ProductAccessInterceptor} fail closed.
 */
public final class RemoteProductAccessPolicy implements ProductAccessPolicy {
    private static final int MAX_ATTEMPTS = 3;
    private static final long RETRY_SLEEP_MS = 150L;

    private final RestTemplate client;
    private final int maxAttempts;
    private final long retrySleepMs;

    public RemoteProductAccessPolicy(RestTemplate client) {
        this(client, MAX_ATTEMPTS, RETRY_SLEEP_MS);
    }

    RemoteProductAccessPolicy(RestTemplate client, int maxAttempts, long retrySleepMs) {
        this.client = Objects.requireNonNull(client, "client");
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be >= 1");
        }
        this.maxAttempts = maxAttempts;
        this.retrySleepMs = Math.max(0L, retrySleepMs);
    }

    @Override
    public boolean isEnabled(String code, HttpServletRequest request) {
        RuntimeException last = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return exchangeEnabled(code, request);
            } catch (RestClientException ex) {
                last = ex;
                if (attempt < maxAttempts) {
                    sleepQuietly(retrySleepMs);
                }
            }
        }
        throw last != null ? last : new IllegalStateException("product access check failed");
    }

    private boolean exchangeEnabled(String code, HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();
        // 与 JwtAuthenticationTokenFilter 对齐：部分客户端只传 token/Token，
        // 产品闸问 base 时必须带 Authorization，否则会误判为不可用。
        String authorization = request.getHeader("Authorization");
        if (authorization == null || authorization.isBlank()
                || "null".equalsIgnoreCase(authorization.trim())
                || "undefined".equalsIgnoreCase(authorization.trim())) {
            String token = request.getHeader("Token");
            if (token == null || token.isBlank()) {
                token = request.getHeader("token");
            }
            if (token != null && !token.isBlank()) {
                authorization = token.regionMatches(true, 0, "Bearer ", 0, 7)
                        ? token : ("Bearer " + token);
            }
        }
        if (authorization != null && !authorization.isBlank()) {
            headers.set(HttpHeaders.AUTHORIZATION, authorization);
        }
        for (String name : new String[]{"X-Org-Id", "X-Internal-Job-Token"}) {
            String value = request.getHeader(name);
            if (value != null) {
                headers.set(name, value);
            }
        }
        ResponseEntity<Map> result = client.exchange(
                "http://base/internal/plugin-access/" + code,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class);
        return result.getBody() != null && Boolean.TRUE.equals(result.getBody().get("enabled"));
    }

    private static void sleepQuietly(long ms) {
        if (ms <= 0L) {
            return;
        }
        try {
            Thread.sleep(ms);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
        }
    }
}
