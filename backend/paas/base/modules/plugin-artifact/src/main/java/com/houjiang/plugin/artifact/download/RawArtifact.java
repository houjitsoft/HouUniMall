package com.houjiang.plugin.artifact.download;

import com.houjiang.plugin.artifact.model.ArtifactStream;
import java.util.Objects;

/**
 * 回源结果：打开的制品流 + 对应的制品元信息。
 *
 * <p>把「流」和「元信息」绑在一起返回，是为了让端点能同时拿到
 * 响应头需要的 md5 / sha256 / 文件名，以及真正要写的字节流——
 * 分两次查库既浪费，也可能读到不一致的快照。
 *
 * @author Amos
 * @since M2
 */
public final class RawArtifact {

    private final ArtifactStream stream;
    private final Runnable receipt;

    private final String artifactId;

    private final String pluginCode;

    private final String version;

    private final String md5;

    private final String sha256;

    private final long sizeBytes;

    private final String fileName;

    /**
     * 构造回源结果。
     *
     * @param stream     制品流
     * @param artifactId 制品 ID
     * @param pluginCode 插件编码
     * @param version    版本
     * @param md5        制品 md5
     * @param sha256     制品 sha256
     * @param sizeBytes  制品字节数
     */
    public RawArtifact(ArtifactStream stream,
                       String artifactId,
                       String pluginCode,
                       String version,
                       String md5,
                       String sha256,
                       long sizeBytes) {
        this(stream, artifactId, pluginCode, version, md5, sha256, sizeBytes, null);
    }

    public RawArtifact(ArtifactStream stream, String artifactId, String pluginCode, String version,
                       String md5, String sha256, long sizeBytes, String fileName) {
        this(stream,artifactId,pluginCode,version,md5,sha256,sizeBytes,fileName,()->{});
    }

    public RawArtifact(ArtifactStream stream,String artifactId,String pluginCode,String version,String md5,String sha256,long sizeBytes,String fileName,Runnable receipt) {
        this.receipt=receipt;
        this.stream = Objects.requireNonNull(stream, "制品流不能为空");
        this.artifactId = artifactId;
        this.pluginCode = pluginCode;
        this.version = version;
        this.md5 = md5;
        this.sha256 = sha256;
        this.sizeBytes = sizeBytes;
        this.fileName = fileName;
    }

    public void transferred() { receipt.run(); }

    /**
     * @return 制品流
     */
    public ArtifactStream getStream() {
        return stream;
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
     * @return 下载文件名 {@code {code}-{version}.jar}
     */
    public String getFileName() {
        return fileName == null || fileName.isBlank() ? pluginCode + "-" + version + ".jar" : fileName;
    }
}
