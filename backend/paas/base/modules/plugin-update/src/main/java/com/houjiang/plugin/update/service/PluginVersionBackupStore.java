package com.houjiang.plugin.update.service;

import com.houjiang.plugin.engine.artifact.PluginArtifactStore;
import com.houjiang.commons.version.VersionComparison;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;

/**
 * 版本目录与回滚锚点的文件系统管理（方案 §5.3「备份 / 恢复 / 清理」、§7 P0-8）。
 *
 * <p><b>目录契约</b>（与 {@link PluginArtifactStore} 共用同一个 root）：
 * <pre>
 * {root}/{code}/{version}/{code}-{version}.jar       —— 版本目录，天然并存
 * {root}/{code}/.backup-{ts}/{version}/{code}-{version}.jar  —— 升级前备份，回滚锚点
 * </pre>
 *
 * <p><b>为什么备份用「复制」而不是「移动」</b>：升级进行到一半失败时，
 * 移动过的旧版本目录已经不在原位，回滚脚本找不到东西恢复；
 * 复制则保证「旧版本一直在原地，直到新版本确认可用」。
 * 代价是多占一份磁盘，64MB 的 jar 完全可接受。
 *
 * <p><b>清理的红线（P0-8）</b>：{@link #cleanup} 只删<b>既非当前版本、也非回滚锚点、
 * 又不在最近 N 个之内</b>的版本目录。{@code previous_version} 与所有安装记录引用到的版本
 * 是硬锚点，哪怕已经排到 N 之外也绝不删——把回滚点删掉的「清理」比不清理更糟，
 * 因为它会让下一次升级失败时无路可退。
 *
 * @author Amos
 * @since M4
 */
@Slf4j
public class PluginVersionBackupStore {

    /**
     * 备份目录名时间戳格式（目录名按字典序即时间序，便于「取最新备份」）。
     */
    private static final DateTimeFormatter BACKUP_TS = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /**
     * 备份目录前缀（以 {@code .} 开头，因此不会被 {@link PluginArtifactStore#listVersions} 当成版本）。
     */
    private static final String BACKUP_PREFIX = ".backup-";

    private final PluginArtifactStore artifactStore;

    /**
     * 构造版本备份仓库。
     *
     * @param artifactStore 制品落盘仓库（提供 root 与版本解析）
     */
    public PluginVersionBackupStore(PluginArtifactStore artifactStore) {
        this.artifactStore = java.util.Objects.requireNonNull(artifactStore, "PluginArtifactStore 不能为空");
    }

    /**
     * 备份当前版本目录到 {@code .backup-{ts}/{version}/}。
     *
     * <p>版本目录不存在时返回 {@code null}（全新安装场景无旧版本可备份，不是错误）。
     *
     * @param pluginCode 插件编码
     * @param version    要备份的版本
     * @return 备份目录绝对路径，无内容可备份时返回 null
     * @throws IOException 复制失败（Windows 句柄未释放时抛 AccessDeniedException）
     */
    public Path backup(String pluginCode, String version) throws IOException {
        Path source = artifactStore.resolve(pluginCode, version).getParent();
        if (source == null || !Files.isDirectory(source)) {
            log.info("无版本目录可备份，跳过: {}/{}", pluginCode, version);
            return null;
        }
        String stamp = LocalDateTime.now().format(BACKUP_TS) + "-" + (System.nanoTime() % 100000L);
        Path backupDir = artifactStore.pluginsRoot().resolve(pluginCode).resolve(BACKUP_PREFIX + stamp);
        Path target = backupDir.resolve(version);
        Files.createDirectories(target.getParent());
        copyDirectory(source, target);
        log.info("已备份版本目录: {}/{} -> {}", pluginCode, version, target);
        return backupDir;
    }

    /**
     * 从最新一份包含该版本的备份恢复到 {@code {root}/{code}/{version}/}。
     *
     * <p>目标目录已存在时按「覆盖同名文件」恢复——升级失败后残留的可能是一个写了一半的目录，
     * 以备份为准覆盖它比保留半包更安全。
     *
     * @param pluginCode 插件编码
     * @param version    要恢复的版本
     * @return true 表示找到了备份并已恢复
     */
    public boolean restoreLatest(String pluginCode, String version) {
        List<String> backups = new ArrayList<>(artifactStore.listBackups(pluginCode));
        backups.sort(Comparator.reverseOrder());
        for (String backup : backups) {
            Path source = artifactStore.pluginsRoot().resolve(pluginCode).resolve(backup).resolve(version);
            if (!Files.isDirectory(source)) {
                continue;
            }
            Path target = artifactStore.resolve(pluginCode, version).getParent();
            try {
                Files.createDirectories(target);
                copyDirectory(source, target);
                log.info("已从备份恢复版本目录: {}/{} <- {}", pluginCode, version, source);
                return true;
            } catch (IOException e) {
                log.error("从备份恢复失败: {}/{} (备份 {}) : {}", pluginCode, version, backup, e.getMessage());
                return false;
            }
        }
        log.warn("未找到可用备份: {}/{}（备份数={}）", pluginCode, version, backups.size());
        return false;
    }

    /**
     * 判断是否存在可用于回滚的备份点。
     *
     * @param pluginCode 插件编码
     * @param version    目标版本
     * @return true 表示存在
     */
    public boolean hasBackup(String pluginCode, String version) {
        for (String backup : artifactStore.listBackups(pluginCode)) {
            Path dir = artifactStore.pluginsRoot().resolve(pluginCode).resolve(backup).resolve(version);
            if (Files.isDirectory(dir)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 删除备份点（回滚成功后调用；失败时保留，供人工介入）。
     *
     * @param pluginCode 插件编码
     * @param backupDir  备份目录
     */
    public void deleteBackup(String pluginCode, Path backupDir) {
        if (backupDir == null) {
            return;
        }
        try (Stream<Path> paths = Files.walk(backupDir)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException e) {
                    log.warn("删除备份失败：{}（{}）", path, e.getMessage());
                }
            });
        } catch (IOException e) {
            log.warn("遍历备份目录失败：{}（{}）", backupDir, e.getMessage());
        }
    }

    /**
     * 版本目录清理：保留「当前版本 + 回滚锚点 + 最近 N 个版本」，其余删除。
     *
     * <p><b>锚点优先于 N</b>：即使锚点排在最旧的位置也保留。
     * 删除失败只告警不抛出——清理是优化不是关键路径，
     * 因为清理失败而把一次成功的升级判成失败是完全不划算的。
     *
     * @param pluginCode    插件编码
     * @param anchored      必须保留的版本集合（当前版本 + previous_version + 安装记录引用的版本）
     * @param retainVersions 除锚点外额外保留的版本数
     * @param retainBackups 保留的备份点数量（超出按时间从旧到新删）
     * @return 实际删除的版本目录名
     */
    public List<String> cleanup(String pluginCode, Set<String> anchored,
                                int retainVersions, int retainBackups) {
        Set<String> keep = new LinkedHashSet<>();
        if (anchored != null) {
            for (String version : anchored) {
                if (version != null && !version.isBlank()) {
                    keep.add(version.trim());
                }
            }
        }
        List<String> versions = new ArrayList<>(artifactStore.listVersions(pluginCode));
        // 新版本在前：保留「最近的 N 个」，而不是字典序的前 N 个
        versions.sort(Comparator.comparing((String v) -> v, VersionComparison::compare).reversed());

        // 关键：锚点不占 N 的名额。若先按「最近 N 个」取再并上锚点，
        // 而锚点恰好是较旧的版本（升级后 previous_version 必然比当前版本旧），
        // N 个名额会被锚点自己吃满，真正的历史版本一个都留不下，
        // 于是「保留 N 个」退化成「只留锚点」——既违背了保留策略，也让 N 失去意义。
        int remaining = Math.max(0, retainVersions);
        Set<String> keepByRecency = new LinkedHashSet<>();
        for (String version : versions) {
            if (keep.contains(version)) {
                continue;
            }
            if (remaining <= 0) {
                break;
            }
            keepByRecency.add(version);
            remaining--;
        }

        List<String> deleted = new ArrayList<>();
        for (String version : versions) {
            if (keep.contains(version) || keepByRecency.contains(version)) {
                continue;
            }
            try {
                if (artifactStore.deleteVersion(pluginCode, version)) {
                    deleted.add(version);
                }
            } catch (IOException e) {
                log.warn("清理版本目录失败，已跳过: {}/{} : {}", pluginCode, version, e.getMessage());
            }
        }
        pruneBackups(pluginCode, Math.max(1, retainBackups));
        if (!deleted.isEmpty()) {
            log.info("版本目录清理完成: {} 已删除 {}，保留 {}", pluginCode, deleted, versions.size() - deleted.size());
        }
        return deleted;
    }

    /**
     * 只保留最近 {@code retain} 份备份（目录名含时间戳，倒序即最新在前）。
     *
     * @param pluginCode 插件编码
     * @param retain     保留份数
     */
    private void pruneBackups(String pluginCode, int retain) {
        List<String> backups = new ArrayList<>(artifactStore.listBackups(pluginCode));
        if (backups.size() <= retain) {
            return;
        }
        backups.sort(Comparator.reverseOrder());
        for (String backup : backups.subList(retain, backups.size())) {
            deleteBackup(pluginCode, artifactStore.pluginsRoot().resolve(pluginCode).resolve(backup));
        }
    }

    /**
     * 递归复制目录（覆盖同名文件）。
     *
     * @param source 源目录
     * @param target 目标目录
     * @throws IOException 复制失败
     */
    private static void copyDirectory(Path source, Path target) throws IOException {
        List<Path> sources;
        try (Stream<Path> walk = Files.walk(source)) {
            sources = walk.toList();
        }
        for (Path path : sources) {
            Path destination = target.resolve(source.relativize(path).toString());
            if (Files.isDirectory(path)) {
                Files.createDirectories(destination);
            } else {
                Files.createDirectories(destination.getParent());
                Files.copy(path, destination, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }
}
