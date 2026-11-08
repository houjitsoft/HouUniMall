package com.houjiang.commons.errorcode.domain;

import com.houjiang.commons.errorcode.ErrorCategory;
import com.houjiang.commons.errorcode.ErrorCodeDef;
import com.houjiang.commons.errorcode.ErrorLevel;
import com.houjiang.commons.errorcode.ErrorMeta;
import com.houjiang.commons.errorcode.HttpSemantic;
import com.houjiang.commons.errorcode.UiAction;

/**
 * VALIDATION（域 21）参数校验错误码，号段 21001-21999。
 *
 * <p>解决的问题：commons 的 GlobalExceptionHandler 用
 * {@code ResponseResult.fail(CommonStatusEnum.FAIL.getCode(), "参数验证失败")} 返回参数错误 ——
 * 码值是 0（失败码），语义完全不明；bff-web 又用裸码 400 表示「请求方法不支持」。
 * 本域把参数与方法级校验登记成独立号段，前端统一走「表单行内错误」展示。
 *
 * @author Amos
 * @since v1.2.0
 */
public enum ValidationErrorCode implements ErrorCodeDef {

    /**
     * 参数校验失败（字段缺失、格式错误、取值非法）。
     *
     * <p>触发：{@code MethodArgumentNotValidException} / {@code ValidationException} 命中。
     * 用户看到「请求参数不合法，请检查后重试」，字段级错误会同时放在 data 里做行内提示。
     *
     * <p>排查：优先看 data 中的 fieldErrors；旧码 0 是通用失败码，语义不明，本码取而代之。
     */
    PARAM_INVALID(21002, ErrorMeta.builder(21002, "请求参数不合法，请检查后重试")
            .devMessage("MethodArgumentNotValidException / ValidationException：参数校验未通过")
            .level(ErrorLevel.INFO).retryable(true)
            .uiAction(UiAction.INLINE).category(ErrorCategory.VALIDATION)
            .httpSemantic(HttpSemantic.BAD_REQUEST_400)
            .exposeToUser(true)
            .legacyCodes(400, 422, 10001, 10043, 10048, 10049, 10051, 10052, 11017, 11018, 11030, 11041, 15010, 18009, 18016)
            .since("v1.2.0")
            .remark("替代 handler 里 CommonStatusEnum.FAIL(0) + 「参数验证失败」的裸码出口；"
                    + "HTTP 裸码 400（Bad Request）与 422（参数不可处理）一并收口到本码")
            .build()),

    /**
     * HTTP 请求方法不支持（GET 打开了只接受 POST 的接口）。
     *
     * <p>触发：{@code HttpRequestMethodNotSupportedException}。用户看到「请求方式不正确，请重试」。
     *
     * <p>排查：确认前端调用方式与后端 Mapping 一致；常见于在浏览器地址栏直接打开 /web/... 数据接口。
     * 旧码 400 是 HTTP 裸码，本码收口。
     */
    METHOD_NOT_ALLOWED(21003, ErrorMeta.builder(21003, "请求方式不正确，请重试")
            .devMessage("HttpRequestMethodNotSupportedException：请求方法与 Mapping 不匹配")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.VALIDATION)
            .httpSemantic(HttpSemantic.METHOD_NOT_ALLOWED_405)
            .exposeToUser(true)
            .legacyCodes(405)
            .since("v1.2.0")
            .remark("bff-web 历史上用 HTTP 裸码 400 表示「请求方法不支持」，这是误用：400 是 Bad Request，"
                    + "方法不支持是 405。本码按正确语义认领 405，裸码 400 已归口到 21002 PARAM_INVALID")
            .build()),

    /**
     * 字段类型不匹配（字符串传给了数字字段等）。
     *
     * <p>触发：参数绑定阶段类型转换失败。用户看到「字段类型不正确，请检查输入」。
     *
     * <p>排查：对比前端表单控件与后端 DTO 字段类型，重点看日期与金额。
     */
    TYPE_MISMATCH(21004, ErrorMeta.builder(21004, "字段类型不正确，请检查输入")
            .devMessage("参数绑定阶段类型转换失败")
            .level(ErrorLevel.INFO).retryable(true)
            .uiAction(UiAction.INLINE).category(ErrorCategory.VALIDATION)
            .httpSemantic(HttpSemantic.BAD_REQUEST_400)
            .exposeToUser(true)
            .since("v1.2.0")
            .build()),

    /**
     * 取值超出允许范围（分页大小、数量、金额区间）。
     *
     * <p>触发：参数合法但超出业务约定的上下限。用户看到「取值超出允许范围」。
     *
     * <p>排查：确认前后端对上限的约定是否一致。
     */
    VALUE_OUT_OF_RANGE(21005, ErrorMeta.builder(21005, "取值超出允许范围，请调整")
            .devMessage("参数取值超出业务约定的上下限")
            .level(ErrorLevel.INFO).retryable(true)
            .uiAction(UiAction.INLINE).category(ErrorCategory.VALIDATION)
            .httpSemantic(HttpSemantic.BAD_REQUEST_400)
            .exposeToUser(true)
            .legacyCodes(10022, 10041, 11014, 11082, 15004, 15019, 15026, 15033)
            .since("v1.2.0")
            .build()),

    /**
     * 格式不正确（手机号、邮箱、身份证等正则校验未通过）。
     *
     * <p>触发：Bean Validation 的格式注解未通过。用户看到「填写格式不正确，请检查」。
     *
     * <p>排查：确认前端与后端使用同一套正则，避免前端放过后端拦。
     */
    FORMAT_INVALID(21006, ErrorMeta.builder(21006, "填写格式不正确，请检查")
            .devMessage("格式类校验注解未通过")
            .level(ErrorLevel.INFO).retryable(true)
            .uiAction(UiAction.INLINE).category(ErrorCategory.VALIDATION)
            .httpSemantic(HttpSemantic.BAD_REQUEST_400)
            .exposeToUser(true)
            .legacyCodes(10007, 10040, 10070, 10090, 11072)
            .since("v1.2.0")
            .build()),

    /**
     * 请求体缺失（POST/PUT 没有传 body）。
     *
     * <p>触发：{@code HttpMessageNotReadableException}。用户看到「请求内容为空，请重新提交」。
     *
     * <p>排查：确认 Content-Type 与请求体是否同时正确。
     */
    BODY_MISSING(21010, ErrorMeta.builder(21010, "请求内容为空，请重新提交")
            .devMessage("请求体缺失或不可读")
            .level(ErrorLevel.INFO).retryable(true)
            .uiAction(UiAction.INLINE).category(ErrorCategory.VALIDATION)
            .httpSemantic(HttpSemantic.BAD_REQUEST_400)
            .exposeToUser(true)
            .since("v1.2.0")
            .build()),

    /**
     * JSON 解析失败（请求体不是合法 JSON）。
     *
     * <p>触发：JSON 反序列化报错。用户看到「提交内容格式有误，请重试」。
     *
     * <p>排查：让前端打印原始请求体；常见于手工拼字符串而非 JSON.stringify。
     */
    JSON_PARSE_FAILED(21011, ErrorMeta.builder(21011, "提交内容格式有误，请重试")
            .devMessage("请求体 JSON 反序列化失败")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.INLINE).category(ErrorCategory.VALIDATION)
            .httpSemantic(HttpSemantic.BAD_REQUEST_400)
            .exposeToUser(true)
            .since("v1.2.0")
            .build()),
    /**
     * 内容长度超出限制，请精简。
     *
     * <p>触发/排查：文本长度超过业务上限。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    LENGTH_EXCEEDED(21007, ErrorMeta.builder(21007, "内容长度超出限制，请精简")
            .devMessage("文本长度超过业务上限")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.INLINE).category(ErrorCategory.VALIDATION)
            .httpSemantic(HttpSemantic.BAD_REQUEST_400)
            .exposeToUser(true)
            .legacyCodes(17036)
            .since("v1.3.0")
            .build()),

    /**
     * 数量超出允许上限，请调整。
     *
     * <p>触发/排查：条目/数量超过业务上限。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    COUNT_EXCEEDED(21012, ErrorMeta.builder(21012, "数量超出允许上限，请调整")
            .devMessage("条目/数量超过业务上限")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.INLINE).category(ErrorCategory.VALIDATION)
            .httpSemantic(HttpSemantic.BAD_REQUEST_400)
            .exposeToUser(true)
            .legacyCodes(10035, 10046, 10068, 11073)
            .since("v1.3.0")
            .build()),


    FLOOR_GOODS_COUNT_EXCEEDED(21801, ErrorMeta.builder(21801, "层商品数量不允许超过8个")
            .devMessage("楼层绑定的商品数量超过上限 8")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.INLINE).category(ErrorCategory.VALIDATION)
            .httpSemantic(HttpSemantic.BAD_REQUEST_400)
            .exposeToUser(true)
            .since("v1.3.0")
            .legacyCodes(10036)
            .build()),
    FLOOR_GOODS_COUNT_TOO_FEW(21802, ErrorMeta.builder(21802, "楼层商品数量不允许小于0个")
            .devMessage("楼层绑定的商品数量小于下限 0")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.INLINE).category(ErrorCategory.VALIDATION)
            .httpSemantic(HttpSemantic.BAD_REQUEST_400)
            .exposeToUser(true)
            .since("v1.3.0")
            .legacyCodes(10037)
            .build()),
    ;

    private final int code;
    private final ErrorMeta meta;

    ValidationErrorCode(int code, ErrorMeta meta) {
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
