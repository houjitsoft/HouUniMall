package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.controller.plugin.ProxyRestTemplateFactory;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.*;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 银联企业订单分账：BFF 透传到 payment 宿主 /plugin/ums-corp-order/**。
 * <p>直连 payment 服务发现/配置，不经过 PluginServiceResolver 的插件清单
 * （避免 unipay 已退役或 bff 未带上 ums 清单条目时返回 Unknown plugin）。
 */
@Slf4j
@RestController
public class WebUmsCorpOrderController {

    private static final String WEB_PREFIX = "/web/plugin/ums-corp-order";
    private static final String PAY_PREFIX = "/plugin/ums-corp-order";

    private final RestTemplate restTemplate;
    private final DiscoveryClient discovery;
    private final String configuredPaymentUrl;

    public WebUmsCorpOrderController(
            DiscoveryClient discovery,
            @Value("${feign.payment.url:}") String configuredPaymentUrl) {
        this.discovery = discovery;
        this.configuredPaymentUrl = configuredPaymentUrl == null ? "" : configuredPaymentUrl.trim();
        this.restTemplate = ProxyRestTemplateFactory.create(5000, 35000);
    }

    @RequestMapping(value = "/web/plugin/ums-corp-order/**",
            method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<byte[]> proxy(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String suffix = uri.startsWith(WEB_PREFIX) ? uri.substring(WEB_PREFIX.length()) : "";
        if (suffix.isEmpty()) {
            suffix = "/";
        }
        String targetPath = PAY_PREFIX + (suffix.startsWith("/") ? suffix : "/" + suffix);

        String origin = resolvePaymentOrigin();
        if (origin == null) {
            return jsonError(503, "支付服务暂时不可用");
        }
        String target = origin + targetPath;
        if (request.getQueryString() != null && !request.getQueryString().isEmpty()) {
            target = target + "?" + request.getQueryString();
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            copy(request, headers, HttpHeaders.AUTHORIZATION);
            copy(request, headers, "token");
            copy(request, headers, "X-Org-Id");
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpMethod method = HttpMethod.valueOf(request.getMethod().toUpperCase());
            byte[] body = method == HttpMethod.GET ? null : StreamUtils.copyToByteArray(request.getInputStream());
            ResponseEntity<byte[]> resp = restTemplate.exchange(
                    URI.create(target), method, new HttpEntity<>(body, headers), byte[].class);
            HttpHeaders out = new HttpHeaders();
            if (resp.getHeaders().getContentType() != null) {
                out.setContentType(resp.getHeaders().getContentType());
            }
            out.remove(HttpHeaders.TRANSFER_ENCODING);
            out.remove(HttpHeaders.CONTENT_LENGTH);
            out.remove(HttpHeaders.CONNECTION);
            return new ResponseEntity<>(resp.getBody(), out, resp.getStatusCode());
        } catch (Exception ex) {
            log.error("[UMS] proxy failed {} -> {}", uri, ex.getMessage());
            return jsonError(503, "银联企业订单服务暂时不可用");
        }
    }

    private String resolvePaymentOrigin() {
        if (!configuredPaymentUrl.isEmpty()) {
            return normalizeOrigin(configuredPaymentUrl);
        }
        try {
            List<ServiceInstance> instances = discovery == null ? List.of() : discovery.getInstances("payment");
            if (instances != null && !instances.isEmpty()) {
                return normalizeOrigin(instances.get(0).getUri().toString());
            }
        } catch (RuntimeException ex) {
            log.warn("[UMS] payment discovery failed: {}", ex.getMessage());
        }
        return null;
    }

    private static String normalizeOrigin(String value) {
        try {
            URI uri = URI.create(value.trim());
            if (!("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
                    || uri.getHost() == null) {
                return null;
            }
            String origin = uri.getScheme() + "://" + uri.getHost()
                    + (uri.getPort() > 0 ? ":" + uri.getPort() : "");
            return origin.endsWith("/") ? origin.substring(0, origin.length() - 1) : origin;
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private static void copy(HttpServletRequest request, HttpHeaders headers, String name) {
        String v = request.getHeader(name);
        if (v != null && !v.isBlank()) {
            headers.set(name, v);
        }
    }

    private static ResponseEntity<byte[]> jsonError(int status, String message) {
        String json = "{\"code\":" + status + ",\"message\":\"" + message.replace("\"", "'") + "\",\"data\":null}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        int http = status >= 400 && status < 600 ? status : 500;
        return new ResponseEntity<>(json.getBytes(StandardCharsets.UTF_8), headers, HttpStatus.valueOf(http));
    }
}
