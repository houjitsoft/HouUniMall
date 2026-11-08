package com.houjiang.mall.service.market;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.houjiang.mall.entity.market.MarketPluginLicense;
import com.houjiang.mall.mapper.market.MarketPluginLicenseMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 插件授权服务单测：签发 / 激活 / 校验 / 吊销 + 安装门控。
 *
 * <p>这是「收费安装」的底座：付费插件能否被安装，取决于本服务能否查到有效授权。
 *
 * @author Amos
 */
class PluginLicenseServiceTest {

    private MarketPluginLicense license(String code, String type, Integer status, LocalDateTime expireTime) {
        MarketPluginLicense license = new MarketPluginLicense();
        license.setId(1L);
        license.setPluginCode(code);
        license.setBuyerId(1L);
        license.setLicenseKey("HOU-WEBPOS-0123456789ABCDEF");
        license.setLicenseType(type);
        license.setMaxInstances(1);
        license.setStatus(status);
        license.setExpireTime(expireTime);
        return license;
    }

    private MarketPluginLicenseMapper mapperReturning(MarketPluginLicense license) {
        MarketPluginLicenseMapper mapper = mock(MarketPluginLicenseMapper.class);
        when(mapper.selectOne(any(Wrapper.class))).thenReturn(license);
        return mapper;
    }

    @Test void paidMaintenanceExpirationDoesNotPreventActivation() {
        var paid=license("erp","PAID",1,LocalDateTime.now().minusDays(1));
        paid.setMaintenanceUntil(LocalDateTime.now().minusDays(1));
        var service=new PluginLicenseService(mapperReturning(paid));
        assertNotNull(service.activate("erp",paid.getLicenseKey(),"site-1"));
        assertEquals(1,paid.getStatus());
    }

    @Test
    void requiresLicenseOnlyForPaidAndTrial() {
        assertTrue(PluginLicenseService.requiresLicense("PAID"));
        assertTrue(PluginLicenseService.requiresLicense("paid"));
        assertTrue(PluginLicenseService.requiresLicense("TRIAL"));
        assertFalse(PluginLicenseService.requiresLicense("FREE"));
        assertFalse(PluginLicenseService.requiresLicense(null));
        assertFalse(PluginLicenseService.requiresLicense("  "));
    }

    @Test
    void assertInstallablePassesWhenValidLicenseExists() {
        PluginLicenseService service = new PluginLicenseService(
                mapperReturning(license("mall-webpos", "PAID", 1, null)));

        MarketPluginLicense hit = service.assertInstallable("mall-webpos");

        assertNotNull(hit);
        assertEquals("mall-webpos", hit.getPluginCode());
    }

    @Test
    void assertInstallableRejectsWhenNoLicense() {
        PluginLicenseService service = new PluginLicenseService(mapperReturning(null));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.assertInstallable("mall-webpos"));

        assertTrue(ex.getMessage().contains("需先购买并激活授权"), "实际=" + ex.getMessage());
        assertTrue(ex.getMessage().contains("mall-webpos"), "实际=" + ex.getMessage());
    }

    @Test
    void findValidReturnsNullForBlankCode() {
        PluginLicenseService service = new PluginLicenseService(mapperReturning(
                license("mall-webpos", "PAID", 1, null)));

        assertNull(service.findValid(null));
        assertNull(service.findValid("  "));
    }

    @Test
    void issueGeneratesKeyAndPermanentLicenseWhenNoValidDays() {
        MarketPluginLicenseMapper mapper = mock(MarketPluginLicenseMapper.class);
        PluginLicenseService service = new PluginLicenseService(mapper);

        MarketPluginLicense issued = service.issue("mall-webpos", null, null, null, null);

        assertTrue(issued.getLicenseKey().startsWith("HOU-MALLWEBPOS-"), "实际=" + issued.getLicenseKey());
        assertEquals("PAID", issued.getLicenseType());
        assertEquals(1L, issued.getBuyerId());
        assertEquals(1, issued.getStatus());
        assertNull(issued.getExpireTime(), "validDays 为空应为永久授权");
        verify(mapper).insert(any(MarketPluginLicense.class));
    }

    /**
     * TRIAL 签发<b>不预先扣时间</b>（M5，方案 §7.3）。
     *
     * <p>旧断言是「TRIAL + validDays=15 → expire_time = 15 天后」，与方案 §7.3
     * 「试用期以首次成功安装为起点」直接冲突，因此本次按方案改正：
     * 签发时 {@code expire_time} 为 NULL，等 {@code markFirstInstall()} 回填。
     * 否则会出现「买了没装却在倒计时」——这是方案里点名的典型投诉源。
     */
    @Test
    void issueKeepsTrialExpiryEmptyUntilFirstInstall() {
        PluginLicenseService service = new PluginLicenseService(mock(MarketPluginLicenseMapper.class));

        MarketPluginLicense issued = service.issue("mall-activity", 2L, "cloud.houjit.com", "TRIAL", 15);

        assertEquals("TRIAL", issued.getLicenseType());
        assertEquals(2L, issued.getBuyerId());
        assertEquals("cloud.houjit.com", issued.getInstanceId());
        assertNull(issued.getExpireTime(), "TRIAL 不预先扣时间，试用期从首次安装起算");
        assertEquals(PluginLicenseService.STATE_ISSUED, issued.getState());
    }

    @Test
    void paidPurchaseKeepsRuntimePerpetualRegardlessOfLegacyValidDays() {
        PluginLicenseService service = new PluginLicenseService(mock(MarketPluginLicenseMapper.class));

        MarketPluginLicense issued = service.issue("mall-activity", 2L, "cloud.houjit.com", "PAID", 15);

        assertNull(issued.getExpireTime());
        assertNotNull(issued.getMaintenanceUntil());
        assertTrue(issued.getMaintenanceUntil().isAfter(LocalDateTime.now().plusMonths(11)));
    }

    @Test
    void issueRejectsFreeAndUnknownType() {
        PluginLicenseService service = new PluginLicenseService(mock(MarketPluginLicenseMapper.class));

        assertThrows(IllegalArgumentException.class,
                () -> service.issue("mall-points", null, null, "FREE", null));
        assertThrows(IllegalArgumentException.class,
                () -> service.issue("mall-points", null, null, "WHATEVER", null));
        assertThrows(IllegalArgumentException.class,
                () -> service.issue("  ", null, null, "PAID", null));
    }

    @Test
    void activateBindsInstance() {
        MarketPluginLicenseMapper mapper = mapperReturning(license("mall-webpos", "PAID", 1, null));
        PluginLicenseService service = new PluginLicenseService(mapper);

        MarketPluginLicense activated = service.activate("mall-webpos", "HOU-WEBPOS-0123456789ABCDEF",
                "cloud.houjit.com");

        assertEquals("cloud.houjit.com", activated.getInstanceId());
        verify(mapper).updateById(any(MarketPluginLicense.class));
    }

    @Test
    void activateRejectsUnknownRevokedExpiredAndMismatched() {
        PluginLicenseService unknown = new PluginLicenseService(mapperReturning(null));
        assertThrows(IllegalArgumentException.class,
                () -> unknown.activate("mall-webpos", "HOU-NOPE", null));

        PluginLicenseService revoked = new PluginLicenseService(
                mapperReturning(license("mall-webpos", "PAID", 0, null)));
        IllegalStateException revokedEx = assertThrows(IllegalStateException.class,
                () -> revoked.activate("mall-webpos", "HOU-WEBPOS-0123456789ABCDEF", null));
        assertTrue(revokedEx.getMessage().contains("已吊销"), "实际=" + revokedEx.getMessage());

        PluginLicenseService expired = new PluginLicenseService(mapperReturning(
                license("mall-webpos", "TRIAL", 1, LocalDateTime.now().minusDays(1))));
        IllegalStateException expiredEx = assertThrows(IllegalStateException.class,
                () -> expired.activate("mall-webpos", "HOU-WEBPOS-0123456789ABCDEF", null));
        assertTrue(expiredEx.getMessage().contains("已过期"), "实际=" + expiredEx.getMessage());

        PluginLicenseService mismatched = new PluginLicenseService(
                mapperReturning(license("mall-webpos", "PAID", 1, null)));
        IllegalArgumentException mismatchEx = assertThrows(IllegalArgumentException.class,
                () -> mismatched.activate("mall-activity", "HOU-WEBPOS-0123456789ABCDEF", null));
        assertTrue(mismatchEx.getMessage().contains("不匹配"), "实际=" + mismatchEx.getMessage());
    }

    @Test
    void activateRejectsInstanceConflict() {
        MarketPluginLicense bound = license("mall-webpos", "PAID", 1, null);
        bound.setInstanceId("a.houjit.com");
        PluginLicenseService service = new PluginLicenseService(mapperReturning(bound));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.activate("mall-webpos", "HOU-WEBPOS-0123456789ABCDEF", "b.houjit.com"));

        assertTrue(ex.getMessage().contains("已绑定其它实例"), "实际=" + ex.getMessage());
    }

    @Test
    void revokeMarksLicenseInvalid() {
        MarketPluginLicenseMapper mapper = mapperReturning(license("mall-webpos", "PAID", 1, null));
        PluginLicenseService service = new PluginLicenseService(mapper);

        service.revoke("HOU-WEBPOS-0123456789ABCDEF");

        verify(mapper).updateById(any(MarketPluginLicense.class));
    }

    @Test
    void revokeRejectsUnknownKey() {
        PluginLicenseService service = new PluginLicenseService(mapperReturning(null));

        assertThrows(IllegalArgumentException.class, () -> service.revoke("HOU-NOPE"));
        assertThrows(IllegalArgumentException.class, () -> service.revoke("  "));
    }

    @Test
    void statusOfReportsLicensedAndPermanent() {
        PluginLicenseService licensed = new PluginLicenseService(
                mapperReturning(license("mall-webpos", "PAID", 1, null)));

        assertEquals(true, licensed.statusOf("mall-webpos").get("licensed"));
        assertEquals(true, licensed.statusOf("mall-webpos").get("permanent"));
        assertEquals("PAID", licensed.statusOf("mall-webpos").get("licenseType"));

        PluginLicenseService unlicensed = new PluginLicenseService(mapperReturning(null));
        assertEquals(false, unlicensed.statusOf("mall-webpos").get("licensed"));
        assertNull(unlicensed.statusOf("mall-webpos").get("licenseKey"));
    }

    @Test
    void generateKeyNormalizesPluginCode() {
        PluginLicenseService service = new PluginLicenseService(mock(MarketPluginLicenseMapper.class));

        MarketPluginLicense issued = service.issue("mall.webpos-v2", null, null, "PAID", 0);

        assertTrue(issued.getLicenseKey().startsWith("HOU-MALLWEBPOSV2-"), "实际=" + issued.getLicenseKey());
        assertFalse(issued.getLicenseKey().contains("."), "授权码不应含点号");
    }
}
