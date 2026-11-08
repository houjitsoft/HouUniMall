package com.houjiang.commons.filter;

import com.houjiang.commons.rocketmq.MqSendUtils;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HTTP 访问日志过滤器
 * <p>
 * 记录所有 HTTP 请求的访问轨迹，通过 RocketMQ 异步发送访问日志。
 * 记录内容包括：请求方法、路径、查询参数、客户端 IP、User-Agent、响应状态码、响应耗时。
 * </p>
 *
 * <p><b>开关控制：</b> 通过配置 {@code houcloud.log.access.enabled=true} 启用，
 * 默认关闭（matchIfMissing=false），避免影响现有服务。</p>
 *
 * <p><b>排除路径：</b> 自动排除健康检查、Swagger UI、静态资源等路径，
 * 减少无效日志噪音。</p>
 *
 * <p><b>安全保障：</b> 所有操作包裹在 try-catch 中，任何异常均不影响正常请求处理。
 * 使用 {@link ContentCachingRequestWrapper} 和 {@link ContentCachingResponseWrapper}
 * 避免流被消费导致后续处理失败。</p>
 *
 * @author Amos
 * @since 2026/04/13
 */
@Component
@Order(Integer.MIN_VALUE + 100)
@ConditionalOnProperty(name = "houcloud.log.access.enabled", havingValue = "true", matchIfMissing = false)
public class AccessLogFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(AccessLogFilter.class);

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 需要排除的路径前缀或模式列表
     * 这些路径的请求不会记录访问日志
     */
    private static final List<String> EXCLUDE_PREFIXES = Arrays.asList(
            "/actuator",
            "/swagger-ui",
            "/v3/api-docs",
            "/v2/api-docs",
            "/swagger-resources",
            "/webjars",
            "/favicon.ico"
    );

    /**
     * 需要排除的文件后缀列表（静态资源）
     */
    private static final List<String> EXCLUDE_SUFFIXES = Arrays.asList(
            ".css", ".js", ".png", ".jpg", ".jpeg", ".gif",
            ".ico", ".woff", ".woff2", ".ttf", ".map"
    );

    /**
     * 初始化过滤器
     *
     * @param filterConfig 过滤器配置
     */
    @Override
    public void init(FilterConfig filterConfig) {
        log.info("[AccessLogFilter] HTTP 访问日志过滤器已启用");
    }

    /**
     * 核心过滤逻辑，记录请求和响应信息
     * <p>
     * 执行流程：
     * 1. 判断是否需要排除该路径
     * 2. 使用 ContentCachingWrapper 包装请求和响应
     * 3. 执行后续过滤链
     * 4. 异步发送访问日志（try-catch 保护）
     * </p>
     *
     * @param request  Servlet 请求
     * @param response Servlet 响应
     * @param chain    过滤链
     * @throws IOException      IO 异常（直接抛出，不拦截）
     * @throws ServletException Servlet 异常（直接抛出，不拦截）
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (!(request instanceof HttpServletRequest)) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // 排除特定路径，不记录日志
        if (shouldExclude(httpRequest.getRequestURI())) {
            chain.doFilter(request, response);
            return;
        }

        // 使用缓存包装器，避免流被消费
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(httpRequest);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(httpResponse);

        long startTime = System.currentTimeMillis();
        try {
            chain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            long executionTime = System.currentTimeMillis() - startTime;
            // 必须将响应内容写回原始响应，否则客户端收不到数据
            try {
                wrappedResponse.copyBodyToResponse();
            } catch (Exception e) {
                log.warn("[AccessLogFilter] 写回响应内容失败: {}", e.getMessage());
            }
            // 异步记录访问日志
            try {
                recordAccessLog(wrappedRequest, wrappedResponse, executionTime);
            } catch (Exception e) {
                log.warn("[AccessLogFilter] 记录访问日志失败: {}", e.getMessage());
            }
        }
    }

    /**
     * 构建访问日志数据并通过 MQ 发送
     *
     * @param request       带缓存的 HTTP 请求
     * @param response      带缓存的 HTTP 响应
     * @param executionTime 请求处理耗时（毫秒）
     */
    private void recordAccessLog(ContentCachingRequestWrapper request,
                                  ContentCachingResponseWrapper response,
                                  long executionTime) {
        Map<String, Object> data = new HashMap<>();
        data.put("method", request.getMethod());
        data.put("path", request.getRequestURI());
        data.put("query", request.getQueryString());
        data.put("ip", getIpAddr(request));
        data.put("userAgent", request.getHeader("User-Agent"));
        data.put("statusCode", response.getStatus());
        data.put("executionTime", executionTime);
        data.put("createTime", LocalDateTime.now().format(DATE_FORMATTER));

        // 尝试解析 token 获取用户信息（可选，失败不影响日志记录）
        try {
            String token = extractToken(request);
            data.put("token", token != null ? maskToken(token) : null);
        } catch (Exception e) {
            // 忽略
        }

        // 提取模块名（从路径第一段）
        String module = extractModuleFromPath(request.getRequestURI());

        MqSendUtils.sendLogMessage("ACCESS", module, data);
    }

    /**
     * 判断是否应排除该路径
     *
     * @param uri 请求 URI
     * @return true 表示应排除（不记录日志）
     */
    private boolean shouldExclude(String uri) {
        if (uri == null) {
            return false;
        }
        // 检查路径前缀
        for (String prefix : EXCLUDE_PREFIXES) {
            if (uri.startsWith(prefix)) {
                return true;
            }
        }
        // 检查文件后缀（静态资源）
        String lowerUri = uri.toLowerCase();
        for (String suffix : EXCLUDE_SUFFIXES) {
            if (lowerUri.endsWith(suffix)) {
                return true;
            }
        }
        return false;
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
     * 对 token 进行掩码处理，仅保留前 8 位，保护用户隐私
     *
     * @param token 原始 token
     * @return 掩码后的 token
     */
    private String maskToken(String token) {
        if (token.length() <= 8) {
            return "****";
        }
        return token.substring(0, 8) + "****";
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

    /**
     * 销毁过滤器时的清理操作
     */
    @Override
    public void destroy() {
        log.info("[AccessLogFilter] HTTP 访问日志过滤器已关闭");
    }
}
