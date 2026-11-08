package com.houjiang.mall.service.plugin;

import com.houjiang.mall.entity.plugin.MallPluginInstall;
import com.houjiang.mall.mapper.plugin.MallPluginInstallMapper;
import com.houjiang.plugin.engine.core.PluginLifecycleManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 宿主插件运行态恢复单测（P0-2「以安装状态驱动装载」语义验证）。
 *
 * <p>断言 mall 宿主启动装载严格由【安装状态】驱动，而非目录扫描：
 * <ol>
 *   <li>只装载 installed=1 的记录，且按登记的 jar_path 精确装载（不扫描目录）；</li>
 *   <li>enabled=0 的记录装载后立刻回到停用态；</li>
 *   <li>无任何已安装插件时干净跳过——不装载、不报错、不空转（宿主只跑核心服务）；</li>
 *   <li>磁盘上存在但未登记安装的 JAR 绝不会被启动（未安装 ≠ 装载）；</li>
 *   <li>jar_path 缺失 / 记录非法 / 已装载 → 跳过，不重复装载；</li>
 *   <li>数据库异常被隔离，宿主继续启动（不向上传播）。</li>
 * </ol>
 *
 * @author Amos
 */
class PluginRuntimeBootstrapTest {

    @TempDir
    Path tempDir;

    private MallPluginInstall install(String code, int installed, int enabled, String jarPath) {
        MallPluginInstall record = new MallPluginInstall();
        record.setPluginCode(code);
        record.setInstalled(installed);
        record.setEnabled(enabled);
        record.setVersion("1.0.0");
        record.setJarPath(jarPath);
        return record;
    }

    private Path realJar(String name) throws Exception {
        Path jar = tempDir.resolve(name);
        Files.writeString(jar, "dummy-plugin-bytes");
        return jar;
    }

    /** 规则①：installed=1 且 enabled=1 → 按登记 jar_path 装载并保持启用。 */
    @Test
    void restoresInstalledEnabledPluginByRecordedJarPath() throws Exception {
        MallPluginInstallMapper mapper = mock(MallPluginInstallMapper.class);
        PluginLifecycleManager lifecycle = mock(PluginLifecycleManager.class);
        Path jar = realJar("demo-1.0.0.jar");
        when(mapper.selectList(any())).thenReturn(List.of(install("demo", 1, 1, jar.toString())));
        when(lifecycle.isLoaded("demo")).thenReturn(false);

        new PluginRuntimeBootstrap(mapper, lifecycle).restoreInstalledPlugins();

        verify(lifecycle, times(1)).install(eq("demo"), eq(jar));
        verify(lifecycle, never()).disable(any());
    }

    /** 规则②：installed=1 但 enabled=0 → 装载后立刻回到停用态。 */
    @Test
    void restoresThenAppliesDisabledState() throws Exception {
        MallPluginInstallMapper mapper = mock(MallPluginInstallMapper.class);
        PluginLifecycleManager lifecycle = mock(PluginLifecycleManager.class);
        Path jar = realJar("demo-1.0.0.jar");
        when(mapper.selectList(any())).thenReturn(List.of(install("demo", 1, 0, jar.toString())));
        when(lifecycle.isLoaded("demo")).thenReturn(false);

        new PluginRuntimeBootstrap(mapper, lifecycle).restoreInstalledPlugins();

        verify(lifecycle).install(eq("demo"), eq(jar));
        verify(lifecycle).disable("demo");
    }

    /** 规则③：无任何已安装插件 → 查询安装态后干净跳过，不装载、不空转。 */
    @Test
    void noInstalledPluginsLoadsNothing() {
        MallPluginInstallMapper mapper = mock(MallPluginInstallMapper.class);
        PluginLifecycleManager lifecycle = mock(PluginLifecycleManager.class);
        when(mapper.selectList(any())).thenReturn(List.of());

        new PluginRuntimeBootstrap(mapper, lifecycle).restoreInstalledPlugins();

        // 证明装载确实「以安装状态驱动」：查询了安装表，但因为没有记录而不装载任何东西
        verify(mapper).selectList(any());
        verify(lifecycle, never()).install(any(), any());
        verify(lifecycle, never()).disable(any());
    }

    /** 规则④：磁盘上存在 JAR 但安装表无记录（未安装）→ 绝不会被启动。 */
    @Test
    void uninstalledJarOnDiskIsNeverLoaded() throws Exception {
        MallPluginInstallMapper mapper = mock(MallPluginInstallMapper.class);
        PluginLifecycleManager lifecycle = mock(PluginLifecycleManager.class);
        realJar("ghost-plugin-1.0.0.jar");                 // 落盘但未登记安装
        when(mapper.selectList(any())).thenReturn(List.of()); // 安装表为空

        new PluginRuntimeBootstrap(mapper, lifecycle).restoreInstalledPlugins();

        verify(lifecycle, never()).install(any(), any());
    }

    /** 规则⑤：登记的 jar_path 指向不存在的文件 → 跳过该插件，保持未加载，不报错。 */
    @Test
    void skipsWhenRecordedJarPathMissing() {
        MallPluginInstallMapper mapper = mock(MallPluginInstallMapper.class);
        PluginLifecycleManager lifecycle = mock(PluginLifecycleManager.class);
        Path missing = tempDir.resolve("ghost").resolve("demo-1.0.0.jar");
        when(mapper.selectList(any())).thenReturn(List.of(install("demo", 1, 1, missing.toString())));

        assertDoesNotThrow(() -> new PluginRuntimeBootstrap(mapper, lifecycle).restoreInstalledPlugins());

        verify(lifecycle, never()).install(any(), any());
    }

    /** 规则⑤：code / jar_path 非法的记录 → 跳过，不装载。 */
    @Test
    void skipsInvalidRecordWithoutCodeOrJarPath() {
        MallPluginInstallMapper mapper = mock(MallPluginInstallMapper.class);
        PluginLifecycleManager lifecycle = mock(PluginLifecycleManager.class);
        when(mapper.selectList(any())).thenReturn(List.of(
                install(null, 1, 1, null),
                install("  ", 1, 1, "  ")));

        assertDoesNotThrow(() -> new PluginRuntimeBootstrap(mapper, lifecycle).restoreInstalledPlugins());

        verify(lifecycle, never()).install(any(), any());
    }

    /** 规则⑤：已装载的插件（如市场安装当次已 load）→ 幂等跳过，不重复装载。 */
    @Test
    void skipsAlreadyLoadedPluginToAvoidDoubleInstall() throws Exception {
        MallPluginInstallMapper mapper = mock(MallPluginInstallMapper.class);
        PluginLifecycleManager lifecycle = mock(PluginLifecycleManager.class);
        Path jar = realJar("demo-1.0.0.jar");
        when(mapper.selectList(any())).thenReturn(List.of(install("demo", 1, 1, jar.toString())));
        when(lifecycle.isLoaded("demo")).thenReturn(true);

        new PluginRuntimeBootstrap(mapper, lifecycle).restoreInstalledPlugins();

        verify(lifecycle, never()).install(any(), any());
    }

    /** 规则⑥：数据库未就绪 / 查询异常 → 隔离，宿主继续启动，不向上传播。 */
    @Test
    void databaseFailureIsIsolatedAndDoesNotPropagate() {
        MallPluginInstallMapper mapper = mock(MallPluginInstallMapper.class);
        PluginLifecycleManager lifecycle = mock(PluginLifecycleManager.class);
        when(mapper.selectList(any())).thenThrow(new RuntimeException("db down"));

        assertDoesNotThrow(() -> new PluginRuntimeBootstrap(mapper, lifecycle).restoreInstalledPlugins());

        verify(lifecycle, never()).install(any(), any());
    }
}
