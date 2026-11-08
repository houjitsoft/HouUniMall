package com.houjiang.plugin.installer;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import static com.houjiang.plugin.installer.InstallerTestSupport.*;
import static org.junit.jupiter.api.Assertions.*;

class InstallerSignatureTest {
    @TempDir Path directory;
    InstallerTestSupport f;
    @BeforeEach void setup() throws Exception { f = new InstallerTestSupport(directory); }
    @Test void verifiesRealJdkEd25519Signature() throws Exception {
        try (InstallerAgent agent = f.agent()) { assertEquals(manifest(), agent.verify(f.signed(manifest()))); assertTrue(f.host.commands.isEmpty()); }
    }
    @Test void verifiesOriginalBytesRatherThanReserializedJson() throws Exception {
        byte[] payload = (" \n" + InstallerAgent.JSON.writerWithDefaultPrettyPrinter().writeValueAsString(manifest()) + "\n").getBytes(StandardCharsets.UTF_8);
        try (InstallerAgent agent = f.agent()) { assertEquals(manifest(), agent.verify(f.signed(payload))); }
    }
    @Test void rejectsTamperedSignatureAndPayload() throws Exception {
        try (InstallerAgent agent = f.agent()) {
            ObjectNode signature = f.signed(manifest()); signature.put("signature", Base64.getEncoder().encodeToString(new byte[64]));
            assertTrue(assertThrows(IllegalArgumentException.class, () -> agent.verify(signature)).getMessage().contains("Signature"));
            ObjectNode payload = f.signed(manifest()); payload.put("payload", Base64.getEncoder().encodeToString("{}".getBytes(StandardCharsets.UTF_8)));
            assertThrows(IllegalArgumentException.class, () -> agent.verify(payload));
        }
    }
    @Test void rejectsUnknownKeyAndWrongPublicKey() throws Exception {
        try (InstallerAgent agent = f.agent()) {
            ObjectNode unknown = f.signed(manifest()); unknown.put("kid", "untrusted");
            assertTrue(assertThrows(IllegalArgumentException.class, () -> agent.verify(unknown)).getMessage().contains("unknown"));
            ObjectNode envelope = f.signed(manifest());
            Path other = directory.resolve("other"); Files.createDirectory(other);
            InstallerTestSupport second = new InstallerTestSupport(other);
            Files.copy(Path.of(second.config.path("publicKeys").path("test").asText()), Path.of(f.config.path("publicKeys").path("test").asText()), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            assertThrows(IllegalArgumentException.class, () -> agent.verify(envelope));
        }
    }
    @Test void signedInvalidOwnershipAndIdentityAreRejected() throws Exception {
        try (InstallerAgent agent = f.agent()) {
            for (String change : List.of("{\"hostService\":\"crm\"}", "{\"pluginCode\":\"unknown\"}", "{\"runtime\":\"pf4j\"}",
                    "{\"sourceLicense\":\"proprietary\"}", "{\"image\":\"unimall-crm:1.2.3\"}", "{\"imageId\":\"sha256:bad\"}",
                    "{\"version\":\"../unsafe\"}", "{\"sourceSha256\":\"wrong\"}", "{\"sourceSize\":true}",
                    "{\"runtimeSize\":1.5}", "{\"sourceSize\":0}", "{\"runtimeSize\":-1}", "{\"sourceSize\":9223372036854775808}")) {
                ObjectNode m = manifest(); m.setAll((ObjectNode) InstallerAgent.JSON.readTree(change));
                assertThrows(IllegalArgumentException.class, () -> agent.verify(f.signed(m)), change);
            }
        }
    }
    @Test void signedSizeOverConfiguredMaximumIsRejected() throws Exception {
        f.config.put("maxArtifactBytes", 4);
        try (InstallerAgent agent = f.agent()) { assertThrows(IllegalArgumentException.class, () -> agent.verify(f.signed(manifest()))); }
    }
    @Test void malformedBase64AndSignatureLengthAreRejected() throws Exception {
        try (InstallerAgent agent = f.agent()) {
            for (String field : List.of("payload", "signature")) for (String value : List.of("!bad", "YQ", "Y Q=", "====")) {
                ObjectNode envelope = f.signed(manifest()); envelope.put(field, value);
                assertThrows(IllegalArgumentException.class, () -> agent.verify(envelope));
            }
            ObjectNode envelope = f.signed(manifest()); envelope.put("signature", Base64.getEncoder().encodeToString(new byte[63]));
            assertThrows(IllegalArgumentException.class, () -> agent.verify(envelope));
        }
    }
    @Test void rejectsOversizedPayloadAndSignedInvalidJson() throws Exception {
        try (InstallerAgent agent = f.agent()) {
            for (byte[] bytes : List.of(new byte[65537], "{".getBytes(StandardCharsets.UTF_8), (manifest() + " {}").getBytes(StandardCharsets.UTF_8)))
                assertThrows(IllegalArgumentException.class, () -> agent.verify(f.signed(bytes)));
        }
    }
    @Test void rejectsMissingDeploymentConfiguration() throws Exception {
        ((ObjectNode) f.config.path("services")).remove("erp");
        try (InstallerAgent agent = f.agent()) { assertThrows(IllegalArgumentException.class, () -> agent.verify(f.signed(manifest()))); }
    }
    @Test void malformedPemDoesNotLeakItsContent() throws Exception {
        Files.writeString(Path.of(f.config.path("publicKeys").path("test").asText()), "synthetic-private-marker");
        try (InstallerAgent agent = f.agent()) {
            Exception error = assertThrows(IllegalArgumentException.class, () -> agent.verify(f.signed(manifest())));
            assertEquals("Signature verification failed", InstallerAgent.safeMessage(error));
        }
    }
}
