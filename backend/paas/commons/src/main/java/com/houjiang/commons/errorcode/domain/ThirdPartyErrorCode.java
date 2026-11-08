package com.houjiang.commons.errorcode.domain;

import com.houjiang.commons.errorcode.ErrorCategory;
import com.houjiang.commons.errorcode.ErrorCodeDef;
import com.houjiang.commons.errorcode.ErrorLevel;
import com.houjiang.commons.errorcode.ErrorMeta;
import com.houjiang.commons.errorcode.HttpSemantic;
import com.houjiang.commons.errorcode.UiAction;

/**
 * THIRDPARTY（域 23）第三方集成错误码，号段 23001-23999。
 *
 * <p>解决的问题：微信 / 支付宝 / 短信网关 / 对象存储返回的原始错误描述被直接回显给用户，
 * 既看不懂也可能泄露商户号、签名等内部信息（PRD §6.3「第三方原文禁止出屏」）。
 * 本域统一收口为「第三方服务异常，请稍后再试」这类泛化文案，原文只进日志。
 *
 * @author Amos
 * @since v1.2.0
 */
public enum ThirdPartyErrorCode implements ErrorCodeDef {

    /**
     * 第三方账号/主体不存在（商户号、应用号未配置或已被注销）。
     *
     * <p>触发：调用第三方时返回账号不存在。用户看到「服务未开通，请联系管理员」。
     *
     * <p>排查：确认 thirdparty 模块里的第三方账号配置是否正确。
     */
    THIRDPARTY_ACCOUNT_NOT_FOUND(23001, ErrorMeta.builder(23001, "服务未开通，请联系管理员")
            .devMessage("第三方账号/主体不存在或未配置")
            .level(ErrorLevel.FATAL).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.UPSTREAM)
            .httpSemantic(HttpSemantic.INTERNAL_500)
            .exposeToUser(true)
            .troubleshooting("1. 检查 thirdparty 模块的第三方账号配置；2. 确认商户号/应用号未被注销。")
            .since("v1.2.0")
            .build()),

    /**
     * 传给第三方的参数不合法。
     *
     * <p>触发：第三方返回参数类错误。用户看到「提交内容有误，请检查后重试」。
     *
     * <p>排查：确认组装给第三方的参数符合其最新文档。
     */
    THIRDPARTY_PARAM_INVALID(23002, ErrorMeta.builder(23002, "提交内容有误，请检查后重试")
            .devMessage("第三方返回参数校验失败")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.UPSTREAM)
            .httpSemantic(HttpSemantic.BAD_REQUEST_400)
            .exposeToUser(true)
            .since("v1.2.0")
            .build()),

    /**
     * 第三方调用失败（通用）。
     *
     * <p>触发：调用第三方接口失败且未细分渠道。用户看到「第三方服务异常，请稍后再试」。
     *
     * <p>排查：<b>第三方返回的原始错误描述不得出屏</b>，只在日志里查。
     */
    THIRDPARTY_CALL_FAILED(23008, ErrorMeta.builder(23008, "第三方服务异常，请稍后再试")
            .devMessage("第三方接口调用失败，原始错误描述见服务端日志")
            .level(ErrorLevel.ERROR).retryable(true)
            .uiAction(UiAction.SILENT_RETRY).category(ErrorCategory.UPSTREAM)
            .httpSemantic(HttpSemantic.UNAVAILABLE_503)
            .exposeToUser(true)
            .troubleshooting("1. 从日志取第三方原始返回；2. 确认网络出口与白名单；3. 确认配额未耗尽。")
            .since("v1.2.0")
            .build()),

    /**
     * 第三方配置缺失（AppId、密钥、回调地址未配置）。
     *
     * <p>触发：发起调用前发现必要配置为空。用户看到「服务未配置，请联系管理员」。
     *
     * <p>排查：补配置后需重启或刷新配置缓存。FATAL 级别需人工介入。
     */
    THIRDPARTY_CONFIG_MISSING(23009, ErrorMeta.builder(23009, "服务未配置，请联系管理员")
            .devMessage("第三方必要配置为空（AppId / 密钥 / 回调地址）")
            .level(ErrorLevel.FATAL).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.UPSTREAM)
            .httpSemantic(HttpSemantic.INTERNAL_500)
            .exposeToUser(true)
            .troubleshooting("1. 检查 thirdparty 配置项是否下发；2. 补配置后刷新缓存或重启服务。")
            .legacyCodes(10053)
            .since("v1.2.0")
            .build()),

    /**
     * 微信接口调用失败。
     *
     * <p>触发：微信开放平台 / 支付接口返回错误。用户看到「微信服务异常，请稍后再试」。
     *
     * <p>排查：微信返回的 errcode 只进日志，不出屏。
     */
    THIRDPARTY_WECHAT_CALL_FAILED(23010, ErrorMeta.builder(23010, "微信服务异常，请稍后再试")
            .devMessage("微信接口返回错误码，errcode 见服务端日志")
            .level(ErrorLevel.ERROR).retryable(true)
            .uiAction(UiAction.SILENT_RETRY).category(ErrorCategory.UPSTREAM)
            .httpSemantic(HttpSemantic.UNAVAILABLE_503)
            .exposeToUser(true)
            .troubleshooting("1. 从日志取微信 errcode/errmsg；2. 对照微信官方错误码表处理。")
            .since("v1.2.0")
            .build()),

    /**
     * 支付宝接口调用失败。
     *
     * <p>触发：支付宝开放接口返回错误。用户看到「支付服务异常，请稍后再试」。
     *
     * <p>排查：支付宝返回的 sub_code 只进日志，不出屏。
     */
    THIRDPARTY_ALIPAY_CALL_FAILED(23011, ErrorMeta.builder(23011, "支付服务异常，请稍后再试")
            .devMessage("支付宝接口返回错误码，sub_code 见服务端日志")
            .level(ErrorLevel.ERROR).retryable(true)
            .uiAction(UiAction.SILENT_RETRY).category(ErrorCategory.UPSTREAM)
            .httpSemantic(HttpSemantic.UNAVAILABLE_503)
            .exposeToUser(true)
            .troubleshooting("1. 从日志取支付宝 sub_code/sub_msg；2. 对照支付宝官方错误码表处理。")
            .since("v1.2.0")
            .build()),

    /**
     * 短信发送失败。
     *
     * <p>触发：短信网关返回失败。用户看到「短信发送失败，请稍后重试」。
     *
     * <p>排查：注意与「短信发送频率过高」（RATE_LIMIT 域 25011）区分，这里是网关侧失败。
     */
    THIRDPARTY_SMS_SEND_FAILED(23012, ErrorMeta.builder(23012, "短信发送失败，请稍后重试")
            .devMessage("短信网关返回失败，原始回执见服务端日志")
            .level(ErrorLevel.ERROR).retryable(true)
            .uiAction(UiAction.TOAST).category(ErrorCategory.UPSTREAM)
            .httpSemantic(HttpSemantic.UNAVAILABLE_503)
            .exposeToUser(true)
            .troubleshooting("1. 检查短信余额与签名模板；2. 确认手机号未被黑名单；3. 查看网关回执。")
            .legacyCodes(8004, 10015, 10064)
            .since("v1.2.0")
            .build()),

    /**
     * 对象存储调用失败（上传/下载/签名 URL）。
     *
     * <p>触发：OSS/COS 返回错误。用户看到「文件服务异常，请稍后再试」。
     *
     * <p>排查：确认 AK/SK 与 Bucket 权限；文件类问题优先看 FILE 域（24）。
     */
    THIRDPARTY_OSS_CALL_FAILED(23013, ErrorMeta.builder(23013, "文件服务异常，请稍后再试")
            .devMessage("对象存储接口返回错误，原始描述见服务端日志")
            .level(ErrorLevel.ERROR).retryable(true)
            .uiAction(UiAction.SILENT_RETRY).category(ErrorCategory.UPSTREAM)
            .httpSemantic(HttpSemantic.UNAVAILABLE_503)
            .exposeToUser(true)
            .troubleshooting("1. 检查 AK/SK 是否过期；2. 确认 Bucket 权限与地域；3. 查看原始返回。")
            .legacyCodes(10066)
            .since("v1.2.0")
            .build())
    ;

    private final int code;
    private final ErrorMeta meta;

    ThirdPartyErrorCode(int code, ErrorMeta meta) {
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
