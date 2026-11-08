package com.houjiang.commons.util;

import com.houjiang.commons.vo.user.UserInfoVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SecurityContextUtilsPermissionTest {

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void readsExactPermissionsFromLoginUser() {
        UserInfoVO user = new UserInfoVO();
        LoginUserPrincipal principal = new LoginUserPrincipal(user, List.of("shell:organization:view"));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null));

        assertTrue(SecurityContextUtils.hasPermission("shell:organization:view"));
        assertFalse(SecurityContextUtils.hasPermission("shell:organization:edit"));
        assertFalse(SecurityContextUtils.hasPermission("shell:organization:view:extra"));
    }

    @Test
    void fallsBackToUserInfoPermissionFieldsAndDeniesWhenEmpty() {
        UserInfoVO user = new UserInfoVO();
        user.setRightsLimitsAll("shell:organization, shell:organization:add");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null));

        assertTrue(SecurityContextUtils.hasPermission("shell:organization"));
        assertTrue(SecurityContextUtils.hasPermission("shell:organization:add"));
        assertFalse(SecurityContextUtils.hasPermission("shell:organization:delete"));

        SecurityContextHolder.clearContext();
        assertFalse(SecurityContextUtils.hasPermission("shell:organization"));
    }

    public static final class LoginUserPrincipal {
        private final UserInfoVO user;
        private final List<String> permissions;

        public LoginUserPrincipal(UserInfoVO user, List<String> permissions) {
            this.user = user;
            this.permissions = permissions;
        }

        public UserInfoVO getUser() {
            return user;
        }

        public List<String> getPermissions() {
            return permissions;
        }
    }
}
