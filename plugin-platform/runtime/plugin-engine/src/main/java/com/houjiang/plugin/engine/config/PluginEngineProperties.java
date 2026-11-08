package com.houjiang.plugin.engine.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 插件引擎配置。
 *
 * <p>前缀 <code>houcloud.plugin</code>，可在所属服务的 application.yml 中覆盖。
 *
 * @author HouCloud
 */
@ConfigurationProperties(prefix = "houcloud.plugin")
public class PluginEngineProperties {

    /** Explicit identity; otherwise use spring.application.name. No mall fallback. */
    private String hostService;

    /** Accept legacy host only when it names one of the 24 known services. */
    private boolean allowLegacyHost = false;

    public String getHostService() { return hostService; }
    public void setHostService(String hostService) { this.hostService = hostService; }
    public boolean isAllowLegacyHost() { return allowLegacyHost; }
    public void setAllowLegacyHost(boolean allowLegacyHost) { this.allowLegacyHost = allowLegacyHost; }

    public String resolveHostService(String applicationName) {
        // Identity is checked at installation, so metadata-only consumers need no host identity.
        return hostService == null || hostService.isBlank() ? applicationName : hostService;
    }

    /** 插件 JAR 落盘根目录（相对宿主工作目录；jar 启动目录下为 ./plugins） */
    private String pluginsDir = "plugins";

    /** 插件私有数据目录 */
    private String dataDir = "plugin-data";

    /** 单个扩展点执行超时（毫秒），超时按失败处理但不向主流程传播；&le;0 表示同线程直调 */
    private long extensionTimeoutMs = 200;

    /**
     * 扩展执行工作线程上限（bulkhead 隔离，R3.1）。
     * <p>大于 0 时作为线程池固定大小；否则回落到 {@link Executor#getMaxSize()}。
     * 同一宿主可同时被多个请求调用，插件阻塞不得让宿主线程随并发量线性增长。
     */
    private int extensionMaxConcurrent = 0;

    /** 引擎总开关 */
    private boolean enabled = true;

    /**
     * 宿主启动装载开关。仅需要装载本服务扩展的业务服务设为 true：
     * 启动时扫描 pluginsDir 下所有 JAR 并装载，PF4J 真正启动、扩展点注册表生效。
     * 仅使用 PluginArtifactStore 落盘安装包的模块保持默认 false，避免无谓启动 PF4J。
     */
    private boolean bootEnabled = false;

    /**
     * Whether {@code @HouExtension} beans supplied by build-time custom modules
     * are registered during host startup. Runtime PF4J loading is independent.
     */
    private boolean classpathExtensionsEnabled = true;

    /**
     * 插件类加载器追加的阻断包名前缀（在引擎默认黑名单之上叠加）。
     *
     * <p>默认黑名单只含 {@code sun.misc.} / {@code jdk.internal.} / 引擎内部包——
     * 刻意做到最小，避免误伤既有插件。宿主若要进一步收紧（例如禁止插件直接碰
     * 宿主内部实现包），在此追加即可。
     */
    private List<String> blockedPackages = new ArrayList<>();

    /**
     * 是否启用带可见性约束的插件 ClassLoader（{@code houcloud.plugin.class-loader.guarded}，默认 false）。
     *
     * <p>默认关闭的原因写在 {@code HouPluginManager#createPluginLoader}：
     * 替换装载器一旦漏掉「把插件 jar 加进类路径」这一步，所有插件都会 ClassNotFoundException，
     * 代价远大于收益。确需收紧时显式置 true，并先在预发环境逐个插件验证能起来。
     */
    private boolean classLoaderGuarded = false;

    /**
     * 引擎侧可信公钥（PEM 或 Base64）。为空时验签一律失败（fail-closed）。
     *
     * <p>与业务侧 {@code houcloud.update.require-signature} 是两回事：
     * 那个开关决定「要不要验」，这个配置决定「拿什么验」。
     */
    private String signaturePublicKey = "";

    /**
     * 签名算法（JDK 标准名）。为空时回落 {@code SHA256withRSA}。
     */
    private String signatureAlgorithm = "";

    /** 扩展执行线程池（R3.1）。 */
    private Executor executor = new Executor();

    /** 健康/熔断（R3.1）。 */
    private Health health = new Health();

    /**
     * 降级策略覆盖（R3.2）：键为扩展点短名，值为 {@code IGNORE}/{@code ABORT}/{@code FALLBACK}。
     * 例：{@code houcloud.plugin.degrade.PromotionCalc=ABORT}。
     */
    private Map<String, String> degrade = new LinkedHashMap<>();

    /**
     * 运行期禁用项默认装载源（R3.3）：元素格式 {@code <pointShortName>#<source>}
     * 或 {@code <pointShortName>#<source>#<implClass>}。
     * 例：{@code PriceCalc#custom-goods} 表示禁用 custom-goods 在 PriceCalc 下的实现。
     */
    private List<String> disabledExtensions = new ArrayList<>();

    /**
     * 未声明权限的插件路由策略：{@code false}（默认）=仅校验登录态（历史兼容），
     * {@code true}=fail-closed 直接拒绝（403）。对应 {@code PluginDispatcher} 的
     * {@code requireDeclaredRoutePermission} 开关，配置键
     * {@code houcloud.plugin.require-declared-route-permission}。
     *
     * <p><b>默认必须为 {@code false}</b>：pm / activity / crm 等插件尚未补齐路由权限声明，
     * 默认开启会让这些未声明权限的路由大面积 403（见 {@code PluginRouteScanner}
     * 装载期告警「开启 require-declared-route-permission 前需先补齐权限声明」）。
     * 本字段只让开关「够得着」，不改变既有默认行为。
     */
    private boolean requireDeclaredRoutePermission = false;

    /**
     * 未在 {@code @HouRoute} 声明 {@code allowCallers} 时的兜底策略：
     * {@code false}（默认）= 不限制调用方（历史兼容，任何登录用户或插件都能调）；
     * {@code true}= fail-closed，未声明即只允许宿主，插件间调用一律拒绝（403）。
     * 配置键 {@code houcloud.plugin.enforce-caller-whitelist}。
     *
     * <p><b>默认必须为 {@code false}</b>：存量插件尚未声明 {@code allowCallers}，
     * 且插件间存在既有的 Feign 内部调用（如 geo/oa 的 job 链路），默认开启会直接打断它们。
     * 与 {@link #requireDeclaredRoutePermission} 一样，本字段只让开关「够得着」，
     * 收紧时机交给宿主按迁移进度决定。
     */
    private boolean enforceCallerWhitelist = false;

    /**
     * 路由扫描时从插件 Controller 类级 {@code @RequestMapping} 剥离的路径前缀（如 {@code /api/erp}）。
     * 通用核心框架不再硬编码任何业务前缀，由宿主按插件实际约定配置；为空则不剥离。
     */
    private List<String> routeStripPrefixes = new ArrayList<>(List.of(
            "/api/erp",
            "/api/webpos",
            "/api/oa",
            "/api/mes",
            "/api/finance",
            "/api/cms",
            "/api/activity",
            "/api/geo",
            "/api/orderfood",
            "/api/pm",
            "/api/crm",
            "/api/im",
            "/api/logging",
            "/activity",
            "/cms",
            "/finance",
            "/mes",
            "/oa",
            "/webpos",
            "/geo",
            "/orderfood",
            "/pm",
            "/crm",
            "/im",
            "/logging"
    ));

    /** 扩展执行线程池配置。 */
    public static class Executor {

        /** 核心线程数 */
        private int coreSize = 4;

        /** 最大线程数（无 {@code extensionMaxConcurrent} 覆盖时即池大小） */
        private int maxSize = 8;

        /** 任务队列容量 */
        private int queueCapacity = 256;

        /** 是否启用异步执行；false 时回落同步直调 */
        private boolean enabled = true;

        public int getCoreSize() {
            return coreSize;
        }

        public void setCoreSize(int coreSize) {
            this.coreSize = coreSize;
        }

        public int getMaxSize() {
            return maxSize;
        }

        public void setMaxSize(int maxSize) {
            this.maxSize = maxSize;
        }

        public int getQueueCapacity() {
            return queueCapacity;
        }

        public void setQueueCapacity(int queueCapacity) {
            this.queueCapacity = queueCapacity;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    /** 健康/熔断配置。 */
    public static class Health {

        /** 连续失败摘除阈值；&le;0 表示关闭熔断 */
        private int failureThreshold = 5;

        /** 恢复窗口（毫秒，预留；本轮为手动重置） */
        private long recoveryWindowMs = 60_000L;

        public int getFailureThreshold() {
            return failureThreshold;
        }

        public void setFailureThreshold(int failureThreshold) {
            this.failureThreshold = failureThreshold;
        }

        public long getRecoveryWindowMs() {
            return recoveryWindowMs;
        }

        public void setRecoveryWindowMs(long recoveryWindowMs) {
            this.recoveryWindowMs = recoveryWindowMs;
        }
    }

    public List<String> getBlockedPackages() {
        return blockedPackages;
    }

    public void setBlockedPackages(List<String> blockedPackages) {
        this.blockedPackages = blockedPackages == null ? new ArrayList<>() : blockedPackages;
    }

    public boolean isClassLoaderGuarded() {
        return classLoaderGuarded;
    }

    public void setClassLoaderGuarded(boolean classLoaderGuarded) {
        this.classLoaderGuarded = classLoaderGuarded;
    }

    public String getSignaturePublicKey() {
        return signaturePublicKey;
    }

    public void setSignaturePublicKey(String signaturePublicKey) {
        this.signaturePublicKey = signaturePublicKey == null ? "" : signaturePublicKey;
    }

    public String getSignatureAlgorithm() {
        return signatureAlgorithm;
    }

    public void setSignatureAlgorithm(String signatureAlgorithm) {
        this.signatureAlgorithm = signatureAlgorithm == null ? "" : signatureAlgorithm;
    }

    /** 生效的扩展执行线程数：优先 {@code extensionMaxConcurrent}，否则 {@code executor.maxSize}。 */
    public int effectiveMaxConcurrent() {
        if (extensionMaxConcurrent > 0) {
            return extensionMaxConcurrent;
        }
        return executor.getMaxSize();
    }

    public String getPluginsDir() {
        return pluginsDir;
    }

    public void setPluginsDir(String pluginsDir) {
        this.pluginsDir = pluginsDir;
    }

    public String getDataDir() {
        return dataDir;
    }

    public void setDataDir(String dataDir) {
        this.dataDir = dataDir;
    }

    public long getExtensionTimeoutMs() {
        return extensionTimeoutMs;
    }

    public void setExtensionTimeoutMs(long extensionTimeoutMs) {
        this.extensionTimeoutMs = extensionTimeoutMs;
    }

    public int getExtensionMaxConcurrent() {
        return extensionMaxConcurrent;
    }

    public void setExtensionMaxConcurrent(int extensionMaxConcurrent) {
        this.extensionMaxConcurrent = extensionMaxConcurrent;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isBootEnabled() {
        return bootEnabled;
    }

    public void setBootEnabled(boolean bootEnabled) {
        this.bootEnabled = bootEnabled;
    }

    public boolean isClasspathExtensionsEnabled() {
        return classpathExtensionsEnabled;
    }

    public void setClasspathExtensionsEnabled(boolean classpathExtensionsEnabled) {
        this.classpathExtensionsEnabled = classpathExtensionsEnabled;
    }

    public Executor getExecutor() {
        return executor;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    public Health getHealth() {
        return health;
    }

    public void setHealth(Health health) {
        this.health = health;
    }

    public Map<String, String> getDegrade() {
        return degrade;
    }

    public void setDegrade(Map<String, String> degrade) {
        this.degrade = degrade;
    }

    public List<String> getDisabledExtensions() {
        return disabledExtensions;
    }

    public void setDisabledExtensions(List<String> disabledExtensions) {
        this.disabledExtensions = disabledExtensions;
    }

    public List<String> getRouteStripPrefixes() {
        return routeStripPrefixes;
    }

    public void setRouteStripPrefixes(List<String> routeStripPrefixes) {
        this.routeStripPrefixes = routeStripPrefixes;
    }

    public boolean isRequireDeclaredRoutePermission() {
        return requireDeclaredRoutePermission;
    }

    public void setRequireDeclaredRoutePermission(boolean requireDeclaredRoutePermission) {
        this.requireDeclaredRoutePermission = requireDeclaredRoutePermission;
    }

    public boolean isEnforceCallerWhitelist() {
        return enforceCallerWhitelist;
    }

    public void setEnforceCallerWhitelist(boolean enforceCallerWhitelist) {
        this.enforceCallerWhitelist = enforceCallerWhitelist;
    }
}
