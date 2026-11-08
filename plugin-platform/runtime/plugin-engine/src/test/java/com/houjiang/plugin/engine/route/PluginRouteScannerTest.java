package com.houjiang.plugin.engine.route;

import com.houjiang.plugin.engine.registry.RouteRegistry;
import com.houjiang.plugin.sdk.annotation.HouCaller;
import com.houjiang.plugin.sdk.annotation.HouRoute;
import com.houjiang.plugin.sdk.annotation.HouRouteDefaults;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 路由扫描器单测：把插件类打包成临时 JAR 后直接喂给 {@link PluginRouteScanner#scan}，
 * 验证注解解析、前缀剥离与路径归一，无需启动 PF4J。
 *
 * @author HouCloud
 */
class PluginRouteScannerTest {

    @TempDir
    Path tempDir;

    @RequestMapping("/api/erp/order")
    @HouRouteDefaults(readPermission = "erp:order:read", writePermission = "erp:order:write")
    static class ErpOrderController {
        @HouRoute(path = "/list", method = "POST", permission = "erp:order:list",
                allowCallers = {"crm", "host"})
        public Object list() {
            return null;
        }

        @PostMapping("/override")
        @HouRoute(path = "/override", method = "POST", permission = "erp:order:override")
        public Object override() {
            return null;
        }

        @GetMapping("/{id}")
        public Object get() {
            return null;
        }

        @PostMapping
        public Object create() {
            return null;
        }
    }

    @RequestMapping("/crm/customer")
    static class CrmController {
        @PutMapping("/{id}")
        public Object update() {
            return null;
        }

        @DeleteMapping
        public Object delete() {
            return null;
        }
    }

    public static class MissingDependency {
    }

    public static class UnannotatedOptionalIntegration {
        public MissingDependency dependency() {
            return null;
        }
    }

    public static class MethodOnlyCouponController {
        @HouRoute(path = "/coupon/claim", method = "POST", permission = "coupon:claim")
        public Object claim() {
            return null;
        }
    }

    @RequestMapping("/broken")
    public static class BrokenController {
        @GetMapping("/dependency")
        public MissingDependency dependency() {
            return null;
        }
    }

    /**
     * 存量主流写法：Spring 原生 Controller + 类级 {@link HouCaller}（GEO / OA 的 job feign 接口即如此）。
     */
    @RequestMapping("/geo/job/feign")
    @HouCaller({"internal"})
    static class JobFeignController {
        @PostMapping("/scheduled-publish")
        public Object scheduledPublish() {
            return null;
        }

        @PostMapping("/keepalive")
        @HouCaller({"host"})
        public Object keepalive() {
            return null;
        }
    }

    /** 类级 {@code @HouCaller} 与 {@code @HouRoute(allowCallers)} 混用。 */
    @RequestMapping("/erp/stock")
    @HouCaller({"internal"})
    static class MixedCallerController {
        @HouRoute(path = "/reserve", method = "POST", permission = "erp:stock:reserve",
                allowCallers = {"crm"})
        public Object reserve() {
            return null;
        }

        @GetMapping("/list")
        public Object list() {
            return null;
        }
    }

    @RequestMapping("/healthy")
    public static class HealthyController {
        @GetMapping("/ping")
        public Object ping() {
            return null;
        }
    }

    /**
     * 短前缀（{@code /finance}、{@code /supplier}）与类级前缀 {@code /api/erp} 共存时的回归夹具。
     *
     * <p>2026-09-20 线上事故：类级与方法级各剥一次，{@code /api/erp/finance} 先被 {@code /api/erp}
     * 剥成 {@code /finance}，再被 {@code /finance} 剥成空；{@code /api/erp/supplier/page} 被
     * {@code /supplier} 剥成 {@code /page}。修复后这两个接口必须原样保留。</p>
     */
    @RequestMapping("/api/erp")
    static class ErpShortSegmentController {
        @GetMapping("/finance")
        public Object finance() {
            return null;
        }

        @GetMapping("/finance/accounting")
        public Object accounting() {
            return null;
        }

        @GetMapping("/supplier/page")
        public Object supplierPage() {
            return null;
        }
    }

    private Path buildPluginJar() throws Exception {
        Path testClasses = Path.of(getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
        String base = "com/houjiang/plugin/engine/route/";
        List<String> fixtures = List.of(
                "PluginRouteScannerTest$ErpOrderController",
                "PluginRouteScannerTest$CrmController");
        Path jar = tempDir.resolve("scan-test.jar");
        try (JarOutputStream jos = new JarOutputStream(Files.newOutputStream(jar))) {
            for (String f : fixtures) {
                byte[] bytes = Files.readAllBytes(testClasses.resolve(base + f + ".class"));
                jos.putNextEntry(new ZipEntry(base + f + ".class"));
                jos.write(bytes);
                jos.closeEntry();
            }
        }
        return jar;
    }

    private Path buildPluginJar(List<String> fixtures, String fileName) throws Exception {
        Path testClasses = Path.of(getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
        String base = "com/houjiang/plugin/engine/route/";
        Path jar = tempDir.resolve(fileName);
        try (JarOutputStream jos = new JarOutputStream(Files.newOutputStream(jar))) {
            for (String fixture : fixtures) {
                byte[] bytes = Files.readAllBytes(testClasses.resolve(base + fixture + ".class"));
                jos.putNextEntry(new ZipEntry(base + fixture + ".class"));
                jos.write(bytes);
                jos.closeEntry();
            }
        }
        return jar;
    }

    private PluginRouteScanner.ScanRequest request(String pluginCode, Path jar, List<String> strip) throws Exception {
        URLClassLoader loader = new URLClassLoader(
                new URL[]{jar.toUri().toURL()}, getClass().getClassLoader());
        return new PluginRouteScanner.ScanRequest(pluginCode, jar, loader, null, strip);
    }

    @Test
    void scansHouRouteAndSpringMappingsWithConfigurablePrefixStrip() throws Exception {
        Path jar = buildPluginJar();
        List<RouteRegistry.RouteEntry> entries =
                new PluginRouteScanner().scan(request("erp", jar, List.of("/api/erp")));

        assertEquals(6, entries.size(), "两个控制器共 6 条路由");

        RouteRegistry.RouteEntry list = find(entries, "POST", "/list");
        assertNotNull(list, "HouRoute 路径应原样保留");
        assertEquals("erp:order:list", list.permission, "HouRoute 的 permission 必须保留");
        assertEquals("erp", list.code);
        assertEquals(Set.of("crm", "host"), list.allowCallers,
                "HouRoute 的 allowCallers 必须传到路由条目（大小写归一）");
        RouteRegistry.RouteEntry override = find(entries, "POST", "/override");
        assertNotNull(override, "方法级 HouRoute 应覆盖 Spring 映射路径");
        assertEquals("erp:order:override", override.permission, "方法级 HouRoute 权限必须保留且不能被 Spring 映射覆盖");
        assertNull(override.allowCallers,
                "未声明 allowCallers 的路由必须保持 null（＝不限制），不能退化成空集合");

        RouteRegistry.RouteEntry get = find(entries, "GET", "/order/{id}");
        assertNotNull(get, "类前缀 /api/erp 应被剥离为 /order");
        assertEquals("erp:order:read", get.permission, "控制器默认读权限应应用到 GET 路由");
        RouteRegistry.RouteEntry create = find(entries, "POST", "/order/");
        assertNotNull(create, "@PostMapping(无 path) 应拼出 /order/");
        assertEquals("erp:order:write", create.permission, "控制器默认写权限应应用到 POST 路由");
        assertNotNull(find(entries, "PUT", "/crm/customer/{id}"), "无前缀剥离的控制器路径应完整");
        assertNotNull(find(entries, "DELETE", "/crm/customer/"));
    }

    /**
     * {@link HouCaller} 是给 Spring 原生 Controller 用的调用方声明入口：
     * 存量插件（GEO / OA 等）没有 {@code @HouRoute} 可挂，只有它能声明。
     */
    @Test
    void houCallerDeclarationAppliesToSpringControllers() throws Exception {
        Path jar = buildPluginJar(List.of(
                "PluginRouteScannerTest$JobFeignController",
                "PluginRouteScannerTest$MixedCallerController"), "hou-caller.jar");
        List<RouteRegistry.RouteEntry> entries =
                new PluginRouteScanner().scan(request("geo", jar, List.of()));

        assertEquals(Set.of("internal"),
                find(entries, "POST", "/geo/job/feign/scheduled-publish").allowCallers,
                "类级声明必须对全类生效");
        assertEquals(Set.of("internal", "host"),
                find(entries, "POST", "/geo/job/feign/keepalive").allowCallers,
                "方法级声明必须与类级取并集，不能覆盖掉类级");
        assertEquals(Set.of("crm", "internal"),
                find(entries, "POST", "/reserve").allowCallers,
                "@HouRoute 的 allowCallers 与类级 @HouCaller 必须取并集");
        assertEquals(Set.of("internal"),
                find(entries, "GET", "/erp/stock/list").allowCallers,
                "未额外声明的方法沿用类级声明");
    }

    @Test
    void noStripPrefixLeavesFullClassPath() throws Exception {
        Path jar = buildPluginJar();
        List<RouteRegistry.RouteEntry> entries =
                new PluginRouteScanner().scan(request("erp", jar, List.of()));

        assertEquals(6, entries.size());
        assertNotNull(find(entries, "GET", "/api/erp/order/{id}"), "未配置剥离时类前缀应完整保留");
        assertNotNull(find(entries, "POST", "/api/erp/order/"));
        assertNotNull(find(entries, "PUT", "/crm/customer/{id}"));
        assertNotNull(find(entries, "DELETE", "/crm/customer/"));
    }

    @Test
    void shortStripPrefixMustNotEatMethodLevelPath() throws Exception {
        Path jar = buildPluginJar(List.of("PluginRouteScannerTest$ErpShortSegmentController"), "erp-short.jar");
        List<RouteRegistry.RouteEntry> entries = new PluginRouteScanner()
                .scan(request("erp", jar, List.of("/api/erp", "/finance", "/supplier")));

        assertEquals(3, entries.size(), "三条路由必须全部登记，不能被短前缀剥空");
        assertNotNull(find(entries, "GET", "/finance"), "类级 /api/erp + 方法级 /finance 只应剥掉 /api/erp");
        assertNotNull(find(entries, "GET", "/finance/accounting"), "短前缀不得吃掉 /finance 之后的子路径");
        assertNotNull(find(entries, "GET", "/supplier/page"), "短前缀 /supplier 不得把路径剥成 /page");
    }

    @Test
    void skipsControllerWhoseMethodSignatureDependencyIsUnavailable() throws Exception {
        Path jar = buildPluginJar(List.of(
                "PluginRouteScannerTest$BrokenController",
                "PluginRouteScannerTest$HealthyController"), "missing-dependency.jar");
        String brokenController = BrokenController.class.getName();
        String healthyController = HealthyController.class.getName();
        String missingDependency = MissingDependency.class.getName();

        try (URLClassLoader loader = new URLClassLoader(new URL[]{jar.toUri().toURL()}, getClass().getClassLoader()) {
            @Override
            protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                if (name.equals(missingDependency)) {
                    throw new ClassNotFoundException(name);
                }
                if (name.equals(brokenController) || name.equals(healthyController)) {
                    synchronized (getClassLoadingLock(name)) {
                        Class<?> loaded = findLoadedClass(name);
                        if (loaded == null) {
                            loaded = findClass(name);
                        }
                        if (resolve) {
                            resolveClass(loaded);
                        }
                        return loaded;
                    }
                }
                return super.loadClass(name, resolve);
            }
        }) {
            PluginRouteScanner.ScanRequest request = new PluginRouteScanner.ScanRequest(
                    "geo", jar, loader, null, List.of());
            List<RouteRegistry.RouteEntry> entries = new PluginRouteScanner().scan(request);

            assertEquals(1, entries.size(), "the healthy controller must remain available");
            assertNotNull(find(entries, "GET", "/healthy/ping"));
        }
    }

    @Test
    void doesNotLoadUnannotatedClassesButKeepsMethodOnlyHouRouteCompatibility() throws Exception {
        Path jar = buildPluginJar(List.of(
                "PluginRouteScannerTest$UnannotatedOptionalIntegration",
                "PluginRouteScannerTest$MethodOnlyCouponController"), "route-candidates.jar");
        String optionalIntegration = UnannotatedOptionalIntegration.class.getName();
        String methodOnlyController = MethodOnlyCouponController.class.getName();
        AtomicInteger optionalIntegrationLoadAttempts = new AtomicInteger();

        try (URLClassLoader loader = new URLClassLoader(new URL[]{jar.toUri().toURL()}, getClass().getClassLoader()) {
            @Override
            protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                if (name.equals(optionalIntegration)) {
                    optionalIntegrationLoadAttempts.incrementAndGet();
                }
                if (name.equals(optionalIntegration) || name.equals(methodOnlyController)) {
                    synchronized (getClassLoadingLock(name)) {
                        Class<?> loaded = findLoadedClass(name);
                        if (loaded == null) {
                            loaded = findClass(name);
                        }
                        if (resolve) {
                            resolveClass(loaded);
                        }
                        return loaded;
                    }
                }
                return super.loadClass(name, resolve);
            }
        }) {
            PluginRouteScanner.ScanRequest request = new PluginRouteScanner.ScanRequest(
                    "demo-coupon", jar, loader, null, List.of());
            List<RouteRegistry.RouteEntry> entries = new PluginRouteScanner().scan(request);

            assertEquals(0, optionalIntegrationLoadAttempts.get(),
                    "classes without route annotations must not be loaded");
            assertEquals(1, entries.size());
            RouteRegistry.RouteEntry claim = find(entries, "POST", "/coupon/claim");
            assertNotNull(claim, "method-only HouRoute plugins must remain compatible");
            assertEquals("coupon:claim", claim.permission);
        }
    }

    private RouteRegistry.RouteEntry find(List<RouteRegistry.RouteEntry> entries, String method, String path) {
        return entries.stream()
                .filter(e -> e.httpMethod.equalsIgnoreCase(method) && e.path.equals(path))
                .findFirst()
                .orElse(null);
    }
}
