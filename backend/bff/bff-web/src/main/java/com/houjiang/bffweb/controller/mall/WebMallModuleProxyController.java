package com.houjiang.bffweb.controller.mall;

import com.houjiang.bffweb.support.MallBaseUrlResolver;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * 商城内核模块直通（BFF {@code /web/mall/module/{module}/**} → mall {@code /mall/{module}/**}）。
 *
 * <p>背景：返利结算 / 分销推广 / 积分商城三个插件的后端<b>不是 PF4J 插件</b>，
 * 而是 mall 主服务的原生 Controller（{@code /mall/rebate/**}、{@code /mall/distribution/**}、
 * {@code /mall/points-tool/**}），既没有 {@code /plugin/{code}/**} 路由，也不该为它们
 * 各写一个 DTO 转发层。它们的插件形态只承载前端页面与菜单（与 rider 同构：
 * 前端插件 + 宿主直通后端）。
 *
 * <p>为什么不开通配：直通通道等价于把 mall 的这部分接口面暴露到 BFF 之外，
 * 因此 module 走<b>显式白名单</b>，不在清单内一律 400 —— 新增模块必须在这里登记，
 * 顺带留一条审计痕迹。BFF 自身的登录鉴权仍然罩在这条通道之上。
 *
 * 旧商城别名入口已经移除；所有调用方必须使用显式的
 * {@code /web/mall/module/**} 路由，本控制器只做「新插件 → 商城内核模块」的干净转发。
 *
 * @author Amos
 */
@Slf4j
@RestController
@RequestMapping("/web/mall/module")
public class WebMallModuleProxyController {

    /**
     * BFF 侧前缀。
     */
    private static final String WEB_PREFIX = "/web/mall/module";

    /**
     * 转发到 mall 的目标前缀（module 段原样拼接，因此这里只到 {@code /mall}）。
     */
    private static final String TARGET_PREFIX = "/mall";

    /**
     * 允许直通的商城内核模块（对应 mall 的 {@code /mall/<module>} 一组 Controller）。
     *
     * <p>{@code points-tool} 是积分商城工具的真实路径段（{@code PointsToolController}），
     * 与插件码 {@code mall-points} 不同名，别写成 points。
     */
    private static final Set<String> ALLOWED_MODULES = Set.of("rebate", "distribution", "points-tool");

    private static final int CONNECT_TIMEOUT_MS = 5000;

    private static final int READ_TIMEOUT_MS = 45000;

    private final MallBaseUrlResolver mallBaseUrlResolver;

    private final RestTemplate restTemplate;

    /**
     * 构造转发控制器（Spring 装配）。
     *
     * @param mallBaseUrlResolver mall 基址解析
     */
    @Autowired
    public WebMallModuleProxyController(MallBaseUrlResolver mallBaseUrlResolver) {
        this(mallBaseUrlResolver, createRestTemplate());
    }

    /**
     * 构造转发控制器（便于单测注入 RestTemplate）。
     *
     * @param mallBaseUrlResolver mall 基址解析
     * @param restTemplate        转发用的 RestTemplate
     */
    WebMallModuleProxyController(MallBaseUrlResolver mallBaseUrlResolver, RestTemplate restTemplate) {
        this.mallBaseUrlResolver = java.util.Objects.requireNonNull(
                mallBaseUrlResolver, "MallBaseUrlResolver 不能为空");
        this.restTemplate = restTemplate;
    }

    /**
     * 直通转发：GET/DELETE 不带体，其余透传原始字节。
     */
    @RequestMapping(value = "/**", method = {RequestMethod.GET, RequestMethod.POST,
            RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH})
    public ResponseEntity<byte[]> forward(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String suffix = uri.startsWith(WEB_PREFIX) ? uri.substring(WEB_PREFIX.length()) : "";
        suffix = suffix.startsWith("/") ? suffix.substring(1) : suffix;
        int slash = suffix.indexOf('/');
        String module = slash < 0 ? suffix : suffix.substring(0, slash);
        String rest = slash < 0 ? "" : suffix.substring(slash);

        if (!ALLOWED_MODULES.contains(module)) {
            log.warn("商城模块直通被拒绝（模块未登记）：module={}, uri={}", module, uri);
            return jsonError(HttpStatus.BAD_REQUEST, "不支持的商城模块: " + module);
        }

        String target = mallBaseUrlResolver.resolve().url() + TARGET_PREFIX + "/" + module + rest;
        if (request.getQueryString() != null && !request.getQueryString().isBlank()) {
            target = target + "?" + request.getQueryString();
        }

        HttpHeaders headers = new HttpHeaders();
        copyHeader(request, headers, HttpHeaders.AUTHORIZATION);
        copyHeader(request, headers, "token");
        copyHeader(request, headers, "Token");
        copyHeader(request, headers, "X-Org-Id");
        String contentType = request.getContentType();
        if (contentType != null && !contentType.isBlank()) {
            try {
                headers.setContentType(MediaType.parseMediaType(contentType));
            } catch (Exception ignored) {
                headers.setContentType(MediaType.APPLICATION_JSON);
            }
        }

        HttpMethod method = HttpMethod.valueOf(request.getMethod().toUpperCase());
        HttpEntity<byte[]> entity;
        if (method == HttpMethod.GET || method == HttpMethod.DELETE) {
            entity = new HttpEntity<>(headers);
        } else {
            try {
                entity = new HttpEntity<>(request.getInputStream().readAllBytes(), headers);
            } catch (IOException e) {
                log.error("读取商城模块请求体失败，uri: {}, err: {}", uri, e.getMessage());
                return jsonError(HttpStatus.BAD_REQUEST, "请求体读取失败");
            }
        }

        try {
            ResponseEntity<byte[]> response = restTemplate.exchange(target, method, entity, byte[].class);
            HttpHeaders responseHeaders = new HttpHeaders();
            MediaType responseType = response.getHeaders().getContentType();
            responseHeaders.setContentType(responseType == null ? MediaType.APPLICATION_JSON : responseType);
            return ResponseEntity.status(response.getStatusCode()).headers(responseHeaders).body(response.getBody());
        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            // 上游 4xx/5xx 原样回传，保留字段级校验明细
            HttpHeaders responseHeaders = new HttpHeaders();
            HttpHeaders upstream = e.getResponseHeaders();
            MediaType responseType = upstream == null ? null : upstream.getContentType();
            responseHeaders.setContentType(responseType == null ? MediaType.APPLICATION_JSON : responseType);
            return ResponseEntity.status(e.getStatusCode()).headers(responseHeaders)
                    .body(e.getResponseBodyAsByteArray());
        } catch (Exception e) {
            log.error("商城模块直通转发失败 target={}, err={}", target, e.getMessage());
            return jsonError(HttpStatus.BAD_GATEWAY, "商城服务暂不可用");
        }
    }

    private static void copyHeader(HttpServletRequest request, HttpHeaders headers, String name) {
        String value = request.getHeader(name);
        if (value != null && !value.isBlank()) {
            headers.set(name, value);
        }
    }

    private static ResponseEntity<byte[]> jsonError(HttpStatus status, String message) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8));
        String body = "{\"code\":0,\"message\":\"" + message + "\",\"data\":null}";
        return ResponseEntity.status(status).headers(headers).body(body.getBytes(StandardCharsets.UTF_8));
    }

    private static RestTemplate createRestTemplate() {
        org.springframework.http.client.SimpleClientHttpRequestFactory factory =
                new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(CONNECT_TIMEOUT_MS);
        factory.setReadTimeout(READ_TIMEOUT_MS);
        return new RestTemplate(factory);
    }
}
