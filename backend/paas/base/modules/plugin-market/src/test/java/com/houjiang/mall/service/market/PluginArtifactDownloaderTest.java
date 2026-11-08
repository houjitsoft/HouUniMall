package com.houjiang.mall.service.market;

import com.houjiang.plugin.engine.artifact.PluginChecksumVerifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 安装包下载器单测：验证 {@code file:} 协议下载、摘要计算与体积上限。
 *
 * <p>{@code http(s)} 分支需要外网，验收环境不联网，故只用 {@code file:} 覆盖「下载 → 摘要」
 * 这条与协议无关的主干逻辑。
 *
 * @author Amos
 */
class PluginArtifactDownloaderTest {

    @TempDir
    Path tempDir;

    private Path writeJar(String name, byte[] content) throws IOException {
        Path jar = tempDir.resolve(name);
        Files.write(jar, content);
        return jar;
    }

    @Test
    void downloadReadsLocalFileAndComputesMd5() throws IOException {
        byte[] content = "mall-live-1.0.0-bytes".getBytes();
        Path jar = writeJar("mall-live-1.0.0.jar", content);
        PluginArtifactDownloader downloader = new PluginArtifactDownloader(1024 * 1024);

        PluginArtifactDownloader.Artifact artifact = downloader.download(jar.toUri().toString());

        assertEquals(content.length, artifact.size());
        assertEquals("file", artifact.getProtocol());
        assertEquals(new PluginChecksumVerifier().md5(content), artifact.md5());
        assertEquals(jar.toUri().toString(), artifact.getSourceUrl());
    }

    @Test
    void downloadRejectsMissingLocalFile() {
        PluginArtifactDownloader downloader = new PluginArtifactDownloader(1024);
        String url = tempDir.resolve("not-exists.jar").toUri().toString();

        IOException ex = assertThrows(IOException.class, () -> downloader.download(url));

        assertTrue(ex.getMessage().contains("安装包文件不存在"), "实际=" + ex.getMessage());
    }

    @Test
    void downloadRejectsUnsupportedProtocol() {
        PluginArtifactDownloader downloader = new PluginArtifactDownloader(1024);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> downloader.download("ftp://market.houjit.com/jars/a.jar"));

        assertTrue(ex.getMessage().contains("不支持的安装包地址协议"), "实际=" + ex.getMessage());
    }

    @Test
    void downloadRejectsOversizeLocalFile() throws IOException {
        Path jar = writeJar("big.jar", new byte[2048]);
        PluginArtifactDownloader downloader = new PluginArtifactDownloader(1024);

        IOException ex = assertThrows(IOException.class, () -> downloader.download(jar.toUri().toString()));

        assertTrue(ex.getMessage().contains("超出体积上限"), "实际=" + ex.getMessage());
    }

    @Test
    void supportsRecognizesSupportedProtocolsOnly() {
        PluginArtifactDownloader downloader = new PluginArtifactDownloader(1024);

        assertTrue(downloader.supports("https://market.houjit.com/jars/a.jar"));
        assertTrue(downloader.supports("http://127.0.0.1/a.jar"));
        assertTrue(downloader.supports("file:///tmp/a.jar"));
        assertFalse(downloader.supports("ftp://market.houjit.com/a.jar"));
        assertFalse(downloader.supports(null));
        assertFalse(downloader.supports("  "));
    }
}
