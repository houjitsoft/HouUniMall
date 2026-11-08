package com.houjiang.plugin.sdk.annotation;

import java.lang.annotation.*;

/**
 * 标注插件生命周期钩子。
 * 当插件被安装、卸载、启用、禁用时，框架自动调用对应钩子。
 *
 * <p>示例：</p>
 * <pre>
 * {@code @HouHook(event = HookEvent.ON_INSTALL)}
 * public class MyInstallHook implements PluginLifecycleHook {
 *     ...
 * }
 * </pre>
 *
 * @author HouCloud
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface HouHook {

    /**
     * 钩子绑定的生命周期事件
     *
     * @return 事件类型
     */
    HookEvent event();

    /**
     * 执行顺序，数值越小越先执行
     *
     * @return 排序值
     */
    int order() default 100;

    /**
     * 生命周期事件枚举
     */
    enum HookEvent {
        /** 插件安装完成后触发 */
        ON_INSTALL,
        /** 插件卸载前触发 */
        ON_UNINSTALL,
        /** 插件启用后触发 */
        ON_ENABLE,
        /** 插件禁用前触发 */
        ON_DISABLE,
        /** 插件升级前触发 */
        ON_UPGRADE,
        /** 框架启动时，已安装插件自动触发 */
        ON_STARTUP
    }
}
