package com.houjiang.plugin.artifact.local;

import com.houjiang.plugin.artifact.config.ArtifactProperties;
import com.houjiang.plugin.artifact.exception.ArtifactConflictException;
import com.houjiang.plugin.artifact.exception.ArtifactIntegrityException;
import com.houjiang.plugin.artifact.exception.ArtifactNotFoundException;
import com.houjiang.plugin.artifact.key.ArtifactKeys;
import com.houjiang.plugin.artifact.model.ArtifactStream;
import com.houjiang.plugin.artifact.model.StoredObject;
import com.houjiang.plugin.artifact.spi.ArtifactStorage;
import com.houjiang.plugin.artifact.util.ArtifactDigests;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 本地磁盘制品存储实现（P0 默认）。
 *
 * <p>三条实现底线：
 * <ol>
 *   <li><b>先写 {@code {key}.tmp-{uuid}}，写完 {@code Files.move(..., ATOMIC_MOVE)} 落位</b>。
 *       上传中断只会留下临时文件，不会在正式 key 上留下半截制品被后续下载读到。
 *       注意：{@code ATOMIC_MOVE} 不能与 {@code REPLACE_EXISTING} 同用，
 *       这正是「绝不覆盖」在文件系统层面的保证。</li>
 *   <li><b>目标已存在直接抛 {@link ArtifactConflictException}</b>，不覆盖、不静默成功。</li>
 *   <li><b>写入过程中一次读流同时算 md5 + sha256 + 字节数</b>，完成后与 declaredSize 校验。</li>
 * </ol>
 *
 * <p>切换 {@code houcloud.artifact.type=minio|cos} 时由对应实现接管；本类通过
 * {@code @ConditionalOnProperty(..., havingValue = "local", matchIfMissing = true)}
 * 仅在本地模式启用。
 *
 * @author Amos
 * @since M0
 */
@Component
@ConditionalOnProperty(name = "houcloud.artifact.type", havingValue = "local", matchIfMissing = true)
public class LocalDiskArtifactStorage implements ArtifactStorage {

    /**
     * 存储类型标识，写入 {@code hou_plugin_artifact.storage_type}。
     */
    private static final String STORAGE_TYPE = "local";

    /**
     * Windows 下的默认根目录（生产机为 Windows + D 盘数据盘）。
     */
    private static final String WINDOWS_DEFAULT_ROOT = "D:/wwwroot/houcloud/artifacts";

    /**
     * 制品配置。
     */
    private final ArtifactProperties properties;

    /**
     * 解析后的存储根目录（绝对路径）。
     */
    private final Path root;

    /**
     * 构造本地磁盘存储。
     *
     * @param properties 制品配置，{@code root} 为空时按操作系统取默认值
     */
    public LocalDiskArtifactStorage(ArtifactProperties properties) {
        this.properties = Objects.requireNonNull(properties, "ArtifactProperties 不能为空");
        this.root = Path.of(resolveRoot(properties.getRoot())).toAbsolutePath().normalize();
    }

    @Override
    public StoredObject putIfAbsent(String key, InputStream in, long declaredSize) throws IOException {
        Objects.requireNonNull(in, "制品输入流不能为空");
        ArtifactKeys.validate(key);
        Path target = resolve(key);
        if (Files.exists(target)) {
            // 已 published 的制品绝不覆盖，否则会出现「同版本号两个 jar」的不可逆不一致
            throw new ArtifactConflictException(key);
        }
        Files.createDirectories(target.getParent());

        Path tmpFile = target.resolveSibling(target.getFileName() + ".tmp-" + UUID.randomUUID());
        MessageDigest md5 = ArtifactDigests.newDigest("MD5");
        MessageDigest sha256 = ArtifactDigests.newDigest("SHA-256");
        long size;
        try (OutputStream out = Files.newOutputStream(tmpFile,
                StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            size = ArtifactDigests.drain(in, md5, sha256, out::write);
        } catch (IOException e) {
            deleteQuietly(tmpFile);
            throw e;
        }

        if (declaredSize > 0 && size != declaredSize) {
            deleteQuietly(tmpFile);
            throw new ArtifactIntegrityException(
                    "制品大小与声明不一致：key=" + key + "，declared=" + declaredSize + "，actual=" + size);
        }

        try {
            Files.move(tmpFile, target, StandardCopyOption.ATOMIC_MOVE);
        } catch (FileAlreadyExistsException e) {
            // 并发上传同一 key 时的竞态：后来者判负
            deleteQuietly(tmpFile);
            throw new ArtifactConflictException(key, "并发写入，已有其它请求先落位");
        } catch (AtomicMoveNotSupportedException e) {
            // 跨文件系统/特殊文件系统不支持原子移动时退化，先确认目标仍不存在再落位
            if (Files.exists(target)) {
                deleteQuietly(tmpFile);
                throw new ArtifactConflictException(key);
            }
            Files.move(tmpFile, target);
        }

        // digest() 已完成增量计算，这里只做十六进制编码；若再套 md5Hex/sha256Hex 会变成二次哈希
        return new StoredObject(key, ArtifactDigests.toHexString(md5.digest()),
                ArtifactDigests.toHexString(sha256.digest()), size, STORAGE_TYPE);
    }

    @Override
    public ArtifactStream open(String key, Long rangeStart, Long rangeEnd) throws IOException {
        ArtifactKeys.validate(key);
        Path target = resolve(key);
        if (!Files.isRegularFile(target)) {
            throw new ArtifactNotFoundException(key);
        }
        long totalSize = Files.size(target);
        long start = rangeStart == null ? 0L : Math.max(0L, rangeStart);
        long end = rangeEnd == null ? totalSize - 1 : Math.min(rangeEnd, totalSize - 1);
        if (start > end || start >= totalSize) {
            // 调用方（下载端点）应捕获并响应 HTTP 416 Range Not Satisfiable
            throw new IllegalArgumentException(
                    "Range 区间非法：key=" + key + "，start=" + start + "，end=" + end + "，total=" + totalSize);
        }
        InputStream stream = Files.newInputStream(target, StandardOpenOption.READ);
        if (start > 0) {
            long skipped = stream.skip(start);
            if (skipped != start) {
                stream.close();
                throw new IOException("跳过字节数不足：期望 " + start + "，实际 " + skipped);
            }
        }
        long length = end - start + 1;
        InputStream limited = new BoundedInputStream(stream, length);
        return new ArtifactStream(limited, length, totalSize, start, end, null);
    }

    @Override
    public void deleteVersion(String pluginCode, String version) {
        Path dir = resolve(ArtifactKeys.versionDir(pluginCode, version));
        if (!Files.exists(dir)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(dir)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException e) {
                    throw new ArtifactIntegrityException("删除制品目录失败：path=" + path, e);
                }
            });
        } catch (IOException e) {
            throw new ArtifactIntegrityException("遍历制品目录失败：dir=" + dir, e);
        }
    }

    @Override
    public void copyVersion(String fromKey, String toKey) {
        ArtifactKeys.validate(fromKey);
        ArtifactKeys.validate(toKey);
        Path source = resolve(fromKey);
        Path target = resolve(toKey);
        if (!Files.isRegularFile(source)) {
            throw new ArtifactNotFoundException(fromKey);
        }
        if (Files.exists(target)) {
            throw new ArtifactConflictException(toKey);
        }
        try {
            Files.createDirectories(target.getParent());
            Files.copy(source, target, StandardCopyOption.COPY_ATTRIBUTES);
        } catch (IOException e) {
            throw new ArtifactIntegrityException(
                    "复制制品失败：from=" + fromKey + "，to=" + toKey, e);
        }
    }

    @Override
    public String signedInternalUrl(String key, Duration ttl) {
        // 本地磁盘没有独立下载端点，必须经本进程 /base/plugin-center/artifact/raw 回源流式输出；
        // 该方法仅在云存储（COS/OSS 预签名 URL）实现中才有意义。
        throw new UnsupportedOperationException(
                "本地磁盘存储不支持签发内部直链，请经 /base/plugin-center/artifact/raw 回源下载：key=" + key);
    }

    /**
     * @return 解析后的存储根目录绝对路径
     */
    public Path getRoot() {
        return root;
    }

    /**
     * 把存储键解析为本地绝对路径，并校验解析结果仍在根目录内（二次防目录穿越）。
     *
     * @param key 存储键
     * @return 绝对路径
     */
    private Path resolve(String key) {
        Path resolved = root.resolve(key).normalize();
        if (!resolved.startsWith(root)) {
            throw new IllegalArgumentException("制品存储键越出了存储根目录：key=" + key);
        }
        return resolved;
    }

    /**
     * 确定存储根目录：配置优先，未配置时按操作系统取默认值。
     *
     * @param configuredRoot 配置值，允许为空或空白
     * @return 根目录字符串
     */
    private static String resolveRoot(String configuredRoot) {
        if (configuredRoot != null && !configuredRoot.isBlank()) {
            return configuredRoot.trim();
        }
        String osName = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (osName.contains("win")) {
            return WINDOWS_DEFAULT_ROOT;
        }
        return System.getProperty("user.home", ".") + "/houcloud/artifacts";
    }

    private static void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
            // 清理临时文件失败不影响主流程，仅留下待清理的 .tmp-* 文件
        }
    }

    /**
     * 限制读取长度的装饰流，用于精确返回 Range 区间字节。
     */
    private static final class BoundedInputStream extends InputStream {

        private final InputStream delegate;

        private long remaining;

        private BoundedInputStream(InputStream delegate, long length) {
            this.delegate = delegate;
            this.remaining = length;
        }

        @Override
        public int read() throws IOException {
            if (remaining <= 0) {
                return -1;
            }
            int value = delegate.read();
            if (value >= 0) {
                remaining--;
            }
            return value;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            if (remaining <= 0) {
                return -1;
            }
            int toRead = (int) Math.min(len, remaining);
            int read = delegate.read(b, off, toRead);
            if (read > 0) {
                remaining -= read;
            }
            return read;
        }

        @Override
        public long skip(long n) throws IOException {
            long skipped = delegate.skip(Math.min(n, remaining));
            remaining -= skipped;
            return skipped;
        }

        @Override
        public int available() throws IOException {
            return (int) Math.min(delegate.available(), remaining);
        }

        @Override
        public void close() throws IOException {
            delegate.close();
        }
    }
}
