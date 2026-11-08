package com.houjiang.plugin.installer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import static com.houjiang.plugin.installer.InstallerTestSupport.*;
import static org.junit.jupiter.api.Assertions.*;

class InstallerAgentTest {
    @TempDir Path directory;
    InstallerTestSupport f;
    @BeforeEach void setup() throws Exception { f = new InstallerTestSupport(directory); }
    void state(String expected, JsonNode result) { assertEquals(expected, result.path("state").asText(), result.toString()); }
    void untouched(ObjectNode old) {
        assertSame(old, f.host.containers.get("unimall-erp"));
        assertTrue(old.path("State").path("Running").asBoolean());
    }
    void restored(Predicate<List<String>> before, Predicate<List<String>> after) throws Exception {
        ObjectNode old = f.host.add("unimall-erp");
        f.host.fail = before; f.host.failAfter = after;
        try (InstallerAgent agent = f.agent()) {
            state("ROLLED_BACK", f.install(agent)); untouched(old);
            assertEquals(Set.of("unimall-erp"), f.host.containers.keySet());
            assertTrue(f.host.commands.contains(List.of("/check-registration", "erp", "1.0.0")));
        }
    }

    @Test void successPersistsSignedIdentityAndUsesIsolatedCandidate() throws Exception {
        try (InstallerAgent agent = f.agent()) {
            JsonNode result = f.install(agent); state("SUCCESS", result);
            for (String key : List.of("pluginCode", "version", "imageId", "sourceSha256", "runtimeSha256")) assertEquals(manifest().get(key), result.get(key));
            assertArrayEquals(SOURCE, Files.readAllBytes(agent.root().resolve("sources/erp/1.2.3/source.tar.gz")));
            assertEquals(IMAGE, f.host.containers.get("unimall-erp").path("Image").asText());
            List<List<String>> launches = f.host.commands.stream().filter(a -> command(a, "docker", "run")).toList();
            assertEquals(2, launches.size());
            assertTrue(launches.stream().noneMatch(a -> a.contains("--volume") || a.contains("--mount")));
            assertTrue(launches.getFirst().contains("SPRING_CLOUD_NACOS_DISCOVERY_REGISTER_ENABLED=false"));
            assertFalse(launches.getLast().contains("SPRING_CLOUD_NACOS_DISCOVERY_REGISTER_ENABLED=false"));
            assertEquals(List.of("/check-registration", "erp", "1.2.3"), f.host.commands.getLast());
            assertEquals(IMAGE, InstallerAgent.JSON.readTree(result.path("result").asText()).path("imageId").asText());
        }
    }
    @Test void identicalPendingSubmissionIsIdempotent() throws Exception {
        try (InstallerAgent agent = f.agent()) {
            JsonNode first = agent.submit(f.bundle()), second = agent.submit(f.bundle());
            assertEquals(first, second); assertEquals(1, f.executor.queue.size());
            assertEquals(IMAGE, first.path("imageId").asText());
        }
    }
    @Test void differentPendingReleaseIdentityIsRejected() throws Exception {
        try (InstallerAgent agent = f.agent()) {
            agent.submit(f.bundle());
            for (String field : List.of("version", "imageId", "sourceSha256", "runtimeSha256")) {
                ObjectNode m = manifest();
                if (field.equals("version")) m.put("version", "2.0.0").put("image", "unimall-erp:2.0.0");
                else m.put(field, field.equals("imageId") ? OLD_IMAGE : "f".repeat(64));
                assertTrue(assertThrows(IllegalArgumentException.class, () -> agent.submit(f.bundle(m))).getMessage().contains("already"));
            }
        }
    }
    @Test void differentServiceGetsSeparateTask() throws Exception {
        try (InstallerAgent agent = f.agent()) {
            JsonNode first = agent.submit(f.bundle()), other = agent.submit(f.bundle(manifest("crm", "1.2.3")));
            assertNotEquals(first.path("taskId"), other.path("taskId")); assertEquals(2, agent.history().size());
        }
    }
    @Test void requestAndReturnedSnapshotsAreIndependent() throws Exception {
        try (InstallerAgent agent = f.agent()) {
            ObjectNode request = f.bundle(); JsonNode task = agent.submit(request);
            ((ObjectNode) request.path("source")).put("url", "changed");
            ((ObjectNode) task).put("state", "changed");
            assertEquals("PENDING", agent.status(task.path("taskId").asText()).path("state").asText());
            f.executor.drain(); state("SUCCESS", agent.status(task.path("taskId").asText()));
            assertTrue(f.downloads.getFirst().endsWith("=source"));
        }
    }
    @Test void unmanagedContainerIsNeverReplaced() throws Exception {
        ObjectNode old = f.host.add("unimall-erp", false);
        try (InstallerAgent agent = f.agent()) { state("FAILED", f.install(agent)); untouched(old); assertTrue(f.downloads.isEmpty()); }
    }
    @Test void preflightFailureKeepsOldRunning() throws Exception {
        ObjectNode old = f.host.add("unimall-erp"); f.host.fail = a -> a.getFirst().equals("/upgrade-preflight");
        try (InstallerAgent agent = f.agent()) { state("FAILED", f.install(agent)); untouched(old); assertFalse(f.host.commands.stream().anyMatch(a -> command(a, "docker", "run"))); }
    }
    @Test void malformedHooksAreRejectedBeforeQueueOrExternalWork() throws Exception {
        int index = 0;
        for (String key : List.of("registrationCheck", "upgradePreflight")) {
            for (String invalid : List.of("null", "\"shell command\"", "[]", "[\"\"]", "[123]", "[\"/check\",null]", "[\"/check\\u0000\"]")) {
                f.config.put("root", directory.resolve("invalid-" + index++).toString());
                ObjectNode cfg = (ObjectNode) f.config.path("services").path("erp"); JsonNode original = cfg.get(key);
                cfg.set(key, InstallerAgent.JSON.readTree(invalid));
                try (InstallerAgent agent = f.agent()) {
                    assertThrows(IllegalArgumentException.class, () -> agent.submit(f.bundle()));
                    assertTrue(f.executor.queue.isEmpty()); assertTrue(f.host.commands.isEmpty()); assertTrue(f.downloads.isEmpty());
                }
                cfg.set(key, original);
            }
        }
    }
    @Test void candidateHealthFailureKeepsOldRunning() throws Exception {
        ObjectNode old = f.host.add("unimall-erp"); f.host.unhealthy.add("unimall-erp-candidate");
        try (InstallerAgent agent = f.agent()) { state("FAILED", f.install(agent)); untouched(old); assertEquals(Set.of("unimall-erp"), f.host.containers.keySet()); }
    }
    @Test void finalHealthFailureRestoresOldImage() throws Exception {
        f.host.unhealthy.add("unimall-erp"); restored(a -> false, a -> false);
    }
    @Test void registrationFailureRestoresOldContainer() throws Exception { restored(a -> command(a, "/check-registration", "erp", "1.2.3"), a -> false); }
    @Test void renameFailureRestartsOldWithoutRemovingIt() throws Exception { restored(a -> command(a, "docker", "rename", "unimall-erp"), a -> false); }
    @Test void renameSideEffectThenErrorRestoresOldById() throws Exception { restored(a -> false, a -> command(a, "docker", "rename", "unimall-erp")); }
    @Test void candidateRunSideEffectThenErrorRemovesOnlyCandidate() throws Exception {
        ObjectNode old = f.host.add("unimall-erp"); f.host.failAfter = a -> command(a, "docker", "run");
        try (InstallerAgent agent = f.agent()) { state("FAILED", f.install(agent)); untouched(old); assertEquals(Set.of("unimall-erp"), f.host.containers.keySet()); }
    }
    @Test void inspectDaemonFailureIsNotAbsence() throws Exception {
        f.host.fail = a -> command(a, "docker", "inspect");
        try (InstallerAgent agent = f.agent()) { assertThrows(CommandException.class, () -> agent.inspect("unimall-erp")); }
    }
    @Test void imageIdentityMismatchPreventsLaunch() throws Exception {
        f.host.imageId = OLD_IMAGE;
        try (InstallerAgent agent = f.agent()) { state("FAILED", f.install(agent)); assertTrue(f.host.containers.isEmpty()); }
    }
    @Test void imageWithoutHealthcheckPreventsLaunch() throws Exception {
        f.host.healthcheck = InstallerAgent.JSON.createArrayNode().add("NONE");
        try (InstallerAgent agent = f.agent()) { state("FAILED", f.install(agent)); assertTrue(f.host.containers.isEmpty()); }
    }
    @Test void initialRegistrationFailureRemovesNewContainer() throws Exception {
        f.host.fail = a -> a.getFirst().equals("/check-registration");
        try (InstallerAgent agent = f.agent()) { state("FAILED", f.install(agent)); assertTrue(f.host.containers.isEmpty()); }
    }
    @Test void staleCandidateIsNotRemoved() throws Exception {
        ObjectNode stale = f.host.add("unimall-erp-candidate");
        try (InstallerAgent agent = f.agent()) { state("FAILED", f.install(agent)); assertSame(stale, f.host.containers.get("unimall-erp-candidate")); assertTrue(f.downloads.isEmpty()); }
    }
    @Test void successfulUpgradeRetainsStoppedPreviousContainer() throws Exception {
        ObjectNode old = f.host.add("unimall-erp");
        try (InstallerAgent agent = f.agent()) {
            JsonNode task = f.install(agent); state("SUCCESS", task);
            assertSame(old, f.host.containers.get("unimall-erp-previous-" + task.path("taskId").asText()));
            assertFalse(old.path("State").path("Running").asBoolean());
            assertTrue(f.host.commands.contains(List.of("/upgrade-preflight", "1.2.3")));
        }
    }
    @Test void finalRunSideEffectThenErrorRollsBack() throws Exception { restored(a -> false, a -> command(a, "docker", "run") && a.get(a.indexOf("--name") + 1).equals("unimall-erp")); }
    @Test void candidateCleanupRejectsWrongTaskLabel() throws Exception {
        ObjectNode foreign = f.host.add("unimall-erp-candidate");
        ((ObjectNode) foreign.path("Config").path("Labels")).put("unimall.task", "other-task");
        try (InstallerAgent agent = f.agent()) { assertThrows(IllegalArgumentException.class, () -> agent.removeCandidate("unimall-erp-candidate", "erp", "this-task")); assertSame(foreign, f.host.containers.get("unimall-erp-candidate")); }
    }
    @Test void candidateCleanupRejectsWrongServiceLabel() throws Exception {
        ObjectNode foreign = f.host.add("unimall-erp-candidate");
        ((ObjectNode) foreign.path("Config").path("Labels")).put("unimall.task", "task").put("unimall.service", "crm");
        try (InstallerAgent agent = f.agent()) { assertThrows(IllegalArgumentException.class, () -> agent.removeCandidate("unimall-erp-candidate", "erp", "task")); assertSame(foreign, f.host.containers.get("unimall-erp-candidate")); }
    }
    @Test void rollbackNeverRemovesForeignReplacement() throws Exception {
        ObjectNode old = f.host.add("unimall-erp");
        f.host.failAfter = a -> {
            if (command(a, "docker", "run") && a.get(a.indexOf("--name") + 1).equals("unimall-erp")) {
                ((ObjectNode) f.host.containers.get("unimall-erp").path("Config").path("Labels")).put("unimall.task", "foreign-task"); return true;
            }
            return false;
        };
        try (InstallerAgent agent = f.agent()) {
            JsonNode result = f.install(agent); state("FAILED", result);
            assertTrue(result.path("result").asText().contains("manual recovery"));
            assertEquals("foreign-task", f.host.containers.get("unimall-erp").path("Config").path("Labels").path("unimall.task").asText());
            assertTrue(f.host.containers.containsValue(old));
        }
    }
    @Test void stoppedPreviousContainerRemainsStoppedOnRollback() throws Exception {
        ObjectNode old = f.host.add("unimall-erp"); ((ObjectNode) old.path("State")).put("Running", false).put("Status", "exited");
        f.host.fail = a -> command(a, "/check-registration", "erp", "1.2.3");
        try (InstallerAgent agent = f.agent()) {
            state("ROLLED_BACK", f.install(agent)); assertSame(old, f.host.containers.get("unimall-erp"));
            assertFalse(old.path("State").path("Running").asBoolean());
            assertFalse(f.host.commands.stream().anyMatch(a -> command(a, "docker", "start")));
        }
    }
    @Test void queueRejectionPersistsSafeFailure() throws Exception {
        f.executor.reject = true;
        try (InstallerAgent agent = f.agent()) {
            assertThrows(IllegalArgumentException.class, () -> agent.submit(f.bundle()));
            JsonNode task = agent.history().get(0); state("FAILED", task); assertEquals("QUEUE", task.path("step").asText());
        }
    }
}
