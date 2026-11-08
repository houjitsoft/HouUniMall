package com.houjiang.plugin.engine.spi;

import com.houjiang.plugin.sdk.model.PluginManifest;

import java.nio.file.Path;

/**
 * Provides the verified runtime manifest for an installed plugin artifact.
 *
 * <p>The provider is deliberately an engine SPI so every installation path
 * (market install, process restart and an operator-triggered reload) uses the
 * same source of truth. Implementations must read the manifest from the
 * artifact itself or from another already-verified store; callers must not
 * infer ownership from extension annotations.</p>
 */
@FunctionalInterface
public interface PluginManifestProvider {

    /**
     * Reads the runtime manifest for an artifact.
     *
     * @param code    code supplied by the installation record or caller
     * @param jarPath immutable plugin artifact
     * @return manifest, never {@code null}
     * @throws RuntimeException when the artifact has no valid manifest or the
     *                          declared code does not match {@code code}
     */
    PluginManifest load(String code, Path jarPath);
}
