package com.houjiang.plugin.update.model;

import com.houjiang.mall.entity.update.PluginUpdateTask;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 任务状态视图（{@code /base/plugin-center/update/task/status} 与 {@code /history} 的条目）。
 *
 * <p>时间统一格式化成 {@code yyyy-MM-dd HH:mm:ss} 字符串：
 * 直接把 {@code LocalDateTime} 交给 Jackson 会受全局配置影响（可能输出数组），
 * 前端拿到的是不可控格式；这里固定成可读字符串，前端无需二次处理。
 *
 * @author Amos
 * @since M3
 */
public final class TaskStatus {

    /**
     * 时间格式。
     */
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final String taskId;

    private final String pluginCode;

    private final String opType;

    private final String fromVersion;

    private final String toVersion;

    private final String state;

    private final String step;

    private final String errorCode;

    private final String errorMsg;

    private final String startTime;

    private final String endTime;

    /**
     * 由任务实体构造状态视图。
     *
     * @param task 任务实体
     */
    public TaskStatus(PluginUpdateTask task) {
        this.taskId = task.getTaskId();
        this.pluginCode = task.getPluginCode();
        this.opType = task.getOpType();
        this.fromVersion = task.getFromVersion();
        this.toVersion = task.getToVersion();
        this.state = task.getState();
        this.step = task.getStep();
        this.errorCode = task.getErrorCode();
        this.errorMsg = task.getErrorMsg();
        this.startTime = format(task.getStartTime());
        this.endTime = format(task.getEndTime());
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
     * @return 来源版本
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
     * @return 任务状态
     */
    public String getState() {
        return state;
    }

    /**
     * @return 当前阶段
     */
    public String getStep() {
        return step;
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

    /**
     * @return 开始时间字符串
     */
    public String getStartTime() {
        return startTime;
    }

    /**
     * @return 结束时间字符串
     */
    public String getEndTime() {
        return endTime;
    }

    /**
     * 格式化时间，空值返回 null（不返回空串，避免前端误判为「有值但为空」）。
     *
     * @param time 时间
     * @return 格式化结果
     */
    private static String format(LocalDateTime time) {
        return time == null ? null : time.format(FORMATTER);
    }
}
