package com.houjiang.plugin.artifact.minio;

import com.houjiang.plugin.artifact.config.ArtifactProperties;
import com.houjiang.plugin.artifact.s3.AbstractS3CompatibleArtifactStorage;
import io.minio.MinioClient;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 自建 MinIO（S3 API）制品存储。
 *
 * <p>生产对接站点内网 S3 API（例如 {@code http://127.0.0.1:19000}）；
 * Console（{@code https://fss.../login}）不能给 SDK 用。
 */
@Component
@ConditionalOnProperty(name = "houcloud.artifact.type", havingValue = "minio")
public class MinioArtifactStorage extends AbstractS3CompatibleArtifactStorage {

    @Autowired
    public MinioArtifactStorage(ArtifactProperties properties) {
        super(toSettings(Objects.requireNonNull(properties, "ArtifactProperties").getMinio()), "minio");
    }

    MinioArtifactStorage(ArtifactProperties.Minio config, MinioClient client) {
        super(toSettings(config), "minio", client);
    }

    private static S3EndpointSettings toSettings(ArtifactProperties.Minio config) {
        Objects.requireNonNull(config, "houcloud.artifact.minio 不能为空");
        return new S3EndpointSettings(
                config.getEndpoint(),
                config.getAccessKey(),
                config.getSecretKey(),
                config.getBucket(),
                config.getKeyPrefix(),
                config.getSecure(),
                config.getRegion());
    }
}
