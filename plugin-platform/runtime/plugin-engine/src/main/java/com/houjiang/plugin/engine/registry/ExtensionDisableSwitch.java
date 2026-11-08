package com.houjiang.plugin.engine.registry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 运行期扩展点禁用开关（R3.3 / §1.3）。
 *
 * <p>内存态单一事实源：记录「某个扩展点下被排除的实现键」。
 * 实现键约定为 {@code source#implClass}（精确到单个实现），也接受仅 {@code source}
 * （禁用某来源在该扩展点下的全部实现）。
 *
 * <p><b>即刻生效</b>：{@link #disable}/{@link #enable} 变更后立即驱动
 * {@link ExtensionRegistry} 在同一次写锁内过滤 + 原子替换快照；热路径
 * {@link ExtensionRegistry#getExtensions(Class)} 仍是零锁 volatile 读，不查库、不加锁。
 *
 * <p><b>并发安全</b>：变更发生在写锁内、快照整体替换；并发中的旧请求持有的是旧快照，
 * 不会被就地修改，因此<strong>旧请求不中断</strong>——只有新请求看到新快照。
 *
 * <p><b>可插拔装载源</b>：通过 {@link #addSource(ExtensionDisableSource)} 注册装载源
 * （默认装载源读取配置 {@code houcloud.plugin.disabled-extensions}），
 * {@link #refreshFromSources()} 在启动期把配置项装载进内存态。
 *
 * <p>本类也是熔断摘除/恢复的共用路径：{@code ExtensionHealthTracker} 达到阈值时调用
 * {@link #disable}，手动恢复时调用 {@link #enable}，两者复用同一快照重建入口。
 *
 * @author HouCloud
 */
public class ExtensionDisableSwitch {

    private static final Logger log = LoggerFactory.getLogger(ExtensionDisableSwitch.class);

    /** 禁用键：point 规范名 → 被排除的实现键集合。 */
    private final Map<String, Set<String>> disabled = new ConcurrentHashMap<>();

    /** 可插拔装载源（启动期装载默认配置）。 */
    private final List<ExtensionDisableSource> sources = new CopyOnWriteArrayList<>();

    /** 快照重建目标；可为 {@code null}（纯内存态，用于单测/独立使用）。 */
    private final ExtensionRegistry registry;

    /** 构造器：绑定注册表，使变更即刻生效。 */
    public ExtensionDisableSwitch(ExtensionRegistry registry) {
        this.registry = registry;
    }

    /** 构造器：不绑定注册表（仅内存态，需自行调用 {@link #applyToRegistry()}）。 */
    public ExtensionDisableSwitch() {
        this.registry = null;
    }

    /** 禁用某个扩展点下的一个实现键。 */
    public void disable(Class<?> point, String key) {
        disable(ExtensionPointKeys.canonicalOf(point), key);
    }

    /** 重新启用某个扩展点下的一个实现键。 */
    public void enable(Class<?> point, String key) {
        enable(ExtensionPointKeys.canonicalOf(point), key);
    }

    /** 某扩展点下被禁用的实现键快照（不可变）。 */
    public Set<String> disabledKeys(Class<?> point) {
        Set<String> keys = disabled.get(ExtensionPointKeys.canonicalOf(point));
        return keys == null ? Set.of() : Collections.unmodifiableSet(keys);
    }

    /** 当前全部禁用项快照（point 规范名 → 实现键集合），供诊断。 */
    public Map<String, Set<String>> snapshot() {
        Map<String, Set<String>> copy = new ConcurrentHashMap<>();
        disabled.forEach((k, v) -> copy.put(k, Set.copyOf(v)));
        return Map.copyOf(copy);
    }

    /**
     * 按规范短名禁用一个实现键。
     *
     * @param pointCanonical 已规范化的扩展点短名（见 {@link ExtensionPointKeys#canonical(String)}）
     * @param key            实现键（{@code source#implClass} 或 {@code source}）
     */
    public void disable(String pointCanonical, String key) {
        String normalizedKey = normalizeKey(key);
        if (normalizedKey.isEmpty()) {
            return;
        }
        Set<String> keys = disabled.computeIfAbsent(pointCanonical, k -> ConcurrentHashMap.newKeySet());
        if (keys.add(normalizedKey)) {
            log.info("[ext-disable] 运行期禁用：point={}, key={}", pointCanonical, normalizedKey);
            applyToRegistry(pointCanonical);
        }
    }

    /** 按规范短名重新启用一个实现键。 */
    public void enable(String pointCanonical, String key) {
        String normalizedKey = normalizeKey(key);
        Set<String> keys = disabled.get(pointCanonical);
        if (keys != null && keys.remove(normalizedKey)) {
            log.info("[ext-disable] 运行期恢复：point={}, key={}", pointCanonical, normalizedKey);
            applyToRegistry(pointCanonical);
        }
    }

    /** 某扩展点按规范短名查询被禁用的实现键快照。 */
    public Set<String> disabledKeys(String pointCanonical) {
        Set<String> keys = disabled.get(pointCanonical);
        return keys == null ? Set.of() : Collections.unmodifiableSet(keys);
    }

    /** 判断某实现键是否被禁用（含「禁用整个 source」的匹配）。 */
    public boolean isDisabled(Class<?> point, String key) {
        return isDisabledCanonical(ExtensionPointKeys.canonicalOf(point), key);
    }

    /** 判断某实现键是否被禁用（按规范短名）。 */
    public boolean isDisabledCanonical(String pointCanonical, String key) {
        Set<String> keys = disabled.get(pointCanonical);
        if (keys == null || keys.isEmpty()) {
            return false;
        }
        String normalizedKey = normalizeKey(key);
        if (keys.contains(normalizedKey)) {
            return true;
        }
        return keys.contains(ExtensionPointKeys.sourceOf(normalizedKey));
    }

    /** 注册一个可插拔装载源。 */
    public void addSource(ExtensionDisableSource source) {
        if (source != null) {
            sources.add(source);
        }
    }

    /**
     * 从全部装载源重新装载禁用项并应用。
     *
     * <p>幂等：重复装载不会产生重复项（{@link ConcurrentHashMap#newKeySet()} + {@link Set#add}）。
     */
    public void refreshFromSources() {
        for (ExtensionDisableSource source : sources) {
            List<String> entries;
            try {
                entries = source.load();
            } catch (RuntimeException ex) {
                log.warn("[ext-disable] 装载源 {} 加载失败，已跳过", source.getClass().getSimpleName(), ex);
                continue;
            }
            if (entries == null) {
                continue;
            }
            for (String entry : entries) {
                applyConfigEntry(entry);
            }
        }
        applyAllToRegistry();
    }

    /** 解析并装载单条配置项，格式 {@code <pointShortName>#<source>} 或 {@code <point>#<source>#<implClass>}。 */
    private void applyConfigEntry(String entry) {
        if (entry == null || entry.isBlank()) {
            return;
        }
        String trimmed = entry.trim();
        int idx = trimmed.indexOf('#');
        if (idx <= 0) {
            throw new IllegalStateException("禁用项格式非法（应为 <point>#<source>）：" + entry);
        }
        String pointName = trimmed.substring(0, idx);
        String key = trimmed.substring(idx + 1);
        String canonicalPoint;
        try {
            canonicalPoint = ExtensionPointKeys.canonical(pointName);
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("禁用项扩展点短名非法：" + entry, ex);
        }
        disable(canonicalPoint, key);
    }

    /** 把某扩展点的禁用集合应用到注册表（找到对应类型并原子替换该片快照）。 */
    private void applyToRegistry(String pointCanonical) {
        if (registry == null) {
            return;
        }
        registry.applyDisabledByPointName(pointCanonical, disabled.get(pointCanonical));
    }

    /** 把所有禁用项应用到注册表（启动期全量应用）。 */
    private void applyAllToRegistry() {
        if (registry == null) {
            return;
        }
        registry.applyDisabledAll(disabled);
    }

    private static String normalizeKey(String key) {
        return key == null ? "" : key.trim();
    }

    /**
     * 可插拔禁用项装载源。
     *
     * <p>返回若干条 {@code <pointShortName>#<source>} / {@code <point>#<source>#<implClass>}
     * 形式的配置项。默认实现读取配置 {@code houcloud.plugin.disabled-extensions}；
     * 后续可扩展为 DB 装载源（下一迭代）。
     */
    @FunctionalInterface
    public interface ExtensionDisableSource {

        /** @return 禁用项列表，可为 {@code null} */
        List<String> load();
    }

    /** 便捷工厂：基于列表的静态装载源。 */
    public static ExtensionDisableSource ofList(List<String> entries) {
        List<String> copy = entries == null ? List.of() : new ArrayList<>(entries);
        return () -> copy;
    }
}
