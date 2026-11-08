package com.houjiang.commons.errorcode.domain;

import com.houjiang.commons.errorcode.ErrorCategory;
import com.houjiang.commons.errorcode.ErrorCodeDef;
import com.houjiang.commons.errorcode.ErrorLevel;
import com.houjiang.commons.errorcode.ErrorMeta;
import com.houjiang.commons.errorcode.HttpSemantic;
import com.houjiang.commons.errorcode.UiAction;

/**
 * JOB（域 28）任务与异步错误码，号段 28001-28999。
 *
 * <p>解决的问题：定时任务/异步任务失败过去只在日志里，前端触发任务后拿不到明确反馈，
 * 用户只能盲等。本域把「任务已存在 / 正在跑 / 执行失败 / 未配置」登记成可判定码。
 *
 * @author Amos
 * @since v1.2.0
 */
public enum JobErrorCode implements ErrorCodeDef {

    /**
     * 任务不存在（任务 ID 无效或已删除）。
     *
     * <p>触发：按任务 ID 查不到定义。用户看到「任务不存在或已被删除」。
     *
     * <p>排查：确认任务 ID 与 job 模块的任务注册表。
     */
    JOB_NOT_FOUND(28001, ErrorMeta.builder(28001, "任务不存在或已被删除")
            .devMessage("按任务 ID 未查到任务定义")
            .level(ErrorLevel.INFO).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.NOT_FOUND)
            .httpSemantic(HttpSemantic.NOT_FOUND_404)
            .exposeToUser(true)
            .since("v1.2.0")
            .build()),

    /**
     * 任务状态不允许（已停止的任务被启动等）。
     *
     * <p>触发：对任务执行了当前状态不允许的操作。用户看到「任务状态不允许该操作」。
     *
     * <p>排查：确认任务状态机流转是否合法。
     */
    JOB_STATE_NOT_ALLOWED(28003, ErrorMeta.builder(28003, "任务状态不允许该操作")
            .devMessage("任务状态机流转非法")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.BIZ)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .legacyCodes(10085)
            .since("v1.2.0")
            .build()),

    /**
     * 任务正在执行中（重复触发）。
     *
     * <p>触发：同一任务已有运行中的实例。用户看到「任务正在执行，请勿重复触发」。
     *
     * <p>排查：这是保护性拦截；如需并发执行，确认任务是否支持。
     */
    JOB_ALREADY_RUNNING(28004, ErrorMeta.builder(28004, "任务正在执行，请勿重复触发")
            .devMessage("同一任务已有运行中的实例")
            .level(ErrorLevel.WARN).retryable(true)
            .uiAction(UiAction.TOAST).category(ErrorCategory.CONFLICT)
            .httpSemantic(HttpSemantic.CONFLICT_409)
            .exposeToUser(true)
            .since("v1.2.0")
            .build()),

    /**
     * 任务执行失败。
     *
     * <p>触发：任务运行体抛异常。用户看到「任务执行失败，请查看执行记录」。
     *
     * <p>排查：查 job 执行记录表与服务端日志；可安全重试。
     */
    JOB_EXECUTE_FAILED(28008, ErrorMeta.builder(28008, "任务执行失败，请查看执行记录")
            .devMessage("任务运行体抛出异常，堆栈见服务端日志")
            .level(ErrorLevel.ERROR).retryable(true)
            .uiAction(UiAction.TOAST).category(ErrorCategory.SYSTEM)
            .httpSemantic(HttpSemantic.INTERNAL_500)
            .exposeToUser(true)
            .troubleshooting("1. 查 job 执行记录表；2. 按 traceId 检索服务端日志；3. 修复后可手工重跑。")
            .since("v1.2.0")
            .build()),

    /**
     * 任务未配置（缺失必要的任务参数）。
     *
     * <p>触发：任务缺少执行所需配置。用户看到「任务未配置，请联系管理员」。
     *
     * <p>排查：补齐任务参数后重试。
     */
    JOB_NOT_CONFIGURED(28009, ErrorMeta.builder(28009, "任务未配置，请联系管理员")
            .devMessage("任务缺少执行所需的必要参数")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.SYSTEM)
            .httpSemantic(HttpSemantic.INTERNAL_500)
            .exposeToUser(true)
            .since("v1.2.0")
            .build()),

    /**
     * 任务触发失败（调度器未响应）。
     *
     * <p>触发：向调度器下发触发指令失败。用户看到「任务触发失败，请稍后重试」。
     *
     * <p>排查：确认调度服务健康；可重试。
     */
    JOB_TRIGGER_FAILED(28010, ErrorMeta.builder(28010, "任务触发失败，请稍后重试")
            .devMessage("调度器下发触发指令失败")
            .level(ErrorLevel.ERROR).retryable(true)
            .uiAction(UiAction.SILENT_RETRY).category(ErrorCategory.UPSTREAM)
            .httpSemantic(HttpSemantic.UNAVAILABLE_503)
            .exposeToUser(true)
            .since("v1.2.0")
            .build()),

    /**
     * 任务执行超时。
     *
     * <p>触发：任务运行超过最大允许时长。用户看到「任务执行超时，请稍后查看结果」。
     *
     * <p>排查：拆分大任务或调大超时阈值。
     */
    JOB_TIMEOUT(28011, ErrorMeta.builder(28011, "任务执行超时，请稍后查看结果")
            .devMessage("任务运行超过最大允许时长被中断")
            .level(ErrorLevel.ERROR).retryable(true)
            .uiAction(UiAction.TOAST).category(ErrorCategory.SYSTEM)
            .httpSemantic(HttpSemantic.INTERNAL_500)
            .exposeToUser(true)
            .troubleshooting("1. 确认任务是否需要拆分；2. 评估调大超时阈值；3. 检查下游依赖是否拖慢。")
            .since("v1.2.0")
            .build()),
    /**
     * 任务参数不完整，请检查。
     *
     * <p>触发/排查：定时任务入参缺失或非法。
     *
     * <p>第二阶段重构（旧枚举改为引用统一码）新增；业务新代码请直接使用本枚举，
     * 不要再从 BizCodeEnume / RiderCodeEnume / GlobalExceptionEnum 取码值。
     */
    JOB_PARAM_INVALID(28002, ErrorMeta.builder(28002, "任务参数不完整，请检查")
            .devMessage("定时任务入参缺失或非法")
            .level(ErrorLevel.WARN).retryable(false)
            .uiAction(UiAction.TOAST).category(ErrorCategory.SYSTEM)
            .httpSemantic(HttpSemantic.BAD_REQUEST_400)
            .exposeToUser(true)
            .legacyCodes(10077, 10078, 10079, 10080, 10081, 10082, 10083)
            .since("v1.3.0")
            .build()),

    ;

    private final int code;
    private final ErrorMeta meta;

    JobErrorCode(int code, ErrorMeta meta) {
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
