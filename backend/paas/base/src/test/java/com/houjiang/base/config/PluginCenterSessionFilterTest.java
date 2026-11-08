package com.houjiang.base.config;

import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.dto.userms.UserInfoDTO;
import com.houjiang.commons.util.SiteInstanceResolver;
import com.houjiang.commons.vo.user.UserInfoVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PluginCenterSessionFilterTest {
    private final RedisTemplate<String, Object> redis = mock(RedisTemplate.class);
    private final ValueOperations<String, Object> values = mock(ValueOperations.class);
    @AfterEach void clear() { SecurityContextHolder.clearContext(); }

    @Test void missingSessionCannotReachPurchaseOrInstall() throws Exception {
        var response = new MockHttpServletResponse();
        var reached = new AtomicBoolean();
        new PluginCenterSessionFilter(redis).doFilter(request("/base/plugin-center/market/order/create"), response,
                (req, res) -> reached.set(true));
        assertEquals(401, response.getStatus());
        assertFalse(reached.get());
        verifyNoInteractions(redis);
    }

    @Test void resolvesOrganizationAndPermissionsFromPersistedSessionOnly() throws Exception {
        when(redis.opsForValue()).thenReturn(values);
        var user = new UserInfoVO(); user.setUserId("7"); user.setOrgId("org-a");
        var session = new UserInfoDTO(); session.setUser(user); session.setPermissions(List.of("shell:plugin:manage"));
        when(values.get(HouConfig.HOU_USER_TOKEN_USER + "session-token")).thenReturn(session);
        var request = request("/base/plugin-center/market/list");
        request.addHeader("Authorization", "Bearer session-token");
        request.addHeader("X-Instance-Id", "org-victim");
        new PluginCenterSessionFilter(redis).doFilter(request, new MockHttpServletResponse(), (req, res) -> {
            assertEquals("org-a", SiteInstanceResolver.resolveOrNull());
            assertTrue(SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("shell:plugin:manage")));
        });
    }

    @Test void legacyAdminKeepsExistingPurchaseIdentity() throws Exception {
        when(redis.opsForValue()).thenReturn(values);
        when(values.get("MALL_ADMIN_TOKEN:admin-token")).thenReturn(Map.of("id", 9, "root", 1));
        var request = request("/base/plugin-center/market/list"); request.addHeader("Authorization", "admin-token");
        new PluginCenterSessionFilter(redis).doFilter(request, new MockHttpServletResponse(), (req, res) ->
                assertEquals("mall-9", SiteInstanceResolver.resolveOrNull()));
    }

    @Test void onlyExactRawAndPaymentCallbackAreSessionExempt() throws Exception {
        for (String path : List.of("/base/plugin-center/artifact/raw-extra", "/base/plugin-center/market/pay/sandbox/confirm")) {
            var response = new MockHttpServletResponse();
            new PluginCenterSessionFilter(redis).doFilter(request(path), response, (req, res) -> fail("must authenticate"));
            assertEquals(401, response.getStatus());
        }
        var raw = new MockHttpServletRequest("GET", "/base/plugin-center/artifact/raw");
        var reached = new AtomicBoolean();
        new PluginCenterSessionFilter(redis).doFilter(raw, new MockHttpServletResponse(), (req,res) -> reached.set(true));
        assertTrue(reached.get()); // Token validation remains in the raw controller.
    }
    private MockHttpServletRequest request(String path) { return new MockHttpServletRequest("POST", path); }
}
