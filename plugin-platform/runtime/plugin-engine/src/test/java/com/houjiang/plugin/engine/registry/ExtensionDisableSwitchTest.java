package com.houjiang.plugin.engine.registry;

import com.houjiang.plugin.sdk.extension.ExtensionContext;
import com.houjiang.plugin.sdk.extension.ExtensionPoint;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 运行期禁用开关单测（T02 / R3.3）：内存态、可插拔装载源、即刻生效、幂等。
 *
 * @author HouCloud
 */
class ExtensionDisableSwitchTest {

    interface Probe extends ExtensionPoint<ExtensionContext> {
    }

    static final class ImplA implements Probe {
        @Override
        public void execute(ExtensionContext context) {
        }
    }

    static final class ImplB implements Probe {
        @Override
        public void execute(ExtensionContext context) {
        }
    }

    private static String key(String source, Class<?> impl) {
        return source + "#" + impl.getName();
    }

    @Test
    void disableAndEnableAreImmediateAndTracked() {
        ExtensionRegistry registry = new ExtensionRegistry();
        registry.register(Probe.class, new ImplA(), "custom-goods", 10);
        ExtensionDisableSwitch disableSwitch = new ExtensionDisableSwitch(registry);

        disableSwitch.disable(Probe.class, key("custom-goods", ImplA.class));
        assertTrue(disableSwitch.isDisabled(Probe.class, key("custom-goods", ImplA.class)));
        assertEquals(Set.of(key("custom-goods", ImplA.class)), disableSwitch.disabledKeys(Probe.class));
        assertTrue(registry.getExtensions(Probe.class).isEmpty(), "禁用应即刻反映到快照");

        disableSwitch.enable(Probe.class, key("custom-goods", ImplA.class));
        assertFalse(disableSwitch.isDisabled(Probe.class, key("custom-goods", ImplA.class)));
        assertEquals(1, registry.getExtensions(Probe.class).size());
    }

    @Test
    void configSourceIsLoadedAndIdempotent() {
        ExtensionRegistry registry = new ExtensionRegistry();
        registry.register(Probe.class, new ImplA(), "custom-goods", 10);
        registry.register(Probe.class, new ImplB(), "custom-goods", 20);
        ExtensionDisableSwitch disableSwitch = new ExtensionDisableSwitch(registry);
        disableSwitch.addSource(ExtensionDisableSwitch.ofList(List.of("Probe#custom-goods")));

        disableSwitch.refreshFromSources();
        disableSwitch.refreshFromSources();

        assertEquals(Set.of("custom-goods"), disableSwitch.disabledKeys(Probe.class),
                "重复装载不得产生重复项");
        assertTrue(registry.getExtensions(Probe.class).isEmpty(),
                "配置装载 pointShortName#source 应移除该来源全部实现");
    }

    @Test
    void pluggableSourceSupportsDynamicReload() {
        ExtensionRegistry registry = new ExtensionRegistry();
        registry.register(Probe.class, new ImplA(), "custom-goods", 10);
        ExtensionDisableSwitch disableSwitch = new ExtensionDisableSwitch(registry);
        AtomicInteger loads = new AtomicInteger();
        disableSwitch.addSource(() -> {
            loads.incrementAndGet();
            return List.of("Probe#" + key("custom-goods", ImplA.class));
        });

        disableSwitch.refreshFromSources();

        assertEquals(1, loads.get(), "装载源应被调用一次");
        assertTrue(registry.getExtensions(Probe.class).isEmpty());
    }

    @Test
    void malformedConfigEntryFailsFast() {
        ExtensionDisableSwitch disableSwitch = new ExtensionDisableSwitch();
        disableSwitch.addSource(ExtensionDisableSwitch.ofList(List.of("no-separator-entry")));

        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class,
                disableSwitch::refreshFromSources,
                "缺少 # 分隔符的禁用项应 fail-fast 而非静默忽略");
    }

    @Test
    void disabledKeysSnapshotIsImmutable() {
        ExtensionDisableSwitch disableSwitch = new ExtensionDisableSwitch();
        disableSwitch.disable(Probe.class, "custom-goods");

        org.junit.jupiter.api.Assertions.assertThrows(UnsupportedOperationException.class,
                () -> disableSwitch.disabledKeys(Probe.class).add("x"));
    }
}
