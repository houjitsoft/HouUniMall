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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import jakarta.servlet.FilterChain;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JwtAuthenticationTokenFilterPermissionsTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldRestoreCachedPermissionsAsAuthorities() throws Exception {
        Authentication authentication = authenticate(List.of(
                "shell:setting:permission",
                "shell:setting:permission:menu"));

        assertNotNull(authentication);
        assertEquals(List.of(
                        "shell:setting:permission",
                        "shell:setting:permission:menu"),
                authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList());
    }

    @Test
    void shouldRestoreEmptyPermissionListAsNonNullAuthorities() throws Exception {
        Authentication authentication = authenticate(List.of());

        assertNotNull(authentication);
        assertNotNull(authentication.getAuthorities());
        assertEquals(0, authentication.getAuthorities().size());
    }

    private Authentication authenticate(List<String> permissions) throws Exception {
        String userId = "790708000000000001";
        String token = JwtUtil.createJWT(userId);
        UserInfoVO user = new UserInfoVO();
        user.setUserId(userId);
        user.setUserName("permission-user");
        UserInfoDTO cachedLogin = new UserInfoDTO();
        cachedLogin.setUser(user);
        cachedLogin.setPermissions(permissions);

        RedisCache redisCache = mock(RedisCache.class);
        when(redisCache.getCacheObject(HouConfig.HOU_USER_TOKEN_USER + token))
                .thenReturn(JSON.toJSONString(cachedLogin));
        when(redisCache.expire(anyString(), org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.eq(TimeUnit.SECONDS))).thenReturn(true);
        HandlerExceptionResolver resolver = mock(HandlerExceptionResolver.class);
        OncePerRequestFilter filter = new JwtAuthenticationTokenFilter<>(TestLoginUser.class, "/public/**");
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

    public static final class TestLoginUser implements UserDetails {
        private final UserInfoVO user;
        private final List<String> permissions;

        public TestLoginUser(UserInfoVO user, List<String> permissions) {
            this.user = user;
            this.permissions = permissions == null ? List.of() : List.copyOf(permissions);
        }

        public UserInfoVO getUser() {
            return user;
        }

        public List<String> getPermissions() {
            return permissions;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return permissions.stream().map(SimpleGrantedAuthority::new).toList();
        }

        @Override
        public String getPassword() {
            return "";
        }

        @Override
        public String getUsername() {
            return user.getUserName();
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    }
}
