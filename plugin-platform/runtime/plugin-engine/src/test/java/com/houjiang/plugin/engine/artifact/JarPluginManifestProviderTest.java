package com.houjiang.plugin.engine.artifact;

import com.houjiang.plugin.sdk.model.PluginManifest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JarPluginManifestProviderTest {

    @TempDir
    Path tempDir;

    @Test
    void loadsManifestFromArtifactAndNormalizesOwner() throws Exception {
        Path jar = writeJar("code: demo\nversion: 1.2.3\nowner: THIRDParty\nhostService: goods\nruntime: standalone\n");

        PluginManifest manifest = new JarPluginManifestProvider().load("demo", jar);

        assertEquals("demo", manifest.getCode());
        assertEquals("1.2.3", manifest.getVersion());
        assertEquals("thirdparty", manifest.getOwner());
        assertEquals("standalone", manifest.getRuntime());
    }

    @Test
    void rejectsMissingManifest() throws Exception {
        Path jar = tempDir.resolve("demo-1.0.0.jar");
        try (JarOutputStream output = new JarOutputStream(Files.newOutputStream(jar))) {
            output.putNextEntry(new JarEntry("empty.txt"));
            output.write("ok".getBytes(StandardCharsets.UTF_8));
            output.closeEntry();
        }

        assertThrows(IllegalStateException.class, () -> new JarPluginManifestProvider().load("demo", jar));
    }

    @Test
    void rejectsCodeMismatchAndUnsupportedOwner() throws Exception {
        Path mismatch = writeJar("code: other\nversion: 1.0.0\nowner: official\n");
        assertThrows(IllegalStateException.class, () -> new JarPluginManifestProvider().load("demo", mismatch));

        Path unsupported = writeJar("code: demo\nversion: 1.0.0\nowner: forged\n");
        assertThrows(IllegalStateException.class, () -> new JarPluginManifestProvider().load("demo", unsupported));
    }

    @Test
    void rejectsMissingHostServiceByDefault() throws Exception {
        Path jar = writeJar("code: demo\nversion: 1.0.0\n");
        assertThrows(IllegalStateException.class, () -> new JarPluginManifestProvider().load("demo", jar));
    }

    @ParameterizedTest
    @ValueSource(strings = {"mall", "unknown", "http://goods", "GOODS", "goods/../oa", "127.0.0.1"})
    void rejectsUnknownOrUrlServiceOwnership(String hostService) throws Exception {
        Path jar = writeJar("code: demo\nversion: 1.0.0\nhostService: " + hostService + "\n");
        assertThrows(IllegalStateException.class, () -> new JarPluginManifestProvider().load("demo", jar));
    }

    @ParameterizedTest
    @ValueSource(strings = {"oa", "http://goods", "http://127.0.0.1:8320"})
    void rejectsConflictingOrUrlLegacyHost(String host) throws Exception {
        Path jar = writeJar("code: demo\nversion: 1.0.0\nhostService: goods\nhost: " + host + "\n");
        assertThrows(IllegalStateException.class, () -> new JarPluginManifestProvider().load("demo", jar));
    }

    @Test
    void explicitlyEnabledLegacyModeConvertsKnownHostOnly() throws Exception {
        Path jar = writeJar("code: demo\nversion: 1.0.0\nhost: goods\n");
        assertThrows(IllegalStateException.class, () -> new JarPluginManifestProvider().load("demo", jar));
        assertEquals("goods", new JarPluginManifestProvider(true).load("demo", jar).getHostService());
        Path oldMall = writeJar("code: demo\nversion: 1.0.0\nhost: mall\n");
        assertThrows(IllegalStateException.class, () -> new JarPluginManifestProvider(true).load("demo", oldMall));
    }

    @Test
    void matchingLegacyHostDoesNotConflictWithCanonicalOwnership() throws Exception {
        Path jar = writeJar("code: demo\nversion: 1.0.0\nhostService: goods\nhost: goods\n");
        assertEquals("goods", new JarPluginManifestProvider().load("demo", jar).getHostService());
    }

    private Path writeJar(String yaml) throws Exception {
        Path jar = Files.createTempFile(tempDir, "demo-", ".jar");
        try (JarOutputStream output = new JarOutputStream(Files.newOutputStream(jar))) {
            output.putNextEntry(new JarEntry("plugin.yaml"));
            output.write(yaml.getBytes(StandardCharsets.UTF_8));
            output.closeEntry();
        }
        return jar;
    }
}
