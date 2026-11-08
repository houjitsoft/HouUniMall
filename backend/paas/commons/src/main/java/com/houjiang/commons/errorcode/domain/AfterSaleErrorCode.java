package com.houjiang.commons.errorcode.domain;

import com.houjiang.commons.errorcode.ErrorCategory;
import com.houjiang.commons.errorcode.ErrorCodeDef;
import com.houjiang.commons.errorcode.ErrorLevel;
import com.houjiang.commons.errorcode.ErrorMeta;
import com.houjiang.commons.errorcode.HttpSemantic;
import com.houjiang.commons.errorcode.UiAction;

/**
 * AFTER_SALE（域 43）售后服务错误码，号段 43001-43999。
 *
 * <p>解决的问题（team-lead 裁决 1）：cmsadmin 白名单里旧码 <b>14052</b>
 * 「没有获取到售后信息！」被当成合法返回值放行。本域的
 * {@link #AFTER_SALE_RECORD_NOT_FOUND}（43001）是其正式归属，
 * 语义是「查询为空」，按 INFO + EMPTY_STATE <b>保留放行行为</b>（不弹 Toast），
 * 但从魔法数字白名单改为按新码显式声明。
 *
 * @author Amos
 * @since v1.2.0
 */
public enum AfterSaleErrorCode implements ErrorCodeDef {

    /**
     * 没有售后信息（订单尚未发起售后）。
     *
     * <p>触发：查询售后单为空。用户看到空状态占位，<b>不弹 Toast</b>。
     *
     * <p>排查：旧码 14052 在 cmsadmin 被放进白名单放行；本码继续按 EMPTY_STATE 放行，
     * 但改为显式声明，禁止再写魔法数字白名单。
     */
    AFTER_SALE_RECORD_NOT_FOUND(43001, ErrorMeta.builder(43001, "该订单还没有售后记录")
            .devMessage("售后单查询为空")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.EMPTY_STATE).category(ErrorCategory.NOT_FOUND)
            .httpSemantic(HttpSemantic.OK_200)
            .exposeToUser(true)
            .legacyCodes(14052)
            .since("v1.2.0")
            .remark("cmsadmin 白名单码 14052 的新归属；保持放行，改按新码显式声明")
            .build()),

    /**
     * 售后申请参数不合法（原因、凭证缺失）。
     *
     * <p>触发：提交售后时校验失败。用户看到「售后信息填写不完整，请检查」。
     *
     * <p>排查：确认原因、说明、凭证图片是否齐全。
     */
    AFTER_SALE_PARAM_INVALID(43002, ErrorMeta.builder(43002, "售后信息填写不完整，请检查")
            .devMessage("售后申请参数校验失败")
            .level(ErrorLevel.INFO).retryable(true)
            .uiAction(UiAction.INLINE).category(ErrorCategory.VALIDATION)
            .httpSemantic(HttpSemantic.BAD_REQUEST_400)
            .exposeToUser(true)
            .legacyCodes(14008, 14010, 14077)
            .since("v1.2.0")
            .build()),

    /**
     * 售后状态不允许（已完成的售后不允许再改）。
     *
     * <p>触发：对当前售后状态执行不允许的操作。用户看到「售后单当前状态不允许该操作」。
     *
     * <p>排查：确认售后状态机流转是否合法。
     */
    AFTER_SALE_STATE_NOT_ALLOWED(43003, ErrorMeta.builder(43003, "售后单当前状态不允许该操作")
            .devMessage("售后状态机流转非法")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.BIZ)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(14043, 14044, 14060, 14078)
            .since("v1.2.0")
            .build()),

    /**
     * 重复提交售后申请。
     *
     * <p>触发：同一订单同一类型已有在途售后。用户看到「已有在途售后，请勿重复申请」。
     *
     * <p>排查：引导用户查看已有售后单进展。
     */
    AFTER_SALE_DUPLICATE_APPLY(43004, ErrorMeta.builder(43004, "已有在途售后，请勿重复申请")
            .devMessage("同订单同类型存在未完结售后单")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.CONFLICT)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(14042)
            .since("v1.2.0")
            .build()),

    /**
     * 超出售后时效（超过 7 天无理由期限等）。
     *
     * <p>触发：提交时已过可售后期限。用户看到「已超出可售后时限」。
     *
     * <p>排查：确认时效规则与订单完成时间。
     */
    AFTER_SALE_TIME_EXPIRED(43006, ErrorMeta.builder(43006, "已超出可售后时限")
            .devMessage("提交时间超过售后时效窗口")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.BIZ)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(14009, 14061)
            .since("v1.2.0")
            .build()),

    /**
     * 查询不到售后数据（筛选条件无命中）。
     *
     * <p>触发：售后列表查询返回空。用户看到空状态占位，不弹 Toast。
     *
     * <p>排查：这是 INFO 级别，正常业务结果。
     */
    AFTER_SALE_QUERY_RESULT_EMPTY(43007, ErrorMeta.builder(43007, "没有找到符合条件的售后单")
            .devMessage("售后列表查询无命中")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.EMPTY_STATE).category(ErrorCategory.NOT_FOUND)
            .httpSemantic(HttpSemantic.OK_200)
            .exposeToUser(true)
            .since("v1.2.0")
            .build()),

    /**
     * 退款失败。
     *
     * <p>触发：调用支付渠道退款失败。用户看到「退款失败，请联系客服处理」。
     *
     * <p>排查：<b>资金类错误禁止回显渠道原文</b>；确认退款单状态，必要时人工介入。
     */
    AFTER_SALE_REFUND_FAILED(43010, ErrorMeta.builder(43010, "退款失败，请联系客服处理")
            .devMessage("支付渠道退款调用失败，渠道原文见服务端日志")
            .level(ErrorLevel.FATAL).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.SYSTEM)
            .httpSemantic(HttpSemantic.INTERNAL_500)
            .exposeToUser(false)
            .troubleshooting("1. 查退款单状态与渠道返回码；2. 确认商户余额充足；3. 必要时走人工退款流程。")
            .since("v1.2.0")
            .remark("资金类错误：exposeToUser=false，前端一律出类别兜底文案")
            .build())
    ;

    private final int code;
    private final ErrorMeta meta;

    AfterSaleErrorCode(int code, ErrorMeta meta) {
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
