package com.houjiang.commons.errorcode.domain;

import com.houjiang.commons.errorcode.ErrorCategory;
import com.houjiang.commons.errorcode.ErrorCodeDef;
import com.houjiang.commons.errorcode.ErrorLevel;
import com.houjiang.commons.errorcode.ErrorMeta;
import com.houjiang.commons.errorcode.HttpSemantic;
import com.houjiang.commons.errorcode.UiAction;

/**
 * MESSAGE（域 51）消息与 IM 错误码，号段 51001-51999。
 *
 * <p>解决的问题：客服账号创建、坐席在线状态、会话生命周期的失败码挂在
 * {@code BizCodeEnume} 的 17060-17063 段，跟支付/财务码挤在一起，
 * 且 17063 同时是「未结束会话」和「客服服务未配置」。
 * 本域把 IM 与客服相关状态登记成独立号段。
 *
 * <p>码位约定：51001-51005 由「第二阶段：旧枚举改为引用统一码」登记；
 * 51100 之后留给后续子模块（站内信、消息模板、推送通道）。
 *
 * @author Amos
 * @since v1.3.0
 */
public enum MessageErrorCode implements ErrorCodeDef {
    /**
     * 客服账号已被占用，请更换。
     *
     * <p>触发/排查：创建客服账号时唯一键冲突。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    IM_ACCOUNT_OCCUPIED(51001, ErrorMeta.builder(51001, "客服账号已被占用，请更换")
            .devMessage("创建客服账号时唯一键冲突")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.CONFLICT)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(17060)
            .since("v1.3.0")
            .build()),

    /**
     * 客服账号不存在，请先创建。
     *
     * <p>触发/排查：按账号查不到客服配置。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    IM_ACCOUNT_NOT_FOUND(51002, ErrorMeta.builder(51002, "客服账号不存在，请先创建")
            .devMessage("按账号查不到客服配置")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.NOT_FOUND)
            .httpSemantic(HttpSemantic.NOT_FOUND_404)
            .exposeToUser(true)
            .legacyCodes(17061)
            .since("v1.3.0")
            .build()),

    /**
     * 客服不在线，请稍后再试。
     *
     * <p>触发/排查：客服坐席离线或不存在会话。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    IM_AGENT_OFFLINE(51003, ErrorMeta.builder(51003, "客服不在线，请稍后再试")
            .devMessage("客服坐席离线或不存在会话")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.BIZ)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(17062)
            .since("v1.3.0")
            .build()),

    /**
     * 客服服务未开通，请联系管理员。
     *
     * <p>触发/排查：客服服务配置缺失。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    IM_NOT_CONFIGURED(51004, ErrorMeta.builder(51004, "客服服务未开通，请联系管理员")
            .devMessage("客服服务配置缺失")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.SYSTEM)
            .httpSemantic(HttpSemantic.UNAVAILABLE_503)
            .exposeToUser(true)
            .legacyCodes(17063)
            .since("v1.3.0")
            .build()),

    /**
     * 还有未结束的会话，请先结束。
     *
     * <p>触发/排查：存在未结束的会话记录。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    IM_SESSION_UNCLOSED(51005, ErrorMeta.builder(51005, "还有未结束的会话，请先结束")
            .devMessage("存在未结束的会话记录")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.CONFLICT)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(17063)
            .since("v1.3.0")
            .build()),

    ;

    private final int code;
    private final ErrorMeta meta;

    MessageErrorCode(int code, ErrorMeta meta) {
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
