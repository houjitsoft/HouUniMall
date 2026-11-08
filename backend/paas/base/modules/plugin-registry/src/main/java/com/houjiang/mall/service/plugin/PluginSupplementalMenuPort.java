package com.houjiang.mall.service.plugin;

/**
 * Adds terminal-specific navigation to the shared plugin lifecycle.
 *
 * <p>The primary {@link PluginMenuSyncPort} owns admin navigation. Implementations
 * use service APIs to maintain menus owned by another terminal/service without
 * crossing database boundaries.</p>
 */
public interface PluginSupplementalMenuPort {

    void sync(String pluginCode);

    void enable(String pluginCode);

    void disable(String pluginCode);

    void remove(String pluginCode);
}
