package com.houjiang.userms.ucenter;

import com.houjiang.commons.vo.user.UserInfoVO;
import com.houjiang.userms.context.LoginContext;
import com.houjiang.userms.ucenter.service.UcenterIdentityService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UcenterIdentityServiceTest {
    private final UcenterIdentityService service = new UcenterIdentityService();

    @AfterEach
    void clearContexts() {
        SecurityContextHolder.clearContext();
        LoginContext.remove();
    }

    @Test
    void usesAuthenticatedSecurityPrincipalInsteadOfStaleLegacyThreadLocal() {
        UserInfoVO authenticated = user("42", "林野", "13800138000");
        UserInfoVO stale = user("77", "旧线程用户", "13900139000");
        LoginContext.binding(new LoginContext(stale));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(authenticated, null));

        UcenterIdentityService.CurrentUser current = service.requireCurrentUser();

        assertEquals(42L, current.userId());
        assertEquals("林野", current.displayName());
        assertEquals("8000", current.maskedMobile());
    }

    private static UserInfoVO user(String id, String name, String mobile) {
        UserInfoVO user = new UserInfoVO();
        user.setUserId(id);
        user.setUserNickname(name);
        user.setUserMobile(mobile);
        return user;
    }
}
