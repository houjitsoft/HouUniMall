package com.houjiang.commons.errorcode.domain;

import com.houjiang.commons.errorcode.ErrorCategory;
import com.houjiang.commons.errorcode.ErrorCodeDef;
import com.houjiang.commons.errorcode.ErrorLevel;
import com.houjiang.commons.errorcode.ErrorMeta;
import com.houjiang.commons.errorcode.HttpSemantic;
import com.houjiang.commons.errorcode.UiAction;

/**
 * LOGISTICS（域 46）物流配送错误码，号段 46001-46999。
 *
 * <p>解决的问题：发货信息校验、配送单状态判断与「同城配送」判定混在
 * {@code BizCodeEnume} 的 14035-14041、14053、14065 段和 {@code RiderCodeEnume} 的
 * 11012-11022 段里，订单模块和骑手模块各自维护一套，语义还对不上。
 * 本域把发货与配送单生命周期登记成独立号段，订单与骑手两侧共用。
 *
 * <p>码位约定：46001-46011 由「第二阶段：旧枚举改为引用统一码」登记；
 * 46100 之后留给后续子模块（运单轨迹、自提点、第三方快递）。
 *
 * @author Amos
 * @since v1.3.0
 */
public enum LogisticsErrorCode implements ErrorCodeDef {
    /**
     * 物流信息不完整，请检查后提交。
     *
     * <p>触发/排查：发货/必填物流字段缺失。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    LOGISTICS_INFO_INCOMPLETE(46001, ErrorMeta.builder(46001, "物流信息不完整，请检查后提交")
            .devMessage("发货/必填物流字段缺失")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.INLINE).category(ErrorCategory.VALIDATION)
            .httpSemantic(HttpSemantic.BAD_REQUEST_400)
            .exposeToUser(true)
            .legacyCodes(14035, 14036, 14037, 14038, 14039, 14040, 14041, 14066)
            .since("v1.3.0")
            .build()),

    /**
     * 填写物流失败，请稍后重试。
     *
     * <p>触发/排查：物流信息写入失败。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    LOGISTICS_DELIVERY_FAILED(46002, ErrorMeta.builder(46002, "填写物流失败，请稍后重试")
            .devMessage("物流信息写入失败")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.SYSTEM)
            .httpSemantic(HttpSemantic.INTERNAL_500)
            .exposeToUser(true)
            .legacyCodes(14053)
            .since("v1.3.0")
            .build()),

    /**
     * 发货数量不能大于采购数量。
     *
     * <p>触发/排查：发货数超出采购数。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    LOGISTICS_SHIPMENT_QUANTITY_EXCEEDED(46003, ErrorMeta.builder(46003, "发货数量不能大于采购数量")
            .devMessage("发货数超出采购数")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.INLINE).category(ErrorCategory.VALIDATION)
            .httpSemantic(HttpSemantic.BAD_REQUEST_400)
            .exposeToUser(true)
            .legacyCodes(14065)
            .since("v1.3.0")
            .build()),

    /**
     * 配送单已被绑定。
     *
     * <p>触发/排查：配送单已绑定其它配送员。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    LOGISTICS_ORDER_BOUND(46004, ErrorMeta.builder(46004, "配送单已被绑定")
            .devMessage("配送单已绑定其它配送员")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.CONFLICT)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(11017)
            .since("v1.3.0")
            .build()),

    /**
     * 配送单不允许绑定。
     *
     * <p>触发/排查：配送单状态不允许绑定。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    LOGISTICS_ORDER_BIND_NOT_ALLOWED(46005, ErrorMeta.builder(46005, "配送单不允许绑定")
            .devMessage("配送单状态不允许绑定")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.CONFLICT)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(11018)
            .since("v1.3.0")
            .build()),

    /**
     * 配送单已完成，无法操作。
     *
     * <p>触发/排查：配送单已处于完成状态。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    LOGISTICS_ORDER_COMPLETED(46006, ErrorMeta.builder(46006, "配送单已完成，无法操作")
            .devMessage("配送单已处于完成状态")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.CONFLICT)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(11019)
            .since("v1.3.0")
            .build()),

    /**
     * 配送单异常，请联系管理员。
     *
     * <p>触发/排查：配送单数据异常无法处理。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    LOGISTICS_ORDER_ERROR(46007, ErrorMeta.builder(46007, "配送单异常，请联系管理员")
            .devMessage("配送单数据异常无法处理")
            .level(ErrorLevel.ERROR).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.SYSTEM)
            .httpSemantic(HttpSemantic.INTERNAL_500)
            .exposeToUser(true)
            .legacyCodes(11020)
            .since("v1.3.0")
            .build()),

    /**
     * 该订单不是同城配送订单。
     *
     * <p>触发/排查：配送范围限定同城。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    LOGISTICS_NOT_SAME_CITY(46008, ErrorMeta.builder(46008, "该订单不是同城配送订单")
            .devMessage("配送范围限定同城")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.BIZ)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(11021)
            .since("v1.3.0")
            .build()),

    /**
     * 配送单未完结，无法删除。
     *
     * <p>触发/排查：删除时配送单尚未完结。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    LOGISTICS_ORDER_NOT_COMPLETED(46009, ErrorMeta.builder(46009, "配送单未完结，无法删除")
            .devMessage("删除时配送单尚未完结")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.CONFLICT)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(11013)
            .since("v1.3.0")
            .build()),

    /**
     * 请勿重复确认配送单。
     *
     * <p>触发/排查：重复提交配送确认。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    LOGISTICS_CONFIRM_DUPLICATE(46010, ErrorMeta.builder(46010, "请勿重复确认配送单")
            .devMessage("重复提交配送确认")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.CONFLICT)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(11014)
            .since("v1.3.0")
            .build()),

    /**
     * 配送已开始，请勿重复操作。
     *
     * <p>触发/排查：重复触发开始配送。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    LOGISTICS_DELIVERY_STARTED(46011, ErrorMeta.builder(46011, "配送已开始，请勿重复操作")
            .devMessage("重复触发开始配送")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.CONFLICT)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(11022)
            .since("v1.3.0")
            .build()),

    ;

    private final int code;
    private final ErrorMeta meta;

    LogisticsErrorCode(int code, ErrorMeta meta) {
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
