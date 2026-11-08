package com.houjiang.plugin.engine;

import com.houjiang.plugin.api.extension.goods.PriceCalcExtension;
import com.houjiang.plugin.engine.PluginEngineBootstrap;
import com.houjiang.plugin.engine.registry.ExtensionInvoker;
import com.houjiang.plugin.engine.registry.ExtensionRegistry;
import com.houjiang.plugin.engine.itest.DemoPlugin;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * 宿主接线测试（R3 关闭「引擎与宿主假连接」的最终证明）。
 *
 * <p>不手工拼装引擎 Bean，而是把 {@link PluginEngineAutoConfiguration} 直接放进一个
 * Spring 上下文，仅通过环境变量 {@code houcloud.plugin.boot-enabled=true} 与
 * {@code houcloud.plugin.plugins-dir} 驱动，断言：
 * ① autoconfig 把全部引擎 Bean（含 {@link PluginEngineBootstrap}）装配出来；
 * ② 上下文刷新后模拟 Spring Boot 启动回调（{@code ApplicationRunner}），
 *    {@link PluginEngineBootstrap} 真的扫描 pluginsDir 下的真实 JAR 并装载；
 * ③ 装载后扩展点进入 ExtensionRegistry，且 {@link ExtensionInvoker} 真正生效（价格 9 折）。
 *
 * <p>这就是「goods 宿主 @Import 引擎 + 置 boot-enabled=true」在 Spring 接线层面的等价验证，
 * 不需要拉起 goods 全量运行时（Nacos/DB 等本机不可用）。
 */
class PluginEngineHostWiringTest {

    @Test
    void autoconfigWithBootEnabledLoadsRealPluginJarAndRegistersExtension() throws Exception {
        // 1) 准备一个真实的插件 JAR，放到一个（初始为空）的插件目录
        Path pluginsDir = Files.createTempDirectory("host-plugins");
        Path jar = buildPluginJar();
        Files.copy(jar, pluginsDir.resolve("demo-coupon-1.0.0.jar"));

        // 2) 仅用 autoconfig + 环境变量装配引擎（等价于 goods 引入 plugin-engine 并被 @Import）
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.getEnvironment().getPropertySources().addFirst(
                new org.springframework.core.env.MapPropertySource("host-wiring-test",
                        java.util.Map.of(
                                "spring.application.name", "goods",
                                "houcloud.plugin.boot-enabled", "true",
                                "houcloud.plugin.plugins-dir", pluginsDir.toString())));
        ctx.register(PluginEngineAutoConfiguration.class);
        ctx.refresh();

        try {
            // ① autoconfig 真的把启动装载器装配出来了
            PluginEngineBootstrap bootstrap = ctx.getBean(PluginEngineBootstrap.class);
            ExtensionRegistry extensionRegistry = ctx.getBean(ExtensionRegistry.class);
            ExtensionInvoker invoker = ctx.getBean(ExtensionInvoker.class);

            // 3) 模拟 Spring Boot 启动回调（ApplicationRunner），驱动宿主装载
            bootstrap.run(new DefaultApplicationArguments());

            // ② 扩展点已注册（证明 bootstrap 真的装载了 JAR 而非跳过）
            assertFalse(extensionRegistry.getExtensions(PriceCalcExtension.class).isEmpty(),
                    "boot-enabled=true 时应由 PluginEngineBootstrap 装载 demo-coupon 并注册扩展点");

            // ③ ExtensionInvoker 真正生效：价格打 9 折
            PriceCalcExtension.Context c = new PriceCalcExtension.Context();
            c.skuId = 1L;
            c.basePrice = new BigDecimal("100");
            c.finalPrice = c.basePrice;
            invoker.invoke(PriceCalcExtension.class, c);
            assertEquals(0, c.finalPrice.compareTo(new BigDecimal("90")),
                    "扩展点应把 100 改为 90（9 折）");
        } finally {
            ctx.close();
        }
    }

    /**
     * 嵌套落盘布局装载测试：市场在线安装把 JAR 落盘到
     * {@code {plugins-dir}/{code}/{version}/{code}-{version}.jar}（见 {@code PluginArtifactStore} 契约），
     * 与手工平铺布局不同。断言 {@link PluginEngineBootstrap} 递归遍历也能发现并装载嵌套布局的真实 JAR，
     * 使 goods 宿主 {@code boot-enabled=true} 时对 {@code jar/plugins} 下的实际产物真正生效
     * （非递归的 {@code Files.list} 只扫一层，会漏掉全部嵌套 JAR）。
     */
    @Test
    void autoconfigWithBootEnabledLoadsNestedLayoutPluginJar() throws Exception {
        // 1) 按 PluginArtifactStore 契约放置：{root}/{code}/{version}/{code}-{version}.jar
        Path pluginsDir = Files.createTempDirectory("host-plugins-nested");
        Path versionDir = pluginsDir.resolve("demo-coupon").resolve("1.0.0");
        Files.createDirectories(versionDir);
        Files.copy(buildPluginJar(), versionDir.resolve("demo-coupon-1.0.0.jar"));

        // 2) 仅用 autoconfig + 环境变量装配引擎（等价于 goods @Import 引擎并置 boot-enabled=true）
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.getEnvironment().getPropertySources().addFirst(
                new org.springframework.core.env.MapPropertySource("host-wiring-nested-test",
                        java.util.Map.of(
                                "spring.application.name", "custom-process-name",
                                "houcloud.plugin.host-service", "goods",
                                "houcloud.plugin.boot-enabled", "true",
                                "houcloud.plugin.plugins-dir", pluginsDir.toString())));
        ctx.register(PluginEngineAutoConfiguration.class);
        ctx.refresh();

        try {
            PluginEngineBootstrap bootstrap = ctx.getBean(PluginEngineBootstrap.class);
            ExtensionRegistry extensionRegistry = ctx.getBean(ExtensionRegistry.class);
            ExtensionInvoker invoker = ctx.getBean(ExtensionInvoker.class);

            // 3) 模拟 Spring Boot 启动回调，驱动宿主递归扫描并装载嵌套布局 JAR
            bootstrap.run(new DefaultApplicationArguments());

            // ① 嵌套布局的 JAR 被发现并装载 → 扩展点已注册
            assertFalse(extensionRegistry.getExtensions(PriceCalcExtension.class).isEmpty(),
                    "boot-enabled=true 时 PluginEngineBootstrap 应递归发现 {code}/{version}/ 下的 JAR 并注册扩展点");

            // ② ExtensionInvoker 真正生效：价格打 9 折
            PriceCalcExtension.Context c = new PriceCalcExtension.Context();
            c.skuId = 1L;
            c.basePrice = new BigDecimal("100");
            c.finalPrice = c.basePrice;
            invoker.invoke(PriceCalcExtension.class, c);
            assertEquals(0, c.finalPrice.compareTo(new BigDecimal("90")),
                    "扩展点应把 100 改为 90（9 折）");
        } finally {
            ctx.close();
        }
    }

    /** 把测试编译产物中的 itest 包打成 PF4J 合规 JAR（与 PluginEngineLoopTest 同款）。 */
    private Path buildPluginJar() throws Exception {
        Path testClasses = Path.of(PluginEngineHostWiringTest.class.getProtectionDomain()
                .getCodeSource().getLocation().toURI());
        Path pkgDir = testClasses.resolve("com/houjiang/plugin/engine/itest");

        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        manifest.getMainAttributes().putValue("Plugin-Id", "demo-coupon");
        manifest.getMainAttributes().putValue("Plugin-Class", DemoPlugin.class.getName());
        manifest.getMainAttributes().putValue("Plugin-Version", "1.0.0");

        Path out = Files.createTempFile("demo-coupon", ".jar");
        try (JarOutputStream jos = new JarOutputStream(Files.newOutputStream(out), manifest)) {
            jos.putNextEntry(new JarEntry("plugin.yaml"));
            jos.write("code: demo-coupon\nversion: 1.0.0\nowner: thirdparty\nhostService: goods\nruntime: embedded\n"
                    .getBytes(StandardCharsets.UTF_8));
            jos.closeEntry();
            Files.walk(pkgDir).filter(Files::isRegularFile).forEach(p -> {
                String name = testClasses.relativize(p).toString().replace(File.separatorChar, '/');
                try (InputStream in = Files.newInputStream(p)) {
                    jos.putNextEntry(new JarEntry(name));
                    in.transferTo(jos);
                    jos.closeEntry();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
        return out;
    }
}
