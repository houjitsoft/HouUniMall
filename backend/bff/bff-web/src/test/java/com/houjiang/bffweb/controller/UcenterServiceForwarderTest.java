package com.houjiang.bffweb.controller;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockMultipartHttpServletRequest;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UcenterServiceForwarderTest {
    @Test
    void forwardsWalletBodyAndTokenToPayment() throws Exception {
        RestTemplate http = mock(RestTemplate.class);
        when(http.exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(byte[].class)))
                .thenReturn(ResponseEntity.ok("{\"code\":1}".getBytes(StandardCharsets.UTF_8)));
        UcenterPaymentRouteController controller = new UcenterPaymentRouteController(
                "http://payment:8207", mock(DiscoveryClient.class), http);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/web/pay/payment/userResource/balanceDisplay");
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);
        request.addHeader("token", "user-token");
        request.setContent("{\"token\":\"user-token\"}".getBytes(StandardCharsets.UTF_8));

        controller.post(request);
        ArgumentCaptor<URI> url = ArgumentCaptor.forClass(URI.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<HttpEntity<?>> entity = ArgumentCaptor.forClass(HttpEntity.class);
        verify(http).exchange(url.capture(), eq(HttpMethod.POST), entity.capture(), eq(byte[].class));
        assertEquals("http://payment:8207/payment/userResource/balanceDisplay", url.getValue().toString());
        assertEquals("user-token", entity.getValue().getHeaders().getFirst("token"));
        assertArrayEquals(request.getContentAsByteArray(), (byte[]) entity.getValue().getBody());
    }

    @Test
    void forwardsAvatarAsMultipartWithFileAndFormToken() throws Exception {
        RestTemplate http = mock(RestTemplate.class);
        when(http.exchange(any(URI.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(byte[].class)))
                .thenReturn(ResponseEntity.ok(new byte[0]));
        UcenterUserRouteController controller = new UcenterUserRouteController(
                "http://userms:8200", mock(DiscoveryClient.class), http);
        MockMultipartHttpServletRequest request = new MockMultipartHttpServletRequest();
        request.setRequestURI("/web/user/user/ms/uploadPhoto");
        request.setParameter("token", "form-token");
        request.addFile(new MockMultipartFile("file", "avatar.png", "image/png", new byte[]{1, 2, 3}));

        controller.post(request);
        ArgumentCaptor<URI> url = ArgumentCaptor.forClass(URI.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<HttpEntity<?>> entity = ArgumentCaptor.forClass(HttpEntity.class);
        verify(http).exchange(url.capture(), eq(HttpMethod.POST), entity.capture(), eq(byte[].class));
        assertEquals("http://userms:8200/user/ms/uploadPhoto", url.getValue().toString());
        assertEquals(MediaType.MULTIPART_FORM_DATA, entity.getValue().getHeaders().getContentType());
        @SuppressWarnings("unchecked")
        MultiValueMap<String, Object> parts = (MultiValueMap<String, Object>) entity.getValue().getBody();
        assertEquals("form-token", parts.getFirst("token"));
        assertTrue(parts.getFirst("file") instanceof org.springframework.core.io.Resource);
    }
}
