package com.houjiang.auth.service.impl;

import com.houjiang.commons.vo.user.UserInfoVO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LoginServiceImplPlatformTest {

    @Test
    void keepsExplicitPcLoginPlatformWhenPlatFormIsAbsent() {
        UserInfoVO user = new UserInfoVO();
        user.setLoginPlatform(1);

        LoginServiceImpl.resolveLoginPlatform(user);

        assertEquals(1, user.getLoginPlatform());
    }

    @Test
    void defaultsMissingLoginPlatformToBusinessLogin() {
        UserInfoVO user = new UserInfoVO();

        LoginServiceImpl.resolveLoginPlatform(user);

        assertEquals(3, user.getLoginPlatform());
    }
}
