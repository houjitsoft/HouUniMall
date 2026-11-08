package com.houjiang.plugin.publish.exception;

import com.houjiang.plugin.publish.model.ValidationResult;

/**
 * 发布链路业务异常：统一携带 3001-3018 体系错误码，可选携带字段级校验结果。
 *
 * <p>Controller 统一捕获它，转成 {@code ResponseResult.fail(code, message)}，
 * 有字段级问题时把 {@code validations} 一并放进 data，供前端逐字段提示。
 *
 * @author Amos
 * @since M1
 */
public class PluginPublishException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 错误码（{@code PluginDistErrorCode}）。
     */
    private final int code;

    /**
     * 字段级校验结果，可为 null。
     */
    private final ValidationResult validations;

    /**
     * 构造异常。
     *
     * @param code    错误码
     * @param message 中文描述
     */
    public PluginPublishException(int code, String message) {
        this(code, message, null);
    }

    /**
     * 构造异常。
     *
     * @param code        错误码
     * @param message     中文描述
     * @param validations 字段级校验结果，可为 null
     */
    public PluginPublishException(int code, String message, ValidationResult validations) {
        super(message);
        this.code = code;
        this.validations = validations;
    }

    /**
     * @return 错误码
     */
    public int getCode() {
        return code;
    }

    /**
     * @return 字段级校验结果，可能为 null
     */
    public ValidationResult getValidations() {
        return validations;
    }
}
