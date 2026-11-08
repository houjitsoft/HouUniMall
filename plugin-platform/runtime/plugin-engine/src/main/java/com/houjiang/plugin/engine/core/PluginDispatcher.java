package com.houjiang.plugin.engine.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.houjiang.plugin.engine.registry.PluginRegistry;
import com.houjiang.plugin.engine.registry.RouteRegistry;
import com.houjiang.plugin.engine.security.PluginPermissionEvaluator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ValueConstants;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;

/**
 * 插件自有接口统一分发端点（方案 4.4）。
 *
 * <pre>
 * /plugin/{code}/{path}
 *   → ① 插件启用校验 ② 权限校验（未声明权限的策略见 requireDeclaredRoutePermission）
 *     ③ 参数反序列化 ④ 反射调用 ⑤ 异常兜底 + 埋点
 * </pre>
 *
 * 插件<strong>不注册自己的 Controller</strong>，所有接口经此单点分发，因此不可能覆盖宿主路径，
 * 且鉴权 / 限流 / 审计可在此集中处理。
 *
 * <p><b>调用方校验</b>：插件接口默认对宿主（浏览器登录用户）开放，但跨插件调用属于被调方
 * 的私有领地，必须由被调方在 {@code @HouRoute(allowCallers = ...)} 里点名放行。
 * 调用方身份<b>只采信服务端写入的 request attribute</b>，前端自带的请求头一律无视——
 * 插件产物与宿主同处一个 JS 运行时，任何前端传来的身份都可伪造，采信即等于没有校验。
 *
 * @author HouCloud
 */
@RestController
@RequestMapping("/plugin")
public class PluginDispatcher {

    private static final Logger log = LoggerFactory.getLogger(PluginDispatcher.class);

    /**
     * request 属性名：本次请求已被宿主可信过滤器认定为「来自某插件的内部调用」，值为插件编码。
     *
     * <p>由宿主在校验内部凭证后写入（参考 mall 的
     * {@code PluginInternalJobTokenFilter}）；本类只读不写。
     * 前端无法触达 request attribute，因此这是唯一可信的调用方来源。
     */
    public static final String ATTRIBUTE_TRUSTED_PLUGIN_CALLER =
            PluginDispatcher.class.getName() + ".TRUSTED_PLUGIN_CALLER";

    /** {@code allowCallers} 里的宿主侧代号：浏览器登录用户发起的调用。 */
    public static final String CALLER_HOST = "host";

    /** {@code allowCallers} 里的通配代号：任意插件（不含宿主）。 */
    public static final String CALLER_ANY_PLUGIN = "*";

    /**
     * {@code allowCallers} 里的宿主内部链路代号：job / 调度等宿主可信服务，
     * 凭内部任务令牌调用插件（不是浏览器用户，也不是另一个插件）。
     */
    public static final String CALLER_INTERNAL = "internal";

    private final RouteRegistry routeRegistry;
    private final PluginRegistry pluginRegistry;
    private final PluginPermissionEvaluator permissionEvaluator;

    /**
     * 可选：升级排空窗口登记（{@code null} 时不做排空拦截，保持 M4 之前的行为）。
     */
    private final PluginDrainGate drainGate;

    /**
     * 未声明权限的插件路由策略：{@code false}=仅校验登录态（历史兼容），
     * {@code true}=fail-closed 直接拒绝（{@code houcloud.plugin.require-declared-route-permission}）。
     */
    private final boolean requireDeclaredRoutePermission;

    /**
     * 未在 {@code @HouRoute} 上声明 {@code allowCallers} 时的兜底策略（{@code houcloud.plugin.enforce-caller-whitelist}）：
     * {@code false}= 不限制（历史兼容，任何登录用户或插件都能调）；
     * {@code true}= fail-closed，未声明即只允许宿主，插件间调用一律拒绝。
     *
     * <p>与 {@link #requireDeclaredRoutePermission} 同构：先给出声明能力，再把「收不收紧」
     * 交给宿主按迁移进度决定，避免一次切换打爆存量插件。
     */
    private final boolean enforceCallerWhitelist;

    /**
     * 启动装载窗口闸门（{@code null} 表示宿主未接入，保持 M4 之前的 404 行为）。
     *
     * <p>插件装载跑在 Servlet 容器开始监听之后，这段窗口里路由表还是空的，
     * 未命中路由必须回 503（而不是 404），否则每次发版重启前端都会吃到一批假 404。
     */
    private final PluginBootGate bootGate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public PluginDispatcher(RouteRegistry routeRegistry, PluginRegistry pluginRegistry) {
        this(routeRegistry, pluginRegistry,
                new com.houjiang.plugin.engine.security.SpringSecurityPluginPermissionEvaluator(), false);
    }

    public PluginDispatcher(RouteRegistry routeRegistry,
                            PluginRegistry pluginRegistry,
                            PluginPermissionEvaluator permissionEvaluator) {
        this(routeRegistry, pluginRegistry, permissionEvaluator, false);
    }

    public PluginDispatcher(RouteRegistry routeRegistry,
                            PluginRegistry pluginRegistry,
                            PluginPermissionEvaluator permissionEvaluator,
                            boolean requireDeclaredRoutePermission) {
        this(routeRegistry, pluginRegistry, permissionEvaluator, requireDeclaredRoutePermission, null);
    }

    /**
     * 完整构造：额外注入升级排空窗口登记表。
     *
     * @param drainGate 排空窗口登记，可为 {@code null}（未接入升级编排的宿主）
     */
    public PluginDispatcher(RouteRegistry routeRegistry,
                            PluginRegistry pluginRegistry,
                            PluginPermissionEvaluator permissionEvaluator,
                            boolean requireDeclaredRoutePermission,
                            PluginDrainGate drainGate) {
        this(routeRegistry, pluginRegistry, permissionEvaluator, requireDeclaredRoutePermission, drainGate, false);
    }

    /**
     * 完整构造：额外注入排空窗口与调用方白名单兜底策略。
     *
     * @param drainGate 排空窗口登记，可为 {@code null}（未接入升级编排的宿主）
     * @param enforceCallerWhitelist 未声明 {@code allowCallers} 时是否拒绝插件间调用
     */
    public PluginDispatcher(RouteRegistry routeRegistry,
                            PluginRegistry pluginRegistry,
                            PluginPermissionEvaluator permissionEvaluator,
                            boolean requireDeclaredRoutePermission,
                            PluginDrainGate drainGate,
                            boolean enforceCallerWhitelist) {
        this(routeRegistry, pluginRegistry, permissionEvaluator, requireDeclaredRoutePermission,
                drainGate, enforceCallerWhitelist, null);
    }

    /**
     * 完整构造：额外注入启动装载窗口闸门。
     *
     * @param bootGate 启动装载窗口闸门，{@code null} 表示未接入（未命中路由仍回 404）
     */
    public PluginDispatcher(RouteRegistry routeRegistry,
                            PluginRegistry pluginRegistry,
                            PluginPermissionEvaluator permissionEvaluator,
                            boolean requireDeclaredRoutePermission,
                            PluginDrainGate drainGate,
                            boolean enforceCallerWhitelist,
                            PluginBootGate bootGate) {
        this.routeRegistry = routeRegistry;
        this.pluginRegistry = pluginRegistry;
        this.permissionEvaluator = permissionEvaluator;
        this.requireDeclaredRoutePermission = requireDeclaredRoutePermission;
        this.drainGate = drainGate;
        this.enforceCallerWhitelist = enforceCallerWhitelist;
        this.bootGate = bootGate;
    }

    @RequestMapping(value = {"/{code}/**", "/{code}/{*path}"}, method = {
            RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE
    })
    public ResponseEntity<?> dispatch(@PathVariable String code, HttpServletRequest request,
                                      HttpServletResponse response) {
        return dispatchInternal(code, request, response);
    }

    /** Keeps the small two-argument API used by engine unit tests and embedders. */
    public ResponseEntity<?> dispatch(String code, HttpServletRequest request) {
        return dispatchInternal(code, request, null);
    }

    private ResponseEntity<?> dispatchInternal(String code, HttpServletRequest request,
                                               HttpServletResponse response) {
        // 升级排空窗口（方案 §5.3 关键约定 3）：路由已被撤走、新版本尚未装载的这段真空期里，
        // 必须显式回 503 + Retry-After，而不是让请求落到「查不到路由」的 404 或半卸载状态的 500。
        // 前端靠 503 提示「功能升级中」并自动重试；这也是排空期唯一诚实的答案。
        if (drainGate != null && drainGate.isDraining(code)) {
            return ResponseEntity.status(503)
                    .header(HttpHeaders.RETRY_AFTER, String.valueOf(drainGate.retryAfterSeconds()))
                    .body(Map.of("error", "插件正在升级，请稍后重试", "retryAfter", drainGate.retryAfterSeconds()));
        }
        String path = resolvePath(request, code);
        RouteRegistry.RouteEntry entry = routeRegistry.lookup(code, path, request.getMethod());
        if (entry == null) {
            // 装载窗口内路由表还没建起来，404 是假象——插件装完就有了。
            // 与升级排空窗口同样回 503 + Retry-After，让前端能把它和「接口不存在」区分开。
            if (bootGate != null && bootGate.isLoading()) {
                return ResponseEntity.status(503)
                        .header(HttpHeaders.RETRY_AFTER, String.valueOf(bootGate.retryAfterSeconds()))
                        .body(Map.of("error", "插件正在装载，请稍后重试",
                                "retryAfter", bootGate.retryAfterSeconds()));
            }
            return ResponseEntity.notFound().build();
        }
        if (!pluginRegistry.isActive(code)) {
            return ResponseEntity.status(423).body(Map.of("error", "插件未启用"));
        }
        if (!permissionEvaluator.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("error", "未登录或登录已过期"));
        }
        if (!callerAllowed(entry, request)) {
            String caller = trustedPluginCaller(request);
            log.warn("插件 {} 路由 {} {} 拒绝调用方 {}（allowCallers={}）",
                    code, request.getMethod(), path,
                    caller == null ? CALLER_HOST : caller, entry.allowCallers);
            return ResponseEntity.status(403).body(Map.of("error", "该插件接口不允许当前调用方访问"));
        }
        String permission = entry.permission;
        if (permission == null || permission.isBlank()) {
            // 未声明权限：默认保持历史兼容（仅校验登录态），可由配置收敛为 fail-closed。
            // 注意不能把它交给权限求值器——求值器对空权限是拒绝的（fail-closed）。
            if (requireDeclaredRoutePermission) {
                log.warn("插件 {} 路由 {} {} 未声明权限，已按 fail-closed 策略拒绝",
                        code, request.getMethod(), path);
                return ResponseEntity.status(403).body(Map.of("error", "插件接口未声明权限，已拒绝访问"));
            }
            log.debug("插件 {} 路由 {} {} 未声明权限，按兼容策略仅校验登录态", code, request.getMethod(), path);
        } else if (!permissionEvaluator.hasPermission(permission)) {
            return ResponseEntity.status(403).body(Map.of("error", "无权访问插件接口"));
        }

        try {
            Object[] args = resolveArgs(entry.method, request, response, path, entry.path);
            Object result = entry.method.invoke(entry.bean, args);
            if (result instanceof ResponseEntity<?> responseEntity) {
                return responseEntity;
            }
            if (entry.method.getReturnType() == void.class || result == null) {
                return ResponseEntity.ok().build();
            }
            return ResponseEntity.ok(result);
        } catch (java.lang.reflect.InvocationTargetException e) {
            Throwable target = e.getTargetException();
            if (target instanceof ResponseStatusException responseStatusException) {
                Map<String, Object> body = new java.util.LinkedHashMap<>();
                body.put("code", responseStatusException.getStatusCode().value());
                body.put("message", responseStatusException.getReason() == null
                        ? responseStatusException.getStatusCode().toString()
                        : responseStatusException.getReason());
                body.put("data", null);
                return ResponseEntity.status(responseStatusException.getStatusCode()).body(body);
            }
            log.error("插件 {} 路由 {} 执行异常", code, path, target);
            return ResponseEntity.status(500).body(Map.of("error", target.getMessage()));
        } catch (Exception e) {
            log.error("插件 {} 路由 {} 分发失败", code, path, e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 调用方白名单判定：跨插件调用必须由被调方显式点名放行。
     *
     * <p>规则（{@code host}＝浏览器登录用户，{@code *}＝任意插件但不含宿主）：
     * <ul>
     *   <li>声明了 {@code allowCallers}：调用方代号必须命中，或插件调用命中 {@code *}；</li>
     *   <li>未声明：{@link #enforceCallerWhitelist} 为 {@code false} 时放行（历史兼容），
     *       {@code true} 时只允许宿主（fail-closed）。</li>
     * </ul>
     */
    private boolean callerAllowed(RouteRegistry.RouteEntry entry, HttpServletRequest request) {
        String caller = trustedPluginCaller(request);
        boolean fromPlugin = caller != null;
        Set<String> allow = entry.allowCallers;
        if (allow == null || allow.isEmpty()) {
            return !(enforceCallerWhitelist && fromPlugin);
        }
        if (allow.contains(fromPlugin ? caller : CALLER_HOST)) {
            return true;
        }
        // "*" 只代表任意插件，不代表宿主：宿主必须显式写 host，避免一次通配把接口完全放开
        return fromPlugin && allow.contains(CALLER_ANY_PLUGIN);
    }

    /**
     * 取服务端认定的调用方插件编码；{@code null} 表示宿主侧（浏览器登录用户）。
     *
     * <p>刻意<b>不读任何请求头</b>：插件前端与宿主同处一个 JS 运行时，
     * {@code X-Plugin-Caller} 之类的头任何人都能伪造，采信等于没有校验。
     * 只有宿主过滤器在校验内部凭证后写入的 request attribute 才算数。
     */
    private String trustedPluginCaller(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        Object attribute = request.getAttribute(ATTRIBUTE_TRUSTED_PLUGIN_CALLER);
        if (!(attribute instanceof String value) || value.isBlank()) {
            return null;
        }
        return value.trim().toLowerCase();
    }

    /**
     * 供宿主可信过滤器写入调用方身份（引擎侧唯一的身份入口）。
     *
     * <p>引擎只做判定、不做认定，认定权必须在宿主的边界过滤器手里：只有那里才握有
     * 内部凭证（如内部任务令牌）的校验结果。业务代码与插件都不得调用本方法。
     *
     * @param request 当前请求
     * @param caller  调用方代号：插件编码、{@link #CALLER_INTERNAL}，或 {@code null} 表示宿主（浏览器用户）
     */
    public static void markCaller(HttpServletRequest request, String caller) {
        if (request == null) {
            return;
        }
        if (caller == null || caller.isBlank()) {
            request.removeAttribute(ATTRIBUTE_TRUSTED_PLUGIN_CALLER);
            return;
        }
        request.setAttribute(ATTRIBUTE_TRUSTED_PLUGIN_CALLER, caller.trim().toLowerCase());
    }

    private String resolvePath(HttpServletRequest request, String code) {
        String uri = request.getRequestURI();
        String prefix = "/plugin/" + code;
        if (uri.startsWith(prefix)) {
            String p = uri.substring(prefix.length());
            return p.isEmpty() ? "/" : p;
        }
        return "/";
    }

    private Object[] resolveArgs(Method method, HttpServletRequest request, HttpServletResponse response,
                                 String requestPath,
                                 String routeTemplate) throws Exception {
        Parameter[] params = method.getParameters();
        if (params.length == 0) {
            return new Object[0];
        }
        String body = null;
        Map<String, String> pathVariables = pathVariables(routeTemplate, requestPath);
        Object[] args = new Object[params.length];
        for (int i = 0; i < params.length; i++) {
            Parameter parameter = params[i];
            if (HttpServletRequest.class.isAssignableFrom(parameter.getType())) {
                args[i] = request;
                continue;
            }
            if (HttpServletResponse.class.isAssignableFrom(parameter.getType())) {
                args[i] = response;
                continue;
            }
            RequestPart requestPart = parameter.getAnnotation(RequestPart.class);
            if (requestPart != null) {
                String name = requestPart.name().isBlank() ? requestPart.value() : requestPart.name();
                if (name.isBlank()) name = parameter.getName();
                args[i] = multipartValue(request, name, parameter.getType());
                continue;
            }
            RequestHeader requestHeader = parameter.getAnnotation(RequestHeader.class);
            if (requestHeader != null) {
                String name = requestHeader.name().isBlank() ? requestHeader.value() : requestHeader.name();
                if (name.isBlank()) name = parameter.getName();
                args[i] = convert(request.getHeader(name), parameter.getType());
                continue;
            }
            RequestBody requestBody = parameter.getAnnotation(RequestBody.class);
            RequestParam requestParam = parameter.getAnnotation(RequestParam.class);
            if (requestParam != null) {
                String name = requestParam.name().isBlank() ? requestParam.value() : requestParam.name();
                if (name.isBlank()) name = parameter.getName();
                if (MultipartFile.class.isAssignableFrom(parameter.getType())
                        || parameter.getType() == MultipartFile[].class) {
                    args[i] = multipartValue(request, name, parameter.getType());
                } else {
                    String value = request.getParameter(name);
                    if ((value == null || value.isBlank())
                            && !ValueConstants.DEFAULT_NONE.equals(requestParam.defaultValue())) {
                        value = requestParam.defaultValue();
                    }
                    args[i] = convert(value, parameter.getType());
                }
                continue;
            }
            if (requestBody != null || (params.length == 1
                    && parameter.getAnnotation(PathVariable.class) == null)) {
                if (body == null) body = new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                args[i] = body == null || body.isBlank() ? null
                        : objectMapper.readValue(body, objectMapper.constructType(parameter.getParameterizedType()));
                continue;
            }
            PathVariable pathVariable = parameter.getAnnotation(PathVariable.class);
            if (pathVariable != null) {
                String name = pathVariable.name().isBlank() ? pathVariable.value() : pathVariable.name();
                if (name.isBlank()) name = parameter.getName();
                args[i] = convert(pathVariables.get(name), parameter.getType());
                continue;
            }
            args[i] = null;
        }
        return args;
    }

    private Object multipartValue(HttpServletRequest request, String name, Class<?> type) {
        if (!(request instanceof MultipartHttpServletRequest multipart)) {
            return null;
        }
        if (type == MultipartFile[].class) {
            return multipart.getFiles(name).toArray(MultipartFile[]::new);
        }
        if (MultipartFile.class.isAssignableFrom(type)) {
            return multipart.getFile(name);
        }
        return null;
    }

    private Object convert(String value, Class<?> type) {
        if (value == null || value.isBlank()) return type.isPrimitive() ? primitiveDefault(type) : null;
        if (type == String.class) return value;
        if (type == int.class || type == Integer.class) return Integer.valueOf(value);
        if (type == long.class || type == Long.class) return Long.valueOf(value);
        if (type == boolean.class || type == Boolean.class) return Boolean.valueOf(value);
        if (type == double.class || type == Double.class) return Double.valueOf(value);
        if (type == float.class || type == Float.class) return Float.valueOf(value);
        if (type.isEnum()) return Enum.valueOf((Class) type, value);
        return value;
    }

    private Object primitiveDefault(Class<?> type) {
        if (type == boolean.class) return false;
        if (type == char.class) return '\0';
        if (type == byte.class) return (byte) 0;
        if (type == short.class) return (short) 0;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == float.class) return 0F;
        if (type == double.class) return 0D;
        return null;
    }

    private Map<String, String> pathVariables(String template, String path) {
        Map<String, String> values = new java.util.HashMap<>();
        if (template == null || path == null) return values;
        String[] expected = template.split("/");
        String[] actual = path.split("/");
        if (expected.length != actual.length) return values;
        for (int i = 0; i < expected.length; i++) {
            String segment = expected[i];
            if (segment.startsWith("{") && segment.endsWith("}")) {
                values.put(segment.substring(1, segment.length() - 1), actual[i]);
            }
        }
        return values;
    }
}
