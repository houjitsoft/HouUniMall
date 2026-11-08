package com.houjiang.bffweb.controller.plugin;

import com.houjiang.bffweb.support.BasePluginCenterUrlResolver;
import com.houjiang.bffweb.support.PluginServiceResolver;
import java.io.IOException;
import java.net.URI;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

/**
 * Routes plugin APIs directly to their owning service. The bundled release manifest
 * determines ownership; request parameters never select a host. Asset delivery is
 * a separate migration path and temporarily retains its existing resolver.
 */
@Slf4j
@RestController
public class PluginProxyController {

    /** 转发的两条前缀（BFF 侧） */
    private static final String WEB_PREFIX = "/web";

    private static final String ASSET_PREFIX = "/web/plugin-assets/";

    private static final String API_PREFIX = "/web/plugin/";

    /**
     * 插件接口转发的读超时（毫秒），可被配置项覆盖。
     *
     * <p>默认 180000（3 分钟）：历史值是 45000，但 AI 分平台适配实测单篇 63 秒以上，之所以要能配：AI 分平台适配这类「一次请求内让模型现写内容」
     * 的接口，实测单篇 63 秒以上，固定 45 秒必然 Read timed out → 上游其实成功了，
     * 前端却只拿到 502。线上通过 {@code bff.plugin.proxy.read-timeout-ms} 调大即可，
     * 不必再改代码发版。
     */
    private static volatile int pluginReadTimeoutMs = 180000;

    /**
     * 由配置注入读超时；未配置时保持 45000，不改变其它插件接口的既有行为。
     *
     * @param readTimeoutMs 读超时毫秒数
     */
    @org.springframework.beans.factory.annotation.Value("${bff.plugin.proxy.read-timeout-ms:180000}")
    public void setPluginReadTimeoutMs(int readTimeoutMs) {
        if (readTimeoutMs > 0) {
            pluginReadTimeoutMs = readTimeoutMs;
            log.info("[PluginProxy] 读超时配置为 {} ms", readTimeoutMs);
        }
    }

    private final BasePluginCenterUrlResolver basePluginCenterUrlResolver;

    private final RestTemplate restTemplate;

    private final PluginServiceResolver pluginServiceResolver;

    @Autowired
    public PluginProxyController(PluginServiceResolver pluginServiceResolver, BasePluginCenterUrlResolver basePluginCenterUrlResolver) {
        this(pluginServiceResolver, basePluginCenterUrlResolver, createRestTemplate());
    }

    PluginProxyController(PluginServiceResolver pluginServiceResolver, BasePluginCenterUrlResolver basePluginCenterUrlResolver,
                          RestTemplate restTemplate) {
        this.pluginServiceResolver = java.util.Objects.requireNonNull(pluginServiceResolver);
        this.basePluginCenterUrlResolver = java.util.Objects.requireNonNull(basePluginCenterUrlResolver);
        this.restTemplate = restTemplate;
    }

    /**
     * 插件前端产物：公开只读，透传缓存头。
     */
    @RequestMapping(value = "/web/plugin-assets/{code}/{version}/**",
            method = {RequestMethod.GET, RequestMethod.HEAD})
    public ResponseEntity<byte[]> proxyAsset(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String rest = uri.startsWith(WEB_PREFIX) ? uri.substring(WEB_PREFIX.length()) : uri;
        if (!isSafePath(rest)) {
            log.warn("插件产物转发被拒绝（非法路径）：{}", uri);
            return jsonError(400, "插件产物路径非法");
        }
        BasePluginCenterUrlResolver.Resolved resolved = basePluginCenterUrlResolver.resolve();
        String target = resolved.url() + rest;
        HttpHeaders headers = new HttpHeaders();
        HttpMethod method = HttpMethod.valueOf(request.getMethod().toUpperCase());
        return exchange(target, resolved.source(), method, new HttpEntity<>(headers), false);
    }

    /**
     * 插件自有接口：泛化转发，透传鉴权与请求体。
     */
    @RequestMapping(value = "/web/plugin/{code}/**",
            method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<byte[]> proxyPluginApi(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String rest = uri.startsWith(WEB_PREFIX) ? uri.substring(WEB_PREFIX.length()) : uri;
        return forwardPluginApi(request, rest);
    }

    /**
     * ERP migration aliases. Existing clients may still call either the old
     * BFF path ({@code /web/erp/**}) or the original API path
     * ({@code /api/erp/**}); both route directly to the ERP service.
     */
    @RequestMapping(value = {"/web/erp/**", "/api/erp/**"},
            method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<byte[]> proxyLegacyErpApi(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String prefix = uri.startsWith("/web/erp") ? "/web/erp" : "/api/erp";
        String suffix = uri.substring(prefix.length());
        String normalizedSuffix = suffix.isEmpty() ? "/" : (suffix.startsWith("/") ? suffix : "/" + suffix);
        return forwardPluginApi(request, "/plugin/erp" + normalizedSuffix);
    }

    /**
     * PM 迁移期兼容入口：旧客户端仍请求 {@code /web/pm/**} 时，改写到
     * PM 服务的插件 API 适配端点。
     */
    @RequestMapping(value = "/web/pm/**",
            method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<byte[]> proxyLegacyPmApi(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String suffix = uri.startsWith("/web/pm") ? uri.substring("/web/pm".length()) : "/";
        String rest = "/plugin/pm" + (suffix.isEmpty() ? "/" : suffix);
        return forwardPluginApi(request, rest);
    }

    private ResponseEntity<byte[]> forwardPluginApi(HttpServletRequest request, String rest) {
        String uri = request.getRequestURI();
        if (!isSafePath(rest)) {
            log.warn("插件接口转发被拒绝（非法路径）：{}", uri);
            return jsonError(400, "插件接口路径非法");
        }

        if (!rest.startsWith("/plugin/")) return jsonError(400, "插件接口路径非法");
        String code = rest.substring("/plugin/".length()).split("/", 2)[0];
        PluginServiceResolver.Resolved resolved;
        try {
            resolved = pluginServiceResolver.resolve(code);
        } catch (PluginServiceResolver.ResolutionException ex) {
            return jsonError(ex.status(), ex.getMessage());
        }
        HttpHeaders headers = new HttpHeaders();
        copyHeader(request, headers, HttpHeaders.AUTHORIZATION);
        copyHeader(request, headers, "token");
        copyHeader(request, headers, "X-Org-Id");
        copyHeader(request, headers, HttpHeaders.ACCEPT);
        String contentType = request.getContentType();
        if (contentType != null && !contentType.isBlank()) {
            try {
                headers.setContentType(MediaType.parseMediaType(contentType));
            } catch (Exception ignored) {
                headers.setContentType(MediaType.APPLICATION_JSON);
            }
        }

        HttpMethod method = HttpMethod.valueOf(request.getMethod().toUpperCase());
        HttpEntity<?> entity;
        if (method == HttpMethod.GET) {
            entity = new HttpEntity<>(headers);
        } else if (request instanceof MultipartHttpServletRequest multipartRequest) {
            // Servlet 容器已解析 multipart 后，原始输入流不再能可靠代表完整请求体。
            // 重新组装字段和文件，并让 RestTemplate 生成新的合法 boundary。
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            MultiValueMap<String, Object> multipartBody = new LinkedMultiValueMap<>();
            for (Map.Entry<String, String[]> entry : multipartRequest.getParameterMap().entrySet()) {
                for (String value : entry.getValue()) {
                    multipartBody.add(entry.getKey(), value);
                }
            }
            for (Map.Entry<String, List<MultipartFile>> entry
                    : multipartRequest.getMultiFileMap().entrySet()) {
                for (MultipartFile file : entry.getValue()) {
                    multipartBody.add(entry.getKey(), file.getResource());
                }
            }
            entity = new HttpEntity<>(multipartBody, headers);
        } else {
            try {
                entity = new HttpEntity<>(request.getInputStream().readAllBytes(), headers);
            } catch (IOException e) {
                log.error("读取插件接口请求体失败，uri: {}, err: {}", uri, e.getMessage());
                return jsonError(400, "插件接口请求体读取失败");
            }
        }

        String apiPath = "im".equals(code) ? rest.substring("/plugin/im".length())
                : "aicode".equals(code) && "aicode-transition".equals(resolved.source())
                ? rest.substring("/plugin".length()) : rest;
        String target = resolved.url() + apiPath;
        if (request.getQueryString() != null && !request.getQueryString().isEmpty()) {
            target = target + "?" + request.getQueryString();
        }
        return exchange(target, resolved.source(), method, entity, true);
    }

    private ResponseEntity<byte[]> exchange(String target, String source, HttpMethod method,
                                            HttpEntity<?> entity, boolean jsonDefaultContentType) {
        try {
            ResponseEntity<byte[]> response = restTemplate.exchange(URI.create(target), method, entity, byte[].class);
            HttpHeaders responseHeaders = new HttpHeaders();
            MediaType contentType = response.getHeaders().getContentType();
            if (contentType != null) {
                responseHeaders.setContentType(contentType);
            } else if (jsonDefaultContentType) {
                responseHeaders.setContentType(MediaType.APPLICATION_JSON);
            }
            String cacheControl = response.getHeaders().getFirst(HttpHeaders.CACHE_CONTROL);
            if (cacheControl != null && !cacheControl.isBlank()) {
                responseHeaders.set(HttpHeaders.CACHE_CONTROL, cacheControl);
            }
            String contentTypeOptions = response.getHeaders().getFirst("X-Content-Type-Options");
            if (contentTypeOptions != null && !contentTypeOptions.isBlank()) {
                responseHeaders.set("X-Content-Type-Options", contentTypeOptions);
            }
            return ResponseEntity.status(response.getStatusCode())
                    .headers(responseHeaders)
                    .body(response.getBody());
        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            // 上游 4xx/5xx 原样回传（含插件未启用 423、产物不存在 404 的 JSON 体）
            HttpHeaders responseHeaders = new HttpHeaders();
            HttpHeaders upstream = e.getResponseHeaders();
            MediaType contentType = upstream == null ? null : upstream.getContentType();
            responseHeaders.setContentType(contentType == null ? MediaType.APPLICATION_JSON : contentType);
            return ResponseEntity.status(e.getStatusCode()).headers(responseHeaders).body(e.getResponseBodyAsByteArray());
        } catch (Exception e) {
            log.error("转发插件请求失败 url={} : {}", target, e.toString(), e);
            if (!jsonDefaultContentType) basePluginCenterUrlResolver.recordFailure(target, source, e);
            return jsonError(502, "插件服务调用失败");
        }
    }

    private void copyHeader(HttpServletRequest request, HttpHeaders headers, String name) {
        String value = request.getHeader(name);
        if (value != null && !value.isBlank()) {
            headers.set(name, value);
        }
    }

    /**
     * 转发目标路径安全校验：拒绝穿越、反斜杠、重复斜杠与编码绕过。
     *
     * @param path 已去掉 {@code /web} 前缀的转发路径，如 {@code /plugin/mall-coupon/list}
     */
    static boolean isSafePath(String path) {
        if (path == null || path.isBlank() || !path.startsWith("/")) {
            return false;
        }
        String decoded;
        try {
            decoded = URLDecoder.decode(path.replace("+", "%2B"), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return false;
        }
        if (decoded.contains("..") || decoded.contains("\\") || decoded.contains("//") || decoded.contains("%") || decoded.contains("?") || decoded.contains("#")) {
            return false;
        }
        return decoded.chars().noneMatch(ch -> ch < 0x20 || ch == 0x7f);
    }

    /**
     * 拼接转发目标 URL（便于单测）。
     */
    static String buildTargetUrl(String baseUrl, String path, String query) {
        StringBuilder target = new StringBuilder(baseUrl).append(path);
        if (query != null && !query.isEmpty()) {
            target.append('?').append(query);
        }
        return target.toString();
    }

    private ResponseEntity<byte[]> jsonError(int status, String message) {
        String body = "{\"code\":" + status + ",\"message\":\"" + message + "\",\"data\":null}";
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 创建转发用的 RestTemplate（包可见：单测直接验证它带不带 ErrorHandler）。
     *
     * @return 转发客户端
     */
    static RestTemplate createRestTemplate() {
        // 必须走工厂：出厂默认的 DefaultResponseErrorHandler 会把上游 4xx/5xx 当异常抛，
        // 业务错误码（423 插件未启用 / 404 产物不存在...）会退化成 502。见 ProxyRestTemplateFactory。
        // 读超时走可配置项（默认 45000）：AI 分平台适配单篇实测 63 秒+，死守 45 秒会假 502。
        return ProxyRestTemplateFactory.create(5000, pluginReadTimeoutMs);
    }

    /**
     * 供单测断言使用的前缀常量。
     */
    static String assetPrefix() {
        return ASSET_PREFIX;
    }

    static String apiPrefix() {
        return API_PREFIX;
    }
}
