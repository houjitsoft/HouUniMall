package com.houjiang.plugin.engine.core;

import com.houjiang.plugin.engine.registry.ExtensionRegistry;
import com.houjiang.plugin.engine.registry.PluginRegistry;
import com.houjiang.plugin.engine.registry.RouteRegistry;
import com.houjiang.plugin.engine.route.PluginRouteScanner;
import com.houjiang.plugin.engine.spi.PluginMigrationPort;
import com.houjiang.plugin.engine.spi.PluginManifestProvider;
import com.houjiang.plugin.engine.spi.PluginMenuPort;
import com.houjiang.plugin.sdk.annotation.HouExtension;
import com.houjiang.plugin.sdk.extension.ExtensionMode;
import com.houjiang.plugin.sdk.extension.ExtensionPoint;
import com.houjiang.plugin.sdk.model.PluginManifest;
import com.houjiang.plugin.sdk.model.PluginServiceOwnership;
import org.pf4j.DefaultPluginManager;
import org.pf4j.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

/**
 * 插件生命周期状态机（方案 4.6）。
 *
 * <p>本类把「装载 → 校验 → 激活 → 注册扩展点/路由 → 卸载」串起来，并维护
 * {@link PluginRegistry} 中的状态。状态流转：
 * <pre>
 * RESOLVED → DOWNLOADED → VERIFIED → ACTIVE ⇄ DISABLED → UNINSTALLED
 *                                    ↘ FAILED
 * </pre>
 *
 * <p><b>已实现</b>：从已落盘安装包真实装载（PF4J {@code loadPlugin} + {@code startPlugin}）、
 * 卸载（{@code stopPlugin} + {@code unloadPlugin} + 注册表清理）。
 * <b>未实现</b>：schema 迁移（MIGRATED）、签名验签——后续迭代填充。
 * 失败回滚与健康检查已实现（2026-09-13）。
 *
 * @author HouCloud
 */
public class PluginLifecycleManager {

    private static final Logger log = LoggerFactory.getLogger(PluginLifecycleManager.class);

    private final DefaultPluginManager pluginManager;
    private final ExtensionRegistry extensionRegistry;
    private final RouteRegistry routeRegistry;
    private final PluginRegistry pluginRegistry;
    private final AutowireCapableBeanFactory beanFactory;
    private final PluginRouteScanner routeScanner;
    private final List<String> routeStripPrefixes;
    private final PluginSpringContextManager springContexts;

    /** 可选：插件 schema 迁移（宿主注入）；为 null 时跳过 MIGRATED 阶段 */
    private final PluginMigrationPort migrationPort;
    private final PluginMenuPort menuPort;
    private final PluginManifestProvider manifestProvider;
    private final String hostService;
    private final boolean allowLegacyHost;

    public PluginLifecycleManager(DefaultPluginManager pluginManager,
                                  ExtensionRegistry extensionRegistry,
                                  RouteRegistry routeRegistry,
                                  PluginRegistry pluginRegistry) {
        this(pluginManager, extensionRegistry, routeRegistry, pluginRegistry, null, List.of());
    }

    public PluginLifecycleManager(DefaultPluginManager pluginManager,
                                  ExtensionRegistry extensionRegistry,
                                  RouteRegistry routeRegistry,
                                  PluginRegistry pluginRegistry,
                                  AutowireCapableBeanFactory beanFactory) {
        this(pluginManager, extensionRegistry, routeRegistry, pluginRegistry, beanFactory, List.of());
    }

    public PluginLifecycleManager(DefaultPluginManager pluginManager,
                                  ExtensionRegistry extensionRegistry,
                                  RouteRegistry routeRegistry,
                                  PluginRegistry pluginRegistry,
                                  AutowireCapableBeanFactory beanFactory,
                                  List<String> routeStripPrefixes) {
        this(pluginManager, extensionRegistry, routeRegistry, pluginRegistry, beanFactory,
                routeStripPrefixes, null);
    }

    /**
     * @param migrationPort 可选：插件 schema 迁移（宿主注入）；null 表示跳过 MIGRATED 阶段
     */
    public PluginLifecycleManager(DefaultPluginManager pluginManager,
                                  ExtensionRegistry extensionRegistry,
                                  RouteRegistry routeRegistry,
                                  PluginRegistry pluginRegistry,
                                  AutowireCapableBeanFactory beanFactory,
                                  List<String> routeStripPrefixes,
                                  PluginMigrationPort migrationPort) {
        this(pluginManager, extensionRegistry, routeRegistry, pluginRegistry, beanFactory,
                routeStripPrefixes, migrationPort, null, null);
    }

    public PluginLifecycleManager(DefaultPluginManager pluginManager,
                                  ExtensionRegistry extensionRegistry,
                                  RouteRegistry routeRegistry,
                                  PluginRegistry pluginRegistry,
                                  AutowireCapableBeanFactory beanFactory,
                                  List<String> routeStripPrefixes,
                                  PluginMigrationPort migrationPort,
                                  PluginMenuPort menuPort,
                                  PluginSpringContextManager springContexts) {
        this(pluginManager, extensionRegistry, routeRegistry, pluginRegistry, beanFactory,
                routeStripPrefixes, migrationPort, menuPort, springContexts, null);
    }

    /**
     * Full constructor used by a production host. The manifest provider is
     * shared by marketplace installation and restart recovery so the artifact
     * remains the authoritative source for owner/runtime metadata.
     */
    public PluginLifecycleManager(DefaultPluginManager pluginManager,
                                  ExtensionRegistry extensionRegistry,
                                  RouteRegistry routeRegistry,
                                  PluginRegistry pluginRegistry,
                                  AutowireCapableBeanFactory beanFactory,
                                  List<String> routeStripPrefixes,
                                  PluginMigrationPort migrationPort,
                                  PluginMenuPort menuPort,
                                  PluginSpringContextManager springContexts,
                                  PluginManifestProvider manifestProvider) {
        this(pluginManager, extensionRegistry, routeRegistry, pluginRegistry, beanFactory,
                routeStripPrefixes, migrationPort, menuPort, springContexts, manifestProvider, null, false);
    }

    public PluginLifecycleManager(DefaultPluginManager pluginManager,
                                  ExtensionRegistry extensionRegistry,
                                  RouteRegistry routeRegistry,
                                  PluginRegistry pluginRegistry,
                                  AutowireCapableBeanFactory beanFactory,
                                  List<String> routeStripPrefixes,
                                  PluginMigrationPort migrationPort,
                                  PluginMenuPort menuPort,
                                  PluginSpringContextManager springContexts,
                                  PluginManifestProvider manifestProvider,
                                  String hostService,
                                  boolean allowLegacyHost) {
        this.pluginManager = pluginManager;
        this.extensionRegistry = extensionRegistry;
        this.routeRegistry = routeRegistry;
        this.pluginRegistry = pluginRegistry;
        this.beanFactory = beanFactory;
        this.routeScanner = new PluginRouteScanner();
        this.routeStripPrefixes = routeStripPrefixes == null ? List.of() : List.copyOf(routeStripPrefixes);
        this.migrationPort = migrationPort;
        this.menuPort = menuPort;
        this.springContexts = springContexts;
        this.manifestProvider = manifestProvider;
        this.hostService = hostService;
        this.allowLegacyHost = allowLegacyHost;
    }

    public PluginLifecycleManager(DefaultPluginManager pluginManager,
                                  ExtensionRegistry extensionRegistry,
                                  RouteRegistry routeRegistry,
                                  PluginRegistry pluginRegistry,
                                  AutowireCapableBeanFactory beanFactory,
                                  List<String> routeStripPrefixes,
                                  PluginMigrationPort migrationPort,
                                  PluginMenuPort menuPort) {
        this(pluginManager, extensionRegistry, routeRegistry, pluginRegistry, beanFactory,
                routeStripPrefixes, migrationPort, menuPort, null);
    }

    /** 注册扩展点实现（由加载流程在插件 class 解析后调用），默认官方归属与 APPEND 策略。 */
    public <E extends ExtensionPoint<?>> void registerExtension(Class<E> type, E instance, String pluginCode, int priority) {
        registerExtension(type, instance, pluginCode, priority, ExtensionRegistry.OWNER_OFFICIAL);
    }

    /**
     * 注册扩展点实现（由加载流程在插件 class 解析后调用）。
     *
     * @param owner 实现归属：{@code official} 官方、{@code custom} 构建期二开、{@code thirdparty}
     *              商业插件（取自 plugin.yaml 的 {@code owner}，见 {@code PluginManifest#getOwner()}）。
     *              二开只有在明确接管官方实现时才使用 {@link com.houjiang.plugin.sdk.extension.ExtensionMode#REPLACE}。
     */
    public <E extends ExtensionPoint<?>> void registerExtension(Class<E> type, E instance, String pluginCode, int priority, String owner) {
        registerExtension(type, instance, pluginCode, priority, owner,
                com.houjiang.plugin.sdk.extension.ExtensionMode.APPEND);
    }

    /** 注册扩展点实现并显式声明 APPEND/REPLACE 策略。 */
    public <E extends ExtensionPoint<?>> void registerExtension(Class<E> type, E instance, String pluginCode,
                                                                 int priority, String owner,
                                                                 ExtensionMode mode) {
        extensionRegistry.register(type, instance, pluginCode, priority, owner, mode);
        log.info("插件 {} 注册扩展点 {}（owner={}, mode={}）", pluginCode, type.getSimpleName(), owner, mode);
    }

    /** 注册路由（由加载流程扫描 {@code @HouRoute} 后调用） */
    public void registerRoute(RouteRegistry.RouteEntry entry) {
        routeRegistry.register(entry);
        log.info("插件 {} 注册路由 {} {}", entry.code, entry.httpMethod, entry.path);
    }

    public void setState(String code, PluginState state) {
        pluginRegistry.putState(code, state);
    }

    /** 判断 PF4J 是否已经装载该插件，供插件中心兼容旧的内建目录项。 */
    public boolean isLoaded(String code) {
        try {
            return code != null && pluginManager.getPlugin(code) != null;
        } catch (RuntimeException e) {
            return false;
        }
    }

    /**
     * 从磁盘安装包真实装载插件（PF4J {@code loadPlugin} + {@code startPlugin}）。
     *
     * <p>前置：市场在线安装已把 jar 落盘到 {@code PluginArtifactStore}；本方法只负责
     * 「校验存在 → 装载 → 启动 → 置状态」，不做下载与落盘。
     *
     * <p>约定：插件描述符中的 {@code Plugin-Id} 必须等于 {@code code}，便于按编码启停与卸载。
     *
     * @param code    插件编码（= PF4J pluginId）
     * @param jarPath 已落盘的安装包路径
     * @return 终态（成功为 {@link PluginState#ACTIVE}）
     */
    public PluginState install(String code, Path jarPath) {
        return install(code, jarPath, null);
    }

    /**
     * Installs a verified artifact and publishes its manifest before scanning
     * runtime extensions. Ownership must be explicit and match this service
     * before any PF4J loading, schema migration or menu synchronization.
     */
    public PluginState install(String code, Path jarPath, PluginManifest manifest) {
        if (jarPath == null || !Files.isRegularFile(jarPath)) {
            pluginRegistry.putStateIfAbsent(code, PluginState.FAILED);
            throw new IllegalArgumentException("插件安装包不存在: " + jarPath);
        }
        String pluginId = null;
        boolean installationStarted = false;
        try {
            PluginManifest effectiveManifest = resolveManifest(code, jarPath, manifest);
            effectiveManifest.setHostService(validateLocalPlugin(code, effectiveManifest));
            installationStarted = true;
            pluginRegistry.putManifest(code, effectiveManifest);
            pluginRegistry.putState(code, PluginState.DOWNLOADED);
            pluginId = pluginManager.loadPlugin(jarPath);
            if (pluginId == null) {
                throw new IllegalStateException("插件描述符解析失败，无法装载: " + jarPath);
            }
            // 完整性校验（MD5）在市场落盘前已完成，这里推进状态即可
            pluginRegistry.putState(code, PluginState.VERIFIED);
            // schema 迁移（阶段 2）：仅在宿主注入迁移端口时执行，未注入则跳过 MIGRATED 以保持旧行为。
            // 迁移失败会进入下方 catch 走整体回滚，不会留下「表已建、插件未生效」的中间态。
            if (migrationPort != null) {
                migrationPort.migrate(code, resolveVersion(pluginId));
                pluginRegistry.putState(code, PluginState.MIGRATED);
            }
            if (menuPort != null) {
                menuPort.sync(code, jarPath);
            }
            pluginManager.startPlugin(pluginId);
            if (springContexts != null) {
                springContexts.start(code, pluginManager.getPlugin(pluginId));
            }
            // 无 Spring BeanFactory 时是纯引擎单测/工具场景，不尝试实例化插件 Controller。
            // 生产宿主始终由自动配置注入 BeanFactory，因此会执行真实路由扫描。
            if (beanFactory != null || springContexts != null) {
                scanAndRegisterRoutes(pluginId);
            }
            // 扩展点注册不依赖 BeanFactory：无工厂时用无参构造实例化，有工厂时按依赖注入装配。
            scanAndRegisterExtensions(pluginId);
            pluginRegistry.putState(code, PluginState.ACTIVE);
            log.info("插件已安装并激活：{}（pluginId={}, jar={}）", code, pluginId, jarPath);
            return PluginState.ACTIVE;
        } catch (RuntimeException e) {
            // 失败回滚（§4 安装链路 / §7.4.2 P1）：中途失败会留下「路由已注册但扩展未注册」
            // 的半生效状态（例如扩展点触发 P0 冲突），必须整体回退到安装前。
            if (installationStarted) {
                rollback(code, pluginId);
            } else {
                pluginRegistry.putStateIfAbsent(code, PluginState.FAILED);
            }
            log.error("插件安装失败：{}（{}，已开始装载={}）", code, e.getMessage(), installationStarted);
            throw e;
        }
    }

    private String validateLocalPlugin(String code, PluginManifest manifest) {
        String ownerService = PluginServiceOwnership.resolve(manifest, allowLegacyHost);
        if (code == null || !code.equals(manifest.getCode())) {
            throw new IllegalStateException("Plugin manifest code must match lifecycle code: " + code);
        }
        if (!"embedded".equals(manifest.getRuntime())) {
            throw new IllegalStateException("Only embedded plugins can be loaded by PF4J: " + code);
        }
        if (ExtensionRegistry.OWNER_CUSTOM.equalsIgnoreCase(manifest.getOwner())) {
            throw new IllegalStateException("Runtime plugin manifest cannot use owner=custom: " + code);
        }
        if (!PluginServiceOwnership.requireService(hostService).equals(ownerService)) {
            throw new IllegalStateException("Plugin hostService does not match this service: plugin="
                    + ownerService + ", engine=" + hostService);
        }
        return ownerService;
    }

    /**
     * Resolves the manifest before PF4J scans extension classes. A supplied
     * declaration (for example a marketplace row) is only supplemental; when
     * a provider is configured, the JAR's plugin.yaml wins and code/version
     * disagreements fail the installation.
     */
    private PluginManifest resolveManifest(String code, Path jarPath, PluginManifest supplied) {
        if (manifestProvider == null) {
            if (supplied != null && (supplied.getCode() == null || !code.equals(supplied.getCode()))) {
                throw new IllegalArgumentException("Plugin manifest code must match install code: " + code);
            }
            if (supplied != null && ExtensionRegistry.OWNER_CUSTOM.equalsIgnoreCase(supplied.getOwner())) {
                throw new IllegalStateException("Runtime plugin manifest cannot use owner=custom; "
                        + "build-time customizations must be assembled through backend/extensions: " + code);
            }
            return supplied;
        }
        PluginManifest artifact = manifestProvider.load(code, jarPath);
        PluginServiceOwnership.resolve(artifact, allowLegacyHost);
        if (ExtensionRegistry.OWNER_CUSTOM.equalsIgnoreCase(artifact.getOwner())) {
            throw new IllegalStateException("Runtime plugin manifest cannot use owner=custom; "
                    + "build-time customizations must be assembled through backend/extensions: " + code);
        }
        if (supplied != null) {
            if (!PluginServiceOwnership.resolve(supplied, allowLegacyHost)
                    .equals(PluginServiceOwnership.resolve(artifact, allowLegacyHost))) {
                throw new IllegalStateException("Plugin hostService conflicts with artifact ownership");
            }
            if (!java.util.Objects.equals(supplied.getRuntime(), artifact.getRuntime())) {
                throw new IllegalStateException("Plugin runtime conflicts with artifact runtime");
            }
            if (supplied.getCode() != null && !code.equals(supplied.getCode())) {
                throw new IllegalArgumentException("Plugin manifest code must match install code: " + code);
            }
            if (supplied.getVersion() != null && !supplied.getVersion().isBlank()
                    && !supplied.getVersion().equals(artifact.getVersion())) {
                throw new IllegalStateException("插件清单 version 与市场声明不一致: expected="
                        + supplied.getVersion() + ", actual=" + artifact.getVersion());
            }
        }
        return artifact;
    }

    /** 取插件版本用于迁移记录；取不到时返回 unknown，不影响安装继续。 */
    private String resolveVersion(String pluginId) {
        try {
            PluginWrapper wrapper = pluginManager.getPlugin(pluginId);
            if (wrapper == null || wrapper.getDescriptor() == null
                    || wrapper.getDescriptor().getVersion() == null) {
                return "unknown";
            }
            return wrapper.getDescriptor().getVersion();
        } catch (RuntimeException e) {
            return "unknown";
        }
    }

    /**
     * 安装失败回滚：清注册表 → 停止并卸载 ClassLoader → 置 FAILED。
     *
     * <p>清理动作自身出错只告警不抛出，避免掩盖真正的安装失败原因。
     */
    private void rollback(String code, String pluginId) {
        try {
            if (menuPort != null) {
                menuPort.remove(code);
            }
            extensionRegistry.unregisterByPlugin(code);
            routeRegistry.removeByPlugin(code);
            if (pluginId != null && !pluginId.equals(code)) {
                extensionRegistry.unregisterByPlugin(pluginId);
                routeRegistry.removeByPlugin(pluginId);
            }
            if (pluginId != null) {
                if (springContexts != null) {
                    springContexts.stop(code);
                }
                try {
                    pluginManager.stopPlugin(pluginId);
                } catch (RuntimeException ignored) {
                    // 未启动成功时停止会抛，忽略
                }
                try {
                    pluginManager.unloadPlugin(pluginId);
                } catch (RuntimeException ignored) {
                    // 卸载失败不影响状态置位
                }
            }
        } catch (RuntimeException cleanupError) {
            log.warn("插件 {} 安装回滚时清理报错：{}", code, cleanupError.getMessage());
        }
        pluginRegistry.putState(code, PluginState.FAILED);
    }

    /**
     * 健康检查（阶段 2）：插件处于 ACTIVE 且 PF4J 侧确已 STARTED 才认为健康。
     *
     * <p>用于安装后验收与运行期探针。只查状态表是不够的——注册表与引擎可能不同步
     * （例如启动后半途失败），因此同时校验 PF4J 实际状态。任何异常一律判为不健康。
     *
     * <p><b>修复</b>：原实现为 {@code wrapper == null || ...}，即「PF4J 查不到该插件」
     * 反而判为健康——状态表残留 ACTIVE 而引擎侧已无该插件时，探针会一直报健康，
     * 掩盖「注册表与引擎不一致」。现要求 wrapper 非空且确为 STARTED。
     */
    public boolean isHealthy(String code) {
        if (code == null || !pluginRegistry.isActive(code)) {
            return false;
        }
        try {
            PluginWrapper wrapper = pluginManager.getPlugin(code);
            return wrapper != null && wrapper.getPluginState() == org.pf4j.PluginState.STARTED;
        } catch (RuntimeException e) {
            log.warn("插件 {} 健康检查异常：{}", code, e.getMessage());
            return false;
        }
    }

    /**
     * A catalog entry cannot activate a runtime plugin. Retained only to reject old callers
     * explicitly; standalone product state belongs to the service/control-plane lifecycle.
     * Local plugins must use artifact installation with verified ownership.
     */
    @Deprecated
    public PluginState install(String code) {
        throw new IllegalStateException("Metadata-only installation cannot activate a plugin; "
                + "use a verified artifact or the standalone service lifecycle: " + code);
    }

    /**
     * 卸载：停止插件 → 卸载 ClassLoader → 清理注册表。
     *
     * <p>落盘产物的删除由宿主决定（{@code PluginArtifactStore.delete}），引擎不越权删文件。
     */
    public void uninstall(String code) {
        try {
            PluginWrapper wrapper = pluginManager.getPlugin(code);
            if (wrapper != null) {
                if (wrapper.getPluginState() == org.pf4j.PluginState.STARTED) {
                    pluginManager.stopPlugin(code);
                }
                pluginManager.unloadPlugin(code);
            }
        } catch (RuntimeException e) {
            log.warn("插件 {} 引擎侧卸载报错，已继续清理注册表：{}", code, e.getMessage());
        }
        if (springContexts != null) {
            springContexts.stop(code);
        }
        extensionRegistry.unregisterByPlugin(code);
        routeRegistry.removeByPlugin(code);
        if (menuPort != null) {
            menuPort.remove(code);
        }
        pluginRegistry.remove(code);
        log.info("插件已卸载：{}", code);
    }

    public void enable(String code) {
        // Reject invalid/restored metadata before entering the rollback region: a rejected
        // instruction must not stop an existing plugin or remove its routes and menus.
        validateLocalPlugin(code, pluginRegistry.getManifest(code));
        boolean started = false;
        try {
            pluginManager.startPlugin(code);
            started = true;
            if (springContexts != null) {
                springContexts.start(code, pluginManager.getPlugin(code));
            }
            if (beanFactory != null || springContexts != null) {
                scanAndRegisterRoutes(code);
            }
            scanAndRegisterExtensions(code);
            if (menuPort != null) {
                menuPort.enable(code);
            }
            pluginRegistry.putState(code, PluginState.ACTIVE);
        } catch (RuntimeException ex) {
            // 启用是事务边界：路由、扩展和菜单必须一起成功，否则恢复 DISABLED。
            routeRegistry.removeByPlugin(code);
            extensionRegistry.unregisterByPlugin(code);
            if (menuPort != null) {
                try {
                    menuPort.disable(code);
                } catch (RuntimeException menuError) {
                    log.warn("插件 {} 启用失败后的菜单回滚报错：{}", code, menuError.getMessage());
                }
            }
            if (started) {
                try {
                    pluginManager.stopPlugin(code);
                } catch (RuntimeException stopError) {
                    log.warn("插件 {} 启用失败后的引擎回滚报错：{}", code, stopError.getMessage());
                }
            }
            pluginRegistry.putState(code, PluginState.DISABLED);
            throw ex;
        }
    }

    /**
     * 停用：停止插件 → 移除路由 → 移除扩展点 → 置为 DISABLED。
     *
     * <p><b>修复（§7.4.2 P1「快照失效时机不完整」）</b>：原实现只清路由、不清扩展点，
     * 导致插件停用后其扩展实现仍留在快照中被 Core 调用——已停用的插件仍在改价/改单，
     * 属于「停用不生效」的严重缺陷。停用后扩展点与路由必须一起失效，
     * 重新 {@link #enable} 时再由扫描重新注册。
     */
    public void disable(String code) {
        RuntimeException lifecycleError = null;
        try {
            pluginManager.stopPlugin(code);
        } catch (RuntimeException ex) {
            lifecycleError = ex;
        } finally {
            routeRegistry.removeByPlugin(code);
            extensionRegistry.unregisterByPlugin(code);
            if (menuPort != null) {
                try {
                    menuPort.disable(code);
                } catch (RuntimeException menuError) {
                    log.warn("插件 {} 停用后的菜单清理失败：{}", code, menuError.getMessage());
                    if (lifecycleError == null) {
                        lifecycleError = menuError;
                    }
                }
            }
            pluginRegistry.putState(code, PluginState.DISABLED);
        }
        log.info("插件已停用：{}", code);
        if (lifecycleError != null) {
            throw lifecycleError;
        }
    }

    /**
     * 扫描插件 JAR 中的 {@link com.houjiang.plugin.sdk.annotation.HouRoute}、
     * {@link com.houjiang.plugin.sdk.annotation.HouRouteDefaults} 与 Spring 映射方法，
     * 交给 {@link PluginRouteScanner} 产出路由条目后逐个注册。
     * 插件类不会被宿主的组件扫描自动纳入，因此按插件 ClassLoader 做一次确定性扫描。
     */
    private void scanAndRegisterRoutes(String pluginCode) {
        PluginWrapper wrapper = pluginManager.getPlugin(pluginCode);
        if (wrapper == null) {
            throw new IllegalStateException("插件未找到: " + pluginCode);
        }
        // 插件 Controller 依赖的是插件自己的 Service/Resolver，这些 Bean 只存在于插件的
        // Spring 子上下文里。用宿主 BeanFactory 实例化会触发 UnsatisfiedDependency，
        // 路由被整条跳过（表现为 /plugin/{code}/** 全部 404），因此必须与扩展点扫描一样
        // 走插件子上下文；无子上下文时回退到宿主工厂，保持纯引擎单测可用。
        PluginRouteScanner.ScanRequest request = new PluginRouteScanner.ScanRequest(
                pluginCode, wrapper.getPluginPath(), wrapper.getPluginClassLoader(),
                beanFactoryFor(pluginCode), routeStripPrefixes);
        for (RouteRegistry.RouteEntry entry : routeScanner.scan(request)) {
            registerRoute(entry);
        }
    }

    /**
     * 扫描插件中的 {@link HouExtension} 实现类，实例化后注册进扩展点注册表。
     * PF4J 仅负责装载 ClassLoader，扩展点的发现与注册由引擎自己完成（不依赖 PF4J 的扩展工厂）。
     */
    private void scanAndRegisterExtensions(String pluginCode) {
        PluginWrapper wrapper = pluginManager.getPlugin(pluginCode);
        if (wrapper == null) {
            return;
        }
        AutowireCapableBeanFactory pluginBeanFactory = beanFactoryFor(pluginCode);
        PluginRouteScanner.ScanRequest request = new PluginRouteScanner.ScanRequest(
                pluginCode, wrapper.getPluginPath(), wrapper.getPluginClassLoader(),
                pluginBeanFactory, routeStripPrefixes);
        for (Class<?> extClass : routeScanner.scanExtensions(request)) {
            HouExtension ann = extClass.getAnnotation(HouExtension.class);
            if (ann == null) {
                continue;
            }
            if (!ExtensionPoint.class.isAssignableFrom(extClass)) {
                log.warn("插件 {} 的扩展点实现 {} 未实现 ExtensionPoint，已跳过", pluginCode, extClass.getName());
                continue;
            }
            Object instance = instantiate(extClass, pluginBeanFactory);
            // 注册表 register 为泛型 <E>，用裸类型转换规避通配符推导冲突（ann.point() 是 Class<? extends ExtensionPoint<?>>）
            @SuppressWarnings({"unchecked", "rawtypes"})
            Class pointType = ann.point();
            String effectiveOwner = resolveRuntimeExtensionOwner(pluginCode, ann);
            extensionRegistry.register((Class) pointType, (ExtensionPoint<?>) instance,
                    pluginCode, ann.priority(), effectiveOwner, ann.mode());
            log.info("插件 {} 注册扩展点 {}（owner={}, mode={}, priority={}）",
                    pluginCode, ann.point().getSimpleName(), effectiveOwner, ann.mode(), ann.priority());
        }
    }

    /**
     * Resolves the runtime plugin owner from the installed manifest.
     *
     * <p>The annotation is an extension declaration, not an authorization
     * boundary. A runtime artifact must not become a build-time customization
     * by declaring {@code owner=custom}; the marketplace-verified manifest is
     * authoritative. Missing manifests are allowed for the low-level PF4J
     * embedding tests, but they still cannot request the custom owner.</p>
     */
    String resolveRuntimeExtensionOwner(String pluginCode, HouExtension annotation) {
        if (annotation == null) {
            throw new IllegalArgumentException("Runtime extension annotation must not be null");
        }
        PluginManifest manifest = pluginRegistry.getManifest(pluginCode);
        if (manifest == null) {
            String declared = normalizeRuntimeOwner(annotation.owner());
            if (ExtensionRegistry.OWNER_CUSTOM.equals(declared)) {
                throw new IllegalStateException("Runtime plugin cannot declare owner=custom without a verified manifest: "
                        + pluginCode);
            }
            return declared;
        }

        String manifestOwner = normalizeRuntimeOwner(manifest.getOwner());
        if (ExtensionRegistry.OWNER_CUSTOM.equals(manifestOwner)) {
            throw new IllegalStateException("Runtime plugin manifest cannot use owner=custom; "
                    + "build-time customizations must be assembled through backend/extensions: " + pluginCode);
        }
        if (!manifestOwner.equalsIgnoreCase(normalizeRuntimeOwner(annotation.owner()))) {
            log.warn("插件 {} 的扩展注解 owner={} 被 plugin.yaml owner={} 覆盖",
                    pluginCode, annotation.owner(), manifestOwner);
        }
        return manifestOwner;
    }

    private static String normalizeRuntimeOwner(String owner) {
        String value = owner == null || owner.isBlank()
                ? ExtensionRegistry.OWNER_OFFICIAL
                : owner.trim().toLowerCase(Locale.ROOT);
        if (!ExtensionRegistry.OWNER_OFFICIAL.equals(value)
                && !ExtensionRegistry.OWNER_THIRDPARTY.equals(value)
                && !ExtensionRegistry.OWNER_CUSTOM.equals(value)) {
            throw new IllegalArgumentException("Unsupported runtime plugin owner: " + owner);
        }
        return value;
    }

    private AutowireCapableBeanFactory beanFactoryFor(String pluginCode) {
        if (springContexts != null && pluginCode != null) {
            try {
                return springContexts.beanFactory(pluginCode);
            } catch (RuntimeException ignored) {
                // Unit-test and non-Spring embedders may not install a child context.
            }
        }
        return beanFactory;
    }

    private Object instantiate(Class<?> extClass, AutowireCapableBeanFactory pluginBeanFactory) {
        try {
            if (pluginBeanFactory != null) {
                return pluginBeanFactory.autowire(extClass, AutowireCapableBeanFactory.AUTOWIRE_CONSTRUCTOR, false);
            }
            return extClass.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("插件扩展点实例化失败: " + extClass, e);
        }
    }
}
