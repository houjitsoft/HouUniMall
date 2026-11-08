package com.houjiang.plugin.update;

import java.util.Locale;

/**
 * 升级任务操作类型，对应 {@code hou_plugin_update_task.op_type}。
 *
 * @author Amos
 * @since M0
 */
public enum UpdateOpType {

    /**
     * 全新安装。
     */
    INSTALL("INSTALL", "安装"),

    /**
     * 版本升级。
     */
    UPGRADE("UPGRADE", "升级"),

    /**
     * 回滚到历史版本（履约动作，即使插件已下架或授权失效也必须允许）。
     */
    ROLLBACK("ROLLBACK", "回滚"),

    /**
     * 卸载插件。
     *
     * <p>与「直接把安装状态位置 0」的旧做法的区别：卸载是一次<b>带任务流水与补偿</b>的编排，
     * 会走排空窗口、释放 ClassLoader、关闭插件子上下文、回收版本目录、清除迁移审计，
     * 全过程在 {@code hou_plugin_update_task} 留痕，失败可查、可重试。
     */
    UNINSTALL("UNINSTALL", "卸载");

    /**
     * 入库值。
     */
    private final String code;

    /**
     * 中文描述。
     */
    private final String description;

    UpdateOpType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 按入库值解析枚举，大小写不敏感。
     *
     * @param code 入库值
     * @return 枚举实例
     * @throws IllegalArgumentException 未知操作类型时抛出
     */
    public static UpdateOpType fromCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("操作类型不能为空");
        }
        String normalized = code.trim().toUpperCase(Locale.ROOT);
        for (UpdateOpType type : values()) {
            if (type.code.equals(normalized)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的操作类型：" + code);
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
}
