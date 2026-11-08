package com.houjiang.plugin.update;

import java.util.Locale;

/**
 * 升级任务状态，对应 {@code hou_plugin_update_task.state}。
 *
 * <p>互斥键是 {@code uk_active(active_key)}：{@code active_key} 为生成列，
 * 非终态（PENDING / RUNNING）时等于 {@code instance_id|plugin_code|op_type|to_version}、
 * 终态为 NULL。因此「同一组合的非终态记录只能有一条」（并发互斥的数据库层保证），
 * 而「同一组合的终态记录可以有多条」（历史可追溯）。
 * 早期方案把 state 直接放进普通唯一键 {@code uk_idem}，语义恰好相反——
 * 终态只能留一条，已在 {@code plugin_artifact_tables.sql 第六段}修正。
 *
 * @author Amos
 * @since M0
 */
public enum UpdateTaskState {

    /**
     * 已建单，等待执行。
     */
    PENDING("PENDING", "等待中", false),

    /**
     * 执行中。
     */
    RUNNING("RUNNING", "执行中", false),

    /**
     * 执行成功。
     */
    SUCCESS("SUCCESS", "成功", true),

    /**
     * 已回滚（升级失败后回退到原版本）。
     */
    ROLLED_BACK("ROLLED_BACK", "已回滚", true),

    /**
     * 执行失败且未能自动回滚。
     */
    FAILED("FAILED", "失败", true);

    /**
     * 入库值。
     */
    private final String code;

    /**
     * 中文描述。
     */
    private final String description;

    /**
     * 是否为终态。
     */
    private final boolean terminal;

    UpdateTaskState(String code, String description, boolean terminal) {
        this.code = code;
        this.description = description;
        this.terminal = terminal;
    }

    /**
     * 按入库值解析枚举，大小写不敏感。
     *
     * @param code 入库值
     * @return 枚举实例
     * @throws IllegalArgumentException 未知状态时抛出
     */
    public static UpdateTaskState fromCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("任务状态不能为空");
        }
        String normalized = code.trim().toUpperCase(Locale.ROOT);
        for (UpdateTaskState state : values()) {
            if (state.code.equals(normalized)) {
                return state;
            }
        }
        throw new IllegalArgumentException("未知的任务状态：" + code);
    }

    /**
     * @return 入库值
     */
    public String getCode() {
        return code;
    }

    /**
     * @return 中文描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return 是否为终态（终态任务不再占用并发互斥锁）
     */
    public boolean isTerminal() {
        return terminal;
    }

    /**
     * @return 是否为进行中的非终态（用于错误码 3016 TASK_IN_PROGRESS 判定）
     */
    public boolean isActive() {
        return !terminal;
    }
}
