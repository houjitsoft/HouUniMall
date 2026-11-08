package com.houjiang.bffweb.controller;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.houjiang.bffweb.feign.MerchantServiceFeign;
import com.houjiang.bffweb.feign.MerchantServiceFeignFallback;
import feign.Request;
import feign.RetryableException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

class WebMerchantControllerTest {

    @Test
    void uploadLogsAuthorizationPresenceWithoutCredentialContent() throws Exception {
        MerchantServiceFeign feign = mock(MerchantServiceFeign.class);
        MockMvc mockMvc = mockMvc(feign);
        MockMultipartFile file = new MockMultipartFile(
                "file", "logo.png", "image/png", new byte[]{1});
        Logger logger = (Logger) LoggerFactory.getLogger(WebMerchantController.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);

        try {
            mockMvc.perform(multipart("/web/merchant/upload/image")
                            .file(file)
                            .header("Authorization", "Bearer super-secret-upload-token"))
                    .andExpect(status().isOk());

            List<String> messages = appender.list.stream()
                    .map(ILoggingEvent::getFormattedMessage)
                    .toList();
            assertTrue(messages.stream().anyMatch(message -> message.contains("authorizationPresent=true")));
            assertFalse(messages.stream().anyMatch(message -> message.contains("super-secret")));
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }
    }

    @Test
    void routesAdditionalExactAdminModulesToAdminapi() throws Exception {
        MerchantServiceFeign feign = mock(MerchantServiceFeign.class);
        MockMvc mockMvc = mockMvc(feign);

        mockMvc.perform(post("/web/merchant/setting.system.cache/clear")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/web/merchant/recharge.recharge/getConfig"))
                .andExpect(status().isOk());

        verify(feign).forwardAdminapiPost("setting.system.cache", "clear", Map.of());
        verify(feign).forwardAdminapiGet("recharge.recharge", "getConfig", Map.of());
    }

    @Test
    void routesUnknownDottedModuleToMerchantByDefault() throws Exception {
        MerchantServiceFeign feign = mock(MerchantServiceFeign.class);
        MockMvc mockMvc = mockMvc(feign);

        mockMvc.perform(get("/web/merchant/custom.extension/detail"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/web/merchant/custom.extension/save")
                        .contentType("application/json")
                        .content("{\"enabled\":true}"))
                .andExpect(status().isOk());

        verify(feign).forwardGet("custom.extension", "detail", Map.of());
        verify(feign).forwardPost("custom.extension", "save", Map.of("enabled", true));
    }

    @Test
    void routesAdvertisingThroughItsExplicitMerchantContract() throws Exception {
        MerchantServiceFeign feign = mock(MerchantServiceFeign.class);
        MockMvc mockMvc = mockMvc(feign);

        mockMvc.perform(post("/web/merchant/advertising/save")
                        .contentType("application/json")
                        .content("{\"shopId\":999,\"application\":{\"title\":\"秋季上新\"}}"))
                .andExpect(status().isOk());

        verify(feign).advertising("save", Map.of(
                "shopId", 999,
                "application", Map.of("title", "秋季上新")));
        org.mockito.Mockito.verify(feign, org.mockito.Mockito.never())
                .forwardPost(eq("advertising"), eq("save"), anyMap());
    }

    @ParameterizedTest
    @CsvSource({
            "gather.gather, add",
            "gather.gatherGoods, edit_goods",
            "printer.printer, add",
            "printer.printerTemplate, add"
    })
    void routesKnownDottedMerchantModulesToMerchantPost(String module, String action) throws Exception {
        MerchantServiceFeign feign = mock(MerchantServiceFeign.class);
        MockMvc mockMvc = mockMvc(feign);

        mockMvc.perform(post("/web/merchant/{module}/{action}", module, action)
                        .contentType("application/json")
                        .content("{\"id\":9}"))
                .andExpect(status().isOk());

        verify(feign).forwardPost(module, action, Map.of("id", 9));
    }

    @ParameterizedTest
    @CsvSource({
            "gather.gather, lists",
            "gather.gatherGoods, detail",
            "printer.printer, lists",
            "printer.printerTemplate, lists",
            "finance.account_log, lists",
            "finance.refund, record"
    })
    void routesKnownDottedMerchantModulesToMerchantGet(String module, String action) throws Exception {
        MerchantServiceFeign feign = mock(MerchantServiceFeign.class);
        MockMvc mockMvc = mockMvc(feign);

        mockMvc.perform(get("/web/merchant/{module}/{action}", module, action))
                .andExpect(status().isOk());

        verify(feign).forwardGet(module, action, Map.of());
    }

    @Test
    void merchantFeignExposesAuthUploadProvisioningAndGenericForwardContracts() {
        Set<String> methodNames = Arrays.stream(MerchantServiceFeign.class.getDeclaredMethods())
                .map(method -> method.getName())
                .collect(Collectors.toSet());

        assertEquals(Set.of(
                "login", "logout", "uploadFile",
                "forwardGet", "forwardPost",
                "forwardAdminapiGet", "forwardAdminapiPost",
                "listProvisionedShops", "createProvisionedShop", "provisionShopOptions",
                "provisionShopAccountOptions", "updateProvisionedShopStatus",
                "getProvisionedShopDetail", "updateProvisionedShop", "advertising"), methodNames);
    }

    @Test
    void exposesOnlyExplicitAuthUploadAndGenericForwardHandlers() {
        Set<String> publicMethods = Arrays.stream(WebMerchantController.class.getDeclaredMethods())
                .filter(method -> java.lang.reflect.Modifier.isPublic(method.getModifiers()))
                .map(method -> method.getName())
                .collect(Collectors.toSet());

        assertEquals(Set.of("login", "logout", "uploadFile", "advertising", "forwardGet", "forwardPost"), publicMethods);
    }

    @Test
    void keepsLogoutExplicitWithAuthorization() throws Exception {
        MerchantServiceFeign feign = mock(MerchantServiceFeign.class);
        MockMvc mockMvc = mockMvc(feign);

        mockMvc.perform(post("/web/merchant/login/out")
                        .header("Authorization", "Bearer logout-token"))
                .andExpect(status().isOk());

        verify(feign).logout("Bearer logout-token");
    }

    @Test
    void keepsLoginExplicit() throws Exception {
        MerchantServiceFeign feign = mock(MerchantServiceFeign.class);
        MockMvc mockMvc = mockMvc(feign);

        mockMvc.perform(post("/web/merchant/login/login")
                        .contentType("application/json")
                        .content("{\"username\":\"admin\",\"password\":\"secret\"}"))
                .andExpect(status().isOk());

        verify(feign).login(Map.of("username", "admin", "password", "secret"));
    }

    @Test
    void sanitizesNestedLoginFailureAndLogsThrowableWithoutCredentials() throws Exception {
        MerchantServiceFeign feign = mock(MerchantServiceFeign.class);
        RuntimeException root = new RuntimeException(
                "SELECT password FROM users at Mapper.java:41; http://10.0.0.8:8848/nacos root secret");
        RuntimeException exception = new RuntimeException("Feign internal failure", root);
        when(feign.login(anyMap())).thenThrow(exception);
        MockMvc mockMvc = mockMvc(feign);
        Logger logger = (Logger) LoggerFactory.getLogger(WebMerchantController.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);

        try {
            String response = mockMvc.perform(post("/web/merchant/login/login")
                            .contentType("application/json")
                            .content("{\"username\":\"private-admin\",\"password\":\"login-secret\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.show").value(1))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data").isEmpty())
                    .andExpect(jsonPath("$.msg").value("登录失败，请稍后重试"))
                    .andReturn().getResponse().getContentAsString();

            for (String sensitive : List.of(
                    "RuntimeException", "Feign", "SELECT", "Mapper", "password",
                    "10.0.0.8", "8848", "Nacos", "root", "secret")) {
                assertFalse(response.contains(sensitive));
            }
            ILoggingEvent event = appender.list.get(appender.list.size() - 1);
            assertTrue(event.getFormattedMessage().startsWith("商家登录调用失败\n"));
            PatternLayout layout = new PatternLayout();
            layout.setContext(logger.getLoggerContext());
            layout.setPattern("%msg%n%ex");
            layout.start();
            String renderedLog = layout.doLayout(event);
            assertTrue(renderedLog.contains(RuntimeException.class.getName()));
            assertTrue(renderedLog.contains("WebMerchantControllerTest.sanitizesNestedLoginFailure"));
            for (String sensitive : List.of(
                    "Feign internal failure", "SELECT password", "Mapper.java:41",
                    "10.0.0.8", "8848", "Nacos", "root secret",
                    "private-admin", "login-secret")) {
                assertFalse(renderedLog.contains(sensitive), renderedLog);
            }
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }
    }

    @Test
    void returnsStableTimeoutMessageWithoutRootDetails() throws Exception {
        MerchantServiceFeign feign = mock(MerchantServiceFeign.class);
        when(feign.login(anyMap())).thenThrow(
                new RuntimeException("Feign call failed", new SocketTimeoutException(
                        "Read timed out from merchant.internal:8080 with root secret")));

        mockMvc(feign).perform(post("/web/merchant/login/login")
                        .contentType("application/json")
                        .content("{\"username\":\"admin\",\"password\":\"secret\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.show").value(1))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.msg").value("商户服务请求超时，请稍后重试"));
    }

    @Test
    void classifiesRetryableHttp503AsUnavailableInsteadOfTimeout() throws Exception {
        MerchantServiceFeign feign = mock(MerchantServiceFeign.class);
        Request request = Request.create(
                Request.HttpMethod.POST,
                "http://merchant.internal/login",
                Map.of(),
                null,
                StandardCharsets.UTF_8,
                null);
        when(feign.login(anyMap())).thenThrow(new RetryableException(
                503,
                "upstream internal address and secret",
                Request.HttpMethod.POST,
                (Long) null,
                request));

        mockMvc(feign).perform(post("/web/merchant/login/login")
                        .contentType("application/json")
                        .content("{\"username\":\"admin\",\"password\":\"secret\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("商户服务暂时不可用，请稍后重试"));
    }

    @Test
    void cyclicCauseChainTerminatesAndReturnsStableFailure() {
        MerchantServiceFeign feign = mock(MerchantServiceFeign.class);
        CyclicFailure first = new CyclicFailure();
        CyclicFailure second = new CyclicFailure();
        first.next = second;
        second.next = first;
        when(feign.login(anyMap())).thenThrow(first);

        assertTimeoutPreemptively(Duration.ofSeconds(1), () ->
                mockMvc(feign).perform(post("/web/merchant/login/login")
                                .contentType("application/json")
                                .content("{\"username\":\"admin\",\"password\":\"secret\"}"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.code").value(0))
                        .andExpect(jsonPath("$.msg").value("登录失败，请稍后重试")));
    }

    @Test
    void returnsStableUnavailableMessageWithoutDiscoveryDetails() throws Exception {
        MerchantServiceFeign feign = mock(MerchantServiceFeign.class);
        when(feign.login(anyMap())).thenThrow(new RuntimeException(
                "No instances available for merchant via Nacos at 10.0.0.8:8848"));

        String response = mockMvc(feign).perform(post("/web/merchant/login/login")
                        .contentType("application/json")
                        .content("{\"username\":\"admin\",\"password\":\"secret\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.show").value(1))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.msg").value("商户服务暂时不可用，请稍后重试"))
                .andReturn().getResponse().getContentAsString();

        assertFalse(response.contains("Nacos"));
        assertFalse(response.contains("10.0.0.8"));
        assertFalse(response.contains("No instances"));
    }

    @Test
    void fallbackMethodsReturnTheSameSanitizedEnvelope() {
        MerchantServiceFeignFallback fallback = new MerchantServiceFeignFallback();
        Map<String, Object> expected = Map.of(
                "code", 0,
                "msg", "商户服务暂时不可用，请稍后重试",
                "data", List.of(),
                "show", 1);

        assertEquals(expected, fallback.login(Map.of("password", "secret")));
        assertEquals(expected, fallback.logout("Bearer secret"));
        assertEquals(expected, fallback.forwardPost("mall.goods", "lists", Map.of()));
        assertEquals(expected, fallback.forwardGet("mall.goods", "lists", Map.of()));
        assertEquals(expected, fallback.forwardAdminapiPost("auth.menu", "lists", Map.of()));
        assertEquals(expected, fallback.forwardAdminapiGet("auth.menu", "lists", Map.of()));
        assertEquals(expected, fallback.uploadFile("image", null, "0", "Bearer secret"));
    }

    @Test
    void routesJsonUploadPathThroughGenericPostInsteadOfMultipartHandler() throws Exception {
        MerchantServiceFeign feign = mock(MerchantServiceFeign.class);
        MockMvc mockMvc = mockMvc(feign);

        mockMvc.perform(post("/web/merchant/upload/image")
                        .contentType("application/json")
                        .content("{\"url\":\"/logo.png\"}"))
                .andExpect(status().isOk());

        verify(feign).forwardPost("upload", "image", Map.of("url", "/logo.png"));
    }

    @Test
    void keepsMultipartUploadExplicitWithDefaultCidAndAuthorization() throws Exception {
        MerchantServiceFeign feign = mock(MerchantServiceFeign.class);
        MockMvc mockMvc = mockMvc(feign);
        MockMultipartFile file = new MockMultipartFile(
                "file", "logo.png", "image/png", new byte[]{1, 2, 3});

        mockMvc.perform(multipart("/web/merchant/upload/image")
                        .file(file)
                        .header("Authorization", "Bearer upload-token"))
                .andExpect(status().isOk());

        verify(feign).uploadFile(eq("image"), same(file), eq("0"), eq("Bearer upload-token"));
    }

    @Test
    void forwardsMenuListThroughGenericMerchantGet() throws Exception {
        MerchantServiceFeign feign = mock(MerchantServiceFeign.class);
        MockMvc mockMvc = mockMvc(feign);

        mockMvc.perform(get("/web/merchant/index/menu_list"))
                .andExpect(status().isOk());

        verify(feign).forwardGet("index", "menu_list", Map.of());
    }

    @Test
    void forwardsMySelfThroughGenericMerchantGet() throws Exception {
        MerchantServiceFeign feign = mock(MerchantServiceFeign.class);
        MockMvc mockMvc = mockMvc(feign);

        mockMvc.perform(get("/web/merchant/index/mySelf"))
                .andExpect(status().isOk());

        verify(feign).forwardGet("index", "mySelf", Map.of());
    }

    @Test
    void routesDottedAdminModulePostToAdminapi() throws Exception {
        MerchantServiceFeign feign = mock(MerchantServiceFeign.class);
        MockMvc mockMvc = mockMvc(feign);

        mockMvc.perform(post("/web/merchant/decorate.page/save")
                        .contentType("application/json")
                        .content("{\"id\":3}"))
                .andExpect(status().isOk());

        verify(feign).forwardAdminapiPost("decorate.page", "save", Map.of("id", 3));
    }

    @Test
    void routesDottedAdminModuleGetToAdminapi() throws Exception {
        MerchantServiceFeign feign = mock(MerchantServiceFeign.class);
        MockMvc mockMvc = mockMvc(feign);

        mockMvc.perform(get("/web/merchant/auth.menu/lists"))
                .andExpect(status().isOk());

        verify(feign).forwardAdminapiGet("auth.menu", "lists", Map.of());
    }

    @Test
    void preservesUppercaseGoodsModuleAndJsonBodyOnGenericPost() throws Exception {
        MerchantServiceFeign feign = mock(MerchantServiceFeign.class);
        MockMvc mockMvc = mockMvc(feign);

        mockMvc.perform(post("/web/merchant/mall.Goods/add")
                        .contentType("application/json")
                        .content("{\"name\":\"Tea\",\"stock\":12}"))
                .andExpect(status().isOk());

        verify(feign).forwardPost("mall.Goods", "add", Map.of("name", "Tea", "stock", 12));
    }

    @Test
    void forwardsLowercaseGoodsCommonListThroughGenericGet() throws Exception {
        MerchantServiceFeign feign = mock(MerchantServiceFeign.class);
        MockMvc mockMvc = mockMvc(feign);

        mockMvc.perform(get("/web/merchant/mall.goods/commonList"))
                .andExpect(status().isOk());

        verify(feign).forwardGet("mall.goods", "commonList", Map.of());
    }

    @Test
    void preservesUppercaseGoodsModuleOnGenericGet() throws Exception {
        MerchantServiceFeign feign = mock(MerchantServiceFeign.class);
        MockMvc mockMvc = mockMvc(feign);

        mockMvc.perform(get("/web/merchant/mall.Goods/lists"))
                .andExpect(status().isOk());

        verify(feign).forwardGet("mall.Goods", "lists", Map.of());
    }

    @Test
    void forwardsUserListsQueryThroughGenericMerchantGet() throws Exception {
        MerchantServiceFeign feign = mock(MerchantServiceFeign.class);
        MockMvc mockMvc = mockMvc(feign);

        mockMvc.perform(get("/web/merchant/user/lists").param("page_no", "1"))
                .andExpect(status().isOk());

        verify(feign).forwardGet("user", "lists", Map.of("page_no", "1"));
    }

    @Test
    void forwardsLowercaseGoodsDeleteThroughGenericMerchantPost() throws Exception {
        MerchantServiceFeign feign = mock(MerchantServiceFeign.class);
        MockMvc mockMvc = mockMvc(feign);

        mockMvc.perform(post("/web/merchant/mall.goods/delete")
                        .contentType("application/json")
                        .content("{\"id\":7}"))
                .andExpect(status().isOk());

        verify(feign).forwardPost("mall.goods", "delete", Map.of("id", 7));
    }

    private static MockMvc mockMvc(MerchantServiceFeign feign) {
        WebMerchantController controller = new WebMerchantController();
        ReflectionTestUtils.setField(controller, "merchantServiceFeign", feign);
        return MockMvcBuilders.standaloneSetup(controller).build();
    }

    private static final class CyclicFailure extends RuntimeException {
        private Throwable next;

        @Override
        public synchronized Throwable getCause() {
            return next;
        }
    }
}
