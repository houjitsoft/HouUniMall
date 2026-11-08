package com.houjiang.plugin.artifact.authorization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.houjiang.commons.constant.PluginDistErrorCode;
import com.houjiang.mall.entity.market.MarketPlugin;
import com.houjiang.mall.entity.market.MarketPluginLicense;
import com.houjiang.mall.entity.market.MarketPluginVersion;
import com.houjiang.mall.mapper.market.MarketPluginLicenseMapper;
import com.houjiang.mall.mapper.market.MarketPluginMapper;
import com.houjiang.mall.mapper.market.MarketPluginVersionMapper;
import com.houjiang.mall.service.market.PluginLicenseService;
import com.houjiang.plugin.artifact.identity.PluginIdentity;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 下载授权矩阵单测（方案 §6.3）。
 *
 * <p>覆盖 6 个判定态 + 2 个 fail-closed 分支：
 * 免费放行 / 付费有效放行 / 未购买 3002 / 授权过期 3003 / 授权吊销 3004 /
 * 已下架 3005 / 版本未发布 3006，外加「制品不存在 3009」与「付费但无站点实例 3002」。
 *
 * <p>{@link PluginLicenseService} 是 mock：这里只验证门控的判定顺序与错误码选择，
 * 授权本身的查询逻辑属于既有实现，M2 不改动它，也不在这里重复测。
 *
 * @author Amos
 * @since M2
 */
class PluginDownloadAuthorizationServiceTest {

    private MarketPluginVersionMapper versionMapper;

    private MarketPluginMapper pluginMapper;

    private PluginLicenseService licenseService;

    private MarketPluginLicenseMapper licenseMapper;

    private PluginDownloadAuthorizationService service;

    @BeforeEach
    void setUp() {
        versionMapper = mock(MarketPluginVersionMapper.class);
        pluginMapper = mock(MarketPluginMapper.class);
        licenseService = mock(PluginLicenseService.class);
        licenseMapper = mock(MarketPluginLicenseMapper.class);
        service = new PluginDownloadAuthorizationService(
                versionMapper, pluginMapper, licenseService, licenseMapper);
    }

    @Test
    void freePluginAllowedAfterLogin() {
        givenVersion(1);
        givenPlugin("FREE");

        DownloadDecision decision = service.authorize("coupon-demo", "1.0.0", identity("org-a"));

        assertTrue(decision.isAllowed());
        assertEquals(0, decision.getCode());
    }

    @Test
    void trialPluginRequiresValidLicense() {
        givenVersion(1);
        givenPlugin("TRIAL");
        when(licenseService.findValid("coupon-demo", "org-a")).thenReturn(license(1, null));

        assertTrue(service.authorize("coupon-demo", "1.0.0", identity("org-a")).isAllowed());
    }

    @Test
    void paidPluginAllowedWithValidLicense() {
        givenVersion(1);
        givenPlugin("PAID");
        when(licenseService.findValid("coupon-demo", "org-a"))
                .thenReturn(license(1, LocalDateTime.now().plusDays(30)));

        DownloadDecision decision = service.authorize("coupon-demo", "1.0.0", identity("org-a"));

        assertTrue(decision.isAllowed());
    }

    @Test
    void paidPluginDeniedWhenNoLicenseAtAll() {
        givenVersion(1);
        givenPlugin("PAID");
        when(licenseService.findValid("coupon-demo", "org-a")).thenReturn(null);
        when(licenseMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        DownloadDecision decision = service.authorize("coupon-demo", "1.0.0", identity("org-a"));

        assertFalse(decision.isAllowed());
        assertEquals(PluginDistErrorCode.NOT_PURCHASED, decision.getCode());
    }

    @Test
    void paidPluginDeniedWhenLicenseExpired() {
        givenVersion(1);
        givenPlugin("PAID");
        when(licenseService.findValid("coupon-demo", "org-a")).thenReturn(null);
        when(licenseMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(license(1, LocalDateTime.now().minusDays(1)));

        DownloadDecision decision = service.authorize("coupon-demo", "1.0.0", identity("org-a"));

        assertFalse(decision.isAllowed());
        assertEquals(PluginDistErrorCode.LICENSE_EXPIRED, decision.getCode(),
                "过期与未购买动作不同（续费 vs 购买），错误码必须区分");
    }

    @Test
    void paidPluginDeniedWhenLicenseRevoked() {
        givenVersion(1);
        givenPlugin("PAID");
        when(licenseService.findValid("coupon-demo", "org-a")).thenReturn(null);
        when(licenseMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(license(0, LocalDateTime.now().plusDays(30)));

        DownloadDecision decision = service.authorize("coupon-demo", "1.0.0", identity("org-a"));

        assertFalse(decision.isAllowed());
        assertEquals(PluginDistErrorCode.LICENSE_REVOKED, decision.getCode());
    }

    @Test
    void offlineVersionDeniedBeforeLicenseCheck() {
        givenVersion(5);
        givenPlugin("PAID");

        DownloadDecision decision = service.authorize("coupon-demo", "1.0.0", identity("org-a"));

        assertFalse(decision.isAllowed());
        assertEquals(PluginDistErrorCode.PLUGIN_OR_VERSION_OFFLINE, decision.getCode());
    }

    @Test
    void unpublishedVersionDenied() {
        givenVersion(0);
        givenPlugin("FREE");

        DownloadDecision decision = service.authorize("coupon-demo", "1.0.0", identity("org-a"));

        assertFalse(decision.isAllowed());
        assertEquals(PluginDistErrorCode.VERSION_NOT_PUBLISHED, decision.getCode());
    }

    @Test
    void pendingVersionDenied() {
        givenVersion(3);
        givenPlugin("FREE");

        DownloadDecision decision = service.authorize("coupon-demo", "1.0.0", identity("org-a"));

        assertEquals(PluginDistErrorCode.VERSION_NOT_PUBLISHED, decision.getCode());
    }

    @Test
    void rejectedVersionDenied() {
        givenVersion(4);
        givenPlugin("FREE");

        assertEquals(PluginDistErrorCode.VERSION_NOT_PUBLISHED,
                service.authorize("coupon-demo", "1.0.0", identity("org-a")).getCode());
    }

    @Test
    void missingVersionDenied() {
        when(versionMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        DownloadDecision decision = service.authorize("coupon-demo", "9.9.9", identity("org-a"));

        assertFalse(decision.isAllowed());
        assertEquals(PluginDistErrorCode.ARTIFACT_NOT_FOUND, decision.getCode());
    }

    @Test
    void paidPluginWithoutInstanceIdFailsClosed() {
        givenVersion(1);
        givenPlugin("PAID");

        DownloadDecision decision = service.authorize("coupon-demo", "1.0.0", identity(""));

        assertFalse(decision.isAllowed());
        assertEquals(PluginDistErrorCode.NOT_PURCHASED, decision.getCode(),
                "站点实例缺失时 findValid 会退化成「不限实例」，必须拦掉");
    }

    @Test
    void missingPluginRowFailsClosed() {
        givenVersion(1);
        when(pluginMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        DownloadDecision decision = service.authorize("coupon-demo", "1.0.0", identity("org-a"));
        assertFalse(decision.isAllowed());
        assertEquals(PluginDistErrorCode.NOT_PURCHASED, decision.getCode());
    }

    @Test
    void unknownLicenseTypeNeverBecomesAFreeProduct() {
        givenVersion(1);
        for (String type : new String[] {null, "", "PAID_TYPO"}) {
            givenPlugin(type);
            assertFalse(service.authorize("coupon-demo", "1.0.0", identity("org-a")).isAllowed());
        }
    }

    @Test
    void blankParametersDenied() {
        DownloadDecision decision = service.authorize("  ", "1.0.0", identity("org-a"));
        assertFalse(decision.isAllowed());
        assertEquals(PluginDistErrorCode.ARTIFACT_NOT_FOUND, decision.getCode());
        assertNull(decision.getVersion());
    }

    private void givenVersion(int status) {
        MarketPluginVersion version = new MarketPluginVersion();
        version.setId(1L);
        version.setPluginCode("coupon-demo");
        version.setVersion("1.0.0");
        version.setStatus(status);
        when(versionMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(version);
    }

    private void givenPlugin(String licenseType) {
        MarketPlugin plugin = new MarketPlugin();
        plugin.setId(1L);
        plugin.setPluginCode("coupon-demo");
        plugin.setLicenseType(licenseType);
        when(pluginMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(plugin);
    }

    private static MarketPluginLicense license(int status, LocalDateTime expireTime) {
        MarketPluginLicense license = new MarketPluginLicense();
        license.setId(1L);
        license.setPluginCode("coupon-demo");
        license.setInstanceId("org-a");
        license.setStatus(status);
        license.setExpireTime(expireTime);
        return license;
    }

    private static PluginIdentity identity(String instanceId) {
        return new PluginIdentity(instanceId, 42L);
    }
}
