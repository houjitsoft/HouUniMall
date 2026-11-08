package com.houjiang.bffweb.controller.plugin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.houjiang.bffweb.support.BasePluginCenterUrlResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

/**
 * 插件制品下载转发（BFF {@code /web/plugin-center/artifact/**} → base {@code /base/plugin-center/artifact/**}）。
 *
 * <p><b>身份字段一律不接前端传值</b>：本控制器只从请求体里取 {@code pluginCode} 与 {@code version}，
 * 其余字段（尤其是 {@code instanceId} / {@code userId}）全部丢弃，
 * 再把 {@code Authorization} 原样透传给 base —— 由 base 用登录 Token 解析出真实身份。
 *
 * <p><b>为什么身份解析不放在 BFF</b>：bff-web 按设计<b>不做 JWT 校验</b>
 * （{@code SecurityConfig} 里 {@code anyRequest().permitAll()}，认证全在下游 PAAS 服务），
 * 因此 BFF 进程内根本没有登录态可拿，硬要解析就得新增一次 auth 服务调用
 * （多一跳、多一个失败点，还会引入新依赖）。由已经验过 Token 的 base 注入是唯一正确的位置。
 *
 * <p>raw 走流式：64MB 制品不能读进内存再吐出去，
 * 用 {@code RestTemplate.execute + ResponseExtractor} 边收边写。
 *
 * @author Amos
 * @since M2
 */
@Slf4j
@RestController
@RequestMapping("/web/plugin-center/artifact")
public class PluginArtifactProxyController {

    /**
     * BFF 侧前缀。
     */
    private static final String WEB_PREFIX = "/web/plugin-center/artifact";

    /**
     * 连接超时（毫秒）。
     */
    private static final int CONNECT_TIMEOUT_MS = 5000;

    /**
     * 读取超时（毫秒）：64MB 制品 + 弱网，比普通接口长得多。
     */
    private static final int READ_TIMEOUT_MS = 180000;

    /**
     * 单测可见：转发给 base 的请求体。
     *
     * <p>只保留 pluginCode / version，其余（含攻击者塞进来的身份字段）一律丢弃。
     *
     * @param incoming 前端请求体，可为 null
     * @return 转发给 base 的请求体
     */
    static Map<String, Object> buildForwardBody(Map<String, Object> incoming) {
        Map<String, Object> forward = new LinkedHashMap<>();
        if (incoming != null) {
            Object pluginCode = incoming.get("pluginCode");
            Object version = incoming.get("version");
            if (incoming.get("artifactKind") != null) forward.put("artifactKind", incoming.get("artifactKind").toString());
            if (pluginCode != null) {
                forward.put("pluginCode", pluginCode.toString().trim());
            }
            if (version != null) {
                forward.put("version", version.toString().trim());
            }
        }
        return forward;
    }

    private final BasePluginCenterUrlResolver basePluginCenterUrlResolver;

    private final RestTemplate restTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 构造转发控制器（Spring 装配）。
     *
     * <p>基址解析交给共用的 {@link BasePluginCenterUrlResolver}：<b>配置 → Nacos → 本机兜底</b>。
     * 以前这里自己实现了「Nacos 优先」，与既有 Feign 走的配置地址是两条不同的路，
     * 是 502 的直接成因。详见 {@link BasePluginCenterUrlResolver} 类注释。
     *
     * @param basePluginCenterUrlResolver base 基址解析
     */
    @Autowired
    public PluginArtifactProxyController(BasePluginCenterUrlResolver basePluginCenterUrlResolver) {
        this(basePluginCenterUrlResolver, createRestTemplate());
    }

    /**
     * 构造转发控制器（便于单测注入 RestTemplate）。
     *
     * @param basePluginCenterUrlResolver base 基址解析
     * @param restTemplate        转发用的 RestTemplate
     */
    PluginArtifactProxyController(BasePluginCenterUrlResolver basePluginCenterUrlResolver, RestTemplate restTemplate) {
        this.basePluginCenterUrlResolver = java.util.Objects.requireNonNull(
                basePluginCenterUrlResolver, "BasePluginCenterUrlResolver 不能为空");
        this.restTemplate = restTemplate;
    }

    /**
     * 构造转发控制器（显式直连地址，不使用注册中心）。
     *
     * <p>只用于「地址已知」的场景（单测、嵌入式调用）：没有注册中心实例，
     * 解析结果必然是配置优先，行为完全确定，不会被注册中心里的地址带偏。
     *
     * @param configuredBaseUrl base 直连地址
     * @param restTemplate      转发用的 RestTemplate
     */
    PluginArtifactProxyController(String configuredBaseUrl, RestTemplate restTemplate) {
        this(new BasePluginCenterUrlResolver(configuredBaseUrl, null), restTemplate);
    }

    /**
     * 换取下载链接：只转发 pluginCode / version + 登录 Token。
     */
    @PostMapping("/download-url")
    public void downloadUrl(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, Object> incoming = readBody(request);
        Map<String, Object> forward = buildForwardBody(incoming);
        if (forward.get("pluginCode") == null || forward.get("version") == null) {
            writeError(response, 400, 3013, "pluginCode 与 version 不能为空");
            return;
        }
        byte[] body = objectMapper.writeValueAsBytes(forward);
        BasePluginCenterUrlResolver.Resolved resolved = basePluginCenterUrlResolver.resolve();
        String target = resolved.url() + "/base/plugin-center/artifact/download-url";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        copyHeader(request, headers, HttpHeaders.AUTHORIZATION);
        copyHeader(request, headers, "token");

        RequestCallback callback = clientRequest -> {
            clientRequest.getHeaders().addAll(headers);
            StreamUtils.copy(body, clientRequest.getBody());
        };
        ResponseExtractor<Void> extractor = clientResponse -> {
            copyToServletResponse(clientResponse, response, true);
            return null;
        };
        try {
            restTemplate.execute(target, HttpMethod.POST, callback, extractor);
        } catch (Exception e) {
            // 走到这里只可能是 IO 故障（连接拒绝 / 读超时）——上游返回的错误码已在 extractor 里透传，
            // 不会再变成异常。日志必须带 URL 与异常类型，否则 502 就是个黑盒。
            log.error("转发 base 失败 url={} : {}", target, e.toString(), e);
            basePluginCenterUrlResolver.recordFailure(target, resolved.source(), e);
            writeError(response, 502, 500, "插件制品服务调用失败");
        }
    }

    /**
     * 回源下载：流式转发字节与响应头（含 206 的 Content-Range）。
     */
    @GetMapping("/raw")
    public void raw(HttpServletRequest request, HttpServletResponse response) {
        String query = request.getQueryString();
        BasePluginCenterUrlResolver.Resolved resolved = basePluginCenterUrlResolver.resolve();
        String target = resolved.url() + "/base/plugin-center/artifact/raw"
                + (query == null || query.isEmpty() ? "" : "?" + query);
        RequestCallback callback = clientRequest -> {
            copyHeader(request, clientRequest.getHeaders(), HttpHeaders.RANGE);
            copyHeader(request, clientRequest.getHeaders(), HttpHeaders.IF_RANGE);
        };
        ResponseExtractor<Void> extractor = clientResponse -> {
            copyToServletResponse(clientResponse, response, false);
            return null;
        };
        try {
            restTemplate.execute(target, HttpMethod.GET, callback, extractor);
        } catch (Exception e) {
            log.error("转发 base 失败 url={} : {}", target, e.toString(), e);
            basePluginCenterUrlResolver.recordFailure(target, resolved.source(), e);
            response.setStatus(502);
        }
    }

    /**
     * 把上游响应原样写给客户端。
     *
     * @param clientResponse 上游响应
     * @param response       客户端响应
     * @param bufferJson     true 表示先读成字节（JSON，需要回填错误时用）；
     *                       false 表示直接流式搬运（二进制制品，绝不进内存）
     */
    private void copyToServletResponse(ClientHttpResponse clientResponse,
                                       HttpServletResponse response,
                                       boolean bufferJson) {
        try {
            response.setStatus(clientResponse.getStatusCode().value());
            HttpHeaders upstream = clientResponse.getHeaders();
            MediaType contentType = upstream.getContentType();
            response.setContentType(contentType == null
                    ? MediaType.APPLICATION_OCTET_STREAM_VALUE
                    : contentType.toString());
            if (!bufferJson) copyIfPresent(upstream, response, HttpHeaders.CONTENT_LENGTH);
            copyIfPresent(upstream, response, HttpHeaders.CONTENT_DISPOSITION);
            copyIfPresent(upstream, response, HttpHeaders.ACCEPT_RANGES);
            copyIfPresent(upstream, response, HttpHeaders.CONTENT_RANGE);
            copyIfPresent(upstream, response, HttpHeaders.CACHE_CONTROL);
            copyIfPresent(upstream, response, "X-Artifact-Md5");
            copyIfPresent(upstream, response, "X-Artifact-Sha256");
            copyIfPresent(upstream, response, "X-Artifact-Size");
            try (InputStream in = clientResponse.getBody()) {
                if (bufferJson) {
                    byte[] payload = in == null ? new byte[0] : in.readAllBytes();
                    var json = objectMapper.readTree(payload);
                    var data = json == null ? null : json.get("data");
                    if (data != null && data.isObject() && data.has("url")) {
                        String url = data.get("url").asText();
                        if (url.startsWith("/base/plugin-center/artifact/raw?token="))
                            ((com.fasterxml.jackson.databind.node.ObjectNode) data).put("url", url.replace("/base/plugin-center/", "/web/plugin-center/"));
                    }
                    byte[] outgoing = json == null ? payload : objectMapper.writeValueAsBytes(json);
                    response.setContentLength(outgoing.length);
                    response.getOutputStream().write(outgoing);
                } else if (in != null) {
                    StreamUtils.copy(in, response.getOutputStream());
                }
            }
            response.flushBuffer();
        } catch (IOException e) {
            log.error("回写客户端响应失败: {}", e.getMessage());
        }
    }

    private static void copyIfPresent(HttpHeaders upstream, HttpServletResponse response, String name) {
        String value = upstream.getFirst(name);
        if (value != null && !value.isBlank()) {
            response.setHeader(name, value);
        }
    }

    private static void copyHeader(HttpServletRequest request, HttpHeaders headers, String name) {
        String value = request.getHeader(name);
        if (value != null && !value.isBlank()) {
            headers.set(name, value);
        }
    }

    private Map<String, Object> readBody(HttpServletRequest request) {
        try {
            byte[] bytes = request.getInputStream().readAllBytes();
            if (bytes.length == 0) {
                return new LinkedHashMap<>();
            }
            return objectMapper.readValue(bytes, Map.class);
        } catch (IOException e) {
            log.warn("解析下载换票请求体失败: {}", e.getMessage());
            return new LinkedHashMap<>();
        }
    }

    private static void writeError(HttpServletResponse response, int status, int code, String message)
            throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("{\"code\":" + code + ",\"message\":\"" + message
                + "\",\"data\":null}");
        response.getWriter().flush();
    }

    /**
     * 创建转发用的 RestTemplate（包可见：单测直接验证它带不带 ErrorHandler）。
     *
     * @return 转发客户端
     */
    static RestTemplate createRestTemplate() {
        // 必须走工厂：出厂默认的 DefaultResponseErrorHandler 会把上游 4xx/5xx 当异常抛，
        // 业务错误码（3002/3007/3008...）会全部退化成 502。见 ProxyRestTemplateFactory。
        return ProxyRestTemplateFactory.create(CONNECT_TIMEOUT_MS, READ_TIMEOUT_MS);
    }
}
