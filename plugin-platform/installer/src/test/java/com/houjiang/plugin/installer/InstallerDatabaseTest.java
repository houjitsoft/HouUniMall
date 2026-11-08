package com.houjiang.plugin.installer;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.util.HashSet;
import java.util.Set;
import static com.houjiang.plugin.installer.InstallerTestSupport.*;
import static org.junit.jupiter.api.Assertions.*;

class InstallerDatabaseTest {
    @TempDir Path directory;
    @Test void migratesLegacySchemaAndInterruptsOnlyActiveTasks() throws Exception {
        InstallerTestSupport f = new InstallerTestSupport(directory);
        Path root = Path.of(f.config.path("root").asText()); Files.createDirectories(root);
        try (var db = DriverManager.getConnection("jdbc:sqlite:" + root.resolve("tasks.sqlite")); var sql = db.createStatement()) {
            sql.execute("CREATE TABLE tasks(id TEXT PRIMARY KEY,code TEXT,version TEXT,state TEXT,step TEXT,result TEXT,created INTEGER)");
            for (String state : Set.of("SUCCESS", "FAILED", "ROLLED_BACK", "PENDING", "RUNNING"))
                sql.execute("INSERT INTO tasks VALUES('" + state + "','erp','1.0.0','" + state + "','START','legacy-result',1)");
        }
        try (InstallerAgent agent = f.agent()) {
            for (JsonNode row : agent.history()) {
                String id = row.path("taskId").asText();
                assertTrue(row.path("imageId").isNull()); assertTrue(row.path("sourceSha256").isNull()); assertTrue(row.path("runtimeSha256").isNull());
                if (Set.of("PENDING", "RUNNING").contains(id)) {
                    assertEquals("FAILED", row.path("state").asText()); assertEquals("INTERRUPTED", row.path("step").asText());
                    assertTrue(row.path("result").asText().contains("restarted"));
                } else { assertEquals(id, row.path("state").asText()); assertEquals("legacy-result", row.path("result").asText()); }
            }
            JsonNode task = agent.submit(f.bundle()); assertEquals(IMAGE, task.path("imageId").asText());
        }
        try (var db = DriverManager.getConnection("jdbc:sqlite:" + root.resolve("tasks.sqlite")); var sql = db.createStatement(); var rows = sql.executeQuery("PRAGMA table_info(tasks)")) {
            Set<String> columns = new HashSet<>(); while (rows.next()) columns.add(rows.getString("name"));
            assertTrue(columns.containsAll(Set.of("image_id", "source_sha256", "runtime_sha256")));
        }
    }
    @Test void restartPersistsIdentityAndMigrationIsIdempotent() throws Exception {
        InstallerTestSupport f = new InstallerTestSupport(directory); String id;
        try (InstallerAgent agent = f.agent()) { JsonNode task = f.install(agent); id = task.path("taskId").asText(); }
        try (InstallerAgent agent = f.agent()) {
            assertEquals("SUCCESS", agent.status(id).path("state").asText()); assertEquals(IMAGE, agent.status(id).path("imageId").asText());
            assertEquals(sha(SOURCE), agent.status(id).path("sourceSha256").asText());
        }
    }
    @Test void processLockRejectsSecondAgentAndReleasesOnClose() throws Exception {
        InstallerTestSupport f = new InstallerTestSupport(directory);
        try (InstallerAgent first = f.agent()) {
            var workers = new QueueExecutor();
            Exception error = assertThrows(IllegalArgumentException.class, () -> new InstallerAgent(f.config, f.host,
                    (uri, timeout) -> { throw new AssertionError("不应下载"); }, d -> {}, workers));
            assertTrue(error.getMessage().contains("owns this root")); assertTrue(workers.isShutdown());
            assertEquals(Path.of(f.config.path("root").asText()).toRealPath(), first.root());
        }
        try (InstallerAgent reopened = f.agent()) { assertNotNull(reopened.root()); }
    }
    @Test void historyIsLimitedOrderedAndIndependent() throws Exception {
        InstallerTestSupport f = new InstallerTestSupport(directory);
        try (InstallerAgent agent = f.agent()) {
            try (var db = DriverManager.getConnection("jdbc:sqlite:" + agent.root().resolve("tasks.sqlite")); var insert = db.prepareStatement("INSERT INTO tasks(id,code,version,state,step,result,created) VALUES(?,'erp','1.0.0','SUCCESS','HEALTH','',?)")) {
                for (int i = 0; i < 105; i++) { insert.setString(1, "history-" + i); insert.setInt(2, i); insert.executeUpdate(); }
            }
            var history = agent.history(); assertEquals(100, history.size()); assertEquals("history-104", history.get(0).path("taskId").asText());
            assertEquals("history-5", history.get(99).path("taskId").asText()); history.removeAll(); assertEquals(100, agent.history().size());
            assertThrows(IllegalArgumentException.class, () -> agent.status("unknown"));
        }
    }
    @Test void closeDrainsSubmittedWorkAndRejectsNewSubmissions() throws Exception {
        InstallerTestSupport f = new InstallerTestSupport(directory); InstallerAgent agent = f.agent();
        String id = agent.submit(f.bundle()).path("taskId").asText(); agent.close(); agent.close();
        assertEquals("SUCCESS", agent.status(id).path("state").asText());
        assertThrows(IllegalArgumentException.class, () -> agent.submit(f.bundle()));
    }
}
