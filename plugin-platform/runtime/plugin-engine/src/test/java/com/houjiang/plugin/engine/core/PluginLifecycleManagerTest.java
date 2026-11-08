package com.houjiang.plugin.engine.core;

import com.houjiang.plugin.engine.core.PluginState;
import com.houjiang.plugin.engine.registry.ExtensionRegistry;
import com.houjiang.plugin.engine.registry.PluginRegistry;
import com.houjiang.plugin.engine.registry.RouteRegistry;
import com.houjiang.plugin.engine.spi.PluginManifestProvider;
import com.houjiang.plugin.engine.spi.PluginMigrationPort;
import com.houjiang.plugin.engine.spi.PluginMenuPort;
import com.houjiang.plugin.sdk.extension.ExtensionContext;
import com.houjiang.plugin.sdk.annotation.HouExtension;
import com.houjiang.plugin.sdk.extension.ExtensionPoint;
import com.houjiang.plugin.sdk.model.PluginManifest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.pf4j.DefaultPluginManager;
import org.pf4j.PluginWrapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 插件生命周期状态机单测：验证「从已落盘安装包真实装载」与「卸载」两条主链路。
 *
 * <p>用 Mockito 替代真实 PF4J 加载（真实加载需要合法的插件描述符 jar），
 * 从而把断言聚焦在本类自身的状态流转与调用编排上。
 *
 * @author HouCloud
 */
class PluginLifecycleManagerTest {

    @TempDir
    Path tempDir;

    private PluginLifecycleManager newManager(DefaultPluginManager pluginManager, PluginRegistry registry) {
        return testManager(pluginManager, new ExtensionRegistry(), new RouteRegistry(), registry);
    }

    @Test
    void standaloneProductNeverReachesPf4j() throws IOException {
        DefaultPluginManager pf4j = mock(DefaultPluginManager.class);
        when(pf4j.loadPlugin(any(Path.class))).thenReturn("mall-live");
        PluginManifest manifest = new PluginManifest();
        manifest.setHostService("cms");
        manifest.setCode("mall-live");
        manifest.setVersion("1.0.0");
        manifest.setRuntime("standalone");
        manifest.setHostService("cms");
        PluginLifecycleManager manager = newManager(pf4j, new PluginRegistry());
        assertThrows(IllegalStateException.class, () -> manager.install("mall-live", writeJar(), manifest));
        verify(pf4j, never()).loadPlugin(any(Path.class));
    }

    @SuppressWarnings("unchecked")
    private PluginLifecycleManager testManager(DefaultPluginManager pf4j, ExtensionRegistry extensions,
                                               RouteRegistry routes, PluginRegistry registry, Object... ports) {
        PluginManifestProvider provider = ports.length > 5 ? (PluginManifestProvider) ports[5] : (code, jar) -> {
            PluginManifest artifact = new PluginManifest();
            artifact.setCode(code);
            artifact.setVersion("1.0.0");
            artifact.setHostService("cms");
            return artifact;
        };
        return new PluginLifecycleManager(pf4j, extensions, routes, registry,
                ports.length > 0 ? (org.springframework.beans.factory.config.AutowireCapableBeanFactory) ports[0] : null,
                ports.length > 1 ? (List<String>) ports[1] : List.of(),
                ports.length > 2 ? (PluginMigrationPort) ports[2] : null,
                ports.length > 3 ? (PluginMenuPort) ports[3] : null,
                ports.length > 4 ? (PluginSpringContextManager) ports[4] : null,
                provider, "cms", false);
    }

    private Path writeJar() throws IOException {
        Path jar = tempDir.resolve("mall-live-1.0.0.jar");
        Files.write(jar, "fake-jar".getBytes());
        return jar;
    }

    @Test
    void installLoadsAndStartsPluginFromDisk() throws IOException {
        DefaultPluginManager pluginManager = mock(DefaultPluginManager.class);
        when(pluginManager.loadPlugin(any(Path.class))).thenReturn("mall-live");
        PluginRegistry registry = new PluginRegistry();
        PluginLifecycleManager manager = newManager(pluginManager, registry);
        Path jar = writeJar();

        PluginState state = manager.install("mall-live", jar);

        assertEquals(PluginState.ACTIVE, state);
        assertEquals(PluginState.ACTIVE, registry.getState("mall-live"));
        verify(pluginManager).loadPlugin(jar);
        verify(pluginManager).startPlugin("mall-live");
    }

    @Test
    void installFailsWhenJarMissingAndMarksFailed() {
        DefaultPluginManager pluginManager = mock(DefaultPluginManager.class);
        PluginRegistry registry = new PluginRegistry();
        PluginLifecycleManager manager = newManager(pluginManager, registry);
        Path missing = tempDir.resolve("not-exists.jar");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> manager.install("mall-live", missing));

        assertEquals("插件安装包不存在: " + missing, ex.getMessage());
        assertEquals(PluginState.FAILED, registry.getState("mall-live"));
        verify(pluginManager, never()).loadPlugin(any(Path.class));
    }

    @Test
    void installMarksFailedWhenDescriptorUnresolved() throws IOException {
        DefaultPluginManager pluginManager = mock(DefaultPluginManager.class);
        when(pluginManager.loadPlugin(any(Path.class))).thenReturn(null);
        PluginRegistry registry = new PluginRegistry();
        PluginLifecycleManager manager = newManager(pluginManager, registry);
        Path jar = writeJar();

        assertThrows(IllegalStateException.class, () -> manager.install("mall-live", jar));

        assertEquals(PluginState.FAILED, registry.getState("mall-live"));
        verify(pluginManager, never()).startPlugin(any());
    }

    @Test
    void installMarksFailedWhenStartThrows() throws IOException {
        DefaultPluginManager pluginManager = mock(DefaultPluginManager.class);
        when(pluginManager.loadPlugin(any(Path.class))).thenReturn("mall-live");
        when(pluginManager.startPlugin(eq("mall-live"))).thenThrow(new IllegalStateException("依赖缺失"));
        PluginRegistry registry = new PluginRegistry();
        PluginLifecycleManager manager = newManager(pluginManager, registry);
        Path jar = writeJar();

        assertThrows(IllegalStateException.class, () -> manager.install("mall-live", jar));

        assertEquals(PluginState.FAILED, registry.getState("mall-live"));
    }

    @Test
    void installLoadsManifestFromProviderBeforeRuntimeExtensionScan() throws IOException {
        DefaultPluginManager pluginManager = mock(DefaultPluginManager.class);
        when(pluginManager.loadPlugin(any(Path.class))).thenReturn("mall-live");
        PluginRegistry registry = new PluginRegistry();
        PluginManifest manifest = new PluginManifest();
        manifest.setHostService("cms");
        manifest.setCode("mall-live");
        manifest.setVersion("1.0.0");
        manifest.setOwner("thirdparty");
        PluginManifestProvider provider = mock(PluginManifestProvider.class);
        when(provider.load(eq("mall-live"), any(Path.class))).thenReturn(manifest);
        PluginLifecycleManager manager = testManager(pluginManager, new ExtensionRegistry(),
                new RouteRegistry(), registry, null, List.of(), null, null, null, provider);

        manager.install("mall-live", writeJar());

        assertEquals("thirdparty", registry.getManifest("mall-live").getOwner());
        verify(provider).load(eq("mall-live"), any(Path.class));
    }

    @Test
    void installFailsAndMarksFailedWhenProviderRejectsArtifact() throws IOException {
        DefaultPluginManager pluginManager = mock(DefaultPluginManager.class);
        PluginRegistry registry = new PluginRegistry();
        PluginManifestProvider provider = mock(PluginManifestProvider.class);
        when(provider.load(eq("mall-live"), any(Path.class)))
                .thenThrow(new IllegalStateException("plugin.yaml 缺失"));
        PluginLifecycleManager manager = testManager(pluginManager, new ExtensionRegistry(),
                new RouteRegistry(), registry, null, List.of(), null, null, null, provider);

        assertThrows(IllegalStateException.class, () -> manager.install("mall-live", writeJar()));

        assertEquals(PluginState.FAILED, registry.getState("mall-live"));
        verify(pluginManager, never()).loadPlugin(any(Path.class));
    }

    @Test
    void installRejectsCustomOwnerFromRuntimeManifest() throws IOException {
        DefaultPluginManager pluginManager = mock(DefaultPluginManager.class);
        PluginRegistry registry = new PluginRegistry();
        PluginManifest manifest = new PluginManifest();
        manifest.setHostService("cms");
        manifest.setCode("mall-live");
        manifest.setVersion("1.0.0");
        manifest.setOwner("custom");
        PluginManifestProvider provider = mock(PluginManifestProvider.class);
        when(provider.load(eq("mall-live"), any(Path.class))).thenReturn(manifest);
        PluginLifecycleManager manager = testManager(pluginManager, new ExtensionRegistry(),
                new RouteRegistry(), registry, null, List.of(), null, null, null, provider);

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> manager.install("mall-live", writeJar()));

        assertTrue(error.getMessage().contains("owner=custom"));
        assertEquals(PluginState.FAILED, registry.getState("mall-live"));
        verify(pluginManager, never()).loadPlugin(any(Path.class));
    }

    @Test
    void uninstallStopsAndUnloadsThenClearsRegistry() {
        DefaultPluginManager pluginManager = mock(DefaultPluginManager.class);
        PluginWrapper wrapper = mock(PluginWrapper.class);
        when(wrapper.getPluginState()).thenReturn(org.pf4j.PluginState.STARTED);
        when(pluginManager.getPlugin("mall-live")).thenReturn(wrapper);
        PluginRegistry registry = new PluginRegistry();
        registry.putState("mall-live", PluginState.ACTIVE);
        PluginLifecycleManager manager = newManager(pluginManager, registry);

        manager.uninstall("mall-live");

        verify(pluginManager).stopPlugin("mall-live");
        verify(pluginManager).unloadPlugin("mall-live");
        assertNull(registry.getState("mall-live"), "注册表应已清理");
    }

    /** 观测停用是否连带清理扩展点的扩展点类型 */
    private interface DemoPoint extends ExtensionPoint<ExtensionContext> {
    }

    private static final class DemoExt implements DemoPoint {
        @Override
        public void execute(ExtensionContext context) {
        }
    }

    @Test
    void disableRemovesExtensionsNotJustRoutes() {
        DefaultPluginManager pluginManager = mock(DefaultPluginManager.class);
        PluginRegistry registry = new PluginRegistry();
        ExtensionRegistry extensions = new ExtensionRegistry();
        PluginLifecycleManager manager = testManager(
                pluginManager, extensions, new RouteRegistry(), registry);

        manager.registerExtension(DemoPoint.class, new DemoExt(), "demo", 100);
        assertEquals(1, extensions.getExtensions(DemoPoint.class).size(), "前置：扩展点已注册");

        manager.disable("demo");

        assertTrue(extensions.getExtensions(DemoPoint.class).isEmpty(),
                "停用后扩展点必须一并失效，否则已停用插件的扩展仍会被 Core 调用");
        assertEquals(PluginState.DISABLED, registry.getState("demo"));
        verify(pluginManager).stopPlugin("demo");
    }

    @Test
    void installRollsBackResidualRegistrationsOnFailure() throws IOException {
        DefaultPluginManager pluginManager = mock(DefaultPluginManager.class);
        when(pluginManager.loadPlugin(any(Path.class))).thenReturn("mall-live");
        doThrow(new IllegalStateException("依赖缺失")).when(pluginManager).startPlugin("mall-live");
        PluginRegistry registry = new PluginRegistry();
        ExtensionRegistry extensions = new ExtensionRegistry();
        PluginLifecycleManager manager = testManager(
                pluginManager, extensions, new RouteRegistry(), registry);
        // 模拟「安装中途已注册过扩展点」的半生效状态（如重试安装遗留）
        manager.registerExtension(DemoPoint.class, new DemoExt(), "mall-live", 100);
        assertEquals(1, extensions.getExtensions(DemoPoint.class).size(), "前置：残留一个扩展点");
        Path jar = writeJar();

        assertThrows(IllegalStateException.class, () -> manager.install("mall-live", jar));

        assertEquals(PluginState.FAILED, registry.getState("mall-live"), "失败应置 FAILED");
        assertTrue(extensions.getExtensions(DemoPoint.class).isEmpty(), "回滚必须清掉残留扩展点");
        verify(pluginManager).unloadPlugin("mall-live");
    }

    @Test
    void installRunsMigrationBeforeActivationWhenPortProvided() throws IOException {
        DefaultPluginManager pluginManager = mock(DefaultPluginManager.class);
        when(pluginManager.loadPlugin(any(Path.class))).thenReturn("mall-live");
        PluginRegistry registry = new PluginRegistry();
        PluginMigrationPort migration = mock(PluginMigrationPort.class);
        PluginLifecycleManager manager = testManager(pluginManager, new ExtensionRegistry(),
                new RouteRegistry(), registry, null, List.of(), migration);
        Path jar = writeJar();

        assertEquals(PluginState.ACTIVE, manager.install("mall-live", jar));

        verify(migration).migrate(eq("mall-live"), any());
    }

    @HouExtension(point = DemoPoint.class, owner = "custom")
    private static final class CustomDeclaredDemoExt implements DemoPoint {
        @Override
        public void execute(ExtensionContext context) {
        }
    }

    @Test
    void runtimePluginCannotUseBuildTimeCustomOwnerWithoutManifest() {
        DefaultPluginManager pluginManager = mock(DefaultPluginManager.class);
        PluginRegistry registry = new PluginRegistry();
        PluginLifecycleManager manager = newManager(pluginManager, registry);

        assertThrows(IllegalStateException.class,
                () -> manager.resolveRuntimeExtensionOwner("demo", CustomDeclaredDemoExt.class
                        .getAnnotation(HouExtension.class)));
    }

    @Test
    void verifiedManifestOwnerOverridesExtensionAnnotation() {
        DefaultPluginManager pluginManager = mock(DefaultPluginManager.class);
        PluginRegistry registry = new PluginRegistry();
        PluginManifest manifest = new PluginManifest();
        manifest.setHostService("cms");
        manifest.setCode("demo");
        manifest.setOwner("thirdparty");
        registry.put("demo", PluginState.DISABLED, manifest);
        PluginLifecycleManager manager = newManager(pluginManager, registry);

        assertEquals(ExtensionRegistry.OWNER_THIRDPARTY,
                manager.resolveRuntimeExtensionOwner("demo", CustomDeclaredDemoExt.class
                        .getAnnotation(HouExtension.class)));
    }

    @Test
    void lifecycleSynchronizesPluginMenusAcrossStates() throws IOException {
        DefaultPluginManager pluginManager = mock(DefaultPluginManager.class);
        when(pluginManager.loadPlugin(any(Path.class))).thenReturn("mall-live");
        PluginRegistry registry = new PluginRegistry();
        PluginMenuPort menuPort = mock(PluginMenuPort.class);
        PluginLifecycleManager manager = testManager(pluginManager, new ExtensionRegistry(),
                new RouteRegistry(), registry, null, List.of(), null, menuPort);
        Path jar = writeJar();

        manager.install("mall-live", jar);
        manager.disable("mall-live");
        manager.enable("mall-live");
        manager.uninstall("mall-live");

        verify(menuPort).sync("mall-live", jar);
        verify(menuPort).disable("mall-live");
        verify(menuPort).enable("mall-live");
        verify(menuPort).remove("mall-live");
    }

    @Test
    void installSkipsMigrationWhenPortAbsent() throws IOException {
        DefaultPluginManager pluginManager = mock(DefaultPluginManager.class);
        when(pluginManager.loadPlugin(any(Path.class))).thenReturn("mall-live");
        PluginRegistry registry = new PluginRegistry();
        PluginLifecycleManager manager = newManager(pluginManager, registry);

        assertEquals(PluginState.ACTIVE, manager.install("mall-live", writeJar()),
                "未注入迁移端口时应跳过 MIGRATED，保持旧行为");
    }

    @Test
    void installRollsBackWhenMigrationFails() throws IOException {
        DefaultPluginManager pluginManager = mock(DefaultPluginManager.class);
        when(pluginManager.loadPlugin(any(Path.class))).thenReturn("mall-live");
        PluginRegistry registry = new PluginRegistry();
        PluginMigrationPort migration = mock(PluginMigrationPort.class);
        doThrow(new IllegalStateException("建表失败")).when(migration).migrate(any(), any());
        PluginLifecycleManager manager = testManager(pluginManager, new ExtensionRegistry(),
                new RouteRegistry(), registry, null, List.of(), migration);
        Path jar = writeJar();

        assertThrows(IllegalStateException.class, () -> manager.install("mall-live", jar));

        assertEquals(PluginState.FAILED, registry.getState("mall-live"), "迁移失败必须置 FAILED");
        verify(pluginManager, never()).startPlugin("mall-live");
    }

    @Test
    void isHealthyRequiresActiveStateAndPf4jStarted() {
        DefaultPluginManager pluginManager = mock(DefaultPluginManager.class);
        PluginRegistry registry = new PluginRegistry();
        PluginLifecycleManager manager = newManager(pluginManager, registry);

        assertFalse(manager.isHealthy("demo"), "未安装/非 ACTIVE 一律不健康");

        registry.putState("demo", PluginState.ACTIVE);
        PluginWrapper started = mock(PluginWrapper.class);
        when(started.getPluginState()).thenReturn(org.pf4j.PluginState.STARTED);
        when(pluginManager.getPlugin("demo")).thenReturn(started);
        assertTrue(manager.isHealthy("demo"), "ACTIVE 且 PF4J 已启动才健康");

        PluginWrapper stopped = mock(PluginWrapper.class);
        when(stopped.getPluginState()).thenReturn(org.pf4j.PluginState.STOPPED);
        when(pluginManager.getPlugin("demo")).thenReturn(stopped);
        assertFalse(manager.isHealthy("demo"), "状态表 ACTIVE 但引擎已停止，应判不健康");

        registry.putState("demo", PluginState.DISABLED);
        assertFalse(manager.isHealthy("demo"));
    }

    @Test
    void isHealthyIsFalseWhenEngineNoLongerKnowsThePlugin() {
        DefaultPluginManager pluginManager = mock(DefaultPluginManager.class);
        PluginRegistry registry = new PluginRegistry();
        registry.putState("demo", PluginState.ACTIVE);
        when(pluginManager.getPlugin("demo")).thenReturn(null);
        PluginLifecycleManager manager = newManager(pluginManager, registry);

        // 修复点：原实现为 `wrapper == null || ...`，即引擎查不到该插件反而判健康，
        // 会掩盖「状态表残留 ACTIVE、引擎侧已无该插件」的不一致。
        assertFalse(manager.isHealthy("demo"), "状态表 ACTIVE 但引擎查不到该插件时必须判不健康");
    }

    @Test
    void isHealthyReturnsFalseOnProbeException() {
        DefaultPluginManager pluginManager = mock(DefaultPluginManager.class);
        PluginRegistry registry = new PluginRegistry();
        registry.putState("demo", PluginState.ACTIVE);
        when(pluginManager.getPlugin("demo")).thenThrow(new RuntimeException("PF4J 内部错误"));
        PluginLifecycleManager manager = newManager(pluginManager, registry);

        assertFalse(manager.isHealthy("demo"), "探针异常应判不健康而非抛出");
    }

    @Test
    void disableIsIdempotent() {
        DefaultPluginManager pluginManager = mock(DefaultPluginManager.class);
        PluginRegistry registry = new PluginRegistry();
        ExtensionRegistry extensions = new ExtensionRegistry();
        PluginLifecycleManager manager = testManager(
                pluginManager, extensions, new RouteRegistry(), registry);

        manager.registerExtension(DemoPoint.class, new DemoExt(), "demo", 100);
        manager.disable("demo");
        manager.disable("demo");

        assertTrue(extensions.getExtensions(DemoPoint.class).isEmpty(), "重复停用不应报错也不应复活扩展");
        assertEquals(PluginState.DISABLED, registry.getState("demo"));
    }

    @Test
    void disablePersistsDisabledStateWhenMenuCleanupFails() {
        DefaultPluginManager pluginManager = mock(DefaultPluginManager.class);
        PluginRegistry registry = new PluginRegistry();
        PluginMenuPort menuPort = mock(PluginMenuPort.class);
        doThrow(new IllegalStateException("menu store unavailable")).when(menuPort).disable("demo");
        PluginLifecycleManager manager = testManager(pluginManager, new ExtensionRegistry(),
                new RouteRegistry(), registry, null, List.of(), null, menuPort);

        IllegalStateException error = assertThrows(IllegalStateException.class, () -> manager.disable("demo"));

        assertEquals("menu store unavailable", error.getMessage());
        assertEquals(PluginState.DISABLED, registry.getState("demo"),
                "菜单清理失败也不能让插件停用状态停留在未知状态");
        verify(menuPort).disable("demo");
    }

    @Test
    void uninstallDoesNotStopPluginThatIsNotStarted() {
        DefaultPluginManager pluginManager = mock(DefaultPluginManager.class);
        PluginWrapper wrapper = mock(PluginWrapper.class);
        when(wrapper.getPluginState()).thenReturn(org.pf4j.PluginState.RESOLVED);
        when(pluginManager.getPlugin("mall-live")).thenReturn(wrapper);
        PluginRegistry registry = new PluginRegistry();
        PluginLifecycleManager manager = newManager(pluginManager, registry);

        manager.uninstall("mall-live");

        verify(pluginManager, never()).stopPlugin(any());
        verify(pluginManager).unloadPlugin("mall-live");
    }

    @Test
    void uninstallToleratesEngineErrors() {
        DefaultPluginManager pluginManager = mock(DefaultPluginManager.class);
        when(pluginManager.getPlugin("mall-live")).thenThrow(new IllegalStateException("引擎异常"));
        PluginRegistry registry = new PluginRegistry();
        registry.putState("mall-live", PluginState.ACTIVE);
        PluginLifecycleManager manager = newManager(pluginManager, registry);

        manager.uninstall("mall-live");

        assertNull(registry.getState("mall-live"), "引擎报错也必须完成注册表清理");
    }

    @Test
    void disableAndEnableDelegateToEngineAndState() {
        DefaultPluginManager pluginManager = mock(DefaultPluginManager.class);
        PluginRegistry registry = new PluginRegistry();
        PluginManifest installed = new PluginManifest();
        installed.setCode("mall-live");
        installed.setHostService("cms");
        registry.put("mall-live", PluginState.ACTIVE, installed);
        PluginLifecycleManager manager = newManager(pluginManager, registry);

        manager.disable("mall-live");
        assertEquals(PluginState.DISABLED, registry.getState("mall-live"));
        verify(pluginManager).stopPlugin("mall-live");

        manager.enable("mall-live");
        assertEquals(PluginState.ACTIVE, registry.getState("mall-live"));
        verify(pluginManager).startPlugin("mall-live");
    }
}
