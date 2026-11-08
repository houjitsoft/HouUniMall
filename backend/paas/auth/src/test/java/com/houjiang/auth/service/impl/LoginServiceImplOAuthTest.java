package com.houjiang.auth.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.houjiang.auth.feign.UserServiceClient;
import com.houjiang.auth.login.channel.*;
import com.houjiang.commons.config.RedisCache;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.vo.user.UserInfoVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LoginServiceImplOAuthTest {
    private LoginServiceImpl service;
    private ConfigurableOAuthChannelProvider provider;
    private UserServiceClient users;
    private MockEnvironment env;
    private LoginChannelDef definition;
    private final Map<String, Object> cache = new ConcurrentHashMap<>();
    private static final String KEY = "unilogin_qrcode_wechat_";

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setup() {
        service = new LoginServiceImpl();
        provider = mock(ConfigurableOAuthChannelProvider.class);
        users = mock(UserServiceClient.class);
        definition = new LoginChannelDef();
        definition.setAppId("verified-app");
        definition.setAutoRegister(true);
        when(provider.definition()).thenReturn(definition);
        when(provider.code()).thenReturn("wechat");
        when(provider.displayName()).thenReturn("微信");
        when(provider.isConfigured()).thenReturn(true);
        when(provider.buildAuthorizeUrl(anyString())).thenAnswer(i -> "authorize:" + i.getArgument(0));
        LoginIdentity identity = new LoginIdentity();
        identity.setProviderUserId("verified-openid");
        identity.setUnionId("verified-unionid");
        identity.setNickname("测试昵称");
        identity.setAvatar("avatar");
        when(provider.exchange(anyString())).thenReturn(identity);
        LoginChannelRegistry registry = mock(LoginChannelRegistry.class);
        when(registry.get("wechat")).thenReturn(provider);
        RedisCache redis = mock(RedisCache.class);
        RedisTemplate template = mock(RedisTemplate.class);
        ValueOperations ops = mock(ValueOperations.class);
        redis.redisTemplate = template;
        when(template.opsForValue()).thenReturn(ops);
        when(ops.setIfAbsent(anyString(), any(), anyLong(), any(TimeUnit.class)))
                .thenAnswer(i -> cache.putIfAbsent(i.getArgument(0), i.getArgument(1)) == null);
        when(redis.getCacheObject(anyString())).thenAnswer(i -> cache.get(i.getArgument(0)));
        doAnswer(i -> { cache.put(i.getArgument(0), i.getArgument(1)); return null; })
                .when(redis).setCacheObject(anyString(), any(), anyInt(), any(TimeUnit.class));
        env = new MockEnvironment().withProperty("unilogin.internal-token", "test-internal-secret")
                .withProperty("unilogin.channels.wechat.auto-register", "true")
                .withProperty("unilogin.applications.ocr.auto-register", "true")
                .withProperty("unilogin.applications.ocr.register-terminal", "houocr")
                .withProperty("unilogin.applications.ocr.allowed-channels", "wechat,wecom");
        ReflectionTestUtils.setField(service, "environment", env);
        ReflectionTestUtils.setField(service, "redisCache", redis);
        ReflectionTestUtils.setField(service, "loginChannelRegistry", registry);
        ReflectionTestUtils.setField(service, "userServiceClient", users);
        UserInfoVO user = new UserInfoVO();
        user.setUserId("member-1");
        when(users.getUserInfoByUserId("member-1")).thenReturn(user);
        when(users.getOrCreateOAuthMember(anyString(), anyMap()))
                .thenReturn(ResponseResult.success(Map.of("userId", "member-1", "created", true)));
    }

    private JSONObject data(ResponseResult result) {
        return JSON.parseObject(JSON.toJSONString(result.getData()));
    }

    private String qr(String application) {
        ResponseResult result = service.getLoginQrCode("wechat", application);
        assertEquals(1, result.getCode());
        return data(result).getString("qrCodeId");
    }

    @Test
    void ocrPolicyIsFrozenAndIdentityComesOnlyFromExchange() throws Exception {
        String state = qr("ocr");
        env.setProperty("unilogin.applications.ocr.auto-register", "false");
        env.setProperty("unilogin.applications.ocr.register-terminal", "mall");
        assertEquals("confirmed", data(service.loginCallback("wechat", "provider-code", state)).getString("status"));
        verify(users).getOrCreateOAuthMember(eq("test-internal-secret"), argThat(body ->
                body.equals(Map.of("channel", "wechat", "providerAppId", "verified-app",
                        "openid", "verified-openid", "unionid", "verified-unionid", "nickname", "测试昵称",
                        "avatar", "avatar", "registerTerminal", "houocr", "allowCreate", "true"))));
        JSONObject stored = JSON.parseObject((String) cache.get(KEY + state));
        assertEquals("ocr", stored.getString("application"));
        assertEquals("houocr", stored.getString("registerTerminal"));
        assertTrue(stored.getBooleanValue("completed"));
        assertFalse(stored.containsKey("internalToken"));
        verify(users, never()).getUserInfoByMobile(anyString());
        verify(users, never()).thirDregister(anyMap());
        verify(users, never()).selectUserIdByOpenid(anyString());
    }

    @Test
    void defaultBackendNeverUsesGlobalAutoRegister() {
        String state = qr(null);
        service.loginCallback("wechat", "code", state);
        verify(users).getOrCreateOAuthMember(anyString(), argThat(body ->
                "false".equals(body.get("allowCreate")) && "houcloud".equals(body.get("registerTerminal"))));
    }

    @Test
    void rejectsUnknownApplicationsAndDisallowedChannels() {
        assertNotEquals(1, service.getLoginQrCode("wechat", "unknown").getCode());
        env.setProperty("unilogin.applications.ocr.allowed-channels", "qq");
        assertNotEquals(1, service.getLoginQrCode("wechat", "ocr").getCode());
        assertTrue(cache.isEmpty());
        verifyNoInteractions(users);
    }

    @Test
    void indexedAllowedChannelsAreSupported() {
        env = new MockEnvironment();
        ReflectionTestUtils.setField(service, "environment", env);
        env.setProperty("unilogin.applications.ocr.allowed-channels[0]", "wechat");
        env.setProperty("unilogin.applications.ocr.register-terminal", "houocr");
        assertNotNull(qr("ocr"));
    }

    @Test
    void exchangeFailureCannotRegister() {
        String state = qr("ocr");
        when(provider.exchange(anyString())).thenThrow(new LoginChannelException("换码失败"));
        assertNotEquals(1, service.loginCallback("wechat", "bad-code", state).getCode());
        verifyNoInteractions(users);
        assertEquals("ocr", JSON.parseObject((String) cache.get(KEY + state)).getString("application"));
    }

    @Test
    void emptyIdentityCannotRegister() {
        String state = qr("ocr");
        when(provider.exchange(anyString())).thenReturn(new LoginIdentity());
        assertNotEquals(1, service.loginCallback("wechat", "code", state).getCode());
        verifyNoInteractions(users);
    }

    @Test
    void missingStateCannotExchangeOrRegister() {
        assertEquals("expired", data(service.loginCallback("wechat", "code", "missing")).getString("status"));
        verify(provider, never()).exchange(anyString());
        verifyNoInteractions(users);
    }

    @Test
    void completedCallbackAndPollingAreIdempotent() {
        String state = qr("ocr");
        JSONObject first = data(service.loginCallback("wechat", "code", state));
        JSONObject second = data(service.loginCallback("wechat", "code", state));
        assertEquals("confirmed", first.getString("status"));
        assertEquals(first, second);
        assertEquals(first, data(service.checkLoginStatus("wechat", state)));
        assertEquals(first, data(service.checkLoginStatus("wechat", state)));
        verify(provider, times(1)).exchange(anyString());
        verify(users, times(1)).getOrCreateOAuthMember(anyString(), anyMap());
    }

    @Test
    void concurrentCallbackDoesNotExchangeTwice() throws Exception {
        String state = qr("ocr");
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        LoginIdentity identity = new LoginIdentity();
        identity.setProviderUserId("verified-openid");
        when(provider.exchange(anyString())).thenAnswer(i -> {
            entered.countDown();
            assertTrue(release.await(5, TimeUnit.SECONDS));
            return identity;
        });
        CompletableFuture<ResponseResult> first = CompletableFuture.supplyAsync(() -> service.loginCallback("wechat", "code", state));
        try {
            assertTrue(entered.await(5, TimeUnit.SECONDS));
            assertEquals("processing", data(service.loginCallback("wechat", "code", state)).getString("status"));
        } finally {
            release.countDown();
        }
        assertEquals("confirmed", data(first.get(5, TimeUnit.SECONDS)).getString("status"));
        verify(provider, times(1)).exchange(anyString());
        verify(users, times(1)).getOrCreateOAuthMember(anyString(), anyMap());
    }

    @Test
    void providerAppChangeFailsClosed() {
        String state = qr("ocr");
        definition.setAppId("another-app");
        assertNotEquals(1, service.loginCallback("wechat", "code", state).getCode());
        verifyNoInteractions(users);
    }

    @Test
    void missingInternalTokenFailsClosed() {
        String state = qr("ocr");
        env.setProperty("unilogin.internal-token", "");
        assertNotEquals(1, service.loginCallback("wechat", "code", state).getCode());
        verifyNoInteractions(users);
    }

    @Test
    void existingIdentityIsAcceptedWithoutCreatingAgain() {
        String state = qr("ocr");
        when(users.getOrCreateOAuthMember(anyString(), anyMap())).thenReturn(
                ResponseResult.success(Map.of("userId", "member-1", "created", false)));
        assertEquals("confirmed", data(service.loginCallback("wechat", "code", state)).getString("status"));
    }

    @Test
    void internalFeignUsesPostBodyAndDedicatedHeader() {
        feign.MethodMetadata metadata = new org.springframework.cloud.openfeign.support.SpringMvcContract()
                .parseAndValidateMetadata(UserServiceClient.class).stream()
                .filter(method -> method.configKey().contains("getOrCreateOAuthMember"))
                .findFirst().orElseThrow();
        assertEquals("POST", metadata.template().method());
        assertEquals("/user/ms/feign/oauth/get-or-create", metadata.template().url());
        assertTrue(metadata.template().headers().containsKey("X-Internal-OAuth-Token"));
        assertEquals(1, metadata.bodyIndex());
    }

    @Test
    void authControllerApplicationIsOptional() throws Exception {
        var parameter = com.houjiang.auth.controller.LoginController.class
                .getMethod("getLoginQrCode", String.class, String.class).getParameters()[1]
                .getAnnotation(org.springframework.web.bind.annotation.RequestParam.class);
        assertEquals("application", parameter.value());
        assertFalse(parameter.required());
    }

    @Test
    void unboundIsNotCreatedWhenPolicyDisallowsIt() {
        String state = qr(null);
        when(users.getOrCreateOAuthMember(anyString(), anyMap())).thenReturn(
                ResponseResult.fail(409, "OAuth 身份尚未绑定会员"));
        assertEquals("unbound", data(service.loginCallback("wechat", "code", state)).getString("status"));
        assertEquals("", JSON.parseObject((String) cache.get(KEY + state)).getString("application"));
        verify(users, never()).getUserInfoByUserId(anyString());
    }
}
