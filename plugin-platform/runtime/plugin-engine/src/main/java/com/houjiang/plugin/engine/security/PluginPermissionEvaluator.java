package com.houjiang.plugin.engine.security;

/**
 * Host-provided authorization contract for plugin routes.
 */
public interface PluginPermissionEvaluator {

    boolean isAuthenticated();

    boolean hasPermission(String permission);
}
