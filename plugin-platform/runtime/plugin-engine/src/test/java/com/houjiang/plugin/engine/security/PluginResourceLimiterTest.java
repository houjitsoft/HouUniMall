package com.houjiang.plugin.engine.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 插件资源配额。
 *
 * <p>重点验证两件事：① 未配置配额时不限制（保证引入本组件不会突然拒绝既有插件）；
 * ② 配额耗尽后必须能靠 {@code release} 完整归还，否则配额会被永久泄漏。
 *
 * @author HouCloud
 */
class PluginResourceLimiterTest {

    private PluginResourceLimiter limiter;

    @BeforeEach
    void setUp() {
        limiter = new PluginResourceLimiter();
    }

    @Test
    void 默认不配置配额时始终放行() {
        limiter.apply("erp");
        for (int i = 0; i < 100; i++) {
            assertTrue(limiter.acquire("erp"));
        }
        assertEquals(Integer.MAX_VALUE, limiter.availablePermits("erp"));
    }

    @Test
    void 并发配额耗尽后拒绝并计数() {
        limiter.configure("erp", new PluginResourceLimiter.Quota(2, 0));
        assertTrue(limiter.acquire("erp"));
        assertTrue(limiter.acquire("erp"));
        assertFalse(limiter.acquire("erp"), "第三个并发应被拒绝");
        assertEquals(1, limiter.snapshot("erp").rejected);
    }

    @Test
    void 归还后配额恢复不会泄漏() {
        limiter.configure("erp", new PluginResourceLimiter.Quota(1, 0));
        assertTrue(limiter.acquire("erp"));
        assertFalse(limiter.acquire("erp"));
        limiter.release("erp");
        assertTrue(limiter.acquire("erp"), "归还后应可再次获取，否则配额泄漏");
        limiter.release("erp");
        assertEquals(1, limiter.availablePermits("erp"));
    }

    @Test
    void QPS配额在窗口内耗尽后拒绝() {
        limiter.configure("erp", new PluginResourceLimiter.Quota(0, 1));
        assertTrue(limiter.acquire("erp"));
        assertFalse(limiter.acquire("erp"), "同一令牌窗口内第二次应被拒绝");
    }

    @Test
    void 并发与QPS同时配置时先拿信号量再消耗令牌() {
        limiter.configure("erp", new PluginResourceLimiter.Quota(1, 1));
        assertTrue(limiter.acquire("erp"));
        assertFalse(limiter.acquire("erp"));
        limiter.release("erp");
        // 信号量归还了，但令牌桶还没补充，仍应被拒（双重判定都生效）
        assertFalse(limiter.acquire("erp"));
    }

    @Test
    void 配额按插件隔离互不影响() {
        limiter.configure("erp", new PluginResourceLimiter.Quota(1, 0));
        limiter.configure("oa", new PluginResourceLimiter.Quota(1, 0));
        assertTrue(limiter.acquire("erp"));
        assertTrue(limiter.acquire("oa"), "不同插件的配额应各自独立");
    }

    @Test
    void 统计只累计真实调用不包含被拒次数() {
        limiter.configure("erp", new PluginResourceLimiter.Quota(0, 1));
        limiter.acquire("erp");
        limiter.record("erp", true);
        limiter.acquire("erp");
        limiter.record("erp", false);
        PluginResourceLimiter.Snapshot snapshot = limiter.snapshot("erp");
        assertEquals(2, snapshot.total);
        assertEquals(1, snapshot.failed);
        assertEquals(1, snapshot.rejected);
    }

    @Test
    void 空插件编码抛参数异常() {
        assertThrows(IllegalArgumentException.class, () -> limiter.apply(null));
        assertThrows(IllegalArgumentException.class, () -> limiter.apply("  "));
        assertThrows(IllegalArgumentException.class, () -> limiter.acquire(null));
    }

    @Test
    void 配额为null按不限制处理() {
        limiter.configure("erp", null);
        assertTrue(limiter.quotaOf("erp").unlimited());
        assertTrue(limiter.acquire("erp"));
    }

    @Test
    void 负数配额按不限制处理() {
        PluginResourceLimiter.Quota quota = new PluginResourceLimiter.Quota(-5, -5);
        assertTrue(quota.unlimited());
        assertEquals(0, quota.maxConcurrent());
        assertEquals(0, quota.qps());
    }

    @Test
    void 重置会清空配额与统计() {
        limiter.configure("erp", new PluginResourceLimiter.Quota(1, 0));
        limiter.acquire("erp");
        limiter.reset();
        assertTrue(limiter.acquire("erp"));
        assertEquals(0, limiter.snapshot("erp").total);
    }
}
