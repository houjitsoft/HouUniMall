package com.houjiang.plugin.runtime;

import com.houjiang.plugin.api.host.PluginCapability;
import com.houjiang.plugin.api.host.PluginHostApi;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 最终版 §3.2：插件未安装/未启用时 Core 必须能降级，不得阻断主流程。
 */
class PluginCapabilityGatewayTest {

    /** 营销插件暴露的能力接口（测试用）。 */
    interface PromotionCapability {
        String name();
    }

    /** 可控的 PluginHostApi 替身。 */
    private static final class StubHostApi implements PluginHostApi {
        private final boolean active;
        private final Object capability;

        StubHostApi(boolean active, Object capability) {
            this.active = active;
            this.capability = capability;
        }

        @Override
        public boolean isActive(String pluginCode) {
            return active;
        }

        @Override
        public Optional<PluginCapability> discover(String pluginCode) {
            return Optional.empty();
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> Optional<T> capability(String pluginCode, Class<T> capabilityType) {
            if (!active || capability == null || !capabilityType.isInstance(capability)) {
                return Optional.empty();
            }
            return Optional.of((T) capability);
        }

        @Override
        public List<PluginCapability> activeCapabilities() {
            return new ArrayList<>();
        }
    }

    private static final PromotionCapability REAL = () -> "promotion";

    @Test
    void 插件未启用时isActive为false() {
        PluginCapabilityGateway gateway = new PluginCapabilityGateway(new StubHostApi(false, REAL));
        assertFalse(gateway.isActive("activity"));
    }

    @Test
    void 插件未安装时能力发现返回空而不是抛异常() {
        PluginCapabilityGateway gateway = new PluginCapabilityGateway(new StubHostApi(false, null));
        assertTrue(gateway.capability("activity", PromotionCapability.class).isEmpty());
    }

    @Test
    void 插件未安装时按兜底值降级() {
        PromotionCapability fallback = () -> "none";
        PluginCapabilityGateway gateway = new PluginCapabilityGateway(new StubHostApi(false, null));

        PromotionCapability result = gateway.capabilityOrElse("activity", PromotionCapability.class, fallback);

        assertSame(fallback, result);
        assertEquals("none", result.name());
    }

    @Test
    void 插件可用时返回真实能力而非兜底() {
        PluginCapabilityGateway gateway = new PluginCapabilityGateway(new StubHostApi(true, REAL));
        assertEquals("promotion", gateway.capabilityOrElse("activity", PromotionCapability.class, () -> "none").name());
    }

    @Test
    void 插件缺失时执行降级分支且不执行副作用() {
        PluginCapabilityGateway gateway = new PluginCapabilityGateway(new StubHostApi(false, null));
        AtomicBoolean actionRan = new AtomicBoolean(false);
        AtomicBoolean absentRan = new AtomicBoolean(false);

        gateway.ifPresent("activity", PromotionCapability.class,
                c -> actionRan.set(true),
                () -> absentRan.set(true));

        assertFalse(actionRan.get(), "插件缺失时不应执行插件副作用");
        assertTrue(absentRan.get(), "插件缺失时应执行降级分支");
    }

    @Test
    void 插件可用时执行副作用且不执行降级() {
        PluginCapabilityGateway gateway = new PluginCapabilityGateway(new StubHostApi(true, REAL));
        AtomicBoolean actionRan = new AtomicBoolean(false);
        AtomicBoolean absentRan = new AtomicBoolean(false);

        gateway.ifPresent("activity", PromotionCapability.class,
                c -> actionRan.set(true),
                () -> absentRan.set(true));

        assertTrue(actionRan.get());
        assertFalse(absentRan.get());
    }

    @Test
    void 缺少PluginHostApi时快速失败() {
        try {
            new PluginCapabilityGateway(null);
        } catch (NullPointerException e) {
            assertNotNull(e.getMessage());
        }
    }
}
