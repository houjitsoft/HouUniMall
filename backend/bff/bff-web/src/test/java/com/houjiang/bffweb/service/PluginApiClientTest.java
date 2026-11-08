package com.houjiang.bffweb.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.houjiang.bffweb.util.ResponseResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;
import java.net.URI;
import com.houjiang.bffweb.support.PluginServiceResolver;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PluginApiClientTest {
    private RestTemplate restTemplate;
    private PluginApiClient client;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        client = new PluginApiClient(new PluginServiceResolver(new MockEnvironment().withProperty(
                "feign.erp.url", "http://erp-service:8090"), (DiscoveryClient) null), restTemplate, new ObjectMapper());
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/web/common/file/import/confirm");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer test-token");
        request.addHeader("X-Org-Id", "org-1");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void postsToPluginHostAndForwardsTenantHeaders() {
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(ResponseResult.class)))
                .thenReturn(new ResponseEntity<>(ResponseResult.success(Map.of("id", 8)), HttpStatus.OK));

        ResponseResult<?> result = client.post("erp", "/goods", Map.of("goodsName", "demo"));

        assertEquals(1, result.getCode());
        ArgumentCaptor<URI> target = ArgumentCaptor.forClass(URI.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<HttpEntity<Map<String, Object>>> entity = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(target.capture(), eq(HttpMethod.POST), entity.capture(), eq(ResponseResult.class));
        assertEquals("http://erp-service:8090/plugin/erp/goods", target.getValue().toString());
        assertEquals("Bearer test-token", entity.getValue().getHeaders().getFirst(HttpHeaders.AUTHORIZATION));
        assertEquals("org-1", entity.getValue().getHeaders().getFirst("X-Org-Id"));
        assertEquals(MediaType.APPLICATION_JSON, entity.getValue().getHeaders().getContentType());
    }

    @Test
    void rejectsUnsafePluginPathsBeforeCallingUpstream() {
        ResponseResult<?> result = client.post("erp", "/goods/../secret", Map.of());

        assertEquals(400, result.getCode());
        assertFalse(PluginApiClient.isSafePath("/goods/../secret"));
        assertFalse(PluginApiClient.isSafePath("/goods/%ZZ"));
    }

    @Test
    void mapsUpstreamConnectivityFailureToStableResponse() {
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(ResponseResult.class)))
                .thenThrow(new RestClientException("offline"));

        ResponseResult<?> result = client.post("erp", "/goods", Map.of());

        assertEquals(503, result.getCode());
    }

    @Test
    void trustedGetUsesInternalTokenWithoutForwardingBrowserAuthorization() {
        client = new PluginApiClient(new PluginServiceResolver(new MockEnvironment().withProperty(
                "feign.order.url", "http://order-service:8209"), (DiscoveryClient) null),
                restTemplate, new ObjectMapper(), "service-secret");
        when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(new ResponseEntity<>(java.util.List.of(Map.of("id", 9)), HttpStatus.OK));

        PluginApiClient.PluginCallResult result = client.internalGet(
                "mall-advertising", "/public/slots/pc-login-left");

        assertEquals(200, result.status());
        ArgumentCaptor<URI> target = ArgumentCaptor.forClass(URI.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<HttpEntity<Void>> entity = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(target.capture(), eq(HttpMethod.GET), entity.capture(), eq(Object.class));
        assertEquals("http://order-service:8209/plugin/mall-advertising/public/slots/pc-login-left",
                target.getValue().toString());
        assertEquals("service-secret", entity.getValue().getHeaders().getFirst("X-Internal-Job-Token"));
        assertEquals(null, entity.getValue().getHeaders().getFirst(HttpHeaders.AUTHORIZATION));
    }
    @Test
    void missingOwnerOrServiceDoesNotCallMall() {
        assertEquals(404, client.post("unknown", "/list", Map.of()).getCode());
        assertEquals(503, client.post("oa", "/list", Map.of()).getCode());
        org.mockito.Mockito.verifyNoInteractions(restTemplate);
    }

}
