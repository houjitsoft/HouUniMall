package com.houjiang.plugin.engine.route;

import com.houjiang.plugin.engine.registry.RouteRegistry;
import com.houjiang.plugin.sdk.annotation.HouCaller;
import com.houjiang.plugin.sdk.annotation.HouExtension;
import com.houjiang.plugin.sdk.annotation.HouRoute;
import com.houjiang.plugin.sdk.annotation.HouRouteDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.jar.JarFile;

/**
 * 插件路由扫描器：从 {@code PluginLifecycleManager} 抽出的深模块。
 *
 * <p>对外只有一个方法 {@link #scan(ScanRequest)}：给定插件编码、插件路径（JAR 或含
 * {@code classes/} 的目录）、ClassLoader、可选 Spring {@code BeanFactory} 与可剥离的路径前缀，
 * 产出完整的 {@link RouteRegistry.RouteEntry} 列表。所有反射、注解解析、路径归一、Class 扫描
 * 都藏在实现里，因此可脱离 PF4J 单独单测。
 *
 * <p><b>前缀剥离</b>：插件 Controller 常带业务前缀（如 ERP 的 {@code /api/erp}），由宿主通过
 * {@code houcloud.plugin.route-strip-prefixes} 配置注入，引擎自身不再硬编码任何业务前缀——
 * 这是把通用核心与商业插件解耦的必要一步（方案 §2.4）。
 *
 * @author HouCloud
 */
public final class PluginRouteScanner {

    private static final Logger log = LoggerFactory.getLogger(PluginRouteScanner.class);
    private static final List<byte[]> ROUTE_ANNOTATION_DESCRIPTORS = List.of(
            descriptor(HouRoute.class),
            descriptor(HouRouteDefaults.class),
            descriptor(RequestMapping.class),
            descriptor(GetMapping.class),
            descriptor(PostMapping.class),
            descriptor(PutMapping.class),
            descriptor(DeleteMapping.class));

    /**
     * 扫描单个插件，返回其全部路由条目（已设置 code/path/httpMethod/bean/method/permission）。
     */
    public List<RouteRegistry.RouteEntry> scan(ScanRequest request) {
        List<RouteRegistry.RouteEntry> entries = new ArrayList<>();
        try {
            List<String> classNames = routeCandidateClassNames(request.pluginPath());
            ClassLoader loader = request.classLoader();
            for (String className : classNames) {
                Class<?> type;
                try {
                    type = Class.forName(className, false, loader);
                } catch (LinkageError | ClassNotFoundException ignored) {
                    continue;
                }
                if (type.isInterface()
                        || type.isAnnotation()
                        || type.isEnum()
                        || Modifier.isAbstract(type.getModifiers())) {
                    continue;
                }
                try {
                    Object bean = null;
                    // 类级 @RequestMapping 允许声明多个别名（如 {"geo/aiTokenUsage", "geo/ai"}），
                    // 每个别名都是一条独立可访问的路径前缀，必须全部登记；只取 path[0] 会让
                    // 靠后别名下的全部接口 404。
                    List<String> classPrefixes = controllerPrefixes(type);
                    HouRouteDefaults defaults = type.getAnnotation(HouRouteDefaults.class);
                    // 类级 @HouCaller：对全类生效，方法级声明优先且取并集
                    java.util.Set<String> classCallers =
                            toCallerSet(AnnotationUtils.getAnnotation(type, HouCaller.class));
                    for (Method method : type.getDeclaredMethods()) {
                        List<RouteSpec> specs =
                                routeSpecs(method, classPrefixes, defaults, classCallers, request.stripPrefixes());
                        if (specs.isEmpty()) {
                            continue;
                        }
                        if (bean == null) {
                            try {
                                bean = request.beanFactory() == null
                                        ? type.getDeclaredConstructor().newInstance()
                                        : request.beanFactory().createBean(type);
                            } catch (RuntimeException | ReflectiveOperationException ex) {
                                // A plugin may expose optional integrations whose
                                // external client is not installed in this host.
                                // Keep the rest of the plugin routes available.
                                log.warn("Skipping unavailable plugin controller {}: {}", className, ex.getMessage());
                                break;
                            }
                        }
                        method.setAccessible(true);
                        for (RouteSpec spec : specs) {
                            RouteRegistry.RouteEntry entry = new RouteRegistry.RouteEntry();
                            entry.code = request.pluginCode();
                            // 方法级路径同样参与前缀剥离：无类级 @RequestMapping 的 Controller
                            // 习惯把业务前缀直接写死在方法上（如 GEO 的 /geo/content/expand-modes），
                            // 若只剥类级前缀，这类路由会以带前缀的形式登记，而分发器剥掉 /plugin/{code}
                            // 后拿到的 path 不带前缀，永远匹配不上，表现为整批接口 404。
                            // 前缀剥离后路径为空（类级/方法级路径恰好等于被剥离的前缀，或注解漏写路径）
                            // 只该丢弃这一条路由并告警：整条抛出会让「插件安装失败并回滚」，
                            // 一个漏写路径的方法就拖垮该插件的全部接口（ERP 曾因此整批 404）。
                            // 前缀剥离已在 routeSpecs 内对「类级 + 方法级」完整路径完成，此处不再二次剥离。
                            String strippedPath = spec.path();
                            if (strippedPath == null || strippedPath.isBlank()) {
                                log.error("插件 {} 路由路径为空已跳过：class={} method={} 原始路径={} 类级前缀={}"
                                                + "（请补写方法级路径，或检查 route-strip-prefixes 是否剥过了头）",
                                        request.pluginCode(), className, method.getName(), spec.path(), classPrefixes);
                                continue;
                            }
                            entry.path = normalizeRoutePath(strippedPath);
                            entry.httpMethod = spec.method().toUpperCase();
                            entry.bean = bean;
                            entry.method = method;
                            entry.permission = spec.permission();
                            entry.allowCallers = spec.allowCallers();
                            entries.add(entry);
                        }
                    }
                } catch (LinkageError ex) {
                    // Loading the class itself does not resolve every method
                    // signature. Optional SDKs can therefore fail only when
                    // reflection asks for declared methods. One unavailable
                    // controller must not abort the host or other plugins.
                    log.warn("Skipping plugin controller {} because a dependency is unavailable: {}",
                            className, ex.toString());
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("插件路由扫描失败: " + request.pluginCode(), e);
        }
        logPermissionInventory(request.pluginCode(), entries);
        return entries;
    }

    /**
     * 输出「未声明权限路由」清单。
     *
     * <p>未声明权限的路由默认按「仅校验登录态」放行（历史兼容），是提权面：
     * 任何登录用户都能调到该插件的接口。这里在扫描期把清单打出来，作为迁移
     * {@code houcloud.plugin.require-declared-route-permission=true}（fail-closed）的输入，
     * 避免开启时才发现哪些接口会 403。
     */
    private static void logPermissionInventory(String pluginCode, List<RouteRegistry.RouteEntry> entries) {
        List<RouteRegistry.RouteEntry> undeclared = entries.stream()
                .filter(entry -> entry.permission == null || entry.permission.isBlank())
                .toList();
        if (undeclared.isEmpty()) {
            log.info("插件 {} 路由扫描完成：共 {} 条，权限声明齐全", pluginCode, entries.size());
            return;
        }
        int sampleSize = 10;
        String sample = undeclared.stream()
                .limit(sampleSize)
                .map(entry -> entry.httpMethod + " " + entry.path
                        + " ← " + entry.method.getDeclaringClass().getName() + "#" + entry.method.getName())
                .collect(java.util.stream.Collectors.joining("; "));
        log.warn("插件 {} 路由扫描完成：共 {} 条，其中 {} 条未声明权限（暂按仅校验登录态放行，"
                        + "开启 require-declared-route-permission 前需先补齐权限声明）：{}{}",
                pluginCode, entries.size(), undeclared.size(), sample,
                undeclared.size() > sampleSize ? " …（共 " + undeclared.size() + " 条，此处仅列前 " + sampleSize + " 条）" : "");
    }

    private static String normalizeRoutePath(String path) {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("插件路由不能为空");
        }
        String normalized = path.startsWith("/") ? path : "/" + path;
        return normalized.replaceAll("/{2,}", "/");
    }

    /**
     * 解析类级 {@code @RequestMapping} 声明的全部路径前缀（<b>原始值，未剥离</b>）。
     *
     * <p>无类级注解时返回单个空串——方法上写的就是全路径。有注解时按声明顺序去重返回
     * 每个别名，保持与 Spring 一致的语义。</p>
     *
     * <p><b>为什么不在这里剥离</b>：剥离必须作用于「类级 + 方法级拼出的完整路径」。
     * 早期版本在类级与方法级各剥一次，于是配置里的短前缀（{@code /finance}、{@code /supplier}）
     * 会把同名的方法级路径剥空或剥残——ERP 的 {@code /api/erp/finance} 先被 {@code /api/erp}
     * 剥成 {@code /finance}，再被 {@code /finance} 剥成空，接口直接消失（2026-09-20）。</p>
     */
    private static List<String> controllerPrefixes(Class<?> type) {
        RequestMapping mapping = AnnotationUtils.getAnnotation(type, RequestMapping.class);
        if (mapping == null || mapping.path().length == 0) {
            return List.of("");
        }
        LinkedHashSet<String> prefixes = new LinkedHashSet<>();
        for (String path : mapping.path()) {
            prefixes.add(path == null ? "" : path);
        }
        return List.copyOf(prefixes);
    }

    /**
     * 剥离宿主配置的业务前缀（{@code houcloud.plugin.route-strip-prefixes}）。
     *
     * <p>在全部候选中取<b>最长匹配</b>的那一个，而不是「第一个匹配」：配置里同时存在
     * {@code /api/erp} 与 {@code /finance} 这类长短前缀时，按声明顺序命中短前缀会把
     * {@code /api/erp/finance} 先剥成 {@code /finance}（看似正确），但下一次/另一条路径
     * 又会被继续剥空。对完整路径只剥一次且取最长，语义才稳定。</p>
     */
    private static String stripPrefix(String value, List<String> stripPrefixes) {
        if (value == null || stripPrefixes == null) {
            return value;
        }
        String matched = null;
        for (String prefix : stripPrefixes) {
            if (prefix == null || prefix.isBlank() || !value.startsWith(prefix)) {
                continue;
            }
            if (matched == null || prefix.length() > matched.length()) {
                matched = prefix;
            }
        }
        return matched == null ? value : value.substring(matched.length());
    }

    private static List<RouteSpec> routeSpecs(Method method, List<String> prefixes, HouRouteDefaults defaults,
                                              java.util.Set<String> classCallers, List<String> stripPrefixes) {
        List<RouteSpec> specs = new ArrayList<>();
        // 方法级 @HouCaller 优先于类级；两者都声明时取并集，避免类级「只放行 internal」
        // 把方法级额外放行的调用方挤掉（或反之）。
        java.util.Set<String> callers = mergeCallers(
                toCallerSet(AnnotationUtils.getAnnotation(method, HouCaller.class)), classCallers);
        HouRoute houRoute = method.getAnnotation(HouRoute.class);
        if (houRoute != null) {
            // HouRoute is the plugin contract. A Spring mapping on the same
            // method is only an implementation detail and must not add a
            // second entry that could overwrite its explicit permission.
            specs.add(new RouteSpec(houRoute.path(), houRoute.method(), houRoute.permission(),
                    mergeCallers(toCallerSet(houRoute.allowCallers()), callers)));
            return stripAll(specs, stripPrefixes);
        }
        for (String prefix : prefixes) {
            addSpringMappings(specs, AnnotationUtils.getAnnotation(method, GetMapping.class), prefix, "GET",
                    permissionFor(defaults, "GET"), callers);
            addSpringMappings(specs, AnnotationUtils.getAnnotation(method, PostMapping.class), prefix, "POST",
                    permissionFor(defaults, "POST"), callers);
            addSpringMappings(specs, AnnotationUtils.getAnnotation(method, PutMapping.class), prefix, "PUT",
                    permissionFor(defaults, "PUT"), callers);
            addSpringMappings(specs, AnnotationUtils.getAnnotation(method, DeleteMapping.class), prefix, "DELETE",
                    permissionFor(defaults, "DELETE"), callers);
            if (method.getDeclaredAnnotation(RequestMapping.class) != null) {
                RequestMapping generic = AnnotationUtils.getAnnotation(method, RequestMapping.class);
                String[] methods = generic.method().length == 0
                        ? new String[]{"GET", "POST", "PUT", "DELETE"}
                        : Arrays.stream(generic.method()).map(Object::toString).toArray(String[]::new);
                for (String methodName : methods) {
                    addSpringMappings(specs, generic.path(), new String[]{methodName}, prefix,
                            permissionFor(defaults, methodName), callers);
                }
            }
        }
        return stripAll(specs, stripPrefixes);
    }

    /**
     * 对已拼成完整路径的路由条目统一剥离业务前缀（只剥一次，取最长匹配）。
     */
    private static List<RouteSpec> stripAll(List<RouteSpec> specs, List<String> stripPrefixes) {
        return specs.stream()
                .distinct()
                .map(spec -> new RouteSpec(stripPrefix(spec.path(), stripPrefixes), spec.method(),
                        spec.permission(), spec.allowCallers()))
                .toList();
    }

    private static void addSpringMappings(List<RouteSpec> specs, GetMapping mapping, String prefix, String method,
                                          String permission, java.util.Set<String> callers) {
        if (mapping != null) {
            addSpringMappings(specs, mapping.path(), new String[]{method}, prefix, permission, callers);
        }
    }

    private static void addSpringMappings(List<RouteSpec> specs, PostMapping mapping, String prefix, String method,
                                          String permission, java.util.Set<String> callers) {
        if (mapping != null) {
            addSpringMappings(specs, mapping.path(), new String[]{method}, prefix, permission, callers);
        }
    }

    private static void addSpringMappings(List<RouteSpec> specs, PutMapping mapping, String prefix, String method,
                                          String permission, java.util.Set<String> callers) {
        if (mapping != null) {
            addSpringMappings(specs, mapping.path(), new String[]{method}, prefix, permission, callers);
        }
    }

    private static void addSpringMappings(List<RouteSpec> specs, DeleteMapping mapping, String prefix, String method,
                                          String permission, java.util.Set<String> callers) {
        if (mapping != null) {
            addSpringMappings(specs, mapping.path(), new String[]{method}, prefix, permission, callers);
        }
    }

    private static void addSpringMappings(List<RouteSpec> specs, String[] paths, String[] methods,
                                          String prefix, String permission, java.util.Set<String> callers) {
        String[] safePaths = paths == null || paths.length == 0 ? new String[]{""} : paths;
        for (String path : safePaths) {
            String full = normalizeRoutePath(prefix + "/" + (path == null ? "" : path));
            for (String method : methods) {
                specs.add(new RouteSpec(full, method, permission, callers));
            }
        }
    }

    /**
     * 把注解上的 {@code allowCallers} 归一化成不可变集合。
     *
     * <p>空数组返回 {@code null}（＝ 不做调用方限制），避免给每条路由都挂一个空 Set，
     * 也让「未声明」与「声明了但被清空」在判定处走同一条分支。
     */
    private static java.util.Set<String> toCallerSet(String[] allowCallers) {
        if (allowCallers == null || allowCallers.length == 0) {
            return null;
        }
        java.util.LinkedHashSet<String> normalized = new java.util.LinkedHashSet<>();
        for (String caller : allowCallers) {
            if (caller != null && !caller.isBlank()) {
                normalized.add(caller.trim().toLowerCase());
            }
        }
        return normalized.isEmpty() ? null : java.util.Collections.unmodifiableSet(normalized);
    }

    /**
     * 合并两级调用方声明：{@code primary}（方法级 / {@code @HouRoute}）优先保留，
     * {@code fallback}（类级）作为基底。任一侧为空时直接返回另一侧，保持「未声明即不限制」。
     */
    private static java.util.Set<String> mergeCallers(java.util.Set<String> primary,
                                                      java.util.Set<String> fallback) {
        if (primary == null || primary.isEmpty()) {
            return fallback;
        }
        if (fallback == null || fallback.isEmpty()) {
            return primary;
        }
        java.util.LinkedHashSet<String> merged = new java.util.LinkedHashSet<>(fallback);
        merged.addAll(primary);
        return java.util.Collections.unmodifiableSet(merged);
    }

    private static java.util.Set<String> toCallerSet(HouCaller caller) {
        return caller == null ? null : toCallerSet(caller.value());
    }

    private static String permissionFor(HouRouteDefaults defaults, String method) {
        if (defaults == null || method == null) {
            return "";
        }
        return "GET".equalsIgnoreCase(method) ? defaults.readPermission() : defaults.writePermission();
    }

    private record RouteSpec(String path, String method, String permission,
                             java.util.Set<String> allowCallers) { }

    /**
     * 扫描插件中的 {@link HouExtension} 实现类，返回可直接实例化的扩展点类。
     * 与路由扫描共用 Class 枚举逻辑与插件 ClassLoader，因此可一次遍历同时得到路由与扩展点。
     */
    public List<Class<?>> scanExtensions(ScanRequest request) {
        List<Class<?>> result = new ArrayList<>();
        try {
            ClassLoader loader = request.classLoader();
            for (String className : classNames(request.pluginPath())) {
                try {
                    Class<?> type = Class.forName(className, false, loader);
                    if (!type.isInterface()
                            && type.isAnnotationPresent(HouExtension.class)
                            && !java.lang.reflect.Modifier.isAbstract(type.getModifiers())) {
                        result.add(type);
                    }
                } catch (LinkageError | ClassNotFoundException ignored) {
                    // 跳过不可加载的类
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("插件扩展点扫描失败: " + request.pluginCode(), e);
        }
        return result;
    }

    private static List<String> classNames(Path pluginPath) throws IOException {
        List<String> names = new ArrayList<>();
        if (Files.isDirectory(pluginPath)) {
            try (var stream = Files.walk(pluginPath.resolve("classes"))) {
                stream.filter(p -> p.toString().endsWith(".class"))
                        .forEach(p -> names.add(toClassName(pluginPath.resolve("classes"), p)));
            }
            names.sort(String::compareTo);
            return names;
        }
        try (JarFile jar = new JarFile(pluginPath.toFile())) {
            jar.stream().filter(e -> !e.isDirectory() && e.getName().endsWith(".class"))
                    .forEach(e -> names.add(e.getName().substring(0, e.getName().length() - 6).replace('/', '.')));
        }
        names.sort(String::compareTo);
        return names;
    }

    private static List<String> routeCandidateClassNames(Path pluginPath) throws IOException {
        List<String> names = new ArrayList<>();
        if (Files.isDirectory(pluginPath)) {
            Path classesRoot = pluginPath.resolve("classes");
            try (var stream = Files.walk(classesRoot)) {
                for (Path file : stream.filter(path -> path.toString().endsWith(".class")).toList()) {
                    if (containsRouteAnnotation(Files.readAllBytes(file))) {
                        names.add(toClassName(classesRoot, file));
                    }
                }
            }
        } else {
            try (JarFile jar = new JarFile(pluginPath.toFile())) {
                var entries = jar.entries();
                while (entries.hasMoreElements()) {
                    var entry = entries.nextElement();
                    if (entry.isDirectory()
                            || !entry.getName().endsWith(".class")
                            || entry.getName().startsWith("META-INF/")) {
                        continue;
                    }
                    try (var input = jar.getInputStream(entry)) {
                        if (containsRouteAnnotation(input.readAllBytes())) {
                            names.add(entry.getName().substring(0, entry.getName().length() - 6)
                                    .replace('/', '.'));
                        }
                    }
                }
            }
        }
        names.sort(String::compareTo);
        return names;
    }

    private static byte[] descriptor(Class<?> annotationType) {
        return ("L" + annotationType.getName().replace('.', '/') + ";")
                .getBytes(StandardCharsets.ISO_8859_1);
    }

    private static boolean containsRouteAnnotation(byte[] classBytes) {
        for (byte[] descriptor : ROUTE_ANNOTATION_DESCRIPTORS) {
            if (contains(classBytes, descriptor)) {
                return true;
            }
        }
        return false;
    }

    private static boolean contains(byte[] bytes, byte[] candidate) {
        outer:
        for (int i = 0; i <= bytes.length - candidate.length; i++) {
            for (int j = 0; j < candidate.length; j++) {
                if (bytes[i + j] != candidate[j]) {
                    continue outer;
                }
            }
            return true;
        }
        return false;
    }

    private static String toClassName(Path root, Path file) {
        String relative = root.relativize(file).toString();
        return relative.substring(0, relative.length() - 6).replace(java.io.File.separatorChar, '.');
    }

    /**
     * 一次路由扫描的入参。字段皆为不可变，构造后即只读。
     */
    public static final class ScanRequest {
        private final String pluginCode;
        private final Path pluginPath;
        private final ClassLoader classLoader;
        private final AutowireCapableBeanFactory beanFactory;
        private final List<String> stripPrefixes;

        public ScanRequest(String pluginCode, Path pluginPath, ClassLoader classLoader,
                           AutowireCapableBeanFactory beanFactory, List<String> stripPrefixes) {
            this.pluginCode = pluginCode;
            this.pluginPath = pluginPath;
            this.classLoader = classLoader;
            this.beanFactory = beanFactory;
            this.stripPrefixes = stripPrefixes == null ? List.of() : List.copyOf(stripPrefixes);
        }

        public String pluginCode() {
            return pluginCode;
        }

        public Path pluginPath() {
            return pluginPath;
        }

        public ClassLoader classLoader() {
            return classLoader;
        }

        public AutowireCapableBeanFactory beanFactory() {
            return beanFactory;
        }

        public List<String> stripPrefixes() {
            return stripPrefixes;
        }
    }
}
