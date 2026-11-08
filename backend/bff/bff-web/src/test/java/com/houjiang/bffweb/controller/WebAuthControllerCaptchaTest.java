package com.houjiang.bffweb.controller;

import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WebAuthControllerCaptchaTest {

    private com.houjiang.bffweb.feign.AuthServiceFeign auth;
    private org.springframework.test.web.servlet.MockMvc mvc;

    @org.junit.jupiter.api.BeforeEach
    void setup() {
        auth = mock(com.houjiang.bffweb.feign.AuthServiceFeign.class);
        WebAuthController controller = new WebAuthController();
        org.springframework.test.util.ReflectionTestUtils.setField(controller, "authServiceFeign", auth);
        mvc = org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup(controller).build();
        when(auth.getLoginQrCode(anyString(), nullable(String.class)))
                .thenReturn(com.houjiang.bffweb.util.ResponseResult.success());
        when(auth.getWechatQRCode(nullable(String.class)))
                .thenReturn(com.houjiang.bffweb.util.ResponseResult.success());
    }

    @Test
    void applicationIsForwardedForGetAndPostWithoutClientPolicy() throws Exception {
        mvc.perform(get("/web/auth/unilogin/wechat/qrcode").param("application", "ocr")
                .param("allowCreate", "true").param("registerTerminal", "mall")).andExpect(status().isOk());
        mvc.perform(post("/web/auth/unilogin/wechat/qrcode").param("application", "ocr"))
                .andExpect(status().isOk());
        verify(auth, times(2)).getLoginQrCode("wechat", "ocr");
    }

    @Test
    void omittedApplicationRemainsCompatible() throws Exception {
        mvc.perform(get("/web/auth/unilogin/wechat/qrcode")).andExpect(status().isOk());
        verify(auth).getLoginQrCode("wechat", null);
        mvc.perform(get("/web/auth/wechat/qrcode")).andExpect(status().isOk());
        verify(auth).getWechatQRCode(null);
    }

    @Test
    void legacyWechatRouteForwardsApplication() throws Exception {
        mvc.perform(post("/web/auth/wechat/qrcode").param("application", "ocr")).andExpect(status().isOk());
        verify(auth).getWechatQRCode("ocr");
    }

    @Test
    void feignApplicationIsOptionalQueryParameter() throws Exception {
        org.springframework.web.bind.annotation.RequestParam parameter =
                com.houjiang.bffweb.feign.AuthServiceFeign.class.getMethod("getLoginQrCode", String.class, String.class)
                        .getParameters()[1].getAnnotation(org.springframework.web.bind.annotation.RequestParam.class);
        assertEquals("application", parameter.value());
        assertFalse(parameter.required());
    }

    @Test
    void internalIdentityEndpointIsNotExposed() throws Exception {
        mvc.perform(post("/web/auth/feign/oauth/get-or-create")).andExpect(status().isNotFound());
        mvc.perform(post("/web/auth/oauth/get-or-create")).andExpect(status().isNotFound());
    }

    @Test
    void generateCaptchaAcceptsBrowserGetAndApiPost() throws NoSuchMethodException {
        Method method = WebAuthController.class.getDeclaredMethod("generateSlideCaptcha");
        RequestMapping mapping = method.getAnnotation(RequestMapping.class);

        assertTrue(Arrays.asList(mapping.method()).contains(RequestMethod.GET));
        assertTrue(Arrays.asList(mapping.method()).contains(RequestMethod.POST));
    }

    @Test
    void wechatQrCodeAcceptsBrowserGetAndApiPost() throws NoSuchMethodException {
        Method method = WebAuthController.class.getDeclaredMethod("getWechatQRCode", String.class);
        RequestMapping mapping = method.getAnnotation(RequestMapping.class);

        assertTrue(Arrays.asList(mapping.method()).contains(RequestMethod.GET));
        assertTrue(Arrays.asList(mapping.method()).contains(RequestMethod.POST));
    }
}
