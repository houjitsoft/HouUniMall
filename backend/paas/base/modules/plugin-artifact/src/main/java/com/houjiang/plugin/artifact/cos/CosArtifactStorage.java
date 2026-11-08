package com.houjiang.plugin.artifact.cos;

import com.houjiang.plugin.artifact.config.ArtifactProperties;
import com.houjiang.plugin.artifact.s3.AbstractS3CompatibleArtifactStorage;
import io.minio.MinioClient;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 腾讯云 COS（S3 兼容 API）制品存储。
 *
 * <p>与自建 MinIO 共用 {@link AbstractS3CompatibleArtifactStorage}；
 * 切换 {@code houcloud.artifact.type=cos} 并配置 {@code houcloud.artifact.cos.*}。
 * 自建 MinIO 场景请继续用 {@code type=minio}。
 */
@Component
@ConditionalOnProperty(name = "houcloud.artifact.type", havingValue = "cos")
public class CosArtifactStorage extends AbstractS3CompatibleArtifactStorage {

    @Autowired
    public CosArtifactStorage(ArtifactProperties properties) {
        super(toSettings(Objects.requireNonNull(properties, "ArtifactProperties").getCos()), "cos");
    }

    CosArtifactStorage(ArtifactProperties.Cos config, MinioClient client) {
        super(toSettings(config), "cos", client);
    }

    private static S3EndpointSettings toSettings(ArtifactProperties.Cos config) {
        Objects.requireNonNull(config, "houcloud.artifact.cos 不能为空");
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
