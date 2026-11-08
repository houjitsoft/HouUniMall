package com.houjiang.commons.errorcode.domain;

import com.houjiang.commons.errorcode.ErrorCategory;
import com.houjiang.commons.errorcode.ErrorCodeDef;
import com.houjiang.commons.errorcode.ErrorLevel;
import com.houjiang.commons.errorcode.ErrorMeta;
import com.houjiang.commons.errorcode.HttpSemantic;
import com.houjiang.commons.errorcode.UiAction;

/**
 * UPSTREAM（域 22）上游依赖错误码，号段 22001-22999。
 *
 * <p>解决的问题：bff-web 的 handler 用 {@code ResponseResult.fail(5001, "下游服务不可达，请稍后重试（"+simpleCause+"）")}
 * 把异常根因拼进响应返回浏览器。本域把下游调用失败登记成独立号段，
 * 前端统一走「静默重试 1-2 次 → 仍失败再 Toast」，且<b>禁止回显后端原文</b>。
 *
 * @author Amos
 * @since v1.2.0
 */
public enum UpstreamErrorCode implements ErrorCodeDef {

    /**
     * 上游服务实例不存在（Nacos 里没有可用实例）。
     *
     * <p>触发：LoadBalancer 找不到目标服务实例。用户看到「服务暂时不可用，请稍后再试」。
     *
     * <p>排查：确认目标服务已注册到 Nacos 且命名空间一致。
     */
    UPSTREAM_SERVICE_NOT_FOUND(22001, ErrorMeta.builder(22001, "服务暂时不可用，请稍后再试")
            .devMessage("服务发现未找到可用实例")
            .level(ErrorLevel.FATAL).retryable(true)
            .uiAction(UiAction.SILENT_RETRY).category(ErrorCategory.UPSTREAM)
            .httpSemantic(HttpSemantic.UNAVAILABLE_503)
            .exposeToUser(true)
            .troubleshooting("1. 检查目标服务是否在 Nacos 注册；2. 确认命名空间与分组一致；3. 确认实例健康状态。")
            .since("v1.2.0")
            .build()),

    /**
     * 上游返回的参数错误（下游校验未通过）。
     *
     * <p>触发：下游返回参数类错误。用户看到「提交内容有误，请检查后重试」。
     *
     * <p>排查：对比下游接口的最新字段约定。
     */
    UPSTREAM_PARAM_INVALID(22002, ErrorMeta.builder(22002, "提交内容有误，请检查后重试")
            .devMessage("下游服务返回参数校验失败")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.UPSTREAM)
            .httpSemantic(HttpSemantic.BAD_REQUEST_400)
            .exposeToUser(true)
            .since("v1.2.0")
            .build()),

    /**
     * 上游调用失败（通用，非超时非不可达）。
     *
     * <p>触发：下游返回非 2xx 或响应异常。用户看到「服务暂时不可用，请稍后再试」。
     *
     * <p>排查：看下游服务的错误日志与返回的原始状态码（只在日志里，不出屏）。
     */
    UPSTREAM_CALL_FAILED(22008, ErrorMeta.builder(22008, "服务开小差了，请稍后再试")
            .devMessage("下游服务返回非预期响应，原始状态码见服务端日志")
            .level(ErrorLevel.ERROR).retryable(true)
            .uiAction(UiAction.SILENT_RETRY).category(ErrorCategory.UPSTREAM)
            .httpSemantic(HttpSemantic.UNAVAILABLE_503)
            .exposeToUser(true)
            .troubleshooting("1. 从日志取下游返回的原始状态码与响应体；2. 确认下游版本与契约一致。")
            .since("v1.2.0")
            .build()),

    /**
     * Feign 响应解码失败（下游返回体无法反序列化）。
     *
     * <p>触发：Feign 解码阶段抛异常。用户看到「服务返回数据异常，请稍后再试」。
     *
     * <p>排查：对比下游 DTO 版本；确认没有把错误页 HTML 当 JSON 返回。
     */
    FEIGN_DECODE_FAILED(22601, ErrorMeta.builder(22601, "服务返回数据异常，请稍后再试")
            .devMessage("Feign 响应解码失败")
            .level(ErrorLevel.ERROR).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.UPSTREAM)
            .httpSemantic(HttpSemantic.INTERNAL_500)
            .exposeToUser(true)
            .troubleshooting("1. 打印下游原始响应体；2. 核对 DTO 字段与类型；3. 确认下游未被网关拦截返回 HTML。")
            .since("v1.2.0")
            .build()),

    /**
     * 下游服务不可达（连接被拒、进程未启动）。
     *
     * <p>触发：Feign status == -1，通常目标进程未启动或网络不通。
     * 用户看到「服务暂时不可用，请稍后再试」。
     *
     * <p>排查：<b>旧码 5001 曾把 simpleCause 拼进响应</b>，现已停止；根因只在日志里。
     * 检查目标服务进程与端口连通性。
     */
    SERVICE_UNREACHABLE(22602, ErrorMeta.builder(22602, "服务连接失败，请稍后再试")
            .devMessage("Feign status=-1：连接被拒或目标进程未启动")
            .level(ErrorLevel.FATAL).retryable(true)
            .uiAction(UiAction.SILENT_RETRY).category(ErrorCategory.UPSTREAM)
            .httpSemantic(HttpSemantic.UNAVAILABLE_503)
            .exposeToUser(true)
            .troubleshooting("1. 确认目标服务进程在跑；2. 检查端口与网络策略；3. 查看 Nacos 实例健康状态。")
              .legacyCodes(503, 5001, 8003)
              .since("v1.2.0")
              .remark("替代 bff-web 裸码 5001；根因 simpleCause 只进日志，不再拼进响应；"
                      + "HTTP 裸码 503（服务不可用）一并收口到本码")
            .build()),

    /**
     * 上游调用超时（超过 readTimeout）。
     *
     * <p>触发：Feign 读超时。用户看到「服务响应超时，请稍后再试」。
     *
     * <p>排查：确认下游慢查询；必要时为该接口单独调大 readTimeout。
     */
    UPSTREAM_TIMEOUT(22603, ErrorMeta.builder(22603, "服务响应超时，请稍后再试")
            .devMessage("下游调用超过 readTimeout")
            .level(ErrorLevel.ERROR).retryable(true)
            .uiAction(UiAction.SILENT_RETRY).category(ErrorCategory.UPSTREAM)
            .httpSemantic(HttpSemantic.UNAVAILABLE_503)
            .exposeToUser(true)
            .troubleshooting("1. 定位下游慢接口；2. 检查数据库慢查询；3. 评估单独调大该接口 readTimeout。")
            .since("v1.2.0")
            .build()),

    /**
     * 熔断/限流打开（Sentinel 拦截）。
     *
     * <p>触发：Sentinel 规则命中。用户看到「请求过于频繁，请稍后再试」。
     *
     * <p>排查：看 Sentinel 控制台的实时规则与统计。
     */
    UPSTREAM_CIRCUIT_BREAK(22604, ErrorMeta.builder(22604, "请求过于频繁，请稍后再试")
            .devMessage("Sentinel 熔断或限流规则命中")
            .level(ErrorLevel.WARN).retryable(true)
            .uiAction(UiAction.TOAST).category(ErrorCategory.UPSTREAM)
            .httpSemantic(HttpSemantic.TOO_MANY_REQUESTS_429)
            .exposeToUser(true)
            .troubleshooting("1. 查看 Sentinel 控制台命中的规则；2. 确认是否为流量突增或下游拖慢。")
            .since("v1.2.0")
            .build())
    ;

    private final int code;
    private final ErrorMeta meta;

    UpstreamErrorCode(int code, ErrorMeta meta) {
        this.code = code;
        this.meta = meta;
    }

    @Override
    public int code() {
        return code;
    }

    @Override
    public ErrorMeta meta() {
        return meta;
    }
}
