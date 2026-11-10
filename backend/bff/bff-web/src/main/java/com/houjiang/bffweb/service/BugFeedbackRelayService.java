package com.houjiang.bffweb.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.*;

@Service
public class BugFeedbackRelayService {
    private final StringRedisTemplate redis;
    private final ObjectMapper json;
    private final RestTemplate http;
    @Value("${houcloud.bug-feedback.central-url:https://unimall.houjit.com}")
    private String centralUrl = "https://unimall.houjit.com";
    @Value("${houcloud.bug-feedback.allowed-hosts:unimall.houjit.com}")
    private String allowedHosts = "unimall.houjit.com";
    private static final Map<String, Integer> FIELDS = Map.of(
            "sourceSite", 255, "serviceCode", 100, "menuName", 255, "pageUrl", 1000,
            "apiUrl", 1000, "errorContent", 16000, "analysis", 16000,
            "reporterName", 100, "clientVersion", 64);

    public BugFeedbackRelayService(@org.springframework.beans.factory.annotation.Qualifier("bugFeedbackRedisTemplate")
                                   StringRedisTemplate redis, ObjectMapper json) {
        this.redis = redis; this.json = json;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory() {
            @Override protected void prepareConnection(HttpURLConnection connection, String method) throws IOException {
                super.prepareConnection(connection, method);
                connection.setInstanceFollowRedirects(false);
            }
        };
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(20000);
        this.http = new RestTemplate(factory);
    }

    public void requireSession(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || token.isBlank()) token = request.getHeader("token");
        if (token != null && token.regionMatches(true, 0, "Bearer ", 0, 7)) token = token.substring(7);
        if (token == null || token.isBlank() || token.length() > 8192 || token.chars().anyMatch(Character::isWhitespace))
            throw error(401);
        final String session;
        try { session = redis.opsForValue().get("hou-user-token-user_" + token); }
        catch (RuntimeException unavailable) { throw error(503); }
        if (session == null || session.isBlank() || "null".equals(session)) throw error(401);
    }

    public ResponseEntity<byte[]> submit(String payload, List<MultipartFile> files, HttpServletRequest request) {
        requireSession(request);
        ObjectNode clean = payload(payload);
        try {
            clean.put("sourceSite", new URI(request.getScheme(), null, request.getServerName(),
                    request.getServerPort(), null, null, null).toString());
        } catch (Exception invalid) { throw error(400); }
        validateFiles(files, request);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        var parts = new LinkedMultiValueMap<String, Object>();
        parts.add("payload", new HttpEntity<>(clean.toString(), jsonHeaders()));
        if (files != null) for (MultipartFile file : files) {
            HttpHeaders partHeaders = new HttpHeaders();
            partHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            partHeaders.setContentDispositionFormData("files", "feedback-image");
            parts.add("files", new HttpEntity<>(file.getResource(), partHeaders));
        }
        // 独立 RestTemplate，不经过会复制用户 JWT 的 Feign interceptor。
        ResponseEntity<byte[]> response = exchange(central() + "/web/platform/bug-feedback/report",
                HttpMethod.POST, new HttpEntity<>(parts, headers));
        JsonNode receipt = responseJson(response);
        if (!receipt.path("code").isIntegralNumber() || receipt.path("code").intValue() != 1) throw error(502);
        JsonNode id = receipt.path("data").path("id");
        if (!id.isTextual() || id.asText().isBlank() || id.asText().length() > 128) throw error(502);
        // 仅返回回执，不把中央可能包含的调试信息暴露给站点用户。
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(
                ("{\"code\":1,\"message\":\"成功\",\"data\":{\"id\":" + id + "}}").getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    private ObjectNode payload(String raw) {
        if (raw == null || raw.isBlank() || raw.length() > 48000) throw error(400);
        try {
            JsonNode input = json.readTree(raw);
            if (!input.isObject()) throw error(400);
            ObjectNode clean = json.createObjectNode();
            for (var entry : FIELDS.entrySet()) {
                JsonNode value = input.get(entry.getKey());
                if (value != null && (!value.isTextual() || value.asText().length() > entry.getValue())) throw error(400);
                clean.put(entry.getKey(), value == null ? "" : value.asText());
            }
            if (clean.path("errorContent").asText().isBlank()) throw error(400);
            for (String field : List.of("pageUrl", "apiUrl")) {
                String value = clean.path(field).asText();
                if (!value.isEmpty()) {
                    URI uri = URI.create(value);
                    if (!("https".equalsIgnoreCase(uri.getScheme()) || "http".equalsIgnoreCase(uri.getScheme()))
                            || uri.getHost() == null || uri.getUserInfo() != null) throw error(400);
                }
            }
            return clean;
        } catch (IOException | IllegalArgumentException invalid) { throw error(400); }
    }

    private void validateFiles(List<MultipartFile> files, HttpServletRequest request) {
        if (request.getContentLengthLong() > 12L * 1024 * 1024 || (files != null && files.size() > 4)) throw error(413);
        long total = 0;
        if (files != null) for (MultipartFile file : files) {
            if (file == null || file.isEmpty() || file.getSize() > 4L * 1024 * 1024) throw error(413);
            total += file.getSize();
        }
        if (total > 12L * 1024 * 1024) throw error(413);
    }

    private ResponseEntity<byte[]> exchange(String url, HttpMethod method, HttpEntity<?> entity) {
        try {
            ResponseEntity<byte[]> response = http.exchange(URI.create(url), method, entity, byte[].class);
            if (!response.getStatusCode().is2xxSuccessful()) throw error(502);
            return response;
        } catch (org.springframework.web.client.HttpStatusCodeException upstream) {
            int status = upstream.getStatusCode().value();
            throw error(status >= 400 && status < 500 ? status : 502);
        } catch (org.springframework.web.client.RestClientException unavailable) { throw error(502); }
    }

    private JsonNode responseJson(ResponseEntity<byte[]> response) {
        try {
            JsonNode result = json.readTree(response.getBody());
            if (result == null || !result.isObject()) throw error(502);
            return result;
        } catch (IOException | IllegalArgumentException invalid) { throw error(502); }
    }

    private String central() {
        URI uri = origin(centralUrl);
        if (!"https".equalsIgnoreCase(uri.getScheme()) || Arrays.stream(allowedHosts.split(","))
                .map(String::trim).noneMatch(host -> host.equalsIgnoreCase(uri.getHost()))) throw error(503);
        return uri.toString().replaceAll("/$", "");
    }

    private URI origin(String url) {
        try {
            URI uri = URI.create(url);
            if (!("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
                    || uri.getHost() == null || uri.getUserInfo() != null || uri.getRawQuery() != null
                    || uri.getRawFragment() != null || !(uri.getPath().isEmpty() || uri.getPath().equals("/"))) throw error(503);
            return uri;
        } catch (IllegalArgumentException invalid) { throw error(503); }
    }

    private HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders(); headers.setContentType(MediaType.APPLICATION_JSON); return headers;
    }

    public static ResponseStatusException error(int status) {
        String message = switch (status) {
            case 400 -> "反馈参数不合法";
            case 401 -> "请先登录";
            case 403 -> "无权访问反馈管理";
            case 413 -> "反馈文件或请求体超过限制";
            case 429 -> "反馈提交过于频繁";
            default -> "反馈服务暂时不可用";
        };
        return new ResponseStatusException(HttpStatusCode.valueOf(status), message);
    }
}
