package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.support.ForwardDiagnostics;
import com.houjiang.bffweb.support.MallBaseUrlResolver;
import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 转发诊断端点（P0 502 排查）。
 *
 * <ul>
 *   <li>{@code GET /web/admin/forward-diag}            最近 20 条失败 + 最近 20 次地址解析</li>
 *   <li>{@code GET /web/admin/forward-diag?limit=50}   调整条数（1-200）</li>
 * </ul>
 *
 * <p><b>为什么必须留这个端点</b>：BFF 部署在 X99（Windows）上，日志文件取不到，
 * 一次 502 只能靠猜。「连接被拒」「读超时」「上游返回错误码」三种完全不同的故障
 * 在客户端看到的都是同一个 502。有了它，一次请求就能区分三者，
 * 顺便还能看到地址解析到底选了 {@code config} / {@code nacos} / {@code default} 哪条路。
 *
 * <p><b>鉴权</b>：请求头 {@code X-Admin-Token}，值与 {@code restart.admin.token} 一致
 * （与 {@code /web/admin/restart} 同一套）。失败返回 <b>401</b> 而不是抛异常——
 * 抛异常会被全局处理器变成 500，让人误以为服务坏了。
 *
 * <p><b>安全边界</b>：只输出 URL（已打码）与异常类型/消息，<b>不输出请求体、不输出响应体、
 * 不输出令牌原文</b>；数据只存在内存有界队列里，<b>不落盘</b>，进程重启即清空。
 *
 * @author Amos
 * @since M4 补丁（502 排查）
 */
@RestController
@RequestMapping("/web/admin/forward-diag")
public class ForwardDiagController {

    private final ForwardDiagnostics diagnostics;

    private final MallBaseUrlResolver resolver;

    @Value("${restart.admin.token}")
    private String adminToken;

    /**
     * 构造诊断端点。
     *
     * @param diagnostics 诊断记录
     * @param resolver    mall 基址解析（顺带暴露当前配置值与当前解析结果）
     */
    public ForwardDiagController(ForwardDiagnostics diagnostics, MallBaseUrlResolver resolver) {
        this.diagnostics = diagnostics;
        this.resolver = resolver;
    }

    /**
     * 返回最近的转发失败与地址解析记录。
     *
     * @param request 原始请求（取 {@code X-Admin-Token}）
     * @param limit   返回条数，默认 20，上限 200
     * @return 诊断数据
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> diag(HttpServletRequest request,
                                    @RequestParam(name = "limit", defaultValue = "20") int limit) {
        if (!authorized(request)) {
            Map<String, Object> denied = new LinkedHashMap<>();
            denied.put("code", 401);
            denied.put("message", "X-Admin-Token 鉴权失败");
            denied.put("data", null);
            return denied;
        }
        int max = Math.min(Math.max(limit, 1), ForwardDiagnostics.CAPACITY);
        List<ForwardDiagnostics.Entry> failures = diagnostics.recentFailures(max);
        List<ForwardDiagnostics.Entry> resolved = diagnostics.recentResolved(max);

        MallBaseUrlResolver.Resolved current = resolver.resolve();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("configuredUrl", resolver.configuredBaseUrl());
        data.put("currentUrl", current.url());
        data.put("currentSource", current.source());
        data.put("failureCount", failures.size());
        data.put("failures", failures);
        data.put("resolved", resolved);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", 0);
        body.put("message", "ok");
        body.put("data", data);
        return body;
    }

    private boolean authorized(HttpServletRequest request) {
        String provided = request == null ? null : request.getHeader("X-Admin-Token");
        return provided != null && !provided.isBlank() && provided.equals(adminToken);
    }
}
