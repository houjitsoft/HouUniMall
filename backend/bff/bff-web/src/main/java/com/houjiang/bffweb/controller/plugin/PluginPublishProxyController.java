package com.houjiang.bffweb.controller.plugin;

import com.houjiang.bffweb.support.BasePluginCenterUrlResolver;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
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
 * 插件发布接口转发（BFF {@code /web/plugin-center/publish/**} → base {@code /base/plugin-center/publish/**}）。
 *
 * <p>为什么不用 {@code PluginCenterServiceFeign}：Feign 是 JSON 客户端，<b>无法承载 multipart</b>。
 * 这里与 {@link PluginProxyController} 保持一致，走 {@code RestTemplate} 字节流转发，
 * 服务地址解析顺序同为：Nacos(base) → {@code feign.base.url} → {@code 127.0.0.1:8214}。
 *
 * <p><b>超时单独配 180s</b>：64MB 制品在弱网下的上传耗时远超普通接口，
 * 不能复用 {@code readTimeout=45000}，否则大包必然中途断连。
 *
 * @author Amos
 * @since M1
 */
@Slf4j
@RestController
@RequestMapping("/web/plugin-center/publish")
public class PluginPublishProxyController {

    /**
     * BFF 侧前缀。
     */
    private static final String WEB_PREFIX = "/web/plugin-center/publish";

    /**
     * 转发到 base 的目标前缀。
     */
    private static final String TARGET_PREFIX = "/base/plugin-center/publish";

    /**
     * 连接超时（毫秒）。
     */
    private static final int CONNECT_TIMEOUT_MS = 5000;

    /**
     * 读取超时（毫秒）：64MB 制品 + 弱网，必须比普通接口长得多。
     */
    private static final int READ_TIMEOUT_MS = 180000;

    private final BasePluginCenterUrlResolver basePluginCenterUrlResolver;

    private final RestTemplate restTemplate;

    /**
     * 构造转发控制器（Spring 装配）。
     *
     * <p>基址解析交给共用的 {@link BasePluginCenterUrlResolver}：<b>配置 → Nacos → 本机兜底</b>，
     * 与既有 Feign 走同一条路。详见 {@link BasePluginCenterUrlResolver} 类注释。
     *
     * @param basePluginCenterUrlResolver base 基址解析
     */
    @Autowired
    public PluginPublishProxyController(BasePluginCenterUrlResolver basePluginCenterUrlResolver) {
        this(basePluginCenterUrlResolver, createRestTemplate());
    }

    /**
     * 构造转发控制器（便于单测注入 RestTemplate）。
     *
     * @param basePluginCenterUrlResolver base 基址解析
     * @param restTemplate        转发用的 RestTemplate
     */
    PluginPublishProxyController(BasePluginCenterUrlResolver basePluginCenterUrlResolver, RestTemplate restTemplate) {
        this.basePluginCenterUrlResolver = java.util.Objects.requireNonNull(
                basePluginCenterUrlResolver, "BasePluginCenterUrlResolver 不能为空");
        this.restTemplate = restTemplate;
    }

    /**
     * 构造转发控制器（显式直连地址，不使用注册中心）。
     *
     * <p>只用于「地址已知」的场景：没有注册中心实例，解析结果必然是配置优先，
     * 不会被注册中心里的地址带偏。
     *
     * @param configuredBaseUrl base 直连地址
     * @param restTemplate      转发用的 RestTemplate
     */
    PluginPublishProxyController(String configuredBaseUrl, RestTemplate restTemplate) {
        this(new BasePluginCenterUrlResolver(configuredBaseUrl, null), restTemplate);
    }

    /**
     * 泛化转发：GET 不带体，其余透传原始字节（multipart 靠原始 Content-Type 里的 boundary 保持可用）。
     */
    @RequestMapping(value = "/**", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<byte[]> forward(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String suffix = uri.startsWith(WEB_PREFIX) ? uri.substring(WEB_PREFIX.length()) : "";
        BasePluginCenterUrlResolver.Resolved resolved = basePluginCenterUrlResolver.resolve();
        String target = resolved.url() + TARGET_PREFIX + (suffix.startsWith("/") || suffix.isEmpty() ? suffix : "/" + suffix);
        if (request.getQueryString() != null && !request.getQueryString().isEmpty()) {
            target = target + "?" + request.getQueryString();
        }

        HttpHeaders headers = new HttpHeaders();
        copyHeader(request, headers, HttpHeaders.AUTHORIZATION);
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
                log.error("读取发布请求体失败，uri: {}, err: {}", uri, e.getMessage());
                return jsonError(400, "请求体读取失败");
            }
        }

        try {
            ResponseEntity<byte[]> response = restTemplate.exchange(target, method, entity, byte[].class);
            HttpHeaders responseHeaders = new HttpHeaders();
            MediaType responseType = response.getHeaders().getContentType();
            responseHeaders.setContentType(responseType == null ? MediaType.APPLICATION_JSON : responseType);
            return ResponseEntity.status(response.getStatusCode()).headers(responseHeaders).body(response.getBody());
        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            // 上游 4xx/5xx 原样回传，保留字段级校验明细（清单校验失败时前端要靠它逐字段提示）
            HttpHeaders responseHeaders = new HttpHeaders();
            HttpHeaders upstream = e.getResponseHeaders();
            MediaType responseType = upstream == null ? null : upstream.getContentType();
            responseHeaders.setContentType(responseType == null ? MediaType.APPLICATION_JSON : responseType);
            return ResponseEntity.status(e.getStatusCode())
                    .headers(responseHeaders)
                    .body(e.getResponseBodyAsByteArray());
        } catch (Exception e) {
            log.error("转发 base 失败 url={} : {}", target, e.toString(), e);
            basePluginCenterUrlResolver.recordFailure(target, resolved.source(), e);
            return jsonError(502, "插件发布服务调用失败");
        }
    }

    private void copyHeader(HttpServletRequest request, HttpHeaders headers, String name) {
        String value = request.getHeader(name);
        if (value != null && !value.isBlank()) {
            headers.set(name, value);
        }
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
        // 业务错误码（3002/3007/3008...）会全部退化成 502。见 ProxyRestTemplateFactory。
        return ProxyRestTemplateFactory.create(CONNECT_TIMEOUT_MS, READ_TIMEOUT_MS);
    }
}
