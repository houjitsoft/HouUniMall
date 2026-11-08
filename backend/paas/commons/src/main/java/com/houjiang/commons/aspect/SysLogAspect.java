package com.houjiang.commons.aspect;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.houjiang.commons.annnotation.NoSysLog;
import com.houjiang.commons.annnotation.SysLog;
import com.houjiang.commons.rocketmq.MqSendUtils;
import com.houjiang.commons.util.PayToolUtil;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamSource;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 系统操作日志 AOP：
 * <ul>
 *   <li>手工 {@link SysLog} 注解</li>
 *   <li>自动采集：{@link RestController} 且类名含 {@code .controller.}（默认开启）</li>
 * </ul>
 * 经 bff-web Feign 会带 {@code X-Hou-Gateway=bff-web} 标记来源。
 * bff-web 不依赖 commons、无本切面，故操作日志在 paas Controller 落库。
 * @author Amos
 */
@Aspect
@Component
@RestController
public class SysLogAspect {

    private static final Logger log = LoggerFactory.getLogger(SysLogAspect.class);

    public static final String GATEWAY_HEADER = "X-Hou-Gateway";
    public static final String GATEWAY_BFF = "bff-web";

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final int MAX_PARAMS_LEN = 4096;

    private static final Set<String> SENSITIVE_KEYS = Set.of(
            "password", "passwd", "pwd", "oldpassword", "newpassword",
            "token", "accesstoken", "refreshtoken", "authorization",
            "secret", "clientsecret", "appsecret", "apikey", "api_key",
            "privatekey", "credential", "credentials"
    );

    @Value("${houcloud.log.operation.auto-all:true}")
    private boolean autoAll;

    @Pointcut("@annotation(com.houjiang.commons.annnotation.SysLog)")
    public void annotatedSysLog() {
    }

    @Pointcut("@within(org.springframework.web.bind.annotation.RestController) "
            + "&& execution(* com.houjiang..*(..))")
    public void restControllerMethods() {
    }

    @Around("annotatedSysLog() || restControllerMethods()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Throwable throwable = null;
        Object result = null;

        try {
            result = joinPoint.proceed();
        } catch (Throwable t) {
            throwable = t;
        }

        long executionTime = System.currentTimeMillis() - startTime;

        try {
            saveLog(joinPoint, executionTime, throwable);
        } catch (Exception e) {
            log.warn("记录操作日志失败: {}", e.getMessage());
        }

        if (throwable != null) {
            throw throwable;
        }
        return result;
    }

    private void saveLog(ProceedingJoinPoint joinPoint, long executionTime, Throwable throwable) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Class<?> targetClass = joinPoint.getTarget().getClass();
        String className = targetClass.getName();

        if (shouldSkip(method, targetClass, className)) {
            return;
        }

        HttpServletRequest request = currentRequest();
        SysLog myLog = method.getAnnotation(SysLog.class);
        boolean hasSysLog = myLog != null;

        // bff-web 不依赖 commons：网站经 Feign 进来的请求必须在 paas 记日志（不可因网关头跳过）

        boolean isControllerClass = className.contains(".controller.");
        if (!hasSysLog) {
            if (!autoAll || !isControllerClass) {
                return;
            }
            if (isExcludedUrl(request)) {
                return;
            }
        }

        String operation;
        String logType = "OPERATION";
        String module = "";
        if (hasSysLog) {
            operation = myLog.value();
            logType = myLog.logType();
            module = myLog.module() != null ? myLog.module() : "";
        } else {
            operation = buildAutoOperation(method, request);
            module = inferModule(className, request);
        }
        if (module == null || module.isBlank()) {
            module = inferModule(className, request);
        }

        String methodName = className + "." + method.getName();
        String requestParams = sanitizeAndTruncate(extractRequestParams(joinPoint, request));
        String requestUrl = "";
        String httpMethod = "";
        String ip = "";
        if (request != null) {
            requestUrl = request.getRequestURI();
            httpMethod = request.getMethod();
            ip = PayToolUtil.getIpAddr(request);
        }

        String userId = "0";
        String username = "0";
        try {
            String token = extractToken(request, requestParams);
            if (token != null && !token.isEmpty()) {
                UserPermissionsResult userResult = UserService.judgePermissions(token);
                if (userResult != null && Boolean.TRUE.equals(userResult.getFlag())) {
                    userId = userResult.getUserId() != null ? userResult.getUserId() : "0";
                    username = userResult.getUserName() != null ? userResult.getUserName() : "0";
                }
            }
        } catch (Exception e) {
            log.warn("解析用户信息失败: {}", e.getMessage());
        }

        int status = throwable == null ? 1 : 0;
        String errorMsg = throwable != null ? throwable.getMessage() : null;

        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("username", username);
        data.put("module", module);
        data.put("operation", operation);
        data.put("method", methodName);
        data.put("requestMethod", httpMethod);
        data.put("requestUrl", requestUrl);
        data.put("requestParams", requestParams);
        data.put("ip", ip);
        data.put("executionTime", executionTime);
        data.put("status", status);
        data.put("errorMsg", errorMsg);
        data.put("createTime", LocalDateTime.now().format(DATE_FORMATTER));

        log.info("[SysLog] logType={}, module={}, operation={}, method={}, http={}, cost={}ms, status={}",
                logType, module, operation, methodName, httpMethod, executionTime, status);

        MqSendUtils.sendLogMessage(logType, module, data);

        if (throwable != null) {
            Map<String, Object> errorData = new HashMap<>(data);
            errorData.put("errorType", throwable.getClass().getName());
            MqSendUtils.sendLogMessage("ERROR", module, errorData);
        }
    }

    private boolean shouldSkip(Method method, Class<?> targetClass, String className) {
        if (method.isAnnotationPresent(NoSysLog.class) || targetClass.isAnnotationPresent(NoSysLog.class)) {
            return true;
        }
        String lower = className.toLowerCase(Locale.ROOT);
        if (lower.contains(".feign.") || lower.contains(".fallback.")) {
            return true;
        }
        return false;
    }

    private boolean isExcludedUrl(HttpServletRequest request) {
        if (request == null) {
            return false;
        }
        String uri = request.getRequestURI();
        if (uri == null) {
            return false;
        }
        String u = uri.toLowerCase(Locale.ROOT);
        return u.contains("/actuator")
                || u.contains("/error")
                || u.contains("/v3/api-docs")
                || u.contains("/swagger")
                || u.contains("/swagger-ui")
                || u.contains("/webjars/");
    }

    private String buildAutoOperation(Method method, HttpServletRequest request) {
        Operation op = method.getAnnotation(Operation.class);
        if (op != null && op.summary() != null && !op.summary().isBlank()) {
            return op.summary().trim();
        }
        String verb = "请求";
        String http = request != null ? request.getMethod() : "";
        if (http != null) {
            switch (http.toUpperCase(Locale.ROOT)) {
                case "GET" -> verb = "查询";
                case "POST" -> verb = "新增或提交";
                case "PUT", "PATCH" -> verb = "修改";
                case "DELETE" -> verb = "删除";
                default -> {
                }
            }
        }
        String uri = request != null && request.getRequestURI() != null ? request.getRequestURI() : method.getName();
        return verb + " " + uri;
    }

    private String inferModule(String className, HttpServletRequest request) {
        if (request != null && request.getRequestURI() != null) {
            String uri = request.getRequestURI();
            // /web/oa/... or /oa/...
            String[] parts = uri.split("/");
            for (int i = 0; i < parts.length; i++) {
                if ("web".equals(parts[i]) && i + 1 < parts.length && !parts[i + 1].isBlank()) {
                    return parts[i + 1];
                }
            }
            if (parts.length > 1 && !parts[1].isBlank()) {
                return parts[1];
            }
        }
        // com.houjiang.oa.controller... / com.houjiang.bffweb.controller...
        if (className.startsWith("com.houjiang.bffweb.")) {
            return "bff";
        }
        if (className.startsWith("com.houjiang.")) {
            String rest = className.substring("com.houjiang.".length());
            int dot = rest.indexOf('.');
            if (dot > 0) {
                return rest.substring(0, dot);
            }
        }
        return "";
    }

    private HttpServletRequest currentRequest() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                return attrs.getRequest();
            }
        } catch (Exception e) {
            log.warn("获取 HttpServletRequest 失败: {}", e.getMessage());
        }
        return null;
    }

    private String extractRequestParams(ProceedingJoinPoint joinPoint, HttpServletRequest request) {
        try {
            if (request != null) {
                String query = request.getQueryString();
                JsonRequestWrapper wrapper = new JsonRequestWrapper(request);
                String body = wrapper.getBody();
                if (body != null && !body.trim().isEmpty()) {
                    if (query != null && !query.isBlank()) {
                        return "{\"query\":\"" + query.replace("\"", "\\\"") + "\",\"body\":" + body + "}";
                    }
                    return body;
                }
                if (query != null && !query.isBlank()) {
                    return "{\"query\":\"" + query.replace("\"", "\\\"") + "\"}";
                }
                Map<String, String[]> paramMap = request.getParameterMap();
                if (paramMap != null && !paramMap.isEmpty()) {
                    return JSON.toJSONString(paramMap);
                }
            }
        } catch (Exception e) {
            log.warn("从请求体读取参数失败: {}", e.getMessage());
        }
        try {
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                List<Object> safe = new ArrayList<>();
                for (Object arg : args) {
                    if (arg == null) {
                        safe.add(null);
                    } else if (arg instanceof ServletRequest || arg instanceof ServletResponse
                            || arg instanceof MultipartFile || arg instanceof MultipartFile[]
                            || arg instanceof InputStreamSource) {
                        safe.add(arg.getClass().getSimpleName());
                    } else {
                        safe.add(arg);
                    }
                }
                return JSON.toJSONString(safe);
            }
        } catch (Exception e) {
            log.warn("序列化方法入参失败: {}", e.getMessage());
        }
        return null;
    }

    private String sanitizeAndTruncate(String raw) {
        if (raw == null || raw.isBlank()) {
            return raw;
        }
        String masked = maskSensitive(raw);
        if (masked.length() <= MAX_PARAMS_LEN) {
            return masked;
        }
        return masked.substring(0, MAX_PARAMS_LEN) + "...(truncated)";
    }

    private String maskSensitive(String raw) {
        try {
            Object parsed = JSON.parse(raw);
            maskJsonValue(parsed);
            return JSON.toJSONString(parsed);
        } catch (Exception ignore) {
            // 非 JSON：简单替换常见字段形态
            String s = raw;
            for (String key : SENSITIVE_KEYS) {
                s = s.replaceAll("(?i)(\"" + key + "\"\\s*:\\s*\")([^\"]*)(\")", "$1***$3");
            }
            return s;
        }
    }

    private void maskJsonValue(Object node) {
        if (node instanceof JSONObject obj) {
            for (String key : new ArrayList<>(obj.keySet())) {
                if (isSensitiveKey(key)) {
                    obj.put(key, "***");
                } else {
                    maskJsonValue(obj.get(key));
                }
            }
        } else if (node instanceof JSONArray arr) {
            for (int i = 0; i < arr.size(); i++) {
                maskJsonValue(arr.get(i));
            }
        } else if (node instanceof Map<?, ?> map) {
            @SuppressWarnings("unchecked")
            Map<Object, Object> m = (Map<Object, Object>) map;
            for (Object key : new ArrayList<>(m.keySet())) {
                if (key != null && isSensitiveKey(String.valueOf(key))) {
                    m.put(key, "***");
                } else {
                    maskJsonValue(m.get(key));
                }
            }
        } else if (node instanceof List<?> list) {
            for (Object item : list) {
                maskJsonValue(item);
            }
        }
    }

    private boolean isSensitiveKey(String key) {
        if (key == null) {
            return false;
        }
        String k = key.replace("_", "").replace("-", "").toLowerCase(Locale.ROOT);
        return SENSITIVE_KEYS.contains(k);
    }

    private String extractToken(HttpServletRequest request, String requestParams) {
        if (request != null) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                return authHeader.substring(7);
            }
            String tokenHeader = request.getHeader("token");
            if (tokenHeader != null && !tokenHeader.isEmpty()) {
                return tokenHeader;
            }
        }
        if (requestParams != null && !requestParams.trim().isEmpty()) {
            try {
                Map<?, ?> paramMap = JSON.parseObject(requestParams, Map.class);
                if (paramMap != null && paramMap.containsKey("token")) {
                    Object token = paramMap.get("token");
                    return token != null ? token.toString() : null;
                }
            } catch (Exception e) {
                // ignore
            }
        }
        return null;
    }

    public String getIp(HttpServletRequest request) {
        String ip = null;
        try {
            ip = request.getHeader("X-Forwarded-For");
            if (ip != null && !ip.isEmpty() && !"unKnown".equalsIgnoreCase(ip)) {
                int index = ip.indexOf(",");
                return index != -1 ? ip.substring(0, index) : ip;
            }
            ip = request.getHeader("X-Real-IP");
            if (ip != null && !ip.isEmpty() && !"unKnown".equalsIgnoreCase(ip)) {
                return ip;
            }
            ip = request.getHeader("Proxy-Client-IP");
            if (ip != null && !ip.isEmpty() && !"unKnown".equalsIgnoreCase(ip)) {
                return ip;
            }
            ip = request.getHeader("WL-Proxy-Client-IP");
            if (ip != null && !ip.isEmpty() && !"unKnown".equalsIgnoreCase(ip)) {
                return ip;
            }
            ip = request.getRemoteAddr();
        } catch (Exception e) {
            log.warn("获取客户端 IP 失败: {}", e.getMessage());
        }
        if (ip == null) {
            return "unknown";
        }
        return "0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : ip;
    }
}
