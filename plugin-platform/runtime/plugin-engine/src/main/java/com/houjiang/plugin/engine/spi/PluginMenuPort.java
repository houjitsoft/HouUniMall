package com.houjiang.plugin.engine.spi;

import java.nio.file.Path;

/**
 * Host-side bridge for synchronizing a plugin's navigation permissions.
 * The engine deliberately knows nothing about the permission service schema.
 */
public interface PluginMenuPort {

    /** Register or refresh the plugin-owned menu rows before activation. */
    default void sync(String pluginCode, Path pluginPath) {
    }

    /** Make plugin-owned menu rows visible and enabled. */
    default void enable(String pluginCode) {
    }

    /** Hide plugin-owned menu rows while retaining their role assignments. */
    default void disable(String pluginCode) {
    }

    /** Remove plugin-owned menu rows and their role assignments. */
    default void remove(String pluginCode) {
    }
}
