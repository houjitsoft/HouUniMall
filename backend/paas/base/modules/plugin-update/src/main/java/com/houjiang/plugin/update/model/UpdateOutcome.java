package com.houjiang.plugin.update.model;

/**
 * 升级 / 回滚结果（{@code /base/plugin-center/update/upgrade} 与 {@code /rollback} 的 data）。
 *
 * <p><b>为什么要单独一个模型而不复用 {@link InstallResult}</b>：升级有一个安装没有的状态——
 * {@code rolledBack}。升级失败后自动回滚成功时，接口返回的是<b>成功</b>（HTTP 语义上这次操作
 * 没有把系统留在坏状态），但业务上必须让前端知道「升级没成，已退回 X 版本」，
 * 否则用户会以为升级成功了。用 {@code InstallResult} 表达不出这层信息，
 * 硬塞进 {@code state} 又会让状态枚举膨胀成业务判断的垃圾桶。
 *
 * @author Amos
 * @since M4
 */
public final class UpdateOutcome {

    private final String taskId;

    private final String pluginCode;

    private final String opType;

    private final String fromVersion;

    private final String toVersion;

    /**
     * 最终停留在的版本（回滚成功时等于 {@code fromVersion}）。
     */
    private final String currentVersion;

    private final String state;

    private final String step;

    private final boolean rolledBack;

    private final String errorCode;

    private final String errorMsg;

    /**
     * 构造升级 / 回滚结果。
     *
     * @param taskId         任务号
     * @param pluginCode     插件编码
     * @param opType         操作类型（UPGRADE / ROLLBACK）
     * @param fromVersion    起始版本
     * @param toVersion      目标版本
     * @param currentVersion 最终停留版本
     * @param state          任务终态
     * @param step           最后停留的阶段
     * @param rolledBack     是否发生了回滚
     * @param errorCode      错误码，成功为 null
     * @param errorMsg       错误描述，成功为 null
     */
    public UpdateOutcome(String taskId,
                         String pluginCode,
                         String opType,
                         String fromVersion,
                         String toVersion,
                         String currentVersion,
                         String state,
                         String step,
                         boolean rolledBack,
                         String errorCode,
                         String errorMsg) {
        this.taskId = taskId;
        this.pluginCode = pluginCode;
        this.opType = opType;
        this.fromVersion = fromVersion;
        this.toVersion = toVersion;
        this.currentVersion = currentVersion;
        this.state = state;
        this.step = step;
        this.rolledBack = rolledBack;
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    /**
     * @return 任务号
     */
    public String getTaskId() {
        return taskId;
    }

    /**
     * @return 插件编码
     */
    public String getPluginCode() {
        return pluginCode;
    }

    /**
     * @return 操作类型
     */
    public String getOpType() {
        return opType;
    }

    /**
     * @return 起始版本
     */
    public String getFromVersion() {
        return fromVersion;
    }

    /**
     * @return 目标版本
     */
    public String getToVersion() {
        return toVersion;
    }

    /**
     * @return 最终停留版本
     */
    public String getCurrentVersion() {
        return currentVersion;
    }

    /**
     * @return 任务终态
     */
    public String getState() {
        return state;
    }

    /**
     * @return 最后停留的阶段
     */
    public String getStep() {
        return step;
    }

    /**
     * @return 是否发生了回滚
     */
    public boolean isRolledBack() {
        return rolledBack;
    }

    /**
     * @return 错误码
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * @return 错误描述
     */
    public String getErrorMsg() {
        return errorMsg;
    }
}
