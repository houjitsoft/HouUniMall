package com.houjiang.mall.service.plugin;

import com.houjiang.plugin.sdk.model.PluginServiceOwnership;
import java.nio.file.Files;
import java.nio.file.Path;

/** Resolves canonical owner/code sources and owner-scoped versioned artifacts. */
final class PluginSourceLayout {
    private PluginSourceLayout() {}

    static java.util.List<Path> productDirectories(Path root, String code) {
        PluginAssetResolver.requireCode(code);
        Path normalized = root.toAbsolutePath().normalize();
        Path found = null;
        for (String service : PluginServiceOwnership.SERVICES) {
            Path candidate = normalized.resolve(service).resolve(code);
            if (!Files.isDirectory(candidate)) continue;
            if (found != null) throw new IllegalStateException("Duplicate plugin code across service directories: " + code);
            found = candidate;
        }
        // Existing deployed flat artifacts remain readable during cutover. New
        // repository sources and release tooling always use owner/code.
        Path legacy = normalized.resolve(code);
        return found == null ? java.util.List.of(legacy) : java.util.List.of(found, legacy);
    }
}
