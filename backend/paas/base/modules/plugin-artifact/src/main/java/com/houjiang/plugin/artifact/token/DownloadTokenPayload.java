package com.houjiang.plugin.artifact.token;

import java.util.Objects;

/**
 * 下载令牌载荷（方案 §6.4）。
 *
 * <p>明文结构（{@code |} 分隔，共 8 段）：
 * <pre>
 *   aid|c|v|tid|uid|exp|n|lim
 *    |  |  |   |   |   |  |
 *    |  |  |   |   |   |  └─ lim 最大命中次数
 *    |  |  |   |   |   └──── n   nonce，防重放（落库 uk_nonce 占用）
 *    |  |  |   |   └──────── exp 过期时间（epoch 毫秒）
 *    |  |  |   └──────────── uid 下载用户
 *    |  |  └──────────────── tid 租户/站点实例（instanceId）
 *    |  └─────────────────── v   版本
 *    └────────────────────── c   插件编码
 *   aid                          制品 ID（不可变身份）
 * </pre>
 *
 * <p><b>载荷是明文</b>：令牌只做完整性保护（HMAC-SHA256），不做加密。
 * 这里不放任何敏感信息——即便被 base64url 解出来，也只是「谁在什么时候可以下哪个制品」。
 * 真正的保密性由「链接 10 分钟过期 + 限次 + 租户绑定」三件事兜底，而不是靠载荷不可读。
 *
 * <p><b>{@code |} 是保留分隔符</b>：所有字段在构造时都会校验不含 {@code |}，
 * 否则解析会串段（typed 的租户 ID 来自外部系统，必须挡在入口）。
 *
 * @author Amos
 * @since M2
 */
public final class DownloadTokenPayload {

    /**
     * 载荷段分隔符。
     */
    public static final char SEPARATOR = '|';

    /**
     * 载荷段数。
     */
    private static final int SEGMENT_COUNT = 8;

    private final String artifactId;

    private final String pluginCode;

    private final String version;

    private final String tenantId;

    private final Long userId;

    private final long expiresAtMillis;

    private final String nonce;

    private final int maxHits;

    /**
     * 构造下载令牌载荷。
     *
     * @param artifactId      制品 ID
     * @param pluginCode      插件编码
     * @param version         版本
     * @param tenantId        租户/站点实例（instanceId）
     * @param userId          下载用户，可为 null
     * @param expiresAtMillis 过期时间（epoch 毫秒）
     * @param nonce           防重放随机串
     * @param maxHits         最大命中次数
     */
    public DownloadTokenPayload(String artifactId,
                                String pluginCode,
                                String version,
                                String tenantId,
                                Long userId,
                                long expiresAtMillis,
                                String nonce,
                                int maxHits) {
        this.artifactId = requireNoPipe("artifactId", artifactId);
        this.pluginCode = requireNoPipe("pluginCode", pluginCode);
        this.version = requireNoPipe("version", version);
        this.tenantId = requireNoPipe("tenantId", tenantId == null ? "" : tenantId);
        this.userId = userId;
        this.expiresAtMillis = expiresAtMillis;
        this.nonce = requireNoPipe("nonce", nonce);
        this.maxHits = maxHits;
    }

    /**
     * 序列化为明文载荷串（{@code |} 分隔 8 段）。
     *
     * @return 载荷串
     */
    public String serialize() {
        return String.valueOf(artifactId)
                + SEPARATOR + pluginCode
                + SEPARATOR + version
                + SEPARATOR + tenantId
                + SEPARATOR + (userId == null ? "" : String.valueOf(userId))
                + SEPARATOR + expiresAtMillis
                + SEPARATOR + nonce
                + SEPARATOR + maxHits;
    }

    /**
     * 解析明文载荷串。
     *
     * @param raw 载荷串
     * @return 载荷对象
     * @throws DownloadTokenPayloadFormatException 段数不对或字段非法时抛出
     */
    public static DownloadTokenPayload parse(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new DownloadTokenPayloadFormatException("下载令牌载荷为空");
        }
        String[] segments = raw.split("\\" + SEPARATOR, -1);
        if (segments.length != SEGMENT_COUNT) {
            throw new DownloadTokenPayloadFormatException(
                    "下载令牌载荷必须为 " + SEGMENT_COUNT + " 段，实际 " + segments.length + " 段");
        }
        Long userId = null;
        if (!segments[4].isEmpty()) {
            try {
                userId = Long.parseLong(segments[4]);
            } catch (NumberFormatException e) {
                throw new DownloadTokenPayloadFormatException("下载令牌载荷 userId 非法：" + segments[4]);
            }
        }
        long expiresAtMillis;
        int maxHits;
        try {
            expiresAtMillis = Long.parseLong(segments[5]);
            maxHits = Integer.parseInt(segments[7]);
        } catch (NumberFormatException e) {
            throw new DownloadTokenPayloadFormatException("下载令牌载荷 exp/lim 非法：" + e.getMessage());
        }
        return new DownloadTokenPayload(segments[0], segments[1], segments[2], segments[3],
                userId, expiresAtMillis, segments[6], maxHits);
    }

    /**
     * 判断载荷是否已过期。
     *
     * @param nowMillis 当前时间（epoch 毫秒）
     * @return true 表示已过期
     */
    public boolean isExpired(long nowMillis) {
        return nowMillis >= expiresAtMillis;
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
     * @return 租户/站点实例（instanceId），无值时为空串
     */
    public String getTenantId() {
        return tenantId;
    }

    /**
     * @return 下载用户，可为 null
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * @return 过期时间（epoch 毫秒）
     */
    public long getExpiresAtMillis() {
        return expiresAtMillis;
    }

    /**
     * @return 防重放随机串
     */
    public String getNonce() {
        return nonce;
    }

    /**
     * @return 最大命中次数
     */
    public int getMaxHits() {
        return maxHits;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof DownloadTokenPayload that)) {
            return false;
        }
        return expiresAtMillis == that.expiresAtMillis
                && maxHits == that.maxHits
                && Objects.equals(artifactId, that.artifactId)
                && Objects.equals(pluginCode, that.pluginCode)
                && Objects.equals(version, that.version)
                && Objects.equals(tenantId, that.tenantId)
                && Objects.equals(userId, that.userId)
                && Objects.equals(nonce, that.nonce);
    }

    @Override
    public int hashCode() {
        return Objects.hash(artifactId, pluginCode, version, tenantId, userId,
                expiresAtMillis, nonce, maxHits);
    }

    @Override
    public String toString() {
        return "DownloadTokenPayload{code=" + pluginCode + ", version=" + version
                + ", tenant=" + tenantId + ", exp=" + expiresAtMillis + ", hits=" + maxHits + "}";
    }

    /**
     * 校验字段不含保留分隔符。
     *
     * @param name  字段名
     * @param value 字段值
     * @return 原值
     * @throws DownloadTokenPayloadFormatException 含分隔符时抛出
     */
    private static String requireNoPipe(String name, String value) {
        if (value == null) {
            throw new DownloadTokenPayloadFormatException(name + " 不能为 null");
        }
        if (value.indexOf(SEPARATOR) >= 0) {
            throw new DownloadTokenPayloadFormatException(
                    name + " 不允许包含载荷分隔符 '" + SEPARATOR + "'：" + value);
        }
        return value;
    }

    /**
     * 载荷格式非法（段数/字段不合法）。
     */
    public static class DownloadTokenPayloadFormatException extends IllegalArgumentException {

        private static final long serialVersionUID = 1L;

        /**
         * 构造格式异常。
         *
         * @param message 描述
         */
        public DownloadTokenPayloadFormatException(String message) {
            super(message);
        }
    }
}
