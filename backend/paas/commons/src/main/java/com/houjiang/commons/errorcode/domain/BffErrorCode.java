package com.houjiang.commons.errorcode.domain;

import com.houjiang.commons.errorcode.ErrorCategory;
import com.houjiang.commons.errorcode.ErrorCodeDef;
import com.houjiang.commons.errorcode.ErrorLevel;
import com.houjiang.commons.errorcode.ErrorMeta;
import com.houjiang.commons.errorcode.HttpSemantic;
import com.houjiang.commons.errorcode.UiAction;

/**
 * BFF（域 80）BFF 契约错误码，号段 80001-80999。
 *
 * <p>解决的问题：bff-web 的 handler 用裸码 5000 / 5001 / 404 / 400 出异常，
 * 其中 5000 还会把异常根因拼进响应（{@code "服务端异常：" + simpleCause(e)}），
 * 浏览器直接能看到内部类名与下游状态。本域把这些出口登记成有元信息的码，
 * 根因一律只进日志。
 *
 * @author Amos
 * @since v1.2.0
 */
public enum BffErrorCode implements ErrorCodeDef {

    /**
     * BFF 路由未命中（请求路径没有对应的转发规则）。
     *
     * <p>触发：{@code NoResourceFoundException} 且 path 未命中任何已注册路由。
     * 用户看到「请求的资源不存在，请确认后重试」。
     *
     * <p>排查：<b>旧码 404 曾把具体 path 拼进响应</b>（可能泄露内部路径），
     * 现在 path 只进日志。确认前端路由与后端映射是否同步。
     */
    BFF_ROUTE_NOT_FOUND(80001, ErrorMeta.builder(80001, "请求的资源不存在，请确认后重试")
            .devMessage("NoResourceFoundException：BFF 无匹配转发规则，path 见服务端日志")
            .level(ErrorLevel.ERROR).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.NOT_FOUND)
            .httpSemantic(HttpSemantic.NOT_FOUND_404)
            .exposeToUser(true)
            .since("v1.2.0")
            .remark("bff-web 裸码 404 由 SYSTEM 域 20001 统一认领；本码用于 BFF 侧专属路由上下文")
            .build()),

    /**
     * BFF 入参不合法。
     *
     * <p>触发：BFF 层参数校验失败。用户看到「请求参数不合法，请检查后重试」。
     *
     * <p>排查：确认前端提交字段与 BFF 契约一致。
     */
    BFF_PARAM_INVALID(80002, ErrorMeta.builder(80002, "请求参数不合法，请检查后重试")
            .devMessage("BFF 层参数校验失败")
            .level(ErrorLevel.INFO).retryable(true)
            .uiAction(UiAction.INLINE).category(ErrorCategory.VALIDATION)
            .httpSemantic(HttpSemantic.BAD_REQUEST_400)
            .exposeToUser(true)
            .since("v1.2.0")
            .build()),

    /**
     * BFF 转发下游失败（通用）。
     *
     * <p>触发：Feign 转发失败但不属于「不可达」细分场景。用户看到「服务暂时不可用，请稍后再试」。
     *
     * <p>排查：具体通道请用 UPSTREAM 域（22）的码；这里只做 BFF 层兜底。
     */
    BFF_UPSTREAM_CALL_FAILED(80008, ErrorMeta.builder(80008, "服务暂时不可用，请稍后再试")
            .devMessage("BFF 转发下游失败，下游返回见服务端日志")
            .level(ErrorLevel.ERROR).retryable(true)
            .uiAction(UiAction.SILENT_RETRY).category(ErrorCategory.UPSTREAM)
            .httpSemantic(HttpSemantic.UNAVAILABLE_503)
            .exposeToUser(true)
            .since("v1.2.0")
            .build()),

    /**
     * BFF 路由未配置（新服务尚未接入转发表）。
     *
     * <p>触发：转发表里查不到目标服务。用户看到「该服务尚未接入，请联系管理员」。
     *
     * <p>排查：在 BFF 路由配置中补登记目标服务。FATAL 级别需人工介入。
     */
    BFF_ROUTE_NOT_CONFIGURED(80009, ErrorMeta.builder(80009, "该服务尚未接入，请联系管理员")
            .devMessage("BFF 转发表中查不到目标服务配置")
            .level(ErrorLevel.FATAL).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.SYSTEM)
            .httpSemantic(HttpSemantic.INTERNAL_500)
            .exposeToUser(true)
            .troubleshooting("1. 在 BFF 路由配置中补登记目标服务；2. 确认服务名与 Nacos 注册名一致；3. 重启 BFF。")
            .since("v1.2.0")
            .build()),

    /**
     * 请求转发失败（构造下游请求或发送阶段失败）。
     *
     * <p>触发：转发前置处理（Header、租户上下文）失败。用户看到「请求处理失败，请稍后重试」。
     *
     * <p>排查：确认拦截器链路（FeignRequestInterceptor）是否正常。
     */
    BFF_REQUEST_FORWARD_FAILED(80010, ErrorMeta.builder(80010, "请求处理失败，请稍后重试")
            .devMessage("请求转发前置处理失败，异常见服务端日志")
            .level(ErrorLevel.ERROR).retryable(true)
            .uiAction(UiAction.SILENT_RETRY).category(ErrorCategory.UPSTREAM)
            .httpSemantic(HttpSemantic.INTERNAL_500)
            .exposeToUser(true)
            .since("v1.2.0")
            .build()),

    /**
     * 下游响应解码失败。
     *
     * <p>触发：BFF 侧反序列化下游响应失败。用户看到「服务返回数据异常，请稍后再试」。
     *
     * <p>排查：确认下游返回的不是错误页 HTML；对比 DTO 版本。
     */
    BFF_RESPONSE_DECODE_FAILED(80011, ErrorMeta.builder(80011, "服务返回数据异常，请稍后再试")
            .devMessage("下游响应反序列化失败，原始响应见服务端日志")
            .level(ErrorLevel.ERROR).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.UPSTREAM)
            .httpSemantic(HttpSemantic.INTERNAL_500)
            .exposeToUser(true)
            .troubleshooting("1. 打印下游原始响应；2. 核对 DTO 字段与类型；3. 确认下游未被网关拦截。")
            .since("v1.2.0")
            .build()),

    /**
     * 租户解析失败（X-Org-Id 缺失或多租户上下文冲突）。
     *
     * <p>触发：多租户接口无法确定归属租户。用户看到「无法确定所属组织，请重新选择」。
     *
     * <p>排查：确认前端是否携带 X-Org-Id；多租户插件场景下常见于切换组织后未刷新。
     */
    BFF_TENANT_RESOLVE_FAILED(80012, ErrorMeta.builder(80012, "无法确定所属组织，请重新选择")
            .devMessage("租户上下文解析失败：X-Org-Id 缺失或冲突")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.VALIDATION)
            .httpSemantic(HttpSemantic.BAD_REQUEST_400)
            .exposeToUser(true)
            .since("v1.2.0")
            .build())
    ;

    private final int code;
    private final ErrorMeta meta;

    BffErrorCode(int code, ErrorMeta meta) {
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
