package com.houjiang.commons.exception;

import com.houjiang.commons.errorcode.domain.AfterSaleErrorCode;
import com.houjiang.commons.errorcode.domain.AuthErrorCode;
import com.houjiang.commons.errorcode.domain.CartErrorCode;
import com.houjiang.commons.errorcode.domain.ConfigErrorCode;
import com.houjiang.commons.errorcode.domain.FileErrorCode;
import com.houjiang.commons.errorcode.domain.GoodsErrorCode;
import com.houjiang.commons.errorcode.domain.InventoryErrorCode;
import com.houjiang.commons.errorcode.domain.JobErrorCode;
import com.houjiang.commons.errorcode.domain.LogisticsErrorCode;
import com.houjiang.commons.errorcode.domain.MarketingErrorCode;
import com.houjiang.commons.errorcode.domain.MerchantErrorCode;
import com.houjiang.commons.errorcode.domain.MessageErrorCode;
import com.houjiang.commons.errorcode.domain.OrderErrorCode;
import com.houjiang.commons.errorcode.domain.OrgErrorCode;
import com.houjiang.commons.errorcode.domain.PaymentErrorCode;
import com.houjiang.commons.errorcode.domain.PermissionErrorCode;
import com.houjiang.commons.errorcode.domain.PluginPlatformErrorCode;
import com.houjiang.commons.errorcode.domain.RateLimitErrorCode;
import com.houjiang.commons.errorcode.domain.SecurityErrorCode;
import com.houjiang.commons.errorcode.domain.SystemErrorCode;
import com.houjiang.commons.errorcode.domain.ThirdPartyErrorCode;
import com.houjiang.commons.errorcode.domain.UserErrorCode;
import com.houjiang.commons.errorcode.domain.ValidationErrorCode;

/**
 * BizCodeEnume —— 统一错误码的<b>引用入口</b>，不再是第二套码源。
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
public enum BizCodeEnume {

    /**
     * 系统未知异常。
     *
     * <p>引用统一码 {@link SystemErrorCode#UNKNOWN_ERROR}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link SystemErrorCode#UNKNOWN_ERROR}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    UNKNOW_EXCEPTION(SystemErrorCode.UNKNOWN_ERROR.code(), SystemErrorCode.UNKNOWN_ERROR.meta().userMessage()),

    /**
     * 参数格式异常。
     *
     * <p>引用统一码 {@link ValidationErrorCode#PARAM_INVALID}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link ValidationErrorCode#PARAM_INVALID}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    VALID_EXCEPTION(ValidationErrorCode.PARAM_INVALID.code(), ValidationErrorCode.PARAM_INVALID.meta().userMessage()),

    /**
     * token非法。
     *
     * <p>引用统一码 {@link AuthErrorCode#TOKEN_ILLEGAL}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link AuthErrorCode#TOKEN_ILLEGAL}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    TOKEN_IS_ILLEGAL(AuthErrorCode.TOKEN_ILLEGAL.code(), AuthErrorCode.TOKEN_ILLEGAL.meta().userMessage()),

    /**
     * 短信发送频率太高，稍等一会发送!。
     *
     * <p>引用统一码 {@link RateLimitErrorCode#SMS_TOO_FREQUENT}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link RateLimitErrorCode#SMS_TOO_FREQUENT}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    VALID_SMS_EXCEPTION(RateLimitErrorCode.SMS_TOO_FREQUENT.code(), RateLimitErrorCode.SMS_TOO_FREQUENT.meta().userMessage()),

    /**
     * 发送成功。
     *
     * <p><b>成功语义，不属于错误码体系</b>：码值保持历史值 10004 不变。
     * 它不是一个错误码，不能编成 5 位统一码 —— 那会让前端把成功判成失败。
     * 前端统一用 {@code SuccessCode.isSuccess(code)} 判定，不要用码值硬比。
     */
    SENDING_SUCCEEDED(10004, "发送成功"),

    /**
     * 该手机号或邮箱暂未注册，请前往注册页进行注册。
     *
     * <p>引用统一码 {@link AuthErrorCode#ACCOUNT_NOT_FOUND}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link AuthErrorCode#ACCOUNT_NOT_FOUND}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    MOBILE_NUMBER_NOT_EXIST(AuthErrorCode.ACCOUNT_NOT_FOUND.code(), AuthErrorCode.ACCOUNT_NOT_FOUND.meta().userMessage()),

    /**
     * SMS配置信息存在并返回成功。
     *
     * <p><b>成功语义，不属于错误码体系</b>：码值保持历史值 10006 不变。
     * 它不是一个错误码，不能编成 5 位统一码 —— 那会让前端把成功判成失败。
     * 前端统一用 {@code SuccessCode.isSuccess(code)} 判定，不要用码值硬比。
     */
    SMS_SUCCESS(10006, "SMS配置信息存在并返回成功"),

    /**
     * 密码不规范。
     *
     * <p>引用统一码 {@link ValidationErrorCode#FORMAT_INVALID}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link ValidationErrorCode#FORMAT_INVALID}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    PASSWORD_NOT_STANDARD(ValidationErrorCode.FORMAT_INVALID.code(), ValidationErrorCode.FORMAT_INVALID.meta().userMessage()),

    /**
     * 查询错误。
     *
     * <p>引用统一码 {@link SystemErrorCode#MYBATIS_ERROR}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link SystemErrorCode#MYBATIS_ERROR}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    QUERY_ERROR(SystemErrorCode.MYBATIS_ERROR.code(), SystemErrorCode.MYBATIS_ERROR.meta().userMessage()),

    /**
     * 审核错误。
     *
     * <p>引用统一码 {@link SystemErrorCode#SYSTEM_AUDIT_FAILED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link SystemErrorCode#SYSTEM_AUDIT_FAILED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    AUDIT_ERROR(SystemErrorCode.SYSTEM_AUDIT_FAILED.code(), SystemErrorCode.SYSTEM_AUDIT_FAILED.meta().userMessage()),

    /**
     * 上传错误。
     *
     * <p>引用统一码 {@link FileErrorCode#FILE_UPLOAD_FAILED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link FileErrorCode#FILE_UPLOAD_FAILED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    UPLOAD_ERROR(FileErrorCode.FILE_UPLOAD_FAILED.code(), FileErrorCode.FILE_UPLOAD_FAILED.meta().userMessage()),

    /**
     * 重复提交。
     *
     * <p>引用统一码 {@link RateLimitErrorCode#REPEAT_SUBMIT}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link RateLimitErrorCode#REPEAT_SUBMIT}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    OPERATE_ERROR(RateLimitErrorCode.REPEAT_SUBMIT.code(), RateLimitErrorCode.REPEAT_SUBMIT.meta().userMessage()),

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
     * token失效,请重新登陆。
     *
     * <p>引用统一码 {@link AuthErrorCode#TOKEN_INVALID}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link AuthErrorCode#TOKEN_INVALID}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    TOKEN_IS_INVALID(AuthErrorCode.TOKEN_INVALID.code(), AuthErrorCode.TOKEN_INVALID.meta().userMessage()),

    /**
     * 更新成功。
     *
     * <p><b>成功语义，不属于错误码体系</b>：码值保持历史值 10017 不变。
     * 它不是一个错误码，不能编成 5 位统一码 —— 那会让前端把成功判成失败。
     * 前端统一用 {@code SuccessCode.isSuccess(code)} 判定，不要用码值硬比。
     */
    UPDATE_SUCCEEDED(10017, "更新成功"),

    /**
     * 验签失败。
     *
     * <p>引用统一码 {@link SecurityErrorCode#SECURITY_SIGNATURE_INVALID}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link SecurityErrorCode#SECURITY_SIGNATURE_INVALID}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    SIGNATURE_VERIFICATION_FAILED(SecurityErrorCode.SECURITY_SIGNATURE_INVALID.code(), SecurityErrorCode.SECURITY_SIGNATURE_INVALID.meta().userMessage()),

    /**
     * OSS配置信息存在并返回成功。
     *
     * <p><b>成功语义，不属于错误码体系</b>：码值保持历史值 10019 不变。
     * 它不是一个错误码，不能编成 5 位统一码 —— 那会让前端把成功判成失败。
     * 前端统一用 {@code SuccessCode.isSuccess(code)} 判定，不要用码值硬比。
     */
    OSS_SUCCESS(10019, "OSS配置信息存在并返回成功"),

    /**
     * 上传成功。
     *
     * <p><b>成功语义，不属于错误码体系</b>：码值保持历史值 10020 不变。
     * 它不是一个错误码，不能编成 5 位统一码 —— 那会让前端把成功判成失败。
     * 前端统一用 {@code SuccessCode.isSuccess(code)} 判定，不要用码值硬比。
     */
    UPLOAD_SUCCESS(10020, "上传成功"),

    /**
     * 类型名称不可重复。
     *
     * <p>引用统一码 {@link ConfigErrorCode#CONFIG_DUPLICATE_KEY}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link ConfigErrorCode#CONFIG_DUPLICATE_KEY}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    TYPE_ERROR(ConfigErrorCode.CONFIG_DUPLICATE_KEY.code(), ConfigErrorCode.CONFIG_DUPLICATE_KEY.meta().userMessage()),

    /**
     * 数值有误！请输入1-200的整数。
     *
     * <p>引用统一码 {@link ValidationErrorCode#VALUE_OUT_OF_RANGE}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link ValidationErrorCode#VALUE_OUT_OF_RANGE}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    DISPLAYORDER_ERROR(ValidationErrorCode.VALUE_OUT_OF_RANGE.code(), ValidationErrorCode.VALUE_OUT_OF_RANGE.meta().userMessage()),

    /**
     * 删除异常。
     *
     * <p>引用统一码 {@link SystemErrorCode#DB_ACCESS_FAILED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link SystemErrorCode#DB_ACCESS_FAILED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    DELETE_ERROR(SystemErrorCode.DB_ACCESS_FAILED.code(), SystemErrorCode.DB_ACCESS_FAILED.meta().userMessage()),

    /**
     * 更新失败。
     *
     * <p>引用统一码 {@link SystemErrorCode#DB_ACCESS_FAILED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link SystemErrorCode#DB_ACCESS_FAILED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    UPDATE_ERROR(SystemErrorCode.DB_ACCESS_FAILED.code(), SystemErrorCode.DB_ACCESS_FAILED.meta().userMessage()),

    /**
     * 未获取到信息！。
     *
     * <p>引用统一码 {@link SystemErrorCode#SYSTEM_QUERY_RESULT_EMPTY}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link SystemErrorCode#SYSTEM_QUERY_RESULT_EMPTY}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    NO_INFORMATION_OBTAINED(SystemErrorCode.SYSTEM_QUERY_RESULT_EMPTY.code(), SystemErrorCode.SYSTEM_QUERY_RESULT_EMPTY.meta().userMessage()),

    /**
     * configKey值不能为空！。
     *
     * <p>引用统一码 {@link ConfigErrorCode#CONFIG_PARAM_INVALID}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link ConfigErrorCode#CONFIG_PARAM_INVALID}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    KEY_VALUE_CANNOT_BE_EMPTY(ConfigErrorCode.CONFIG_PARAM_INVALID.code(), ConfigErrorCode.CONFIG_PARAM_INVALID.meta().userMessage()),

    /**
     * token不能为空！。
     *
     * <p>引用统一码 {@link AuthErrorCode#TOKEN_MISSING}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link AuthErrorCode#TOKEN_MISSING}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    TOKEN_IS_NULL(AuthErrorCode.TOKEN_MISSING.code(), AuthErrorCode.TOKEN_MISSING.meta().userMessage()),

    /**
     * 图片类型只能是.png！。
     *
     * <p>引用统一码 {@link FileErrorCode#FILE_TYPE_NOT_ALLOWED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link FileErrorCode#FILE_TYPE_NOT_ALLOWED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    UPLOAD_TYPE_NOT_PNG(FileErrorCode.FILE_TYPE_NOT_ALLOWED.code(), FileErrorCode.FILE_TYPE_NOT_ALLOWED.meta().userMessage()),

    /**
     * 图片宽高必须一致！。
     *
     * <p>引用统一码 {@link FileErrorCode#FILE_TYPE_NOT_ALLOWED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link FileErrorCode#FILE_TYPE_NOT_ALLOWED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    EQUAL_ALTITUDE(FileErrorCode.FILE_TYPE_NOT_ALLOWED.code(), FileErrorCode.FILE_TYPE_NOT_ALLOWED.meta().userMessage()),

    /**
     * 提交审核失败！。
     *
     * <p>引用统一码 {@link SystemErrorCode#SYSTEM_APPLY_FAILED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link SystemErrorCode#SYSTEM_APPLY_FAILED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    SUBMIT_FOR_REVIEW_ERROR(SystemErrorCode.SYSTEM_APPLY_FAILED.code(), SystemErrorCode.SYSTEM_APPLY_FAILED.meta().userMessage()),

    /**
     * 请选择头像。
     *
     * <p>引用统一码 {@link FileErrorCode#FILE_NOT_PROVIDED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link FileErrorCode#FILE_NOT_PROVIDED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    PLEASE_SELECT_AN_IMAGE(FileErrorCode.FILE_NOT_PROVIDED.code(), FileErrorCode.FILE_NOT_PROVIDED.meta().userMessage()),

    /**
     * 用户名重复！。
     *
     * <p>引用统一码 {@link OrgErrorCode#ORG_ACCOUNT_DUPLICATE}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link OrgErrorCode#ORG_ACCOUNT_DUPLICATE}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    ADD_ADMIN_USER_ERROR(OrgErrorCode.ORG_ACCOUNT_DUPLICATE.code(), OrgErrorCode.ORG_ACCOUNT_DUPLICATE.meta().userMessage()),

    /**
     * 该部门下有用户，删除失败！。
     *
     * <p>引用统一码 {@link OrgErrorCode#ORG_DEPT_HAS_USER}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link OrgErrorCode#ORG_DEPT_HAS_USER}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    DELETE_ADMIN_DEPARTMENT_USER_ERROR(OrgErrorCode.ORG_DEPT_HAS_USER.code(), OrgErrorCode.ORG_DEPT_HAS_USER.meta().userMessage()),

    /**
     * 默认搜索词不能超过9个。
     *
     * <p>引用统一码 {@link ValidationErrorCode#COUNT_EXCEEDED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link ValidationErrorCode#COUNT_EXCEEDED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    PLATFROM_SEARCH_UPDATE_ERROP(ValidationErrorCode.COUNT_EXCEEDED.code(), ValidationErrorCode.COUNT_EXCEEDED.meta().userMessage()),

    /**
     * 该分类目前存在商品不能删除。
     *
     * <p>引用统一码 {@link SystemErrorCode#SYSTEM_RESOURCE_IN_USE}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link SystemErrorCode#SYSTEM_RESOURCE_IN_USE}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    ELETE_GOODS_CAT_ERROP(SystemErrorCode.SYSTEM_RESOURCE_IN_USE.code(), SystemErrorCode.SYSTEM_RESOURCE_IN_USE.meta().userMessage()),

    /**
     * 请勿重复激活。
     *
     * <p>引用统一码 {@link SystemErrorCode#SYSTEM_DUPLICATE_KEY}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link SystemErrorCode#SYSTEM_DUPLICATE_KEY}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    UPDATE_RECHARGE_CARD_ERROP(SystemErrorCode.SYSTEM_DUPLICATE_KEY.code(), SystemErrorCode.SYSTEM_DUPLICATE_KEY.meta().userMessage()),

    /**
     * 输入金额有误,正确格式： '+'增加金额，'-'扣除金额。
     *
     * <p>引用统一码 {@link ValidationErrorCode#FORMAT_INVALID}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link ValidationErrorCode#FORMAT_INVALID}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    UPDATE_USER_MONEY_ERROP(ValidationErrorCode.FORMAT_INVALID.code(), ValidationErrorCode.FORMAT_INVALID.meta().userMessage()),

    /**
     * 剩余金额为负数，不能修改。
     *
     * <p>引用统一码 {@link ValidationErrorCode#VALUE_OUT_OF_RANGE}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link ValidationErrorCode#VALUE_OUT_OF_RANGE}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    UPDATE_USER_MONEY_ERROP_TOW(ValidationErrorCode.VALUE_OUT_OF_RANGE.code(), ValidationErrorCode.VALUE_OUT_OF_RANGE.meta().userMessage()),

    /**
     * 会员标签名称不能重复。
     *
     * <p>引用统一码 {@link SystemErrorCode#SYSTEM_DUPLICATE_KEY}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link SystemErrorCode#SYSTEM_DUPLICATE_KEY}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    ADD_TAG_INFO_ERROR(SystemErrorCode.SYSTEM_DUPLICATE_KEY.code(), SystemErrorCode.SYSTEM_DUPLICATE_KEY.meta().userMessage()),

    /**
     * 未扫码。
     *
     * <p>引用统一码 {@link ValidationErrorCode#PARAM_INVALID}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link ValidationErrorCode#PARAM_INVALID}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    UNSCANNED_CODE(ValidationErrorCode.PARAM_INVALID.code(), ValidationErrorCode.PARAM_INVALID.meta().userMessage()),

    /**
     * 添加成功。
     *
     * <p><b>成功语义，不属于错误码体系</b>：码值保持历史值 10044 不变。
     * 它不是一个错误码，不能编成 5 位统一码 —— 那会让前端把成功判成失败。
     * 前端统一用 {@code SuccessCode.isSuccess(code)} 判定，不要用码值硬比。
     */
    ADD_SUCCESS(10044, "添加成功"),

    /**
     * 添加失败。
     *
     * <p>引用统一码 {@link SystemErrorCode#DB_ACCESS_FAILED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link SystemErrorCode#DB_ACCESS_FAILED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    ADD_ERROR(SystemErrorCode.DB_ACCESS_FAILED.code(), SystemErrorCode.DB_ACCESS_FAILED.meta().userMessage()),

    /**
     * 最多上传5张照片。
     *
     * <p>引用统一码 {@link ValidationErrorCode#COUNT_EXCEEDED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link ValidationErrorCode#COUNT_EXCEEDED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    UPLOAD_UP_TO_5_PHOTOS(ValidationErrorCode.COUNT_EXCEEDED.code(), ValidationErrorCode.COUNT_EXCEEDED.meta().userMessage()),

    /**
     * 没有审核信息。
     *
     * <p>引用统一码 {@link MerchantErrorCode#MERCHANT_AUDIT_INFO_NOT_FOUND}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link MerchantErrorCode#MERCHANT_AUDIT_INFO_NOT_FOUND}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    NO_AUDIT_INFORMATION(MerchantErrorCode.MERCHANT_AUDIT_INFO_NOT_FOUND.code(), MerchantErrorCode.MERCHANT_AUDIT_INFO_NOT_FOUND.meta().userMessage()),

    /**
     * 缺少参数。
     *
     * <p>引用统一码 {@link ValidationErrorCode#PARAM_INVALID}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link ValidationErrorCode#PARAM_INVALID}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    MISSING_PARAMETER(ValidationErrorCode.PARAM_INVALID.code(), ValidationErrorCode.PARAM_INVALID.meta().userMessage()),

    /**
     * shopId不能为空。
     *
     * <p>引用统一码 {@link ValidationErrorCode#PARAM_INVALID}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link ValidationErrorCode#PARAM_INVALID}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    SHOP_ID_IS_NULL(ValidationErrorCode.PARAM_INVALID.code(), ValidationErrorCode.PARAM_INVALID.meta().userMessage()),

    /**
     * 账户积分不足。
     *
     * <p>引用统一码 {@link UserErrorCode#USER_POINTS_NOT_ENOUGH}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link UserErrorCode#USER_POINTS_NOT_ENOUGH}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    PAY_INTEGRAL_ERROR(UserErrorCode.USER_POINTS_NOT_ENOUGH.code(), UserErrorCode.USER_POINTS_NOT_ENOUGH.meta().userMessage()),

    /**
     * 操作成功。
     *
     * <p><b>成功语义，不属于错误码体系</b>：码值保持历史值 1 不变。
     * 它不是一个错误码，不能编成 5 位统一码 —— 那会让前端把成功判成失败。
     * 前端统一用 {@code SuccessCode.isSuccess(code)} 判定，不要用码值硬比。
     */
    OPERATION_SUCCESSFUL(1, "操作成功"),

    /**
     * 操作失败。
     *
     * <p>引用统一码 {@link SystemErrorCode#UNKNOWN_ERROR}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link SystemErrorCode#UNKNOWN_ERROR}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    OPERATION_FAILED(SystemErrorCode.UNKNOWN_ERROR.code(), SystemErrorCode.UNKNOWN_ERROR.meta().userMessage()),

    /**
     * 审核失败。
     *
     * <p>引用统一码 {@link SystemErrorCode#SYSTEM_AUDIT_FAILED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link SystemErrorCode#SYSTEM_AUDIT_FAILED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    APPROVE_FAILED(SystemErrorCode.SYSTEM_AUDIT_FAILED.code(), SystemErrorCode.SYSTEM_AUDIT_FAILED.meta().userMessage()),

    /**
     * 数据查询为空。。
     *
     * <p>引用统一码 {@link SystemErrorCode#SYSTEM_QUERY_RESULT_EMPTY}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link SystemErrorCode#SYSTEM_QUERY_RESULT_EMPTY}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    SELECT_DATA_NULL(SystemErrorCode.SYSTEM_QUERY_RESULT_EMPTY.code(), SystemErrorCode.SYSTEM_QUERY_RESULT_EMPTY.meta().userMessage()),

    /**
     * 删除成功。
     *
     * <p><b>成功语义，不属于错误码体系</b>：码值保持历史值 10061 不变。
     * 它不是一个错误码，不能编成 5 位统一码 —— 那会让前端把成功判成失败。
     * 前端统一用 {@code SuccessCode.isSuccess(code)} 判定，不要用码值硬比。
     */
    DELETE_SUCCESS(10061, "删除成功"),

    /**
     * 请勿重复点击。
     *
     * <p>引用统一码 {@link RateLimitErrorCode#REPEAT_SUBMIT}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link RateLimitErrorCode#REPEAT_SUBMIT}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    REPEATED_SENDING(RateLimitErrorCode.REPEAT_SUBMIT.code(), RateLimitErrorCode.REPEAT_SUBMIT.meta().userMessage()),

    /**
     * 发送失败。
     *
     * <p>引用统一码 {@link ThirdPartyErrorCode#THIRDPARTY_SMS_SEND_FAILED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link ThirdPartyErrorCode#THIRDPARTY_SMS_SEND_FAILED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    SENDING_FAIL(ThirdPartyErrorCode.THIRDPARTY_SMS_SEND_FAILED.code(), ThirdPartyErrorCode.THIRDPARTY_SMS_SEND_FAILED.meta().userMessage()),

    /**
     * 初始化成功。
     *
     * <p><b>成功语义，不属于错误码体系</b>：码值保持历史值 10065 不变。
     * 它不是一个错误码，不能编成 5 位统一码 —— 那会让前端把成功判成失败。
     * 前端统一用 {@code SuccessCode.isSuccess(code)} 判定，不要用码值硬比。
     */
    OSS_STACT_SUCCESS(10065, "初始化成功"),

    /**
     * 初始化失败。
     *
     * <p>引用统一码 {@link ThirdPartyErrorCode#THIRDPARTY_OSS_CALL_FAILED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link ThirdPartyErrorCode#THIRDPARTY_OSS_CALL_FAILED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    OSS_FAIL(ThirdPartyErrorCode.THIRDPARTY_OSS_CALL_FAILED.code(), ThirdPartyErrorCode.THIRDPARTY_OSS_CALL_FAILED.meta().userMessage()),

    /**
     * 支付密码不正确，您已经尝试3次，支付密码将锁定1小时，或找回密码后重试！。
     *
     * <p>引用统一码 {@link PaymentErrorCode#PAYMENT_PASSWORD_LOCKED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link PaymentErrorCode#PAYMENT_PASSWORD_LOCKED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    PAY_PASSWORD_ERROR(PaymentErrorCode.PAYMENT_PASSWORD_LOCKED.code(), PaymentErrorCode.PAYMENT_PASSWORD_LOCKED.meta().userMessage()),

    /**
     * 导出数量过大，请联系运维。
     *
     * <p>引用统一码 {@link SystemErrorCode#SYSTEM_EXPORT_FAILED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link SystemErrorCode#SYSTEM_EXPORT_FAILED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    EXPORT_QUANTITY_IS_TOO_LARGE(SystemErrorCode.SYSTEM_EXPORT_FAILED.code(), SystemErrorCode.SYSTEM_EXPORT_FAILED.meta().userMessage()),

    /**
     * 请小于三万条。
     *
     * <p>引用统一码 {@link ValidationErrorCode#COUNT_EXCEEDED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link ValidationErrorCode#COUNT_EXCEEDED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    PLEASE_BE_LESS_THAN_30000_PIECES(ValidationErrorCode.COUNT_EXCEEDED.code(), ValidationErrorCode.COUNT_EXCEEDED.meta().userMessage()),

    /**
     * 手机号或邮箱输入错误，请新输入。
     *
     * <p>引用统一码 {@link ValidationErrorCode#FORMAT_INVALID}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link ValidationErrorCode#FORMAT_INVALID}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    MOBILE_EMAIL_IS_ERROR(ValidationErrorCode.FORMAT_INVALID.code(), ValidationErrorCode.FORMAT_INVALID.meta().userMessage()),

    /**
     * 未设置任务名称!。
     *
     * <p>引用统一码 {@link JobErrorCode#JOB_PARAM_INVALID}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link JobErrorCode#JOB_PARAM_INVALID}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    NO_SET_JOB_NAME(JobErrorCode.JOB_PARAM_INVALID.code(), JobErrorCode.JOB_PARAM_INVALID.meta().userMessage()),

    /**
     * 未选择调用任务!。
     *
     * <p>引用统一码 {@link JobErrorCode#JOB_PARAM_INVALID}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link JobErrorCode#JOB_PARAM_INVALID}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    NO_SET_JOB_INVOKETARGET(JobErrorCode.JOB_PARAM_INVALID.code(), JobErrorCode.JOB_PARAM_INVALID.meta().userMessage()),

    /**
     * 未设置月!。
     *
     * <p>引用统一码 {@link JobErrorCode#JOB_PARAM_INVALID}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link JobErrorCode#JOB_PARAM_INVALID}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    NO_SET_JOB_MOUTH(JobErrorCode.JOB_PARAM_INVALID.code(), JobErrorCode.JOB_PARAM_INVALID.meta().userMessage()),

    /**
     * 未设置周!。
     *
     * <p>引用统一码 {@link JobErrorCode#JOB_PARAM_INVALID}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link JobErrorCode#JOB_PARAM_INVALID}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    NO_SET_JOB_WEEK(JobErrorCode.JOB_PARAM_INVALID.code(), JobErrorCode.JOB_PARAM_INVALID.meta().userMessage()),

    /**
     * 未设置日!。
     *
     * <p>引用统一码 {@link JobErrorCode#JOB_PARAM_INVALID}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link JobErrorCode#JOB_PARAM_INVALID}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    NO_SET_JOB_DAY(JobErrorCode.JOB_PARAM_INVALID.code(), JobErrorCode.JOB_PARAM_INVALID.meta().userMessage()),

    /**
     * 未设置时!。
     *
     * <p>引用统一码 {@link JobErrorCode#JOB_PARAM_INVALID}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link JobErrorCode#JOB_PARAM_INVALID}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    NO_SET_JOB_HOUR(JobErrorCode.JOB_PARAM_INVALID.code(), JobErrorCode.JOB_PARAM_INVALID.meta().userMessage()),

    /**
     * 未设置分!。
     *
     * <p>引用统一码 {@link JobErrorCode#JOB_PARAM_INVALID}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link JobErrorCode#JOB_PARAM_INVALID}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    NO_SET_JOB_MIN(JobErrorCode.JOB_PARAM_INVALID.code(), JobErrorCode.JOB_PARAM_INVALID.meta().userMessage()),

    /**
     * 该任务已经添加！！。
     *
     * <p>引用统一码 {@link SystemErrorCode#SYSTEM_DUPLICATE_KEY}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link SystemErrorCode#SYSTEM_DUPLICATE_KEY}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    JOB_IS_EXIST(SystemErrorCode.SYSTEM_DUPLICATE_KEY.code(), SystemErrorCode.SYSTEM_DUPLICATE_KEY.meta().userMessage()),

    /**
     * 该定时任务还未关闭，请稍后再试！。
     *
     * <p>引用统一码 {@link JobErrorCode#JOB_STATE_NOT_ALLOWED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link JobErrorCode#JOB_STATE_NOT_ALLOWED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    JOB_NO_YET_CLOSED(JobErrorCode.JOB_STATE_NOT_ALLOWED.code(), JobErrorCode.JOB_STATE_NOT_ALLOWED.meta().userMessage()),

    /**
     * 快递公司或者快递公司首字符不能重复。
     *
     * <p>引用统一码 {@link SystemErrorCode#SYSTEM_DUPLICATE_KEY}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link SystemErrorCode#SYSTEM_DUPLICATE_KEY}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    FIRST_CHARACTER_NOT_REPEAT(SystemErrorCode.SYSTEM_DUPLICATE_KEY.code(), SystemErrorCode.SYSTEM_DUPLICATE_KEY.meta().userMessage()),

    /**
     * 文件大小超过2M，无法上传。
     *
     * <p>引用统一码 {@link FileErrorCode#FILE_SIZE_EXCEEDED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link FileErrorCode#FILE_SIZE_EXCEEDED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    FILE_SIZE_GT_TWO(FileErrorCode.FILE_SIZE_EXCEEDED.code(), FileErrorCode.FILE_SIZE_EXCEEDED.meta().userMessage()),

    /**
     * 手机号码格式不正确，请重新输入！。
     *
     * <p>引用统一码 {@link ValidationErrorCode#FORMAT_INVALID}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link ValidationErrorCode#FORMAT_INVALID}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    INCORRECT_PHONE_NUMBER_FORMAT(ValidationErrorCode.FORMAT_INVALID.code(), ValidationErrorCode.FORMAT_INVALID.meta().userMessage()),

    /**
     * 校验成功。
     *
     * <p><b>成功语义，不属于错误码体系</b>：码值保持历史值 10092 不变。
     * 它不是一个错误码，不能编成 5 位统一码 —— 那会让前端把成功判成失败。
     * 前端统一用 {@code SuccessCode.isSuccess(code)} 判定，不要用码值硬比。
     */
    VERIFICATION_SUCCESSFUL(10092, "校验成功"),

    /**
     * 使用中，请勿删除。
     *
     * <p>引用统一码 {@link SystemErrorCode#SYSTEM_RESOURCE_IN_USE}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link SystemErrorCode#SYSTEM_RESOURCE_IN_USE}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    IS_USE_NOT_DELETE(SystemErrorCode.SYSTEM_RESOURCE_IN_USE.code(), SystemErrorCode.SYSTEM_RESOURCE_IN_USE.meta().userMessage()),

    /**
     * 您的账号已在其他设备上登录，如非本人操作，请立即修改密码。。
     *
     * <p>引用统一码 {@link AuthErrorCode#TOKEN_INVALID}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link AuthErrorCode#TOKEN_INVALID}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    TOKEN_IS_OFFSITE_LOGIN(AuthErrorCode.TOKEN_INVALID.code(), AuthErrorCode.TOKEN_INVALID.meta().userMessage()),

    /**
     * 非商家账号，无法查看内容。
     *
     * <p>引用统一码 {@link MerchantErrorCode#STORE_LOGIN_ONLY}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link MerchantErrorCode#STORE_LOGIN_ONLY}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    NO_SHOP_ACCOUNT(MerchantErrorCode.STORE_LOGIN_ONLY.code(), MerchantErrorCode.STORE_LOGIN_ONLY.meta().userMessage()),

    /**
     * 文本内容存在敏感词。
     *
     * <p>引用统一码 {@link SecurityErrorCode#SECURITY_SENSITIVE_WORD}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link SecurityErrorCode#SECURITY_SENSITIVE_WORD}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    EXIST_SENSITIVE(SecurityErrorCode.SECURITY_SENSITIVE_WORD.code(), SecurityErrorCode.SECURITY_SENSITIVE_WORD.meta().userMessage()),

    /**
     * 账号已禁用,请联系管理员。
     *
     * <p>引用统一码 {@link AuthErrorCode#ACCOUNT_DISABLED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link AuthErrorCode#ACCOUNT_DISABLED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    USER_IS_DISABLED(AuthErrorCode.ACCOUNT_DISABLED.code(), AuthErrorCode.ACCOUNT_DISABLED.meta().userMessage()),

    /**
     * 登录密码不正确。
     *
     * <p>引用统一码 {@link AuthErrorCode#CREDENTIAL_INVALID}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link AuthErrorCode#CREDENTIAL_INVALID}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    PASSWORD_ERROR(AuthErrorCode.CREDENTIAL_INVALID.code(), AuthErrorCode.CREDENTIAL_INVALID.meta().userMessage()),

    /**
     * 至少保证一种货币处于开启状态。
     *
     * <p>引用统一码 {@link ValidationErrorCode#PARAM_INVALID}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link ValidationErrorCode#PARAM_INVALID}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    IS_USE_NOT_UPDATE_STATUS(ValidationErrorCode.PARAM_INVALID.code(), ValidationErrorCode.PARAM_INVALID.meta().userMessage()),

    /**
     * 请重新修改配置，不能将*值保存库中！。
     *
     * <p>引用统一码 {@link ValidationErrorCode#PARAM_INVALID}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link ValidationErrorCode#PARAM_INVALID}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    REVISED_AGAIN(ValidationErrorCode.PARAM_INVALID.code(), ValidationErrorCode.PARAM_INVALID.meta().userMessage()),

    /**
     * 请先配置平台短信手机号。
     *
     * <p>引用统一码 {@link ThirdPartyErrorCode#THIRDPARTY_CONFIG_MISSING}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link ThirdPartyErrorCode#THIRDPARTY_CONFIG_MISSING}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    PLATFORM_SMS_MOBILE_IS_NULL(ThirdPartyErrorCode.THIRDPARTY_CONFIG_MISSING.code(), ThirdPartyErrorCode.THIRDPARTY_CONFIG_MISSING.meta().userMessage()),

    /**
     * 登录成功。
     *
     * <p><b>成功语义，不属于错误码体系</b>：码值保持历史值 11000 不变。
     * 它不是一个错误码，不能编成 5 位统一码 —— 那会让前端把成功判成失败。
     * 前端统一用 {@code SuccessCode.isSuccess(code)} 判定，不要用码值硬比。
     */
    LOGIN_SUCCEEDED(11000, "登录成功"),

    /**
     * 退出成功。
     *
     * <p><b>成功语义，不属于错误码体系</b>：码值保持历史值 11001 不变。
     * 它不是一个错误码，不能编成 5 位统一码 —— 那会让前端把成功判成失败。
     * 前端统一用 {@code SuccessCode.isSuccess(code)} 判定，不要用码值硬比。
     */
    EXIT_SUCCESSFULLY(11001, "退出成功"),

    /**
     * 用户不存在,请先注册再登录。
     *
     * <p>引用统一码 {@link UserErrorCode#USER_NOT_FOUND}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link UserErrorCode#USER_NOT_FOUND}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    USER_DOES_NOT_EXIST(UserErrorCode.USER_NOT_FOUND.code(), UserErrorCode.USER_NOT_FOUND.meta().userMessage()),

    /**
     * 账号或者密码错误。
     *
     * <p>引用统一码 {@link AuthErrorCode#CREDENTIAL_INVALID}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link AuthErrorCode#CREDENTIAL_INVALID}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    USERNAME_PHONE_VALID_EXCEPTION(AuthErrorCode.CREDENTIAL_INVALID.code(), AuthErrorCode.CREDENTIAL_INVALID.meta().userMessage()),

    /**
     * 验证码不正确。
     *
     * <p>引用统一码 {@link AuthErrorCode#SMS_CODE_INVALID}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link AuthErrorCode#SMS_CODE_INVALID}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    INCORRECT_VERIFICATION_CODE(AuthErrorCode.SMS_CODE_INVALID.code(), AuthErrorCode.SMS_CODE_INVALID.meta().userMessage()),

    /**
     * 用户未登录。
     *
     * <p>引用统一码 {@link AuthErrorCode#USER_NOT_LOGGED_IN}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link AuthErrorCode#USER_NOT_LOGGED_IN}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    USER_IS_NOT_LOGGED_IN(AuthErrorCode.USER_NOT_LOGGED_IN.code(), AuthErrorCode.USER_NOT_LOGGED_IN.meta().userMessage()),

    /**
     * 权限不足。
     *
     * <p>引用统一码 {@link PermissionErrorCode#PERMISSION_ACCESS_DENIED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link PermissionErrorCode#PERMISSION_ACCESS_DENIED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    INSUFFICIENT_PERMISSIONS(PermissionErrorCode.PERMISSION_ACCESS_DENIED.code(), PermissionErrorCode.PERMISSION_ACCESS_DENIED.meta().userMessage()),

    /**
     * 账号尚未审核通过，请耐心等待！。
     *
     * <p>引用统一码 {@link AuthErrorCode#ACCOUNT_NOT_APPROVED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link AuthErrorCode#ACCOUNT_NOT_APPROVED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    ACCOUNT_HAS_NOT_BEEN_APPROVED(AuthErrorCode.ACCOUNT_NOT_APPROVED.code(), AuthErrorCode.ACCOUNT_NOT_APPROVED.meta().userMessage()),

    /**
     * 账号已被冻结。
     *
     * <p>引用统一码 {@link AuthErrorCode#ACCOUNT_FROZEN}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link AuthErrorCode#ACCOUNT_FROZEN}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    ACCOUNT_HAS_BEEN_FROZEN(AuthErrorCode.ACCOUNT_FROZEN.code(), AuthErrorCode.ACCOUNT_FROZEN.meta().userMessage()),

    /**
     * 会员注册。
     *
     * <p>引用统一码 {@link UserErrorCode#USER_REGISTER_FAILED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link UserErrorCode#USER_REGISTER_FAILED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    MEMBER_REGISTRATION(UserErrorCode.USER_REGISTER_FAILED.code(), UserErrorCode.USER_REGISTER_FAILED.meta().userMessage()),

    /**
     * 注册失败。
     *
     * <p>引用统一码 {@link UserErrorCode#USER_REGISTER_FAILED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link UserErrorCode#USER_REGISTER_FAILED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    REGISTRATION_FAIL(UserErrorCode.USER_REGISTER_FAILED.code(), UserErrorCode.USER_REGISTER_FAILED.meta().userMessage()),

    /**
     * 注册成功。
     *
     * <p><b>成功语义，不属于错误码体系</b>：码值保持历史值 1 不变。
     * 它不是一个错误码，不能编成 5 位统一码 —— 那会让前端把成功判成失败。
     * 前端统一用 {@code SuccessCode.isSuccess(code)} 判定，不要用码值硬比。
     */
    REGISTRATION_SUCCESS(1, "注册成功"),

    /**
     * 验证码已过期，请重新获取。
     *
     * <p>引用统一码 {@link AuthErrorCode#SMS_CODE_INVALID}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link AuthErrorCode#SMS_CODE_INVALID}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    VERIFICATION_CODE_OVERDUE(AuthErrorCode.SMS_CODE_INVALID.code(), AuthErrorCode.SMS_CODE_INVALID.meta().userMessage()),

    /**
     * 积分不能小于0。
     *
     * <p>引用统一码 {@link ValidationErrorCode#VALUE_OUT_OF_RANGE}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link ValidationErrorCode#VALUE_OUT_OF_RANGE}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    INTEGRAL_CANNOT_BE_LESS_THAN_0(ValidationErrorCode.VALUE_OUT_OF_RANGE.code(), ValidationErrorCode.VALUE_OUT_OF_RANGE.meta().userMessage()),

    /**
     * 手机号已存在。
     *
     * <p>引用统一码 {@link UserErrorCode#USER_DUPLICATE_PHONE}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link UserErrorCode#USER_DUPLICATE_PHONE}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    PHONE_EXSIT_EXCEPTION(UserErrorCode.USER_DUPLICATE_PHONE.code(), UserErrorCode.USER_DUPLICATE_PHONE.meta().userMessage()),

    /**
     * 邮箱已存在。
     *
     * <p>引用统一码 {@link UserErrorCode#USER_DUPLICATE_EMAIL}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link UserErrorCode#USER_DUPLICATE_EMAIL}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    EMAIL_EXSIT_EXCEPTION(UserErrorCode.USER_DUPLICATE_EMAIL.code(), UserErrorCode.USER_DUPLICATE_EMAIL.meta().userMessage()),

    /**
     * 密码不能为空。
     *
     * <p>引用统一码 {@link ValidationErrorCode#PARAM_INVALID}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link ValidationErrorCode#PARAM_INVALID}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    PASSWORD_CANNOT_BE_EMPTY(ValidationErrorCode.PARAM_INVALID.code(), ValidationErrorCode.PARAM_INVALID.meta().userMessage()),

    /**
     * 输入密码不能和原密码相同。
     *
     * <p>引用统一码 {@link ValidationErrorCode#PARAM_INVALID}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link ValidationErrorCode#PARAM_INVALID}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    PASSWORD_CANNOT_BE_OLD_PASSWORD(ValidationErrorCode.PARAM_INVALID.code(), ValidationErrorCode.PARAM_INVALID.meta().userMessage()),

    /**
     * 更新密码成功。
     *
     * <p><b>成功语义，不属于错误码体系</b>：码值保持历史值 11019 不变。
     * 它不是一个错误码，不能编成 5 位统一码 —— 那会让前端把成功判成失败。
     * 前端统一用 {@code SuccessCode.isSuccess(code)} 判定，不要用码值硬比。
     */
    PASSWORD_UPDATED_SUCCESSFULLY(11019, "更新密码成功"),

    /**
     * 更新密码失败。
     *
     * <p>引用统一码 {@link SystemErrorCode#DB_ACCESS_FAILED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link SystemErrorCode#DB_ACCESS_FAILED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    PASSWORD_UPDATED_FAILED(SystemErrorCode.DB_ACCESS_FAILED.code(), SystemErrorCode.DB_ACCESS_FAILED.meta().userMessage()),

    /**
     * 增加商户失败。
     *
     * <p>引用统一码 {@link MerchantErrorCode#STORE_OPEN_FAILED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link MerchantErrorCode#STORE_OPEN_FAILED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    SHOP_REGISTRATION_FAIL(MerchantErrorCode.STORE_OPEN_FAILED.code(), MerchantErrorCode.STORE_OPEN_FAILED.meta().userMessage()),

    /**
     * 验证码不能为空。
     *
     * <p>引用统一码 {@link ValidationErrorCode#PARAM_INVALID}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link ValidationErrorCode#PARAM_INVALID}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    VERIFICATION_CODE_CANNOT_BE_EMPTY(ValidationErrorCode.PARAM_INVALID.code(), ValidationErrorCode.PARAM_INVALID.meta().userMessage()),

    /**
     * 验证码生成成功。
     *
     * <p><b>成功语义，不属于错误码体系</b>：码值保持历史值 11031 不变。
     * 它不是一个错误码，不能编成 5 位统一码 —— 那会让前端把成功判成失败。
     * 前端统一用 {@code SuccessCode.isSuccess(code)} 判定，不要用码值硬比。
     */
    VERIFICATION_CODE_SUCCESS(11031, "验证码生成成功"),

    /**
     * 获取用户信息失败。
     *
     * <p>引用统一码 {@link UserErrorCode#USER_QUERY_FAILED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link UserErrorCode#USER_QUERY_FAILED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    QUERY_USER_FAIL(UserErrorCode.USER_QUERY_FAILED.code(), UserErrorCode.USER_QUERY_FAILED.meta().userMessage()),

    /**
     * 没有红包。
     *
     * <p>引用统一码 {@link MarketingErrorCode#MARKETING_RED_ENVELOPE_NONE}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link MarketingErrorCode#MARKETING_RED_ENVELOPE_NONE}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    NO_RED_ENVELOPES(MarketingErrorCode.MARKETING_RED_ENVELOPE_NONE.code(), MarketingErrorCode.MARKETING_RED_ENVELOPE_NONE.meta().userMessage()),

    /**
     * 收藏为空。
     *
     * <p>引用统一码 {@link UserErrorCode#USER_FAVORITE_EMPTY}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link UserErrorCode#USER_FAVORITE_EMPTY}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    COLLECTION_IS_EMPTY(UserErrorCode.USER_FAVORITE_EMPTY.code(), UserErrorCode.USER_FAVORITE_EMPTY.meta().userMessage()),

    /**
     * 查询收藏商品成功。
     *
     * <p><b>成功语义，不属于错误码体系</b>：码值保持历史值 11036 不变。
     * 它不是一个错误码，不能编成 5 位统一码 —— 那会让前端把成功判成失败。
     * 前端统一用 {@code SuccessCode.isSuccess(code)} 判定，不要用码值硬比。
     */
    SUCCESSFULLY_QUERIED_FAVORITE_PRODUCTS(11036, "查询收藏商品成功"),

    /**
     * 验证旧手机号或邮箱成功。
     *
     * <p><b>成功语义，不属于错误码体系</b>：码值保持历史值 11038 不变。
     * 它不是一个错误码，不能编成 5 位统一码 —— 那会让前端把成功判成失败。
     * 前端统一用 {@code SuccessCode.isSuccess(code)} 判定，不要用码值硬比。
     */
    SUCCESSFULLY_VERIFIED_THE_OLD_PHONE_NUMBER(11038, "验证旧手机号或邮箱成功"),

    /**
     * 验证旧手机号或邮箱失败。
     *
     * <p>引用统一码 {@link UserErrorCode#USER_PHONE_VERIFY_FAILED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link UserErrorCode#USER_PHONE_VERIFY_FAILED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    FAILED_VERIFIED_THE_OLD_PHONE_NUMBER(UserErrorCode.USER_PHONE_VERIFY_FAILED.code(), UserErrorCode.USER_PHONE_VERIFY_FAILED.meta().userMessage()),

    /**
     * 手机号或邮箱已被使用。
     *
     * <p>引用统一码 {@link UserErrorCode#USER_DUPLICATE_PHONE}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link UserErrorCode#USER_DUPLICATE_PHONE}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    MOBILE_NUMBER_HAS_BEEN_USED(UserErrorCode.USER_DUPLICATE_PHONE.code(), UserErrorCode.USER_DUPLICATE_PHONE.meta().userMessage()),

    /**
     * 原手机号或邮箱为空。
     *
     * <p>引用统一码 {@link ValidationErrorCode#PARAM_INVALID}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link ValidationErrorCode#PARAM_INVALID}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    ORIGINAL_PHONE_NUMBER_IS_EMPTY(ValidationErrorCode.PARAM_INVALID.code(), ValidationErrorCode.PARAM_INVALID.meta().userMessage()),

    /**
     * 更换手机号或邮箱成功。
     *
     * <p><b>成功语义，不属于错误码体系</b>：码值保持历史值 11042 不变。
     * 它不是一个错误码，不能编成 5 位统一码 —— 那会让前端把成功判成失败。
     * 前端统一用 {@code SuccessCode.isSuccess(code)} 判定，不要用码值硬比。
     */
    SUCCESSFULLY_CHANGED_THE_PHONE_NUMBER(11042, "更换手机号或邮箱成功"),

    /**
     * 更换手机号或邮箱失败。
     *
     * <p>引用统一码 {@link UserErrorCode#USER_PHONE_CHANGE_FAILED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link UserErrorCode#USER_PHONE_CHANGE_FAILED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    FAILED_CHANGED_THE_PHONE_NUMBER(UserErrorCode.USER_PHONE_CHANGE_FAILED.code(), UserErrorCode.USER_PHONE_CHANGE_FAILED.meta().userMessage()),

    /**
     * 文件不存在。
     *
     * <p>引用统一码 {@link FileErrorCode#FILE_NOT_FOUND}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link FileErrorCode#FILE_NOT_FOUND}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    FILE_NOT_EXIST(FileErrorCode.FILE_NOT_FOUND.code(), FileErrorCode.FILE_NOT_FOUND.meta().userMessage()),

    /**
     * 文件格式错误。
     *
     * <p>引用统一码 {@link FileErrorCode#FILE_TYPE_NOT_ALLOWED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link FileErrorCode#FILE_TYPE_NOT_ALLOWED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    FILE_FORMAT_WRONG(FileErrorCode.FILE_TYPE_NOT_ALLOWED.code(), FileErrorCode.FILE_TYPE_NOT_ALLOWED.meta().userMessage()),

    /**
     * 数据格式错误，请按照模板格式上传文件。
     *
     * <p>引用统一码 {@link ValidationErrorCode#FORMAT_INVALID}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link ValidationErrorCode#FORMAT_INVALID}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    DATA_FORMAT_WRONG(ValidationErrorCode.FORMAT_INVALID.code(), ValidationErrorCode.FORMAT_INVALID.meta().userMessage()),

    /**
     * 上传Excel的最大行数不应超过100行。
     *
     * <p>引用统一码 {@link ValidationErrorCode#COUNT_EXCEEDED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link ValidationErrorCode#COUNT_EXCEEDED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    EXCEL_MAX_ROWS(ValidationErrorCode.COUNT_EXCEEDED.code(), ValidationErrorCode.COUNT_EXCEEDED.meta().userMessage()),

    /**
     * 暂未匹配到商品，请重新上传！。
     *
     * <p>引用统一码 {@link GoodsErrorCode#GOODS_QUERY_RESULT_EMPTY}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link GoodsErrorCode#GOODS_QUERY_RESULT_EMPTY}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    EXCEL_NO_GOODS(GoodsErrorCode.GOODS_QUERY_RESULT_EMPTY.code(), GoodsErrorCode.GOODS_QUERY_RESULT_EMPTY.meta().userMessage()),

    /**
     * 未查询到商品信息。
     *
     * <p>引用统一码 {@link GoodsErrorCode#GOODS_QUERY_RESULT_EMPTY}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link GoodsErrorCode#GOODS_QUERY_RESULT_EMPTY}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    NO_GOODS(GoodsErrorCode.GOODS_QUERY_RESULT_EMPTY.code(), GoodsErrorCode.GOODS_QUERY_RESULT_EMPTY.meta().userMessage()),

    /**
     * userid为null。
     *
     * <p>引用统一码 {@link UserErrorCode#USER_PARAM_INVALID}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link UserErrorCode#USER_PARAM_INVALID}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    USER_ID_IS_NULL(UserErrorCode.USER_PARAM_INVALID.code(), UserErrorCode.USER_PARAM_INVALID.meta().userMessage()),

    /**
     * 未设置支付密码。
     *
     * <p>引用统一码 {@link PaymentErrorCode#PAYMENT_PASSWORD_NOT_SET}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link PaymentErrorCode#PAYMENT_PASSWORD_NOT_SET}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    PAYMENT_PASSWORD_IS_NULL(PaymentErrorCode.PAYMENT_PASSWORD_NOT_SET.code(), PaymentErrorCode.PAYMENT_PASSWORD_NOT_SET.meta().userMessage()),

    /**
     * 先登录才能查看积分详情。
     *
     * <p>引用统一码 {@link AuthErrorCode#USER_NOT_LOGGED_IN}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link AuthErrorCode#USER_NOT_LOGGED_IN}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    INTEGRAL_DETAIL_MUST_LOGIN(AuthErrorCode.USER_NOT_LOGGED_IN.code(), AuthErrorCode.USER_NOT_LOGGED_IN.meta().userMessage()),

    /**
     * 该会员等级下存在会员，不能删除！。
     *
     * <p>引用统一码 {@link UserErrorCode#USER_LEVEL_IN_USE}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link UserErrorCode#USER_LEVEL_IN_USE}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    MEMBER_GRADE_DELETE(UserErrorCode.USER_LEVEL_IN_USE.code(), UserErrorCode.USER_LEVEL_IN_USE.meta().userMessage()),

    /**
     * 请输入平台已注册用户！。
     *
     * <p>引用统一码 {@link UserErrorCode#USER_NOT_PLATFORM_REGISTERED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link UserErrorCode#USER_NOT_PLATFORM_REGISTERED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    PLATFORM_NOT_REGISTERED(UserErrorCode.USER_NOT_PLATFORM_REGISTERED.code(), UserErrorCode.USER_NOT_PLATFORM_REGISTERED.meta().userMessage()),

    /**
     * 注册会员等级积分不能高于注册获得的积分！。
     *
     * <p>引用统一码 {@link ValidationErrorCode#VALUE_OUT_OF_RANGE}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link ValidationErrorCode#VALUE_OUT_OF_RANGE}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    INTEGRAL_NOT_REGISTERED(ValidationErrorCode.VALUE_OUT_OF_RANGE.code(), ValidationErrorCode.VALUE_OUT_OF_RANGE.meta().userMessage()),

    /**
     * 请勿给本账号转账！。
     *
     * <p>引用统一码 {@link PaymentErrorCode#PAYMENT_TRANSFER_SELF_FORBIDDEN}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link PaymentErrorCode#PAYMENT_TRANSFER_SELF_FORBIDDEN}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    DANGEROUS_TRANSFER_OF_ACCOUNT(PaymentErrorCode.PAYMENT_TRANSFER_SELF_FORBIDDEN.code(), PaymentErrorCode.PAYMENT_TRANSFER_SELF_FORBIDDEN.meta().userMessage()),

    /**
     * 账号不存在，请输入正确账号或确认账号是否正确！。
     *
     * <p>引用统一码 {@link PaymentErrorCode#PAYMENT_TARGET_ACCOUNT_NOT_FOUND}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link PaymentErrorCode#PAYMENT_TARGET_ACCOUNT_NOT_FOUND}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    ACCOUNT_IS_NOT_EXIST(PaymentErrorCode.PAYMENT_TARGET_ACCOUNT_NOT_FOUND.code(), PaymentErrorCode.PAYMENT_TARGET_ACCOUNT_NOT_FOUND.meta().userMessage()),

    /**
     * 此手机号已注册门店！。
     *
     * <p>引用统一码 {@link MerchantErrorCode#STORE_PHONE_ALREADY_REGISTERED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link MerchantErrorCode#STORE_PHONE_ALREADY_REGISTERED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    REGISTERED_STORE_WITH_PHONE_NUMBER(MerchantErrorCode.STORE_PHONE_ALREADY_REGISTERED.code(), MerchantErrorCode.STORE_PHONE_ALREADY_REGISTERED.meta().userMessage()),

    /**
     * 部门名称需小于30个字符。
     *
     * <p>引用统一码 {@link ValidationErrorCode#LENGTH_EXCEEDED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link ValidationErrorCode#LENGTH_EXCEEDED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    DEPARTMENT_NAME_LIMIT(ValidationErrorCode.LENGTH_EXCEEDED.code(), ValidationErrorCode.LENGTH_EXCEEDED.meta().userMessage()),

    /**
     * 该分组有关联管理员账号。
     *
     * <p>引用统一码 {@link PermissionErrorCode#ROLE_IN_USE}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link PermissionErrorCode#ROLE_IN_USE}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    GROUPING_IS_RELATED(PermissionErrorCode.ROLE_IN_USE.code(), PermissionErrorCode.ROLE_IN_USE.meta().userMessage()),

    /**
     * 微客推广已关闭。
     *
     * <p>引用统一码 {@link MarketingErrorCode#MARKETING_VK_CLOSED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link MarketingErrorCode#MARKETING_VK_CLOSED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    VK_CLOSE(MarketingErrorCode.MARKETING_VK_CLOSED.code(), MarketingErrorCode.MARKETING_VK_CLOSED.meta().userMessage()),

    /**
     * 当前用户已是微客。
     *
     * <p>引用统一码 {@link MarketingErrorCode#MARKETING_VK_ALREADY}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link MarketingErrorCode#MARKETING_VK_ALREADY}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    USER_IS_VK(MarketingErrorCode.MARKETING_VK_ALREADY.code(), MarketingErrorCode.MARKETING_VK_ALREADY.meta().userMessage()),

    /**
     * 该权益存在绑定的套餐，请先至套餐中删除后再进行删除。
     *
     * <p>引用统一码 {@link MarketingErrorCode#MARKETING_MEMBER_RIGHT_IN_USE_DELETE}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link MarketingErrorCode#MARKETING_MEMBER_RIGHT_IN_USE_DELETE}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    PLUS_ERROR(MarketingErrorCode.MARKETING_MEMBER_RIGHT_IN_USE_DELETE.code(), MarketingErrorCode.MARKETING_MEMBER_RIGHT_IN_USE_DELETE.meta().userMessage()),

    /**
     * 该权益存在绑定的套餐，请先至套餐中删除后再进行禁用。
     *
     * <p>引用统一码 {@link MarketingErrorCode#MARKETING_MEMBER_RIGHT_IN_USE_DISABLE}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link MarketingErrorCode#MARKETING_MEMBER_RIGHT_IN_USE_DISABLE}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    PLUS_ERROR2(MarketingErrorCode.MARKETING_MEMBER_RIGHT_IN_USE_DISABLE.code(), MarketingErrorCode.MARKETING_MEMBER_RIGHT_IN_USE_DISABLE.meta().userMessage()),

    /**
     * 非商家账号不支持登录！。
     *
     * <p>引用统一码 {@link MerchantErrorCode#STORE_LOGIN_ONLY}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link MerchantErrorCode#STORE_LOGIN_ONLY}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    IS_NOT_SHOP_LOGIN(MerchantErrorCode.STORE_LOGIN_ONLY.code(), MerchantErrorCode.STORE_LOGIN_ONLY.meta().userMessage()),

    /**
     * 重置邀请人不能和原来的邀请人一样！。
     *
     * <p>引用统一码 {@link MarketingErrorCode#MARKETING_VK_INVITER_DUPLICATE}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link MarketingErrorCode#MARKETING_VK_INVITER_DUPLICATE}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    VK_USER_REPEAT(MarketingErrorCode.MARKETING_VK_INVITER_DUPLICATE.code(), MarketingErrorCode.MARKETING_VK_INVITER_DUPLICATE.meta().userMessage()),

    /**
     * 请输入微客身份的手机号码！。
     *
     * <p>引用统一码 {@link MarketingErrorCode#MARKETING_VK_PHONE_NOT_VK}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link MarketingErrorCode#MARKETING_VK_PHONE_NOT_VK}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    NO_VK_USER(MarketingErrorCode.MARKETING_VK_PHONE_NOT_VK.code(), MarketingErrorCode.MARKETING_VK_PHONE_NOT_VK.meta().userMessage()),

    /**
     * 分享人不是微客！。
     *
     * <p>引用统一码 {@link MarketingErrorCode#MARKETING_VK_INVITER_NOT_VK}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link MarketingErrorCode#MARKETING_VK_INVITER_NOT_VK}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    INVITER_NO_VK_USER(MarketingErrorCode.MARKETING_VK_INVITER_NOT_VK.code(), MarketingErrorCode.MARKETING_VK_INVITER_NOT_VK.meta().userMessage()),

    /**
     * 已存在邀请记录！。
     *
     * <p>引用统一码 {@link MarketingErrorCode#MARKETING_VK_INVITE_RECORD_EXISTED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link MarketingErrorCode#MARKETING_VK_INVITE_RECORD_EXISTED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    INVITER_IS_EXIST(MarketingErrorCode.MARKETING_VK_INVITE_RECORD_EXISTED.code(), MarketingErrorCode.MARKETING_VK_INVITE_RECORD_EXISTED.meta().userMessage()),

    /**
     * 用户不是微客！。
     *
     * <p>引用统一码 {@link MarketingErrorCode#MARKETING_VK_USER_NOT_VK}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link MarketingErrorCode#MARKETING_VK_USER_NOT_VK}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    USER_NOT_VK(MarketingErrorCode.MARKETING_VK_USER_NOT_VK.code(), MarketingErrorCode.MARKETING_VK_USER_NOT_VK.meta().userMessage()),

    /**
     * 该账号未绑定微信，请先绑定微信。
     *
     * <p>引用统一码 {@link UserErrorCode#USER_WECHAT_NOT_BOUND}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link UserErrorCode#USER_WECHAT_NOT_BOUND}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    USER_NO_BIND_WECHAT(UserErrorCode.USER_WECHAT_NOT_BOUND.code(), UserErrorCode.USER_WECHAT_NOT_BOUND.meta().userMessage()),

    /**
     * 客服账号已占用，请切换账号再试！。
     *
     * <p>引用统一码 {@link MessageErrorCode#IM_ACCOUNT_OCCUPIED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link MessageErrorCode#IM_ACCOUNT_OCCUPIED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    KF_EXISTS_NO_CREATE(MessageErrorCode.IM_ACCOUNT_OCCUPIED.code(), MessageErrorCode.IM_ACCOUNT_OCCUPIED.meta().userMessage()),

    /**
     * 客服账号不存在，请创建！。
     *
     * <p>引用统一码 {@link MessageErrorCode#IM_ACCOUNT_NOT_FOUND}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link MessageErrorCode#IM_ACCOUNT_NOT_FOUND}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    KF_NOT_EXISTS(MessageErrorCode.IM_ACCOUNT_NOT_FOUND.code(), MessageErrorCode.IM_ACCOUNT_NOT_FOUND.meta().userMessage()),

    /**
     * 客服未在线！ 可联系：。
     *
     * <p>引用统一码 {@link MessageErrorCode#IM_AGENT_OFFLINE}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link MessageErrorCode#IM_AGENT_OFFLINE}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    KF_NOT_CONFIG_OR_NOT_ONLINE(MessageErrorCode.IM_AGENT_OFFLINE.code(), MessageErrorCode.IM_AGENT_OFFLINE.meta().userMessage()),

    /**
     * 还有未结束的会话，请先结束！。
     *
     * <p>引用统一码 {@link MessageErrorCode#IM_SESSION_UNCLOSED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link MessageErrorCode#IM_SESSION_UNCLOSED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    CONVERSATION_END(MessageErrorCode.IM_SESSION_UNCLOSED.code(), MessageErrorCode.IM_SESSION_UNCLOSED.meta().userMessage()),

    /**
     * 客服服务未配置,请配置后再试。
     *
     * <p>引用统一码 {@link MessageErrorCode#IM_NOT_CONFIGURED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link MessageErrorCode#IM_NOT_CONFIGURED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    IM_SERVER_NOT_CONFIG(MessageErrorCode.IM_NOT_CONFIGURED.code(), MessageErrorCode.IM_NOT_CONFIGURED.meta().userMessage()),

    /**
     * 商品库存不足！。
     *
     * <p>引用统一码 {@link InventoryErrorCode#INVENTORY_STOCK_NOT_ENOUGH}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link InventoryErrorCode#INVENTORY_STOCK_NOT_ENOUGH}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    USER_CART_STOCK_ERROR(InventoryErrorCode.INVENTORY_STOCK_NOT_ENOUGH.code(), InventoryErrorCode.INVENTORY_STOCK_NOT_ENOUGH.meta().userMessage()),

    /**
     * 该订单已取消，请勿重复操作！。
     *
     * <p>引用统一码 {@link OrderErrorCode#ORDER_STATE_NOT_ALLOWED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link OrderErrorCode#ORDER_STATE_NOT_ALLOWED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    ORDER_CANCELED(OrderErrorCode.ORDER_STATE_NOT_ALLOWED.code(), OrderErrorCode.ORDER_STATE_NOT_ALLOWED.meta().userMessage()),

    /**
     * 默认收货成功。
     *
     * <p><b>成功语义，不属于错误码体系</b>：码值保持历史值 14003 不变。
     * 它不是一个错误码，不能编成 5 位统一码 —— 那会让前端把成功判成失败。
     * 前端统一用 {@code SuccessCode.isSuccess(code)} 判定，不要用码值硬比。
     */
    DEFAULT_RECEIPT_SUCCEEDED(14003, "默认收货成功"),

    /**
     * 默认收货失败。
     *
     * <p>引用统一码 {@link OrderErrorCode#ORDER_RECEIPT_FAILED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link OrderErrorCode#ORDER_RECEIPT_FAILED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    DEFAULT_RECEIPT_FAILED(OrderErrorCode.ORDER_RECEIPT_FAILED.code(), OrderErrorCode.ORDER_RECEIPT_FAILED.meta().userMessage()),

    /**
     * 收货成功。
     *
     * <p><b>成功语义，不属于错误码体系</b>：码值保持历史值 14005 不变。
     * 它不是一个错误码，不能编成 5 位统一码 —— 那会让前端把成功判成失败。
     * 前端统一用 {@code SuccessCode.isSuccess(code)} 判定，不要用码值硬比。
     */
    GOODS_RECEIVED_SUCCESSFULLY(14005, "收货成功"),

    /**
     * 收货失败。
     *
     * <p>引用统一码 {@link OrderErrorCode#ORDER_RECEIPT_FAILED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link OrderErrorCode#ORDER_RECEIPT_FAILED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    GOODS_RECEIVED_FAILED(OrderErrorCode.ORDER_RECEIPT_FAILED.code(), OrderErrorCode.ORDER_RECEIPT_FAILED.meta().userMessage()),

    /**
     * 订单号为空，请输入订单号。
     *
     * <p>引用统一码 {@link OrderErrorCode#ORDER_PARAM_INVALID}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link OrderErrorCode#ORDER_PARAM_INVALID}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    ORDER_NUMBER_IS_EMPTY(OrderErrorCode.ORDER_PARAM_INVALID.code(), OrderErrorCode.ORDER_PARAM_INVALID.meta().userMessage()),

    /**
     * 请完善退款信息。
     *
     * <p>引用统一码 {@link AfterSaleErrorCode#AFTER_SALE_PARAM_INVALID}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link AfterSaleErrorCode#AFTER_SALE_PARAM_INVALID}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    PLEASE_COMPLETE_THE_REFUND_INFORMATION(AfterSaleErrorCode.AFTER_SALE_PARAM_INVALID.code(), AfterSaleErrorCode.AFTER_SALE_PARAM_INVALID.meta().userMessage()),

    /**
     * 分销商品超过售后期，无法申请。
     *
     * <p>引用统一码 {@link AfterSaleErrorCode#AFTER_SALE_TIME_EXPIRED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link AfterSaleErrorCode#AFTER_SALE_TIME_EXPIRED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    GOODS_BEYOND_THE_AFTER_SALES_PERIOD(AfterSaleErrorCode.AFTER_SALE_TIME_EXPIRED.code(), AfterSaleErrorCode.AFTER_SALE_TIME_EXPIRED.meta().userMessage()),

    /**
     * 退货原因不能为null。
     *
     * <p>引用统一码 {@link AfterSaleErrorCode#AFTER_SALE_PARAM_INVALID}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link AfterSaleErrorCode#AFTER_SALE_PARAM_INVALID}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    RETURN_REASON_CANNOT_BE_NULL(AfterSaleErrorCode.AFTER_SALE_PARAM_INVALID.code(), AfterSaleErrorCode.AFTER_SALE_PARAM_INVALID.meta().userMessage()),

    /**
     * 申请失败,请联系客服！。
     *
     * <p>引用统一码 {@link SystemErrorCode#SYSTEM_APPLY_FAILED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link SystemErrorCode#SYSTEM_APPLY_FAILED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    APPLICATION_FAILED(SystemErrorCode.SYSTEM_APPLY_FAILED.code(), SystemErrorCode.SYSTEM_APPLY_FAILED.meta().userMessage()),

    /**
     * 发票模版数已达上限。
     *
     * <p>引用统一码 {@link OrderErrorCode#ORDER_INVOICE_TEMPLATE_LIMIT_EXCEEDED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link OrderErrorCode#ORDER_INVOICE_TEMPLATE_LIMIT_EXCEEDED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    INVOICE_TEMPLATES_HAS_SUPERIOR_LIMIT(OrderErrorCode.ORDER_INVOICE_TEMPLATE_LIMIT_EXCEEDED.code(), OrderErrorCode.ORDER_INVOICE_TEMPLATE_LIMIT_EXCEEDED.meta().userMessage()),

    /**
     * 发票地址模版数已达上限。
     *
     * <p>引用统一码 {@link OrderErrorCode#ORDER_INVOICE_ADDRESS_LIMIT_EXCEEDED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link OrderErrorCode#ORDER_INVOICE_ADDRESS_LIMIT_EXCEEDED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    INVOICE_ADDRESS_TEMPLATES_HAS_REACHED_THE_UPPER_LIMIT(OrderErrorCode.ORDER_INVOICE_ADDRESS_LIMIT_EXCEEDED.code(), OrderErrorCode.ORDER_INVOICE_ADDRESS_LIMIT_EXCEEDED.meta().userMessage()),

    /**
     * 订单为空。
     *
     * <p>引用统一码 {@link OrderErrorCode#ORDER_NOT_FOUND}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link OrderErrorCode#ORDER_NOT_FOUND}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    ORDER_IS_NULL(OrderErrorCode.ORDER_NOT_FOUND.code(), OrderErrorCode.ORDER_NOT_FOUND.meta().userMessage()),

    /**
     * 金额的格式有误,请按要求填写。
     *
     * <p>引用统一码 {@link OrderErrorCode#ORDER_AMOUNT_INVALID}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link OrderErrorCode#ORDER_AMOUNT_INVALID}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    ORDER_PRICE_IS_ERROR(OrderErrorCode.ORDER_AMOUNT_INVALID.code(), OrderErrorCode.ORDER_AMOUNT_INVALID.meta().userMessage()),

    /**
     * 商家所用发货地址ID不能为空，请填写后重新上传。。
     *
     * <p>引用统一码 {@link LogisticsErrorCode#LOGISTICS_INFO_INCOMPLETE}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link LogisticsErrorCode#LOGISTICS_INFO_INCOMPLETE}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    SHIPPING_ADDRESS_IS_NULL(LogisticsErrorCode.LOGISTICS_INFO_INCOMPLETE.code(), LogisticsErrorCode.LOGISTICS_INFO_INCOMPLETE.meta().userMessage()),

    /**
     * 发货人姓名不能为空，请填写后重新上传。。
     *
     * <p>引用统一码 {@link LogisticsErrorCode#LOGISTICS_INFO_INCOMPLETE}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link LogisticsErrorCode#LOGISTICS_INFO_INCOMPLETE}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    CONSIGNOR_NAME_IS_NULL(LogisticsErrorCode.LOGISTICS_INFO_INCOMPLETE.code(), LogisticsErrorCode.LOGISTICS_INFO_INCOMPLETE.meta().userMessage()),

    /**
     * 发货人地址不能为空，请填写后重新上传。。
     *
     * <p>引用统一码 {@link LogisticsErrorCode#LOGISTICS_INFO_INCOMPLETE}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link LogisticsErrorCode#LOGISTICS_INFO_INCOMPLETE}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    CONSIGNOR_ADDRESS_IS_NULL(LogisticsErrorCode.LOGISTICS_INFO_INCOMPLETE.code(), LogisticsErrorCode.LOGISTICS_INFO_INCOMPLETE.meta().userMessage()),

    /**
     * 发货人联系方式不能为空，请填写后重新上传。。
     *
     * <p>引用统一码 {@link LogisticsErrorCode#LOGISTICS_INFO_INCOMPLETE}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link LogisticsErrorCode#LOGISTICS_INFO_INCOMPLETE}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    CONSIGNOR_TEL_IS_NULL(LogisticsErrorCode.LOGISTICS_INFO_INCOMPLETE.code(), LogisticsErrorCode.LOGISTICS_INFO_INCOMPLETE.meta().userMessage()),

    /**
     * 配送公司ID不能为空，请填写后重新上传。。
     *
     * <p>引用统一码 {@link LogisticsErrorCode#LOGISTICS_INFO_INCOMPLETE}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link LogisticsErrorCode#LOGISTICS_INFO_INCOMPLETE}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    EXPRESS_ID_IS_NULL(LogisticsErrorCode.LOGISTICS_INFO_INCOMPLETE.code(), LogisticsErrorCode.LOGISTICS_INFO_INCOMPLETE.meta().userMessage()),

    /**
     * 商品ID不能为空，请填写后重新上传。。
     *
     * <p>引用统一码 {@link LogisticsErrorCode#LOGISTICS_INFO_INCOMPLETE}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link LogisticsErrorCode#LOGISTICS_INFO_INCOMPLETE}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    GOODS_ID_IS_NULL(LogisticsErrorCode.LOGISTICS_INFO_INCOMPLETE.code(), LogisticsErrorCode.LOGISTICS_INFO_INCOMPLETE.meta().userMessage()),

    /**
     * 快递单号不能为空，请填写后重新上传。。
     *
     * <p>引用统一码 {@link LogisticsErrorCode#LOGISTICS_INFO_INCOMPLETE}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link LogisticsErrorCode#LOGISTICS_INFO_INCOMPLETE}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    SHOPPING_CODE_IS_NULL(LogisticsErrorCode.LOGISTICS_INFO_INCOMPLETE.code(), LogisticsErrorCode.LOGISTICS_INFO_INCOMPLETE.meta().userMessage()),

    /**
     * 该订单存在退款或退货。
     *
     * <p>引用统一码 {@link AfterSaleErrorCode#AFTER_SALE_DUPLICATE_APPLY}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link AfterSaleErrorCode#AFTER_SALE_DUPLICATE_APPLY}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    ORDER_HAVE_REFUND_RETURN(AfterSaleErrorCode.AFTER_SALE_DUPLICATE_APPLY.code(), AfterSaleErrorCode.AFTER_SALE_DUPLICATE_APPLY.meta().userMessage()),

    /**
     * 该订单正处于售后状态，请勿发货！。
     *
     * <p>引用统一码 {@link AfterSaleErrorCode#AFTER_SALE_STATE_NOT_ALLOWED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link AfterSaleErrorCode#AFTER_SALE_STATE_NOT_ALLOWED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    ORDER_AFTER_SALES(AfterSaleErrorCode.AFTER_SALE_STATE_NOT_ALLOWED.code(), AfterSaleErrorCode.AFTER_SALE_STATE_NOT_ALLOWED.meta().userMessage()),

    /**
     * 该订单已完成售后，请勿操作！。
     *
     * <p>引用统一码 {@link AfterSaleErrorCode#AFTER_SALE_STATE_NOT_ALLOWED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link AfterSaleErrorCode#AFTER_SALE_STATE_NOT_ALLOWED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    ORDER_AFTER_SALES_COMPLETION(AfterSaleErrorCode.AFTER_SALE_STATE_NOT_ALLOWED.code(), AfterSaleErrorCode.AFTER_SALE_STATE_NOT_ALLOWED.meta().userMessage()),

    /**
     * 订单已支付，请勿重复支付！。
     *
     * <p>引用统一码 {@link PaymentErrorCode#PAYMENT_STATE_NOT_ALLOWED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link PaymentErrorCode#PAYMENT_STATE_NOT_ALLOWED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    ORDER_PAID(PaymentErrorCode.PAYMENT_STATE_NOT_ALLOWED.code(), PaymentErrorCode.PAYMENT_STATE_NOT_ALLOWED.meta().userMessage()),

    /**
     * 虚拟订单已过期。
     *
     * <p>引用统一码 {@link OrderErrorCode#ORDER_TIMEOUT_EXPIRED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link OrderErrorCode#ORDER_TIMEOUT_EXPIRED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    INVENTED_ORDER_IS_OVERDUE(OrderErrorCode.ORDER_TIMEOUT_EXPIRED.code(), OrderErrorCode.ORDER_TIMEOUT_EXPIRED.meta().userMessage()),

    /**
     * 扣款余额不足，请先确认其他结算单！。
     *
     * <p>引用统一码 {@link PaymentErrorCode#PAYMENT_BALANCE_NOT_ENOUGH}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link PaymentErrorCode#PAYMENT_BALANCE_NOT_ENOUGH}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    BALANCE_NOT_ENOUGH_ERROR(PaymentErrorCode.PAYMENT_BALANCE_NOT_ENOUGH.code(), PaymentErrorCode.PAYMENT_BALANCE_NOT_ENOUGH.meta().userMessage()),

    /**
     * 发票抬头不能为空。
     *
     * <p>引用统一码 {@link OrderErrorCode#ORDER_INVOICE_TITLE_REQUIRED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link OrderErrorCode#ORDER_INVOICE_TITLE_REQUIRED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    INVOICE_ADDRESS_TITLE_IS_NULL(OrderErrorCode.ORDER_INVOICE_TITLE_REQUIRED.code(), OrderErrorCode.ORDER_INVOICE_TITLE_REQUIRED.meta().userMessage()),

    /**
     * 没有获取到售后信息！。
     *
     * <p>引用统一码 {@link AfterSaleErrorCode#AFTER_SALE_RECORD_NOT_FOUND}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link AfterSaleErrorCode#AFTER_SALE_RECORD_NOT_FOUND}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    ORDER_AFTER_SALES_NULL(AfterSaleErrorCode.AFTER_SALE_RECORD_NOT_FOUND.code(), AfterSaleErrorCode.AFTER_SALE_RECORD_NOT_FOUND.meta().userMessage()),

    /**
     * 填写物流失败！。
     *
     * <p>引用统一码 {@link LogisticsErrorCode#LOGISTICS_DELIVERY_FAILED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link LogisticsErrorCode#LOGISTICS_DELIVERY_FAILED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    FILLING_LOGISTICS_ERROR(LogisticsErrorCode.LOGISTICS_DELIVERY_FAILED.code(), LogisticsErrorCode.LOGISTICS_DELIVERY_FAILED.meta().userMessage()),

    /**
     * 导出数量过大，请联系运维。
     *
     * <p>引用统一码 {@link SystemErrorCode#SYSTEM_EXPORT_FAILED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link SystemErrorCode#SYSTEM_EXPORT_FAILED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    ORDER_EXP_FAIL(SystemErrorCode.SYSTEM_EXPORT_FAILED.code(), SystemErrorCode.SYSTEM_EXPORT_FAILED.meta().userMessage()),

    /**
     * 申请成功。
     *
     * <p><b>成功语义，不属于错误码体系</b>：码值保持历史值 14057 不变。
     * 它不是一个错误码，不能编成 5 位统一码 —— 那会让前端把成功判成失败。
     * 前端统一用 {@code SuccessCode.isSuccess(code)} 判定，不要用码值硬比。
     */
    APPLICATION_SUCCESS(14057, "申请成功"),

    /**
     * 当前订单退货中,请勿重复操作。
     *
     * <p>引用统一码 {@link OrderErrorCode#ORDER_RETURN_IN_PROGRESS}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link OrderErrorCode#ORDER_RETURN_IN_PROGRESS}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    CURRENT_ORDER_RETURN_IN_PROGRESS(OrderErrorCode.ORDER_RETURN_IN_PROGRESS.code(), OrderErrorCode.ORDER_RETURN_IN_PROGRESS.meta().userMessage()),

    /**
     * 未超出售后时间。
     *
     * <p>引用统一码 {@link AfterSaleErrorCode#AFTER_SALE_STATE_NOT_ALLOWED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link AfterSaleErrorCode#AFTER_SALE_STATE_NOT_ALLOWED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    NOT_EXCEEDING_THE_TIME_AFTER_SALE(AfterSaleErrorCode.AFTER_SALE_STATE_NOT_ALLOWED.code(), AfterSaleErrorCode.AFTER_SALE_STATE_NOT_ALLOWED.meta().userMessage()),

    /**
     * 超出售后时间。
     *
     * <p>引用统一码 {@link AfterSaleErrorCode#AFTER_SALE_TIME_EXPIRED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link AfterSaleErrorCode#AFTER_SALE_TIME_EXPIRED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    ORDER_AFTER_TIME_EXCEED(AfterSaleErrorCode.AFTER_SALE_TIME_EXPIRED.code(), AfterSaleErrorCode.AFTER_SALE_TIME_EXPIRED.meta().userMessage()),

    /**
     * 此订单已过期 无法核销！。
     *
     * <p>引用统一码 {@link OrderErrorCode#ORDER_WRITE_OFF_NOT_ALLOWED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link OrderErrorCode#ORDER_WRITE_OFF_NOT_ALLOWED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    ORDER_EXPIRE_CANNOT_BE_CANCELLED(OrderErrorCode.ORDER_WRITE_OFF_NOT_ALLOWED.code(), OrderErrorCode.ORDER_WRITE_OFF_NOT_ALLOWED.meta().userMessage()),

    /**
     * 此订单退款中 无法核销！。
     *
     * <p>引用统一码 {@link OrderErrorCode#ORDER_WRITE_OFF_NOT_ALLOWED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link OrderErrorCode#ORDER_WRITE_OFF_NOT_ALLOWED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    ORDER_REFUND_CANNOT_BE_CANCELLED(OrderErrorCode.ORDER_WRITE_OFF_NOT_ALLOWED.code(), OrderErrorCode.ORDER_WRITE_OFF_NOT_ALLOWED.meta().userMessage()),

    /**
     * 此订单退款完成 无法核销！。
     *
     * <p>引用统一码 {@link OrderErrorCode#ORDER_WRITE_OFF_NOT_ALLOWED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link OrderErrorCode#ORDER_WRITE_OFF_NOT_ALLOWED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    ORDER_REFUND_COMPLETED(OrderErrorCode.ORDER_WRITE_OFF_NOT_ALLOWED.code(), OrderErrorCode.ORDER_WRITE_OFF_NOT_ALLOWED.meta().userMessage()),

    /**
     * 发货数量不能大于商品采购数量。
     *
     * <p>引用统一码 {@link LogisticsErrorCode#LOGISTICS_SHIPMENT_QUANTITY_EXCEEDED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link LogisticsErrorCode#LOGISTICS_SHIPMENT_QUANTITY_EXCEEDED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    SHIPMENT_QUANTITY_GT_GOODS__NUM(LogisticsErrorCode.LOGISTICS_SHIPMENT_QUANTITY_EXCEEDED.code(), LogisticsErrorCode.LOGISTICS_SHIPMENT_QUANTITY_EXCEEDED.meta().userMessage()),

    /**
     * 商品发货数量不能为空，请填写后重新上传。。
     *
     * <p>引用统一码 {@link LogisticsErrorCode#LOGISTICS_INFO_INCOMPLETE}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link LogisticsErrorCode#LOGISTICS_INFO_INCOMPLETE}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    GOODS_NUM_IS_NULL(LogisticsErrorCode.LOGISTICS_INFO_INCOMPLETE.code(), LogisticsErrorCode.LOGISTICS_INFO_INCOMPLETE.meta().userMessage()),

    /**
     * 订单状态已改变，请刷新后重试。
     *
     * <p>引用统一码 {@link OrderErrorCode#ORDER_STATE_NOT_ALLOWED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link OrderErrorCode#ORDER_STATE_NOT_ALLOWED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    ORDER_STATUS_CHANGE(OrderErrorCode.ORDER_STATE_NOT_ALLOWED.code(), OrderErrorCode.ORDER_STATE_NOT_ALLOWED.meta().userMessage()),

    /**
     * 当前订单已投诉。
     *
     * <p><b>成功语义，不属于错误码体系</b>：码值保持历史值 14071 不变。
     * 它不是一个错误码，不能编成 5 位统一码 —— 那会让前端把成功判成失败。
     * 前端统一用 {@code SuccessCode.isSuccess(code)} 判定，不要用码值硬比。
     */
    REVOKE_COMPLAINTS_SUCCESS(14071, "当前订单已投诉"),

    /**
     * 订单还未支付。
     *
     * <p>引用统一码 {@link OrderErrorCode#ORDER_NOT_FOUND}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link OrderErrorCode#ORDER_NOT_FOUND}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    ORDER_NOT_EXIST(OrderErrorCode.ORDER_NOT_FOUND.code(), OrderErrorCode.ORDER_NOT_FOUND.meta().userMessage()),

    /**
     * 运费错误。
     *
     * <p>引用统一码 {@link OrderErrorCode#ORDER_AMOUNT_INVALID}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link OrderErrorCode#ORDER_AMOUNT_INVALID}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    WRONG_FEE(OrderErrorCode.ORDER_AMOUNT_INVALID.code(), OrderErrorCode.ORDER_AMOUNT_INVALID.meta().userMessage()),

    /**
     * 金额错误。
     *
     * <p>引用统一码 {@link OrderErrorCode#ORDER_AMOUNT_INVALID}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link OrderErrorCode#ORDER_AMOUNT_INVALID}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    WRONG_AMOUNT(OrderErrorCode.ORDER_AMOUNT_INVALID.code(), OrderErrorCode.ORDER_AMOUNT_INVALID.meta().userMessage()),

    /**
     * 线下订单售后需上传支付凭证！。
     *
     * <p>引用统一码 {@link AfterSaleErrorCode#AFTER_SALE_PARAM_INVALID}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link AfterSaleErrorCode#AFTER_SALE_PARAM_INVALID}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    PAYMENT_VOUCHER_NULL(AfterSaleErrorCode.AFTER_SALE_PARAM_INVALID.code(), AfterSaleErrorCode.AFTER_SALE_PARAM_INVALID.meta().userMessage()),

    /**
     * 订单售后状态非平台待退款！。
     *
     * <p>引用统一码 {@link AfterSaleErrorCode#AFTER_SALE_STATE_NOT_ALLOWED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link AfterSaleErrorCode#AFTER_SALE_STATE_NOT_ALLOWED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    ORDER_AFTER_SALES_NOT_WAIT(AfterSaleErrorCode.AFTER_SALE_STATE_NOT_ALLOWED.code(), AfterSaleErrorCode.AFTER_SALE_STATE_NOT_ALLOWED.meta().userMessage()),

    /**
     * 商品已下架。
     *
     * <p>引用统一码 {@link GoodsErrorCode#GOODS_STATE_NOT_ALLOWED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link GoodsErrorCode#GOODS_STATE_NOT_ALLOWED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    PRODUCT_HAS_BEEN_REMOVED_FROM_THE_SHELF(GoodsErrorCode.GOODS_STATE_NOT_ALLOWED.code(), GoodsErrorCode.GOODS_STATE_NOT_ALLOWED.meta().userMessage()),

    /**
     * 虚拟商品无法加入购物车。
     *
     * <p>引用统一码 {@link CartErrorCode#CART_VIRTUAL_GOODS_NOT_ALLOWED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link CartErrorCode#CART_VIRTUAL_GOODS_NOT_ALLOWED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    VIRTUAL_PRODUCT_CANNOT_BE_ADDED_TO_SHOPPING_CART(CartErrorCode.CART_VIRTUAL_GOODS_NOT_ALLOWED.code(), CartErrorCode.CART_VIRTUAL_GOODS_NOT_ALLOWED.meta().userMessage()),

    /**
     * 请勿购买自家商品！。
     *
     * <p>引用统一码 {@link GoodsErrorCode#GOODS_SELF_PURCHASE_FORBIDDEN}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link GoodsErrorCode#GOODS_SELF_PURCHASE_FORBIDDEN}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    DO_NOT_PURCHASE_YOUR_OWN_PRODUCTS(GoodsErrorCode.GOODS_SELF_PURCHASE_FORBIDDEN.code(), GoodsErrorCode.GOODS_SELF_PURCHASE_FORBIDDEN.meta().userMessage()),

    /**
     * 宝贝数量超出范围！。
     *
     * <p>引用统一码 {@link ValidationErrorCode#VALUE_OUT_OF_RANGE}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link ValidationErrorCode#VALUE_OUT_OF_RANGE}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    TREASURES_IS_OUT_OF_RANGE(ValidationErrorCode.VALUE_OUT_OF_RANGE.code(), ValidationErrorCode.VALUE_OUT_OF_RANGE.meta().userMessage()),

    /**
     * 商品不存在，联系管理员！。
     *
     * <p>引用统一码 {@link GoodsErrorCode#GOODS_NOT_FOUND}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link GoodsErrorCode#GOODS_NOT_FOUND}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    GOODS_NOT_EXIST(GoodsErrorCode.GOODS_NOT_FOUND.code(), GoodsErrorCode.GOODS_NOT_FOUND.meta().userMessage()),

    /**
     * 规格重复！。
     *
     * <p>引用统一码 {@link GoodsErrorCode#GOODS_SPEC_DUPLICATE}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link GoodsErrorCode#GOODS_SPEC_DUPLICATE}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    SPEC_REPEAT(GoodsErrorCode.GOODS_SPEC_DUPLICATE.code(), GoodsErrorCode.GOODS_SPEC_DUPLICATE.meta().userMessage()),

    /**
     * 未传spuId！。
     *
     * <p>引用统一码 {@link ValidationErrorCode#PARAM_INVALID}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link ValidationErrorCode#PARAM_INVALID}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    SPU_ID_IS_NULL(ValidationErrorCode.PARAM_INVALID.code(), ValidationErrorCode.PARAM_INVALID.meta().userMessage()),

    /**
     * 未传skuId！。
     *
     * <p>引用统一码 {@link ValidationErrorCode#PARAM_INVALID}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link ValidationErrorCode#PARAM_INVALID}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    SKU_ID_IS_NULL(ValidationErrorCode.PARAM_INVALID.code(), ValidationErrorCode.PARAM_INVALID.meta().userMessage()),

    /**
     * 上架询价商品请开通询价功能或编辑商品。
     *
     * <p>引用统一码 {@link GoodsErrorCode#GOODS_ONLINE_NOT_ALLOWED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link GoodsErrorCode#GOODS_ONLINE_NOT_ALLOWED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    ONLINE_GOODS_ERROR(GoodsErrorCode.GOODS_ONLINE_NOT_ALLOWED.code(), GoodsErrorCode.GOODS_ONLINE_NOT_ALLOWED.meta().userMessage()),

    /**
     * 商品已下架。
     *
     * <p>引用统一码 {@link GoodsErrorCode#GOODS_STATE_NOT_ALLOWED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link GoodsErrorCode#GOODS_STATE_NOT_ALLOWED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    GOODS_OFFLINE(GoodsErrorCode.GOODS_STATE_NOT_ALLOWED.code(), GoodsErrorCode.GOODS_STATE_NOT_ALLOWED.meta().userMessage()),

    /**
     * 库存不能为空！。
     *
     * <p>引用统一码 {@link ValidationErrorCode#PARAM_INVALID}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link ValidationErrorCode#PARAM_INVALID}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    STOCK_ID_IS_NULL(ValidationErrorCode.PARAM_INVALID.code(), ValidationErrorCode.PARAM_INVALID.meta().userMessage()),

    /**
     * 无效必传参数：goodsImages为空！。
     *
     * <p>引用统一码 {@link GoodsErrorCode#GOODS_IMAGE_REQUIRED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link GoodsErrorCode#GOODS_IMAGE_REQUIRED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    GOODS_IMAGE_IS_NULL(GoodsErrorCode.GOODS_IMAGE_REQUIRED.code(), GoodsErrorCode.GOODS_IMAGE_REQUIRED.meta().userMessage()),

    /**
     * 规格不能为空！。
     *
     * <p>引用统一码 {@link GoodsErrorCode#GOODS_SPEC_VALUE_REQUIRED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link GoodsErrorCode#GOODS_SPEC_VALUE_REQUIRED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    SPEC_VALUE_IS_NULL(GoodsErrorCode.GOODS_SPEC_VALUE_REQUIRED.code(), GoodsErrorCode.GOODS_SPEC_VALUE_REQUIRED.meta().userMessage()),

    /**
     * 成功发布商品，该商品可在仓库商品查看！。
     *
     * <p><b>成功语义，不属于错误码体系</b>：码值保持历史值 15010 不变。
     * 它不是一个错误码，不能编成 5 位统一码 —— 那会让前端把成功判成失败。
     * 前端统一用 {@code SuccessCode.isSuccess(code)} 判定，不要用码值硬比。
     */
    ADD_GOODS_RELEASED(15010, "成功发布商品，该商品可在仓库商品查看！"),

    /**
     * 成功发布该商品，待平台审核！。
     *
     * <p><b>成功语义，不属于错误码体系</b>：码值保持历史值 15010 不变。
     * 它不是一个错误码，不能编成 5 位统一码 —— 那会让前端把成功判成失败。
     * 前端统一用 {@code SuccessCode.isSuccess(code)} 判定，不要用码值硬比。
     */
    ADD_GOODS_SUCCESS(15010, "成功发布该商品，待平台审核！"),

    /**
     * 存在商品在该售卖区域无货。
     *
     * <p>引用统一码 {@link GoodsErrorCode#GOODS_OUT_OF_STOCK}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link GoodsErrorCode#GOODS_OUT_OF_STOCK}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    PRODUCTS_OUT_OF_STOCK_IN_THIS_SALES_AREA(GoodsErrorCode.GOODS_OUT_OF_STOCK.code(), GoodsErrorCode.GOODS_OUT_OF_STOCK.meta().userMessage()),

    /**
     * 库存不足。
     *
     * <p>引用统一码 {@link InventoryErrorCode#INVENTORY_STOCK_NOT_ENOUGH}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link InventoryErrorCode#INVENTORY_STOCK_NOT_ENOUGH}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    INSUFFICIENT_INVENTORY(InventoryErrorCode.INVENTORY_STOCK_NOT_ENOUGH.code(), InventoryErrorCode.INVENTORY_STOCK_NOT_ENOUGH.meta().userMessage()),

    /**
     * 订单金额错误，请勿操作！。
     *
     * <p>引用统一码 {@link OrderErrorCode#ORDER_AMOUNT_INVALID}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link OrderErrorCode#ORDER_AMOUNT_INVALID}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    ORDER_AMOUNT_ERROR(OrderErrorCode.ORDER_AMOUNT_INVALID.code(), OrderErrorCode.ORDER_AMOUNT_INVALID.meta().userMessage()),

    /**
     * 当前商品数量超过最大限购数量!。
     *
     * <p>引用统一码 {@link OrderErrorCode#ORDER_QUANTITY_EXCEEDED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link OrderErrorCode#ORDER_QUANTITY_EXCEEDED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    GOODS_EXCEEDS_THE_MAXIMUM_PURCHASE_LIMIT(OrderErrorCode.ORDER_QUANTITY_EXCEEDED.code(), OrderErrorCode.ORDER_QUANTITY_EXCEEDED.meta().userMessage()),

    /**
     * 商品属性已有商品在使用暂不能删除。
     *
     * <p>引用统一码 {@link GoodsErrorCode#GOODS_ATTRIBUTE_IN_USE}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link GoodsErrorCode#GOODS_ATTRIBUTE_IN_USE}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    PRODUCT_ATTRIBUTE_HAS_BEEN_PUBLISHED(GoodsErrorCode.GOODS_ATTRIBUTE_IN_USE.code(), GoodsErrorCode.GOODS_ATTRIBUTE_IN_USE.meta().userMessage()),

    /**
     * 品牌名称已存在。
     *
     * <p>引用统一码 {@link GoodsErrorCode#GOODS_BRAND_DUPLICATE}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link GoodsErrorCode#GOODS_BRAND_DUPLICATE}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    BRAND_NAME_ALREADY_EXISTS(GoodsErrorCode.GOODS_BRAND_DUPLICATE.code(), GoodsErrorCode.GOODS_BRAND_DUPLICATE.meta().userMessage()),

    /**
     * 你输入的排序值超出范围!。
     *
     * <p>引用统一码 {@link ValidationErrorCode#VALUE_OUT_OF_RANGE}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link ValidationErrorCode#VALUE_OUT_OF_RANGE}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    SORTING_VALUE_YOU_ENTERED_IS_OUT_OF_RANGE(ValidationErrorCode.VALUE_OUT_OF_RANGE.code(), ValidationErrorCode.VALUE_OUT_OF_RANGE.meta().userMessage()),

    /**
     * 该品牌还有关联商品。
     *
     * <p>引用统一码 {@link GoodsErrorCode#GOODS_BRAND_IN_USE}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link GoodsErrorCode#GOODS_BRAND_IN_USE}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    BRAND_ASSOCIATED_GOODS(GoodsErrorCode.GOODS_BRAND_IN_USE.code(), GoodsErrorCode.GOODS_BRAND_IN_USE.meta().userMessage()),

    /**
     * 宝贝数量小于范围。
     *
     * <p>引用统一码 {@link ValidationErrorCode#VALUE_OUT_OF_RANGE}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link ValidationErrorCode#VALUE_OUT_OF_RANGE}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    TREASURES_IS_LESS_THAN_THE_RANGE(ValidationErrorCode.VALUE_OUT_OF_RANGE.code(), ValidationErrorCode.VALUE_OUT_OF_RANGE.meta().userMessage()),

    /**
     * 商品库存数量不足，请调整购买数量。
     *
     * <p>引用统一码 {@link InventoryErrorCode#INVENTORY_STOCK_NOT_ENOUGH}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link InventoryErrorCode#INVENTORY_STOCK_NOT_ENOUGH}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    EXCEEDING_INVENTORY_QUANTITY(InventoryErrorCode.INVENTORY_STOCK_NOT_ENOUGH.code(), InventoryErrorCode.INVENTORY_STOCK_NOT_ENOUGH.meta().userMessage()),

    /**
     * 在售库存不可大于实际总库存！。
     *
     * <p>引用统一码 {@link ValidationErrorCode#VALUE_OUT_OF_RANGE}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link ValidationErrorCode#VALUE_OUT_OF_RANGE}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    GOODS_STOCK_NOT_ENOUGH(ValidationErrorCode.VALUE_OUT_OF_RANGE.code(), ValidationErrorCode.VALUE_OUT_OF_RANGE.meta().userMessage()),

    /**
     * 商品未设置仓储信息！。
     *
     * <p>引用统一码 {@link InventoryErrorCode#INVENTORY_RECORD_NOT_FOUND}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link InventoryErrorCode#INVENTORY_RECORD_NOT_FOUND}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    GOODS_NOT_STOCK_INFO(InventoryErrorCode.INVENTORY_RECORD_NOT_FOUND.code(), InventoryErrorCode.INVENTORY_RECORD_NOT_FOUND.meta().userMessage()),

    /**
     * 成功发布自营商品！。
     *
     * <p><b>成功语义，不属于错误码体系</b>：码值保持历史值 15035 不变。
     * 它不是一个错误码，不能编成 5 位统一码 —— 那会让前端把成功判成失败。
     * 前端统一用 {@code SuccessCode.isSuccess(code)} 判定，不要用码值硬比。
     */
    ADD_SELF_GOODS_SUCCESS(15035, "成功发布自营商品！"),

    /**
     * 有商品库存为0，不允许进行上架操作。
     *
     * <p>引用统一码 {@link InventoryErrorCode#INVENTORY_STOCK_NOT_ENOUGH}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link InventoryErrorCode#INVENTORY_STOCK_NOT_ENOUGH}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    GOODS_STOCK_IS_ZERO(InventoryErrorCode.INVENTORY_STOCK_NOT_ENOUGH.code(), InventoryErrorCode.INVENTORY_STOCK_NOT_ENOUGH.meta().userMessage()),

    /**
     * 商品库存不足！。
     *
     * <p>引用统一码 {@link InventoryErrorCode#INVENTORY_STOCK_NOT_ENOUGH}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link InventoryErrorCode#INVENTORY_STOCK_NOT_ENOUGH}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    STOCK_INSUFFICIENT(InventoryErrorCode.INVENTORY_STOCK_NOT_ENOUGH.code(), InventoryErrorCode.INVENTORY_STOCK_NOT_ENOUGH.meta().userMessage()),

    /**
     * 缺少分类,不能上架。
     *
     * <p>引用统一码 {@link GoodsErrorCode#GOODS_CATEGORY_NOT_FOUND}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link GoodsErrorCode#GOODS_CATEGORY_NOT_FOUND}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    LACK_OF_CLASSIFICATION(GoodsErrorCode.GOODS_CATEGORY_NOT_FOUND.code(), GoodsErrorCode.GOODS_CATEGORY_NOT_FOUND.meta().userMessage()),

    /**
     * 商品版本不存在！。
     *
     * <p>引用统一码 {@link GoodsErrorCode#GOODS_VERSION_NOT_FOUND}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link GoodsErrorCode#GOODS_VERSION_NOT_FOUND}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    VERSION_NOT_EXIST(GoodsErrorCode.GOODS_VERSION_NOT_FOUND.code(), GoodsErrorCode.GOODS_VERSION_NOT_FOUND.meta().userMessage()),

    /**
     * 当前规格与购买商品不匹配！。
     *
     * <p>引用统一码 {@link GoodsErrorCode#GOODS_SPEC_NOT_MATCH}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link GoodsErrorCode#GOODS_SPEC_NOT_MATCH}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    SPEC_NOT_EXIST(GoodsErrorCode.GOODS_SPEC_NOT_MATCH.code(), GoodsErrorCode.GOODS_SPEC_NOT_MATCH.meta().userMessage()),

    /**
     * 成功修改自营商品！。
     *
     * <p><b>成功语义，不属于错误码体系</b>：码值保持历史值 15043 不变。
     * 它不是一个错误码，不能编成 5 位统一码 —— 那会让前端把成功判成失败。
     * 前端统一用 {@code SuccessCode.isSuccess(code)} 判定，不要用码值硬比。
     */
    UPDATE_SELF_GOODS_SUCCESS(15043, "成功修改自营商品！"),

    /**
     * 无店铺信息。
     *
     * <p>引用统一码 {@link MerchantErrorCode#STORE_NOT_OPENED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link MerchantErrorCode#STORE_NOT_OPENED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    NO_STORE_INFORMATION(MerchantErrorCode.STORE_NOT_OPENED.code(), MerchantErrorCode.STORE_NOT_OPENED.meta().userMessage()),

    /**
     * 查询收藏的店铺成功。
     *
     * <p><b>成功语义，不属于错误码体系</b>：码值保持历史值 16002 不变。
     * 它不是一个错误码，不能编成 5 位统一码 —— 那会让前端把成功判成失败。
     * 前端统一用 {@code SuccessCode.isSuccess(code)} 判定，不要用码值硬比。
     */
    SUCCESSFULLY_QUERIED_FAVORITE_STORES(16002, "查询收藏的店铺成功"),

    /**
     * 未查询到可以用代金券。
     *
     * <p>引用统一码 {@link MarketingErrorCode#MARKETING_VOUCHER_NOT_FOUND}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link MarketingErrorCode#MARKETING_VOUCHER_NOT_FOUND}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    NO_USABLE_VOUCHERS_FOUND(MarketingErrorCode.MARKETING_VOUCHER_NOT_FOUND.code(), MarketingErrorCode.MARKETING_VOUCHER_NOT_FOUND.meta().userMessage()),

    /**
     * 由于当前有未完成开票的订单，暂时无法关闭，请至订单页面查看订单进度。
     *
     * <p>引用统一码 {@link OrderErrorCode#ORDER_INVOICE_UNFINISHED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link OrderErrorCode#ORDER_INVOICE_UNFINISHED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    ALSO_INCOMPLETE_INVOICE(OrderErrorCode.ORDER_INVOICE_UNFINISHED.code(), OrderErrorCode.ORDER_INVOICE_UNFINISHED.meta().userMessage()),

    /**
     * 红包已使用，请重新选择！。
     *
     * <p>引用统一码 {@link MarketingErrorCode#MARKETING_RED_ENVELOPE_USED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link MarketingErrorCode#MARKETING_RED_ENVELOPE_USED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    RED_ENVELOPE_USED(MarketingErrorCode.MARKETING_RED_ENVELOPE_USED.code(), MarketingErrorCode.MARKETING_RED_ENVELOPE_USED.meta().userMessage()),

    /**
     * 经营类目不符合！。
     *
     * <p>引用统一码 {@link MerchantErrorCode#STORE_BUSINESS_CATEGORY_MISMATCH}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link MerchantErrorCode#STORE_BUSINESS_CATEGORY_MISMATCH}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    BUSINESS_CATEGORT_NOT_MATCH(MerchantErrorCode.STORE_BUSINESS_CATEGORY_MISMATCH.code(), MerchantErrorCode.STORE_BUSINESS_CATEGORY_MISMATCH.meta().userMessage()),

    /**
     * 该店铺还有未完成订单或仍有订单处于售后期内，暂时无法关闭店铺！。
     *
     * <p>引用统一码 {@link MerchantErrorCode#STORE_HAS_UNFINISHED_ORDER}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link MerchantErrorCode#STORE_HAS_UNFINISHED_ORDER}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    UNFINISHED_ORDERS_UNDER_THE_STORE(MerchantErrorCode.STORE_HAS_UNFINISHED_ORDER.code(), MerchantErrorCode.STORE_HAS_UNFINISHED_ORDER.meta().userMessage()),

    /**
     * 解码失败。
     *
     * <p>引用统一码 {@link SystemErrorCode#SYSTEM_SERIALIZE_FAILED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link SystemErrorCode#SYSTEM_SERIALIZE_FAILED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    DECODING_FAILED(SystemErrorCode.SYSTEM_SERIALIZE_FAILED.code(), SystemErrorCode.SYSTEM_SERIALIZE_FAILED.meta().userMessage()),

    /**
     * 店铺已被关闭，请联系平台。
     *
     * <p>引用统一码 {@link MerchantErrorCode#STORE_CLOSED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link MerchantErrorCode#STORE_CLOSED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    THE_STORE_HAS_BEEN_CLOSED(MerchantErrorCode.STORE_CLOSED.code(), MerchantErrorCode.STORE_CLOSED.meta().userMessage()),

    /**
     * 该用户未开通店铺。
     *
     * <p>引用统一码 {@link MerchantErrorCode#STORE_NOT_OPENED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link MerchantErrorCode#STORE_NOT_OPENED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    USER_HAS_NOT_OPENED_A_STORE(MerchantErrorCode.STORE_NOT_OPENED.code(), MerchantErrorCode.STORE_NOT_OPENED.meta().userMessage()),

    /**
     * 该用户已开通店铺或是其他店铺的子账号。
     *
     * <p>引用统一码 {@link MerchantErrorCode#STORE_ALREADY_OPENED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link MerchantErrorCode#STORE_ALREADY_OPENED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    USER_HAS_OPENED_A_STORE(MerchantErrorCode.STORE_ALREADY_OPENED.code(), MerchantErrorCode.STORE_ALREADY_OPENED.meta().userMessage()),

    /**
     * 图片空间已达上限，限制范围内的图片已上传成功。
     *
     * <p>引用统一码 {@link FileErrorCode#FILE_STORAGE_QUOTA_EXCEEDED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link FileErrorCode#FILE_STORAGE_QUOTA_EXCEEDED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    IMAGE_SPACE_LIMIT(FileErrorCode.FILE_STORAGE_QUOTA_EXCEEDED.code(), FileErrorCode.FILE_STORAGE_QUOTA_EXCEEDED.meta().userMessage()),

    /**
     * 仓库中还有关联商品。
     *
     * <p>引用统一码 {@link MerchantErrorCode#STORAGE_GOODS_LINKED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link MerchantErrorCode#STORAGE_GOODS_LINKED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    HAS_STORAGE_GOODS(MerchantErrorCode.STORAGE_GOODS_LINKED.code(), MerchantErrorCode.STORAGE_GOODS_LINKED.meta().userMessage()),

    /**
     * 仓库中没有该商品。
     *
     * <p>引用统一码 {@link MerchantErrorCode#STORAGE_GOODS_NOT_FOUND}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link MerchantErrorCode#STORAGE_GOODS_NOT_FOUND}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    STORAGE_NOT_HAS_GOODS(MerchantErrorCode.STORAGE_GOODS_NOT_FOUND.code(), MerchantErrorCode.STORAGE_GOODS_NOT_FOUND.meta().userMessage()),

    /**
     * 店铺经营类目不能为空。
     *
     * <p>引用统一码 {@link MerchantErrorCode#STORE_BUSINESS_CATEGORY_REQUIRED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link MerchantErrorCode#STORE_BUSINESS_CATEGORY_REQUIRED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    SHOP_BUSINESS_CATEGORY_NOT_NULL(MerchantErrorCode.STORE_BUSINESS_CATEGORY_REQUIRED.code(), MerchantErrorCode.STORE_BUSINESS_CATEGORY_REQUIRED.meta().userMessage()),

    /**
     * 店铺经营类目不符。
     *
     * <p>引用统一码 {@link MerchantErrorCode#STORE_BUSINESS_CATEGORY_MISMATCH}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link MerchantErrorCode#STORE_BUSINESS_CATEGORY_MISMATCH}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    SHOP_BUSINESS_CATEGORY_DISCREPANCY(MerchantErrorCode.STORE_BUSINESS_CATEGORY_MISMATCH.code(), MerchantErrorCode.STORE_BUSINESS_CATEGORY_MISMATCH.meta().userMessage()),

    /**
     * 入驻id不能为空。
     *
     * <p>引用统一码 {@link MerchantErrorCode#STORE_JOIN_INFO_REQUIRED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link MerchantErrorCode#STORE_JOIN_INFO_REQUIRED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    JOIN_ID_CANNOT_BE_EMPTY(MerchantErrorCode.STORE_JOIN_INFO_REQUIRED.code(), MerchantErrorCode.STORE_JOIN_INFO_REQUIRED.meta().userMessage()),

    /**
     * 红包已过期，请重新选择！。
     *
     * <p>引用统一码 {@link MarketingErrorCode#MARKETING_RED_ENVELOPE_EXPIRED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link MarketingErrorCode#MARKETING_RED_ENVELOPE_EXPIRED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    RED_ENVELOPE_EXPIRE(MarketingErrorCode.MARKETING_RED_ENVELOPE_EXPIRED.code(), MarketingErrorCode.MARKETING_RED_ENVELOPE_EXPIRED.meta().userMessage()),

    /**
     * 优惠券已过期，请重新选择！。
     *
     * <p>引用统一码 {@link MarketingErrorCode#MARKETING_COUPON_EXPIRED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link MarketingErrorCode#MARKETING_COUPON_EXPIRED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    COUPON_EXPIRE(MarketingErrorCode.MARKETING_COUPON_EXPIRED.code(), MarketingErrorCode.MARKETING_COUPON_EXPIRED.meta().userMessage()),

    /**
     * 优惠券已收回，请重新选择！。
     *
     * <p>引用统一码 {@link MarketingErrorCode#MARKETING_COUPON_RECYCLED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link MarketingErrorCode#MARKETING_COUPON_RECYCLED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    COUPON_RECYCLE(MarketingErrorCode.MARKETING_COUPON_RECYCLED.code(), MarketingErrorCode.MARKETING_COUPON_RECYCLED.meta().userMessage()),

    /**
     * 未填写仓库信息！。
     *
     * <p>引用统一码 {@link MerchantErrorCode#STORE_STORAGE_REQUIRED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link MerchantErrorCode#STORE_STORAGE_REQUIRED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    STORAGE_ID_NOT_EXIST(MerchantErrorCode.STORE_STORAGE_REQUIRED.code(), MerchantErrorCode.STORE_STORAGE_REQUIRED.meta().userMessage()),

    /**
     * 收货人信息为空！。
     *
     * <p>引用统一码 {@link OrderErrorCode#ORDER_ADDRESS_RECEIVER_EMPTY}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link OrderErrorCode#ORDER_ADDRESS_RECEIVER_EMPTY}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    ORDER_RECEIVER_INFO_EMPTY(OrderErrorCode.ORDER_ADDRESS_RECEIVER_EMPTY.code(), OrderErrorCode.ORDER_ADDRESS_RECEIVER_EMPTY.meta().userMessage()),

    /**
     * 该充值卡已使用！。
     *
     * <p>引用统一码 {@link PaymentErrorCode#PAYMENT_RECHARGE_CARD_INVALID}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link PaymentErrorCode#PAYMENT_RECHARGE_CARD_INVALID}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    CARD_HAS_ALREADY_BEEN_USED(PaymentErrorCode.PAYMENT_RECHARGE_CARD_INVALID.code(), PaymentErrorCode.PAYMENT_RECHARGE_CARD_INVALID.meta().userMessage()),

    /**
     * 充值成功！。
     *
     * <p><b>成功语义，不属于错误码体系</b>：码值保持历史值 17002 不变。
     * 它不是一个错误码，不能编成 5 位统一码 —— 那会让前端把成功判成失败。
     * 前端统一用 {@code SuccessCode.isSuccess(code)} 判定，不要用码值硬比。
     */
    RECHARGED_SUCCESSFULLY(17002, "充值成功！"),

    /**
     * 充值失败,请联系客服！。
     *
     * <p>引用统一码 {@link PaymentErrorCode#PAYMENT_RECHARGE_FAILED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link PaymentErrorCode#PAYMENT_RECHARGE_FAILED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    RECHARGE_FAILED(PaymentErrorCode.PAYMENT_RECHARGE_FAILED.code(), PaymentErrorCode.PAYMENT_RECHARGE_FAILED.meta().userMessage()),

    /**
     * 充值卡未激活，请联系客服！。
     *
     * <p>引用统一码 {@link PaymentErrorCode#PAYMENT_RECHARGE_CARD_INVALID}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link PaymentErrorCode#PAYMENT_RECHARGE_CARD_INVALID}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    RECHARGE_CARD_NOT_ACTIVATED(PaymentErrorCode.PAYMENT_RECHARGE_CARD_INVALID.code(), PaymentErrorCode.PAYMENT_RECHARGE_CARD_INVALID.meta().userMessage()),

    /**
     * 账户余额不足！。
     *
     * <p>引用统一码 {@link PaymentErrorCode#PAYMENT_BALANCE_NOT_ENOUGH}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link PaymentErrorCode#PAYMENT_BALANCE_NOT_ENOUGH}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    INSUFFICIENT_ACCOUNT_BALANCE(PaymentErrorCode.PAYMENT_BALANCE_NOT_ENOUGH.code(), PaymentErrorCode.PAYMENT_BALANCE_NOT_ENOUGH.meta().userMessage()),

    /**
     * 提现金额格式不正确，请重新操作！。
     *
     * <p>引用统一码 {@link PaymentErrorCode#PAYMENT_WITHDRAW_AMOUNT_INVALID}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link PaymentErrorCode#PAYMENT_WITHDRAW_AMOUNT_INVALID}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    INCORRECT_FORMAT_OF_WITHDRAWAL_AMOUNT(PaymentErrorCode.PAYMENT_WITHDRAW_AMOUNT_INVALID.code(), PaymentErrorCode.PAYMENT_WITHDRAW_AMOUNT_INVALID.meta().userMessage()),

    /**
     * 提现申请已提交，等待审核中！。
     *
     * <p>引用统一码 {@link PaymentErrorCode#PAYMENT_WITHDRAW_PENDING}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link PaymentErrorCode#PAYMENT_WITHDRAW_PENDING}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    CASH_WITHDRAWAL_APPLICATION(PaymentErrorCode.PAYMENT_WITHDRAW_PENDING.code(), PaymentErrorCode.PAYMENT_WITHDRAW_PENDING.meta().userMessage()),

    /**
     * 提现申请提交失败！。
     *
     * <p>引用统一码 {@link PaymentErrorCode#PAYMENT_WITHDRAW_FAILED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link PaymentErrorCode#PAYMENT_WITHDRAW_FAILED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    WITHDRAWAL_FAILED(PaymentErrorCode.PAYMENT_WITHDRAW_FAILED.code(), PaymentErrorCode.PAYMENT_WITHDRAW_FAILED.meta().userMessage()),

    /**
     * 支付密码错误！。
     *
     * <p>引用统一码 {@link PaymentErrorCode#PAYMENT_PASSWORD_ERROR}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link PaymentErrorCode#PAYMENT_PASSWORD_ERROR}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    PAYMENT_PASSWORD_ERROR(PaymentErrorCode.PAYMENT_PASSWORD_ERROR.code(), PaymentErrorCode.PAYMENT_PASSWORD_ERROR.meta().userMessage()),

    /**
     * 转账成功！。
     *
     * <p><b>成功语义，不属于错误码体系</b>：码值保持历史值 17010 不变。
     * 它不是一个错误码，不能编成 5 位统一码 —— 那会让前端把成功判成失败。
     * 前端统一用 {@code SuccessCode.isSuccess(code)} 判定，不要用码值硬比。
     */
    TRANSFER_SUCCEEDED(17010, "转账成功！"),

    /**
     * 资产更新失败！。
     *
     * <p>引用统一码 {@link PaymentErrorCode#PAYMENT_ASSET_UPDATE_FAILED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link PaymentErrorCode#PAYMENT_ASSET_UPDATE_FAILED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    ASSET_UPDATE_FAILED(PaymentErrorCode.PAYMENT_ASSET_UPDATE_FAILED.code(), PaymentErrorCode.PAYMENT_ASSET_UPDATE_FAILED.meta().userMessage()),

    /**
     * 记录输入失败！。
     *
     * <p>引用统一码 {@link PaymentErrorCode#PAYMENT_ASSET_UPDATE_FAILED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link PaymentErrorCode#PAYMENT_ASSET_UPDATE_FAILED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    RECORD_INPUT_FAILED(PaymentErrorCode.PAYMENT_ASSET_UPDATE_FAILED.code(), PaymentErrorCode.PAYMENT_ASSET_UPDATE_FAILED.meta().userMessage()),

    /**
     * 转账失败！。
     *
     * <p>引用统一码 {@link PaymentErrorCode#PAYMENT_TRANSFER_FAILED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link PaymentErrorCode#PAYMENT_TRANSFER_FAILED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    TRANSFER_FAILED(PaymentErrorCode.PAYMENT_TRANSFER_FAILED.code(), PaymentErrorCode.PAYMENT_TRANSFER_FAILED.meta().userMessage()),

    /**
     * 未设置密码，请前往密码设置！。
     *
     * <p>引用统一码 {@link PaymentErrorCode#PAYMENT_PASSWORD_NOT_SET}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link PaymentErrorCode#PAYMENT_PASSWORD_NOT_SET}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    PASSWORD_NOT_SET(PaymentErrorCode.PAYMENT_PASSWORD_NOT_SET.code(), PaymentErrorCode.PAYMENT_PASSWORD_NOT_SET.meta().userMessage()),

    /**
     * 密码错误，可联系企业管理员修改！。
     *
     * <p>引用统一码 {@link PaymentErrorCode#PAYMENT_PASSWORD_ERROR}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link PaymentErrorCode#PAYMENT_PASSWORD_ERROR}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    PASSWORD_ERROR_ONE(PaymentErrorCode.PAYMENT_PASSWORD_ERROR.code(), PaymentErrorCode.PAYMENT_PASSWORD_ERROR.meta().userMessage()),

    /**
     * 密码错误，可以选择密码重置！。
     *
     * <p>引用统一码 {@link PaymentErrorCode#PAYMENT_PASSWORD_ERROR}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link PaymentErrorCode#PAYMENT_PASSWORD_ERROR}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    PASSWORD_ERROR_TWO(PaymentErrorCode.PAYMENT_PASSWORD_ERROR.code(), PaymentErrorCode.PAYMENT_PASSWORD_ERROR.meta().userMessage()),

    /**
     * 提现申请提交失败!。
     *
     * <p>引用统一码 {@link PaymentErrorCode#PAYMENT_WITHDRAW_FAILED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link PaymentErrorCode#PAYMENT_WITHDRAW_FAILED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    WITHDRAWAL_REQUEST_FAILED(PaymentErrorCode.PAYMENT_WITHDRAW_FAILED.code(), PaymentErrorCode.PAYMENT_WITHDRAW_FAILED.meta().userMessage()),

    /**
     * 有未确认结算单，请确认！。
     *
     * <p>引用统一码 {@link PaymentErrorCode#PAYMENT_SETTLEMENT_UNCONFIRMED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link PaymentErrorCode#PAYMENT_SETTLEMENT_UNCONFIRMED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    UNCONFIRMED_SETTLEMENT__DOCUMENTS(PaymentErrorCode.PAYMENT_SETTLEMENT_UNCONFIRMED.code(), PaymentErrorCode.PAYMENT_SETTLEMENT_UNCONFIRMED.meta().userMessage()),

    /**
     * 支付成功。
     *
     * <p><b>成功语义，不属于错误码体系</b>：码值保持历史值 17023 不变。
     * 它不是一个错误码，不能编成 5 位统一码 —— 那会让前端把成功判成失败。
     * 前端统一用 {@code SuccessCode.isSuccess(code)} 判定，不要用码值硬比。
     */
    PAYMENT_SUCCESSFUL(17023, "支付成功"),

    /**
     * 支付失败。
     *
     * <p>引用统一码 {@link PaymentErrorCode#PAYMENT_CHANNEL_CALL_FAILED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link PaymentErrorCode#PAYMENT_CHANNEL_CALL_FAILED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    PAYMENT_FAILED(PaymentErrorCode.PAYMENT_CHANNEL_CALL_FAILED.code(), PaymentErrorCode.PAYMENT_CHANNEL_CALL_FAILED.meta().userMessage()),

    /**
     * 密码错误三次！。
     *
     * <p>引用统一码 {@link PaymentErrorCode#PAYMENT_PASSWORD_LOCKED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link PaymentErrorCode#PAYMENT_PASSWORD_LOCKED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    PASSWORD_ERROR_THREE_TIMES(PaymentErrorCode.PAYMENT_PASSWORD_LOCKED.code(), PaymentErrorCode.PAYMENT_PASSWORD_LOCKED.meta().userMessage()),

    /**
     * 购物卡还未生效。
     *
     * <p>引用统一码 {@link MarketingErrorCode#MARKETING_CARD_NOT_STARTED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link MarketingErrorCode#MARKETING_CARD_NOT_STARTED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    CARD_NO_START(MarketingErrorCode.MARKETING_CARD_NOT_STARTED.code(), MarketingErrorCode.MARKETING_CARD_NOT_STARTED.meta().userMessage()),

    /**
     * 购物卡已经失效。
     *
     * <p>引用统一码 {@link MarketingErrorCode#MARKETING_CARD_EXPIRED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link MarketingErrorCode#MARKETING_CARD_EXPIRED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    CARD_IS_END(MarketingErrorCode.MARKETING_CARD_EXPIRED.code(), MarketingErrorCode.MARKETING_CARD_EXPIRED.meta().userMessage()),

    /**
     * 该商家已授权小程序。
     *
     * <p>引用统一码 {@link MerchantErrorCode#STORE_MINIAPP_AUTHORIZED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link MerchantErrorCode#STORE_MINIAPP_AUTHORIZED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    MERCHANT_HAS_AUTHORIZED_THE_MINI_PROGRAM(MerchantErrorCode.STORE_MINIAPP_AUTHORIZED.code(), MerchantErrorCode.STORE_MINIAPP_AUTHORIZED.meta().userMessage()),

    /**
     * 该商家未授权小程序。
     *
     * <p>引用统一码 {@link MerchantErrorCode#STORE_MINIAPP_NOT_AUTHORIZED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link MerchantErrorCode#STORE_MINIAPP_NOT_AUTHORIZED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    MERCHANT_IS_NOT_AUTHORIZED_FOR_THE_MINI_PROGRAM(MerchantErrorCode.STORE_MINIAPP_NOT_AUTHORIZED.code(), MerchantErrorCode.STORE_MINIAPP_NOT_AUTHORIZED.meta().userMessage()),

    /**
     * 查询到的代金券为空！。
     *
     * <p>引用统一码 {@link MarketingErrorCode#MARKETING_VOUCHER_NOT_FOUND}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link MarketingErrorCode#MARKETING_VOUCHER_NOT_FOUND}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    VOUCHER_ISEMPTY(MarketingErrorCode.MARKETING_VOUCHER_NOT_FOUND.code(), MarketingErrorCode.MARKETING_VOUCHER_NOT_FOUND.meta().userMessage()),

    /**
     * 订单金额小于代金券面额则不可添加！。
     *
     * <p>引用统一码 {@link MarketingErrorCode#MARKETING_VOUCHER_AMOUNT_INVALID}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link MarketingErrorCode#MARKETING_VOUCHER_AMOUNT_INVALID}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    INCORRECT_AMOUNT(MarketingErrorCode.MARKETING_VOUCHER_AMOUNT_INVALID.code(), MarketingErrorCode.MARKETING_VOUCHER_AMOUNT_INVALID.meta().userMessage()),

    /**
     * 未选择商品！。
     *
     * <p>引用统一码 {@link MarketingErrorCode#MARKETING_GOODS_NOT_SELECTED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link MarketingErrorCode#MARKETING_GOODS_NOT_SELECTED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    NO_CHOICE_GOODS(MarketingErrorCode.MARKETING_GOODS_NOT_SELECTED.code(), MarketingErrorCode.MARKETING_GOODS_NOT_SELECTED.meta().userMessage()),

    /**
     * 优惠券已使用，请重新选择！。
     *
     * <p>引用统一码 {@link MarketingErrorCode#MARKETING_COUPON_USED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link MarketingErrorCode#MARKETING_COUPON_USED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    COUPON_USED(MarketingErrorCode.MARKETING_COUPON_USED.code(), MarketingErrorCode.MARKETING_COUPON_USED.meta().userMessage()),

    /**
     * 该商品已达购买上限。
     *
     * <p>引用统一码 {@link MarketingErrorCode#MARKETING_ACTIVITY_LIMIT_REACHED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link MarketingErrorCode#MARKETING_ACTIVITY_LIMIT_REACHED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    ACTIVITY_LIMIT_PURCHASE_LIMIT(MarketingErrorCode.MARKETING_ACTIVITY_LIMIT_REACHED.code(), MarketingErrorCode.MARKETING_ACTIVITY_LIMIT_REACHED.meta().userMessage()),

    /**
     * 未达上限，可以购买。
     *
     * <p><b>成功语义，不属于错误码体系</b>：码值保持历史值 18006 不变。
     * 它不是一个错误码，不能编成 5 位统一码 —— 那会让前端把成功判成失败。
     * 前端统一用 {@code SuccessCode.isSuccess(code)} 判定，不要用码值硬比。
     */
    ACTIVITY_NO_PURCHASE_LIMIT(18006, "未达上限，可以购买"),

    /**
     * 活动未开始或活动已结束。
     *
     * <p>引用统一码 {@link MarketingErrorCode#MARKETING_ACTIVITY_NOT_ACTIVE}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link MarketingErrorCode#MARKETING_ACTIVITY_NOT_ACTIVE}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    ACTIVITY_ACTIVITY_ENDED(MarketingErrorCode.MARKETING_ACTIVITY_NOT_ACTIVE.code(), MarketingErrorCode.MARKETING_ACTIVITY_NOT_ACTIVE.meta().userMessage()),

    /**
     * 未开通会员或会员已过期。
     *
     * <p>引用统一码 {@link MarketingErrorCode#MARKETING_MEMBER_EXPIRED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link MarketingErrorCode#MARKETING_MEMBER_EXPIRED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    PLUS_MEMBER_NO_EXIST(MarketingErrorCode.MARKETING_MEMBER_EXPIRED.code(), MarketingErrorCode.MARKETING_MEMBER_EXPIRED.meta().userMessage()),

    /**
     * 查询代金券模板成功。
     *
     * <p><b>成功语义，不属于错误码体系</b>：码值保持历史值 18007 不变。
     * 它不是一个错误码，不能编成 5 位统一码 —— 那会让前端把成功判成失败。
     * 前端统一用 {@code SuccessCode.isSuccess(code)} 判定，不要用码值硬比。
     */
    VOUCHER_TEMPLATE_SUCCESS(18007, "查询代金券模板成功"),

    /**
     * 新增代金券成功。
     *
     * <p><b>成功语义，不属于错误码体系</b>：码值保持历史值 18008 不变。
     * 它不是一个错误码，不能编成 5 位统一码 —— 那会让前端把成功判成失败。
     * 前端统一用 {@code SuccessCode.isSuccess(code)} 判定，不要用码值硬比。
     */
    INSERT_VOUCHER_TEMPLATE_SUCCESS(18008, "新增代金券成功"),

    /**
     * 无效参数。
     *
     * <p>引用统一码 {@link ValidationErrorCode#PARAM_INVALID}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link ValidationErrorCode#PARAM_INVALID}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    INVALID_PARAMETER(ValidationErrorCode.PARAM_INVALID.code(), ValidationErrorCode.PARAM_INVALID.meta().userMessage()),

    /**
     * 请解除关联活动,再删除礼品。
     *
     * <p>引用统一码 {@link SystemErrorCode#SYSTEM_RESOURCE_IN_USE}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link SystemErrorCode#SYSTEM_RESOURCE_IN_USE}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    REMOVE_THE_ASSOCIATED_ACTIVITY(SystemErrorCode.SYSTEM_RESOURCE_IN_USE.code(), SystemErrorCode.SYSTEM_RESOURCE_IN_USE.meta().userMessage()),

    /**
     * 赠送数量超出红包总数。
     *
     * <p>引用统一码 {@link MarketingErrorCode#MARKETING_RED_ENVELOPE_QUANTITY_EXCEEDED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link MarketingErrorCode#MARKETING_RED_ENVELOPE_QUANTITY_EXCEEDED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    TOTAL_NUMBER_OF_RED_ENVELOPES(MarketingErrorCode.MARKETING_RED_ENVELOPE_QUANTITY_EXCEEDED.code(), MarketingErrorCode.MARKETING_RED_ENVELOPE_QUANTITY_EXCEEDED.meta().userMessage()),

    /**
     * 订单限额不可以小于红包面额。
     *
     * <p>引用统一码 {@link MarketingErrorCode#MARKETING_RED_ENVELOPE_LIMIT_INVALID}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link MarketingErrorCode#MARKETING_RED_ENVELOPE_LIMIT_INVALID}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    ORDER_LIMIT_CANNOT_BE_LESS(MarketingErrorCode.MARKETING_RED_ENVELOPE_LIMIT_INVALID.code(), MarketingErrorCode.MARKETING_RED_ENVELOPE_LIMIT_INVALID.meta().userMessage()),

    /**
     * 卡片数量需大于0。
     *
     * <p>引用统一码 {@link MarketingErrorCode#MARKETING_CARD_QUANTITY_INVALID}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link MarketingErrorCode#MARKETING_CARD_QUANTITY_INVALID}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    CARD_NUMBER_MUST_GT_ZERO(MarketingErrorCode.MARKETING_CARD_QUANTITY_INVALID.code(), MarketingErrorCode.MARKETING_CARD_QUANTITY_INVALID.meta().userMessage()),

    /**
     * 请选择图片。
     *
     * <p>引用统一码 {@link FileErrorCode#FILE_NOT_PROVIDED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link FileErrorCode#FILE_NOT_PROVIDED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    IMAGE_IS_NULL(FileErrorCode.FILE_NOT_PROVIDED.code(), FileErrorCode.FILE_NOT_PROVIDED.meta().userMessage()),

    /**
     * 未查到markid。
     *
     * <p>引用统一码 {@link ValidationErrorCode#PARAM_INVALID}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link ValidationErrorCode#PARAM_INVALID}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    MARKID_IS_NULL(ValidationErrorCode.PARAM_INVALID.code(), ValidationErrorCode.PARAM_INVALID.meta().userMessage()),

    /**
     * 当前兑换券活动已失效！。
     *
     * <p>引用统一码 {@link MarketingErrorCode#MARKETING_VOUCHER_ACTIVITY_INVALID}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link MarketingErrorCode#MARKETING_VOUCHER_ACTIVITY_INVALID}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    PRODUCT_VOUCHET_INVALID(MarketingErrorCode.MARKETING_VOUCHER_ACTIVITY_INVALID.code(), MarketingErrorCode.MARKETING_VOUCHER_ACTIVITY_INVALID.meta().userMessage()),

    /**
     * 当前商品不适用当前兑换券！。
     *
     * <p>引用统一码 {@link MarketingErrorCode#MARKETING_VOUCHER_GOODS_NOT_APPLICABLE}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link MarketingErrorCode#MARKETING_VOUCHER_GOODS_NOT_APPLICABLE}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    PRODUCT_VOUCHET_GOODS_ICAPPLICABLE(MarketingErrorCode.MARKETING_VOUCHER_GOODS_NOT_APPLICABLE.code(), MarketingErrorCode.MARKETING_VOUCHER_GOODS_NOT_APPLICABLE.meta().userMessage()),

    /**
     * 您的兑换券已过期或已无可用兑换券！。
     *
     * <p>引用统一码 {@link MarketingErrorCode#MARKETING_VOUCHER_EXPIRED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link MarketingErrorCode#MARKETING_VOUCHER_EXPIRED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    USER_PRODUCT_VOUCHET_EXPIRED(MarketingErrorCode.MARKETING_VOUCHER_EXPIRED.code(), MarketingErrorCode.MARKETING_VOUCHER_EXPIRED.meta().userMessage()),

    /**
     * 二开扩展中断。
     *
     * <p>引用统一码 {@link PluginPlatformErrorCode#PLUGIN_EXTENSION_ABORTED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link PluginPlatformErrorCode#PLUGIN_EXTENSION_ABORTED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    PLUGIN_ABORT(PluginPlatformErrorCode.PLUGIN_EXTENSION_ABORTED.code(), PluginPlatformErrorCode.PLUGIN_EXTENSION_ABORTED.meta().userMessage()),

    /**
     * 二开扩展故障。
     *
     * <p>引用统一码 {@link PluginPlatformErrorCode#PLUGIN_EXTENSION_FAULT}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link PluginPlatformErrorCode#PLUGIN_EXTENSION_FAULT}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    PLUGIN_FAULT(PluginPlatformErrorCode.PLUGIN_EXTENSION_FAULT.code(), PluginPlatformErrorCode.PLUGIN_EXTENSION_FAULT.meta().userMessage()),

    // 以下三项曾被 9e8222ed3 误删（base 的 BaseFloorAdpageServiceImpl / BaseWxLiveGoodsServiceImpl 仍在引用），
    // 名称与文案保持删除前原样，不做修正，避免改变既有行为。
    UPDATE_ADPAGE_Floor_MAX_ERROP(ValidationErrorCode.FLOOR_GOODS_COUNT_EXCEEDED.code(),
            ValidationErrorCode.FLOOR_GOODS_COUNT_EXCEEDED.meta().userMessage()),

    UPDATE_ADPAGE_Floor_MIN__ERROP(ValidationErrorCode.FLOOR_GOODS_COUNT_TOO_FEW.code(),
            ValidationErrorCode.FLOOR_GOODS_COUNT_TOO_FEW.meta().userMessage()),

    WX_LIVE_HOST_12(GoodsErrorCode.GOODS_IMAGE_THUMBNAIL_UNSUPPORTED.code(),
            GoodsErrorCode.GOODS_IMAGE_THUMBNAIL_UNSUPPORTED.meta().userMessage()),

    /**
     * 账号异常，请联系客服。
     *
     * <p>引用统一码 {@link UserErrorCode#USER_ACCOUNT_ABNORMAL}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link UserErrorCode#USER_ACCOUNT_ABNORMAL}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    ACCOUNT_ABNORMALITY(UserErrorCode.USER_ACCOUNT_ABNORMAL.code(), UserErrorCode.USER_ACCOUNT_ABNORMAL.meta().userMessage()),

    /**
     * 请输入正确的购物卡。
     *
     * <p>引用统一码 {@link MarketingErrorCode#MARKETING_CARD_INVALID}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link MarketingErrorCode#MARKETING_CARD_INVALID}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    CARD_IS_NUll(MarketingErrorCode.MARKETING_CARD_INVALID.code(), MarketingErrorCode.MARKETING_CARD_INVALID.meta().userMessage()),

    /**
     * 快递公司名称和首字母需小于 30 个字符。
     *
     * <p>引用统一码 {@link ValidationErrorCode#PARAM_INVALID}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link ValidationErrorCode#PARAM_INVALID}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    EXPRESS_NAME_PING_LIMIT(ValidationErrorCode.PARAM_INVALID.code(), ValidationErrorCode.PARAM_INVALID.meta().userMessage()),

    /**
     * 商品已经销。
     *
     * <p>引用统一码 {@link GoodsErrorCode#GOODS_ALREADY_SOLD_OUT}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link GoodsErrorCode#GOODS_ALREADY_SOLD_OUT}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    GOODS_DEALER(GoodsErrorCode.GOODS_ALREADY_SOLD_OUT.code(), GoodsErrorCode.GOODS_ALREADY_SOLD_OUT.meta().userMessage()),

    /**
     * 自己的商品不能操作。
     *
     * <p>引用统一码 {@link GoodsErrorCode#GOODS_SELF_OPERATION_FORBIDDEN}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link GoodsErrorCode#GOODS_SELF_OPERATION_FORBIDDEN}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    GOODS_NOT_WORKING(GoodsErrorCode.GOODS_SELF_OPERATION_FORBIDDEN.code(), GoodsErrorCode.GOODS_SELF_OPERATION_FORBIDDEN.meta().userMessage()),

    /**
     * 内部错误。
     *
     * <p>引用统一码 {@link SystemErrorCode#UNKNOWN_ERROR}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link SystemErrorCode#UNKNOWN_ERROR}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    INTERNAL_ERROR(SystemErrorCode.UNKNOWN_ERROR.code(), SystemErrorCode.UNKNOWN_ERROR.meta().userMessage()),

    /**
     * 未开启购买礼包商品。
     *
     * <p>引用统一码 {@link MarketingErrorCode#MARKETING_GIFT_GOODS_DISABLED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link MarketingErrorCode#MARKETING_GIFT_GOODS_DISABLED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    NO_OPEN_BUY_GIFT_GOODS(MarketingErrorCode.MARKETING_GIFT_GOODS_DISABLED.code(), MarketingErrorCode.MARKETING_GIFT_GOODS_DISABLED.meta().userMessage()),

    /**
     * 未申请经销商。
     *
     * <p>引用统一码 {@link MerchantErrorCode#MERCHANT_NOT_DISTRIBUTOR}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link MerchantErrorCode#MERCHANT_NOT_DISTRIBUTOR}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    NOT_APPLYING_FOR_A_DISTRIBUTOR(MerchantErrorCode.MERCHANT_NOT_DISTRIBUTOR.code(), MerchantErrorCode.MERCHANT_NOT_DISTRIBUTOR.meta().userMessage()),

    /**
     * 该订单已超时，不能支付。
     *
     * <p>引用统一码 {@link OrderErrorCode#ORDER_TIMEOUT_NOT_PAYABLE}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link OrderErrorCode#ORDER_TIMEOUT_NOT_PAYABLE}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    ORDER_HAS_TIMED_OUT_CANNOT_BE_PAID(OrderErrorCode.ORDER_TIMEOUT_NOT_PAYABLE.code(), OrderErrorCode.ORDER_TIMEOUT_NOT_PAYABLE.meta().userMessage()),

    /**
     * 商品不能重复同步。
     *
     * <p>引用统一码 {@link GoodsErrorCode#GOODS_SYNC_DUPLICATED}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link GoodsErrorCode#GOODS_SYNC_DUPLICATED}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    PRODUCT_CANNOT_BE_SYNCHRONIZED_REPEATEDLY(GoodsErrorCode.GOODS_SYNC_DUPLICATED.code(), GoodsErrorCode.GOODS_SYNC_DUPLICATED.meta().userMessage()),

    /**
     * 您不是商家用户，无法操作。
     *
     * <p>引用统一码 {@link MerchantErrorCode#MERCHANT_NOT_BUSINESS_USER}，码值与文案均由该枚举提供。
     *
     * @deprecated 请直接使用 {@link MerchantErrorCode#MERCHANT_NOT_BUSINESS_USER}；本项仅为兼容存量调用点保留，
     *             引用归零后即可删除。
     */
    @Deprecated
    USER_IS_NOT_BUSINESS(MerchantErrorCode.MERCHANT_NOT_BUSINESS_USER.code(), MerchantErrorCode.MERCHANT_NOT_BUSINESS_USER.meta().userMessage()),

    /** 历史哨兵项：原枚举用 End 标记常量列表结束，保留以兼容反射式遍历。 */
    End(999999, "-----");

    private final int code;

    private final String msg;

    BizCodeEnume(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
