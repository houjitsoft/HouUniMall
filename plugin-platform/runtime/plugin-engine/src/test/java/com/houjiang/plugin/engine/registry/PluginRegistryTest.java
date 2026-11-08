package com.houjiang.plugin.engine.registry;

import com.houjiang.plugin.engine.core.PluginState;
import com.houjiang.plugin.sdk.model.PluginManifest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PluginRegistryTest {

    @Test
    void putManifestPreservesStateAndSnapshotIsReadOnly() {
        PluginRegistry registry = new PluginRegistry();
        registry.putState("demo", PluginState.DISABLED);

        PluginManifest manifest = manifest("demo", "1.0.0", "thirdparty");
        registry.putManifest("demo", manifest);

        assertEquals(PluginState.DISABLED, registry.getState("demo"));
        assertEquals(manifest, registry.getManifest("demo"));
        assertEquals(1, registry.snapshot().size());
        assertThrows(UnsupportedOperationException.class,
                () -> registry.snapshot().put("other", new PluginRegistry.PluginRecord(
                        PluginState.RESOLVED, null)));
    }

    @Test
    void putManifestRejectsBlankCode() {
        PluginRegistry registry = new PluginRegistry();
        assertThrows(IllegalArgumentException.class, () -> registry.putManifest(" ", new PluginManifest()));
    }

    private PluginManifest manifest(String code, String version, String owner) {
        PluginManifest manifest = new PluginManifest();
        manifest.setCode(code);
        manifest.setVersion(version);
        manifest.setOwner(owner);
        return manifest;
    }
}
