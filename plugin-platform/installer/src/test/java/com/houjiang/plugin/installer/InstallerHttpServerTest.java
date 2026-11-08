package com.houjiang.plugin.installer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class InstallerHttpServerTest {
    // 仅本地测试使用的公开固定字符串，不是真实认证凭据。
    static final String TOKEN = "synthetic-local-test-token-not-a-real-secret";
    @TempDir Path directory;
    InstallerTestSupport f;
    final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    @BeforeEach void setup() throws Exception { f = new InstallerTestSupport(directory); }
    URI uri(InstallerHttpServer server, String path) { return URI.create("http://127.0.0.1:" + server.address().getPort() + path); }
    HttpRequest.Builder authorized(InstallerHttpServer server, String path) {
        return HttpRequest.newBuilder(uri(server, path)).timeout(Duration.ofSeconds(10)).header("Authorization", "Bearer " + TOKEN).header("X-UniMall-Instance", "test-instance");
    }
    HttpResponse<String> send(HttpRequest.Builder request) throws Exception { return client.send(request.build(), HttpResponse.BodyHandlers.ofString()); }
    @Test void authenticationAndInstanceAreBothRequired() throws Exception {
        try (InstallerAgent agent = f.agent(); InstallerHttpServer server = new InstallerHttpServer(f.config, agent, TOKEN)) {
            server.start();
            for (String[] headers : List.of(new String[]{"", ""}, new String[]{"Bearer wrong", "test-instance"}, new String[]{"Bearer " + TOKEN, "other-instance"}, new String[]{"Bearer " + TOKEN, ""})) {
                var request = HttpRequest.newBuilder(uri(server, "/tasks"));
                if (!headers[0].isEmpty()) request.header("Authorization", headers[0]);
                if (!headers[1].isEmpty()) request.header("X-UniMall-Instance", headers[1]);
                var response = send(request); assertEquals(403, response.statusCode()); assertEquals("Forbidden", InstallerAgent.JSON.readTree(response.body()).path("error").asText());
                assertFalse(response.body().contains(TOKEN));
            }
            assertEquals(200, send(authorized(server, "/tasks")).statusCode());
        }
    }
    @Test void duplicateAuthenticationHeadersAreRejected() throws Exception {
        try (InstallerAgent agent = f.agent(); InstallerHttpServer server = new InstallerHttpServer(f.config, agent, TOKEN)) {
            server.start();
            assertEquals(403, send(authorized(server, "/tasks").header("Authorization", "Bearer " + TOKEN)).statusCode());
            assertEquals(403, send(authorized(server, "/tasks").header("X-UniMall-Instance", "test-instance")).statusCode());
        }
    }
    @Test void taskPostStatusAndHistoryUseSignedRelease() throws Exception {
        try (InstallerAgent agent = f.agent(); InstallerHttpServer server = new InstallerHttpServer(f.config, agent, TOKEN)) {
            server.start();
            var response = send(authorized(server, "/tasks").POST(HttpRequest.BodyPublishers.ofByteArray(InstallerAgent.JSON.writeValueAsBytes(f.bundle()))));
            assertEquals(200, response.statusCode(), response.body());
            JsonNode task = InstallerAgent.JSON.readTree(response.body()); String id = task.path("taskId").asText();
            assertEquals("PENDING", task.path("state").asText());
            assertEquals(InstallerTestSupport.IMAGE, task.path("imageId").asText());
            assertEquals("no-store", response.headers().firstValue("Cache-Control").orElseThrow());
            assertTrue(response.headers().firstValue("Content-Type").orElseThrow().contains("application/json"));
            f.executor.drain();
            var status = send(authorized(server, "/tasks/" + id)); assertEquals(200, status.statusCode());
            assertEquals("SUCCESS", InstallerAgent.JSON.readTree(status.body()).path("state").asText());
            var history = send(authorized(server, "/tasks")); assertEquals(1, InstallerAgent.JSON.readTree(history.body()).size());
        }
    }
    @Test void oversizedEmptyAndChunkedRequestsAreRejected() throws Exception {
        try (InstallerAgent agent = f.agent(); InstallerHttpServer server = new InstallerHttpServer(f.config, agent, TOKEN)) {
            server.start();
            assertEquals(400, send(authorized(server, "/tasks").POST(HttpRequest.BodyPublishers.ofByteArray(new byte[InstallerHttpServer.MAX_REQUEST_BYTES + 1]))).statusCode());
            assertEquals(400, send(authorized(server, "/tasks").POST(HttpRequest.BodyPublishers.noBody())).statusCode());
            assertEquals(400, send(authorized(server, "/tasks").POST(HttpRequest.BodyPublishers.ofInputStream(() -> new ByteArrayInputStream("{}".getBytes(java.nio.charset.StandardCharsets.UTF_8))))).statusCode());
            assertTrue(agent.history().isEmpty());
        }
    }
    @Test void malformedJsonAndTrailingTokensAreRejected() throws Exception {
        try (InstallerAgent agent = f.agent(); InstallerHttpServer server = new InstallerHttpServer(f.config, agent, TOKEN)) {
            server.start();
            for (String body : List.of("{", "[]", "null", "{} {}")) {
                var response = send(authorized(server, "/tasks").POST(HttpRequest.BodyPublishers.ofString(body)));
                assertEquals(400, response.statusCode()); assertEquals("invalid JSON request", InstallerAgent.JSON.readTree(response.body()).path("error").asText());
            }
        }
    }
    @Test void exactRequestLimitIsAcceptedByFramingValidation() throws Exception {
        try (InstallerAgent agent = f.agent(); InstallerHttpServer server = new InstallerHttpServer(f.config, agent, TOKEN)) {
            server.start(); String body = "{}" + " ".repeat(InstallerHttpServer.MAX_REQUEST_BYTES - 2);
            var response = send(authorized(server, "/tasks").POST(HttpRequest.BodyPublishers.ofString(body)));
            assertEquals(400, response.statusCode()); assertFalse(response.body().contains("invalid request size"));
            assertTrue(response.body().contains("payload"));
        }
    }
    @Test void unknownRoutesAndTaskHaveDistinctResponses() throws Exception {
        try (InstallerAgent agent = f.agent(); InstallerHttpServer server = new InstallerHttpServer(f.config, agent, TOKEN)) {
            server.start();
            for (String path : List.of("/", "/tasks?all=true", "/tasks/not-a-uuid", "/tasks/", "/tasks/../tasks")) assertEquals(404, send(authorized(server, path)).statusCode());
            assertEquals(404, send(authorized(server, "/tasks").DELETE()).statusCode());
            assertEquals(400, send(authorized(server, "/tasks/00000000-0000-0000-0000-000000000000")).statusCode());
        }
    }
    @Test void serverRejectsWeakTokenEmptyInstanceAndInvalidPort() throws Exception {
        try (InstallerAgent agent = f.agent()) {
            assertThrows(IllegalArgumentException.class, () -> new InstallerHttpServer(f.config, agent, "short"));
            ObjectNode cfg = f.config.deepCopy(); cfg.put("instanceId", "");
            assertThrows(IllegalArgumentException.class, () -> new InstallerHttpServer(cfg, agent, TOKEN));
            for (String value : List.of("-1", "65536", "1.5", "\"123\"")) {
                ObjectNode invalid = f.config.deepCopy(); invalid.set("port", InstallerAgent.JSON.readTree(value));
                assertThrows(IllegalArgumentException.class, () -> new InstallerHttpServer(invalid, agent, TOKEN));
            }
        }
    }
    @Test void commandAndThirdPartyErrorsNeverExposeSyntheticSensitiveText() {
        String marker = "synthetic-sensitive-output";
        assertEquals("Container command failed; inspect agent host logs", InstallerAgent.safeMessage(new CommandException(1, marker)));
        assertEquals("Installer operation failed; inspect agent host state", InstallerAgent.safeMessage(new RuntimeException(marker)));
    }
}
