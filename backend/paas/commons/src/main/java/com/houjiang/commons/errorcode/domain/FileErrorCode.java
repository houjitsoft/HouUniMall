package com.houjiang.commons.errorcode.domain;

import com.houjiang.commons.errorcode.ErrorCategory;
import com.houjiang.commons.errorcode.ErrorCodeDef;
import com.houjiang.commons.errorcode.ErrorLevel;
import com.houjiang.commons.errorcode.ErrorMeta;
import com.houjiang.commons.errorcode.HttpSemantic;
import com.houjiang.commons.errorcode.UiAction;

/**
 * FILE（域 24）文件与存储错误码，号段 24001-24999。
 *
 * <p>解决的问题：上传/下载失败的提示散落在各处，且失败时常常把存储原始异常
 * （含内部路径与 AK 信息）直接回显。本域统一收口，前端按 Toast / 行内提示处理。
 *
 * @author Amos
 * @since v1.2.0
 */
public enum FileErrorCode implements ErrorCodeDef {

    /**
     * 文件不存在（已删除或路径错误）。
     *
     * <p>触发：按 fileKey 查不到对象。用户看到「文件不存在或已被删除」。
     *
     * <p>排查：确认 fileKey 是否正确、对象是否被生命周期规则清理。
     */
    FILE_NOT_FOUND(24001, ErrorMeta.builder(24001, "文件不存在或已被删除")
            .devMessage("按 fileKey 未查到对象")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.NOT_FOUND)
            .httpSemantic(HttpSemantic.NOT_FOUND_404)
            .exposeToUser(true)
            .legacyCodes(11070)
            .since("v1.2.0")
            .build()),

    /**
     * 文件类型不允许（扩展名/ContentType 不在白名单）。
     *
     * <p>触发：上传时校验扩展名未通过。用户看到「不支持的文件类型，请更换」。
     *
     * <p>排查：确认白名单配置与前端 accept 属性一致。
     */
    FILE_TYPE_NOT_ALLOWED(24002, ErrorMeta.builder(24002, "不支持的文件类型，请更换")
            .devMessage("上传文件扩展名或 ContentType 不在白名单")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.INLINE).category(ErrorCategory.VALIDATION)
            .httpSemantic(HttpSemantic.BAD_REQUEST_400)
            .exposeToUser(true)
            .legacyCodes(10030, 10031, 11071)
            .since("v1.2.0")
            .build()),

    /**
     * 文件超出大小上限。
     *
     * <p>触发：上传文件超过配置的上限。用户看到「文件过大，请压缩后重试」。
     *
     * <p>排查：确认网关/应用/存储三层的大小限制是否一致。
     */
    FILE_SIZE_EXCEEDED(24006, ErrorMeta.builder(24006, "文件过大，请压缩后重试")
            .devMessage("上传文件超过配置的大小上限")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.INLINE).category(ErrorCategory.VALIDATION)
            .httpSemantic(HttpSemantic.BAD_REQUEST_400)
            .exposeToUser(true)
            .legacyCodes(10088)
            .since("v1.2.0")
            .build()),

    /**
     * 存储未配置（Bucket、Endpoint、AK 缺失）。
     *
     * <p>触发：发起存储操作前发现配置为空。用户看到「文件服务未开通，请联系管理员」。
     *
     * <p>排查：补配置后重启服务。FATAL 级别需人工介入。
     */
    FILE_STORAGE_NOT_CONFIGURED(24009, ErrorMeta.builder(24009, "文件服务未开通，请联系管理员")
            .devMessage("存储配置项为空（Bucket / Endpoint / 凭据）")
            .level(ErrorLevel.FATAL).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.SYSTEM)
            .httpSemantic(HttpSemantic.INTERNAL_500)
            .exposeToUser(true)
            .troubleshooting("1. 检查文件存储配置项是否下发；2. 补配置后重启服务；3. 确认 Bucket 可连通。")
            .since("v1.2.0")
            .build()),

    /**
     * 上传失败（通用）。
     *
     * <p>触发：写入存储失败。用户看到「文件上传失败，请稍后重试」。
     *
     * <p>排查：存储原始异常只进日志，注意检查磁盘空间与网络。
     */
    FILE_UPLOAD_FAILED(24010, ErrorMeta.builder(24010, "文件上传失败，请稍后重试")
            .devMessage("写入存储失败，原始异常见服务端日志")
            .level(ErrorLevel.ERROR).retryable(true)
            .uiAction(UiAction.TOAST).category(ErrorCategory.UPSTREAM)
            .httpSemantic(HttpSemantic.INTERNAL_500)
            .exposeToUser(true)
            .troubleshooting("1. 查看存储原始异常；2. 检查磁盘/配额；3. 确认网络出口。")
            .legacyCodes(10010)
            .since("v1.2.0")
            .build()),

    /**
     * 下载失败（通用）。
     *
     * <p>触发：读取对象失败。用户看到「文件下载失败，请稍后重试」。
     *
     * <p>排查：确认对象存在且有读权限。
     */
    FILE_DOWNLOAD_FAILED(24011, ErrorMeta.builder(24011, "文件下载失败，请稍后重试")
            .devMessage("读取对象失败，原始异常见服务端日志")
            .level(ErrorLevel.ERROR).retryable(true)
            .uiAction(UiAction.TOAST).category(ErrorCategory.UPSTREAM)
            .httpSemantic(HttpSemantic.INTERNAL_500)
            .exposeToUser(true)
            .since("v1.2.0")
            .build()),

    /**
     * 删除失败（通用）。
     *
     * <p>触发：删除对象失败。用户看到「文件删除失败，请稍后重试」。
     *
     * <p>排查：确认是否有引用该文件的业务记录未清理。
     */
    FILE_DELETE_FAILED(24012, ErrorMeta.builder(24012, "文件删除失败，请稍后重试")
            .devMessage("删除对象失败，原始异常见服务端日志")
            .level(ErrorLevel.ERROR).retryable(true)
            .uiAction(UiAction.TOAST).category(ErrorCategory.SYSTEM)
            .httpSemantic(HttpSemantic.INTERNAL_500)
            .exposeToUser(true)
            .since("v1.2.0")
            .build()),

    /**
     * 生成访问签名 URL 失败。
     *
     * <p>触发：临时签名生成失败。用户看到「文件链接生成失败，请重试」。
     *
     * <p>排查：确认凭据有效、时钟同步；签名信息不得出现在日志外的任何地方。
     */
    FILE_SIGN_URL_FAILED(24013, ErrorMeta.builder(24013, "文件链接生成失败，请重试")
            .devMessage("生成临时访问签名失败，签名信息不出日志")
            .level(ErrorLevel.ERROR).retryable(true)
            .uiAction(UiAction.TOAST).category(ErrorCategory.SYSTEM)
            .httpSemantic(HttpSemantic.INTERNAL_500)
            .exposeToUser(true)
            .troubleshooting("1. 确认存储凭据有效；2. 检查服务器时钟是否同步；3. 查看原始异常。")
            .since("v1.2.0")
            .build()),
    /**
     * 请先选择文件后重试。
     *
     * <p>触发/排查：必填文件/图片未上传。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    FILE_NOT_PROVIDED(24014, ErrorMeta.builder(24014, "请先选择文件后重试")
            .devMessage("必填文件/图片未上传")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.INLINE).category(ErrorCategory.VALIDATION)
            .httpSemantic(HttpSemantic.BAD_REQUEST_400)
            .exposeToUser(true)
            .legacyCodes(10033, 18015)
            .since("v1.3.0")
            .build()),

    /**
     * 存储空间已满，请清理后重试。
     *
     * <p>触发/排查：对象存储配额不足。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    FILE_STORAGE_QUOTA_EXCEEDED(24015, ErrorMeta.builder(24015, "存储空间已满，请清理后重试")
            .devMessage("对象存储配额不足")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.BIZ)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(16048)
            .since("v1.3.0")
            .build()),

    ;

    private final int code;
    private final ErrorMeta meta;

    FileErrorCode(int code, ErrorMeta meta) {
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
