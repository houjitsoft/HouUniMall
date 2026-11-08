package com.houjiang.plugin.engine.registry;

import com.houjiang.plugin.engine.config.PluginEngineProperties;
import com.houjiang.plugin.sdk.extension.ExtensionContext;
import com.houjiang.plugin.sdk.extension.ExtensionPoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 扩展点调用模板的隔离能力测试。
 *
 * <p>锁定的四条硬约束：<b>超时不拖垮主链路</b>、<b>并发有界</b>、<b>异常不外泄</b>、
 * <b>中断语义与上下文传播不被线程隔离破坏</b>。
 *
 * @author HouCloud
 */
class ExtensionInvokerTest {

    private static final String WORKER_THREAD_PREFIX = "hou-plugin-ext-";

    private final ExtensionRegistry registry = new ExtensionRegistry();
    private final List<ExtensionInvoker> invokers = new ArrayList<>();

    @AfterEach
    void tearDown() {
        invokers.forEach(ExtensionInvoker::shutdown);
        SecurityContextHolder.clearContext();
    }

    @Test
    void invokeExecutesSynchronouslyAndPublishesContextResult() {
        ProbeContext ctx = new ProbeContext();
        registry.register(ProbeExtension.class, (ProbeExtension) c -> c.record("ran"), "demo", 0);
        ExtensionInvoker invoker = invoker(200, 8);

        invoker.invoke(ProbeExtension.class, ctx);

        // 线程隔离不得破坏同步语义：invoke 返回时扩展的写入必须已可见
        assertEquals(List.of("ran"), ctx.log());
        assertTrue(invoker.degradedCounters().isEmpty(), "正常执行不应产生降级计数");
    }

    @Test
    void invokeSkipsBlockingExtensionOnTimeoutAndKeepsMainFlow() {
        ProbeContext ctx = new ProbeContext();
        registry.register(ProbeExtension.class, (ProbeExtension) c -> {
            try {
                Thread.sleep(5_000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            c.record("blocking");
        }, "demo", 0);
        registry.register(ProbeExtension.class, (ProbeExtension) c -> c.record("after"), "demo", 10);
        ExtensionInvoker invoker = invoker(50, 8);

        long start = System.nanoTime();
        invoker.invoke(ProbeExtension.class, ctx);
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        assertTrue(elapsedMs < 2_000, "超时必须及时返回，实际 " + elapsedMs + "ms");
        assertEquals(List.of("after"), ctx.log(), "阻塞的扩展应被跳过，后续扩展继续执行");
        assertFalse(ctx.isAborted(), "超时属于失败降级，不得误判为业务中断");

        Map<String, Long> counters = invoker.degradedCounters();
        assertEquals(1, counters.size(), "应记录一条超时计数，实际=" + counters);
        assertTrue(counters.keySet().iterator().next().startsWith("timeout:"),
                "计数 key 应标识超时的扩展实现，实际=" + counters);
    }

    @Test
    void invokeIsolatesFailingExtensionAndContinues() {
        ProbeContext ctx = new ProbeContext();
        registry.register(ProbeExtension.class, (ProbeExtension) c -> {
            throw new IllegalStateException("插件炸了");
        }, "demo", 0);
        registry.register(ProbeExtension.class, (ProbeExtension) c -> c.record("survived"), "demo", 10);
        ExtensionInvoker invoker = invoker(500, 8);

        invoker.invoke(ProbeExtension.class, ctx);

        assertEquals(List.of("survived"), ctx.log(), "插件异常不得向主流程传播");
    }

    @Test
    void invokeStopsRemainingExtensionsAfterAbort() {
        ProbeContext ctx = new ProbeContext();
        registry.register(ProbeExtension.class, (ProbeExtension) c -> c.abort("超限"), "demo", 0);
        registry.register(ProbeExtension.class, (ProbeExtension) c -> c.record("must-not-run"), "demo", 10);
        ExtensionInvoker invoker = invoker(500, 8);

        invoker.invoke(ProbeExtension.class, ctx);

        assertTrue(ctx.isAborted());
        assertTrue(ctx.log().isEmpty(), "中断后不得再执行后续扩展点");
    }

    @Test
    void invokePropagatesAuthenticationIntoIsolatedThread() {
        ProbeContext ctx = new ProbeContext();
        registry.register(ProbeExtension.class, (ProbeExtension) c -> {
            boolean authenticated =
                    SecurityContextHolder.getContext().getAuthentication() != null;
            c.record(authenticated ? "auth:present" : "auth:missing");
            c.record(Thread.currentThread().getName().startsWith(WORKER_THREAD_PREFIX)
                    ? "thread:isolated" : "thread:caller");
        }, "demo", 0);
        ExtensionInvoker invoker = invoker(500, 8);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken("tester", "n/a", List.of()));
        SecurityContextHolder.setContext(context);
        try {
            invoker.invoke(ProbeExtension.class, ctx);
        } finally {
            SecurityContextHolder.clearContext();
        }

        assertEquals(List.of("auth:present", "thread:isolated"), ctx.log(),
                "插件在隔离线程上仍须能读到当前登录态（上下文必须显式传播）");
    }

    @Test
    void invokeBoundsWorkerThreadsUnderConcurrentBlockingCalls() throws Exception {
        int maxConcurrent = 2;
        int concurrentCalls = 6;
        Set<String> workerThreads = ConcurrentHashMap.newKeySet();
        AtomicInteger started = new AtomicInteger();
        registry.register(ProbeExtension.class, (ProbeExtension) c -> {
            started.incrementAndGet();
            workerThreads.add(Thread.currentThread().getName());
            try {
                Thread.sleep(5_000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "demo", 0);
        ExtensionInvoker invoker = invoker(80, maxConcurrent);

        CountDownLatch done = new CountDownLatch(concurrentCalls);
        for (int i = 0; i < concurrentCalls; i++) {
            Thread caller = new Thread(() -> {
                try {
                    invoker.invoke(ProbeExtension.class, new ProbeContext());
                } finally {
                    done.countDown();
                }
            }, "caller-" + i);
            caller.setDaemon(true);
            caller.start();
        }

        assertTrue(done.await(10, TimeUnit.SECONDS), "并发调用必须在超时窗口内全部返回");

        // bulkhead 不变量：工作线程数不超过 extensionMaxConcurrent，
        // 插件阻塞不会让宿主线程数随并发量线性增长
        assertTrue(workerThreads.size() <= maxConcurrent,
                "工作线程数应 <= " + maxConcurrent + "，实际=" + workerThreads);
        assertTrue(started.get() >= 1, "至少应有一个扩展开始执行");
    }

    @Test
    void invokeRunsInlineWhenTimeoutDisabled() {
        ProbeContext ctx = new ProbeContext();
        registry.register(ProbeExtension.class, (ProbeExtension) c -> c.record(
                Thread.currentThread().getName().startsWith(WORKER_THREAD_PREFIX)
                        ? "thread:isolated" : "thread:caller"), "demo", 0);
        ExtensionInvoker invoker = invoker(0, 8);

        invoker.invoke(ProbeExtension.class, ctx);

        assertEquals(List.of("thread:caller"), ctx.log(),
                "extensionTimeoutMs<=0 时应退化为同线程直调（嵌入方/单测的显式选择）");
    }

    @Test
    void invokeIsNoOpWhenEngineDisabled() {
        ProbeContext ctx = new ProbeContext();
        registry.register(ProbeExtension.class, (ProbeExtension) c -> c.record("ran"), "demo", 0);
        PluginEngineProperties properties = new PluginEngineProperties();
        properties.setEnabled(false);
        ExtensionInvoker invoker = new ExtensionInvoker(registry, properties);
        invokers.add(invoker);

        invoker.invoke(ProbeExtension.class, ctx);

        assertTrue(ctx.log().isEmpty(), "引擎关闭时不得执行任何扩展点");
    }

    private ExtensionInvoker invoker(long timeoutMs, int maxConcurrent) {
        PluginEngineProperties properties = new PluginEngineProperties();
        properties.setExtensionTimeoutMs(timeoutMs);
        properties.setExtensionMaxConcurrent(maxConcurrent);
        ExtensionInvoker invoker = new ExtensionInvoker(registry, properties);
        invokers.add(invoker);
        return invoker;
    }

    /** 测试用扩展点：与真实扩展点同构，只为断言提供记录能力。 */
    interface ProbeExtension extends ExtensionPoint<ProbeContext> {
    }

    /** 测试用上下文：记录扩展执行轨迹。 */
    static final class ProbeContext extends ExtensionContext {

        private final List<String> trace = new ArrayList<>();

        void record(String entry) {
            trace.add(entry);
        }

        List<String> log() {
            return List.copyOf(trace);
        }
    }
}
