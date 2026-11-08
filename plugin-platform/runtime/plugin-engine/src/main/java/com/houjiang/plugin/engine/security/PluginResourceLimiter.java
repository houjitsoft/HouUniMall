package com.houjiang.plugin.engine.security;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 插件资源配额（方案 10.x）。
 *
 * <p>限制单插件<b>可观测的进程内资源占用</b>：并发执行数与调用频率。
 * 目的是让一个缺陷或恶意插件的影响面被关在它自己的配额里，不波及宿主与其它插件。
 *
 * <p><b>能力边界（重要）</b>：进程内配额无法约束 CPU 时间、堆内存、文件 IO——
 * 那需要独立进程形态（standalone / {@code plugin-runtime}）配合 cgroups 才能真正隔离。
 * 本类解决的是「同一 JVM 内，插件把线程池和 QPS 吃光」这一类问题，
 * 属于可观测 + 可降级的第一道闸，不是完整沙箱。
 *
 * <p><b>默认不限制</b>：配额未显式配置时（{@code maxConcurrent <= 0} 且 {@code qps <= 0}），
 * {@link #acquire} 恒返回 {@code true}。这样既有插件不会因引入本类而突然被拒。
 * 需要收紧时通过 {@link #configure} 按插件下发配额。
 *
 * <p><b>调用约定</b>：{@link #acquire} 返回 {@code true} 后必须配对调用 {@link #release}，
 * 建议放在 {@code finally} 块；未获得许可时不得调用 {@code release}。
 *
 * @author HouCloud
 */
public class PluginResourceLimiter {

    /** 无限制配额（默认）。 */
    public static final Quota UNLIMITED = new Quota(0, 0);

    private final Map<String, Quota> quotas = new ConcurrentHashMap<>();
    private final Map<String, Semaphore> semaphores = new ConcurrentHashMap<>();
    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();
    private final Map<String, Stats> stats = new ConcurrentHashMap<>();

    /**
     * 为插件套用默认配额（历史签名）。
     *
     * <p>幂等：重复调用不会重置已有配额，也不会清空统计。
     */
    public void apply(String pluginCode) {
        requireCode(pluginCode);
        quotas.putIfAbsent(pluginCode, UNLIMITED);
    }

    /**
     * 下发插件配额。
     *
     * @param quota 为 {@code null} 时按 {@link #UNLIMITED} 处理
     */
    public void configure(String pluginCode, Quota quota) {
        requireCode(pluginCode);
        Quota effective = quota == null ? UNLIMITED : quota;
        quotas.put(pluginCode, effective);
        semaphores.compute(pluginCode, (code, existing) -> effective.maxConcurrent() > 0
                ? new Semaphore(effective.maxConcurrent(), true)
                : null);
        buckets.compute(pluginCode, (code, existing) -> effective.qps() > 0
                ? new TokenBucket(effective.qps())
                : null);
    }

    /**
     * 尝试获取一次执行许可（并发信号量 + QPS 令牌桶双重判定）。
     *
     * @return 获得许可返回 {@code true}；被限流返回 {@code false}
     */
    public boolean acquire(String pluginCode) {
        requireCode(pluginCode);
        quotas.putIfAbsent(pluginCode, UNLIMITED);
        Semaphore semaphore = semaphores.get(pluginCode);
        if (semaphore != null && !semaphore.tryAcquire()) {
            stats.computeIfAbsent(pluginCode, k -> new Stats()).rejected.incrementAndGet();
            return false;
        }
        TokenBucket bucket = buckets.get(pluginCode);
        if (bucket != null && !bucket.tryConsume()) {
            // 令牌桶拒绝时必须归还已拿到的信号量，否则配额会被永久泄漏
            if (semaphore != null) {
                semaphore.release();
            }
            stats.computeIfAbsent(pluginCode, k -> new Stats()).rejected.incrementAndGet();
            return false;
        }
        return true;
    }

    /** 归还许可。只应在 {@link #acquire} 返回 {@code true} 后调用。 */
    public void release(String pluginCode) {
        requireCode(pluginCode);
        Semaphore semaphore = semaphores.get(pluginCode);
        if (semaphore != null) {
            semaphore.release();
        }
    }

    /**
     * 记录一次调用结果（观测用，不参与限流判定）。
     *
     * @param success 业务是否成功（被限流的调用不应计入失败）
     */
    public void record(String pluginCode, boolean success) {
        requireCode(pluginCode);
        Stats s = stats.computeIfAbsent(pluginCode, k -> new Stats());
        s.total.incrementAndGet();
        if (!success) {
            s.failed.incrementAndGet();
        }
    }

    /** @return 插件当前配额；未配置返回 {@link #UNLIMITED} */
    public Quota quotaOf(String pluginCode) {
        requireCode(pluginCode);
        return quotas.getOrDefault(pluginCode, UNLIMITED);
    }

    /** @return 插件当前可用许可数；未配置并发配额时返回 {@link Integer#MAX_VALUE} */
    public int availablePermits(String pluginCode) {
        requireCode(pluginCode);
        Semaphore semaphore = semaphores.get(pluginCode);
        return semaphore == null ? Integer.MAX_VALUE : semaphore.availablePermits();
    }

    /**
     * 快照统计（诊断与运营看板用）。
     */
    public Snapshot snapshot(String pluginCode) {
        requireCode(pluginCode);
        Stats s = stats.get(pluginCode);
        Quota q = quotaOf(pluginCode);
        return new Snapshot(
                pluginCode,
                q.maxConcurrent(),
                q.qps(),
                availablePermits(pluginCode),
                s == null ? 0 : s.total.get(),
                s == null ? 0 : s.failed.get(),
                s == null ? 0 : s.rejected.get());
    }

    /** 清空配额与统计（测试用）。 */
    public void reset() {
        quotas.clear();
        semaphores.clear();
        buckets.clear();
        stats.clear();
    }

    private static void requireCode(String pluginCode) {
        if (pluginCode == null || pluginCode.isBlank()) {
            throw new IllegalArgumentException("插件编码不能为空");
        }
    }

    /** 插件配额。 */
    public static class Quota {
        private final int maxConcurrent;
        private final int qps;

        /**
         * @param maxConcurrent 并发执行上限；{@code <=0} 表示不限
         * @param qps           每秒调用上限；{@code <=0} 表示不限
         */
        public Quota(int maxConcurrent, int qps) {
            this.maxConcurrent = Math.max(0, maxConcurrent);
            this.qps = Math.max(0, qps);
        }

        public int maxConcurrent() {
            return maxConcurrent;
        }

        public int qps() {
            return qps;
        }

        /** 是否完全不限。 */
        public boolean unlimited() {
            return maxConcurrent <= 0 && qps <= 0;
        }
    }

    /** 观测快照。 */
    public static class Snapshot {
        public final String pluginCode;
        public final int maxConcurrent;
        public final int qps;
        public final int availablePermits;
        public final long total;
        public final long failed;
        public final long rejected;

        public Snapshot(String pluginCode, int maxConcurrent, int qps, int availablePermits,
                        long total, long failed, long rejected) {
            this.pluginCode = pluginCode;
            this.maxConcurrent = maxConcurrent;
            this.qps = qps;
            this.availablePermits = availablePermits;
            this.total = total;
            this.failed = failed;
            this.rejected = rejected;
        }

        @Override
        public String toString() {
            return "plugin=" + pluginCode + " concurrent=" + maxConcurrent + " qps=" + qps
                    + " permits=" + availablePermits + " total=" + total
                    + " failed=" + failed + " rejected=" + rejected;
        }
    }

    private static class Stats {
        private final AtomicLong total = new AtomicLong();
        private final AtomicLong failed = new AtomicLong();
        private final AtomicLong rejected = new AtomicLong();
    }

    /** 平滑令牌桶：按纳秒补充令牌，避免固定窗口在边界处双倍放行。 */
    private static class TokenBucket {
        private final long capacity;
        private final long refillNanosPerToken;
        private long tokens;
        private long lastRefillNanos;

        TokenBucket(int qps) {
            this.capacity = Math.max(1, qps);
            this.refillNanosPerToken = 1_000_000_000L / Math.max(1, qps);
            this.tokens = this.capacity;
            this.lastRefillNanos = System.nanoTime();
        }

        synchronized boolean tryConsume() {
            refill();
            if (tokens <= 0) {
                return false;
            }
            tokens--;
            return true;
        }

        private void refill() {
            long now = System.nanoTime();
            long elapsed = now - lastRefillNanos;
            if (elapsed <= 0) {
                return;
            }
            long add = elapsed / refillNanosPerToken;
            if (add > 0) {
                tokens = Math.min(capacity, tokens + add);
                // 只按「已消耗的整段」推进时间，避免残留不足一个令牌的时间被丢弃
                lastRefillNanos += add * refillNanosPerToken;
            }
        }
    }
}
