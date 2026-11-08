package com.houjiang.commons.exception;

import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.errorcode.domain.AuthErrorCode;
import com.houjiang.commons.errorcode.domain.SystemErrorCode;
import com.houjiang.commons.errorcode.domain.ThirdPartyErrorCode;
import com.houjiang.commons.errorcode.domain.UpstreamErrorCode;

/**
 * GlobalExceptionEnum —— 统一错误码的<b>引用入口</b>，不再是第二套码源。
 *
 * <p>第二阶段重构后的形态：
 * <ul>
 *   <li>每一个常量的 code / msg 都<b>直接取自</b> {@code com.houjiang.commons.errorcode.domain}
 *       下的统一码枚举（{@code XxxErrorCode.CONST.code()} / {@code .meta().userMessage()}），
 *       不手写数字。统一码改了，本枚举自动跟着变，不可能漂移。</li>
 *   <li>只包含<b>活项</b>：在 backend 与 plugins 的真实代码里被调用过的常量。
 *       没人引用的死项已全部删除 —— 死项留着就是第二套码源，等于没重构。</li>
 *   <li>标注 {@code @deprecated} 的常量仅供存量调用点过渡，
 *       新代码请直接使用对应的 {@code XxxErrorCode}。</li>
 *   <li>成功语义的常量<b>不属于错误码体系</b>，码值保持历史值不变，
 *       不能编成 5 位错误码（会把成功判成失败）。</li>
 * </ul>
 *
 * <p>运行时<b>不做任何旧码→新码翻译</b>：业务代码拿到的码值就是统一码，
 * wire 上不再出现历史码值（成功语义的 0/1/2 等除外）。
 *
 * @author Amos
 * @since v1.0.0（历史枚举）/ v1.3.0（改为引用统一码）
 */
public enum GlobalExceptionEnum {

    /**
     * 系统异常，请联系客服。
     *
     * <p>引用统一码 {@link SystemErrorCode#UNKNOWN_ERROR}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link SystemErrorCode#UNKNOWN_ERROR}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    SYSTEM_ERROR(SystemErrorCode.UNKNOWN_ERROR.code(), SystemErrorCode.UNKNOWN_ERROR.meta().userMessage()),

    /**
     * 数据库sql异常。
     *
     * <p>引用统一码 {@link SystemErrorCode#SQL_GRAMMAR_ERROR}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link SystemErrorCode#SQL_GRAMMAR_ERROR}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    MYSQL_EXCEPTION(SystemErrorCode.SQL_GRAMMAR_ERROR.code(), SystemErrorCode.SQL_GRAMMAR_ERROR.meta().userMessage()),

    /**
     * 网络有点不顺畅，小蜂君正在快马加鞭的路上~。
     *
     * <p>引用统一码 {@link UpstreamErrorCode#SERVICE_UNREACHABLE}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link UpstreamErrorCode#SERVICE_UNREACHABLE}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    BLOCKING_EXCEPTION(UpstreamErrorCode.SERVICE_UNREACHABLE.code(), UpstreamErrorCode.SERVICE_UNREACHABLE.meta().userMessage()),

    /**
     * 短信发送失败，请稍后再试。
     *
     * <p>引用统一码 {@link ThirdPartyErrorCode#THIRDPARTY_SMS_SEND_FAILED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link ThirdPartyErrorCode#THIRDPARTY_SMS_SEND_FAILED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    SMS_EXCEPTION(ThirdPartyErrorCode.THIRDPARTY_SMS_SEND_FAILED.code(), ThirdPartyErrorCode.THIRDPARTY_SMS_SEND_FAILED.meta().userMessage()),

    /**
     * 数据库中数据重复。
     *
     * <p>引用统一码 {@link SystemErrorCode#SYSTEM_DUPLICATE_KEY}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link SystemErrorCode#SYSTEM_DUPLICATE_KEY}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    MYSQL_EXCEPTION_DATA_DUPLICATION(SystemErrorCode.SYSTEM_DUPLICATE_KEY.code(), SystemErrorCode.SYSTEM_DUPLICATE_KEY.meta().userMessage()),

    /**
     * 登录失败，请重新登录。
     *
     * <p>引用统一码 {@link AuthErrorCode#LOGIN_FAILED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link AuthErrorCode#LOGIN_FAILED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    NO_FORBIDDEN(AuthErrorCode.LOGIN_FAILED.code(), AuthErrorCode.LOGIN_FAILED.meta().userMessage()),
    ;

    private Integer errorCode;

    private String errorMsg;

    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    private GlobalExceptionEnum(Integer errorCode, String errorMsg) {
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    private GlobalExceptionEnum() {
    }

    public ResponseResult getResult(Integer errorCode, String errorMsg) {
        return ResponseResult.fail(errorCode, errorMsg);
    }

}
