package com.houjiang.plugin.engine;

import com.houjiang.plugin.engine.artifact.PluginArtifactStore;
import com.houjiang.plugin.engine.artifact.PluginChecksumVerifier;
import com.houjiang.plugin.engine.artifact.JarPluginManifestProvider;
import com.houjiang.plugin.engine.config.ExtensionPolicyRegistry;
import com.houjiang.plugin.engine.config.PluginEngineProperties;
import com.houjiang.plugin.engine.core.HouPluginManager;
import com.houjiang.plugin.engine.core.PluginBootGate;
import com.houjiang.plugin.engine.core.PluginDispatcher;
import com.houjiang.plugin.engine.core.PluginDrainGate;
import com.houjiang.plugin.engine.core.PluginExtensionFactory;
import com.houjiang.plugin.engine.core.PluginLifecycleManager;
import com.houjiang.plugin.engine.host.DefaultPluginHostApi;
import com.houjiang.plugin.engine.observability.ExtensionMetrics;
import com.houjiang.plugin.engine.registry.ExtensionDisableSwitch;
import com.houjiang.plugin.engine.registry.ExtensionHealthTracker;
import com.houjiang.plugin.engine.registry.ExtensionInvoker;
import com.houjiang.plugin.engine.registry.ExtensionRegistry;
import com.houjiang.plugin.engine.registry.ClasspathExtensionRegistrar;
import com.houjiang.plugin.engine.registry.PluginRegistry;
import com.houjiang.plugin.engine.registry.RouteRegistry;
import com.houjiang.plugin.engine.security.PluginPermissionEvaluator;
import com.houjiang.plugin.engine.security.PluginResourceLimiter;
import com.houjiang.plugin.engine.security.PluginSignatureVerifier;
import com.houjiang.plugin.engine.security.PluginStartupInspector;
import com.houjiang.plugin.engine.security.SpringSecurityPluginPermissionEvaluator;
import com.houjiang.plugin.engine.spi.PluginMigrationPort;
import com.houjiang.plugin.engine.spi.PluginManifestProvider;
import com.houjiang.plugin.engine.spi.PluginMenuPort;
import io.micrometer.core.instrument.MeterRegistry;
import org.pf4j.DefaultPluginManager;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.ApplicationContext;

import java.nio.file.Path;

/**
 * 插件引擎自动装配（方案 4.x）。
 *
 * <p>宿主（mall 等）引入 {@code plugin-engine} 后即可通过本配置获得引擎全部 Bean。
 * 后续由 {@code spring.factories} / {@code AutoConfiguration.imports} 接入，或显式 {@code @Import}。
 *
 * <p><b>注意</b>：宿主要启用引擎（真实装载插件）才需要 {@code @Import} 本配置；
 * 仅使用 {@link PluginArtifactStore} 落盘安装包的模块不需要引入，避免无谓地启动 PF4J。
 *
 * @author HouCloud
 */
@Configuration
@EnableConfigurationProperties(PluginEngineProperties.class)
public class PluginEngineAutoConfiguration {

    @Bean
    public ExtensionRegistry extensionRegistry() {
        return new ExtensionRegistry();
    }

    /**
     * Build-time custom modules expose annotated implementations as Spring
     * beans. Register them once after singleton creation using the same registry
     * and conflict rules as PF4J plugins.
     */
    @Bean
    public ClasspathExtensionRegistrar classpathExtensionRegistrar(
            ConfigurableListableBeanFactory beanFactory,
            ExtensionRegistry extensionRegistry,
            PluginEngineProperties properties) {
        return new ClasspathExtensionRegistrar(beanFactory, extensionRegistry,
                properties.isClasspathExtensionsEnabled());
    }

    @Bean
    public PluginRegistry pluginRegistry() {
        return new PluginRegistry();
    }

    @Bean
    public RouteRegistry routeRegistry() {
        return new RouteRegistry();
    }

    @Bean
    public DefaultPluginManager houPluginManager(PluginEngineProperties properties,
                                                 PluginExtensionFactory extensionFactory) {
        // 黑名单与守卫开关必须走构造器：PF4J 在 super() 里就调用了 createPluginLoader()，
        // 之后再用 setter 补已经来不及（这点踩过坑，见 HouPluginManager#createPluginLoader 注释）。
        HouPluginManager manager = new HouPluginManager(resolvePluginsRoot(properties), extensionFactory,
                properties.isClassLoaderGuarded(), properties.getBlockedPackages());
        return manager;
    }

    /**
     * 签名验签器：公钥来自 {@code houcloud.plugin.signature.public-key}。
     *
     * <p>未配置公钥时 Bean 照样创建，但任何验签调用都会返回 {@code false}（fail-closed），
     * 由上游的「是否强制验签」开关决定是否阻断安装——绝不允许「没配公钥就当验过了」。
     */
    @Bean
    public PluginSignatureVerifier pluginSignatureVerifier(PluginEngineProperties properties) {
        return new PluginSignatureVerifier(properties.getSignatureAlgorithm());
    }

    /**
     * 资源配额器：默认不限制，由宿主通过 {@code configure} 按插件下发配额。
     */
    @Bean
    public PluginResourceLimiter pluginResourceLimiter() {
        return new PluginResourceLimiter();
    }

    /**
     * 启动期字节码合规扫描器：在装载前发现 {@code System.exit} / {@code Runtime.exec} 等危险引用。
     */
    @Bean
    public PluginStartupInspector pluginStartupInspector() {
        return new PluginStartupInspector();
    }

    /**
     * 安装产物仓库：与 {@code HouPluginManager} 使用同一 pluginsRoot，形成落盘/装载契约。
     */
    @Bean
    public PluginArtifactStore pluginArtifactStore(PluginEngineProperties properties) {
        return new PluginArtifactStore(resolvePluginsRoot(properties));
    }

    @Bean
    public PluginChecksumVerifier pluginChecksumVerifier() {
        return new PluginChecksumVerifier();
    }

    /**
     * Reads the authoritative plugin.yaml from every installed artifact.
     * Keeping this provider in the engine makes fresh installs and restart
     * recovery use exactly the same manifest validation rules.
     */
    @Bean
    public PluginManifestProvider pluginManifestProvider(PluginEngineProperties properties) {
        return new JarPluginManifestProvider(properties.isAllowLegacyHost());
    }

    @Bean
    public PluginExtensionFactory pluginExtensionFactory(AutowireCapableBeanFactory beanFactory) {
        return new PluginExtensionFactory(beanFactory);
    }

    /**
     * 扩展点降级策略表（R3.2）：内置 17 点默认表，宿主可用
     * {@code houcloud.plugin.degrade.{point}} 覆盖。
     *
     * <p><b>fail-fast</b>：若把锁定集（金额 / 库存 / 支付）配置成 {@code IGNORE}，
     * 本 Bean 创建即抛异常，宿主启动失败。
     */
    @Bean
    public ExtensionPolicyRegistry extensionPolicyRegistry(PluginEngineProperties properties) {
        ExtensionPolicyRegistry policyRegistry = new ExtensionPolicyRegistry();
        policyRegistry.applyOverrides(properties.getDegrade());
        return policyRegistry;
    }

    /**
     * 运行期禁用开关（R3.3）：内存态 + 可插拔装载源，默认装载
     * {@code houcloud.plugin.disabled-extensions}；变更即刻重建快照，热路径零查库。
     */
    @Bean
    public ExtensionDisableSwitch extensionDisableSwitch(ExtensionRegistry registry,
                                                        PluginEngineProperties properties) {
        ExtensionDisableSwitch disableSwitch = new ExtensionDisableSwitch(registry);
        disableSwitch.addSource(ExtensionDisableSwitch.ofList(properties.getDisabledExtensions()));
        disableSwitch.refreshFromSources();
        return disableSwitch;
    }

    /**
     * 健康跟踪 / 熔断（R3.1）：连续失败达 {@code houcloud.plugin.health.failure-threshold}
     * 经 {@link ExtensionDisableSwitch} 摘除实现；超过
     * {@code houcloud.plugin.health.recovery-window-ms} 后进入半开试探，成功即恢复，
     * 失败则重新计时。运维仍可用 {@code reset} 即时恢复；把恢复窗口配成 0 即退回纯手动模式。
     */
    @Bean
    public ExtensionHealthTracker extensionHealthTracker(ExtensionDisableSwitch disableSwitch,
                                                        PluginEngineProperties properties) {
        return new ExtensionHealthTracker(disableSwitch,
                properties.getHealth().getFailureThreshold(),
                properties.getHealth().getRecoveryWindowMs());
    }

    @Bean(destroyMethod = "shutdown")
    public ExtensionInvoker extensionInvoker(ExtensionRegistry registry,
                                             PluginEngineProperties properties,
                                             ExtensionPolicyRegistry policyRegistry,
                                             ExtensionHealthTracker healthTracker,
                                             ObjectProvider<ExtensionMetrics> metricsProvider) {
        // 指标为可选软依赖：Micrometer 存在时为结构化日志 + 指标，缺失时退化为纯日志
        return new ExtensionInvoker(registry, properties, policyRegistry, healthTracker,
                metricsProvider.getIfAvailable());
    }

    /**
     * Micrometer 软依赖装配（R3.4）：仅当宿主存在 {@link MeterRegistry} 时装配指标，
     * 否则本配置整体跳过，扩展调用退化为纯结构化日志——保证「零新增运行时依赖」。
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(MeterRegistry.class)
    static class MicrometerMetricsConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public ExtensionMetrics extensionMetrics(ObjectProvider<MeterRegistry> meterRegistryProvider) {
            return new ExtensionMetrics(meterRegistryProvider);
        }
    }

    @Bean
    @ConditionalOnMissingBean
    public PluginPermissionEvaluator pluginPermissionEvaluator() {
        return new SpringSecurityPluginPermissionEvaluator();
    }

    /**
     * 升级排空窗口登记表（M4）。
     *
     * <p>注册成 Bean 是为了让「升级编排（宿主）」与「路由分发（引擎）」拿到同一个实例：
     * 编排侧调用 {@code begin/end}，分发侧查询 {@code isDraining}。
     * 两边各建一个实例等于排空期根本拦不住请求。
     */
    @Bean
    public PluginDrainGate pluginDrainGate() {
        return new PluginDrainGate();
    }

    /**
     * 插件路由分发端点。{@code require-declared-route-permission}（默认 {@code false}）
     * 由 {@link PluginEngineProperties} 喂入，使该开关从配置即可达；默认保持「仅校验登录态」
     * 的历史兼容行为，置 {@code true} 才对未声明权限的路由 fail-closed。
     *
     * <p>排空窗口（{@link PluginDrainGate}）以 {@link ObjectProvider} 注入：宿主未接入升级编排时
     * 拿不到实例，此时保持 M4 之前的行为（不做 503 拦截），不改变既有语义。
     */
    @Bean
    public PluginDispatcher pluginDispatcher(RouteRegistry routeRegistry,
                                             PluginRegistry pluginRegistry,
                                             PluginPermissionEvaluator permissionEvaluator,
                                             PluginEngineProperties properties,
                                             ObjectProvider<PluginDrainGate> drainGate,
                                             ObjectProvider<PluginBootGate> bootGate) {
        return new PluginDispatcher(routeRegistry, pluginRegistry, permissionEvaluator,
                properties.isRequireDeclaredRoutePermission(), drainGate.getIfAvailable(),
                properties.isEnforceCallerWhitelist(), bootGate.getIfAvailable());
    }

    /**
     * 启动装载窗口闸门：与 {@link PluginEngineBootstrap} 共享同一实例，
     * 装载期间让 {@link PluginDispatcher} 把「路由还没建起来」回成 503 而不是 404。
     *
     * <p>默认状态是「已就绪」，因此未装载插件的宿主（boot-enabled=false）行为完全不变。
     */
    @Bean
    public PluginBootGate pluginBootGate() {
        return new PluginBootGate();
    }

    /**
     * 可选能力发现：Core 调用插件能力的唯一入口（总判断第 7 条、§1.4.4）。
     *
     * <p>插件未安装/未启用/未暴露能力时返回空，由调用方降级；不得再新增 {@code @FeignClient} 硬依赖插件。
     */
    @Bean
    public com.houjiang.plugin.api.host.PluginHostApi pluginHostApi(PluginRegistry pluginRegistry,
                                                             DefaultPluginManager pluginManager,
                                                             ObjectProvider<com.houjiang.plugin.engine.spi.PluginServiceEndpointResolver> endpoints) {
        return new DefaultPluginHostApi(pluginRegistry, pluginManager,
                new com.houjiang.plugin.engine.host.StandaloneCapabilityClient(),
                endpoints.getIfAvailable(() -> service -> null));
    }

    @Bean
    public PluginLifecycleManager pluginLifecycleManager(DefaultPluginManager pluginManager,
                                                         ExtensionRegistry extensionRegistry,
                                                         RouteRegistry routeRegistry,
                                                         PluginRegistry pluginRegistry,
                                                         AutowireCapableBeanFactory beanFactory,
                                                         ApplicationContext applicationContext,
                                                         PluginEngineProperties properties,
                                                         ObjectProvider<PluginMigrationPort> migrationPortProvider,
                                                         ObjectProvider<PluginMenuPort> menuPortProvider,
                                                         PluginManifestProvider manifestProvider) {
        // 迁移端口可选：宿主（mall）声明 SchemaMigrationRunner 时注入，MIGRATED 阶段生效；
        // 未提供则 getIfAvailable() 返回 null，install() 跳过迁移，与旧版行为一致（向后兼容）。
        return new PluginLifecycleManager(pluginManager, extensionRegistry, routeRegistry, pluginRegistry,
                beanFactory, properties.getRouteStripPrefixes(), migrationPortProvider.getIfAvailable(),
                menuPortProvider.getIfAvailable(),
                new com.houjiang.plugin.engine.core.PluginSpringContextManager(applicationContext), manifestProvider,
                properties.resolveHostService(applicationContext.getEnvironment().getProperty("spring.application.name")), properties.isAllowLegacyHost());
    }

    /**
     * 宿主启动装载器：受 {@code houcloud.plugin.boot-enabled} 控制（运行时开关，见
     * {@link PluginEngineBootstrap#run}）。仅「插件宿主」设为 true 时才会扫描并装载插件，
     * 使 PF4J 真正运行、扩展点注册表生效；仅使用 PluginArtifactStore 落盘安装包的模块该开关保持
     * 默认 false，不会触发 PF4J 启动。
     */
    @Bean
    public PluginEngineBootstrap pluginEngineBootstrap(PluginLifecycleManager lifecycleManager,
                                                      PluginEngineProperties properties,
                                                      PluginBootGate bootGate) {
        return new PluginEngineBootstrap(lifecycleManager, properties, bootGate);
    }

    /**
     * 解析插件根目录（相对工作目录时按进程工作目录归一为绝对路径）。
     */
    private static Path resolvePluginsRoot(PluginEngineProperties properties) {
        String dir = properties.getPluginsDir();
        return Path.of(dir == null || dir.isBlank() ? "plugins" : dir)
                .toAbsolutePath()
                .normalize();
    }
}
