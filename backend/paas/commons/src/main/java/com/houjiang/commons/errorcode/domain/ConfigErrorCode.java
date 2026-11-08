package com.houjiang.commons.errorcode.domain;

import com.houjiang.commons.errorcode.ErrorCategory;
import com.houjiang.commons.errorcode.ErrorCodeDef;
import com.houjiang.commons.errorcode.ErrorLevel;
import com.houjiang.commons.errorcode.ErrorMeta;
import com.houjiang.commons.errorcode.HttpSemantic;
import com.houjiang.commons.errorcode.UiAction;

/**
 * CONFIG（域 27）配置与字典错误码，号段 27001-27999。
 *
 * <p>解决的问题：配置缺失过去表现为「空指针」或一句不明所以的「操作失败」，
 * 排查时只能翻代码。本域明确区分「配置项不存在 / 字典未初始化 / 更新失败」三种情况。
 *
 * @author Amos
 * @since v1.2.0
 */
public enum ConfigErrorCode implements ErrorCodeDef {

    /**
     * 配置项不存在。
     *
     * <p>触发：按 key 查不到配置。用户看到「配置项不存在，请联系管理员」。
     *
     * <p>排查：确认配置 key 拼写与该环境的配置是否已下发。
     */
    CONFIG_ITEM_NOT_FOUND(27001, ErrorMeta.builder(27001, "配置项不存在，请联系管理员")
            .devMessage("按配置 key 未查到记录")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.NOT_FOUND)
            .httpSemantic(HttpSemantic.NOT_FOUND_404)
            .exposeToUser(true)
            .since("v1.2.0")
            .build()),

    /**
     * 配置状态不允许（只读配置被改、已发布配置被删）。
     *
     * <p>触发：对配置做了当前状态不允许的操作。用户看到「当前配置状态不允许该操作」。
     *
     * <p>排查：确认配置生命周期状态。
     */
    CONFIG_STATE_NOT_ALLOWED(27003, ErrorMeta.builder(27003, "当前配置状态不允许该操作")
            .devMessage("配置生命周期状态与操作不匹配")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.BIZ)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .since("v1.2.0")
            .build()),

    /**
     * 配置项重复（同一 key 已存在）。
     *
     * <p>触发：新增配置时 key 冲突。用户看到「该配置项已存在，请勿重复添加」。
     *
     * <p>排查：确认是否应改为更新而不是新增。
     */
    CONFIG_DUPLICATE_KEY(27004, ErrorMeta.builder(27004, "该配置项已存在，请勿重复添加")
            .devMessage("配置 key 唯一约束冲突")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.INLINE).category(ErrorCategory.CONFLICT)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(10021)
            .since("v1.2.0")
            .build()),

    /**
     * 配置未初始化（字典、缓存未加载）。
     *
     * <p>触发：应用启动后配置缓存为空。用户看到「系统尚未初始化完成，请稍后重试」。
     *
     * <p>排查：确认 base 模块的配置加载任务是否成功执行。FATAL 级别需人工介入。
     */
    CONFIG_NOT_INITIALIZED(27009, ErrorMeta.builder(27009, "系统尚未初始化完成，请稍后重试")
            .devMessage("配置/字典缓存为空，初始化未完成")
            .level(ErrorLevel.FATAL).retryable(true)
            .uiAction(UiAction.TOAST).category(ErrorCategory.SYSTEM)
            .httpSemantic(HttpSemantic.INTERNAL_500)
            .exposeToUser(true)
            .troubleshooting("1. 检查配置加载任务是否成功；2. 确认 Nacos 连通；3. 必要时重启服务触发重载。")
            .since("v1.2.0")
            .build()),

    /**
     * 数据字典项不存在。
     *
     * <p>触发：按字典类型+编码查不到枚举值。用户看到「字典数据不存在，请联系管理员」。
     *
     * <p>排查：确认字典类型与编码是否已维护。
     */
    CONFIG_DICT_NOT_FOUND(27010, ErrorMeta.builder(27010, "字典数据不存在，请联系管理员")
            .devMessage("按字典类型+编码未查到枚举值")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.NOT_FOUND)
            .httpSemantic(HttpSemantic.NOT_FOUND_404)
            .exposeToUser(true)
            .since("v1.2.0")
            .build()),

    /**
     * 配置更新失败（写入失败或刷新缓存失败）。
     *
     * <p>触发：保存配置后写库或刷新缓存失败。用户看到「配置保存失败，请稍后重试」。
     *
     * <p>排查：确认数据库可写、缓存可刷新；写库成功但缓存刷新失败需手动触发重载。
     */
    CONFIG_UPDATE_FAILED(27011, ErrorMeta.builder(27011, "配置保存失败，请稍后重试")
            .devMessage("配置写入或缓存刷新失败")
            .level(ErrorLevel.ERROR).retryable(true)
            .uiAction(UiAction.TOAST).category(ErrorCategory.SYSTEM)
            .httpSemantic(HttpSemantic.INTERNAL_500)
            .exposeToUser(true)
            .troubleshooting("1. 确认数据库可写；2. 确认缓存可刷新；3. 写库成功但缓存失败时手动触发重载。")
            .since("v1.2.0")
            .build()),
    /**
     * 配置参数不完整，请检查。
     *
     * <p>触发/排查：配置项必填参数缺失。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    CONFIG_PARAM_INVALID(27002, ErrorMeta.builder(27002, "配置参数不完整，请检查")
            .devMessage("配置项必填参数缺失")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.SYSTEM)
            .httpSemantic(HttpSemantic.BAD_REQUEST_400)
            .exposeToUser(true)
            .legacyCodes(10027)
            .since("v1.3.0")
            .build()),

    ;

    private final int code;
    private final ErrorMeta meta;

    ConfigErrorCode(int code, ErrorMeta meta) {
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
