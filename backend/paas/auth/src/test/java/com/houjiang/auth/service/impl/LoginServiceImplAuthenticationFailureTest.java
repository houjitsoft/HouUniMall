package com.houjiang.auth.service.impl;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.user.UserInfoVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginServiceImplAuthenticationFailureTest {

    @Mock
    private AuthenticationManager authenticationManager;

    private LoginServiceImpl loginService;

    @BeforeEach
    void setUp() {
        loginService = new LoginServiceImpl();
        ReflectionTestUtils.setField(loginService, "authenticationManager", authenticationManager);
    }

    @Test
    void mapsBadCredentialsToInvalidCredentials() throws Exception {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        ResponseResult result = loginService.login(loginRequest(), request(), response());

        assertEquals(30005, result.getCode());
    }

    @Test
    void doesNotReportUnexpectedAuthenticationFailureAsWrongPassword() throws Exception {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new IllegalStateException("permission store unavailable"));

        ResponseResult result = loginService.login(loginRequest(), request(), response());

        assertEquals(11009, result.getCode());
        assertEquals("登录认证服务暂时不可用，请稍后重试", result.getMessage());
    }

    private UserInfoVO loginRequest() {
        UserInfoVO user = new UserInfoVO();
        user.setUserName("login-test");
        user.setUserMobile("13800138000");
        user.setPassword("not-a-real-password");
        user.setPlatForm("2");
        return user;
    }

    private HttpServletRequest request() {
        return new MockHttpServletRequest();
    }

    private HttpServletResponse response() {
        return new MockHttpServletResponse();
    }
}
