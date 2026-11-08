package com.houjiang.plugin.update.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.houjiang.plugin.engine.artifact.PluginArtifactStore;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * 版本目录 / 回滚锚点测试（M4，方案 §7 P0-8）。
 *
 * <p>这里守的是一条红线：<b>清理永远不能删掉回滚点</b>。
 * 「保留最近 N 个」听起来简单，但 N 是「最近」的，而回滚锚点往往是最旧的那个——
 * 直接用 N 截断会把锚点第一个删掉，让下一次升级失败时无路可退。
 */
class PluginVersionBackupStoreTest {

    private static final String CODE = "demo";

    @TempDir
    Path tempDir;

    private PluginArtifactStore artifactStore;

    private PluginVersionBackupStore store;

    /**
     * 装配真实文件系统仓库（不 mock 磁盘：清理逻辑的全部风险都在文件操作上）。
     */
    @BeforeEach
    void setUp() {
        artifactStore = new PluginArtifactStore(tempDir);
        store = new PluginVersionBackupStore(artifactStore);
    }

    @Test
    void 备份与恢复_版本目录内容逐字节还原() throws Exception {
        byte[] content = "jar-bytes-1.2.0".getBytes(StandardCharsets.UTF_8);
        artifactStore.store(CODE, "1.2.0", content);
        Path backup = store.backup(CODE, "1.2.0");

        assertTrue(backup != null && Files.isDirectory(backup), "备份目录应已创建");
        assertTrue(store.hasBackup(CODE, "1.2.0"));

        // 模拟升级写坏/删掉旧版本目录
        artifactStore.deleteVersion(CODE, "1.2.0");
        assertFalse(artifactStore.exists(CODE, "1.2.0"));

        assertTrue(store.restoreLatest(CODE, "1.2.0"), "必须能从备份恢复");
        org.junit.jupiter.api.Assertions.assertArrayEquals(content,
                Files.readAllBytes(artifactStore.resolve(CODE, "1.2.0")));
    }

    @Test
    void 清理不误删当前版本previous版本与被引用的版本() throws Exception {
        // 当前 1.2.0、回滚锚点 1.1.0、另有安装记录引用 1.0.0，另有 4 个废弃版本
        for (String version : List.of("0.5.0", "0.6.0", "0.7.0", "0.8.0", "1.0.0", "1.1.0", "1.2.0")) {
            artifactStore.store(CODE, version, ("jar-" + version).getBytes(StandardCharsets.UTF_8));
        }
        Set<String> anchored = Set.of("1.2.0", "1.1.0", "1.0.0");

        List<String> deleted = store.cleanup(CODE, anchored, 3, 3);

        // 三个锚点一个都不能少
        assertTrue(artifactStore.exists(CODE, "1.2.0"), "当前版本是硬锚点");
        assertTrue(artifactStore.exists(CODE, "1.1.0"), "previous_version 是回滚锚点");
        assertTrue(artifactStore.exists(CODE, "1.0.0"), "被安装记录引用的版本是锚点");
        // 非锚点按「最近 3 个」保留：0.6.0 / 0.7.0 / 0.8.0 留下，0.5.0 删掉
        assertTrue(deleted.contains("0.5.0"));
        assertEquals(1, deleted.size());
        assertTrue(artifactStore.exists(CODE, "0.8.0"));
        assertTrue(artifactStore.exists(CODE, "0.7.0"));
        assertTrue(artifactStore.exists(CODE, "0.6.0"));
        assertFalse(artifactStore.exists(CODE, "0.5.0"));
    }

    @Test
    void 锚点即使排在最旧也保留() throws Exception {
        for (String version : List.of("1.0.0", "1.1.0", "1.2.0", "1.3.0")) {
            artifactStore.store(CODE, version, ("jar-" + version).getBytes(StandardCharsets.UTF_8));
        }
        // 1.0.0 是最旧的版本，但它是回滚锚点——按 N=1 清理也不能动它
        List<String> deleted = store.cleanup(CODE, Set.of("1.0.0"), 1, 1);

        assertTrue(artifactStore.exists(CODE, "1.0.0"), "锚点优先于保留数量 N");
        assertTrue(artifactStore.exists(CODE, "1.3.0"), "最近的 1 个也要保留");
        assertFalse(deleted.contains("1.0.0"));
    }

    @Test
    void 备份点只保留最近N份() throws Exception {
        artifactStore.store(CODE, "1.0.0", "jar".getBytes(StandardCharsets.UTF_8));
        store.backup(CODE, "1.0.0");
        Thread.sleep(10);
        store.backup(CODE, "1.0.0");
        Thread.sleep(10);
        store.backup(CODE, "1.0.0");
        assertEquals(3, artifactStore.listBackups(CODE).size());

        store.cleanup(CODE, Set.of("1.0.0"), 3, 1);

        assertEquals(1, artifactStore.listBackups(CODE).size(), "超出保留份数的旧备份应被清理");
    }
}
