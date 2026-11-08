package com.houjiang.plugin.artifact.minio;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.houjiang.plugin.artifact.config.ArtifactProperties;
import com.houjiang.plugin.artifact.key.ArtifactKeys;
import com.houjiang.plugin.artifact.model.ArtifactStream;
import com.houjiang.plugin.artifact.model.StoredObject;
import com.houjiang.plugin.artifact.util.ArtifactDigests;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

/**
 * 真实 MinIO 联调（需环境变量）。默认跳过，避免 CI 无凭据失败。
 *
 * <pre>
 *   set HOU_ARTIFACT_MINIO_ENDPOINT=http://127.0.0.1:19000
 *   set HOU_ARTIFACT_MINIO_ACCESS_KEY=...
 *   set HOU_ARTIFACT_MINIO_SECRET_KEY=...
 *   set HOU_ARTIFACT_MINIO_BUCKET=unimall-plugins
 *   set HOU_ARTIFACT_MINIO_IT=true
 * </pre>
 */
class MinioArtifactStorageIT {

    @Test
    void putOpenRangeRoundTrip() throws Exception {
        assumeTrue("true".equalsIgnoreCase(System.getenv("HOU_ARTIFACT_MINIO_IT")));
        assumeTrue(hasText(System.getenv("HOU_ARTIFACT_MINIO_ENDPOINT")));
        assumeTrue(hasText(System.getenv("HOU_ARTIFACT_MINIO_ACCESS_KEY")));
        assumeTrue(hasText(System.getenv("HOU_ARTIFACT_MINIO_SECRET_KEY")));

        ArtifactProperties properties = new ArtifactProperties();
        properties.setType("minio");
        ArtifactProperties.Minio minio = properties.getMinio();
        minio.setEndpoint(System.getenv("HOU_ARTIFACT_MINIO_ENDPOINT"));
        minio.setAccessKey(System.getenv("HOU_ARTIFACT_MINIO_ACCESS_KEY"));
        minio.setSecretKey(System.getenv("HOU_ARTIFACT_MINIO_SECRET_KEY"));
        minio.setBucket(envOr("HOU_ARTIFACT_MINIO_BUCKET", "unimall-plugins"));
        minio.setKeyPrefix(envOr("HOU_ARTIFACT_MINIO_KEY_PREFIX", ""));

        MinioArtifactStorage storage = new MinioArtifactStorage(properties);
        byte[] payload = ("minio-it-" + System.currentTimeMillis()).getBytes(StandardCharsets.UTF_8);
        String key = ArtifactKeys.commercialRuntimeKey("erp", "9.9.9");
        try {
            StoredObject stored = storage.putIfAbsent(key, new ByteArrayInputStream(payload), payload.length);
            assertEquals(payload.length, stored.getSizeBytes());
            assertEquals(ArtifactDigests.md5Hex(payload), stored.getMd5());
            assertEquals("minio", stored.getStorageType());

            try (ArtifactStream full = storage.open(key, null, null)) {
                assertArrayEquals(payload, full.getStream().readAllBytes());
            }
            try (ArtifactStream ranged = storage.open(key, 1L, (long) payload.length - 2)) {
                byte[] slice = ranged.getStream().readAllBytes();
                assertEquals(payload.length - 2, slice.length);
                assertArrayEquals(java.util.Arrays.copyOfRange(payload, 1, payload.length - 1), slice);
            }
            String url = storage.signedInternalUrl(key, java.time.Duration.ofMinutes(5));
            assertTrue(url.startsWith("http"));
        } finally {
            storage.deleteVersion("erp", "9.9.9");
        }
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static String envOr(String name, String fallback) {
        String value = System.getenv(name);
        return hasText(value) ? value : fallback;
    }
}
