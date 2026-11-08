package com.houjiang.plugin.engine.host;

import com.houjiang.plugin.api.host.PluginHostApi;
import com.houjiang.plugin.api.host.PluginCapability;
import com.houjiang.plugin.engine.core.PluginState;
import com.houjiang.plugin.engine.registry.PluginRegistry;
import com.houjiang.plugin.sdk.model.PluginManifest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.pf4j.DefaultPluginManager;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

/**
 * 可选能力发现与降级测试（闭坑清单 P0「Core 失去独立运行」）。
 *
 * <p>核心断言：插件未安装 / 未启用 / 反射失败时，{@link PluginHostApi} 一律返回空且<b>不抛异常</b>。
 * 降级路径一旦抛异常，「插件缺失」就会变成「Core 不可用」，这正是要防的事故。
 */
class DefaultPluginHostApiTest {

    /** 测试用能力接口，模拟 webpos 收银台登录能力。 */
    interface WebPosLoginCapability {
        String login(String username);
    }

    private PluginRegistry registry;
    private DefaultPluginManager pluginManager;
    private DefaultPluginHostApi host;

    @BeforeEach
    void setUp() {
        registry = new PluginRegistry();
        pluginManager = Mockito.mock(DefaultPluginManager.class);
        host = new DefaultPluginHostApi(registry, pluginManager);
    }

    private PluginManifest manifest(String code, String version, String license) {
        PluginManifest m = new PluginManifest();
        m.setCode(code);
        m.setVersion(version);
        m.setLicense(license);
        return m;
    }

    @Test
    void standaloneUsesTrustedServiceDiscovery() {
        PluginManifest manifest = manifest("webpos", "1.0.0", "FREE");
        manifest.setRuntime("standalone");
        manifest.setHostService("webpos");
        registry.put("webpos", PluginState.ACTIVE, manifest);
        StandaloneCapabilityClient client = Mockito.mock(StandaloneCapabilityClient.class);
        WebPosLoginCapability capability = username -> username;
        Mockito.when(client.createProxy("http://discovered-webpos:9000", WebPosLoginCapability.class))
                .thenReturn(capability);
        DefaultPluginHostApi api = new DefaultPluginHostApi(registry, pluginManager, client, service -> {
            assertEquals("webpos", service);
            return "http://discovered-webpos:9000";
        });
        assertEquals(capability, api.capability("webpos", WebPosLoginCapability.class).orElseThrow());
        Mockito.verifyNoInteractions(pluginManager);
    }

    @Test
    void standaloneWithoutEndpointNeverFallsBackToPf4j() {
        PluginManifest manifest = manifest("webpos", "1.0.0", "FREE");
        manifest.setRuntime("standalone");
        manifest.setHostService("webpos");
        registry.put("webpos", PluginState.ACTIVE, manifest);
        assertTrue(host.capability("webpos", WebPosLoginCapability.class).isEmpty());
        Mockito.verifyNoInteractions(pluginManager);
    }

    @Test
    void standaloneCannotUseManifestUrlAsRemoteEndpoint() {
        PluginManifest manifest = manifest("webpos", "1.0.0", "FREE");
        manifest.setRuntime("standalone");
        manifest.setHostService("webpos");
        manifest.setHost("http://127.0.0.1:8320");
        registry.put("webpos", PluginState.ACTIVE, manifest);
        assertTrue(host.capability("webpos", WebPosLoginCapability.class).isEmpty());
        Mockito.verifyNoInteractions(pluginManager);
    }

    @Test
    @DisplayName("未安装插件：一律返回空/false，不抛异常")
    void missingPluginDegrades() {
        assertFalse(host.isActive("webpos"));
        assertTrue(host.discover("webpos").isEmpty());
        assertTrue(host.capability("webpos", WebPosLoginCapability.class).isEmpty());
        assertTrue(host.activeCapabilities().isEmpty());
    }

    @Test
    @DisplayName("已安装但停用：discover 返回描述，callable=false，且取不到能力")
    void disabledPluginDegrades() {
        registry.put("webpos", PluginState.DISABLED, manifest("webpos", "1.0.0", "PAID"));

        assertFalse(host.isActive("webpos"));
        Optional<PluginCapability> cap = host.discover("webpos");
        assertTrue(cap.isPresent());
        assertFalse(cap.get().active());
        assertFalse(cap.get().callable());
        assertEquals("1.0.0", cap.get().version());
        // 停用插件不返回能力实现
        assertTrue(host.capability("webpos", WebPosLoginCapability.class).isEmpty());
    }

    @Test
    @DisplayName("已启用插件：可发现并取到能力实现")
    void activePluginResolvesCapability() {
        registry.put("webpos", PluginState.ACTIVE, manifest("webpos", "1.0.0", "PAID"));
        WebPosLoginCapability impl = username -> "pos-" + username;
        Mockito.when(pluginManager.getExtensions(eq(WebPosLoginCapability.class), eq("webpos")))
                .thenReturn(List.of(impl));

        assertTrue(host.isActive("webpos"));
        Optional<PluginCapability> cap = host.discover("webpos");
        assertTrue(cap.isPresent());
        // PAID 插件本实现不重复校验 License，返回未校验(null)，但因 active 仍可调用
        assertTrue(cap.get().callable());

        Optional<WebPosLoginCapability> found = host.capability("webpos", WebPosLoginCapability.class);
        assertTrue(found.isPresent());
        assertEquals("pos-zhangsan", found.get().login("zhangsan"));
    }

    @Test
    @DisplayName("免费插件 License 视为有效")
    void freePluginLicenseValid() {
        registry.put("demo", PluginState.ACTIVE, manifest("demo", "1.0.0", "FREE"));
        Optional<PluginCapability> cap = host.discover("demo");
        assertTrue(cap.isPresent());
        assertEquals(Boolean.TRUE, cap.get().licenseValid());
        assertTrue(cap.get().callable());
    }

    @Test
    @DisplayName("反射/装载异常：降级为空，绝不抛出")
    void exceptionDegradesToEmpty() {
        registry.put("webpos", PluginState.ACTIVE, manifest("webpos", "1.0.0", "PAID"));
        Mockito.when(pluginManager.getExtensions(eq(WebPosLoginCapability.class), eq("webpos")))
                .thenThrow(new RuntimeException("ClassNotFoundException: xxx"));

        // 关键：不抛异常，返回空
        assertTrue(host.capability("webpos", WebPosLoginCapability.class).isEmpty());
    }

    @Test
    @DisplayName("空/空白入参安全，不抛 NPE")
    void nullSafeArguments() {
        assertFalse(host.isActive(null));
        assertFalse(host.isActive("   "));
        assertTrue(host.discover(null).isEmpty());
        assertTrue(host.capability(null, WebPosLoginCapability.class).isEmpty());
        assertTrue(host.capability("webpos", null).isEmpty());
    }

    @Test
    @DisplayName("activeCapabilities 返回全部已登记插件，且为不可变副本")
    void activeCapabilitiesSnapshot() {
        registry.put("erp", PluginState.ACTIVE, manifest("erp", "2.0.0", "PAID"));
        registry.put("demo", PluginState.DISABLED, manifest("demo", "1.0.0", "FREE"));

        List<PluginCapability> all = host.activeCapabilities();
        assertEquals(2, all.size());
        assertTrue(all.stream().anyMatch(c -> "erp".equals(c.pluginCode()) && c.active()));
        assertTrue(all.stream().anyMatch(c -> "demo".equals(c.pluginCode()) && !c.active()));

        // 快照不可被外部修改，且不受后续状态变更影响
        assertThrows(UnsupportedOperationException.class, () -> all.add(null));
        registry.put("third", PluginState.ACTIVE, manifest("third", "1.0.0", "FREE"));
        assertEquals(2, all.size());
    }

    @Test
    @DisplayName("registry 快照本身不可变")
    void registrySnapshotImmutable() {
        registry.putState("erp", PluginState.ACTIVE);
        var snapshot = registry.snapshot();
        assertNotNull(snapshot.get("erp"));
        assertThrows(UnsupportedOperationException.class, () -> snapshot.put("x", null));
    }

    @Test
    @DisplayName("未装插件时 Core 调用方仍能走通降级分支（模拟 auth 登录）")
    void coreSurvivesWithoutPlugin() {
        // 模拟 auth/LoginServiceImpl：webpos 未安装时按「非收银台用户」放行
        String result = host.capability("webpos", WebPosLoginCapability.class)
                .map(c -> c.login("zhangsan"))
                .orElse("normal-user");
        assertEquals("normal-user", result);

        // 且不应与 PF4J 发生任何交互（未启用直接短路）
        Mockito.verify(pluginManager, Mockito.never()).getExtensions(any(Class.class), any(String.class));
    }
}
