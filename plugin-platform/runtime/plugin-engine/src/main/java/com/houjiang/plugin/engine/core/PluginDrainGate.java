package com.houjiang.plugin.engine.core;

import com.houjiang.plugin.engine.config.ExtensionDegradePolicy;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 升级排空窗口（方案 §5.3 关键约定 3、§7 风险「升级期间 500」）。
 *
 * <p><b>要解决的问题</b>：升级必须先撤路由再换 jar，中间有一段「路由已撤、新版本还没起来」
 * 的真空期。如果不显式接管，这段时间的请求会走 {@code routeRegistry.lookup} 未命中 →
 * <b>404</b>，或者更糟：命中了半卸载状态的插件 → <b>500 / NPE</b>。
 * 前端既无法把 404 与「接口被删了」区分开，也无法把 500 与「插件有 bug」区分开，
 * 最终表现为「升级时用户看到一堆莫名其妙的报错」。
 *
 * <p><b>做法</b>：升级编排在开始撤流量之前调用 {@link #begin} 登记一段排空窗口，
 * 窗口内该插件的所有请求由 {@link PluginDispatcher} 统一返回
 * <b>503 + {@code Retry-After: 10}</b>，前端识别 503 后提示「功能升级中，请稍后重试」并自动重试。
 * 503 是这里唯一诚实的答案：它明确表达「暂时不可用、稍后重试」，
 * 而不是「资源不存在」或「服务器内部错误」。
 *
 * <p><b>为什么复用 {@link ExtensionDegradePolicy} 的语义而不是另立一套</b>：
 * 排空期的处置策略就是 {@link ExtensionDegradePolicy#ABORT}（fail-closed）——
 * 不静默忽略（IGNORE，那会让请求走到不存在的实现上）、
 * 也不回落官方实现（FALLBACK，那会让租户看到「升级后功能凭空变了」），
 * 而是<b>显式中断并让客户端重试</b>。因此这里不新增策略枚举，直接沿用既有语义，
 * 由 {@link #degradePolicy()} 暴露出来供调用方与文档对齐。
 *
 * <p><b>并发与失效</b>：状态放在 {@link ConcurrentHashMap}，按「到期时间戳」判定，
 * 读取时惰性清理——编排无论成功、失败、还是被中断（进程被杀）都不会把插件永久锁在排空态：
 * 最坏情况是窗口自然到期（默认 5 秒）后自动放行。
 *
 * @author HouCloud
 * @since M4
 */
public class PluginDrainGate {

    /**
     * {@code Retry-After} 缺省秒数：与排空窗口（5 秒）同量级并留出装载时间，
     * 让客户端「等一会儿再试」而不是疯狂重试把刚起来的新版本打垮。
     */
    private static final int DEFAULT_RETRY_AFTER_SECONDS = 10;

    /**
     * 排空期间对请求的降级策略（fail-closed，见类注释）。
     */
    private static final ExtensionDegradePolicy DRAIN_POLICY = ExtensionDegradePolicy.ABORT;

    private final ConcurrentHashMap<String, Long> drainUntilMillis = new ConcurrentHashMap<>();

    private final int retryAfterSeconds;

    /**
     * 使用缺省 {@code Retry-After}（10 秒）构造。
     */
    public PluginDrainGate() {
        this(DEFAULT_RETRY_AFTER_SECONDS);
    }

    /**
     * 构造排空窗口登记表。
     *
     * @param retryAfterSeconds 响应头 {@code Retry-After} 的秒数，小于 1 时按缺省值处理
     */
    public PluginDrainGate(int retryAfterSeconds) {
        this.retryAfterSeconds = retryAfterSeconds >= 1 ? retryAfterSeconds : DEFAULT_RETRY_AFTER_SECONDS;
    }

    /**
     * 开始排空：登记窗口截止时间。重复调用按「更晚的窗口」生效（幂等、可续期）。
     *
     * @param pluginCode 插件编码
     * @param windowMillis 排空窗口长度（毫秒），小于等于 0 视为 0（立即结束排空）
     */
    public void begin(String pluginCode, long windowMillis) {
        if (pluginCode == null || pluginCode.isBlank() || windowMillis <= 0) {
            return;
        }
        long until = System.currentTimeMillis() + windowMillis;
        drainUntilMillis.merge(pluginCode, until, Math::max);
    }

    /**
     * 立即结束排空（升级成功或回滚完成后调用）。
     *
     * @param pluginCode 插件编码
     */
    public void end(String pluginCode) {
        if (pluginCode == null) {
            return;
        }
        drainUntilMillis.remove(pluginCode);
    }

    /**
     * 判断插件是否处于排空窗口内。惰性清理已过期的登记项。
     *
     * @param pluginCode 插件编码
     * @return true 表示该插件正在排空，请求应返回 503
     */
    public boolean isDraining(String pluginCode) {
        if (pluginCode == null) {
            return false;
        }
        Long until = drainUntilMillis.get(pluginCode);
        if (until == null) {
            return false;
        }
        if (System.currentTimeMillis() >= until) {
            drainUntilMillis.remove(pluginCode, until);
            return false;
        }
        return true;
    }

    /**
     * @return 排空响应应携带的 {@code Retry-After} 秒数
     */
    public int retryAfterSeconds() {
        return retryAfterSeconds;
    }

    /**
     * @return 排空期的降级策略（恒为 {@link ExtensionDegradePolicy#ABORT}）
     */
    public ExtensionDegradePolicy degradePolicy() {
        return DRAIN_POLICY;
    }

    /**
     * 当前仍在排空窗口内的插件编码（排障与单测用）。
     *
     * @return 不可变快照
     */
    public Set<String> drainingPlugins() {
        Set<String> active = new LinkedHashSet<>();
        for (Map.Entry<String, Long> entry : drainUntilMillis.entrySet()) {
            if (isDraining(entry.getKey())) {
                active.add(entry.getKey());
            }
        }
        return Collections.unmodifiableSet(active);
    }
}
