package com.houjiang.plugin.engine.registry;

import com.houjiang.plugin.engine.core.PluginState;
import com.houjiang.plugin.sdk.model.PluginManifest;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 插件运行状态表：维护每个插件的 {@link PluginState} 与 {@link PluginManifest}。
 *
 * @author HouCloud
 */
public class PluginRegistry {

    public static class PluginRecord {
        private PluginState state;
        private PluginManifest manifest;

        public PluginRecord(PluginState state, PluginManifest manifest) {
            this.state = state;
            this.manifest = manifest;
        }

        public PluginState getState() {
            return state;
        }

        public void setState(PluginState state) {
            this.state = state;
        }

        public PluginManifest getManifest() {
            return manifest;
        }

        public void setManifest(PluginManifest manifest) {
            this.manifest = manifest;
        }
    }

    private final Map<String, PluginRecord> records = new ConcurrentHashMap<>();

    public void put(String code, PluginState state, PluginManifest manifest) {
        records.put(code, new PluginRecord(state, manifest));
    }

    /**
     * 写入状态：记录不存在时创建，已存在时只更新状态并保留原 manifest。
     *
     * <p>修复：原实现仅在记录已存在时更新，导致 {@code install/enable/disable} 在首次调用时
     * 状态被静默丢弃（{@code getState} 恒为 null）。生命周期状态机依赖本方法可 upsert。
     */
    public void putState(String code, PluginState state) {
        records.compute(code, (key, existing) -> existing == null
                ? new PluginRecord(state, null)
                : new PluginRecord(state, existing.getManifest()));
    }

    /** Record an initial failure without overwriting a concurrently existing runtime record. */
    public void putStateIfAbsent(String code, PluginState state) {
        records.putIfAbsent(code, new PluginRecord(state, null));
    }

    /** 更新已验证的 manifest，同时保留当前生命周期状态。 */
    public void putManifest(String code, PluginManifest manifest) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Plugin code must not be blank");
        }
        records.compute(code, (key, existing) -> existing == null
                ? new PluginRecord(PluginState.RESOLVED, manifest)
                : new PluginRecord(existing.getState(), manifest));
    }

    public PluginState getState(String code) {
        PluginRecord r = records.get(code);
        return r == null ? null : r.getState();
    }

    public boolean isActive(String code) {
        return getState(code) == PluginState.ACTIVE;
    }

    public PluginManifest getManifest(String code) {
        PluginRecord r = records.get(code);
        return r == null ? null : r.getManifest();
    }

    public void remove(String code) {
        records.remove(code);
    }

    /**
     * 全量快照（不可变副本），供 {@code PluginHostApi#activeCapabilities} 等遍历场景使用。
     *
     * <p>刻意返回副本而非内部 Map：调用方遍历时不应受并发安装/卸载影响，
     * 也避免外部拿到引用后直接修改状态表。
     */
    public Map<String, PluginRecord> snapshot() {
        return Map.copyOf(records);
    }
}
