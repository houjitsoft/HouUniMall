package com.houjiang.plugin.update;

import java.util.Locale;

/**
 * 升级任务执行阶段，对应 {@code hou_plugin_update_task.step}。
 *
 * <p>顺序即流水线顺序：失败重启后从记录的 step 续做，不必从下载重新开始；
 * 但 {@code BACKUP} 之后的阶段失败必须走 {@code ROLLBACK}，因为旧版本已被停掉。
 *
 * @author Amos
 * @since M0
 */
public enum UpdateStep {

    /**
     * 下载制品。
     */
    DOWNLOAD("DOWNLOAD", "下载制品"),

    /**
     * 校验制品（md5 / sha256 / 签名）。
     */
    VERIFY("VERIFY", "校验制品"),

    /**
     * 兼容性预检（框架版本、依赖插件）。
     */
    COMPAT("COMPAT", "兼容性预检"),

    /**
     * 备份当前版本目录，作为回滚锚点。
     */
    BACKUP("BACKUP", "备份当前版本"),

    /**
     * 停止旧版本插件。
     */
    STOP("STOP", "停止旧版本"),

    /**
     * 执行 schema 迁移（DDL）。
     */
    SCHEMA("SCHEMA", "执行数据迁移"),

    /**
     * 启动新版本插件。
     */
    START("START", "启动新版本"),

    /**
     * 健康检查，确认新版本可用。
     */
    HEALTH("HEALTH", "健康检查");

    /**
     * 入库值。
     */
    private final String code;

    /**
     * 中文描述。
     */
    private final String description;

    UpdateStep(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 按入库值解析枚举，大小写不敏感。
     *
     * @param code 入库值
     * @return 枚举实例
     * @throws IllegalArgumentException 未知阶段时抛出
     */
    public static UpdateStep fromCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("执行阶段不能为空");
        }
        String normalized = code.trim().toUpperCase(Locale.ROOT);
        for (UpdateStep step : values()) {
            if (step.code.equals(normalized)) {
                return step;
            }
        }
        throw new IllegalArgumentException("未知的执行阶段：" + code);
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
     * @return 流水线序号（从 0 开始，可用于计算进度百分比）
     */
    public int getOrder() {
        return ordinal();
    }

    /**
     * 判断该阶段之后失败是否必须回滚。
     *
     * @return true 表示旧版本已被停掉/替换，失败必须回滚
     */
    public boolean requiresRollbackOnFailure() {
        return ordinal() >= BACKUP.ordinal();
    }
}
