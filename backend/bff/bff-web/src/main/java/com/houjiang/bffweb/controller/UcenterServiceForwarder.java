package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.controller.plugin.ProxyRestTemplateFactory;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

/** Transport for explicitly mapped consumer endpoints; service ownership is fixed by the caller. */
final class UcenterServiceForwarder {
    private final String service;
    private final String prefix;
    private final String configuredUrl;
    private final DiscoveryClient discovery;
    private final RestTemplate http;

    UcenterServiceForwarder(String service, String prefix, String configuredUrl,
                            DiscoveryClient discovery, RestTemplate http) {
        this.service = service;
        this.prefix = prefix;
        this.configuredUrl = configuredUrl;
        this.discovery = discovery;
        this.http = http;
    }

    static RestTemplate httpClient() {
        return ProxyRestTemplateFactory.create(5000, 45000);
    }

    /**
     * 去掉逐跳（hop-by-hop）传输头。
     *
     * <p>下游服务的响应本身带 Transfer-Encoding，forward() 原样复制回 ResponseEntity，
     * 容器写出 body 时又会补一条，nginx 收到重复头直接判 502
     * （error.log: upstream sent duplicate header line: "Transfer-Encoding: chunked"）。
     * 大响应（如全量行政区树）走 chunked 才触发，小响应看不出问题，因此这类 502
     * 只在特定接口上出现，极易误判成服务不可达。
     *
     * <p>🔴 所有 {@code forwarder.forward(...)} 的返回值都必须过这个方法。
     */
    static ResponseEntity<byte[]> sanitize(ResponseEntity<byte[]> forwarded) {
        HttpHeaders headers = new HttpHeaders();
        headers.putAll(forwarded.getHeaders());
        headers.remove(HttpHeaders.TRANSFER_ENCODING);
        headers.remove(HttpHeaders.CONTENT_LENGTH);
        headers.remove(HttpHeaders.CONNECTION);
        return new ResponseEntity<>(forwarded.getBody(), headers, forwarded.getStatusCode());
    }

    ResponseEntity<byte[]> forward(HttpServletRequest request, HttpMethod method) throws IOException {
        String path = request.getRequestURI();
        if (!path.startsWith(prefix + "/")) return ResponseEntity.badRequest().build();
        String origin = configuredUrl == null ? "" : configuredUrl.trim();
        if (origin.isEmpty()) {
            List<ServiceInstance> instances = discovery == null ? List.of() : discovery.getInstances(service);
            if (instances == null || instances.isEmpty()) return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
            origin = instances.get(0).getUri().toString();
        }
        URI base;
        try {
            base = URI.create(origin);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        if (!("http".equalsIgnoreCase(base.getScheme()) || "https".equalsIgnoreCase(base.getScheme()))
                || base.getHost() == null || base.getUserInfo() != null || base.getRawQuery() != null
                || base.getRawFragment() != null || !(base.getRawPath().isEmpty() || "/".equals(base.getRawPath()))) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        String target = origin.replaceAll("/$", "") + path.substring(prefix.length());
        if (request.getQueryString() != null && !request.getQueryString().isEmpty()) {
            target += "?" + request.getQueryString();
        }
        HttpHeaders headers = new HttpHeaders();
        for (String name : List.of("Authorization", "token", "Token", "X-Org-Id")) {
            String value = request.getHeader(name);
            if (value != null && !value.isBlank()) headers.set(name, value);
        }
        HttpEntity<?> entity;
        if (request instanceof MultipartHttpServletRequest multipart) {
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
            for (Map.Entry<String, String[]> entry : multipart.getParameterMap().entrySet()) {
                for (String value : entry.getValue()) parts.add(entry.getKey(), value);
            }
            for (Map.Entry<String, List<MultipartFile>> entry : multipart.getMultiFileMap().entrySet()) {
                for (MultipartFile file : entry.getValue()) parts.add(entry.getKey(), file.getResource());
            }
            entity = new HttpEntity<>(parts, headers);
        } else {
            if (request.getContentType() != null) headers.set(HttpHeaders.CONTENT_TYPE, request.getContentType());
            entity = new HttpEntity<>(method == HttpMethod.GET ? null : request.getInputStream().readAllBytes(), headers);
        }
        try {
            ResponseEntity<byte[]> response = http.exchange(URI.create(target), method, entity, byte[].class);
            return ResponseEntity.status(response.getStatusCode()).headers(response.getHeaders()).body(response.getBody());
        } catch (HttpStatusCodeException ex) {
            return ResponseEntity.status(ex.getStatusCode()).headers(ex.getResponseHeaders()).body(ex.getResponseBodyAsByteArray());
        }
    }
}
