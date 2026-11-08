package com.houjiang.bffweb.controller.plugin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.houjiang.bffweb.support.BasePluginCenterUrlResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

/**
 * 插件安装链路转发（BFF {@code /web/plugin-center/update/**} → base {@code /base/plugin-center/update/**}）。
 *
 * <p>M3 四个接口：{@code install} / {@code precheck} / {@code task/status} / {@code history}；
 * M4 补上 {@code upgrade} / {@code rollback}。
 *
 * <p><b>身份字段一律不接前端传值</b>：只转发业务字段（pluginCode / version / taskId / 分页参数），
 * {@code instanceId} / {@code userId} 全部丢弃，再把 {@code Authorization} 原样透传给 base ——
 * 由已经验过 Token 的 base 用登录态解析出真实站点实例。
 *
 * <p><b>为什么转发用白名单而不是整体透传</b>：整体透传等于把「服务端注入身份」这层保障
 * 交回给前端，攻击者只要往请求体里塞一个 instanceId 就能把安装动作挂到别的站点上。
 * 白名单多写几行，换来的是这层保障不可能被绕过。
 *
 * @author Amos
 * @since M3
 */
@Slf4j
@RestController
@RequestMapping("/web/plugin-center/update")
public class PluginUpdateProxyController {

    /**
     * 允许转发的字段白名单（键 → 值类型）。
     */
    private static final Map<String, ValueKind> FORWARD_FIELDS = Map.of(
            "pluginCode", ValueKind.TEXT,
            "version", ValueKind.TEXT,
            "targetVersion", ValueKind.TEXT,
            "taskId", ValueKind.TEXT,
            "page", ValueKind.NUMBER,
            "pageSize", ValueKind.NUMBER,
            // purgeData 用 TEXT 承载：前端传布尔，转成 "true"/"false" 文本后由 base 侧
            // 按严格白名单解析（只认 true/false/1/0/yes/no/on），避免「传错参数就删表」。
            "purgeData", ValueKind.TEXT);

    /**
     * 连接超时（毫秒）。
     */
    private static final int CONNECT_TIMEOUT_MS = 5000;

    /**
     * 读取超时（毫秒）：安装链路包含下载（64MB）与 schema 迁移，比普通接口长得多。
     */
    private static final int READ_TIMEOUT_MS = 180000;

    /**
     * 编排链「插件版本不存在或未发布」业务码（PluginDistErrorCode.ARTIFACT_NOT_FOUND）。
     *
     * <p>站点形态下这是<b>常态</b>而非错误：开源包不带插件版本快照
     * （{@code hou_market_plugin_version} 恒为空行），制品权威源在平台。
     * 编排链按本地版本表 resolveTargetVersion 必然打回 3009——
     * 此时唯一正确的安装路径是市场安装链
     * （{@code /base/plugin-center/market/install}，平台 acquire 换票 + 落盘 + 装载）。
     */
    private static final int MARKET_FALLBACK_TRIGGER_CODE = 3009;

    private final BasePluginCenterUrlResolver basePluginCenterUrlResolver;

    private final RestTemplate restTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 构造转发控制器（Spring 装配）。
     *
     * <p>基址解析交给共用的 {@link BasePluginCenterUrlResolver}：<b>配置 → Nacos → 本机兜底</b>，
     * 与既有 Feign 走同一条路。详见 {@link BasePluginCenterUrlResolver} 类注释。
     *
     * @param basePluginCenterUrlResolver base 基址解析
     */
    @Autowired
    public PluginUpdateProxyController(BasePluginCenterUrlResolver basePluginCenterUrlResolver) {
        this(basePluginCenterUrlResolver, createRestTemplate());
    }

    /**
     * 构造转发控制器（便于单测注入 RestTemplate）。
     *
     * @param basePluginCenterUrlResolver base 基址解析
     * @param restTemplate        转发用的 RestTemplate
     */
    PluginUpdateProxyController(BasePluginCenterUrlResolver basePluginCenterUrlResolver, RestTemplate restTemplate) {
        this.basePluginCenterUrlResolver = java.util.Objects.requireNonNull(
                basePluginCenterUrlResolver, "BasePluginCenterUrlResolver 不能为空");
        this.restTemplate = restTemplate;
    }

    /**
     * 构造转发控制器（显式直连地址，不使用注册中心）。
     *
     * <p>只用于「地址已知」的场景（单测、嵌入式调用）：没有注册中心实例，
     * 解析结果必然是配置优先，不会被注册中心里的地址带偏。
     *
     * @param configuredBaseUrl base 直连地址
     * @param restTemplate      转发用的 RestTemplate
     */
    PluginUpdateProxyController(String configuredBaseUrl, RestTemplate restTemplate) {
        this(new BasePluginCenterUrlResolver(configuredBaseUrl, null), restTemplate);
    }

    /**
     * 安装插件（编排链 + 市场链回退）。
     *
     * <p>2026-10-30：编排链在站点形态下本地版本快照恒为空，3009 是必经分支而非异常。
     * 站长从市场详情页 / 结算页 / 插件中心点「安装」，前端统一走 {@code /update/install}
     * （installAndWait 轮询协议）；BFF 在这里对 3009 自动回退市场安装链（平台 acquire
     * 换票 → 下载 → 校验 → 落盘 → 装载 → 登记安装态，2026-10-30 已在评估站实测走通），
     * 并把同步结果包装成编排终态 {@code {state: SUCCESS}}——前端无 taskId 时直接把
     * result 当终态渲染，协议零改动。
     *
     * <p>生产平台不受影响：平台本地版本表有数据，永远到不了 3009 分支；
     * 真出现 3009（版本行不存在，比如商品在售但版本仍是草稿）时市场链也会给出
     * 更准确的拒绝原因（未上架 / 无授权），语义只增不减。
     */
    @PostMapping("/install")
    public void install(HttpServletRequest request, HttpServletResponse response) throws IOException {
        forwardInstallWithMarketFallback(request, response);
    }

    /**
     * 升级插件（M4）。
     */
    @PostMapping("/upgrade")
    public void upgrade(HttpServletRequest request, HttpServletResponse response) throws IOException {
        forward("upgrade", request, response);
    }

    /**
     * 回滚插件（M4）。
     *
     * <p>走的是同一套白名单转发：回滚同样不能由前端指定站点实例，
     * 否则「回滚到别人的站点」就成了越权入口。
     */
    @PostMapping("/rollback")
    public void rollback(HttpServletRequest request, HttpServletResponse response) throws IOException {
        forward("rollback", request, response);
    }

    /**
     * 卸载插件（编排版）。
     *
     * <p>与 {@code /web/plugin-center/uninstall}（轻量链路）并存：
     * 本接口转发到 base 的 8 步卸载编排，会回收版本目录、清除迁移审计并留下任务流水。
     */
    @PostMapping("/uninstall")
    public void uninstall(HttpServletRequest request, HttpServletResponse response) throws IOException {
        forward("uninstall", request, response);
    }

    /**
     * 兼容性预检。
     */
    @PostMapping("/precheck")
    public void precheck(HttpServletRequest request, HttpServletResponse response) throws IOException {
        forward("precheck", request, response);
    }

    /**
     * 任务状态。
     */
    @PostMapping("/task/status")
    public void taskStatus(HttpServletRequest request, HttpServletResponse response) throws IOException {
        forward("task/status", request, response);
    }

    /**
     * 任务历史。
     */
    @PostMapping("/history")
    public void history(HttpServletRequest request, HttpServletResponse response) throws IOException {
        forward("history", request, response);
    }

    /**
     * 安装专用转发：编排链 3009 时回退市场安装链。
     *
     * <p>三个分支：
     * <ol>
     *   <li>编排链正常返回（含一切非 3009 的业务拒绝码）——原样回写，语义不变；</li>
     *   <li>编排链 3009 → 市场链成功（code=200）——包装成编排终态
     *       {@code data:{state:\"SUCCESS\", pluginCode, currentVersion, source:\"market\"}}；</li>
     *   <li>编排链 3009 → 市场链也拒绝——原样回写市场失败（code!=200 由前端 request
     *       拦截器抛错，catch 分支展示 message），比裸 3009 信息量更大。</li>
     * </ol>
     *
     * <p>市场链网络故障（BFF→base 断连）时回写编排链的原始 3009 响应：
     * 保留「版本不存在」的原语义，不叠加一层 502 把用户打懵。
     *
     * @param request  原始请求
     * @param response 客户端响应
     * @throws IOException 读写失败
     */
    private void forwardInstallWithMarketFallback(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, Object> incoming = readBody(request);
        Map<String, Object> forward = buildForwardBody(incoming);
        byte[] body = objectMapper.writeValueAsBytes(forward);
        BasePluginCenterUrlResolver.Resolved resolved = basePluginCenterUrlResolver.resolve();
        String updateTarget = resolved.url() + "/base/plugin-center/update/install";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        copyHeader(request, headers, HttpHeaders.AUTHORIZATION);
        copyHeader(request, headers, "token");

        byte[] updateResponse;
        try {
            updateResponse = postForBytes(updateTarget, headers, body);
        } catch (Exception e) {
            log.error("转发 base 失败 url={} : {}", updateTarget, e.toString(), e);
            basePluginCenterUrlResolver.recordFailure(updateTarget, resolved.source(), e);
            writeError(response, 502, 500, "插件安装服务调用失败");
            return;
        }

        if (!isMarketFallbackTriggered(updateResponse)) {
            writeJsonBytes(response, updateResponse);
            return;
        }

        String pluginCode = forward.get("pluginCode") == null ? "" : String.valueOf(forward.get("pluginCode"));
        log.info("编排链本地无版本快照（3009），回退市场安装链: plugin={}", pluginCode);
        String marketTarget = resolved.url() + "/base/plugin-center/market/install";
        byte[] marketResponse;
        try {
            marketResponse = postForBytes(marketTarget, headers, body);
        } catch (Exception e) {
            log.error("市场安装链调用失败 url={} : {}", marketTarget, e.toString(), e);
            // 市场链打不通时保留编排链的原语义，而不是让用户看两连跳的 502
            writeJsonBytes(response, updateResponse);
            return;
        }

        try {
            com.fasterxml.jackson.databind.JsonNode marketTree = objectMapper.readTree(marketResponse);
            int marketCode = marketTree == null ? -1 : marketTree.path("code").asInt(-1);
            // 成功码两套体系并存：base ResponseResult 成功=1（线上实测 market/install），
            // 历史上部分模块用 200。两个都认，避免「装上了但前端静默」。
            if (marketCode == 1 || marketCode == 200) {
                com.fasterxml.jackson.databind.JsonNode data = marketTree.path("data");
                Map<String, Object> wrapped = new LinkedHashMap<>();
                wrapped.put("state", "SUCCESS");
                wrapped.put("pluginCode", data.path("code").asText(pluginCode));
                wrapped.put("currentVersion", data.path("version").asText(""));
                wrapped.put("source", "market");
                wrapped.put("hostService", data.path("hostService").asText(""));
                wrapped.put("jarPath", data.path("jarPath").asText(""));
                wrapped.put("market", data);
                Map<String, Object> payload = new LinkedHashMap<>();
                payload.put("code", marketCode);
                payload.put("message", "操作成功");
                payload.put("data", wrapped);
                writeJsonBytes(response, objectMapper.writeValueAsBytes(payload));
                return;
            }
        } catch (IOException e) {
            log.warn("市场安装链响应解析失败，按原样回写: {}", e.getMessage());
        }
        // 市场链拒绝 / 响应异常：原样回写，前端 request 拦截器按 code!=200 抛错展示
        writeJsonBytes(response, marketResponse);
    }

    /**
     * 判断编排响应是否命中市场回退触发条件（code=3009 且响应可解析）。
     *
     * @param updateResponse 编排链响应体
     * @return true 表示应回退市场安装链
     */
    private boolean isMarketFallbackTriggered(byte[] updateResponse) {
        try {
            com.fasterxml.jackson.databind.JsonNode tree = objectMapper.readTree(updateResponse);
            return tree != null && tree.path("code").asInt(-1) == MARKET_FALLBACK_TRIGGER_CODE;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * POST 并把上游响应完整读回字节数组（不写客户端）。
     *
     * @param target  base 内部端点
     * @param headers 请求头（Authorization 等）
     * @param body    请求体
     * @return 上游响应体字节
     */
    private byte[] postForBytes(String target, HttpHeaders headers, byte[] body) {
        RequestCallback callback = clientRequest -> {
            clientRequest.getHeaders().addAll(headers);
            StreamUtils.copy(body, clientRequest.getBody());
        };
        return java.util.Objects.requireNonNull(
                restTemplate.execute(target, HttpMethod.POST, callback,
                        clientResponse -> clientResponse.getBody() == null
                                ? new byte[0]
                                : clientResponse.getBody().readAllBytes()),
                "上游响应缺失");
    }

    /**
     * 把上游 JSON 字节原样写给客户端（HTTP 200，业务码在响应体内）。
     *
     * @param response 客户端响应
     * @param payload  响应体字节
     */
    private void writeJsonBytes(HttpServletResponse response, byte[] payload) {
        response.setStatus(200);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        try {
            response.getOutputStream().write(payload);
            response.flushBuffer();
        } catch (IOException e) {
            log.error("回写客户端响应失败: {}", e.getMessage());
        }
    }

    /**
     * 按白名单构造转发请求体。
     *
     * <p>包可见：单测直接断言「身份字段被剥离」。
     *
     * @param incoming 前端请求体，可为 null
     * @return 转发给 base 的请求体
     */
    static Map<String, Object> buildForwardBody(Map<String, Object> incoming) {
        Map<String, Object> forward = new LinkedHashMap<>();
        if (incoming == null) {
            return forward;
        }
        for (Map.Entry<String, ValueKind> field : FORWARD_FIELDS.entrySet()) {
            Object value = incoming.get(field.getKey());
            if (value == null) {
                continue;
            }
            if (field.getValue() == ValueKind.TEXT) {
                String text = value.toString().trim();
                if (!text.isEmpty()) {
                    forward.put(field.getKey(), text);
                }
            } else {
                try {
                    forward.put(field.getKey(), Integer.parseInt(value.toString().trim()));
                } catch (NumberFormatException ignored) {
                    // 分页参数非法就按缺省处理，不因一个参数把整个请求打回
                }
            }
        }
        return forward;
    }

    /**
     * 转发到 base 的内部端点。
     *
     * @param path     内部端点路径（不含前缀）
     * @param request  原始请求
     * @param response 客户端响应
     * @throws IOException 读写失败
     */
    private void forward(String path, HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, Object> incoming = readBody(request);
        Map<String, Object> forward = buildForwardBody(incoming);
        byte[] body = objectMapper.writeValueAsBytes(forward);
        BasePluginCenterUrlResolver.Resolved resolved = basePluginCenterUrlResolver.resolve();
        String target = resolved.url() + "/base/plugin-center/update/" + path;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        copyHeader(request, headers, HttpHeaders.AUTHORIZATION);
        copyHeader(request, headers, "token");

        RequestCallback callback = clientRequest -> {
            clientRequest.getHeaders().addAll(headers);
            StreamUtils.copy(body, clientRequest.getBody());
        };
        ResponseExtractor<Void> extractor = clientResponse -> {
            copyToServletResponse(clientResponse, response);
            return null;
        };
        try {
            restTemplate.execute(target, HttpMethod.POST, callback, extractor);
        } catch (Exception e) {
            // 同上：只剩 IO 故障会走到这里，日志带 URL 与异常类型，便于一眼定位是哪一跳断了。
            log.error("转发 base 失败 url={} : {}", target, e.toString(), e);
            basePluginCenterUrlResolver.recordFailure(target, resolved.source(), e);
            writeError(response, 502, 500, "插件安装服务调用失败");
        }
    }

    /**
     * 把上游 JSON 响应原样写给客户端。
     *
     * @param clientResponse 上游响应
     * @param response       客户端响应
     */
    private void copyToServletResponse(ClientHttpResponse clientResponse, HttpServletResponse response) {
        try {
            response.setStatus(clientResponse.getStatusCode().value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            try (var in = clientResponse.getBody()) {
                byte[] payload = in == null ? new byte[0] : in.readAllBytes();
                response.getOutputStream().write(payload);
            }
            response.flushBuffer();
        } catch (IOException e) {
            log.error("回写客户端响应失败: {}", e.getMessage());
        }
    }

    private static void copyHeader(HttpServletRequest request, HttpHeaders headers, String name) {
        String value = request.getHeader(name);
        if (value != null && !value.isBlank()) {
            headers.set(name, value);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> readBody(HttpServletRequest request) {
        try {
            byte[] bytes = request.getInputStream().readAllBytes();
            if (bytes.length == 0) {
                return new LinkedHashMap<>();
            }
            return objectMapper.readValue(bytes, Map.class);
        } catch (IOException e) {
            log.warn("解析安装请求体失败: {}", e.getMessage());
            return new LinkedHashMap<>();
        }
    }

    private void writeError(HttpServletResponse response, int httpStatus, int code, String message) {
        response.setStatus(httpStatus);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        try {
            response.getWriter().write("{\"code\":" + code + ",\"message\":\"" + message
                    + "\",\"data\":null}");
            response.getWriter().flush();
        } catch (IOException e) {
            log.error("写错误响应失败: {}", e.getMessage());
        }
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

    /**
     * 转发字段的值类型。
     */
    private enum ValueKind {
        /**
         * 文本。
         */
        TEXT,

        /**
         * 数字。
         */
        NUMBER
    }
}
