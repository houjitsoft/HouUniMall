package com.houjiang.plugin.engine.registry;

import com.houjiang.plugin.sdk.extension.ExtensionMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 扩展点注册表（方案 4.7 / 7.4.1）。
 *
 * <p>写入路径（注册、卸载、运行期禁用、熔断摘除/恢复）在写锁内完成「追加或删除 → 应用排除集
 * → 按优先级重排序 → 冻结为不可变列表 → 一次性替换快照」；读取路径只做一次 volatile 读，
 * 不复制、不排序、不扫描类与文件。由此满足 §7.4.2 两条 P0 闭坑项的验收门槛：同一请求链路无
 * {@code getExtensions} 复制/排序，且并发请求只能看到旧快照或新快照之一。
 *
 * <p>快照按扩展点类型分片，注册/卸载/禁用只重建受影响类型的那一片，其余片沿用旧条目。
 *
 * <p>同级冲突检测（§7.4.2 P0「二开实现冲突」，2026-09-13 落地）：同一扩展点出现两个
 * 相同 {@code priority} 且同为 {@code owner=custom} 的不同实现时，注册阶段直接抛
 * {@link IllegalStateException}，记录扩展点、优先级、双方实现类与来源插件，
 * 不允许按加载顺序碰运气；冲突时注册表保持不变，Core 不进入半生效状态。
 * 二开优先级解析（custom → official → 默认降级的取值约定）不在本类，由注册方按 priority 表达。
 * {@code REPLACE} 是显式的单点接管策略，同一扩展点只能存在一个 custom replacement；
 * {@code APPEND} 才是默认的有序钩子策略。
 *
 * <p><b>运行期管控（R3.1/R3.3）</b>：{@link #applyDisabled(Class, Set)} 与 {@link #rebuildAll()}
 * 仅<b>新增</b>能力，复用既有 {@code writeLock} + 原子替换，读路径依旧零锁；不改变既有注册/卸载语义。
 *
 * @author HouCloud
 */
public class ExtensionRegistry {

    /** 供启动日志、诊断接口和验收使用的只读注册元数据。 */
    public record Registration(String sourceCode, String implementationClass,
                               String owner, int priority, ExtensionMode mode) {
    }

    /** 快照单元：某个实现的实例 + 来源 + 参与方式（供调用期可观测与来源解析）。 */
    public record ExtensionHandle(Object instance, String source, ExtensionMode mode) {
    }

    /** 官方实现归属标识 */
    public static final String OWNER_OFFICIAL = "official";

    /** 二开实现归属标识（§2.4 plugin.yaml 的 owner: custom） */
    public static final String OWNER_CUSTOM = "custom";

    /** 第三方商业插件归属标识（默认只能追加扩展，不得接管官方实现） */
    public static final String OWNER_THIRDPARTY = "thirdparty";

    private static final class Holder {
        final Object instance;
        final String pluginCode;
        final int priority;
        final String owner;
        final ExtensionMode mode;

        Holder(Object instance, String pluginCode, int priority, String owner, ExtensionMode mode) {
            this.instance = instance;
            this.pluginCode = pluginCode;
            this.priority = priority;
            this.owner = owner == null ? OWNER_OFFICIAL : owner;
            this.mode = mode == null ? ExtensionMode.APPEND : mode;
        }
    }

    /**
     * 不可变快照单元：某扩展点按优先级排序后的实例列表与句柄列表。
     *
     * <p><b>为何用 {@code Slice}（而非裸 {@code List<Object>}）</b>：调用期需要「实例 + 来源 + 参与方式」
     * 三元组做可观测与来源解析，而 {@code getExtensions} 又必须是单次 volatile 读、零锁、不复制。
     * 把 {@code instances} 与 {@code handles} 绑进同一个不可变对象，使二者<b>永远来自同一代快照</b>，
     * 避免「两次 volatile 读到不同代」导致来源与实例错配；{@code getExtensions} 直接返回
     * {@code slice.instances()} —— 对外语义与旧 {@code List<Object>} 完全一致（只是内部形态改变）。
     */
    private record Slice(List<Object> instances, List<ExtensionHandle> handles) {
        static final Slice EMPTY = new Slice(List.of(), List.of());
    }

    /** 写锁：对 {@link #holders}、{@link #excluded} 与 {@link #snapshot} 的一切修改都必须持有本锁 */
    private final Object writeLock = new Object();

    /** 可变注册表：只在 {@link #writeLock} 内访问 */
    private final Map<Class<?>, List<Holder>> holders = new HashMap<>();

    /**
     * 运行期排除集：扩展点规范名 → 被排除的实现键（{@code source#implClass} 或 {@code source}）。
     *
     * <p><b>为何「空集 = no-op」</b>：{@link #freeze(Class)} 取到的 {@code excludedKeys} 为 {@code null}
     * 或空集时，{@link #isExcluded(Set, Holder)} <b>立即返回 false</b>（短路），不进入任何键匹配。
     * 因此从未调用过禁用/熔断的正常运行期，本表为空，冻结路径<b>无任何额外过滤开销</b>，
     * 与本次只加不改前的行为逐位等价；只有真正发生运行期禁用（{@link #applyDisabled(Class, Set)}）
     * 时，对应扩展点的键集才非空并触发过滤。默认 {@code HashMap} 空实例即代表"该点无排除"。
     */
    private final Map<String, Set<String>> excluded = new HashMap<>();

    /** 已排序的不可变快照；写锁内整体替换，读路径无锁 */
    private volatile Map<Class<?>, Slice> snapshot = Map.of();

    /** 注册官方实现；二开/第三方实现请使用带 {@code owner} 的重载。 */
    public <E> void register(Class<E> type, E instance, String pluginCode, int priority) {
        register(type, instance, pluginCode, priority, OWNER_OFFICIAL);
    }

    /**
     * 注册扩展点实现。
     *
     * <p><b>同级冲突</b>（§7.4.2 P0「二开实现冲突」）：同一扩展点已存在相同 {@code priority}
     * 且同为 {@code owner=custom} 的不同实现类时，直接抛 {@link IllegalStateException}——
     * 注册发生在插件启动/启用阶段，插件因此启动失败，Core 不进入半生效状态；
     * 冲突时注册表与快照保持不变。错误信息包含扩展点、优先级、双方实现类与来源插件。
     *
     * <p><b>幂等</b>：同一插件对同一实现类的重复注册（如停用后再启用）直接跳过，不产生重复项。
     */
    public <E> void register(Class<E> type, E instance, String pluginCode, int priority, String owner) {
        register(type, instance, pluginCode, priority, owner, ExtensionMode.APPEND);
    }

    /**
     * Registers an implementation with an explicit append/replace strategy.
     * The strategy is resolved while publishing the snapshot, never during a
     * business request.
     */
    public <E> void register(Class<E> type, E instance, String pluginCode, int priority,
                             String owner, ExtensionMode mode) {
        if (type == null) {
            throw new IllegalArgumentException("Extension point type must not be null");
        }
        if (instance == null) {
            throw new IllegalArgumentException("Extension implementation must not be null");
        }
        if (pluginCode == null || pluginCode.isBlank()) {
            throw new IllegalArgumentException("Extension source code must not be blank");
        }
        String effectiveOwner = normalizeOwner(owner);
        ExtensionMode effectiveMode = mode == null ? ExtensionMode.APPEND : mode;
        if (effectiveMode == ExtensionMode.REPLACE && !OWNER_CUSTOM.equals(effectiveOwner)) {
            throw new IllegalArgumentException("Only owner=custom may use ExtensionMode.REPLACE: "
                    + effectiveOwner);
        }
        synchronized (writeLock) {
            List<Holder> list = holders.computeIfAbsent(type, k -> new ArrayList<>());
            // 1) 幂等：同一插件对同一实现类的重复注册（如停用后再启用）直接跳过，不产生重复项
            for (Holder existing : list) {
                if (existing.pluginCode.equals(pluginCode)
                        && existing.instance.getClass().equals(instance.getClass())
                        && existing.priority == priority
                        && existing.owner.equals(effectiveOwner)
                        && existing.mode == effectiveMode) {
                    return;
                }
            }
            if (effectiveMode == ExtensionMode.REPLACE) {
                for (Holder existing : list) {
                    if (existing.mode == ExtensionMode.REPLACE
                            && OWNER_CUSTOM.equals(existing.owner)
                            && (!existing.instance.getClass().equals(instance.getClass())
                            || !existing.pluginCode.equals(pluginCode))) {
                        throw new IllegalStateException(String.format(
                                "Extension point %s has multiple custom replacements: %s (%s) and %s (%s). "
                                        + "Choose one replacement or use APPEND.",
                                type.getName(), existing.instance.getClass().getName(), existing.pluginCode,
                                instance.getClass().getName(), pluginCode));
                    }
                }
            }
            // 2) 同级二开冲突（§7.4.2 P0）：已存在相同 priority 且同为 custom 的不同实现类
            for (Holder existing : list) {
                if (existing.priority == priority
                        && OWNER_CUSTOM.equalsIgnoreCase(existing.owner)
                        && OWNER_CUSTOM.equals(effectiveOwner)
                        && !existing.instance.getClass().equals(instance.getClass())) {
                    throw new IllegalStateException(String.format(
                            "扩展点同级二开实现冲突：%s priority=%d，已存在 %s（插件 %s），新注册 %s（插件 %s）。"
                                    + "按方案 7.4.2 P0 注册阶段直接失败，请调整 priority 或移除其中一个实现。",
                            type.getName(), priority,
                            existing.instance.getClass().getName(), existing.pluginCode,
                            instance.getClass().getName(), pluginCode));
                }
            }
            list.add(new Holder(instance, pluginCode, priority, effectiveOwner, effectiveMode));
            publish(type);
        }
    }

    public void unregisterByPlugin(String pluginCode) {
        synchronized (writeLock) {
            List<Class<?>> touched = new ArrayList<>();
            for (Map.Entry<Class<?>, List<Holder>> entry : holders.entrySet()) {
                if (entry.getValue().removeIf(h -> h.pluginCode.equals(pluginCode))) {
                    touched.add(entry.getKey());
                }
            }
            if (touched.isEmpty()) {
                return;
            }
            // 先备齐所有受影响类型的新快照，再一次性替换：读侧不会看到「半套扩展」
            Map<Class<?>, Slice> next = new HashMap<>(snapshot);
            for (Class<?> type : touched) {
                next.put(type, freeze(type));
            }
            snapshot = Collections.unmodifiableMap(next);
        }
    }

    /**
     * 读取某个扩展点的实现列表。
     *
     * @return 按 {@code priority} 升序排列的不可变列表；无实现时返回空列表，调用方不可修改
     */
    @SuppressWarnings("unchecked")
    public <E> List<E> getExtensions(Class<E> type) {
        Slice slice = snapshot.get(type);
        if (slice == null) {
            return Collections.emptyList();
        }
        return (List<E>) slice.instances();
    }

    /**
     * 读取某个扩展点的实现句柄（实例 + 来源 + 参与方式）。
     *
     * <p>与 {@link #getExtensions(Class)} 同一次 volatile 读，保证实例与来源一致；
     * 供调用期可观测与来源解析使用，热路径无锁、不复制排序。
     */
    public List<ExtensionHandle> getHandles(Class<?> type) {
        Slice slice = snapshot.get(type);
        return slice == null ? List.of() : slice.handles();
    }

    /**
     * Returns the immutable registration metadata for one extension point.
     * The returned view contains no implementation instances, so diagnostics
     * cannot mutate or retain the runtime extension chain accidentally.
     */
    public List<Registration> getRegistrations(Class<?> type) {
        if (type == null) {
            return List.of();
        }
        synchronized (writeLock) {
            List<Holder> list = holders.get(type);
            if (list == null || list.isEmpty()) {
                return List.of();
            }
            List<Holder> sorted = new ArrayList<>(list);
            sorted.sort(Comparator.comparingInt(h -> h.priority));
            boolean customReplacement = sorted.stream().anyMatch(h ->
                    OWNER_CUSTOM.equalsIgnoreCase(h.owner) && h.mode == ExtensionMode.REPLACE);
            List<Registration> result = new ArrayList<>(sorted.size());
            for (Holder holder : sorted) {
                if (customReplacement && OWNER_OFFICIAL.equalsIgnoreCase(holder.owner)) {
                    continue;
                }
                result.add(new Registration(holder.pluginCode, holder.instance.getClass().getName(),
                        holder.owner, holder.priority, holder.mode));
            }
            return List.copyOf(result);
        }
    }

    // ===================== 运行期管控（R3.1 / R3.3，只加不改） =====================

    /**
     * 应用运行期禁用集合（R3.3）。
     *
     * <p>写锁内过滤 + 原子替换该扩展点快照片段；其余类型片段沿用旧条目。
     *
     * @param point 扩展点类型
     * @param keys  被排除的实现键集合（{@code source#implClass} 或 {@code source}）；空集表示清除
     */
    public void applyDisabled(Class<?> point, Set<String> keys) {
        if (point == null) {
            return;
        }
        applyDisabledByPointName(ExtensionPointKeys.canonicalOf(point), keys);
    }

    /**
     * 按扩展点规范名应用禁用集合。
     *
     * <p>可在扩展点注册<b>之前</b>调用——排除集按规范名保存，后续注册冻结时同样生效。
     */
    public void applyDisabledByPointName(String canonicalPoint, Set<String> keys) {
        if (canonicalPoint == null || canonicalPoint.isBlank()) {
            return;
        }
        synchronized (writeLock) {
            boolean changed;
            if (keys == null || keys.isEmpty()) {
                changed = excluded.remove(canonicalPoint) != null;
            } else {
                Set<String> immutable = Set.copyOf(keys);
                Set<String> previous = excluded.put(canonicalPoint, immutable);
                changed = !immutable.equals(previous);
            }
            if (!changed) {
                return;
            }
            Map<Class<?>, Slice> next = new HashMap<>(snapshot);
            for (Class<?> type : new HashSet<>(holders.keySet())) {
                if (ExtensionPointKeys.canonicalOf(type).equals(canonicalPoint)) {
                    next.put(type, freeze(type));
                }
            }
            snapshot = Collections.unmodifiableMap(next);
        }
    }

    /** 全量应用禁用集合（清空后重建）；供启动期装载使用。 */
    public void applyDisabledAll(Map<String, Set<String>> disabled) {
        synchronized (writeLock) {
            excluded.clear();
            if (disabled != null) {
                disabled.forEach((key, value) -> {
                    if (key != null && !key.isBlank() && value != null && !value.isEmpty()) {
                        excluded.put(key, Set.copyOf(value));
                    }
                });
            }
            rebuildAllLocked();
        }
    }

    /**
     * 全量重建快照（熔断摘除 / 运行期禁用 / 手动恢复的共用入口），幂等。
     *
     * <p>写锁内按当前注册表与排除集重新冻结所有类型，一次性原子替换。
     */
    public void rebuildAll() {
        synchronized (writeLock) {
            rebuildAllLocked();
        }
    }

    private void rebuildAllLocked() {
        Set<Class<?>> types = new HashSet<>(holders.keySet());
        types.addAll(snapshot.keySet());
        Map<Class<?>, Slice> next = new HashMap<>(types.size());
        for (Class<?> type : types) {
            next.put(type, freeze(type));
        }
        snapshot = Collections.unmodifiableMap(next);
    }

    /** 重新生成单个扩展点类型的快照片段；调用方必须已持有 {@link #writeLock}。 */
    private Slice freeze(Class<?> type) {
        List<Holder> list = holders.get(type);
        if (list == null || list.isEmpty()) {
            return Slice.EMPTY;
        }
        // List.sort 是稳定排序，同级实现保持注册先后顺序，与插件加载顺序一致
        List<Holder> sorted = new ArrayList<>(list);
        sorted.sort(Comparator.comparingInt(h -> h.priority));
        boolean customReplacement = sorted.stream().anyMatch(h ->
                OWNER_CUSTOM.equalsIgnoreCase(h.owner) && h.mode == ExtensionMode.REPLACE);
        Set<String> excludedKeys = excluded.get(ExtensionPointKeys.canonicalOf(type));
        List<Object> instances = new ArrayList<>(sorted.size());
        List<ExtensionHandle> handles = new ArrayList<>(sorted.size());
        for (Holder holder : sorted) {
            if (customReplacement && OWNER_OFFICIAL.equalsIgnoreCase(holder.owner)) {
                continue;
            }
            if (isExcluded(excludedKeys, holder)) {
                continue;
            }
            instances.add(holder.instance);
            handles.add(new ExtensionHandle(holder.instance, holder.pluginCode, holder.mode));
        }
        return new Slice(List.copyOf(instances), List.copyOf(handles));
    }

    /** 运行期禁用 / 熔断摘除匹配：支持 {@code source#implClass} 与整 {@code source} 两种键。 */
    private static boolean isExcluded(Set<String> keys, Holder holder) {
        if (keys == null || keys.isEmpty()) {
            return false;
        }
        if (keys.contains(holder.pluginCode)) {
            return true;
        }
        return keys.contains(holder.pluginCode + "#" + holder.instance.getClass().getName());
    }

    private static String normalizeOwner(String owner) {
        String value = owner == null || owner.isBlank()
                ? OWNER_OFFICIAL : owner.trim().toLowerCase(Locale.ROOT);
        if (!OWNER_OFFICIAL.equals(value) && !OWNER_CUSTOM.equals(value) && !OWNER_THIRDPARTY.equals(value)) {
            throw new IllegalArgumentException("Unsupported extension owner: " + owner
                    + "; expected official, custom or thirdparty");
        }
        return value;
    }

    /** 只重建 {@code type} 这一片并原子替换整张快照；调用方必须已持有 {@link #writeLock}。 */
    private void publish(Class<?> type) {
        Map<Class<?>, Slice> next = new HashMap<>(snapshot);
        next.put(type, freeze(type));
        snapshot = Collections.unmodifiableMap(next);
    }
}
