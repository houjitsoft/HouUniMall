package com.houjiang.plugin.artifact.token;

import com.houjiang.commons.constant.PluginDistErrorCode;
import com.houjiang.mall.entity.artifact.PluginDownloadToken;
import com.houjiang.mall.mapper.artifact.PluginDownloadTokenMapper;
import com.houjiang.plugin.artifact.config.ArtifactProperties;
import com.houjiang.plugin.artifact.exception.DownloadTokenException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Objects;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 下载令牌服务（方案 §6.4）：签发 / 校验 / 限次 / 防重放。
 *
 * <p>令牌结构：{@code base64url(明文载荷) + "." + base64url(HMAC-SHA256(secret, 明文载荷))}
 * <ul>
 *   <li><b>明文载荷</b>：见 {@link DownloadTokenPayload}，8 段 {@code |} 分隔；</li>
 *   <li><b>签名</b>：HMAC-SHA256，密钥取 {@code houcloud.artifact.download-token-secret}；</li>
 *   <li><b>防重放</b>：载荷带 nonce，落库受 {@code uk_nonce} 约束
 *       —— 方案原文用 Redis SETNX 原子占用，本项目 <b>不引 Redis</b>，改为「DB 唯一键占用」：
 *       签发时 INSERT，撞串即换串重试；校验时按 nonce 定位留痕行，
 *       再以 {@code UPDATE ... SET hits=hits+1 WHERE hits < max_hits} 原子限次。
 *       这样即使并发命中同一令牌，也只会成功 max_hits 次。</li>
 * </ul>
 *
 * <p><b>密钥懒校验</b>：密钥在签发/校验当次才读取，未配置时抛 {@link IllegalStateException}，
 * 而不是启动期失败——避免「没配下载密钥」把整个 mall 拖挂（与 M0 Ed25519 私钥同策略）。
 *
 * @author Amos
 * @since M2
 */
@Slf4j
@Service
public class DownloadTokenService {

    /**
     * 令牌分隔符：载荷段与签名段。
     */
    private static final char TOKEN_SEPARATOR = '.';

    /**
     * HMAC 算法。
     */
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    /**
     * 随机串长度（字节），十六进制后为 32 字符，正好贴合 VARCHAR(32)。
     */
    private static final int RANDOM_BYTES = 16;

    /**
     * 签发时撞 uk_nonce 的最大重试次数。
     */
    private static final int MAX_ISSUE_RETRY = 3;

    private final ArtifactProperties properties;

    private final PluginDownloadTokenMapper tokenMapper;

    private final SecureRandom random = new SecureRandom();

    /**
     * 构造下载令牌服务。
     *
     * @param properties  制品配置（提供密钥 / 有效期 / 限次）
     * @param tokenMapper 令牌留痕 Mapper
     */
    public DownloadTokenService(ArtifactProperties properties, PluginDownloadTokenMapper tokenMapper) {
        this.properties = Objects.requireNonNull(properties, "ArtifactProperties 不能为空");
        this.tokenMapper = Objects.requireNonNull(tokenMapper, "PluginDownloadTokenMapper 不能为空");
    }

    /**
     * 签发下载令牌：生成 nonce → 落留痕（uk_nonce 占用）→ 返回带签名的令牌串。
     *
     * <p><b>为什么必须是 REQUIRES_NEW</b>（2026-10-28 修）：在线安装
     * （{@code PluginMarketService#installFromMarket}）是 {@code @Transactional} 的，
     * 换票与随后的回源下载发生在同一笔事务里。令牌留痕若跟着外层事务走，回源请求
     * （另一条连接、独立事务）查 {@code selectByNonce} 时<b>看不到未提交的行</b>，
     * 一律 403「下载链接无效或已过期」——表现为「换票成功、下载永远失败」。
     * 令牌是「先签发、后使用」的短时效凭证，本就该独立提交：外层安装失败回滚时，
     * 残留的令牌自己会在 10 分钟内过期，无副作用。
     *
     * @param command 签发命令（身份字段由服务端注入）
     * @return 签发的令牌
     * @throws IllegalStateException 未配置签名密钥，或制品 ID / 插件编码缺失
     * @throws DuplicateKeyException 连续重试仍撞上相同的 nonce（概率极低，视为系统异常）
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public IssuedToken issue(IssueTokenCommand command) {
        Objects.requireNonNull(command, "IssueTokenCommand 不能为空");
        if (!StringUtils.hasText(command.getArtifactId())) {
            throw new IllegalStateException("签发下载令牌缺少 artifactId");
        }
        if (!StringUtils.hasText(command.getPluginCode())) {
            throw new IllegalStateException("签发下载令牌缺少 pluginCode");
        }
        String secret = requireSecret();

        long nowMillis = System.currentTimeMillis();
        // 不做下限钳制：配成 0/负数就是「签发即过期」，属于显式的 fail-closed 配置，
        // 若悄悄钳到 1 秒，反而会让错误配置看起来「能用」。
        long ttlMillis = properties.getDownloadTokenTtlSeconds() * 1000L;
        int maxHits = Math.max(1, properties.getDownloadTokenMaxHits());
        long expiresAtMillis = nowMillis + ttlMillis;

        DuplicateKeyException lastConflict = null;
        for (int attempt = 1; attempt <= MAX_ISSUE_RETRY; attempt++) {
            String tokenId = randomHex(RANDOM_BYTES);
            String nonce = randomHex(RANDOM_BYTES);
            DownloadTokenPayload payload = new DownloadTokenPayload(
                    command.getArtifactId(),
                    command.getPluginCode(),
                    command.getVersion() == null ? "" : command.getVersion(),
                    command.getInstanceId(),
                    command.getUserId(),
                    expiresAtMillis,
                    nonce,
                    maxHits);
            String raw = payload.serialize();
            String token = encode(raw) + TOKEN_SEPARATOR + encode(hmacSha256(raw, secret));
            PluginDownloadToken row = command.toEntity(tokenId, nonce,
                    toLocalDateTime(nowMillis), toLocalDateTime(expiresAtMillis), maxHits);
            try {
                tokenMapper.insert(row);
            } catch (DuplicateKeyException e) {
                // nonce 撞了 uk_nonce：换一串重签，绝不复用旧的（复用等于放弃防重放）
                lastConflict = e;
                log.warn("下载令牌 nonce 冲突，重试签发: attempt={}, plugin={}, version={}",
                        attempt, command.getPluginCode(), command.getVersion());
                continue;
            }
            log.info("签发下载令牌: plugin={} version={} instance={} user={} exp={} maxHits={}",
                    command.getPluginCode(), command.getVersion(), command.getInstanceId(),
                    command.getUserId(), expiresAtMillis, maxHits);
            return new IssuedToken(token, tokenId, nonce, expiresAtMillis, maxHits);
        }
        throw new IllegalStateException("下载令牌 nonce 连续冲突，签发失败", lastConflict);
    }

    /**
     * 校验下载令牌并原子占用一次命中次数。
     *
     * <p>校验顺序（先便宜后昂贵，且失败一律不泄露细节）：
     * <ol>
     *   <li>结构 + 签名（篡改 / 伪造 → 3007）；</li>
     *   <li>按 nonce 定位留痕行，行不存在或与载荷不一致 → 3007；</li>
     *   <li>已过期 → 3007；</li>
     *   <li>租户不匹配 → 3008；</li>
     *   <li>{@code hits < max_hits} 原子自增，抢不到 → 3007（次数用尽，含重放）。</li>
     * </ol>
     *
     * @param token           令牌串
     * @param expectedTenantId 期望的租户（站点实例）；为 null 或空白时跳过租户校验
     * @return 解析出的载荷
     * @throws DownloadTokenException 令牌无效 / 过期 / 越权 / 次数用尽
     */
    public DownloadTokenPayload verify(String token, String expectedTenantId) {
        if (!StringUtils.hasText(token)) {
            throw new DownloadTokenException(PluginDistErrorCode.DOWNLOAD_TOKEN_EXPIRED, "下载链接缺少令牌");
        }
        int separator = token.indexOf(TOKEN_SEPARATOR);
        if (separator <= 0 || separator == token.length() - 1) {
            throw new DownloadTokenException(PluginDistErrorCode.DOWNLOAD_TOKEN_EXPIRED, "下载链接格式非法");
        }
        String raw;
        byte[] providedSignature;
        try {
            raw = new String(decode(token.substring(0, separator)), StandardCharsets.UTF_8);
            providedSignature = decode(token.substring(separator + 1));
        } catch (IllegalArgumentException e) {
            throw new DownloadTokenException(PluginDistErrorCode.DOWNLOAD_TOKEN_EXPIRED, "下载链接编码非法");
        }

        String secret = requireSecret();
        byte[] expectedSignature = hmacSha256(raw, secret);
        if (!MessageDigest.isEqual(expectedSignature, providedSignature)) {
            // 篡改 / 用别的密钥签的：统一按「无效或已过期」返回，不泄露原因
            log.warn("下载令牌签名校验失败，疑似伪造链接: raw={}", raw);
            throw new DownloadTokenException(PluginDistErrorCode.DOWNLOAD_TOKEN_EXPIRED, "下载链接无效或已过期");
        }

        DownloadTokenPayload payload;
        try {
            payload = DownloadTokenPayload.parse(raw);
        } catch (RuntimeException e) {
            throw new DownloadTokenException(PluginDistErrorCode.DOWNLOAD_TOKEN_EXPIRED, "下载链接无效或已过期");
        }

        PluginDownloadToken row = tokenMapper.selectByNonce(payload.getNonce());
        if (row == null) {
            throw new DownloadTokenException(PluginDistErrorCode.DOWNLOAD_TOKEN_EXPIRED, "下载链接无效或已过期");
        }
        if (!matches(row, payload)) {
            // 留痕与载荷对不上：有人拿 A 令牌的留痕配 B 令牌
            log.warn("下载令牌留痕与载荷不一致: tokenId={}", row.getTokenId());
            throw new DownloadTokenException(PluginDistErrorCode.DOWNLOAD_TOKEN_EXPIRED, "下载链接无效或已过期");
        }
        if (payload.isExpired(System.currentTimeMillis())) {
            throw new DownloadTokenException(PluginDistErrorCode.DOWNLOAD_TOKEN_EXPIRED, "下载链接已过期");
        }
        if (StringUtils.hasText(expectedTenantId) && !expectedTenantId.trim().equals(payload.getTenantId())) {
            log.warn("下载令牌租户不匹配: 期望={}, 实际={}", expectedTenantId, payload.getTenantId());
            throw new DownloadTokenException(
                    PluginDistErrorCode.TOKEN_TENANT_MISMATCH, "下载链接与当前站点不匹配");
        }

        // 一次语句完成「判断 + 自增」：并发命中同一令牌时不会超发
        int updated = tokenMapper.consumeHit(payload.getNonce());
        if (updated <= 0) {
            throw new DownloadTokenException(
                    PluginDistErrorCode.DOWNLOAD_TOKEN_EXPIRED, "下载链接使用次数已用尽，请重新获取");
        }
        return payload;
    }

    /**
     * @return HMAC 密钥；未配置时抛异常（fail-closed）
     */
    private String requireSecret() {
        String secret = properties.getDownloadTokenSecret();
        if (!StringUtils.hasText(secret)) {
            throw new IllegalStateException(
                    "未配置 houcloud.artifact.download-token-secret，无法签发/校验下载令牌");
        }
        return secret;
    }

    /**
     * 校验留痕行与令牌载荷是否指向同一次签发。
     *
     * @param row     留痕行
     * @param payload 令牌载荷
     * @return true 表示一致
     */
    private static boolean matches(PluginDownloadToken row, DownloadTokenPayload payload) {
        return Objects.equals(row.getArtifactId(), payload.getArtifactId())
                && Objects.equals(row.getPluginCode(), payload.getPluginCode())
                && Objects.equals(nullToEmpty(row.getVersion()), payload.getVersion())
                && Objects.equals(nullToEmpty(row.getInstanceId()), payload.getTenantId());
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static String encode(String value) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private static String encode(byte[] value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }

    private static byte[] decode(String value) {
        return Base64.getUrlDecoder().decode(value);
    }

    /**
     * 计算 HMAC-SHA256。
     *
     * @param data   明文
     * @param secret 密钥
     * @return 签名字节
     */
    private static byte[] hmacSha256(String data, String secret) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("当前 JDK 不支持 " + HMAC_ALGORITHM, e);
        }
    }

    /**
     * 生成十六进制随机串。
     *
     * @param byteCount 随机字节数
     * @return 长度为 {@code byteCount * 2} 的小写十六进制串
     */
    private String randomHex(int byteCount) {
        byte[] bytes = new byte[byteCount];
        random.nextBytes(bytes);
        StringBuilder sb = new StringBuilder(byteCount * 2);
        for (byte b : bytes) {
            sb.append(Character.forDigit((b >> 4) & 0xF, 16));
            sb.append(Character.forDigit(b & 0xF, 16));
        }
        return sb.toString();
    }

    private static LocalDateTime toLocalDateTime(long epochMillis) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneId.systemDefault());
    }

    /**
     * 签发结果。
     */
    public static final class IssuedToken {

        private final String token;

        private final String tokenId;

        private final String nonce;

        private final long expiresAtMillis;

        private final int maxHits;

        private IssuedToken(String token, String tokenId, String nonce, long expiresAtMillis, int maxHits) {
            this.token = token;
            this.tokenId = tokenId;
            this.nonce = nonce;
            this.expiresAtMillis = expiresAtMillis;
            this.maxHits = maxHits;
        }

        /**
         * @return 完整令牌串 {@code base64url(payload).base64url(signature)}
         */
        public String getToken() {
            return token;
        }

        /**
         * @return 令牌 ID（审计用）
         */
        public String getTokenId() {
            return tokenId;
        }

        /**
         * @return 防重放随机串
         */
        public String getNonce() {
            return nonce;
        }

        /**
         * @return 过期时间（epoch 毫秒）
         */
        public long getExpiresAtMillis() {
            return expiresAtMillis;
        }

        /**
         * @return 最大命中次数
         */
        public int getMaxHits() {
            return maxHits;
        }
    }
}
