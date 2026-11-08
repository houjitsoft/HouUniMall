package com.houjiang.plugin.engine.core;

import org.pf4j.JarPluginLoader;
import org.pf4j.PluginDescriptor;
import org.pf4j.PluginManager;

import java.nio.file.Path;
import java.util.List;

/**
 * 带包可见性约束的插件装载器（JAR 形态）。
 *
 * <p><b>🔴 这里必须继承 {@link JarPluginLoader} 而不是 {@code DefaultPluginLoader}</b>：
 * PF4J 3.12 的 {@code DefaultPluginManager.createPluginLoader()} 默认返回的是
 * {@code CompoundPluginLoader}，内含 {@code DevelopmentPluginLoader} 与
 * {@code JarPluginLoader}；<b>把插件 jar 加进类路径的正是 {@code JarPluginLoader#loadPlugin}</b>
 * （{@code new PluginClassLoader(...)} 之后紧接 {@code addFile(pluginPath.toFile())}）。
 * 曾经这里错误地继承了 {@code DefaultPluginLoader}（它只是 {@code BasePluginLoader} 的
 * 直系子类，只加载 {@code classes/} 与 {@code lib/*.jar}，<b>不会加载主 jar</b>），
 * 结果线上 14 个插件全部 {@code ClassNotFoundException}、PF4J 拿不到插件实例而无法启动。
 * 教训：自定义 ClassLoader 时，必须连「jar 是怎么进 classpath 的」一起复刻。
 *
 * <p>启用方式：{@code houcloud.plugin.class-loader.guarded=true}（构造期注入，
 * 因为 {@code createPluginLoader()} 在 {@code super()} 里就被调用，setter 补不进去）。
 * 默认关闭——当前各插件的可见性约束收益有限，而失败代价是整个插件体系起不来。
 *
 * @author HouCloud
 */
public class GuardedPluginLoader extends JarPluginLoader {

    private final List<String> extraBlockedPackages;

    /**
     * @param pluginManager        插件管理器
     * @param extraBlockedPackages 追加的阻断包名前缀，可为空
     */
    public GuardedPluginLoader(PluginManager pluginManager, List<String> extraBlockedPackages) {
        super(pluginManager);
        this.extraBlockedPackages = extraBlockedPackages == null ? List.of() : extraBlockedPackages;
    }

    /**
     * 复刻 {@link JarPluginLoader#loadPlugin}，只把 ClassLoader 换成带守卫的实现。
     *
     * @param pluginPath        插件 jar 路径
     * @param pluginDescriptor  插件描述
     * @return 已把插件 jar 加入类路径的类加载器
     */
    @Override
    public ClassLoader loadPlugin(Path pluginPath, PluginDescriptor pluginDescriptor) {
        GuardedPluginClassLoader classLoader = new GuardedPluginClassLoader(pluginManager, pluginDescriptor,
                getClass().getClassLoader(), extraBlockedPackages);
        // 这一行是主 jar 唯一的入口，漏掉就表现为「插件类找不到」
        classLoader.addFile(pluginPath.toFile());
        return classLoader;
    }
}
