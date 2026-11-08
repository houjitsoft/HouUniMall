package com.houjiang.bffweb.controller.plugin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.houjiang.bffweb.support.PluginServiceResolver;
import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

    /**
     * AI 网关（FreeLLMAPI 独立容器）的平台侧只读通道。
     *
     * <p><b>为什么需要它</b>：网关的管理接口（模型目录、供应商密钥、路由、用量、日志）
     * 都要求网关自己的会话令牌。把令牌放进插件前端等于把凭据发给浏览器，
     * 任何拿到页面的人都能直接调用网关管理接口。正确做法是凭据只留在服务端，
     * 浏览器走平台通道 {@code /web/aigateway/**}，由这里代为调用并附加凭据。
     *
     * <p><b>凭据从哪来</b>：部署方把网关管理员账号放进宿主机私密 env
     * （{@code houcloud.plugin.aigateway.admin-email} / {@code admin-password}），
     * 这里用它登录换取会话令牌并缓存。网关会话有效期 30 天，令牌过期后自动重新登录。
     * 未配置账号时本控制器整体关闭（返回 503 并说明原因），
     * <b>不会</b>退化成无鉴权直连，也不会把凭据下发给浏览器。
     *
     * <p><b>只读边界</b>：本通道只暴露平台展示所需的只读接口。写操作（改密钥、
     * 改路由、轮换统一 API 密钥）一律不在此代理，避免平台页面成为绕过独立账号
     * 二次确认流程的旁路；这类操作仍在网关自己的管理控制台完成。
     *
     * @author Amos
     */
@Slf4j
@RestController
public class WebAigatewayController {

    /** 网关管理接口前缀（FreeLLMAPI 自身的 /api/**）。 */
    private static final String GATEWAY_API_PREFIX = "/api";

    /** 允许代理的只读路径白名单：模型、供应商、路由、健康、用量、日志。 */
    private static final Map<String, String> READONLY_ROUTES = Map.ofEntries(
            Map.entry("/api/analytics/summary", ""),
            Map.entry("/api/analytics/by-model", ""),
            Map.entry("/api/analytics/by-platform", ""),
            Map.entry("/api/analytics/timeline", ""),
            Map.entry("/api/analytics/error-distribution", ""),
            Map.entry("/api/models", ""),
            Map.entry("/api/keys", ""),
            Map.entry("/api/health", ""),
            Map.entry("/api/fallback/routing", ""),
            Map.entry("/api/fallback/quota-forecast", ""),
            Map.entry("/api/status/providers", ""),
            Map.entry("/api/logs", ""),
            Map.entry("/api/settings/version", "")
    );

    private final PluginServiceResolver pluginServiceResolver;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String adminEmail;
    private final String adminPassword;

    /** 网关会话令牌缓存；过期后重新登录换取。 */
    private volatile String cachedToken;

    /**
     * 网关管理员账号：只从环境变量读取，不进 Nacos、不进仓库、不进镜像。
     *
     * <p>Spring 的 relaxed binding 会把环境变量 {@code HOU_PLUGIN_AIGATEWAY_ADMIN_EMAIL}
     * 映射到 {@code hou.plugin.aigateway.admin-email}，所以这里用 relaxed 形式取值；
     * 部署方把宿主机私密 env 传给容器即可，密钥不出现在配置中心。
     */
    @Autowired
    public WebAigatewayController(PluginServiceResolver pluginServiceResolver,
                                  @Value("${hou.plugin.aigateway.admin-email:}") String adminEmail,
                                  @Value("${hou.plugin.aigateway.admin-password:}") String adminPassword) {
        this(pluginServiceResolver, createRestTemplate(), adminEmail, adminPassword);
    }
    WebAigatewayController(PluginServiceResolver pluginServiceResolver,
                           RestTemplate restTemplate,
                           String adminEmail,
                           String adminPassword) {
        this.pluginServiceResolver = pluginServiceResolver;
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
        this.adminEmail = adminEmail == null ? "" : adminEmail.trim();
        this.adminPassword = adminPassword == null ? "" : adminPassword;
    }

    /**
     * 走工厂而不是 {@code new RestTemplate()}：默认的 DefaultResponseErrorHandler
     * 会把上游 4xx 当异常抛出，网关的 401/403 会被错误地退化成 502。
     */
    static RestTemplate createRestTemplate() {
        return ProxyRestTemplateFactory.create(5000, 30000);
    }

    /**
     * 代理网关只读管理接口。
     *
     * <p>路径取请求 URI 的 {@code /web/aigateway} 之后部分，必须精确命中白名单，
     * 且方法必须是 GET——同一个路径上 GET 与 POST 语义不同
     * （例如 {@code /api/keys}：GET 读取已配置密钥，POST 新增密钥），
     * 只按路径放行会把写接口一起放开。
     */
    @RequestMapping(value = "/web/aigateway/**",
            method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<byte[]> proxyGatewayReadApi(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String prefix = "/web/aigateway";
        String suffix = uri.startsWith(prefix) ? uri.substring(prefix.length()) : "";
        String query = request.getQueryString();
        String target = GATEWAY_API_PREFIX + (suffix.isEmpty() ? "" : suffix)
                + (query == null || query.isBlank() ? "" : "?" + query);
        // 白名单与只读方法先判：写路径即使配置齐全也必须被挡住，不能因为缺账号而变成 503
        // ——那会让调用方误以为是配置问题，而不是「该接口不允许走平台通道」。
        if (!READONLY_ROUTES.containsKey(stripQuery(target))) {
            log.warn("AI 网关接口转发被拒绝（不在只读白名单）：{}", uri);
            return jsonError(403, "该网关接口不允许通过平台通道访问");
        }
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            log.warn("AI 网关接口转发被拒绝（非只读方法）：{} {}", request.getMethod(), uri);
            return jsonError(403, "平台通道只开放只读接口");
        }
        if (adminEmail.isEmpty() || adminPassword.isEmpty()) {
            return jsonError(503, "AI 网关管理通道未配置账号，请由部署方注入网关管理员账号");
        }
        try {
            PluginServiceResolver.Resolved resolved = pluginServiceResolver.resolve("aigateway");
            ResponseEntity<byte[]> response = callWithSession(resolved.url(), target, request);
            HttpHeaders out = new HttpHeaders();
            out.setContentType(MediaType.APPLICATION_JSON);
            return new ResponseEntity<>(response.getBody(), out, response.getStatusCode());
        } catch (PluginServiceResolver.ResolutionException ex) {
            return jsonError(ex.status(), ex.getMessage());
        } catch (Exception ex) {
            log.error("AI 网关接口转发失败: {}", target, ex);
            return jsonError(502, "AI 网关暂不可用: " + ex.getMessage());
        }
    }

    /**
     * 带网关会话调用；会话失效（401）时重新登录再试一次。
     *
     * <p>网关会话有效期 30 天，这里不假设它一直有效。
     */
    private ResponseEntity<byte[]> callWithSession(String baseUrl, String target, HttpServletRequest request) {
        ResponseEntity<byte[]> response = callGateway(baseUrl, target, request, sessionToken(baseUrl));
        if (response.getStatusCode().value() == 401) {
            cachedToken = null;
            response = callGateway(baseUrl, target, request, sessionToken(baseUrl));
        }
        return response;
    }

    private ResponseEntity<byte[]> callGateway(String baseUrl, String target,
                                              HttpServletRequest request, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpMethod method = HttpMethod.valueOf(request.getMethod().toUpperCase());
        HttpEntity<String> entity = new HttpEntity<>(method == HttpMethod.GET ? null : "", headers);
        return restTemplate.exchange(baseUrl + target, method, entity, byte[].class);
    }

    /**
     * 取网关会话令牌：命中缓存直接用，否则用管理员账号登录换取。
     *
     * <p>账号密码只在服务端使用，不写日志、不返回给浏览器。
     */
    private synchronized String sessionToken(String baseUrl) {
        String cached = cachedToken;
        if (cached != null && !cached.isBlank()) {
            return cached;
        }
        try {
            Map<String, Object> body = Map.of("email", adminEmail, "password", adminPassword);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            ResponseEntity<String> response = restTemplate.postForEntity(
                    baseUrl + "/api/auth/login", new HttpEntity<>(body, headers), String.class);
            JsonNode json = objectMapper.readTree(response.getBody());
            String token = json.path("token").asText("");
            if (token.isBlank()) {
                throw new IllegalStateException("网关登录未返回会话令牌");
            }
            cachedToken = token;
            return token;
        } catch (Exception ex) {
            log.warn("AI 网关登录失败: {}", ex.getMessage());
            throw new IllegalStateException("AI 网关登录失败", ex);
        }
    }

    private static String stripQuery(String path) {
        int index = path.indexOf('?');
        return index < 0 ? path : path.substring(0, index);
    }

    private ResponseEntity<byte[]> jsonError(int status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", status);
        body.put("message", message);
        try {
            return ResponseEntity.status(status)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(objectMapper.writeValueAsBytes(body));
        } catch (Exception ex) {
            return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON)
                    .body(("{\"code\":" + status + ",\"message\":\"" + message + "\"}").getBytes());
        }
    }
}
