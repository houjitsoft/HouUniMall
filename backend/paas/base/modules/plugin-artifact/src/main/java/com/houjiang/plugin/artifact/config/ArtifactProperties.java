package com.houjiang.plugin.artifact.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 制品存储配置，前缀 {@code houcloud.artifact.*}。
 *
 * <p>配置示例（Nacos / application.yml）：
 * <pre>
 * houcloud:
 *   artifact:
 *     type: local                              # local | minio | cos
 *     root: D:/wwwroot/houcloud/artifacts      # 留空则按 OS 取默认值（仅 local）
 *     retain-versions: 3                       # 每个插件保留的历史版本数（回滚锚点）
 *     minio:
 *       endpoint: http://127.0.0.1:19000
 *       bucket: unimall-plugins
 *     sign:
 *       enabled: true
 * </pre>
 *
 * @author Amos
 * @since M0
 */
@Component
@ConfigurationProperties(prefix = "houcloud.artifact")
public class ArtifactProperties {

    /**
     * 存储类型：local（默认）/ minio（自建）/ cos（腾讯云 COS S3 API）。
     */
    private String type = "local";

    /**
     * MinIO / S3 兼容对象存储配置（{@code type=minio} 时生效）。
     */
    private Minio minio = new Minio();

    /**
     * 腾讯云 COS（S3 兼容）配置（{@code type=cos} 时生效）。
     */
    private Cos cos = new Cos();

    /**
     * 本地存储根目录；留空时按操作系统取默认值
     * （Windows：{@code D:/wwwroot/houcloud/artifacts}，其它：{@code ${user.home}/houcloud/artifacts}）。
     *
     * <p><b>生产以 Nacos {@code mall.yaml} 里的 {@code houcloud.artifact.root} 为准</b>，
     * 这里的默认值只是「Nacos 没配 / 拉不到配置」时的兜底，避免落到意外路径。
     */
    private String root = "";

    /**
     * 每个插件保留的历史版本数，用于回滚锚点，超出后按版本淘汰。
     */
    private int retainVersions = 3;

    /**
     * 制品签名配置。
     */
    private Sign sign = new Sign();

    /**
     * 下载令牌签名密钥（HMAC-SHA256），前缀 {@code houcloud.artifact.download-token-secret}。
     *
     * <p><b>必须配置，且不能与签名私钥混用</b>：该密钥只用于给「下载链接」打水印，
     * 泄露的后果是链接可被伪造（换取制品），与制品签名密钥（证明制品未被篡改）职责不同。
     * 留空时 {@code DownloadTokenService} 在签发当次抛异常（fail-closed），
     * 而不是在启动期把整个 mall 拖挂——与 M0 的 Ed25519 私钥「懒加载」策略保持一致。
     */
    private String downloadTokenSecret = "";

    /**
     * 下载令牌有效期（秒），默认 600（10 分钟）。过期报 3007。
     */
    private long downloadTokenTtlSeconds = 600L;

    /**
     * 单个下载令牌的最大命中次数，默认 5。
     *
     * <p><b>限次而非一次性</b>：断点续传与网络重试会让同一个令牌被命中多次，
     * 一次性会把正常的重试直接打死（方案 §6.4 已明确）。
     */
    private int downloadTokenMaxHits = 5;

    /**
     * @return 存储类型
     */
    public String getType() {
        return type;
    }

    /**
     * @param type 存储类型
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return MinIO 配置
     */
    public Minio getMinio() {
        return minio;
    }

    /**
     * @param minio MinIO 配置
     */
    public void setMinio(Minio minio) {
        this.minio = minio == null ? new Minio() : minio;
    }

    /**
     * @return COS 配置
     */
    public Cos getCos() {
        return cos;
    }

    /**
     * @param cos COS 配置
     */
    public void setCos(Cos cos) {
        this.cos = cos == null ? new Cos() : cos;
    }

    /**
     * @return 本地存储根目录，可能为空串
     */
    public String getRoot() {
        return root;
    }

    /**
     * @param root 本地存储根目录
     */
    public void setRoot(String root) {
        this.root = root;
    }

    /**
     * @return 保留历史版本数
     */
    public int getRetainVersions() {
        return retainVersions;
    }

    /**
     * @param retainVersions 保留历史版本数
     */
    public void setRetainVersions(int retainVersions) {
        this.retainVersions = retainVersions;
    }

    /**
     * @return 签名配置
     */
    public Sign getSign() {
        return sign;
    }

    /**
     * @param sign 签名配置
     */
    public void setSign(Sign sign) {
        this.sign = sign;
    }

    /**
     * @return 下载令牌签名密钥，可能为空串（未配置）
     */
    public String getDownloadTokenSecret() {
        return downloadTokenSecret;
    }

    /**
     * @param downloadTokenSecret 下载令牌签名密钥
     */
    public void setDownloadTokenSecret(String downloadTokenSecret) {
        this.downloadTokenSecret = downloadTokenSecret == null ? "" : downloadTokenSecret;
    }

    /**
     * @return 下载令牌有效期（秒），默认 600
     */
    public long getDownloadTokenTtlSeconds() {
        return downloadTokenTtlSeconds;
    }

    /**
     * @param downloadTokenTtlSeconds 下载令牌有效期（秒）
     */
    public void setDownloadTokenTtlSeconds(long downloadTokenTtlSeconds) {
        this.downloadTokenTtlSeconds = downloadTokenTtlSeconds;
    }

    /**
     * @return 单个下载令牌最大命中次数，默认 5
     */
    public int getDownloadTokenMaxHits() {
        return downloadTokenMaxHits;
    }

    /**
     * @param downloadTokenMaxHits 最大命中次数
     */
    public void setDownloadTokenMaxHits(int downloadTokenMaxHits) {
        this.downloadTokenMaxHits = downloadTokenMaxHits;
    }


    /**
     * MinIO 对象存储配置（{@code houcloud.artifact.minio.*}）。
     *
     * <p>密钥一律走环境变量 / Nacos，禁止写入 git。
     */
    public static class Minio {

        /**
         * S3 API 地址，例如 {@code http://127.0.0.1:19000}（Console 端口不可用）。
         */
        private String endpoint = "";

        /**
         * Access Key。
         */
        private String accessKey = "";

        /**
         * Secret Key。
         */
        private String secretKey = "";

        /**
         * Bucket 名，默认 {@code unimall}。
         */
        private String bucket = "unimall";

        /**
         * 对象键前缀（可空）。正式 key = prefix + ArtifactKeys；
         * 商业制品另由发布服务加 {@code commercial-runtime/} / {@code commercial-source/}。
         */
        private String keyPrefix = "";

        /**
         * 是否 HTTPS；null 时根据 endpoint 自动判断。
         */
        private Boolean secure;

        /**
         * 区域名，MinIO 通常可留空或 {@code us-east-1}。
         */
        private String region = "";

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint == null ? "" : endpoint.trim();
        }

        public String getAccessKey() {
            return accessKey;
        }

        public void setAccessKey(String accessKey) {
            this.accessKey = accessKey == null ? "" : accessKey;
        }

        public String getSecretKey() {
            return secretKey;
        }

        public void setSecretKey(String secretKey) {
            this.secretKey = secretKey == null ? "" : secretKey;
        }

        public String getBucket() {
            return bucket;
        }

        public void setBucket(String bucket) {
            this.bucket = bucket == null || bucket.isBlank() ? "unimall" : bucket.trim();
        }

        public String getKeyPrefix() {
            return keyPrefix;
        }

        public void setKeyPrefix(String keyPrefix) {
            if (keyPrefix == null || keyPrefix.isBlank()) {
                this.keyPrefix = "";
                return;
            }
            String normalized = keyPrefix.trim().replaceAll("^/+", "");
            this.keyPrefix = normalized.endsWith("/") ? normalized : normalized + "/";
        }

        public Boolean getSecure() {
            return secure;
        }

        public void setSecure(Boolean secure) {
            this.secure = secure;
        }

        public String getRegion() {
            return region;
        }

        public void setRegion(String region) {
            this.region = region == null ? "" : region.trim();
        }
    }

    /**
     * 腾讯云 COS（S3 兼容）配置（{@code houcloud.artifact.cos.*}）。
     *
     * <p>endpoint 形如 {@code https://cos.ap-guangzhou.myqcloud.com}；
     * 密钥走环境变量，禁止写入 git。自建 MinIO 请用 {@link Minio}。
     */
    public static class Cos {

        private String endpoint = "";
        private String accessKey = "";
        private String secretKey = "";
        private String bucket = "unimall";
        private String keyPrefix = "";
        private Boolean secure;
        private String region = "";

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint == null ? "" : endpoint.trim();
        }

        public String getAccessKey() {
            return accessKey;
        }

        public void setAccessKey(String accessKey) {
            this.accessKey = accessKey == null ? "" : accessKey;
        }

        public String getSecretKey() {
            return secretKey;
        }

        public void setSecretKey(String secretKey) {
            this.secretKey = secretKey == null ? "" : secretKey;
        }

        public String getBucket() {
            return bucket;
        }

        public void setBucket(String bucket) {
            this.bucket = bucket == null || bucket.isBlank() ? "unimall" : bucket.trim();
        }

        public String getKeyPrefix() {
            return keyPrefix;
        }

        public void setKeyPrefix(String keyPrefix) {
            if (keyPrefix == null || keyPrefix.isBlank()) {
                this.keyPrefix = "";
                return;
            }
            String normalized = keyPrefix.trim().replaceAll("^/+", "");
            this.keyPrefix = normalized.endsWith("/") ? normalized : normalized + "/";
        }

        public Boolean getSecure() {
            return secure;
        }

        public void setSecure(Boolean secure) {
            this.secure = secure;
        }

        public String getRegion() {
            return region;
        }

        public void setRegion(String region) {
            this.region = region == null ? "" : region.trim();
        }
    }

    /**
     * 制品签名配置（{@code houcloud.artifact.sign.*}）。
     */
    public static class Sign {

        /**
         * 是否启用制品签名（关闭后仍可下载，但客户端不强制验签）。
         */
        private boolean enabled = true;

        /**
         * 当前生效的签名密钥 ID（新制品用它签名）。命名规范 {@code k{yyyy}}，如 k2026。
         */
        private String activeKid = "k2026";

        /**
         * @return 是否启用制品签名
         */
        public boolean isEnabled() {
            return enabled;
        }

        /**
         * @param enabled 是否启用制品签名
         */
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        /**
         * @return 当前生效的签名 kid
         */
        public String getActiveKid() {
            return activeKid;
        }

        /**
         * @param activeKid 当前生效的签名 kid
         */
        public void setActiveKid(String activeKid) {
            this.activeKid = activeKid;
        }
    }
}
