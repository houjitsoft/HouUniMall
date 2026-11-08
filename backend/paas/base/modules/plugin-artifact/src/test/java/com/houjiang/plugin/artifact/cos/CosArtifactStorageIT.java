package com.houjiang.plugin.artifact.cos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.houjiang.plugin.artifact.config.ArtifactProperties;
import com.houjiang.plugin.artifact.key.ArtifactKeys;
import com.houjiang.plugin.artifact.model.StoredObject;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

/**
 * 真实 COS 联调（需环境变量）。默认跳过。自建 MinIO 请跑 {@code MinioArtifactStorageIT}。
 *
 * <pre>
 *   set HOU_ARTIFACT_COS_ENDPOINT=https://cos.ap-guangzhou.myqcloud.com
 *   set HOU_ARTIFACT_COS_SECRET_ID=...
 *   set HOU_ARTIFACT_COS_SECRET_KEY=...
 *   set HOU_ARTIFACT_COS_BUCKET=unimall
 *   set HOU_ARTIFACT_COS_IT=true
 * </pre>
 */
class CosArtifactStorageIT {

    @Test
    void putAndPresignRoundTrip() throws Exception {
        assumeTrue("true".equalsIgnoreCase(System.getenv("HOU_ARTIFACT_COS_IT")));
        assumeTrue(hasText(System.getenv("HOU_ARTIFACT_COS_ENDPOINT")));
        assumeTrue(hasText(System.getenv("HOU_ARTIFACT_COS_SECRET_ID")));
        assumeTrue(hasText(System.getenv("HOU_ARTIFACT_COS_SECRET_KEY")));

        ArtifactProperties properties = new ArtifactProperties();
        properties.setType("cos");
        ArtifactProperties.Cos cos = properties.getCos();
        cos.setEndpoint(System.getenv("HOU_ARTIFACT_COS_ENDPOINT"));
        cos.setAccessKey(System.getenv("HOU_ARTIFACT_COS_SECRET_ID"));
        cos.setSecretKey(System.getenv("HOU_ARTIFACT_COS_SECRET_KEY"));
        cos.setBucket(envOr("HOU_ARTIFACT_COS_BUCKET", "unimall"));
        cos.setRegion(envOr("HOU_ARTIFACT_COS_REGION", ""));
        cos.setSecure(true);

        CosArtifactStorage storage = new CosArtifactStorage(properties);
        byte[] payload = ("cos-it-" + System.currentTimeMillis()).getBytes(StandardCharsets.UTF_8);
        String key = ArtifactKeys.commercialSourceKey("erp", "9.9.8");
        try {
            StoredObject stored = storage.putIfAbsent(key, new ByteArrayInputStream(payload), payload.length);
            assertEquals("cos", stored.getStorageType());
            String url = storage.signedInternalUrl(key, java.time.Duration.ofMinutes(5));
            assertTrue(url.startsWith("http"));
        } finally {
            storage.deleteVersion("erp", "9.9.8");
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
