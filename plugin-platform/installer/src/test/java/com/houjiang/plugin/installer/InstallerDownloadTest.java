package com.houjiang.plugin.installer;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import static com.houjiang.plugin.installer.InstallerTestSupport.*;
import static org.junit.jupiter.api.Assertions.*;

class InstallerDownloadTest {
    @TempDir Path directory;
    void localDownload(int status, byte[] body, DownloadCheck check) throws Exception {
        AtomicInteger requests = new AtomicInteger(), redirected = new AtomicInteger();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/base/plugin-center/artifact/raw", exchange -> {
            try (exchange) {
                requests.incrementAndGet();
                exchange.getResponseHeaders().set("Location", "/redirect-target");
                exchange.sendResponseHeaders(status, body.length); exchange.getResponseBody().write(body);
            }
        });
        server.createContext("/redirect-target", exchange -> { redirected.incrementAndGet(); exchange.sendResponseHeaders(200, -1); exchange.close(); });
        server.start();
        try {
            InstallerTestSupport f = new InstallerTestSupport(directory);
            f.config.put("catalogOrigin", "http://127.0.0.1:" + server.getAddress().getPort());
            // 使用双参数构造器，覆盖生产 HTTP transport，不替换下载网络实现。
            try (InstallerAgent agent = new InstallerAgent(f.config, f.host)) { check.run(agent, f, requests); }
            assertEquals(0, redirected.get());
        } finally { server.stop(0); }
    }
    @FunctionalInterface interface DownloadCheck { void run(InstallerAgent agent, InstallerTestSupport f, AtomicInteger requests) throws Exception; }
    void noPartial(Path path) { assertFalse(Files.exists(path.resolveSibling(path.getFileName() + ".part"))); }
    @Test void realHttpDownloadAndVerifiedCacheReuse() throws Exception {
        localDownload(200, SOURCE, (agent, f, requests) -> {
            Path target = directory.resolve("source.tar.gz");
            agent.download(f.bundle().path("source"), target, sha(SOURCE), SOURCE.length);
            assertArrayEquals(SOURCE, Files.readAllBytes(target)); assertEquals(sha(SOURCE), InstallerAgent.digest(target));
            agent.download(f.bundle().path("source"), target, sha(SOURCE), SOURCE.length); assertEquals(1, requests.get()); noPartial(target);
        });
    }
    @Test void corruptCacheIsRedownloaded() throws Exception {
        localDownload(200, SOURCE, (agent, f, requests) -> {
            Path target = directory.resolve("cache"); Files.write(target, new byte[SOURCE.length]);
            agent.download(f.bundle().path("source"), target, sha(SOURCE), SOURCE.length);
            assertEquals(1, requests.get()); assertArrayEquals(SOURCE, Files.readAllBytes(target)); noPartial(target);
        });
    }
    @Test void hashMismatchRemovesPartialAndPreservesExistingFile() throws Exception {
        localDownload(200, SOURCE, (agent, f, requests) -> {
            Path target = directory.resolve("hash"); Files.writeString(target, "previous-cache");
            Exception error = assertThrows(IllegalArgumentException.class, () -> agent.download(f.bundle().path("source"), target, "f".repeat(64), SOURCE.length));
            assertTrue(error.getMessage().contains("digest")); assertEquals("previous-cache", Files.readString(target)); noPartial(target);
        });
    }
    @Test void oversizeResponseIsRejectedAndPartialRemoved() throws Exception {
        localDownload(200, SOURCE, (agent, f, requests) -> {
            Path target = directory.resolve("oversize");
            assertTrue(assertThrows(IllegalArgumentException.class, () -> agent.download(f.bundle().path("source"), target, sha(SOURCE), SOURCE.length - 1)).getMessage().contains("exceeds"));
            assertFalse(Files.exists(target)); noPartial(target);
        });
    }
    @Test void truncatedResponseIsRejectedAndPartialRemoved() throws Exception {
        localDownload(200, SOURCE, (agent, f, requests) -> {
            Path target = directory.resolve("short");
            assertThrows(IllegalArgumentException.class, () -> agent.download(f.bundle().path("source"), target, sha(SOURCE), SOURCE.length + 1));
            assertFalse(Files.exists(target)); noPartial(target);
        });
    }
    @Test void realHttpRedirectIsNeverFollowed() throws Exception {
        localDownload(302, SOURCE, (agent, f, requests) -> {
            Path target = directory.resolve("redirect");
            assertEquals("artifact redirects are forbidden", assertThrows(IllegalArgumentException.class, () -> agent.download(f.bundle().path("source"), target, sha(SOURCE), SOURCE.length)).getMessage());
            assertEquals(1, requests.get()); assertFalse(Files.exists(target)); noPartial(target);
        });
    }
    @Test void realHttpErrorIsSafeAndLeavesNoPartial() throws Exception {
        localDownload(403, SOURCE, (agent, f, requests) -> {
            Path target = directory.resolve("forbidden");
            assertEquals("artifact download failed", assertThrows(IllegalArgumentException.class, () -> agent.download(f.bundle().path("source"), target, sha(SOURCE), SOURCE.length)).getMessage()); noPartial(target);
        });
    }
    @Test void externalAndMalformedUrlsAreRejectedBeforeTransport() throws Exception {
        InstallerTestSupport f = new InstallerTestSupport(directory);
        try (InstallerAgent agent = f.agent()) {
            for (String url : List.of("https://other.invalid/source", "//other.invalid/source", "/base/plugin-center/artifact/raw?token=x#fragment", "/base/plugin-center/artifact/raw?token=x\r\nHeader: x", "/base/plugin-center/artifact/raw/extra?token=x")) {
                var record = InstallerAgent.JSON.createObjectNode().put("url", url);
                assertThrows(IllegalArgumentException.class, () -> agent.download(record, directory.resolve("invalid"), sha(SOURCE), SOURCE.length));
            }
            assertTrue(f.downloads.isEmpty());
        }
    }
    @Test void invalidOriginIsRejectedBeforeTransport() throws Exception {
        InstallerTestSupport f = new InstallerTestSupport(directory); f.config.put("catalogOrigin", "http://user:synthetic@127.0.0.1/path");
        try (InstallerAgent agent = f.agent()) { assertThrows(IllegalArgumentException.class, () -> agent.download(f.bundle().path("source"), directory.resolve("invalid"), sha(SOURCE), SOURCE.length)); assertTrue(f.downloads.isEmpty()); }
    }
}
