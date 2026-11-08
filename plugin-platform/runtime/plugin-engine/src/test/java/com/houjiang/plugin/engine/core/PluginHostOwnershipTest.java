package com.houjiang.plugin.engine.core;

import com.houjiang.plugin.engine.registry.ExtensionRegistry;
import com.houjiang.plugin.engine.artifact.JarPluginManifestProvider;
import com.houjiang.plugin.engine.spi.PluginMenuPort;
import com.houjiang.plugin.engine.spi.PluginMigrationPort;
import com.houjiang.plugin.engine.registry.PluginRegistry;
import com.houjiang.plugin.engine.registry.RouteRegistry;
import com.houjiang.plugin.sdk.model.PluginManifest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.pf4j.DefaultPluginManager;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.nio.charset.StandardCharsets;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PluginHostOwnershipTest {
    @TempDir Path tempDir;

    private PluginManifest manifest(String service) {
        PluginManifest manifest = new PluginManifest();
        manifest.setCode("demo");
        manifest.setVersion("1.0.0");
        manifest.setHostService(service);
        return manifest;
    }

    private PluginLifecycleManager manager(DefaultPluginManager pf4j, PluginManifest artifact, String service) {
        return new PluginLifecycleManager(pf4j, new ExtensionRegistry(), new RouteRegistry(),
                new PluginRegistry(), null, List.of(), null, null, null,
                (code, jar) -> artifact, service, false);
    }

    private Path jar() throws Exception {
        return Files.writeString(tempDir.resolve("demo.jar"), "fixture");
    }

    @Test void matchingEmbeddedOwnershipLoads() throws Exception {
        DefaultPluginManager pf4j = mock(DefaultPluginManager.class);
        when(pf4j.loadPlugin(any(Path.class))).thenReturn("demo");
        assertEquals(PluginState.ACTIVE, manager(pf4j, manifest("goods"), "goods").install("demo", jar()));
        verify(pf4j).startPlugin("demo");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"oa", "mall", "http://goods", "unknown"})
    void missingUnknownOrForeignOwnershipNeverLoads(String owner) throws Exception {
        DefaultPluginManager pf4j = mock(DefaultPluginManager.class);
        Path jar = jar();
        assertThrows(IllegalStateException.class,
                () -> manager(pf4j, manifest(owner), "goods").install("demo", jar));
        verifyNoInteractions(pf4j);
    }

    @Test void standaloneNeverLoadsEvenIntoItsOwnService() throws Exception {
        DefaultPluginManager pf4j = mock(DefaultPluginManager.class);
        PluginManifest manifest = manifest("goods");
        manifest.setRuntime("standalone");
        Path jar = jar();
        assertThrows(IllegalStateException.class,
                () -> manager(pf4j, manifest, "goods").install("demo", jar));
        verifyNoInteractions(pf4j);
    }

    @Test void suppliedOwnershipCannotConflictWithArtifact() throws Exception {
        DefaultPluginManager pf4j = mock(DefaultPluginManager.class);
        Path jar = jar();
        assertThrows(IllegalStateException.class,
                () -> manager(pf4j, manifest("goods"), "goods").install("demo", jar, manifest("oa")));
        verifyNoInteractions(pf4j);
    }

    @Test void omittedManifestNeverLoadsEvenThroughLegacyConstructor() throws Exception {
        DefaultPluginManager pf4j = mock(DefaultPluginManager.class);
        PluginLifecycleManager manager = new PluginLifecycleManager(pf4j, new ExtensionRegistry(),
                new RouteRegistry(), new PluginRegistry());
        Path jar = jar();
        assertThrows(IllegalStateException.class, () -> manager.install("demo", jar));
        verifyNoInteractions(pf4j);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"oa", "unknown", "http://goods"})
    void enableRejectsMissingOrForeignOwnershipWithoutSideEffects(String owner) {
        Fixture fixture = new Fixture();
        PluginManifest previous = owner == null ? null : manifest(owner);
        fixture.registry.put("demo", PluginState.DISABLED, previous);
        assertThrows(IllegalStateException.class, () -> fixture.manager.enable("demo"));
        assertEquals(PluginState.DISABLED, fixture.registry.getState("demo"));
        assertSame(previous, fixture.registry.getManifest("demo"));
        fixture.assertNoRuntimeSideEffects();
    }

    @Test void enableRejectsStandaloneBeforePf4jOrMenus() {
        Fixture fixture = new Fixture();
        PluginManifest previous = manifest("goods");
        previous.setRuntime("standalone");
        fixture.registry.put("demo", PluginState.DISABLED, previous);
        assertThrows(IllegalStateException.class, () -> fixture.manager.enable("demo"));
        assertEquals(PluginState.DISABLED, fixture.registry.getState("demo"));
        fixture.assertNoRuntimeSideEffects();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"unknown", "mall", "http://goods", "oa"})
    void enableRejectsInvalidEngineIdentityAndPreservesActiveRecord(String service) {
        Fixture fixture = new Fixture();
        PluginManifest running = manifest("goods");
        fixture.registry.put("demo", PluginState.ACTIVE, running);
        PluginLifecycleManager manager = new PluginLifecycleManager(fixture.pf4j, fixture.extensions,
                fixture.routes, fixture.registry, null, List.of(), fixture.migrations, fixture.menus,
                fixture.contexts, new JarPluginManifestProvider(), service, false);
        assertThrows(IllegalStateException.class, () -> manager.enable("demo"));
        assertEquals(PluginState.ACTIVE, fixture.registry.getState("demo"));
        assertSame(running, fixture.registry.getManifest("demo"));
        fixture.assertNoRuntimeSideEffects();
    }

    @Test void metadataOnlyInstallCannotPublishActiveState() {
        Fixture fixture = new Fixture();
        assertThrows(IllegalStateException.class, () -> fixture.manager.install("demo"));
        assertNull(fixture.registry.getState("demo"));
        assertThrows(IllegalStateException.class, () -> fixture.manager.enable("demo"));
        fixture.assertNoRuntimeSideEffects();
    }

    @ParameterizedTest
    @ValueSource(strings = {"foreign", "standalone", "unknown", "missing-jar", "missing-host"})
    void rejectedInstallationPreservesRunningPlugin(String rejection) throws Exception {
        Fixture fixture = new Fixture();
        PluginManifest running = manifest("goods");
        fixture.registry.put("demo", PluginState.ACTIVE, running);
        Path artifact = switch (rejection) {
            case "foreign" -> artifact("oa", "embedded");
            case "standalone" -> artifact("goods", "standalone");
            case "unknown" -> artifact("arbitrary", "embedded");
            case "missing-host" -> artifact(null, "embedded");
            default -> tempDir.resolve("missing.jar");
        };
        assertThrows(RuntimeException.class, () -> fixture.manager.install("demo", artifact));
        assertEquals(PluginState.ACTIVE, fixture.registry.getState("demo"));
        assertSame(running, fixture.registry.getManifest("demo"));
        fixture.assertNoRuntimeSideEffects();
    }

    @Test void realStandaloneArtifactIsRejectedForRuntimeNotAConflictingDeclaration() throws Exception {
        Fixture fixture = new Fixture();
        Path standalone = artifact("goods", "standalone");
        IllegalStateException failure = assertThrows(IllegalStateException.class,
                () -> fixture.manager.install("demo", standalone));
        assertTrue(failure.getMessage().contains("Only embedded plugins"));
        fixture.assertNoRuntimeSideEffects();
    }

    private Path artifact(String service, String runtime) throws Exception {
        Path jar = tempDir.resolve("product.jar");
        try (JarOutputStream output = new JarOutputStream(Files.newOutputStream(jar))) {
            output.putNextEntry(new JarEntry("plugin.yaml"));
            output.write(("code: demo\nversion: 1.0.0\nruntime: " + runtime + "\n"
                    + (service == null ? "" : "hostService: " + service + "\n"))
                    .getBytes(StandardCharsets.UTF_8));
            output.closeEntry();
        }
        return jar;
    }

    private static class Fixture {
        final DefaultPluginManager pf4j = mock(DefaultPluginManager.class);
        final PluginRegistry registry = new PluginRegistry();
        final ExtensionRegistry extensions = mock(ExtensionRegistry.class);
        final RouteRegistry routes = mock(RouteRegistry.class);
        final PluginMenuPort menus = mock(PluginMenuPort.class);
        final PluginMigrationPort migrations = mock(PluginMigrationPort.class);
        final PluginSpringContextManager contexts = mock(PluginSpringContextManager.class);
        final PluginLifecycleManager manager = new PluginLifecycleManager(pf4j, extensions, routes,
                registry, null, List.of(), migrations, menus, contexts,
                new JarPluginManifestProvider(), "goods", false);

        void assertNoRuntimeSideEffects() {
            verifyNoInteractions(pf4j, extensions, routes, menus, migrations, contexts);
        }
    }

    @Test void unknownEngineIdentityNeverLoads() throws Exception {
        DefaultPluginManager pf4j = mock(DefaultPluginManager.class);
        Path jar = jar();
        assertThrows(IllegalStateException.class,
                () -> manager(pf4j, manifest("goods"), null).install("demo", jar));
        verifyNoInteractions(pf4j);
    }
}
