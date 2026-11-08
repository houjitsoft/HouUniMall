package com.houjiang.commons.errorcode.domain;

import com.houjiang.commons.errorcode.ErrorCategory;
import com.houjiang.commons.errorcode.ErrorCodeDef;
import com.houjiang.commons.errorcode.ErrorLevel;
import com.houjiang.commons.errorcode.ErrorMeta;
import com.houjiang.commons.errorcode.HttpSemantic;
import com.houjiang.commons.errorcode.UiAction;

/**
 * PLUGIN_PLATFORM（域 70）插件平台错误码，号段 70001-70999。
 *
 * <p>解决的问题：插件分发已有规范的错误码定义（3001-3019，见 PluginDistErrorCode），
 * 但插件<b>运行时</b>（清单校验、引擎加载、扩展点调用）没有码，失败只能抛裸异常。
 * 本域补齐运行时段，与分发段（3001-3019，本轮不动）分工：
 * 3001-3019 = 分发与下载；70001-70999 = 注册、清单、引擎、扩展点。
 *
 * @author Amos
 * @since v1.2.0
 */
public enum PluginPlatformErrorCode implements ErrorCodeDef {

    /**
     * 插件不存在（未安装或已卸载）。
     *
     * <p>触发：按插件 ID 查不到注册信息。用户看到「插件不存在或已卸载」。
     *
     * <p>排查：确认插件 ID 与 plugin-registry 中的注册记录。
     */
    PLUGIN_NOT_FOUND(70001, ErrorMeta.builder(70001, "插件不存在或已卸载")
            .devMessage("按插件 ID 未查到注册记录")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.EMPTY_STATE).category(ErrorCategory.NOT_FOUND)
            .httpSemantic(HttpSemantic.NOT_FOUND_404)
            .exposeToUser(true)
            .since("v1.2.0")
            .build()),

    /**
     * 插件参数不合法（版本号格式错误、配置缺失）。
     *
     * <p>触发：插件操作参数校验失败。用户看到「插件参数有误，请检查」。
     *
     * <p>排查：确认版本号符合语义化版本规范、必填配置项齐全。
     */
    PLUGIN_PARAM_INVALID(70002, ErrorMeta.builder(70002, "插件参数有误，请检查")
            .devMessage("插件操作参数校验失败")
            .level(ErrorLevel.INFO).retryable(true)
            .uiAction(UiAction.INLINE).category(ErrorCategory.VALIDATION)
            .httpSemantic(HttpSemantic.BAD_REQUEST_400)
            .exposeToUser(true)
            .since("v1.2.0")
            .build()),

    /**
     * 插件状态不允许（运行中的插件不允许卸载）。
     *
     * <p>触发：对当前插件状态执行不允许的操作。用户看到「插件当前状态不允许该操作」。
     *
     * <p>排查：确认插件状态机；运行中需先停用。
     */
    PLUGIN_STATE_NOT_ALLOWED(70003, ErrorMeta.builder(70003, "插件当前状态不允许该操作")
            .devMessage("插件状态机流转非法")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.BIZ)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .since("v1.2.0")
            .build()),

    /**
     * 插件已安装（重复安装同一插件同一版本）。
     *
     * <p>触发：安装时发现已存在。用户看到「该插件已安装，无需重复安装」。
     *
     * <p>排查：如需重装请先卸载；升级走 plugin-update 流程。
     */
    PLUGIN_ALREADY_INSTALLED(70004, ErrorMeta.builder(70004, "该插件已安装，无需重复安装")
            .devMessage("同插件同版本已存在注册记录")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.CONFLICT)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .since("v1.2.0")
            .build()),

    /**
     * 插件操作无权限（非租户管理员尝试安装/卸载）。
     *
     * <p>触发：插件管理操作鉴权失败。用户看到「你没有管理插件的权限」。
     *
     * <p>排查：确认操作者是否具备平台管理员角色。
     */
    PLUGIN_ACCESS_DENIED(70005, ErrorMeta.builder(70005, "你没有管理插件的权限")
            .devMessage("插件管理操作鉴权未通过")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.PERMISSION)
            .httpSemantic(HttpSemantic.FORBIDDEN_403)
            .exposeToUser(true)
            .since("v1.2.0")
            .build()),

    /**
     * 插件扩展点调用失败。
     *
     * <p>触发：宿主调用插件扩展点抛异常。用户看到「插件功能调用失败，请稍后重试」。
     *
     * <p>排查：插件内部异常只在日志里；确认插件版本与宿主 SDK 兼容。
     */
    PLUGIN_RUNTIME_CALL_FAILED(70008, ErrorMeta.builder(70008, "插件功能调用失败，请稍后重试")
            .devMessage("扩展点调用抛出异常，插件内部堆栈见服务端日志")
            .level(ErrorLevel.ERROR).retryable(true)
            .uiAction(UiAction.TOAST).category(ErrorCategory.UPSTREAM)
            .httpSemantic(HttpSemantic.INTERNAL_500)
            .exposeToUser(true)
            .troubleshooting("1. 从日志定位插件内部堆栈；2. 确认插件编译所依赖的 plugin-sdk 版本与宿主一致。")
            .since("v1.2.0")
            .build()),

    /**
     * 插件配置缺失（缺少必需的租户配置）。
     *
     * <p>触发：启用插件前发现必需配置为空。用户看到「插件尚未完成配置」。
     *
     * <p>排查：引导用户到插件配置页补齐必填项。
     */
    PLUGIN_CONFIG_MISSING(70009, ErrorMeta.builder(70009, "插件尚未完成配置，请先配置")
            .devMessage("插件必需配置项为空")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.DIALOG).category(ErrorCategory.SYSTEM)
            .httpSemantic(HttpSemantic.INTERNAL_500)
            .exposeToUser(true)
            .since("v1.2.0")
            .build()),

    /**
     * 插件清单非法（manifest 字段缺失或不合法）。
     *
     * <p>触发：解析插件清单校验失败。用户看到「插件包格式不正确」。
     *
     * <p>排查：确认清单必填字段（id / version / mainClass / sdkVersion）齐全。
     */
    PLUGIN_MANIFEST_INVALID(70010, ErrorMeta.builder(70010, "插件包格式不正确，请重新获取")
            .devMessage("插件清单校验失败，缺失必填字段或格式非法")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.VALIDATION)
            .httpSemantic(HttpSemantic.BAD_REQUEST_400)
            .exposeToUser(true)
            .troubleshooting("1. 检查清单必填字段：id / version / mainClass / sdkVersion；2. 确认打包方式正确。")
            .since("v1.2.0")
            .build()),

    /**
     * 插件引擎加载失败（类加载、依赖冲突）。
     *
     * <p>触发：PF4J 加载插件失败。用户看到「插件加载失败，请联系管理员」。
     *
     * <p>排查：多为依赖冲突或 JDK 版本不匹配。FATAL 级别需人工介入。
     */
    PLUGIN_ENGINE_LOAD_FAILED(70011, ErrorMeta.builder(70011, "插件加载失败，请联系管理员")
            .devMessage("PF4J 插件加载失败，类加载异常见服务端日志")
            .level(ErrorLevel.FATAL).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.SYSTEM)
            .httpSemantic(HttpSemantic.INTERNAL_500)
            .exposeToUser(true)
            .troubleshooting("1. 从日志取类加载异常；2. 排查依赖冲突；3. 确认插件编译目标与宿主 JDK 一致。")
            .since("v1.2.0")
            .build()),
    /**
     * 扩展业务中断，请检查扩展配置。
     *
     * <p>触发/排查：二开扩展点主动中断流程。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    PLUGIN_EXTENSION_ABORTED(70012, ErrorMeta.builder(70012, "扩展业务中断，请检查扩展配置")
            .devMessage("二开扩展点主动中断流程")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.BIZ)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(7001)
            .since("v1.3.0")
            .build()),

    /**
     * 扩展执行故障，请联系管理员。
     *
     * <p>触发/排查：二开扩展点执行异常。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    PLUGIN_EXTENSION_FAULT(70013, ErrorMeta.builder(70013, "扩展执行故障，请联系管理员")
            .devMessage("二开扩展点执行异常")
            .level(ErrorLevel.ERROR).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.SYSTEM)
            .httpSemantic(HttpSemantic.INTERNAL_500)
            .exposeToUser(true)
            .legacyCodes(7002)
            .since("v1.3.0")
            .build()),

    ;

    private final int code;
    private final ErrorMeta meta;

    PluginPlatformErrorCode(int code, ErrorMeta meta) {
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
