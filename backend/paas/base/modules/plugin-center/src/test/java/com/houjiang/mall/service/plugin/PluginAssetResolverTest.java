package com.houjiang.mall.service.plugin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link PluginAssetResolver} 单测（T11）：覆盖双形态解析、优先级、穿越与后缀白名单、MIME 与缓存头。
 *
 * @author Amos
 */
class PluginAssetResolverTest {

    @TempDir
    Path tempDir;

    private PluginAssetResolver resolver;

    @Test
    void resolvesOwnedSourceAndVersionedArtifacts() throws IOException {
        write("oa/oa/frontend/index.mjs", "owned source");
        write("erp/erp/1.0.0/frontend/index.mjs", "owned version");
        assertNotNull(resolver.resolve("oa", "1.0.0", "index.mjs"));
        assertNotNull(resolver.resolve("erp", "1.0.0", "index.mjs"));
    }

    @Test
    void duplicateProductAcrossOwnersIsRejected() throws IOException {
        write("oa/duplicate/frontend/index.mjs", "first");
        write("erp/duplicate/frontend/index.mjs", "second");
        assertThrows(IllegalStateException.class, () -> resolver.resolve("duplicate", "1.0.0", "index.mjs"));
    }

    @Test
    void sourceDirectoryWithoutBuiltAssetsDoesNotHideDeployedLegacyVersion() throws IOException {
        write("oa/mixed/plugin.yaml", "code: mixed\nhostService: oa");
        write("mixed/1.0.0/frontend/index.mjs", "deployed artifact");
        assertNotNull(resolver.resolve("mixed", "1.0.0", "index.mjs"));
    }

    @BeforeEach
    void setUp() throws IOException {
        resolver = new PluginAssetResolver(tempDir);
        // 目录形态
        write("demo/1.0.0/frontend/index.mjs", "export const ok = true");
        write("demo/1.0.0/plain.js", "console.log('root-level')");
        write("demo/1.0.0/frontend/forbidden.jsp", "<% out.print(1); %>");
        // JAR 形态
        Map<String, String> jarEntries = new LinkedHashMap<>();
        jarEntries.put("plugin.yaml", "code: jarplug");
        jarEntries.put("frontend/index.mjs", "export const fromJar = true");
        jarEntries.put("frontend/dist/chunk-abc.mjs", "export const chunk = 1");
        jarEntries.put("frontend/asset/style.css", ".a{color:#fff}");
        jarEntries.put("frontend/asset/logo.png", "PNGDATA");
        jarEntries.put("frontend/asset/font.woff2", "WOFF2DATA");
        jarEntries.put("frontend/asset/data.json", "{\"a\":1}");
        writeJar("jarplug/2.0.0/jarplug-2.0.0.jar", jarEntries);
        // 目录与 JAR 同时存在 → 目录优先
        write("both/1.0.0/frontend/index.mjs", "DIR");
        Map<String, String> bothJar = new LinkedHashMap<>();
        bothJar.put("frontend/index.mjs", "JAR");
        writeJar("both/1.0.0/both-1.0.0.jar", bothJar);
        write("legacy/frontend/index.mjs", "LEGACY-DIR");
        // 目录存在但无产物
        Files.createDirectories(tempDir.resolve("empty/1.0.0"));
        // 用于穿越用例的版本目录外文件
        write("secret.txt", "TOP-SECRET");
        write("demo/1.0.0/frontend/../sibling.txt", "SIBLING");
    }

    @Test
    @DisplayName("目录形态：frontend/ 下的产物可命中")
    void resolveFromDirectory() {
        PluginAssetResolver.PluginAsset asset = resolver.resolve("demo", "1.0.0", "index.mjs");
        assertNotNull(asset);
        assertEquals("export const ok = true", new String(asset.content(), StandardCharsets.UTF_8));
        assertEquals("application/javascript; charset=UTF-8", asset.contentType());
        assertEquals(PluginAssetResolver.CACHE_CONTROL, asset.cacheControl());
        assertTrue(asset.source().startsWith("dir:"));
    }

    @Test
    @DisplayName("目录形态：兼容产物直接放在版本目录根，且前导 / 会被归一化")
    void resolveFromDirectoryRootAndLeadingSlash() {
        PluginAssetResolver.PluginAsset asset = resolver.resolve("demo", "1.0.0", "/plain.js");
        assertNotNull(asset);
        assertEquals("console.log('root-level')", new String(asset.content(), StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("JAR 形态：从 frontend/** 读取（含子目录与相对引用）")
    void resolveFromJar() {
        PluginAssetResolver.PluginAsset top = resolver.resolve("jarplug", "2.0.0", "index.mjs");
        assertNotNull(top);
        assertEquals("export const fromJar = true", new String(top.content(), StandardCharsets.UTF_8));
        assertTrue(top.source().replace('\\', '/').contains("jarplug-2.0.0.jar!/frontend/index.mjs"));

        PluginAssetResolver.PluginAsset chunk = resolver.resolve("jarplug", "2.0.0", "dist/chunk-abc.mjs");
        assertNotNull(chunk);
        assertEquals("export const chunk = 1", new String(chunk.content(), StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("目录优先于 JAR（便于开发期热改产物）")
    void directoryWinsOverJar() {
        PluginAssetResolver.PluginAsset asset = resolver.resolve("both", "1.0.0", "index.mjs");
        assertNotNull(asset);
        assertEquals("DIR", new String(asset.content(), StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("Local plugin directory without a version child still serves frontend assets")
    void resolveFromLegacyPluginDirectory() {
        PluginAssetResolver.PluginAsset asset = resolver.resolve("legacy", "1.0.0", "index.mjs");
        assertNotNull(asset);
        assertEquals("LEGACY-DIR", new String(asset.content(), StandardCharsets.UTF_8));
        assertTrue(asset.source().replace('\\', '/').endsWith("legacy/frontend/index.mjs"));
    }

    @Test
    @DisplayName("产物不存在返回 null（调用方映射 404）")
    void notFoundReturnsNull() {
        assertNull(resolver.resolve("demo", "1.0.0", "missing.mjs"));
        assertNull(resolver.resolve("nosuch", "1.0.0", "index.mjs"));
        assertNull(resolver.resolve("empty", "1.0.0", "index.mjs"));
        assertNull(resolver.resolve("jarplug", "2.0.0", "dist/not-exist.mjs"));
        assertNull(resolver.resolve("jarplug", "9.9.9", "index.mjs"));
    }

    @Test
    @DisplayName("路径穿越一律拒绝（明文与 URL 编码）")
    void rejectTraversal() {
        assertEquals(400, assertThrows(PluginAssetException.class,
                () -> resolver.resolve("demo", "1.0.0", "../sibling.txt")).getStatus());
        assertEquals(400, assertThrows(PluginAssetException.class,
                () -> resolver.resolve("demo", "1.0.0", "%2e%2e/sibling.txt")).getStatus());
        assertEquals(400, assertThrows(PluginAssetException.class,
                () -> resolver.resolve("demo", "1.0.0", "..\\sibling.txt")).getStatus());
        assertEquals(400, assertThrows(PluginAssetException.class,
                () -> resolver.resolve("demo", "1.0.0", "frontend//index.mjs")).getStatus());
        assertEquals(400, assertThrows(PluginAssetException.class,
                () -> resolver.resolve("demo", "1.0.0", "")).getStatus());
    }

    @Test
    @DisplayName("后缀不在白名单内一律拒绝（含无后缀）")
    void rejectUnallowedExtension() {
        assertEquals(400, assertThrows(PluginAssetException.class,
                () -> resolver.resolve("demo", "1.0.0", "forbidden.jsp")).getStatus());
        assertEquals(400, assertThrows(PluginAssetException.class,
                () -> resolver.resolve("demo", "1.0.0", "noext")).getStatus());
        assertEquals(400, assertThrows(PluginAssetException.class,
                () -> resolver.resolve("demo", "1.0.0", "shell.sh")).getStatus());
    }

    @Test
    @DisplayName("code / version 格式非法一律拒绝（防拼接注入）")
    void rejectIllegalCodeAndVersion() {
        assertEquals(400, assertThrows(PluginAssetException.class,
                () -> resolver.resolve("../demo", "1.0.0", "index.mjs")).getStatus());
        assertEquals(400, assertThrows(PluginAssetException.class,
                () -> resolver.resolve("Demo", "1.0.0", "index.mjs")).getStatus());
        assertEquals(400, assertThrows(PluginAssetException.class,
                () -> resolver.resolve("demo", "../../etc", "index.mjs")).getStatus());
        assertEquals(400, assertThrows(PluginAssetException.class,
                () -> resolver.resolve("demo", "1.0.0/../..", "index.mjs")).getStatus());
    }

    @Test
    @DisplayName("MIME 映射覆盖脚本/样式/图片/字体/JSON")
    void contentTypeMapping() {
        assertEquals("application/javascript; charset=UTF-8", PluginAssetResolver.contentType("index.mjs"));
        assertEquals("application/javascript; charset=UTF-8", PluginAssetResolver.contentType("dist/app.js"));
        assertEquals("text/css; charset=UTF-8", PluginAssetResolver.contentType("asset/style.css"));
        assertEquals("image/png", PluginAssetResolver.contentType("asset/logo.PNG"));
        assertEquals("image/svg+xml", PluginAssetResolver.contentType("asset/icon.svg"));
        assertEquals("font/woff2", PluginAssetResolver.contentType("asset/font.woff2"));
        assertEquals("application/json; charset=UTF-8", PluginAssetResolver.contentType("asset/data.json"));
        assertEquals("application/octet-stream", PluginAssetResolver.contentType("asset/unknown.bin"));
    }

    @Test
    @DisplayName("JAR 形态下 CSS / 图片 / 字体 / JSON 可正常取到")
    void jarBinaryAssets() {
        assertEquals(".a{color:#fff}",
                new String(resolver.resolve("jarplug", "2.0.0", "asset/style.css").content(), StandardCharsets.UTF_8));
        assertArrayEquals("PNGDATA".getBytes(StandardCharsets.UTF_8),
                resolver.resolve("jarplug", "2.0.0", "asset/logo.png").content());
        assertEquals("font/woff2", resolver.resolve("jarplug", "2.0.0", "asset/font.woff2").contentType());
        assertEquals("application/json; charset=UTF-8",
                resolver.resolve("jarplug", "2.0.0", "asset/data.json").contentType());
    }

    @Test
    @DisplayName("后缀判定与扩展名提取")
    void extensionHelpers() {
        assertTrue(PluginAssetResolver.isAllowedExtension("a/b/c.mjs"));
        assertTrue(PluginAssetResolver.isAllowedExtension("WeIrD.WOFF2"));
        assertFalse(PluginAssetResolver.isAllowedExtension("a/b/c.jsp"));
        assertFalse(PluginAssetResolver.isAllowedExtension("noext"));
        assertEquals("mjs", PluginAssetResolver.extensionOf("dist/x.mjs"));
        assertEquals("", PluginAssetResolver.extensionOf("dist/x."));
        assertEquals("", PluginAssetResolver.extensionOf("dist/x"));
    }

    @Test
    @DisplayName("缓存策略：URL 带版本，产物可长期 immutable 缓存")
    void cacheControlContract() {
        assertEquals("public, max-age=31536000, immutable", PluginAssetResolver.CACHE_CONTROL);
    }

    @Test
    @DisplayName("exists 探活不抛异常，命中/未命中均返回布尔")
    void existsProbe() {
        assertTrue(resolver.exists("demo", "1.0.0", "index.mjs"));
        assertFalse(resolver.exists("demo", "1.0.0", "missing.mjs"));
    }

    private void write(String relative, String content) throws IOException {
        Path path = tempDir.resolve(relative);
        Files.createDirectories(path.getParent());
        Files.write(path, content.getBytes(StandardCharsets.UTF_8));
    }

    private void writeJar(String relative, Map<String, String> entries) throws IOException {
        Path jar = tempDir.resolve(relative);
        Files.createDirectories(jar.getParent());
        try (OutputStream out = Files.newOutputStream(jar); ZipOutputStream zip = new ZipOutputStream(out)) {
            for (Map.Entry<String, String> entry : entries.entrySet()) {
                zip.putNextEntry(new ZipEntry(entry.getKey()));
                zip.write(entry.getValue().getBytes(StandardCharsets.UTF_8));
                zip.closeEntry();
            }
        }
    }
}
