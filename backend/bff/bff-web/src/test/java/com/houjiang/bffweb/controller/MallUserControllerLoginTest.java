package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.controller.mall.MallUserController;
import com.houjiang.bffweb.feign.AuthServiceFeign;
import com.houjiang.bffweb.util.ResponseResult;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

class MallUserControllerLoginTest {

    @Test
    void routesPasswordLoginToAuthService() throws Exception {
        AuthServiceFeign auth = mock(AuthServiceFeign.class);
        MallUserController controller = controllerWith(auth);
        Map<String, Object> body = new HashMap<>(Map.of("userMobile", "13800000000", "password", "encrypted"));
        ResponseResult expected = ResponseResult.success(Map.of("token", "ok"));
        when(auth.login(anyMap())).thenReturn(expected);

        assertSame(expected, controller.login(body));
        verify(auth).login(argThat(request -> "1".equals(String.valueOf(request.get("loginPlatform")))));
        verify(auth, never()).codelogin(anyMap());
    }

    @Test
    void routesPhoneCodeLoginToAuthService() throws Exception {
        AuthServiceFeign auth = mock(AuthServiceFeign.class);
        MallUserController controller = controllerWith(auth);
        Map<String, Object> body = new HashMap<>(Map.of("userMobile", "13800000000", "phoneCode", "1234"));
        ResponseResult expected = ResponseResult.success(Map.of("token", "ok"));
        when(auth.codelogin(anyMap())).thenReturn(expected);

        assertSame(expected, controller.login(body));
        verify(auth).codelogin(argThat(request -> "1".equals(String.valueOf(request.get("loginPlatform")))));
        verify(auth, never()).login(anyMap());
    }

    @Test
    void normalizesCodeAliasBeforeRoutingToPhoneCodeLogin() throws Exception {
        AuthServiceFeign auth = mock(AuthServiceFeign.class);
        MallUserController controller = controllerWith(auth);
        Map<String, Object> body = new HashMap<>(Map.of("phone", "13800000000", "code", "1234"));
        ResponseResult expected = ResponseResult.success(Map.of("token", "ok"));
        when(auth.codelogin(anyMap())).thenReturn(expected);

        assertSame(expected, controller.login(body));
        verify(auth).codelogin(argThat(request ->
                "1234".equals(String.valueOf(request.get("phoneCode")))
                        && "13800000000".equals(String.valueOf(request.get("userMobile")))
                        && "1".equals(String.valueOf(request.get("loginPlatform")))));
        verify(auth, never()).login(anyMap());
    }

    @Test
    void ignoresConflictingLegacyPlatformOnPasswordLogin() throws Exception {
        AuthServiceFeign auth = mock(AuthServiceFeign.class);
        MallUserController controller = controllerWith(auth);
        Map<String, Object> body = new HashMap<>(Map.of(
                "userMobile", "13800000000", "password", "encrypted", "platForm", "3"));
        ResponseResult expected = ResponseResult.success(Map.of("token", "ok"));
        when(auth.login(anyMap())).thenReturn(expected);

        assertSame(expected, controller.login(body));
        verify(auth).login(argThat(request ->
                !request.containsKey("platForm")
                        && "1".equals(String.valueOf(request.get("loginPlatform")))));
    }

    private MallUserController controllerWith(AuthServiceFeign auth) throws Exception {
        MallUserController controller = new MallUserController();
        Field field = MallUserController.class.getDeclaredField("authServiceFeign");
        field.setAccessible(true);
        field.set(controller, auth);
        return controller;
    }
}
