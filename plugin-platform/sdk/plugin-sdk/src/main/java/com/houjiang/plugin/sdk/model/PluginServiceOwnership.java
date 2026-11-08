package com.houjiang.plugin.sdk.model;

import java.util.Set;

/** Shared ownership rules for artifact parsing, installation and service routing. */
public final class PluginServiceOwnership {
    public static final Set<String> SERVICES = Set.of(
            "mall", "goods", "payment", "order", "auth", "base", "job", "merchant", "permission",
            "thirdparty", "userms", "crm", "cms", "erp", "finance", "geo", "im", "logging",
            "mes", "oa", "orderfood", "pm", "rider", "supplier", "webpos", "tool");

    private PluginServiceOwnership() { }

    public static String requireService(String service) {
        if (service == null || !SERVICES.contains(service)) {
            throw new IllegalStateException("Unknown hostService (expected a business service name): " + service);
        }
        return service;
    }

    /**
     * Legacy mode only converts a known service name in host when hostService is absent.
     * URLs never establish ownership and conflicting declarations always fail closed.
     */
    public static String resolve(PluginManifest manifest, boolean allowLegacyHost) {
        if (manifest == null) {
            throw new IllegalStateException("Plugin manifest with hostService is required");
        }
        String service = manifest.getHostService();
        String legacy = manifest.getHost();
        if ((service == null || service.isBlank()) && allowLegacyHost) {
            service = legacy;
        }
        requireService(service);
        if (legacy != null && !legacy.isBlank() && !service.equals(legacy)) {
            throw new IllegalStateException("Legacy host conflicts with hostService; URLs must be resolved "
                    + "outside plugin manifests: host=" + legacy + ", hostService=" + service);
        }
        return service;
    }
}
