package com.houjiang.plugin.engine.artifact;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 安装产物落盘仓库单测：验证「市场安装 → 引擎装载」共用的目录契约。
 *
 * @author HouCloud
 */
class PluginArtifactStoreTest {

    @TempDir
    Path tempDir;

    @Test
    void resolveFollowsDirectoryContract() {
        PluginArtifactStore store = new PluginArtifactStore(tempDir);

        Path target = store.resolve("mall-live", "1.0.0");

        assertTrue(target.startsWith(tempDir.toAbsolutePath().normalize()), "必须落在 pluginsRoot 下");
        assertTrue(target.toString().replace('\\', '/').endsWith("mall-live/1.0.0/mall-live-1.0.0.jar"),
                "目录契约：{root}/{code}/{version}/{code}-{version}.jar，实际=" + target);
    }

    @Test
    void storeWritesBytesAndCreatesParentDirectories() throws IOException {
        PluginArtifactStore store = new PluginArtifactStore(tempDir);
        byte[] content = "plugin-jar-bytes".getBytes();

        Path stored = store.store("mall-live", "1.0.0", content);

        assertTrue(Files.isRegularFile(stored));
        assertArrayEquals(content, Files.readAllBytes(stored));
        assertTrue(store.exists("mall-live", "1.0.0"));
        assertFalse(Files.exists(stored.resolveSibling(stored.getFileName() + ".tmp")),
                "临时文件必须已原子改名为正式文件");
    }

    @Test
    void storeRejectsEmptyContent() {
        PluginArtifactStore store = new PluginArtifactStore(tempDir);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> store.store("mall-live", "1.0.0", new byte[0]));

        assertEquals("安装包内容为空，拒绝落盘", ex.getMessage());
    }

    @Test
    void resolveRejectsBlankCode() {
        PluginArtifactStore store = new PluginArtifactStore(tempDir);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> store.resolve(" ", "1.0.0"));

        assertEquals("插件编码不能为空", ex.getMessage());
    }

    @Test
    void deleteRemovesAllVersionArtifactsOfPlugin() throws IOException {
        PluginArtifactStore store = new PluginArtifactStore(tempDir);
        store.store("mall-risk", "1.0.0", "v1".getBytes());
        store.store("mall-risk", "1.1.0", "v2".getBytes());
        store.store("mall-points", "1.0.0", "keep".getBytes());

        store.delete("mall-risk");

        assertFalse(Files.exists(tempDir.resolve("mall-risk")), "整目录应被删除");
        assertTrue(store.exists("mall-points", "1.0.0"), "其他插件不受影响");
    }

    // ------------------------------------------------------------------
    // M4：版本级删除（方案 §7 P0-8）
    // ------------------------------------------------------------------

    @Test
    void deleteVersionRemovesOnlyThatVersion() throws IOException {
        PluginArtifactStore store = new PluginArtifactStore(tempDir);
        store.store("demo", "1.0.0", "v1".getBytes());
        store.store("demo", "1.1.0", "v2".getBytes());
        store.store("demo", "2.0.0", "v3".getBytes());

        assertTrue(store.deleteVersion("demo", "1.0.0"));

        assertFalse(store.exists("demo", "1.0.0"), "目标版本应被删除");
        assertTrue(store.exists("demo", "1.1.0"), "其他版本必须保留（历史版本是回滚锚点）");
        assertTrue(store.exists("demo", "2.0.0"));
    }

    @Test
    void deleteVersionRefusesPathTraversalLikeVersion() throws IOException {
        PluginArtifactStore store = new PluginArtifactStore(tempDir);
        store.store("demo", "1.0.0", "v1".getBytes());

        assertThrows(IllegalArgumentException.class, () -> store.deleteVersion("demo", "../other"));
        assertThrows(IllegalArgumentException.class, () -> store.deleteVersion("demo", ".backup-20260101"));
    }

    @Test
    void listVersionsExcludesBackupDirectories() throws IOException {
        PluginArtifactStore store = new PluginArtifactStore(tempDir);
        store.store("demo", "1.0.0", "v1".getBytes());
        Files.createDirectories(tempDir.resolve("demo").resolve(".backup-20260101010100-1"));

        assertEquals(java.util.List.of("1.0.0"), store.listVersions("demo"),
                "备份点不是可安装版本，绝不能混进版本列表被清理逻辑删掉");
        assertEquals(java.util.List.of(".backup-20260101010100-1"), store.listBackups("demo"));
    }
}
