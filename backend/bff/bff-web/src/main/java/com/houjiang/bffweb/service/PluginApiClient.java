package com.houjiang.bffweb.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.houjiang.bffweb.config.FeignRequestInterceptor;
import com.houjiang.bffweb.support.PluginServiceResolver;
import com.houjiang.bffweb.util.ResponseResult;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/** Calls plugin APIs directly through the service selected by the trusted release manifest. */
@Slf4j
@Service
public class PluginApiClient {
    private static final String PLUGIN_CODE_PATTERN = "[a-z0-9][a-z0-9-]{0,63}";

    private final PluginServiceResolver pluginServiceResolver;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String internalToken;

    @Autowired
    public PluginApiClient(PluginServiceResolver pluginServiceResolver,
                           @Value("${houcloud.plugin.internal-job-token:}") String internalToken) {
        this(pluginServiceResolver, createRestTemplate(), new ObjectMapper(), internalToken);
    }

    PluginApiClient(PluginServiceResolver pluginServiceResolver, RestTemplate restTemplate,
                    ObjectMapper objectMapper) {
        this(pluginServiceResolver, restTemplate, objectMapper, "");
    }

    PluginApiClient(PluginServiceResolver pluginServiceResolver, RestTemplate restTemplate,
                    ObjectMapper objectMapper, String internalToken) {
        this.pluginServiceResolver = java.util.Objects.requireNonNull(pluginServiceResolver);
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.internalToken = internalToken == null ? "" : internalToken.trim();
    }

    /**
     * Executes a JSON POST against {@code /plugin/{pluginCode}/{path}}.
     * Authentication and tenant headers are copied from the current BFF request.
     */
    public ResponseResult<?> post(String pluginCode, String path, Map<String, Object> body) {
        if (pluginCode == null || !pluginCode.matches(PLUGIN_CODE_PATTERN)
                || path == null || !isSafePath(path)) {
            return ResponseResult.fail(400, "Invalid plugin API path");
        }

        String normalizedPath = path.startsWith("/") ? path : "/" + path;
        PluginServiceResolver.Resolved resolved;
        try {
            resolved = pluginServiceResolver.resolve(pluginCode);
        } catch (PluginServiceResolver.ResolutionException ex) {
            return ResponseResult.fail(ex.status(), ex.getMessage());
        }
        String target = resolved.url() + "/plugin/" + pluginCode + normalizedPath;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.set(FeignRequestInterceptor.GATEWAY_HEADER, FeignRequestInterceptor.GATEWAY_VALUE);
        copyRequestHeaders(headers);

        try {
            ResponseEntity<ResponseResult> response = restTemplate.exchange(
                    java.net.URI.create(target), HttpMethod.POST, new HttpEntity<>(body == null ? Map.of() : body, headers),
                    ResponseResult.class);
            ResponseResult<?> result = response.getBody();
            return result == null ? ResponseResult.fail(503, "Plugin host returned no response") : result;
        } catch (HttpStatusCodeException ex) {
            return decodeError(ex);
        } catch (RestClientException ex) {
            log.error("Plugin API call failed: target={}", target, ex);
            return ResponseResult.fail(503, "Plugin host is unavailable");
        }
    }

    /**
     * Calls a read-only plugin route as a trusted service identity. Browser credentials are deliberately not copied.
     */
    public PluginCallResult internalGet(String pluginCode, String path) {
        if (pluginCode == null || !pluginCode.matches(PLUGIN_CODE_PATTERN)
                || path == null || !isSafePath(path)) {
            return new PluginCallResult(400, null);
        }
        if (internalToken.isBlank()) {
            log.error("Trusted plugin GET is disabled because houcloud.plugin.internal-job-token is empty");
            return new PluginCallResult(503, null);
        }
        String normalizedPath = path.startsWith("/") ? path : "/" + path;
        PluginServiceResolver.Resolved resolved;
        try {
            resolved = pluginServiceResolver.resolve(pluginCode);
        } catch (PluginServiceResolver.ResolutionException ex) {
            return new PluginCallResult(ex.status(), null);
        }
        String target = resolved.url() + "/plugin/" + pluginCode + normalizedPath;
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.set(FeignRequestInterceptor.GATEWAY_HEADER, FeignRequestInterceptor.GATEWAY_VALUE);
        headers.set("X-Internal-Job-Token", internalToken);
        try {
            ResponseEntity<Object> response = restTemplate.exchange(java.net.URI.create(target), HttpMethod.GET,
                    new HttpEntity<Void>(headers), Object.class);
            return new PluginCallResult(response.getStatusCode().value(), response.getBody());
        } catch (HttpStatusCodeException ex) {
            return new PluginCallResult(ex.getStatusCode().value(), null);
        } catch (RestClientException ex) {
            log.warn("Trusted plugin GET failed: target={}", target, ex);
            return new PluginCallResult(503, null);
        }
    }

    public record PluginCallResult(int status, Object body) {
    }

    private ResponseResult<?> decodeError(HttpStatusCodeException ex) {
        byte[] body = ex.getResponseBodyAsByteArray();
        if (body.length > 0) {
            try {
                ResponseResult<?> result = objectMapper.readValue(body, ResponseResult.class);
                if (result != null) return result;
            } catch (Exception ignored) {
                // Fall through to a stable BFF error shape for non-JSON upstream responses.
            }
        }
        return ResponseResult.fail(ex.getStatusCode().value(),
                ex.getStatusText() == null || ex.getStatusText().isBlank()
                        ? "Plugin API request failed" : ex.getStatusText());
    }

    private void copyRequestHeaders(HttpHeaders headers) {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)) {
            return;
        }
        HttpServletRequest request = attributes.getRequest();
        copyHeader(request, headers, HttpHeaders.AUTHORIZATION);
        copyHeader(request, headers, "X-Org-Id");
        copyHeader(request, headers, "X-Forwarded-Host");
        copyHeader(request, headers, "X-Forwarded-Proto");
    }

    private void copyHeader(HttpServletRequest request, HttpHeaders headers, String name) {
        String value = request.getHeader(name);
        if (value != null && !value.isBlank()) headers.set(name, value);
    }

    static boolean isSafePath(String path) {
        if (path == null || path.isBlank() || !path.startsWith("/") || path.contains("//")
                || path.contains("\\") || path.contains("..")) {
            return false;
        }
        try {
            String decoded = java.net.URLDecoder.decode(path.replace("+", "%2B"), StandardCharsets.UTF_8);
            return !decoded.contains("..") && !decoded.contains("\\") && !decoded.contains("//")
                    && !decoded.contains("%") && !decoded.contains("?") && !decoded.contains("#")
                    && decoded.chars().noneMatch(ch -> ch < 0x20 || ch == 0x7f);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private static RestTemplate createRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(45000);
        return new RestTemplate(factory);
    }
}
