package com.houjiang.bffweb.controller;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UcenterOrderRouteControllerTest {
    @Test
    void forwardsConsumerOrderToOrderServiceWithTokenAndBody() throws Exception {
        RestTemplate http = mock(RestTemplate.class);
        DiscoveryClient discovery = mock(DiscoveryClient.class);
        byte[] response = "{\"code\":1}".getBytes(StandardCharsets.UTF_8);
        when(http.exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(byte[].class)))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).body(response));
        UcenterOrderRouteController controller = new UcenterOrderRouteController(
                "http://order-service:8080", discovery, http);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/web/order/order/base/im/queryUserOrderInfo");
        request.addHeader("token", "test-token");
        request.setContentType("application/json");
        request.setContent("{\"page\":1}".getBytes(StandardCharsets.UTF_8));

        assertArrayEquals(response, controller.post(request).getBody());
        ArgumentCaptor<URI> url = ArgumentCaptor.forClass(URI.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<HttpEntity<byte[]>> body = ArgumentCaptor.forClass(HttpEntity.class);
        verify(http).exchange(url.capture(), eq(HttpMethod.POST), body.capture(), eq(byte[].class));
        assertEquals("http://order-service:8080/order/base/im/queryUserOrderInfo", url.getValue().toString());
        assertEquals("test-token", body.getValue().getHeaders().getFirst("token"));
        assertArrayEquals(request.getContentAsByteArray(), body.getValue().getBody());
    }

    @Test
    void doesNotAdvertiseNonexistentAccountingPeriodRoutes() {
        String[] routes = Arrays.stream(UcenterOrderRouteController.class.getDeclaredMethods())
                .map(method -> method.getAnnotation(PostMapping.class))
                .filter(mapping -> mapping != null)
                .flatMap(mapping -> Arrays.stream(mapping.value()))
                .toArray(String[]::new);
        assertFalse(Arrays.stream(routes).anyMatch(route -> route.contains("accountingPeriod")));
        assertTrue(Arrays.asList(routes).contains("/web/order/activity/voucherBase/selectvcbase"));
        assertTrue(Arrays.asList(routes).contains("/web/order/order/return/platform/add/intervention"));
    }
}
