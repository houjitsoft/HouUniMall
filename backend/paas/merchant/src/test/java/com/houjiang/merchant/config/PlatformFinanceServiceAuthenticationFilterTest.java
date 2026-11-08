package com.houjiang.merchant.config;

import com.houjiang.commons.util.PlatformServiceSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlatformFinanceServiceAuthenticationFilterTest {

    private static final long NOW = 1_800_000_000L;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void rejectsDirectUserTokenWithoutMallServiceSignature() throws Exception {
        PlatformFinanceServiceAuthenticationFilter filter = filter();
        MockHttpServletRequest request = request();
        MockHttpServletResponse response = new MockHttpServletResponse();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("cloud-user", null, List.of()));
        AtomicBoolean continued = new AtomicBoolean();

        filter.doFilter(request, response, (req, res) -> continued.set(true));

        assertEquals(403, response.getStatus());
        assertFalse(continued.get());
    }

    @Test
    void grantsPlatformFinanceRoleToSignedMallRequest() throws Exception {
        Set<String> nonces = new HashSet<>();
        PlatformFinanceServiceAuthenticationFilter filter = filter(nonces);
        MockHttpServletRequest request = signedRequest("nonce-1", "{\"id\":42}");
        MockHttpServletResponse response = new MockHttpServletResponse();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("cloud-user", null, List.of()));

        filter.doFilter(request, response, (req, res) -> { });

        assertTrue(SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(authority -> "ROLE_MALL_PLATFORM_FINANCE".equals(authority.getAuthority())));
    }

    @Test
    void rejectsTamperedBodyAndRepeatedNonce() throws Exception {
        Set<String> nonces = new HashSet<>();
        PlatformFinanceServiceAuthenticationFilter filter = filter(nonces);

        MockHttpServletRequest tampered = signedRequest("nonce-tampered", "{\"id\":42}");
        tampered.setContent("{\"id\":43}".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        MockHttpServletResponse tamperedResponse = new MockHttpServletResponse();
        authenticate();
        filter.doFilter(tampered, tamperedResponse, (req, res) -> { });
        assertEquals(403, tamperedResponse.getStatus());

        MockHttpServletRequest first = signedRequest("nonce-replay", "{\"id\":42}");
        MockHttpServletResponse firstResponse = new MockHttpServletResponse();
        AtomicBoolean firstContinued = new AtomicBoolean();
        authenticate();
        filter.doFilter(first, firstResponse, (req, res) -> firstContinued.set(true));
        assertTrue(firstContinued.get());

        MockHttpServletRequest replay = signedRequest("nonce-replay", "{\"id\":42}");
        MockHttpServletResponse replayResponse = new MockHttpServletResponse();
        AtomicBoolean replayContinued = new AtomicBoolean();
        authenticate();
        filter.doFilter(replay, replayResponse, (req, res) -> replayContinued.set(true));
        assertEquals(403, replayResponse.getStatus());
        assertFalse(replayContinued.get());
    }

    @Test
    void rejectsOversizedBodyBeforeAuthentication() throws Exception {
        PlatformFinanceServiceAuthenticationFilter filter = filter();
        MockHttpServletRequest request = request();
        request.setContent(new byte[PlatformFinanceServiceAuthenticationFilter.MAX_BODY_BYTES + 1]);
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean continued = new AtomicBoolean();
        authenticate();

        filter.doFilter(request, response, (req, res) -> continued.set(true));

        assertEquals(413, response.getStatus());
        assertFalse(continued.get());
    }

    private static PlatformFinanceServiceAuthenticationFilter filter() {
        return filter(new HashSet<>());
    }

    private static PlatformFinanceServiceAuthenticationFilter filter(Set<String> nonces) {
        return new PlatformFinanceServiceAuthenticationFilter(
                "test-service-secret", Clock.fixed(Instant.ofEpochSecond(NOW), ZoneOffset.UTC), nonces::add);
    }

    private static MockHttpServletRequest request() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/merchant/platform-finance/refund/list");
        request.addHeader("Authorization", "Bearer cloud-token");
        return request;
    }

    private static MockHttpServletRequest signedRequest(String nonce, String body) {
        MockHttpServletRequest request = request();
        byte[] bytes = body.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        request.setContent(bytes);
        request.addHeader(PlatformServiceSignature.TIMESTAMP_HEADER, Long.toString(NOW));
        request.addHeader(PlatformServiceSignature.NONCE_HEADER, nonce);
        request.addHeader(PlatformServiceSignature.SIGNATURE_HEADER,
                PlatformServiceSignature.sign("test-service-secret", NOW, nonce, "Bearer cloud-token",
                        request.getMethod(), request.getRequestURI(), bytes));
        return request;
    }

    private static void authenticate() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("cloud-user", null, List.of()));
    }
}
