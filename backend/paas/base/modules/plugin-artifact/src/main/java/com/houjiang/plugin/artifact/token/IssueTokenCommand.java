package com.houjiang.plugin.artifact.token;

import com.houjiang.mall.entity.artifact.PluginDownloadToken;
import java.time.LocalDateTime;

/**
 * 签发下载令牌的入参。
 *
 * <p>所有身份字段（instanceId / userId）都由<b>服务端</b>从登录态解析后填入，
 * 构造后再无 setter，避免中间链路被改。
 *
 * @author Amos
 * @since M2
 */
public final class IssueTokenCommand {

    private final String artifactId;

    private final String pluginCode;

    private final String version;

    private final String instanceId;

    private final Long userId;

    private final String clientIp;

    private final String userAgent;

    /**
     * 构造签发命令。
     *
     * @param artifactId 制品 ID
     * @param pluginCode 插件编码
     * @param version    版本
     * @param instanceId 站点实例（租户），可为空串
     * @param userId     下载用户，可为 null
     * @param clientIp   客户端 IP，泄露溯源用，可为 null
     * @param userAgent  客户端 UA，泄露溯源用，可为 null
     */
    public IssueTokenCommand(String artifactId,
                             String pluginCode,
                             String version,
                             String instanceId,
                             Long userId,
                             String clientIp,
                             String userAgent) {
        this.artifactId = artifactId;
        this.pluginCode = pluginCode;
        this.version = version;
        this.instanceId = instanceId == null ? "" : instanceId;
        this.userId = userId;
        this.clientIp = clientIp;
        this.userAgent = userAgent;
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
     * @return 站点实例（租户），无值时为空串
     */
    public String getInstanceId() {
        return instanceId;
    }

    /**
     * @return 下载用户，可为 null
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * @return 客户端 IP，可为 null
     */
    public String getClientIp() {
        return clientIp;
    }

    /**
     * @return 客户端 UA，可为 null
     */
    public String getUserAgent() {
        return userAgent;
    }

    /**
     * 转成落库的令牌留痕行（hits 固定 0，时间由服务层填）。
     *
     * @param tokenId  令牌 ID
     * @param nonce    防重放随机串
     * @param issuedAt 签发时间
     * @param expiresAt 过期时间
     * @param maxHits  最大命中次数
     * @return 待插入的实体
     */
    public PluginDownloadToken toEntity(String tokenId,
                                        String nonce,
                                        LocalDateTime issuedAt,
                                        LocalDateTime expiresAt,
                                        int maxHits) {
        PluginDownloadToken row = new PluginDownloadToken();
        row.setTokenId(tokenId);
        row.setNonce(nonce);
        row.setInstanceId(instanceId);
        row.setUserId(userId);
        row.setPluginCode(pluginCode);
        row.setVersion(version);
        row.setArtifactId(artifactId);
        row.setIssuedAt(issuedAt);
        row.setExpiresAt(expiresAt);
        row.setMaxHits(maxHits);
        row.setHits(0);
        row.setClientIp(clientIp);
        row.setUserAgent(truncate(userAgent, 255));
        return row;
    }

    /**
     * 超长 UA 截断，避免超过列宽（VARCHAR(255)）导致写入失败。
     *
     * @param value 原始值
     * @param max   最大长度
     * @return 截断后的值，原值为 null 时返回 null
     */
    private static String truncate(String value, int max) {
        if (value == null || value.length() <= max) {
            return value;
        }
        return value.substring(0, max);
    }
}
