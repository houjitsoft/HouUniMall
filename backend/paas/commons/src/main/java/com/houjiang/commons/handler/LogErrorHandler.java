package com.houjiang.commons.handler;

import com.houjiang.commons.aspect.UserPermissionsResult;
import com.houjiang.commons.aspect.UserService;
import com.houjiang.commons.rocketmq.MqSendUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常错误日志记录处理器
 * <p>
 * 辅助工具类，供全局异常处理器（{@code @ControllerAdvice}）调用，
 * 捕获未处理异常后通过 RocketMQ 异步发送错误日志。
 * </p>
 *
 * <p><b>注意：</b>本类不是 {@code @ControllerAdvice}，不会和现有全局异常处理器冲突。
 * 需要使用时，在已有的全局异常处理器中注入本类并调用
 * {@link #recordErrorLog(HttpServletRequest, Exception)} 方法。</p>
 *
 * <p><b>开关控制：</b>通过配置 {@code houcloud.log.error.enabled=true} 启用，
 * 默认关闭（matchIfMissing=false），避免影响现有服务。</p>
 *
 * <p><b>安全保障：</b>所有操作包裹在 try-catch 中，任何异常均不影响原有异常处理流程。</p>
 *
 * @author Amos
 * @since 2026/04/13
 */
@Component
@ConditionalOnProperty(name = "houcloud.log.error.enabled", havingValue = "true", matchIfMissing = false)
public class LogErrorHandler {

    private static final Logger log = LoggerFactory.getLogger(LogErrorHandler.class);

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 错误堆栈信息最大保留字符数，避免消息体过大
     */
    private static final int STACK_TRACE_MAX_LENGTH = 2000;

    /**
     * 记录错误日志
     * <p>
     * 从 HTTP 请求中提取上下文信息（URL、方法、IP、用户信息），
     * 结合异常详情构建错误日志数据，通过 RocketMQ 异步发送。
     * 所有操作均在 try-catch 中执行，失败仅打印 warn 级别日志，
     * 不影响原有的异常处理和响应逻辑。
     * </p>
     *
     * @param request   HTTP 请求对象，用于提取 URL、方法、IP 等上下文信息
     * @param exception 捕获到的异常对象
     */
    public void recordErrorLog(HttpServletRequest request, Exception exception) {
        try {
            Map<String, Object> errorData = new HashMap<>();

            // 请求基本信息
            errorData.put("requestUrl", request.getRequestURI());
            errorData.put("requestMethod", request.getMethod());
            errorData.put("ip", getIpAddr(request));
            errorData.put("queryString", request.getQueryString());
            errorData.put("createTime", LocalDateTime.now().format(DATE_FORMATTER));

            // 异常信息
            errorData.put("errorType", exception.getClass().getName());
            errorData.put("errorMessage", exception.getMessage());
            errorData.put("stackTrace", getStackTrace(exception));

            // 提取模块名
            String module = extractModuleFromPath(request.getRequestURI());
            errorData.put("module", module);

            // 尝试从 token 中解析用户信息（可选）
            try {
                String token = extractToken(request);
                if (token != null && !token.isEmpty()) {
                    UserPermissionsResult userResult = UserService.judgePermissions(token);
                    if (userResult != null && Boolean.TRUE.equals(userResult.getFlag())) {
                        errorData.put("userId", userResult.getUserId());
                        errorData.put("username", userResult.getUserName());
                    }
                }
            } catch (Exception e) {
                log.warn("[LogErrorHandler] 解析用户信息失败: {}", e.getMessage());
            }

            MqSendUtils.sendLogMessage("ERROR", module, errorData);

        } catch (Exception e) {
            log.warn("[LogErrorHandler] 记录错误日志失败: {}", e.getMessage());
        }
    }

    /**
     * 记录错误日志（无 HTTP 请求上下文版本）
     * <p>
     * 适用于定时任务、异步线程等非 HTTP 上下文的异常记录场景。
     * </p>
     *
     * @param module    所属模块名称（如 pm、oa、crm 等）
     * @param operation 操作描述
     * @param exception 捕获到的异常对象
     */
    public void recordErrorLog(String module, String operation, Exception exception) {
        try {
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("module", module != null ? module : "");
            errorData.put("operation", operation != null ? operation : "");
            errorData.put("errorType", exception.getClass().getName());
            errorData.put("errorMessage", exception.getMessage());
            errorData.put("stackTrace", getStackTrace(exception));
            errorData.put("createTime", LocalDateTime.now().format(DATE_FORMATTER));

            MqSendUtils.sendLogMessage("ERROR", module, errorData);

        } catch (Exception e) {
            log.warn("[LogErrorHandler] 记录错误日志失败: {}", e.getMessage());
        }
    }

    /**
     * 获取异常堆栈信息字符串，限制最大长度
     *
     * @param exception 异常对象
     * @return 堆栈信息字符串（最多 {@value #STACK_TRACE_MAX_LENGTH} 字符）
     */
    private String getStackTrace(Exception exception) {
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            exception.printStackTrace(pw);
            String stackTrace = sw.toString();
            if (stackTrace.length() > STACK_TRACE_MAX_LENGTH) {
                return stackTrace.substring(0, STACK_TRACE_MAX_LENGTH) + "...[truncated]";
            }
            return stackTrace;
        } catch (Exception e) {
            return exception.getClass().getName() + ": " + exception.getMessage();
        }
    }

    /**
     * 从请求路径中提取模块名称
     * <p>例如 /web/pm/project/list 提取为 pm</p>
     *
     * @param uri 请求 URI
     * @return 模块名称，未能识别时返回空字符串
     */
    private String extractModuleFromPath(String uri) {
        if (uri == null || uri.length() < 2) {
            return "";
        }
        String[] parts = uri.split("/");
        // /web/pm/... -> parts[0]="", parts[1]="web", parts[2]="pm"
        // /pm/... -> parts[0]="", parts[1]="pm"
        if (parts.length >= 3 && "web".equals(parts[1])) {
            return parts[2];
        }
        if (parts.length >= 2) {
            return parts[1];
        }
        return "";
    }

    /**
     * 从请求头或参数中提取 token
     *
     * @param request HTTP 请求
     * @return token 字符串，未找到时返回 null
     */
    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return request.getHeader("token");
    }

    /**
     * 获取客户端真实 IP 地址（支持 Nginx 反向代理）
     *
     * @param request HTTP 请求
     * @return IP 地址字符串
     */
    private String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            int index = ip.indexOf(",");
            return index != -1 ? ip.substring(0, index) : ip;
        }
        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }
        ip = request.getHeader("Proxy-Client-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }
        ip = request.getRemoteAddr();
        return "0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : ip;
    }
}
