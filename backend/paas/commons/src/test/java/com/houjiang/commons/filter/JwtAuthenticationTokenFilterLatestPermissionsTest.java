package com.houjiang.commons.filter;

import com.alibaba.fastjson.JSON;
import com.houjiang.commons.config.HouConfig;
import com.houjiang.commons.config.RedisCache;
import com.houjiang.commons.dto.userms.UserInfoDTO;
import com.houjiang.commons.util.JwtUtil;
import com.houjiang.commons.vo.user.UserInfoVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import jakarta.servlet.FilterChain;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 会话权限快照与权限服务最新快照的叠加行为。
 *
 * <p>背景：登录时权限被固化进 {@code hou-user-token-user_<token>}。插件安装或角色改授权后，
 * 老会话的快照里没有新权限点，插件接口会一直返回「无权访问插件接口」。
 * 过滤器因此在会话权限之外再叠加 {@code user:permissions:<userId>} 的最新快照。
 */
public class JwtAuthenticationTokenFilterLatestPermissionsTest {

    private static final String USER_ID = "790708000000000002";
    private static final String LATEST_KEY = "user:permissions:" + USER_ID;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldMergeLatestPermissionSnapshotIntoSessionAuthorities() throws Exception {
        Authentication authentication = authenticate(
                List.of("shell:setting:permission"),
                JSON.toJSONString(List.of(
                        "shell:setting:permission",
                        "holiday-campaign:review:read",
                        "holiday-campaign:plan:write")));

        List<String> authorities = authoritiesOf(authentication);
        assertEquals(List.of(
                        "shell:setting:permission",
                        "holiday-campaign:review:read",
                        "holiday-campaign:plan:write"),
                authorities);
    }

    @Test
    void shouldKeepSessionAuthoritiesWhenLatestSnapshotMissing() throws Exception {
        Authentication authentication = authenticate(List.of("shell:setting:permission"), null);

        assertEquals(List.of("shell:setting:permission"), authoritiesOf(authentication));
    }

    @Test
    void shouldParseDoubleEncodedPermissionSnapshot() throws Exception {
        // 权限服务写的是 JSON 数组，经 Redis 序列化后可能再被字符串包一层。
        Authentication authentication = authenticate(
                List.of("shell:setting:permission"),
                JSON.toJSONString(JSON.toJSONString(List.of("holiday-campaign:task:read"))));

        List<String> authorities = authoritiesOf(authentication);
        assertTrue(authorities.contains("holiday-campaign:task:read"), authorities.toString());
        assertEquals(2, authorities.size());
    }

    @Test
    void shouldDeduplicatePermissionAppearingInBothSnapshots() throws Exception {
        Authentication authentication = authenticate(
                List.of("holiday-campaign:plan:read"),
                JSON.toJSONString(List.of("holiday-campaign:plan:read", "holiday-campaign:review:read")));

        List<String> authorities = authoritiesOf(authentication);
        assertEquals(2, authorities.size(), authorities.toString());
        assertEquals(1, authorities.stream().filter("holiday-campaign:plan:read"::equals).count());
    }

    private List<String> authoritiesOf(Authentication authentication) {
        assertNotNull(authentication);
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
    }

    private Authentication authenticate(List<String> sessionPermissions, String latestSnapshotJson)
            throws Exception {
        String token = JwtUtil.createJWT(USER_ID);
        UserInfoVO user = new UserInfoVO();
        user.setUserId(USER_ID);
        user.setUserName("latest-permission-user");
        UserInfoDTO cachedLogin = new UserInfoDTO();
        cachedLogin.setUser(user);
        cachedLogin.setPermissions(sessionPermissions);

        RedisCache redisCache = mock(RedisCache.class);
        when(redisCache.getCacheObject(HouConfig.HOU_USER_TOKEN_USER + token))
                .thenReturn(JSON.toJSONString(cachedLogin));
        when(redisCache.getCacheObject(LATEST_KEY)).thenReturn(latestSnapshotJson);
        when(redisCache.expire(anyString(), anyLong(), eq(TimeUnit.SECONDS))).thenReturn(true);
        HandlerExceptionResolver resolver = mock(HandlerExceptionResolver.class);
        OncePerRequestFilter filter =
                new JwtAuthenticationTokenFilter<>(JwtAuthenticationTokenFilterPermissionsTest.TestLoginUser.class,
                        "/public/**");
        ((JwtAuthenticationTokenFilter<?>) filter).setRedisCache(redisCache);
        ((JwtAuthenticationTokenFilter<?>) filter).setResolver(resolver);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/protected");
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        return SecurityContextHolder.getContext().getAuthentication();
    }
}
