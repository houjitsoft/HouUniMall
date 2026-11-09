package com.houjiang.plugin.engine;

import com.houjiang.plugin.engine.config.PluginEngineProperties;
import com.houjiang.plugin.engine.core.PluginBootGate;
import com.houjiang.plugin.engine.core.PluginLifecycleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarFile;
import java.util.stream.Stream;

/**
 * 宿主启动装载器（R3 关闭「引擎与宿主假连接」缺口）。
 *
 * <p>仅当 {@code houcloud.plugin.boot-enabled=true} 时由 {@link PluginEngineAutoConfiguration} 装配。
 * 宿主进程启动后扫描 pluginsDir 下所有插件 JAR 并依次 {@code install}（装载 + 启动 + 注册路由与扩展点），
 * 使 PF4J 真正运行、扩展点注册表生效。仅使用落盘存储的模块该开关保持 false，不触发 PF4J 启动。
 */
public class PluginEngineBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(PluginEngineBootstrap.class);

    private final PluginLifecycleManager lifecycleManager;
    private final PluginEngineProperties properties;

    /**
     * 启动装载窗口闸门：装载期间 {@code /plugin/{code}/**} 未命中路由回 503 而不是 404，
     * 避免每次重启都给前端喂一批假 404（见 {@link PluginBootGate}）。
     */
    private final PluginBootGate bootGate;

    public PluginEngineBootstrap(PluginLifecycleManager lifecycleManager,
                                 PluginEngineProperties properties,
                                 PluginBootGate bootGate) {
        this.lifecycleManager = lifecycleManager;
        this.properties = properties;
        this.bootGate = bootGate;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!properties.isBootEnabled()) {
            log.debug("[插件引擎] houcloud.plugin.boot-enabled=false，跳过自动装载插件");
            return;
        }
        Path root = Path.of(properties.getPluginsDir()).toAbsolutePath().normalize();
        if (!Files.isDirectory(root)) {
            log.info("[插件引擎] 插件目录不存在，跳过自动装载: {}", root);
            return;
        }
        log.info("[插件引擎] 启动装载插件目录: {}", root);
        // 装载跑在 Servlet 容器开始监听之后，这段窗口里路由表是空的；
        // 打开闸门把「还没装完」表达成 503，装完（无论成功失败）必须关掉，否则会永久假 503。
        bootGate.begin();
        try {
            loadAll(root);
        } finally {
            bootGate.complete();
            log.info("[插件引擎] 启动装载窗口已关闭，插件路由全部就绪");
        }
    }

    /**
     * 落盘契约为 {pluginsRoot}/{code}/{version}/{code}-{version}.jar（见 PluginArtifactStore），
     * 故递归遍历而非只扫一层；.tmp 半成品因后缀不匹配被自然排除；排序保证装载顺序稳定可复现。
     */
    private void loadAll(Path root) {
        try (Stream<Path> stream = Files.walk(root)) {
            stream.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName() != null && p.toString().endsWith(".jar"))
                    .sorted()
                    .forEach(jar -> {
                        String code = parseCode(jar);
                        if (!hasPf4jDescriptor(jar)) {
                            log.info("[插件引擎] 跳过非 PF4J 后端包 {} <- {}", code, jar.getFileName());
                            return;
                        }
                        try {
                            lifecycleManager.install(code, jar);
                            log.info("[插件引擎] 已装载插件 {} <- {}", code, jar.getFileName());
                        } catch (IllegalStateException e) {
                            // 多宿主共用一个插件卷是默认部署形态（base 与 mall、order 都挂
                            // ${PLUGINS_DIR}），目录里必然存在别的宿主负责的 jar。那属于
                            // 正常情况，不该刷 ERROR 日志把真正的装载失败淹没。
                            String msg = e.getMessage() == null ? "" : e.getMessage();
                            if (msg.startsWith("Plugin hostService does not match this service")) {
                                log.info("[插件引擎] 跳过非本服务宿主的插件 {} <- {}（{}）",
                                        code, jar.getFileName(), msg);
                                return;
                            }
                            log.error("[插件引擎] 装载插件失败 {} <- {}", code, jar.getFileName(), e);
                        } catch (Exception e) {
                            log.error("[插件引擎] 装载插件失败 {} <- {}", code, jar.getFileName(), e);
                        }
                    });
        } catch (Exception e) {
            log.error("[插件引擎] 扫描插件目录失败: {}", root, e);
        }
    }

    private static boolean hasPf4jDescriptor(Path jar) {
        try (JarFile file = new JarFile(jar.toFile())) {
            if (file.getManifest() == null) {
                return false;
            }
            var attributes = file.getManifest().getMainAttributes();
            String pluginId = attributes.getValue("Plugin-Id");
            String pluginClass = attributes.getValue("Plugin-Class");
            return pluginId != null && !pluginId.isBlank()
                    && pluginClass != null && !pluginClass.isBlank();
        } catch (Exception e) {
            log.warn("[插件引擎] 无法读取 PF4J 描述符，跳过 {}: {}", jar, e.getMessage());
            return false;
        }
    }

    /**
     * 由文件名 {@code {code}-{version}.jar} 解析插件 code。
     */
    private static String parseCode(Path jar) {
        String fileName = jar.getFileName().toString();
        // Prefer the artifact layout {root}/{code}/{version}/{code}-{version}.jar.
        // This remains correct when a semantic version contains '-' (for example
        // 1.0.0-beta), where splitting the filename alone is ambiguous.
        Path versionDir = jar.getParent();
        if (versionDir != null && versionDir.getParent() != null) {
            String codeDir = versionDir.getParent().getFileName().toString();
            if (fileName.startsWith(codeDir + "-")) {
                return codeDir;
            }
        }
        if (versionDir != null) {
            String codeDir = versionDir.getFileName().toString();
            if (fileName.startsWith(codeDir + "-")) {
                return codeDir;
            }
        }
        String n = fileName;
        if (n.endsWith(".jar")) {
            n = n.substring(0, n.length() - 4);
        }
        int dash = n.lastIndexOf('-');
        return dash > 0 ? n.substring(0, dash) : n;
    }
}
