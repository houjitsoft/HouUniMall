package com.houjiang.plugin.sdk.hook;

import com.houjiang.plugin.sdk.annotation.HouHook;
import com.houjiang.plugin.sdk.api.PluginContext;

/**
 * 插件生命周期钩子。
 *
 * <p>配合 {@link HouHook} 使用：宿主在插件安装 / 卸载 / 启用 / 禁用 / 升级 / 启动时回调。
 *
 * <p>示例：
 * <pre>{@code
 * @HouHook(event = HouHook.HookEvent.ON_INSTALL, order = 10)
 * public class CouponInstallHook implements PluginLifecycleHook {
 *     @Override
 *     public void execute(PluginContext context) {
 *         // 初始化插件自有数据
 *     }
 * }
 * }</pre>
 *
 * <p>约定：宿主会捕获钩子的全部异常并记录，单个钩子失败不影响宿主启动；
 * 但 <code>ON_INSTALL</code> 钩子抛异常会导致安装失败并进入回滚流程。
 *
 * @author HouCloud
 */
public interface PluginLifecycleHook {

    /**
     * 执行钩子逻辑。
     *
     * @param context 插件上下文，可获取配置、数据目录与框架服务
     */
    void execute(PluginContext context);
}
