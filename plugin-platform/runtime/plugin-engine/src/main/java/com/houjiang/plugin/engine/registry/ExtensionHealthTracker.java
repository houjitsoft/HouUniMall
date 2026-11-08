package com.houjiang.plugin.engine.registry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 扩展实现健康跟踪与熔断（R3.1 / §1.3）。
 *
 * <p>按 {@code (point, source#implClass)} 维度统计<b>连续失败</b>次数；达到阈值（默认 5）时，
 * 通过 {@link ExtensionDisableSwitch#disable} 触发快照重建，把该实现从
 * {@link ExtensionRegistry#getExtensions(Class)} 结果中<b>摘除</b>。
 *
 * <p><b>三态模型</b>：
 * <pre>
 *   CLOSED（正常）──连续失败达阈值──▶ OPEN（摘除）
 *        ▲                                │
 *        │                      超过 recoveryWindowMs
 *        │                                ▼
 *        └────成功──── HALF_OPEN（放行一次试探）
 *                          │
 *                          └──再失败──▶ OPEN（重新计时）
 * </pre>
 *
 * <p><b>为什么补半开窗口</b>：早期版本只有「阈值摘除 + 手动 {@link #reset}」，
 * 一次抖动（如依赖的下游短暂超时）会让实现永久下线，只能靠人工介入恢复。
 * 半开让系统具备自愈能力：过了恢复窗口后放行一次，成了就彻底恢复，没成就重新计时。
 * {@link #reset} 仍然保留，用于运维即时干预。
 *
 * <p>成功或 {@code ctx.abort}（业务中断，非故障）都会清零连续失败计数；只有故障
 * （抛异常 / 超时）才累加。摘除/恢复共用 {@link ExtensionDisableSwitch} 这一条快照重建路径。
 *
 * @author HouCloud
 */
public class ExtensionHealthTracker {

    /** 默认恢复窗口（毫秒）：熔断后经过多久允许半开试探。 */
    public static final long DEFAULT_RECOVERY_WINDOW_MS = 60_000L;

    private static final Logger log = LoggerFactory.getLogger(ExtensionHealthTracker.class);

    /** 熔断器状态：{@code pointCanonical + '\u0000' + key} → 状态。 */
    private final Map<String, Circuit> circuits = new ConcurrentHashMap<>();

    private final ExtensionDisableSwitch disableSwitch;

    private final int failureThreshold;

    private final long recoveryWindowMs;

    /**
     * 构造器（使用 {@link #DEFAULT_RECOVERY_WINDOW_MS} 作为恢复窗口）。
     *
     * @param disableSwitch    禁用开关（摘除/恢复的共用重建路径）
     * @param failureThreshold 连续失败阈值（&le;0 表示关闭熔断）
     */
    public ExtensionHealthTracker(ExtensionDisableSwitch disableSwitch, int failureThreshold) {
        this(disableSwitch, failureThreshold, DEFAULT_RECOVERY_WINDOW_MS);
    }

    /**
     * 构造器。
     *
     * @param disableSwitch     禁用开关
     * @param failureThreshold  连续失败阈值（&le;0 表示关闭熔断）
     * @param recoveryWindowMs  恢复窗口（毫秒）；&le;0 表示关闭半开，只能手动 {@link #reset} 恢复
     */
    public ExtensionHealthTracker(ExtensionDisableSwitch disableSwitch, int failureThreshold,
                                  long recoveryWindowMs) {
        this.disableSwitch = disableSwitch;
        this.failureThreshold = failureThreshold;
        this.recoveryWindowMs = recoveryWindowMs;
    }

    /** 记录一次成功：清零连续失败计数并结束半开试探。 */
    public void recordSuccess(Class<?> point, String key) {
        Circuit removed = circuits.remove(id(point, key));
        if (removed != null && removed.halfOpen) {
            log.info("[ext-health] 半开试探成功，恢复：point={}, key={}",
                    ExtensionPointKeys.canonicalOf(point), key);
        }
    }

    /**
     * 记录一次故障。
     *
     * @return 本次是否触发/维持了摘除（已摘除返回 {@code true}）
     */
    public boolean recordFailure(Class<?> point, String key) {
        if (failureThreshold <= 0) {
            return false;
        }
        Circuit circuit = circuits.computeIfAbsent(id(point, key), k -> new Circuit());
        if (circuit.halfOpen) {
            // 半开期再次失败：立即重新熔断并把窗口重新计时，避免反复试探放大故障
            circuit.halfOpen = false;
            circuit.openedAtMillis = System.currentTimeMillis();
            circuit.failures.set(failureThreshold);
            log.warn("[ext-health] 半开试探失败，重新熔断：point={}, key={}",
                    ExtensionPointKeys.canonicalOf(point), key);
            disableSwitch.disable(point, key);
            return true;
        }
        int count = circuit.failures.incrementAndGet();
        if (count >= failureThreshold) {
            circuit.openedAtMillis = System.currentTimeMillis();
            log.warn("[ext-health] 扩展实现连续失败 {} 次，触发熔断摘除：point={}, key={}",
                    count, ExtensionPointKeys.canonicalOf(point), key);
            disableSwitch.disable(point, key);
            return true;
        }
        return false;
    }

    /**
     * @return 该实现当前是否健康（未被熔断摘除，或已进入半开可试探）
     */
    public boolean isHealthy(Class<?> point, String key) {
        if (!disableSwitch.isDisabled(point, key)) {
            return true;
        }
        if (recoveryWindowMs <= 0) {
            return false;
        }
        Circuit circuit = circuits.get(id(point, key));
        if (circuit == null) {
            // 无本地状态（如进程重启后仅恢复了禁用开关）：保持摘除，等下一次失败重建状态
            return false;
        }
        if (circuit.halfOpen) {
            return true;
        }
        if (System.currentTimeMillis() - circuit.openedAtMillis < recoveryWindowMs) {
            return false;
        }
        circuit.halfOpen = true;
        circuit.failures.set(0);
        disableSwitch.enable(point, key);
        log.info("[ext-health] 恢复窗口已过，进入半开试探：point={}, key={}",
                ExtensionPointKeys.canonicalOf(point), key);
        return true;
    }

    /**
     * 手动重置/恢复：清零失败计数并重新启用该实现。
     *
     * <p>运维即时干预通道；自动恢复走半开窗口。
     */
    public void reset(Class<?> point, String key) {
        circuits.remove(id(point, key));
        disableSwitch.enable(point, key);
        log.info("[ext-health] 手动恢复：point={}, key={}",
                ExtensionPointKeys.canonicalOf(point), key);
    }

    /** 当前连续失败计数（诊断用）。 */
    public int failureCount(Class<?> point, String key) {
        Circuit circuit = circuits.get(id(point, key));
        return circuit == null ? 0 : circuit.failures.get();
    }

    /** 阈值（诊断用）。 */
    public int failureThreshold() {
        return failureThreshold;
    }

    /** 恢复窗口（毫秒，诊断用）。 */
    public long recoveryWindowMs() {
        return recoveryWindowMs;
    }

    /** @return 是否处于半开试探中（诊断用） */
    public boolean isHalfOpen(Class<?> point, String key) {
        Circuit circuit = circuits.get(id(point, key));
        return circuit != null && circuit.halfOpen;
    }

    private static String id(Class<?> point, String key) {
        return ExtensionPointKeys.canonicalOf(point) + '\u0000' + (key == null ? "" : key);
    }

    /** 单个实现的熔断器状态。字段用 volatile：读写可能在不同线程。 */
    private static class Circuit {
        private final AtomicInteger failures = new AtomicInteger();
        private volatile long openedAtMillis;
        private volatile boolean halfOpen;
    }
}
