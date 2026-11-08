package com.houjiang.plugin.artifact.token;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.houjiang.commons.constant.PluginDistErrorCode;
import com.houjiang.mall.entity.artifact.PluginDownloadToken;
import com.houjiang.mall.mapper.artifact.PluginDownloadTokenMapper;
import com.houjiang.plugin.artifact.config.ArtifactProperties;
import com.houjiang.plugin.artifact.exception.DownloadTokenException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;

/**
 * 下载令牌服务单测：签发 / 验签 / 篡改 / 过期 / 租户不匹配 / 限次 / nonce 重放。
 *
 * <p>Mapper 用一个内存 List 做假实现，让「签发落库 → 回源按 nonce 取回 → 限次自增」
 * 这条真实链路跑起来，而不是只断言「调用了某个方法」——
 * 只 mock 返回值会漏掉「留痕行与载荷对不上」这类真会出问题的分支。
 *
 * @author Amos
 * @since M2
 */
class DownloadTokenServiceTest {

    private static final String SECRET = "unit-test-download-token-secret";

    private ArtifactProperties properties;

    private PluginDownloadTokenMapper tokenMapper;

    private DownloadTokenService service;

    /**
     * 模拟 hou_plugin_download_token 表。
     */
    private final List<PluginDownloadToken> rows = new ArrayList<>();

    @BeforeEach
    void setUp() {
        properties = new ArtifactProperties();
        properties.setDownloadTokenSecret(SECRET);
        properties.setDownloadTokenTtlSeconds(600L);
        properties.setDownloadTokenMaxHits(5);
        tokenMapper = mock(PluginDownloadTokenMapper.class);
        rows.clear();

        when(tokenMapper.insert(any(PluginDownloadToken.class))).thenAnswer(invocation -> {
            PluginDownloadToken row = invocation.getArgument(0);
            rows.add(row);
            return 1;
        });
        when(tokenMapper.selectByNonce(anyString())).thenAnswer(invocation ->
                rows.isEmpty() ? null : rows.get(rows.size() - 1));
        // 默认「限次自增成功」；需要模拟超限时由各用例自行覆盖
        when(tokenMapper.consumeHit(anyString())).thenReturn(1);

        service = new DownloadTokenService(properties, tokenMapper);
    }

    @Test
    void issueAndVerifyRoundTrip() {
        DownloadTokenService.IssuedToken issued = service.issue(command("org-a"));

        assertNotNull(issued.getToken());
        assertTrue(issued.getToken().contains("."), "令牌必须是 载荷.签名 两段");
        assertEquals(1, rows.size(), "签发必须落一条留痕，供限次与审计使用");
        assertEquals(issued.getNonce(), rows.get(0).getNonce());
        assertEquals(0, rows.get(0).getHits());

        DownloadTokenPayload payload = service.verify(issued.getToken(), "org-a");
        assertEquals("aid-1", payload.getArtifactId());
        assertEquals("coupon-demo", payload.getPluginCode());
        assertEquals("1.0.0", payload.getVersion());
        assertEquals("org-a", payload.getTenantId());
        assertEquals(42L, payload.getUserId());
        assertEquals(5, payload.getMaxHits());
        assertEquals(issued.getNonce(), payload.getNonce());
        verify(tokenMapper).consumeHit(issued.getNonce());
    }

    @Test
    void verifyRejectsTamperedPayload() {
        DownloadTokenService.IssuedToken issued = service.issue(command("org-a"));
        String[] parts = issued.getToken().split("\\.");
        String raw = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
        // 把 1.0.0 改成 9.9.9：想借这个令牌下另一个版本的制品
        String tamperedRaw = raw.replace("1.0.0", "9.9.9");
        String forged = encode(tamperedRaw) + "." + parts[1];

        DownloadTokenException e = assertThrows(DownloadTokenException.class,
                () -> service.verify(forged, "org-a"));
        assertEquals(PluginDistErrorCode.DOWNLOAD_TOKEN_EXPIRED, e.getCode());
    }

    @Test
    void verifyRejectsForgedTokenSignedWithAnotherSecret() {
        DownloadTokenService.IssuedToken issued = service.issue(command("org-a"));
        properties.setDownloadTokenSecret("another-secret");

        DownloadTokenException e = assertThrows(DownloadTokenException.class,
                () -> service.verify(issued.getToken(), "org-a"));
        assertEquals(PluginDistErrorCode.DOWNLOAD_TOKEN_EXPIRED, e.getCode(),
                "密钥不一致必须判无效，不能因为「结构对」就放行");
    }

    @Test
    void verifyRejectsExpiredToken() {
        properties.setDownloadTokenTtlSeconds(-1L);
        DownloadTokenService.IssuedToken issued = service.issue(command("org-a"));

        DownloadTokenException e = assertThrows(DownloadTokenException.class,
                () -> service.verify(issued.getToken(), "org-a"));
        assertEquals(PluginDistErrorCode.DOWNLOAD_TOKEN_EXPIRED, e.getCode());
    }

    @Test
    void verifyRejectsTenantMismatch() {
        DownloadTokenService.IssuedToken issued = service.issue(command("org-a"));

        DownloadTokenException e = assertThrows(DownloadTokenException.class,
                () -> service.verify(issued.getToken(), "org-b"));
        assertEquals(PluginDistErrorCode.TOKEN_TENANT_MISMATCH, e.getCode(),
                "A 站点的链接拿到 B 站点用，必须报 3008");
    }

    @Test
    void verifyRejectsWhenHitsExhausted() {
        DownloadTokenService.IssuedToken issued = service.issue(command("org-a"));
        // 模拟 hits 已达上限：原子 UPDATE 抢不到行
        when(tokenMapper.consumeHit(anyString())).thenReturn(0);

        DownloadTokenException e = assertThrows(DownloadTokenException.class,
                () -> service.verify(issued.getToken(), "org-a"));
        assertEquals(PluginDistErrorCode.DOWNLOAD_TOKEN_EXPIRED, e.getCode());
    }

    @Test
    void verifyRejectsRowPayloadMismatch() {
        DownloadTokenService.IssuedToken issued = service.issue(command("org-a"));
        // 按 nonce 定位到的留痕行，其制品却与令牌载荷不一致：
        // 相当于有人把 A 令牌的留痕挪去给 B 令牌用（例如改库或撞 nonce 后复用旧行）
        rows.get(0).setArtifactId("another-artifact");

        DownloadTokenException e = assertThrows(DownloadTokenException.class,
                () -> service.verify(issued.getToken(), "org-a"));
        assertEquals(PluginDistErrorCode.DOWNLOAD_TOKEN_EXPIRED, e.getCode());
    }

    @Test
    void verifyRejectsMissingRow() {
        DownloadTokenService.IssuedToken issued = service.issue(command("org-a"));
        rows.clear();

        DownloadTokenException e = assertThrows(DownloadTokenException.class,
                () -> service.verify(issued.getToken(), "org-a"));
        assertEquals(PluginDistErrorCode.DOWNLOAD_TOKEN_EXPIRED, e.getCode());
    }

    @Test
    void issueRetriesWithFreshNonceOnDuplicateKey() {
        AtomicInteger attempts = new AtomicInteger();
        when(tokenMapper.insert(any(PluginDownloadToken.class))).thenAnswer(invocation -> {
            if (attempts.incrementAndGet() == 1) {
                throw new DuplicateKeyException("uk_nonce 冲突");
            }
            rows.add(invocation.getArgument(0));
            return 1;
        });

        DownloadTokenService.IssuedToken issued = service.issue(command("org-a"));

        assertEquals(2, attempts.get(), "撞 uk_nonce 必须换串重签，绝不能复用旧 nonce");
        assertEquals(1, rows.size());
        assertEquals(issued.getNonce(), rows.get(0).getNonce());
        assertNotNull(service.verify(issued.getToken(), "org-a"));
    }

    @Test
    void issueFailsWhenNonceKeepsColliding() {
        when(tokenMapper.insert(any(PluginDownloadToken.class)))
                .thenThrow(new DuplicateKeyException("uk_nonce 持续冲突"));

        assertThrows(IllegalStateException.class, () -> service.issue(command("org-a")));
    }

    @Test
    void issueRejectsBlankArtifactId() {
        assertThrows(IllegalStateException.class, () -> service.issue(command("org-a", "", "1.0.0")));
    }

    @Test
    void verifyRejectsMalformedToken() {
        DownloadTokenException e = assertThrows(DownloadTokenException.class,
                () -> service.verify("not-a-token", "org-a"));
        assertEquals(PluginDistErrorCode.DOWNLOAD_TOKEN_EXPIRED, e.getCode());
    }

    @Test
    void requiresConfiguredSecret() {
        properties.setDownloadTokenSecret("");
        assertThrows(IllegalStateException.class, () -> service.issue(command("org-a")));
    }

    @Test
    void persistedRowCarriesTraceFields() {
        service.issue(command("org-a"));
        PluginDownloadToken row = rows.get(0);
        assertEquals("coupon-demo", row.getPluginCode());
        assertEquals("1.0.0", row.getVersion());
        assertEquals("aid-1", row.getArtifactId());
        assertEquals("org-a", row.getInstanceId());
        assertEquals(42L, row.getUserId());
        assertEquals("127.0.0.1", row.getClientIp());
        assertEquals("junit", row.getUserAgent());
        assertEquals(5, row.getMaxHits());
        assertTrue(row.getExpiresAt().isAfter(LocalDateTime.now().minusSeconds(1)));
    }

    private static IssueTokenCommand command(String instanceId) {
        return command(instanceId, "aid-1", "1.0.0");
    }

    private static IssueTokenCommand command(String instanceId, String artifactId, String version) {
        return new IssueTokenCommand(artifactId, "coupon-demo", version, instanceId,
                42L, "127.0.0.1", "junit");
    }

    private static String encode(String value) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }
}
