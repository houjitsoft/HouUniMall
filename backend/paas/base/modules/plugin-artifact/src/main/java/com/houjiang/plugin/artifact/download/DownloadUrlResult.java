package com.houjiang.plugin.artifact.download;

/**
 * 下载链接签发结果（{@code /base/plugin-center/artifact/download-url} 的 data）。
 *
 * <p>除了链接本身，还回传 md5 / sha256 / size：客户端下载完成后要<b>先校验再安装</b>，
 * 与其让它下完再回头查一次接口，不如换票时就一并返回，省一次往返也少一个不一致窗口。
 *
 * @author Amos
 * @since M2
 */
public final class DownloadUrlResult {

    private final String url;

    private final String token;

    private final String artifactId;

    private final String pluginCode;

    private final String version;

    private final String md5;

    private final String sha256;

    private final long sizeBytes;

    private final String fileName;

    private final String artifactKind;

    private final long expiresAtMillis;

    private final int maxHits;

    /**
     * 对象存储预签名直链（MinIO/COS/OSS）；本地磁盘时为 {@code null}。
     * 安装代理仍必须走 {@link #url}（相对 raw + 令牌），禁止改用本字段外链。
     */
    private final String cloudUrl;

    /**
     * 构造签发结果。
     *
     * @param url             可直接下载的链接（含令牌）
     * @param token           下载令牌
     * @param artifactId      制品 ID
     * @param pluginCode      插件编码
     * @param version         版本
     * @param md5             制品 md5
     * @param sha256          制品 sha256
     * @param sizeBytes       制品字节数
     * @param fileName        下载文件名
     * @param expiresAtMillis 过期时间（epoch 毫秒）
     * @param maxHits         最大命中次数
     */
    public DownloadUrlResult(String url,
                             String token,
                             String artifactId,
                             String pluginCode,
                             String version,
                             String md5,
                             String sha256,
                             long sizeBytes,
                             String fileName,
                             long expiresAtMillis,
                             int maxHits) {
        this(url, token, artifactId, pluginCode, version, md5, sha256, sizeBytes, fileName, expiresAtMillis, maxHits, "jar", null);
    }

    public DownloadUrlResult(String url, String token, String artifactId, String pluginCode, String version,
                             String md5, String sha256, long sizeBytes, String fileName,
                             long expiresAtMillis, int maxHits, String artifactKind) {
        this(url, token, artifactId, pluginCode, version, md5, sha256, sizeBytes, fileName, expiresAtMillis, maxHits, artifactKind, null);
    }

    public DownloadUrlResult(String url, String token, String artifactId, String pluginCode, String version,
                             String md5, String sha256, long sizeBytes, String fileName,
                             long expiresAtMillis, int maxHits, String artifactKind, String cloudUrl) {
        this.url = url;
        this.token = token;
        this.artifactId = artifactId;
        this.pluginCode = pluginCode;
        this.version = version;
        this.md5 = md5;
        this.sha256 = sha256;
        this.sizeBytes = sizeBytes;
        this.fileName = fileName;
        this.artifactKind = artifactKind == null ? "jar" : artifactKind;
        this.expiresAtMillis = expiresAtMillis;
        this.maxHits = maxHits;
        this.cloudUrl = cloudUrl;
    }

    /**
     * @return 可直接下载的链接
     */
    public String getUrl() {
        return url;
    }

    /**
     * @return 下载令牌
     */
    public String getToken() {
        return token;
    }

    /**
     * @return 制品 ID
     */
    public String getArtifactId() {
        return artifactId;
    }

    /**
     * @return 插件编码
     */
    public String getPluginCode() {
        return pluginCode;
    }

    /**
     * @return 版本
     */
    public String getVersion() {
        return version;
    }

    /**
     * @return 制品 md5
     */
    public String getMd5() {
        return md5;
    }

    /**
     * @return 制品 sha256
     */
    public String getSha256() {
        return sha256;
    }

    /**
     * @return 制品字节数
     */
    public long getSizeBytes() {
        return sizeBytes;
    }

    /**
     * @return 下载文件名
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @return 过期时间（epoch 毫秒）
     */
    public String getArtifactKind() { return artifactKind; }

    public long getExpiresAtMillis() {
        return expiresAtMillis;
    }

    /**
     * @return 最大命中次数
     */
    public int getMaxHits() {
        return maxHits;
    }

    /**
     * @return 对象存储预签名 URL；本地磁盘实现返回 {@code null}
     */
    public String getCloudUrl() {
        return cloudUrl;
    }
}
