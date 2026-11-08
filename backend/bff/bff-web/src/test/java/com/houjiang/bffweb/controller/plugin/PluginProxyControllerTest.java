package com.houjiang.bffweb.controller.plugin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockMultipartHttpServletRequest;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.net.URI;
import com.houjiang.bffweb.support.PluginServiceResolver;
import com.houjiang.bffweb.support.BasePluginCenterUrlResolver;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * {@link PluginProxyController} 单测（T11）：URL 拼接、头透传、上游状态原样回传、非法路径拦截。
 *
 * @author Amos
 */
class PluginProxyControllerTest {

    private static final String BASE_URL = "http://127.0.0.1:8214";

    private RestTemplate restTemplate;

    private PluginProxyController controller;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        MockEnvironment environment = new MockEnvironment();
        for (String code : new String[]{"oa", "erp", "mes", "pm", "order", "im"}) {
            environment.setProperty("feign." + code + ".url", "http://" + code + "-service:8080");
        }
        controller = new PluginProxyController(new PluginServiceResolver(environment, (DiscoveryClient) null),
                new BasePluginCenterUrlResolver(BASE_URL, (DiscoveryClient) null), restTemplate);
    }

    @Test
    @DisplayName("路径安全校验：拒绝穿越、编码绕过、反斜杠、重复斜杠")
    void safePathValidation() {
        assertTrue(PluginProxyController.isSafePath("/plugin/mall-coupon/list"));
        assertTrue(PluginProxyController.isSafePath("/plugin-assets/demo/1.0.0/index.mjs"));
        assertFalse(PluginProxyController.isSafePath("/plugin/../secret"));
        assertFalse(PluginProxyController.isSafePath("/plugin/%2e%2e/secret"));
        assertFalse(PluginProxyController.isSafePath("/plugin/a/..%2fsecret"));
        assertFalse(PluginProxyController.isSafePath("/plugin/a\\b"));
        assertFalse(PluginProxyController.isSafePath("/plugin//x"));
        assertFalse(PluginProxyController.isSafePath("plugin/no-leading-slash"));
        assertFalse(PluginProxyController.isSafePath(""));
        assertFalse(PluginProxyController.isSafePath("/plugin/oa/%252e%252e/secret"));
        assertFalse(PluginProxyController.isSafePath("/plugin/oa/list%3Fadmin=true"));
    }

    @Test
    @DisplayName("目标 URL 拼接：保留原查询串、原有绝对路径")
    void targetUrlBuilding() {
        assertEquals("http://127.0.0.1:8214/plugin/mall-coupon/list",
                PluginProxyController.buildTargetUrl(BASE_URL, "/plugin/mall-coupon/list", null));
        assertEquals("http://127.0.0.1:8214/plugin/mall-coupon/list?page=1&size=10",
                PluginProxyController.buildTargetUrl(BASE_URL, "/plugin/mall-coupon/list", "page=1&size=10"));
    }

    @Test
    @DisplayName("产物通道：转发到宿主 /plugin-assets/**，透传 Content-Type 与 immutable 缓存头")
    void proxyAssetPassesThroughHeaders() {
        byte[] body = "export const ok = true".getBytes(StandardCharsets.UTF_8);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/javascript; charset=UTF-8"));
        headers.set(HttpHeaders.CACHE_CONTROL, "public, max-age=31536000, immutable");
        headers.set("X-Content-Type-Options", "nosniff");
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(byte[].class)))
                .thenReturn(new ResponseEntity<>(body, headers, HttpStatus.OK));

        MockHttpServletRequest request =
                new MockHttpServletRequest("GET", "/web/plugin-assets/demo/1.0.0/index.mjs");
        ResponseEntity<byte[]> response = controller.proxyAsset(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertArrayEquals(body, response.getBody());
        assertEquals("application/javascript;charset=UTF-8", response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE));
        assertEquals("public, max-age=31536000, immutable",
                response.getHeaders().getFirst(HttpHeaders.CACHE_CONTROL));

        ArgumentCaptor<URI> urlCaptor = ArgumentCaptor.forClass(URI.class);
        verify(restTemplate).exchange(urlCaptor.capture(), eq(HttpMethod.GET), any(HttpEntity.class), eq(byte[].class));
        assertEquals("http://127.0.0.1:8214/plugin-assets/demo/1.0.0/index.mjs", urlCaptor.getValue().toString());
    }

    @Test
    @DisplayName("产物通道：无上游缓存头时不伪造缓存头")
    void proxyAssetWithoutCacheHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(byte[].class)))
                .thenReturn(new ResponseEntity<>("{}".getBytes(StandardCharsets.UTF_8), headers, HttpStatus.NOT_FOUND));

        ResponseEntity<byte[]> response = controller.proxyAsset(
                new MockHttpServletRequest("GET", "/web/plugin-assets/demo/1.0.0/missing.mjs"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getHeaders().getFirst(HttpHeaders.CACHE_CONTROL));
    }

    @Test
    @DisplayName("接口通道：透传 Authorization / X-Org-Id / 查询串与请求体")
    void proxyPluginApiPassesAuthAndQuery() {
        byte[] upstream = "{\"code\":200,\"data\":[]}".getBytes(StandardCharsets.UTF_8);
        HttpHeaders upstreamHeaders = new HttpHeaders();
        upstreamHeaders.setContentType(MediaType.APPLICATION_JSON);
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(byte[].class)))
                .thenReturn(new ResponseEntity<>(upstream, upstreamHeaders, HttpStatus.OK));

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/web/plugin/activity/coupon/list");
        request.setQueryString("page=1");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer token-abc");
        request.addHeader("X-Org-Id", "1001");
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);
        request.setContent("{\"status\":1}".getBytes(StandardCharsets.UTF_8));

        ResponseEntity<byte[]> response = controller.proxyPluginApi(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertArrayEquals(upstream, response.getBody());

        ArgumentCaptor<URI> urlCaptor = ArgumentCaptor.forClass(URI.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<HttpEntity<byte[]>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(urlCaptor.capture(), eq(HttpMethod.POST), entityCaptor.capture(), eq(byte[].class));

        assertEquals("http://order-service:8080/plugin/activity/coupon/list?page=1", urlCaptor.getValue().toString());
        HttpHeaders forwarded = entityCaptor.getValue().getHeaders();
        assertEquals("Bearer token-abc", forwarded.getFirst(HttpHeaders.AUTHORIZATION));
        assertEquals("1001", forwarded.getFirst("X-Org-Id"));
        assertArrayEquals("{\"status\":1}".getBytes(StandardCharsets.UTF_8), entityCaptor.getValue().getBody());
    }

    @Test
    @DisplayName("接口通道：multipart 文件与普通字段完整转发")
    void proxyPluginApiPassesMultipartFileAndFields() {
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(byte[].class)))
                .thenReturn(ResponseEntity.ok("{}".getBytes(StandardCharsets.UTF_8)));

        MockMultipartHttpServletRequest request = new MockMultipartHttpServletRequest();
        request.setMethod("POST");
        request.setRequestURI("/web/plugin/oa/contract/file/upload/96");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer token-abc");
        request.addParameter("description", "补充协议");
        request.addFile(new MockMultipartFile(
                "file", "contract.pdf", MediaType.APPLICATION_PDF_VALUE,
                "pdf-content".getBytes(StandardCharsets.UTF_8)));

        ResponseEntity<byte[]> response = controller.proxyPluginApi(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ArgumentCaptor<URI> urlCaptor = ArgumentCaptor.forClass(URI.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<HttpEntity<?>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(urlCaptor.capture(), eq(HttpMethod.POST), entityCaptor.capture(), eq(byte[].class));

        assertEquals("http://oa-service:8080/plugin/oa/contract/file/upload/96", urlCaptor.getValue().toString());
        assertEquals(MediaType.MULTIPART_FORM_DATA, entityCaptor.getValue().getHeaders().getContentType());
        assertTrue(entityCaptor.getValue().getBody() instanceof MultiValueMap);
        @SuppressWarnings("unchecked")
        MultiValueMap<String, Object> parts = (MultiValueMap<String, Object>) entityCaptor.getValue().getBody();
        assertEquals("补充协议", parts.getFirst("description"));
        assertEquals("contract.pdf", ((org.springframework.core.io.Resource) parts.getFirst("file")).getFilename());
    }

    @Test
    void imLoginTargetsIndependentServiceController() {
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(byte[].class)))
                .thenReturn(new ResponseEntity<>(new byte[0], HttpStatus.OK));
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/web/plugin/im/imUser/mallUserLogin");
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);
        request.setContent("{}".getBytes(StandardCharsets.UTF_8));

        assertEquals(HttpStatus.OK, controller.proxyPluginApi(request).getStatusCode());
        ArgumentCaptor<URI> target = ArgumentCaptor.forClass(URI.class);
        verify(restTemplate).exchange(target.capture(), eq(HttpMethod.POST), any(HttpEntity.class), eq(byte[].class));
        assertEquals("http://im-service:8080/imUser/mallUserLogin", target.getValue().toString());
    }

    @Test
    @DisplayName("ERP legacy web entry routes to the independent ERP service")
    void proxyLegacyWebErpApiUsesPluginHost() {
        byte[] upstream = "{\"code\":11006}".getBytes(StandardCharsets.UTF_8);
        HttpHeaders upstreamHeaders = new HttpHeaders();
        upstreamHeaders.setContentType(MediaType.APPLICATION_JSON);
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(byte[].class)))
                .thenReturn(new ResponseEntity<>(upstream, upstreamHeaders, HttpStatus.OK));

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/web/erp/goods/page");
        request.setQueryString("page=1");
        ResponseEntity<byte[]> response = controller.proxyLegacyErpApi(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertArrayEquals(upstream, response.getBody());
        ArgumentCaptor<URI> urlCaptor = ArgumentCaptor.forClass(URI.class);
        verify(restTemplate).exchange(urlCaptor.capture(), eq(HttpMethod.GET), any(HttpEntity.class), eq(byte[].class));
        assertEquals("http://erp-service:8080/plugin/erp/goods/page?page=1", urlCaptor.getValue().toString());
    }

    @Test
    @DisplayName("ERP legacy API entry routes to the independent ERP service")
    void proxyLegacyApiErpUsesPluginHost() {
        byte[] upstream = "{\"code\":11006}".getBytes(StandardCharsets.UTF_8);
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(byte[].class)))
                .thenReturn(new ResponseEntity<>(upstream, new HttpHeaders(), HttpStatus.OK));

        ResponseEntity<byte[]> response = controller.proxyLegacyErpApi(
                new MockHttpServletRequest("GET", "/api/erp/dashboard"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ArgumentCaptor<URI> urlCaptor = ArgumentCaptor.forClass(URI.class);
        verify(restTemplate).exchange(urlCaptor.capture(), eq(HttpMethod.GET), any(HttpEntity.class), eq(byte[].class));
        assertEquals("http://erp-service:8080/plugin/erp/dashboard", urlCaptor.getValue().toString());
    }

    @Test
    @DisplayName("PM 旧入口：改写到独立 PM 服务")
    void proxyLegacyPmApiUsesPluginHost() {
        byte[] upstream = "{\"code\":11006}".getBytes(StandardCharsets.UTF_8);
        HttpHeaders upstreamHeaders = new HttpHeaders();
        upstreamHeaders.setContentType(MediaType.APPLICATION_JSON);
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(byte[].class)))
                .thenReturn(new ResponseEntity<>(upstream, upstreamHeaders, HttpStatus.OK));

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/web/pm/tasks/list");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer token-abc");
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);
        request.setContent("{\"orgId\":\"1\"}".getBytes(StandardCharsets.UTF_8));

        ResponseEntity<byte[]> response = controller.proxyLegacyPmApi(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertArrayEquals(upstream, response.getBody());

        ArgumentCaptor<URI> urlCaptor = ArgumentCaptor.forClass(URI.class);
        verify(restTemplate).exchange(urlCaptor.capture(), eq(HttpMethod.POST), any(HttpEntity.class), eq(byte[].class));
        assertEquals("http://pm-service:8080/plugin/pm/tasks/list", urlCaptor.getValue().toString());
    }

    @Test
    @DisplayName("PM 旧用户列表入口：改写到独立 PM 服务")
    void proxyLegacyPmUserListUsesPluginHost() {
        byte[] upstream = "{\"code\":11006}".getBytes(StandardCharsets.UTF_8);
        HttpHeaders upstreamHeaders = new HttpHeaders();
        upstreamHeaders.setContentType(MediaType.APPLICATION_JSON);
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(byte[].class)))
                .thenReturn(new ResponseEntity<>(upstream, upstreamHeaders, HttpStatus.OK));

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/web/pm/user/list");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer token-abc");
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);
        request.setContent("{\"orgId\":\"1\"}".getBytes(StandardCharsets.UTF_8));

        ResponseEntity<byte[]> response = controller.proxyLegacyPmApi(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertArrayEquals(upstream, response.getBody());

        ArgumentCaptor<URI> urlCaptor = ArgumentCaptor.forClass(URI.class);
        verify(restTemplate).exchange(urlCaptor.capture(), eq(HttpMethod.POST), any(HttpEntity.class), eq(byte[].class));
        assertEquals("http://pm-service:8080/plugin/pm/user/list", urlCaptor.getValue().toString());
    }

    @Test
    @DisplayName("上游 4xx（如插件未启用 423）原样回传状态与响应体")
    void upstreamClientErrorPassThrough() {
        HttpHeaders upstreamHeaders = new HttpHeaders();
        upstreamHeaders.setContentType(MediaType.APPLICATION_JSON);
        byte[] errorBody = "{\"code\":423,\"message\":\"插件未启用: mall-points\"}".getBytes(StandardCharsets.UTF_8);
        when(restTemplate.exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), eq(byte[].class)))
                .thenThrow(HttpClientErrorException.create(HttpStatus.LOCKED, "Locked", upstreamHeaders, errorBody,
                        StandardCharsets.UTF_8));

        ResponseEntity<byte[]> response = controller.proxyAsset(
                new MockHttpServletRequest("GET", "/web/plugin-assets/mall-points/1.0.0/index.mjs"));

        assertEquals(HttpStatus.LOCKED, response.getStatusCode());
        assertArrayEquals(errorBody, response.getBody());
        assertEquals("application/json", response.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE));
    }

    @Test
    @DisplayName("非法路径：直接 400 且不发起任何上游请求")
    void unsafePathRejected() {
        MockHttpServletRequest apiRequest = new MockHttpServletRequest("GET", "/web/plugin/../../etc/passwd");
        ResponseEntity<byte[]> apiResponse = controller.proxyPluginApi(apiRequest);
        assertEquals(HttpStatus.BAD_REQUEST, apiResponse.getStatusCode());

        MockHttpServletRequest assetRequest = new MockHttpServletRequest("GET", "/web/plugin-assets/demo/1.0.0/%2e%2e/secret.txt");
        ResponseEntity<byte[]> assetResponse = controller.proxyAsset(assetRequest);
        assertEquals(HttpStatus.BAD_REQUEST, assetResponse.getStatusCode());

        verifyNoInteractions(restTemplate);
    }

    @Test
    @DisplayName("存量路由前缀常量保持与前端加载器契约一致")
    void routeContract() {
        assertEquals("/web/plugin-assets/", PluginProxyController.assetPrefix());
        assertEquals("/web/plugin/", PluginProxyController.apiPrefix());
    }
    @Test
    void unknownPluginIsRejectedWithoutUpstreamCall() {
        assertEquals(HttpStatus.NOT_FOUND, controller.proxyPluginApi(
                new MockHttpServletRequest("GET", "/web/plugin/not-registered/list")).getStatusCode());
        verifyNoInteractions(restTemplate);
    }

    @Test
    void standaloneProductsUseTheirOwnServiceAddresses() {
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(byte[].class)))
                .thenReturn(ResponseEntity.ok(new byte[0]));
        for (String code : new String[]{"oa", "erp", "mes"}) {
            assertEquals(HttpStatus.OK, controller.proxyPluginApi(
                    new MockHttpServletRequest("GET", "/web/plugin/" + code + "/list")).getStatusCode());
            verify(restTemplate).exchange(eq(URI.create("http://" + code + "-service:8080/plugin/" + code + "/list")),
                    eq(HttpMethod.GET), any(HttpEntity.class), eq(byte[].class));
        }
    }

    @Test
    void missingServiceReturns503WithoutMallFallback() {
        controller = new PluginProxyController(new PluginServiceResolver(new MockEnvironment(), (DiscoveryClient) null),
                new BasePluginCenterUrlResolver(BASE_URL, (DiscoveryClient) null), restTemplate);
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, controller.proxyPluginApi(
                new MockHttpServletRequest("GET", "/web/plugin/oa/list")).getStatusCode());
        verifyNoInteractions(restTemplate);
    }

    @Test
    void encodedQueryAndUntrustedHostHintsDoNotChangeDestination() {
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(byte[].class)))
                .thenReturn(ResponseEntity.ok(new byte[0]));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/web/plugin/oa/list");
        request.setQueryString("q=a%2Fb%20c&hostService=mall&url=http%3A%2F%2Fevil");
        request.addHeader("hostService", "mall");
        controller.proxyPluginApi(request);
        verify(restTemplate).exchange(eq(URI.create("http://oa-service:8080/plugin/oa/list?" + request.getQueryString())),
                eq(HttpMethod.GET), any(HttpEntity.class), eq(byte[].class));
    }

    @Test
    void explicitAicodeTransitionUsesExistingStandaloneApiAndForwardsSession() {
        var resolver = mock(com.houjiang.bffweb.support.PluginServiceResolver.class);
        when(resolver.resolve("aicode")).thenReturn(new com.houjiang.bffweb.support.PluginServiceResolver.Resolved(
                "pm", "http://127.0.0.1:8318", "aicode-transition"));
        var proxy = new PluginProxyController(resolver,
                new com.houjiang.bffweb.support.BasePluginCenterUrlResolver(BASE_URL, (org.springframework.cloud.client.discovery.DiscoveryClient) null), restTemplate);
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(byte[].class)))
                .thenReturn(ResponseEntity.ok("{}".getBytes(StandardCharsets.UTF_8)));
        var request = new MockHttpServletRequest("GET", "/web/plugin/aicode/sources");
        request.addHeader("token", "platform-session");
        request.setQueryString("page=2");
        proxy.proxyPluginApi(request);
        ArgumentCaptor<URI> url = ArgumentCaptor.forClass(URI.class);
        ArgumentCaptor<HttpEntity> entity = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(url.capture(), eq(HttpMethod.GET), entity.capture(), eq(byte[].class));
        assertEquals("http://127.0.0.1:8318/aicode/sources?page=2", url.getValue().toString());
        assertEquals("platform-session", entity.getValue().getHeaders().getFirst("token"));
    }

}
