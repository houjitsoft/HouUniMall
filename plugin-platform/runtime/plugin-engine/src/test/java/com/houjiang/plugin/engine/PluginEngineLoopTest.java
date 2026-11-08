package com.houjiang.plugin.engine;

import com.houjiang.plugin.api.extension.goods.PriceCalcExtension;
import com.houjiang.plugin.engine.config.PluginEngineProperties;
import com.houjiang.plugin.engine.core.HouPluginManager;
import com.houjiang.plugin.engine.core.PluginLifecycleManager;
import com.houjiang.plugin.engine.core.PluginState;
import com.houjiang.plugin.engine.registry.ExtensionInvoker;
import com.houjiang.plugin.engine.registry.ExtensionRegistry;
import com.houjiang.plugin.engine.registry.PluginRegistry;
import com.houjiang.plugin.engine.registry.RouteRegistry;
import com.houjiang.plugin.engine.itest.DemoPlugin;
import com.houjiang.plugin.engine.itest.DemoPriceExtension;
import org.junit.jupiter.api.Test;
import org.pf4j.DefaultPluginManager;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

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
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 闭环测试：把演示插件打包成真实 PF4J JAR → 装载 → 验证
 * ① 扩展点注册进 ExtensionRegistry ② 路由注册进 RouteRegistry ③ ExtensionInvoker 真正生效。
 * 这是「R3 引擎与宿主假连接」「R2 扩展点全纸面」两项缺口关闭后的端到端证明。
 */
class PluginEngineLoopTest {

    @Test
    void loadPluginRegistersExtensionAndRouteAndInvokerWorks() throws Exception {
        Path jar = buildPluginJar();

        PluginEngineProperties properties = new PluginEngineProperties();
        properties.setEnabled(true);
        properties.setRouteStripPrefixes(java.util.List.of());

        ExtensionRegistry extensionRegistry = new ExtensionRegistry();
        PluginRegistry pluginRegistry = new PluginRegistry();
        RouteRegistry routeRegistry = new RouteRegistry();

        // 用 Spring 上下文拿 AutowireCapableBeanFactory，触发真实路由扫描
        AnnotationConfigApplicationContext spring = new AnnotationConfigApplicationContext();
        spring.refresh();
        AutowireCapableBeanFactory beanFactory = spring.getAutowireCapableBeanFactory();

        DefaultPluginManager pluginManager =
                new HouPluginManager(Files.createTempDirectory("plugins"), null);
        ExtensionInvoker invoker = new ExtensionInvoker(extensionRegistry, properties);
        PluginLifecycleManager lifecycle = new PluginLifecycleManager(
                pluginManager, extensionRegistry, routeRegistry, pluginRegistry, beanFactory,
                properties.getRouteStripPrefixes(), null, null, null,
                new com.houjiang.plugin.engine.artifact.JarPluginManifestProvider(), "goods", false);

        // ① 装载插件（等价于宿主启动装载器 PluginEngineBootstrap 的行为）
        PluginState state = lifecycle.install("demo-coupon", jar);
        assertEquals(PluginState.ACTIVE, state);

        // ② 扩展点已注册
        assertFalse(extensionRegistry.getExtensions(PriceCalcExtension.class).isEmpty());

        // ③ 路由已注册
        assertNotNull(routeRegistry.lookup("demo-coupon", "/calc", "POST"), "插件路由 /calc 应已注册");

        // ④ ExtensionInvoker 真正生效：价格打 9 折
        PriceCalcExtension.Context ctx = new PriceCalcExtension.Context();
        ctx.skuId = 1L;
        ctx.basePrice = new BigDecimal("100");
        ctx.finalPrice = ctx.basePrice;
        invoker.invoke(PriceCalcExtension.class, ctx);
        assertEquals(0, ctx.finalPrice.compareTo(new BigDecimal("90")),
                "扩展点应把 100 改为 90（9 折）");

        spring.close();
    }

    /** 把测试编译产物中的 itest 包打成 PF4J 合规 JAR。 */
    private Path buildPluginJar() throws Exception {
        Path testClasses = Path.of(PluginEngineLoopTest.class.getProtectionDomain()
                .getCodeSource().getLocation().toURI());
        Path pkgDir = testClasses.resolve("com/houjiang/plugin/engine/itest");

        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        manifest.getMainAttributes().putValue("Plugin-Id", "demo-coupon");
        manifest.getMainAttributes().putValue("Plugin-Class", DemoPlugin.class.getName());
        manifest.getMainAttributes().putValue("Plugin-Version", "1.0.0");

        Path jar = Files.createTempFile("demo-coupon", ".jar");
        try (JarOutputStream jos = new JarOutputStream(Files.newOutputStream(jar), manifest)) {
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
        return jar;
    }
}
