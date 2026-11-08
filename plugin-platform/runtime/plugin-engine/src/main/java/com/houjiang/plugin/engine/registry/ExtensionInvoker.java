package com.houjiang.plugin.engine.registry;

import com.houjiang.plugin.engine.config.ExtensionDegradePolicy;
import com.houjiang.plugin.engine.config.ExtensionPolicyRegistry;
import com.houjiang.plugin.engine.config.PluginEngineProperties;
import com.houjiang.plugin.engine.observability.ExtensionMetrics;
import com.houjiang.plugin.sdk.extension.ExtensionContext;
import com.houjiang.plugin.sdk.extension.ExtensionMode;
import com.houjiang.plugin.sdk.extension.ExtensionPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

/**
 * 扩展点统一调用模板（方案 4.7 + R3.1/R3.2/R3.4 加固）。
 *
 * <p>所有扩展点都必须走本方法，保证六条硬约束：
 * <ol>
 *   <li>异常隔离：插件抛任何异常都记日志 + 埋点 + 计数，不向主流程传播</li>
 *   <li>真实超时：单扩展点默认 200ms（{@code houcloud.plugin.extension-timeout-ms}），
 *       超时中断并跳过，不占用主链路线程</li>
 *   <li>并发隔离（bulkhead）：扩展点执行走独立有界线程池
 *       （{@code houcloud.plugin.extension-max-concurrent}），队列打满时拒绝并降级跳过，
 *       插件阻塞不会吃光宿主线程</li>
 *   <li>降级策略：故障（异常 / 超时 / 被拒绝）<b>默认 IGNORE</b>（记录并继续）；当注入
 *       {@link ExtensionPolicyRegistry} 且该点策略为 {@code ABORT} 时 fail-closed，抛引擎内部
 *       {@link ExtensionFaultException} 中断主链路（由宿主网关转译为业务错误码）。
 *       <b>注意</b>：策略表并非"只有锁定集才 ABORT"——按 {@link ExtensionPolicyRegistry} 内置表，
 *       11 个点为 {@code ABORT}（含非锁定集的 {@code CartValidate}），{@code PromotionCalc}/{@code FreightCalc}
 *       为 {@code FALLBACK}，4 个旁路点为 {@code IGNORE}，<b>未知点默认 ABORT</b>（fail-closed）。
 *       切勿据本节误判某点"故障会被静默吞掉"。</li>
 *   <li>中断语义：{@code ctx.isAborted()} 后停止后续扩展点（业务中断，非故障）</li>
 *   <li>可观测 / 熔断：逐次 {@link ExtensionMetrics} 记录；连续失败达阈值经
 *       {@link ExtensionHealthTracker} 摘除该实现</li>
 * </ol>
 *
 * <p><b>职责边界（契约）</b>：<b>本类是降级策略判定的唯一归宿</b>——决定一次故障是
 * 「propagate（{@code ABORT} 抛 {@link ExtensionFaultException}）」还是「swallow（{@code IGNORE}/{@code FALLBACK}
 * 记录并继续）」。本类<b>只抛引擎内部异常 {@link ExtensionFaultException}</b>，
 * <b>绝不构造业务异常、绝不引用业务错误码</b>（业务码 7001/7002 的构造与转译归宿主网关）。
 * 「无策略表」这一状态<b>仅存在于单测便捷构造器</b>{@link #ExtensionInvoker(ExtensionRegistry, PluginEngineProperties)}；
 * <b>生产路径必须注入策略表</b>，此时未知点按 {@link ExtensionPolicyRegistry} 默认 {@code ABORT}（fail-closed）。
 *
 * <p><b>线程模型</b>：扩展实现运行在引擎自有的守护线程上，因此<b>不得依赖调用线程绑定的事务</b>
 * （{@code TransactionSynchronizationManager} 的连接不会带过去）。需要写库的插件能力应通过
 * 宿主 API（{@code PluginHostApi}）调用，而不是在扩展实现里直接操作 Mapper。
 * {@code SecurityContext} 与 {@code RequestAttributes} 会被显式传播，插件读取当前登录态不受影响。
 *
 * <p><b>向后兼容与生产约束</b>：{@link #ExtensionInvoker(ExtensionRegistry, PluginEngineProperties)}
 * 便捷构造器<b>不注入策略表 / 健康跟踪</b>（此时故障一律 IGNORE、不抛），<b>仅供单测与嵌入方</b>；
 * <b>生产路径必须使用完整构造器并注入 {@link ExtensionPolicyRegistry}</b>
 * （{@code PluginEngineAutoConfiguration} 已如此装配），否则锁定集将失去 fail-closed 保护。
 * {@code extensionTimeoutMs <= 0} 时退化为原「同线程直调」行为（无超时、无隔离）。
 *
 * @author HouCloud
 */
public final class ExtensionInvoker {

    private static final Logger log = LoggerFactory.getLogger(ExtensionInvoker.class);

    private final ExtensionRegistry registry;
    private final PluginEngineProperties properties;

    /** 降级策略表；为 {@code null} 时故障一律 IGNORE（不抛）。 */
    private final ExtensionPolicyRegistry policyRegistry;

    /** 健康跟踪（熔断摘除）；为 {@code null} 时不做熔断。 */
    private final ExtensionHealthTracker healthTracker;

    /** 可观测。 */
    private final ExtensionMetrics metrics;

    /** 扩展点执行池：懒创建，无扩展点时永不初始化 */
    private volatile ExecutorService executor;

    /** 降级计数：key=扩展实现类名，便于定位是哪个插件的实现拖慢核心链路 */
    private final ConcurrentMap<String, LongAdder> timeoutCounters = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, LongAdder> rejectionCounters = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, LongAdder> errorCounters = new ConcurrentHashMap<>();

    /** 便捷构造器（单测/嵌入方）：无策略表、无健康跟踪，故障一律 IGNORE。 */
    public ExtensionInvoker(ExtensionRegistry registry, PluginEngineProperties properties) {
        this(registry, properties, null, null, null);
    }

    /** 完整构造器：由自动装配注入共享的策略表 / 健康跟踪 / 指标。 */
    public ExtensionInvoker(ExtensionRegistry registry,
                            PluginEngineProperties properties,
                            ExtensionPolicyRegistry policyRegistry,
                            ExtensionHealthTracker healthTracker,
                            ExtensionMetrics metrics) {
        this.registry = registry;
        this.properties = properties;
        this.policyRegistry = policyRegistry;
        this.healthTracker = healthTracker;
        this.metrics = metrics != null ? metrics : new ExtensionMetrics();
    }

    public <E extends ExtensionPoint<C>, C extends ExtensionContext> void invoke(Class<E> type, C ctx) {
        if (!properties.isEnabled()) {
            return;
        }
        List<ExtensionRegistry.ExtensionHandle> handles = registry.getHandles(type);
        if (handles.isEmpty()) {
            return;
        }
        long timeoutMs = properties.getExtensionTimeoutMs();
        String pointName = shortPointName(type);
        for (ExtensionRegistry.ExtensionHandle handle : handles) {
            @SuppressWarnings("unchecked")
            E ext = (E) handle.instance();
            String source = handle.source();
            ExtensionMode mode = handle.mode();
            String key = ExtensionPointKeys.implementationKey(source, ext);

            // 已被禁用/熔断摘除的实现（快照切换竞态下仍可能出现）：记录 disabled 并跳过
            if (healthTracker != null && !healthTracker.isHealthy(type, key)) {
                metrics.record(source, pointName, mode, 0L, "disabled");
                continue;
            }

            long start = System.nanoTime();
            Fault fault;
            try {
                fault = runGuarded(type, ext, ctx, timeoutMs);
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                log.warn("扩展点 {} 调用被中断，已取消后续扩展点执行", type.getSimpleName());
                break;
            }
            long costMs = millisSince(start);

            if (fault != null) {
                metrics.record(source, pointName, mode, costMs, fault.result());
                if (healthTracker != null) {
                    healthTracker.recordFailure(type, key);
                }
                // 锁定集（ABORT）fail-closed：抛故障异常中断主链路，由宿主网关转译业务错误码
                if (policyRegistry != null
                        && policyRegistry.policyOf(type) == ExtensionDegradePolicy.ABORT) {
                    throw new ExtensionFaultException(pointName, source, fault.message(), fault.cause());
                }
                continue;
            }

            if (ctx.isAborted()) {
                metrics.record(source, pointName, mode, costMs, "abort");
                if (healthTracker != null) {
                    healthTracker.recordSuccess(type, key);
                }
                break;
            }
            metrics.record(source, pointName, mode, costMs, "ok");
            if (healthTracker != null) {
                healthTracker.recordSuccess(type, key);
            }
        }
    }

    /**
     * 执行单个扩展点并施加超时与并发隔离。
     *
     * @return {@code null} 表示执行成功；否则返回故障描述（已记日志与计数）
     * @throws InterruptedException 调用方线程被中断（由 {@link #invoke} 处理为停止后续扩展点）
     */
    private <E extends ExtensionPoint<C>, C extends ExtensionContext> Fault runGuarded(Class<E> type, E ext,
                                                                                        C ctx, long timeoutMs)
            throws InterruptedException {
        if (timeoutMs <= 0 || !properties.getExecutor().isEnabled()) {
            return executeInline(type, ext, ctx);
        }
        ExecutorService pool;
        try {
            pool = executor();
        } catch (RuntimeException e) {
            log.warn("扩展点隔离线程池不可用，本次改为同线程直调：{}", e.getMessage());
            return executeInline(type, ext, ctx);
        }

        SecurityContext securityContext = SecurityContextHolder.getContext();
        RequestAttributes requestAttributes = currentRequestAttributes();
        Future<?> future;
        try {
            future = pool.submit(() -> {
                applyContext(securityContext, requestAttributes);
                try {
                    ext.execute(ctx);
                } finally {
                    clearContext();
                }
            });
        } catch (RejectedExecutionException e) {
            recordRejection(ext);
            String message = describeFault(type, ext,
                    "被并发隔离池拒绝（插件过载或阻塞），本次调用已降级跳过");
            log.error("{}；所属插件需排查其扩展实现是否长时间阻塞", message);
            return new Fault("error", message, e);
        }

        try {
            future.get(timeoutMs, TimeUnit.MILLISECONDS);
            return null;
        } catch (TimeoutException e) {
            future.cancel(true);
            recordTimeout(ext);
            String message = describeFault(type, ext, "执行超时（>" + timeoutMs + "ms），已中断并跳过");
            log.error("{}；所属插件需排查其扩展实现是否阻塞", message);
            return new Fault("timeout", message, e);
        } catch (ExecutionException e) {
            Throwable cause = rootCause(e);
            recordError(ext);
            String message = describeFault(type, ext, "执行失败: " + cause);
            log.error("插件扩展点 {} 执行失败，已隔离", type.getSimpleName(), cause);
            return new Fault("error", message, cause);
        } catch (InterruptedException e) {
            future.cancel(true);
            throw e;
        }
    }

    /** 同线程直调：{@code extensionTimeoutMs <= 0} 或隔离池关闭时的兼容路径。 */
    private <E extends ExtensionPoint<C>, C extends ExtensionContext> Fault executeInline(Class<E> type, E ext, C ctx) {
        try {
            ext.execute(ctx);
            return null;
        } catch (Throwable t) {
            recordError(ext);
            String message = describeFault(type, ext, "执行失败: " + t);
            log.error("插件扩展点 {} 执行失败，已隔离", type.getSimpleName(), t);
            return new Fault("error", message, t);
        }
    }

    private static String describeFault(Class<?> type, ExtensionPoint<?> ext, String detail) {
        return "扩展点 " + type.getName() + " 的实现 " + ext.getClass().getName() + " " + detail;
    }

    private static String shortPointName(Class<?> type) {
        return ExtensionPointKeys.shortName(type);
    }

    private ExecutorService executor() {
        ExecutorService current = executor;
        if (current != null) {
            return current;
        }
        synchronized (this) {
            if (executor == null) {
                // 线程数用配置的「生效上限」：extensionMaxConcurrent 显式覆盖，否则回落到 executor.maxSize
                int max = Math.max(1, properties.effectiveMaxConcurrent());
                int queueCapacity = Math.max(1, properties.getExecutor().getQueueCapacity());
                ThreadPoolExecutor pool = new ThreadPoolExecutor(
                        Math.min(properties.getExecutor().getCoreSize(), max), max,
                        60L, TimeUnit.SECONDS,
                        new ArrayBlockingQueue<>(queueCapacity),
                        new ExtensionThreadFactory(),
                        new ThreadPoolExecutor.AbortPolicy());
                // 空闲核心线程可回收，避免宿主长期空转
                pool.allowCoreThreadTimeOut(true);
                executor = pool;
                log.info("扩展点隔离线程池已启用：maxConcurrent={}, queueCapacity={}, timeoutMs={}",
                        max, queueCapacity, properties.getExtensionTimeoutMs());
            }
            return executor;
        }
    }

    /** 关闭隔离线程池（宿主停机或测试收尾）。 */
    public void shutdown() {
        ExecutorService pool = executor;
        if (pool != null) {
            pool.shutdownNow();
        }
    }

    /**
     * 扩展点降级计数快照（超时 / 被拒绝 / 异常），供监控与排障。
     *
     * @return key 形如 {@code timeout:com.houjiang.xxx.MyExtension}，value 为累计次数
     */
    public Map<String, Long> degradedCounters() {
        Map<String, Long> snapshot = new LinkedHashMap<>();
        timeoutCounters.forEach((key, value) -> snapshot.put("timeout:" + key, value.sum()));
        rejectionCounters.forEach((key, value) -> snapshot.put("rejected:" + key, value.sum()));
        errorCounters.forEach((key, value) -> snapshot.put("error:" + key, value.sum()));
        return snapshot;
    }

    private void recordTimeout(ExtensionPoint<?> ext) {
        timeoutCounters.computeIfAbsent(ext.getClass().getName(), key -> new LongAdder()).increment();
    }

    private void recordRejection(ExtensionPoint<?> ext) {
        rejectionCounters.computeIfAbsent(ext.getClass().getName(), key -> new LongAdder()).increment();
    }

    private void recordError(ExtensionPoint<?> ext) {
        errorCounters.computeIfAbsent(ext.getClass().getName(), key -> new LongAdder()).increment();
    }

    private static long millisSince(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000L;
    }

    private static Throwable rootCause(ExecutionException e) {
        return e.getCause() == null ? e : e.getCause();
    }

    private static RequestAttributes currentRequestAttributes() {
        try {
            return RequestContextHolder.getRequestAttributes();
        } catch (RuntimeException e) {
            // 非 Web 上下文（如启动期/后台线程）没有请求作用域，正常情况
            return null;
        }
    }

    private static void applyContext(SecurityContext securityContext, RequestAttributes requestAttributes) {
        if (securityContext != null) {
            SecurityContextHolder.setContext(securityContext);
        }
        if (requestAttributes != null) {
            RequestContextHolder.setRequestAttributes(requestAttributes);
        }
    }

    private static void clearContext() {
        SecurityContextHolder.clearContext();
        RequestContextHolder.resetRequestAttributes();
    }

    /** 单次扩展执行的故障描述（异常 / 超时 / 被拒绝）。 */
    private record Fault(String result, String message, Throwable cause) {
    }

    /** 守护线程工厂：线程名带序号，便于线上 jstack 定位插件扩展调用。 */
    private static final class ExtensionThreadFactory implements ThreadFactory {

        private final AtomicInteger sequence = new AtomicInteger();

        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable, "hou-plugin-ext-" + sequence.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        }
    }
}
