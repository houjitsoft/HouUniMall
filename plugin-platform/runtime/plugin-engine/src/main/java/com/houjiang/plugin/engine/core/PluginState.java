package com.houjiang.plugin.engine.core;

/**
 * 插件生命周期状态机（见方案 4.6）。
 *
 * <pre>
 * RESOLVED → DOWNLOADED → VERIFIED → MIGRATED → ACTIVE ⇄ DISABLED → UNINSTALLED
 *                                              │
 *                                              └──→ FAILED(reason) → ROLLBACK
 * 升级：ACTIVE → UPGRADING → ACTIVE(新版本)
 * </pre>
 *
 * @author HouCloud
 */
public enum PluginState {
    RESOLVED,
    DOWNLOADED,
    VERIFIED,
    MIGRATED,
    ACTIVE,
    DISABLED,
    UNINSTALLED,
    FAILED,
    UPGRADING
}
