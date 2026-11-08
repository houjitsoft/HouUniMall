package com.houjiang.plugin.engine.registry;

import com.houjiang.plugin.sdk.extension.ExtensionContext;
import com.houjiang.plugin.sdk.extension.ExtensionPoint;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 健康跟踪 / 熔断摘除单测（T01 / R3.1）。
 *
 * <p>验证：连续 N 次失败后经快照重建摘除（{@code getExtensions} 不再返回）；中途成功清零计数；
 * 手动 {@code reset} 恢复；阈值 &le;0 关闭熔断。
 *
 * @author HouCloud
 */
class ExtensionHealthTrackerTest {

    interface Probe extends ExtensionPoint<ExtensionContext> {
    }

    static final class Impl implements Probe {
        @Override
        public void execute(ExtensionContext context) {
        }
    }

    private static String key(Class<?> impl) {
        return "custom-goods#" + impl.getName();
    }

    @Test
    void consecutiveFailuresRemoveImplementation() {
        ExtensionRegistry registry = new ExtensionRegistry();
        registry.register(Probe.class, new Impl(), "custom-goods", 10);
        ExtensionDisableSwitch disableSwitch = new ExtensionDisableSwitch(registry);
        ExtensionHealthTracker tracker = new ExtensionHealthTracker(disableSwitch, 3);

        assertFalse(tracker.recordFailure(Probe.class, key(Impl.class)), "第 1 次未达阈值");
        assertFalse(tracker.recordFailure(Probe.class, key(Impl.class)), "第 2 次未达阈值");
        assertTrue(tracker.recordFailure(Probe.class, key(Impl.class)), "第 3 次达到阈值应摘除");

        assertFalse(tracker.isHealthy(Probe.class, key(Impl.class)));
        assertTrue(registry.getExtensions(Probe.class).isEmpty(),
                "达到阈值后 getExtensions 不应再返回该实现");
    }

    @Test
    void resetRestoresAfterRemoval() {
        ExtensionRegistry registry = new ExtensionRegistry();
        registry.register(Probe.class, new Impl(), "custom-goods", 10);
        ExtensionDisableSwitch disableSwitch = new ExtensionDisableSwitch(registry);
        ExtensionHealthTracker tracker = new ExtensionHealthTracker(disableSwitch, 2);

        tracker.recordFailure(Probe.class, key(Impl.class));
        tracker.recordFailure(Probe.class, key(Impl.class));
        assertTrue(registry.getExtensions(Probe.class).isEmpty());

        tracker.reset(Probe.class, key(Impl.class));

        assertTrue(tracker.isHealthy(Probe.class, key(Impl.class)), "reset 后应恢复健康");
        assertEquals(1, registry.getExtensions(Probe.class).size(), "reset 后应重新出现在快照中");
        assertEquals(0, tracker.failureCount(Probe.class, key(Impl.class)));
    }

    @Test
    void successResetsConsecutiveFailureCount() {
        ExtensionRegistry registry = new ExtensionRegistry();
        registry.register(Probe.class, new Impl(), "custom-goods", 10);
        ExtensionDisableSwitch disableSwitch = new ExtensionDisableSwitch(registry);
        ExtensionHealthTracker tracker = new ExtensionHealthTracker(disableSwitch, 3);

        tracker.recordFailure(Probe.class, key(Impl.class));
        tracker.recordFailure(Probe.class, key(Impl.class));
        tracker.recordSuccess(Probe.class, key(Impl.class));

        assertFalse(tracker.recordFailure(Probe.class, key(Impl.class)),
                "成功应清零连续失败计数，此后重新累计");
        assertEquals(1, tracker.failureCount(Probe.class, key(Impl.class)));
        assertEquals(1, registry.getExtensions(Probe.class).size(), "未达阈值不应摘除");
    }

    @Test
    void nonPositiveThresholdDisablesCircuitBreaker() {
        ExtensionRegistry registry = new ExtensionRegistry();
        registry.register(Probe.class, new Impl(), "custom-goods", 10);
        ExtensionDisableSwitch disableSwitch = new ExtensionDisableSwitch(registry);
        ExtensionHealthTracker tracker = new ExtensionHealthTracker(disableSwitch, 0);

        for (int i = 0; i < 100; i++) {
            assertFalse(tracker.recordFailure(Probe.class, key(Impl.class)));
        }
        assertEquals(1, registry.getExtensions(Probe.class).size(), "阈值<=0 时熔断关闭，始终不摘除");
    }
}
