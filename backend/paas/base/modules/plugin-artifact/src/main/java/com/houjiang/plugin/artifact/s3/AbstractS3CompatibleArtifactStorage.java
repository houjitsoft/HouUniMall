package com.houjiang.plugin.artifact.s3;

import com.houjiang.plugin.artifact.exception.ArtifactConflictException;
import com.houjiang.plugin.artifact.exception.ArtifactIntegrityException;
import com.houjiang.plugin.artifact.exception.ArtifactNotFoundException;
import com.houjiang.plugin.artifact.exception.ArtifactStorageException;
import com.houjiang.plugin.artifact.key.ArtifactKeys;
import com.houjiang.plugin.artifact.model.ArtifactStream;
import com.houjiang.plugin.artifact.model.StoredObject;
import com.houjiang.plugin.artifact.spi.ArtifactStorage;
import com.houjiang.plugin.artifact.util.ArtifactDigests;
import io.minio.CopyObjectArgs;
import io.minio.CopySource;
import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.Result;
import io.minio.StatObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.http.Method;
import io.minio.messages.Item;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * S3 兼容对象存储（自建 MinIO / 腾讯云 COS / 阿里云 OSS S3 网关）公共实现。
 *
 * <p>Console 端口不可用；必须使用 S3 API endpoint。
 */
public abstract class AbstractS3CompatibleArtifactStorage implements ArtifactStorage {

    private final MinioClient client;
    private final String bucket;
    private final String keyPrefix;
    private final String storageType;

    protected AbstractS3CompatibleArtifactStorage(S3EndpointSettings settings, String storageType) {
        Objects.requireNonNull(settings, "S3 配置不能为空");
        this.storageType = Objects.requireNonNull(storageType, "storageType");
        if (settings.endpoint() == null || settings.endpoint().isBlank()) {
            throw new IllegalStateException(
                    "houcloud.artifact.type=" + storageType + " 时必须配置 S3 API endpoint（不是 Console）");
        }
        if (settings.accessKey() == null || settings.accessKey().isBlank()
                || settings.secretKey() == null || settings.secretKey().isBlank()) {
            throw new IllegalStateException(
                    "houcloud.artifact.type=" + storageType + " 时必须配置 access-key / secret-key（环境变量注入，禁止写入 git）");
        }
        this.bucket = settings.bucket() == null || settings.bucket().isBlank() ? "unimall" : settings.bucket();
        this.keyPrefix = settings.keyPrefix() == null ? "" : settings.keyPrefix();
        this.client = buildClient(settings);
    }

    /** 单测注入。 */
    protected AbstractS3CompatibleArtifactStorage(S3EndpointSettings settings, String storageType, MinioClient client) {
        this.storageType = storageType;
        this.client = Objects.requireNonNull(client, "client");
        this.bucket = settings.bucket() == null || settings.bucket().isBlank() ? "unimall" : settings.bucket();
        this.keyPrefix = settings.keyPrefix() == null ? "" : settings.keyPrefix();
    }

    @Override
    public StoredObject putIfAbsent(String key, InputStream in, long declaredSize) throws IOException {
        Objects.requireNonNull(in, "制品输入流不能为空");
        ArtifactKeys.validate(key);
        String object = objectName(key);
        if (exists(object)) {
            throw new ArtifactConflictException(key);
        }

        Path tmpFile = Files.createTempFile("unimall-artifact-", ".tmp");
        MessageDigest md5 = ArtifactDigests.newDigest("MD5");
        MessageDigest sha256 = ArtifactDigests.newDigest("SHA-256");
        long size;
        try (OutputStream out = Files.newOutputStream(tmpFile,
                StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
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

        String tmpObject = object + ".tmp-" + UUID.randomUUID();
        try {
            try (InputStream upload = Files.newInputStream(tmpFile)) {
                client.putObject(PutObjectArgs.builder()
                        .bucket(bucket)
                        .object(tmpObject)
                        .stream(upload, size, -1)
                        .contentType("application/octet-stream")
                        .build());
            }
            if (exists(object)) {
                removeQuietly(tmpObject);
                throw new ArtifactConflictException(key, "并发写入，已有其它请求先落位");
            }
            Map<String, String> headers = new HashMap<>();
            headers.put("If-None-Match", "*");
            try {
                client.copyObject(CopyObjectArgs.builder()
                        .bucket(bucket)
                        .object(object)
                        .source(CopySource.builder().bucket(bucket).object(tmpObject).build())
                        .headers(headers)
                        .build());
            } catch (ErrorResponseException e) {
                removeQuietly(tmpObject);
                if (isPreconditionFailed(e) || exists(object)) {
                    throw new ArtifactConflictException(key, "并发写入，已有其它请求先落位");
                }
                client.copyObject(CopyObjectArgs.builder()
                        .bucket(bucket)
                        .object(object)
                        .source(CopySource.builder().bucket(bucket).object(tmpObject).build())
                        .build());
            }
            removeQuietly(tmpObject);
        } catch (ArtifactConflictException e) {
            throw e;
        } catch (Exception e) {
            removeQuietly(tmpObject);
            if (exists(object)) {
                throw new ArtifactConflictException(key, "并发写入，已有其它请求先落位");
            }
            throw new ArtifactStorageException(storageType + " 上传失败：key=" + key + "，" + e.getMessage(), e);
        } finally {
            deleteQuietly(tmpFile);
        }

        return new StoredObject(key, ArtifactDigests.toHexString(md5.digest()),
                ArtifactDigests.toHexString(sha256.digest()), size, storageType);
    }

    @Override
    public ArtifactStream open(String key, Long rangeStart, Long rangeEnd) throws IOException {
        ArtifactKeys.validate(key);
        String object = objectName(key);
        long totalSize;
        try {
            totalSize = client.statObject(StatObjectArgs.builder().bucket(bucket).object(object).build()).size();
        } catch (ErrorResponseException e) {
            if (isNotFound(e)) {
                throw new ArtifactNotFoundException(key);
            }
            throw new ArtifactStorageException(storageType + " stat 失败：key=" + key + "，" + e.getMessage(), e);
        } catch (Exception e) {
            throw new ArtifactStorageException(storageType + " stat 失败：key=" + key + "，" + e.getMessage(), e);
        }

        long start = rangeStart == null ? 0L : Math.max(0L, rangeStart);
        long end = rangeEnd == null ? totalSize - 1 : Math.min(rangeEnd, totalSize - 1);
        if (totalSize <= 0 || start > end || start >= totalSize) {
            throw new IllegalArgumentException(
                    "Range 区间非法：key=" + key + "，start=" + start + "，end=" + end + "，total=" + totalSize);
        }
        long length = end - start + 1;
        try {
            InputStream stream = client.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(object)
                    .offset(start)
                    .length(length)
                    .build());
            return new ArtifactStream(stream, length, totalSize, start, end, null);
        } catch (ErrorResponseException e) {
            if (isNotFound(e)) {
                throw new ArtifactNotFoundException(key);
            }
            throw new ArtifactStorageException(storageType + " 打开失败：key=" + key + "，" + e.getMessage(), e);
        } catch (Exception e) {
            throw new ArtifactStorageException(storageType + " 打开失败：key=" + key + "，" + e.getMessage(), e);
        }
    }

    @Override
    public void deleteVersion(String pluginCode, String version) {
        String prefix = objectName(ArtifactKeys.versionDir(pluginCode, version) + "/");
        try {
            removePrefix(prefix);
            for (String commercial : new String[] {
                    ArtifactKeys.COMMERCIAL_RUNTIME_PREFIX, ArtifactKeys.COMMERCIAL_SOURCE_PREFIX}) {
                removePrefix(objectName(commercial + ArtifactKeys.versionDir(pluginCode, version) + "/"));
            }
        } catch (Exception e) {
            throw new ArtifactIntegrityException(
                    "删除 " + storageType + " 制品目录失败：prefix=" + prefix + "，" + e.getMessage(), e);
        }
    }

    @Override
    public void copyVersion(String fromKey, String toKey) {
        ArtifactKeys.validate(fromKey);
        ArtifactKeys.validate(toKey);
        String from = objectName(fromKey);
        String to = objectName(toKey);
        if (!exists(from)) {
            throw new ArtifactNotFoundException(fromKey);
        }
        if (exists(to)) {
            throw new ArtifactConflictException(toKey);
        }
        try {
            client.copyObject(CopyObjectArgs.builder()
                    .bucket(bucket)
                    .object(to)
                    .source(CopySource.builder().bucket(bucket).object(from).build())
                    .build());
        } catch (Exception e) {
            throw new ArtifactIntegrityException(
                    "复制 " + storageType + " 制品失败：from=" + fromKey + "，to=" + toKey + "，" + e.getMessage(), e);
        }
    }

    @Override
    public String signedInternalUrl(String key, Duration ttl) {
        ArtifactKeys.validate(key);
        Objects.requireNonNull(ttl, "ttl 不能为空");
        long seconds = Math.max(1L, ttl.getSeconds());
        try {
            return client.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucket)
                    .object(objectName(key))
                    .expiry((int) Math.min(seconds, TimeUnit.DAYS.toSeconds(7)))
                    .build());
        } catch (Exception e) {
            throw new ArtifactStorageException(
                    "签发 " + storageType + " 预签名 URL 失败：key=" + key + "，" + e.getMessage(), e);
        }
    }

    String objectName(String key) {
        return keyPrefix + key;
    }

    private void removePrefix(String prefix) throws Exception {
        Iterable<Result<Item>> results = client.listObjects(ListObjectsArgs.builder()
                .bucket(bucket)
                .prefix(prefix)
                .recursive(true)
                .build());
        for (Result<Item> result : results) {
            Item item = result.get();
            if (item == null || item.isDir()) {
                continue;
            }
            client.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(item.objectName()).build());
        }
    }

    private boolean exists(String object) {
        try {
            client.statObject(StatObjectArgs.builder().bucket(bucket).object(object).build());
            return true;
        } catch (ErrorResponseException e) {
            if (isNotFound(e)) {
                return false;
            }
            throw new ArtifactStorageException(storageType + " stat 失败：object=" + object + "，" + e.getMessage(), e);
        } catch (Exception e) {
            throw new ArtifactStorageException(storageType + " stat 失败：object=" + object + "，" + e.getMessage(), e);
        }
    }

    private void removeQuietly(String object) {
        try {
            client.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(object).build());
        } catch (Exception ignored) {
            // 清理临时对象失败不影响主流程
        }
    }

    private static void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
            // ignore
        }
    }

    private static boolean isNotFound(ErrorResponseException e) {
        String code = e.errorResponse() == null ? "" : e.errorResponse().code();
        return "NoSuchKey".equals(code) || "NoSuchObject".equals(code) || "NotFound".equals(code);
    }

    private static boolean isPreconditionFailed(ErrorResponseException e) {
        String code = e.errorResponse() == null ? "" : e.errorResponse().code();
        return "PreconditionFailed".equals(code);
    }

    private static MinioClient buildClient(S3EndpointSettings config) {
        String raw = config.endpoint().trim();
        if (!raw.contains("://")) {
            raw = "http://" + raw;
        }
        URI uri = URI.create(raw);
        boolean secure = config.secure() != null
                ? Boolean.TRUE.equals(config.secure())
                : "https".equalsIgnoreCase(uri.getScheme());
        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            throw new IllegalStateException("非法 S3 endpoint：" + config.endpoint());
        }
        int port = uri.getPort() > 0 ? uri.getPort() : (secure ? 443 : 80);
        MinioClient.Builder builder = MinioClient.builder()
                .endpoint(host, port, secure)
                .credentials(config.accessKey(), config.secretKey());
        if (config.region() != null && !config.region().isBlank()) {
            builder.region(config.region());
        }
        return builder.build();
    }

    /**
     * S3 兼容端点配置快照。
     */
    public record S3EndpointSettings(
            String endpoint,
            String accessKey,
            String secretKey,
            String bucket,
            String keyPrefix,
            Boolean secure,
            String region) {
    }
}
