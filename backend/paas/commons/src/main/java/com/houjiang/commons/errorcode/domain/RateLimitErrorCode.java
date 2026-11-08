package com.houjiang.commons.errorcode.domain;

import com.houjiang.commons.errorcode.ErrorCategory;
import com.houjiang.commons.errorcode.ErrorCodeDef;
import com.houjiang.commons.errorcode.ErrorLevel;
import com.houjiang.commons.errorcode.ErrorMeta;
import com.houjiang.commons.errorcode.HttpSemantic;
import com.houjiang.commons.errorcode.UiAction;

/**
 * RATE_LIMIT（域 25）限流与幂等错误码，号段 25001-25999。
 *
 * <p>解决的问题：限流提示过去直接使用后端原文（含 Redis key、窗口配置等内部信息），
 * 且「重复提交」与「系统错误」混在一起，用户不知道该等一会儿还是该刷新。
 * 本域统一走「Toast + 倒计时禁用按钮」，并明确 retryable=冷却后可重试。
 *
 * @author Amos
 * @since v1.2.0
 */
public enum RateLimitErrorCode implements ErrorCodeDef {

    /**
     * 限流规则不存在（规则被删除或未初始化）。
     *
     * <p>触发：按规则 key 查不到配置。用户看到「操作频率异常，请稍后再试」。
     *
     * <p>排查：确认 AccessLimit 规则已初始化。
     */
    RATE_LIMIT_RULE_NOT_FOUND(25001, ErrorMeta.builder(25001, "操作频率异常，请稍后再试")
            .devMessage("限流规则未找到或未初始化")
            .level(ErrorLevel.WARN).retryable(true)
            .uiAction(UiAction.TOAST).category(ErrorCategory.RATE_LIMIT)
            .httpSemantic(HttpSemantic.TOO_MANY_REQUESTS_429)
            .exposeToUser(true)
            .since("v1.2.0")
            .build()),

    /**
     * 触发限流（超出时间窗口内的允许次数）。
     *
     * <p>触发：AccessLimit 拦截。用户看到「操作过于频繁，请稍后再试」。
     *
     * <p>排查：确认是正常流量还是脚本刷接口；如需放宽，调整规则配置而不是改代码。
     */
    RATE_LIMIT_EXCEEDED(25006, ErrorMeta.builder(25006, "操作过于频繁，请稍后再试")
            .devMessage("超出时间窗口内的允许访问次数")
            .level(ErrorLevel.WARN).retryable(true)
            .uiAction(UiAction.TOAST).category(ErrorCategory.RATE_LIMIT)
            .httpSemantic(HttpSemantic.TOO_MANY_REQUESTS_429)
            .exposeToUser(true)
            .troubleshooting("1. 确认是否为脚本刷接口；2. 如需放宽，调整限流规则配置。")
            .since("v1.2.0")
            .build()),

    /**
     * 重复提交（RepeatClick 拦截）。
     *
     * <p>触发：同一请求在防重窗口内被重复发起。用户看到「请勿重复提交，稍后再试」。
     *
     * <p>排查：这是保护性拦截，重复点击属正常；如误伤，检查防重 key 的粒度。
     */
    REPEAT_SUBMIT(25010, ErrorMeta.builder(25010, "请勿重复提交，稍后再试")
            .devMessage("RepeatClick 防重拦截命中")
            .level(ErrorLevel.WARN).retryable(true)
            .uiAction(UiAction.TOAST).category(ErrorCategory.RATE_LIMIT)
            .httpSemantic(HttpSemantic.TOO_MANY_REQUESTS_429)
            .exposeToUser(true)
            .legacyCodes(10011, 10063)
            .since("v1.2.0")
            .build()),

    /**
     * 短信发送频率过高。
     *
     * <p>触发：同一手机号在冷却期内再次请求验证码。用户看到「验证码发送过于频繁，请稍后再试」。
     *
     * <p>排查：旧码 10003（短信发送频率太高）在 BizCodeEnume 里文案是「短信发送频率太高，稍等一会发送!」，
     * 带感叹号且口语化，本码统一措辞。注意与「短信网关发送失败」（23012）区分。
     */
    SMS_TOO_FREQUENT(25011, ErrorMeta.builder(25011, "验证码发送过于频繁，请稍后再试")
            .devMessage("同一手机号在冷却期内重复请求验证码")
            .level(ErrorLevel.WARN).retryable(true)
            .uiAction(UiAction.TOAST).category(ErrorCategory.RATE_LIMIT)
            .httpSemantic(HttpSemantic.TOO_MANY_REQUESTS_429)
            .exposeToUser(true)
            .legacyCodes(10003)
            .since("v1.2.0")
            .build()),

    /**
     * 接口访问被限制（IP / 账号维度的封禁窗口）。
     *
     * <p>触发：风控或运维配置的访问限制命中。用户看到「访问已被限制，请联系管理员」。
     *
     * <p>排查：确认限制规则是否被误配；误封需运维手动解除。
     */
    ACCESS_LIMITED(25012, ErrorMeta.builder(25012, "访问已被限制，请联系管理员")
            .devMessage("IP/账号维度访问限制命中")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.RATE_LIMIT)
            .httpSemantic(HttpSemantic.TOO_MANY_REQUESTS_429)
            .exposeToUser(true)
            .troubleshooting("1. 查限制规则来源（风控/运维）；2. 误封由运维手动解除。")
            .since("v1.2.0")
            .build()),

    /**
     * 幂等冲突（相同幂等键但请求内容不同）。
     *
     * <p>触发：幂等键命中但参数指纹不一致。用户看到「请求重复且内容不一致，请刷新重试」。
     *
     * <p>排查：确认前端幂等键生成逻辑；同一键重复提交不同内容是设计问题。
     */
    IDEMPOTENT_CONFLICT(25013, ErrorMeta.builder(25013, "请求重复且内容不一致，请刷新重试")
            .devMessage("幂等键命中但请求指纹不一致")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.DIALOG).category(ErrorCategory.RATE_LIMIT)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .since("v1.2.0")
            .build())
    ;

    private final int code;
    private final ErrorMeta meta;

    RateLimitErrorCode(int code, ErrorMeta meta) {
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
