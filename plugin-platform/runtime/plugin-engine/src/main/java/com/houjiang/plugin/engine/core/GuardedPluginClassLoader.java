package com.houjiang.plugin.engine.core;

import org.pf4j.PluginClassLoader;
import org.pf4j.PluginDescriptor;
import org.pf4j.PluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 带包可见性约束的插件类加载器。
 *
 * <p>在 PF4J {@link PluginClassLoader}（parent-last）基础上加一层<b>包名黑名单</b>：
 * 命中前缀的类一律抛 {@link ClassNotFoundException}，插件拿不到 {@code Class} 对象，
 * 自然也就无法调用。
 *
 * <p><b>为什么只做黑名单而不做白名单</b>：插件需要用到宿主 SDK、Spring、MyBatis 等大量宿主类，
 * 白名单枚举不完且极易误伤；黑名单只针对「正常插件绝不会碰」的 JDK 内部 API 与宿主内部实现包，
 * 误伤面小、收益明确。真正的强隔离要等 standalone 进程形态。
 *
 * <p><b>默认黑名单刻意极小</b>：{@code sun.misc.}、{@code jdk.internal.}、引擎内部包。
 * 现有插件不会引用这些，因此引入本类不会造成既有插件加载失败。
 * 需要收紧时通过 {@code houcloud.plugin.class-loader.blocked-packages} 追加。
 *
 * @author HouCloud
 */
public class GuardedPluginClassLoader extends PluginClassLoader {

    /** 默认阻断的 JDK 内部包：不属于任何公开 API，正常插件无正当理由使用。 */
    public static final List<String> DEFAULT_BLOCKED_PACKAGES = List.of(
            "sun.misc.",
            "jdk.internal.",
            "com.houjiang.plugin.engine.internal."
    );

    private static final Logger log = LoggerFactory.getLogger(GuardedPluginClassLoader.class);

    private final List<String> blockedPackages;
    private final String pluginId;

    /**
     * @param pluginManager      插件管理器
     * @param pluginDescriptor   插件描述
     * @param parent             父类加载器（宿主）
     * @param extraBlockedPkgs   追加的阻断包名前缀，可为空
     */
    public GuardedPluginClassLoader(PluginManager pluginManager,
                                    PluginDescriptor pluginDescriptor,
                                    ClassLoader parent,
                                    List<String> extraBlockedPkgs) {
        super(pluginManager, pluginDescriptor, parent);
        this.pluginId = pluginDescriptor == null ? "unknown" : pluginDescriptor.getPluginId();
        List<String> merged = new ArrayList<>(DEFAULT_BLOCKED_PACKAGES);
        if (extraBlockedPkgs != null) {
            for (String pkg : extraBlockedPkgs) {
                if (pkg != null && !pkg.isBlank()) {
                    merged.add(pkg.trim());
                }
            }
        }
        this.blockedPackages = List.copyOf(merged);
    }

    /** @return 生效的阻断包名前缀（不可变） */
    public List<String> blockedPackages() {
        return blockedPackages;
    }

    @Override
    public Class<?> loadClass(String className) throws ClassNotFoundException {
        if (isBlocked(className)) {
            // 抛 CNFE 而不是返回 null：让上层拿到确定的「类不存在」语义，
            // 由插件自己的异常处理决定是降级还是失败，而不是在这里静默吞掉。
            log.warn("[plugin-class-guard] 插件 {} 尝试加载受限类：{}", pluginId, className);
            throw new ClassNotFoundException("插件不允许加载受限类：" + className);
        }
        return super.loadClass(className);
    }

    /**
     * 判定类名是否命中黑名单。
     *
     * <p>按前缀匹配，且要求命中的位置必须是包边界（{@code sun.misc.} 之后任意），
     * 避免 {@code com.houjiang.plugin.engine.internalx.Foo} 被 {@code ...internal.} 误判。
     */
    public boolean isBlocked(String className) {
        if (className == null || className.isEmpty()) {
            return false;
        }
        for (String pkg : blockedPackages) {
            if (className.startsWith(pkg)) {
                return true;
            }
        }
        return false;
    }
}
