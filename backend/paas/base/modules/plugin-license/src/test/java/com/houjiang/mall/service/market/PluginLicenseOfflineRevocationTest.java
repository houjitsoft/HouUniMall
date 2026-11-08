package com.houjiang.mall.service.market;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.houjiang.commons.event.PluginVersionOfflineEvent;
import com.houjiang.mall.entity.market.MarketPluginLicense;
import com.houjiang.mall.mapper.market.MarketPluginLicenseMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 下架联动吊销 + 过期扫描原语（M5b，方案 §7.4 / §7.3）。
 *
 * <p>钉死四条规则：
 * <ol>
 *   <li>下架联动只吊销<b>仍有效</b>的授权，且必须写下 {@code revoke_reason}；</li>
 *   <li><b>只吊销不卸载</b>——授权模块不碰安装态；</li>
 *   <li>监听器异常不外溢（下架不能被联动拖垮）；</li>
 *   <li>过期 / 续费扫描原语只查只写，行为可被心跳编排复用。</li>
 * </ol>
 *
 * @author Amos
 * @since M5b
 */
class PluginLicenseOfflineRevocationTest {

    private static final String PLUGIN = "mall-activity";

    private MarketPluginLicense license(Long id, int status, String state, LocalDateTime expireTime) {
        MarketPluginLicense license = new MarketPluginLicense();
        license.setId(id);
        license.setPluginCode(PLUGIN);
        license.setLicenseKey("HOU-KEY-" + id);
        license.setLicenseType("PAID");
        license.setStatus(status);
        license.setState(state);
        license.setExpireTime(expireTime);
        return license;
    }

    @Test
    void revokeAllRevokesOnlyValidLicensesAndWritesReason() {
        MarketPluginLicense valid = license(1L, 1, PluginLicenseService.STATE_ACTIVATED, null);
        MarketPluginLicense alreadyRevoked = license(2L, 0, PluginLicenseService.STATE_REVOKED, null);
        MarketPluginLicenseMapper mapper = mock(MarketPluginLicenseMapper.class);
        when(mapper.selectList(any(Wrapper.class))).thenReturn(List.of(valid, alreadyRevoked));
        PluginLicenseService service = new PluginLicenseService(mapper);

        int revoked = service.revokeAll(PLUGIN, "EMERGENCY_OFFLINE: 安全漏洞");

        assertEquals(1, revoked, "已吊销的不重复处理");
        assertEquals(0, valid.getStatus());
        assertEquals(PluginLicenseService.STATE_REVOKED, valid.getState());
        assertEquals("EMERGENCY_OFFLINE: 安全漏洞", valid.getRevokeReason());
        verify(mapper, never()).updateById(alreadyRevoked);
    }

    @Test
    void revokeAllRejectsBlankPluginCode() {
        PluginLicenseService service = new PluginLicenseService(mock(MarketPluginLicenseMapper.class));
        assertThrows(IllegalArgumentException.class, () -> service.revokeAll("  ", "x"));
    }

    @Test
    void revokeWithReasonWritesRevokeReason() {
        MarketPluginLicense stored = license(1L, 1, PluginLicenseService.STATE_BOUND, null);
        MarketPluginLicenseMapper mapper = mock(MarketPluginLicenseMapper.class);
        when(mapper.selectOne(any(Wrapper.class))).thenReturn(stored);
        PluginLicenseService service = new PluginLicenseService(mapper);

        service.revoke("HOU-KEY-1", "退款");

        assertEquals("退款", stored.getRevokeReason());
        assertEquals(PluginLicenseService.STATE_REVOKED, stored.getState());
    }

    @Test
    void listenerRetainsPurchasedRuntimeOnCatalogWithdrawal() {
        MarketPluginLicenseMapper mapper = mock(MarketPluginLicenseMapper.class);
        MarketPluginLicense stored = license(1L, 1, PluginLicenseService.STATE_ACTIVATED, null);
        when(mapper.selectList(any(Wrapper.class))).thenReturn(List.of(stored));
        PluginLicenseService service = new PluginLicenseService(mapper);

        new PluginVersionOfflineListener(service).onVersionOffline(
                new PluginVersionOfflineEvent(this, PLUGIN, "1.2.0", "安全漏洞", LocalDateTime.now()));

        assertEquals(PluginLicenseService.STATE_ACTIVATED, stored.getState());
        assertNull(stored.getRevokeReason());
        verify(mapper,never()).updateById(any(MarketPluginLicense.class));
    }

    @Test
    void listenerSwallowsFailures() {
        PluginLicenseService failing = mock(PluginLicenseService.class);
        doThrow(new IllegalStateException("模拟授权库故障"))
                .when(failing).revokeAll(any(), any());

        // 联动失败不能外溢：下架是主流程，授权还有心跳兜底
        new PluginVersionOfflineListener(failing).onVersionOffline(
                new PluginVersionOfflineEvent(this, PLUGIN, "1.2.0", null, LocalDateTime.now()));

        verify(failing,never()).revokeAll(any(),any());
    }

    @Test
    void listenerIgnoresBlankEvents() {
        PluginLicenseService service = mock(PluginLicenseService.class);
        PluginVersionOfflineListener listener = new PluginVersionOfflineListener(service);

        listener.onVersionOffline(null);
        listener.onVersionOffline(new PluginVersionOfflineEvent(this, "  ", "1.0.0", null, LocalDateTime.now()));

        verify(service, never()).revokeAll(any(), any());
    }

    @Test
    void collectExpiredSkipsFutureAndNonActivated() {
        LocalDateTime now = LocalDateTime.now();
        MarketPluginLicense expired = license(1L, 1, PluginLicenseService.STATE_ACTIVATED, now.minusDays(1)); expired.setLicenseType("TRIAL");
        MarketPluginLicense stillValid = license(2L, 1, PluginLicenseService.STATE_ACTIVATED, now.plusDays(1)); stillValid.setLicenseType("TRIAL");
        MarketPluginLicense notActivated = license(3L, 1, PluginLicenseService.STATE_ISSUED, now.minusDays(1)); notActivated.setLicenseType("TRIAL");
        MarketPluginLicenseMapper mapper = mock(MarketPluginLicenseMapper.class);
        when(mapper.selectList(any(Wrapper.class)))
                .thenReturn(List.of(expired, stillValid, notActivated));
        PluginLicenseService service = new PluginLicenseService(mapper);

        List<MarketPluginLicense> expiredRows = service.collectExpired(now, 100);

        assertEquals(1, expiredRows.size());
        assertEquals(1L, expiredRows.get(0).getId());
    }

    @Test
    void markExpiredRecordsHeartbeat() {
        MarketPluginLicenseMapper mapper = mock(MarketPluginLicenseMapper.class);
        MarketPluginLicense stored = license(1L, 1, PluginLicenseService.STATE_ACTIVATED, LocalDateTime.now());
        PluginLicenseService service = new PluginLicenseService(mapper);
        LocalDateTime now = LocalDateTime.now();

        service.markExpired(stored, now);

        assertEquals(PluginLicenseService.STATE_EXPIRED, stored.getState());
        assertEquals(now, stored.getLastHeartbeat());
        assertEquals(1, stored.getStatus(), "过期只改生命周期状态，不动 status（findValid 的语义靠 expire_time 判断）");
    }

    @Test
    void collectRenewedPicksExpiredRowsThatBecameValidAgain() {
        LocalDateTime now = LocalDateTime.now();
        MarketPluginLicense renewed = license(1L, 1, PluginLicenseService.STATE_EXPIRED, now.plusDays(30));
        MarketPluginLicense stillExpired = license(2L, 1, PluginLicenseService.STATE_EXPIRED, now.minusDays(1));
        MarketPluginLicenseMapper mapper = mock(MarketPluginLicenseMapper.class);
        when(mapper.selectList(any(Wrapper.class))).thenReturn(List.of(renewed, stillExpired));
        PluginLicenseService service = new PluginLicenseService(mapper);

        List<MarketPluginLicense> rows = service.collectRenewed(now, 100);

        assertEquals(1, rows.size());
        assertEquals(1L, rows.get(0).getId());
    }

    @Test
    void markRenewedRestoresActivatedAndHeartbeat() {
        MarketPluginLicenseMapper mapper = mock(MarketPluginLicenseMapper.class);
        MarketPluginLicense stored = license(1L, 1, PluginLicenseService.STATE_EXPIRED, LocalDateTime.now().plusDays(1));
        PluginLicenseService service = new PluginLicenseService(mapper);
        LocalDateTime now = LocalDateTime.now();

        service.markRenewed(stored, now);

        assertEquals(PluginLicenseService.STATE_ACTIVATED, stored.getState());
        assertEquals(now, stored.getLastHeartbeat());
    }

    @Test
    void migrateExpiredStillWorksAsBefore() {
        LocalDateTime now = LocalDateTime.now();
        MarketPluginLicense expired = license(1L, 1, PluginLicenseService.STATE_ACTIVATED, now.minusDays(1));
        expired.setLicenseType("TRIAL");
        MarketPluginLicenseMapper mapper = mock(MarketPluginLicenseMapper.class);
        when(mapper.selectList(any(Wrapper.class))).thenReturn(new ArrayList<>(List.of(expired)));
        PluginLicenseService service = new PluginLicenseService(mapper);

        assertEquals(1, service.migrateExpired(now, 100));
        assertEquals(PluginLicenseService.STATE_EXPIRED, expired.getState());
    }

    @Test
    void collectWithInvalidArgumentsReturnsEmpty() {
        PluginLicenseService service = new PluginLicenseService(mock(MarketPluginLicenseMapper.class));
        assertTrue(service.collectExpired(null, 10).isEmpty());
        assertTrue(service.collectExpired(LocalDateTime.now(), 0).isEmpty());
        assertTrue(service.collectRenewed(null, 10).isEmpty());
        assertTrue(service.collectRenewed(LocalDateTime.now(), 0).isEmpty());
    }
}
