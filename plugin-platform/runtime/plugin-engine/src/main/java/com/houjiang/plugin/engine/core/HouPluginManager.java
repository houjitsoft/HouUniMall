package com.houjiang.plugin.engine.core;

import org.pf4j.CompoundPluginLoader;
import org.pf4j.DefaultPluginManager;
import org.pf4j.DevelopmentPluginLoader;
import org.pf4j.PluginLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * 宿主内嵌的插件管理器。
 *
 * <p>基于 PF4J {@link DefaultPluginManager} 复用其插件 ClassLoader 与生命周期能力；
 * 由于运行环境无 <code>pf4j-spring</code>，Spring 集成由 {@link PluginExtensionFactory} 自实现
 * （效果等价于 pf4j-spring 的 <code>SpringPluginManager</code>：扩展点实例由宿主
 * <code>AutowireCapableBeanFactory</code> 创建并注入依赖，插件与宿主共享同一 ApplicationContext）。
 *
 * <p>本类额外挂接了 {@link GuardedPluginLoader}：把插件 ClassLoader 换成
 * {@link GuardedPluginClassLoader}，对 JDK 内部包与引擎内部实现包做可见性约束。
 * 默认黑名单极小（见 {@link GuardedPluginClassLoader#DEFAULT_BLOCKED_PACKAGES}），
 * 不影响既有插件；可通过 {@link #setBlockedPackages} 收紧。
 *
 * <p>TODO：后续 override <code>loadPlugin</code> / <code>startPlugin</code>，在插件 class 解析后
 * 扫描 {@code @HouExtension} / {@code @HouRoute} 并通过 {@link PluginLifecycleManager} 注册。
 *
 * @author HouCloud
 */
public class HouPluginManager extends DefaultPluginManager {

    private final PluginExtensionFactory extensionFactory;

    private static final Logger log = LoggerFactory.getLogger(HouPluginManager.class);

    /** 追加的阻断包名前缀；在默认黑名单之上叠加，由 {@code houcloud.plugin.class-loader.blocked-packages} 注入。 */
    private List<String> blockedPackages = List.of();

    /**
     * 是否启用带可见性约束的插件 ClassLoader（{@code houcloud.plugin.class-loader.guarded}，默认 false）。
     *
     * <p>必须在<b>构造期</b>传入：PF4J 在 {@code super()} 里就调用 {@code createPluginLoader()}，
     * 子类字段的初始化与 setter 都发生在这之后，补不进去。
     */
    private final boolean guardedClassLoader;

    /**
     * @param pluginsRoot 插件安装包根目录；由宿主配置 {@code houcloud.plugin.plugins-dir} 解析而来。
     *                    该目录与 {@code PluginArtifactStore} 使用同一路径，形成「市场落盘 → 引擎装载」契约。
     */
    public HouPluginManager(Path pluginsRoot) {
        this(pluginsRoot, null);
    }

    public HouPluginManager(Path pluginsRoot, PluginExtensionFactory extensionFactory) {
        this(pluginsRoot, extensionFactory, false, List.of());
    }

    /**
     * @param pluginsRoot        插件安装包根目录
     * @param extensionFactory   扩展点工厂，可为 null
     * @param guardedClassLoader 是否启用带可见性约束的插件 ClassLoader
     * @param blockedPackages    追加的阻断包名前缀
     */
    public HouPluginManager(Path pluginsRoot, PluginExtensionFactory extensionFactory,
                            boolean guardedClassLoader, List<String> blockedPackages) {
        super(pluginsRoot);
        this.extensionFactory = extensionFactory;
        this.guardedClassLoader = guardedClassLoader;
        if (blockedPackages != null) {
            this.blockedPackages = List.copyOf(blockedPackages);
        }
        log.info("插件管理器已就绪，pluginsRoot={} guardedClassLoader={}", getPluginsRoot(), guardedClassLoader);
    }

    @Override
    protected org.pf4j.ExtensionFactory createExtensionFactory() {
        return extensionFactory == null ? super.createExtensionFactory() : extensionFactory;
    }

    /**
     * 装载器：默认完全沿用 PF4J 的 {@code CompoundPluginLoader}（开发态 + JAR 形态）。
     *
     * <p><b>为什么默认不启用守卫 loader</b>：PF4J 默认返回的是复合装载器，
     * 其中 {@code JarPluginLoader} 负责把插件 jar 加进类路径。替换装载器时只要
     * 少复刻这一步，表现就是「所有插件 ClassNotFoundException、一个都起不来」——
     * 代价远大于可见性约束的收益。因此 {@code guardedClassLoader} 默认 false；
     * 置 true 时走 {@link GuardedPluginLoader}（已复刻 {@code addFile}），
     * 并保留开发态装载器，行为与默认装载器一致。
     */
    @Override
    protected PluginLoader createPluginLoader() {
        if (!guardedClassLoader) {
            return super.createPluginLoader();
        }
        return new CompoundPluginLoader()
                .add(new DevelopmentPluginLoader(this))
                .add(new GuardedPluginLoader(this, blockedPackages));
    }

    /**
     * 设置追加的阻断包名前缀（在默认黑名单之上叠加）。
     *
     * @param blockedPackages 为空或 {@code null} 表示仅使用默认黑名单
     */
    public void setBlockedPackages(List<String> blockedPackages) {
        List<String> copy = new ArrayList<>();
        if (blockedPackages != null) {
            for (String pkg : blockedPackages) {
                if (pkg != null && !pkg.isBlank()) {
                    copy.add(pkg.trim());
                }
            }
        }
        this.blockedPackages = List.copyOf(copy);
        if (!copy.isEmpty()) {
            log.info("插件类加载黑名单已追加：{}", copy);
        }
    }

    /** @return 当前追加的阻断包名前缀（不含默认黑名单） */
    public List<String> getBlockedPackages() {
        return blockedPackages;
    }
}
