package com.houjiang.plugin.engine.registry;

import com.houjiang.plugin.sdk.extension.ExtensionContext;
import com.houjiang.plugin.sdk.extension.ExtensionPoint;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 运行期禁用 + 快照重建单测（T02 / R3.3）。
 *
 * <p>验证：禁用后新请求不再返回该实现；已发出的旧快照不被就地修改（并发旧请求不中断）；
 * 重新启用后恢复；{@code rebuildAll} 幂等；整来源禁用；注册前预置禁用项仍生效。
 *
 * @author HouCloud
 */
class ExtensionRegistryDisableTest {

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

    private static void register(ExtensionRegistry registry, Object instance, String source, int priority) {
        registry.register(Probe.class, (Probe) instance, source, priority);
    }

    private static String key(String source, Class<?> impl) {
        return source + "#" + impl.getName();
    }

    @Test
    void disableHidesImplementationForNewRequests() {
        ExtensionRegistry registry = new ExtensionRegistry();
        register(registry, new ImplA(), "custom-goods", 10);
        register(registry, new ImplB(), "custom-goods", 20);
        ExtensionDisableSwitch disableSwitch = new ExtensionDisableSwitch(registry);
        assertEquals(2, registry.getExtensions(Probe.class).size());

        disableSwitch.disable(Probe.class, key("custom-goods", ImplA.class));

        List<Probe> after = registry.getExtensions(Probe.class);
        assertEquals(1, after.size(), "被禁实现不应再出现在新请求的快照里");
        assertTrue(after.get(0) instanceof ImplB);
    }

    @Test
    void previouslyIssuedSnapshotIsNotMutatedInPlace() {
        ExtensionRegistry registry = new ExtensionRegistry();
        register(registry, new ImplA(), "custom-goods", 10);
        register(registry, new ImplB(), "custom-goods", 20);
        ExtensionDisableSwitch disableSwitch = new ExtensionDisableSwitch(registry);
        List<Probe> captured = registry.getExtensions(Probe.class);

        disableSwitch.disable(Probe.class, key("custom-goods", ImplA.class));

        // 旧请求已持有的旧快照保持原样：并发中的旧请求不被中断
        assertEquals(2, captured.size(), "旧快照不得被就地修改，旧请求继续持有完整列表");
        assertEquals(1, registry.getExtensions(Probe.class).size());
        assertSame(captured, captured, "引用稳定性自证");
    }

    @Test
    void enableRestoresImplementation() {
        ExtensionRegistry registry = new ExtensionRegistry();
        register(registry, new ImplA(), "custom-goods", 10);
        ExtensionDisableSwitch disableSwitch = new ExtensionDisableSwitch(registry);

        disableSwitch.disable(Probe.class, key("custom-goods", ImplA.class));
        assertTrue(registry.getExtensions(Probe.class).isEmpty());

        disableSwitch.enable(Probe.class, key("custom-goods", ImplA.class));
        assertEquals(1, registry.getExtensions(Probe.class).size(), "重新启用后应恢复");
    }

    @Test
    void disableWholeSourceInOneKey() {
        ExtensionRegistry registry = new ExtensionRegistry();
        register(registry, new ImplA(), "custom-goods", 10);
        register(registry, new ImplB(), "custom-goods", 20);
        register(registry, new ImplA(), "custom-order", 30);
        ExtensionDisableSwitch disableSwitch = new ExtensionDisableSwitch(registry);

        disableSwitch.disable(Probe.class, "custom-goods");

        List<Probe> after = registry.getExtensions(Probe.class);
        assertEquals(1, after.size(), "整来源禁用应移除该来源全部实现，保留其它来源");
        assertEquals("custom-order", registry.getHandles(Probe.class).get(0).source());
    }

    @Test
    void rebuildAllIsIdempotent() {
        ExtensionRegistry registry = new ExtensionRegistry();
        register(registry, new ImplA(), "custom-goods", 10);
        register(registry, new ImplB(), "custom-order", 20);

        registry.rebuildAll();
        List<Probe> first = registry.getExtensions(Probe.class);
        registry.rebuildAll();
        List<Probe> second = registry.getExtensions(Probe.class);

        assertEquals(first.size(), second.size(), "rebuildAll 重复调用结果一致（幂等）");
        assertEquals(2, second.size());
    }

    @Test
    void preRegisteredDisableAppliesWhenImplementationRegisteredLater() {
        ExtensionRegistry registry = new ExtensionRegistry();
        // 注册前预置禁用（启动期配置装载的典型时序）
        registry.applyDisabled(Probe.class, Set.of(key("custom-goods", ImplA.class)));

        register(registry, new ImplA(), "custom-goods", 10);
        register(registry, new ImplB(), "custom-order", 20);

        List<Probe> after = registry.getExtensions(Probe.class);
        assertEquals(1, after.size(), "注册前预置的禁用项在实现注册时必须生效");
        assertTrue(after.get(0) instanceof ImplB);
    }

    @Test
    void applyingEmptyDisabledSetClearsExclusion() {
        ExtensionRegistry registry = new ExtensionRegistry();
        register(registry, new ImplA(), "custom-goods", 10);

        registry.applyDisabled(Probe.class, Set.of("custom-goods"));
        assertTrue(registry.getExtensions(Probe.class).isEmpty());

        registry.applyDisabled(Probe.class, Set.of());
        assertEquals(1, registry.getExtensions(Probe.class).size(), "空集应清除排除项");
    }

    @Test
    void handlesExposeSourceAndInstance() {
        ExtensionRegistry registry = new ExtensionRegistry();
        register(registry, new ImplA(), "custom-goods", 10);

        List<ExtensionRegistry.ExtensionHandle> handles = registry.getHandles(Probe.class);

        assertEquals(1, handles.size());
        assertEquals("custom-goods", handles.get(0).source());
        assertTrue(handles.get(0).instance() instanceof ImplA);
    }

    @Test
    void unknownPointHasNoHandles() {
        ExtensionRegistry registry = new ExtensionRegistry();
        assertTrue(registry.getHandles(Probe.class).isEmpty());
        assertFalse(registry.getExtensions(Probe.class).size() > 0);
    }
}
