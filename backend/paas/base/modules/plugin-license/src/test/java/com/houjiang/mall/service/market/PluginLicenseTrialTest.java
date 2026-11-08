package com.houjiang.mall.service.market;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.houjiang.mall.entity.market.MarketPluginLicense;
import com.houjiang.mall.mapper.market.MarketPluginLicenseMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 试用期起算测试（M5，方案 §7.3）。
 *
 * <p>钉死四条规则：
 * <ol>
 *   <li>TRIAL 签发时 <b>不预先扣时间</b>（{@code expire_time = NULL}）；</li>
 *   <li>首次安装成功才写 {@code first_install_time} 并回填 {@code expire_time}；</li>
 *   <li><b>卸载重装不重置</b>试用期（否则等于无限续期）；</li>
 *   <li>过期的 ACTIVATED 授权能被批量迁移为 EXPIRED。</li>
 * </ol>
 *
 * @author Amos
 * @since M5
 */
class PluginLicenseTrialTest {

    private static final String PLUGIN = "mall-activity";

    private static final String INSTANCE = "cloud.houjit.com";

    private MarketPluginLicense license(String type, Integer expireInDays) {
        MarketPluginLicense license = new MarketPluginLicense();
        license.setId(1L);
        license.setPluginCode(PLUGIN);
        license.setInstanceId(INSTANCE);
        license.setLicenseType(type);
        license.setLicenseKey("HOU-TEST-KEY");
        license.setStatus(1);
        license.setExpireTime(expireInDays == null ? null : LocalDateTime.now().plusDays(expireInDays));
        return license;
    }

    /**
     * 让 {@code findValid} 命中给定授权（install 门控与 markFirstInstall 都走它）。
     */
    private MarketPluginLicenseMapper mapperReturning(MarketPluginLicense license) {
        MarketPluginLicenseMapper mapper = mock(MarketPluginLicenseMapper.class);
        when(mapper.selectOne(any(Wrapper.class))).thenReturn(license);
        return mapper;
    }

    @Test
    void trialIssueDoesNotPrechargeExpiry() {
        MarketPluginLicenseMapper mapper = mock(MarketPluginLicenseMapper.class);
        PluginLicenseService service = new PluginLicenseService(mapper);

        MarketPluginLicense issued = service.issue(PLUGIN, 1L, INSTANCE, "TRIAL", 15);

        assertEquals("TRIAL", issued.getLicenseType());
        assertNull(issued.getExpireTime(),
                "试用期不能从签发起算：买了没装却在倒计时是典型投诉源");
        assertEquals(PluginLicenseService.STATE_ISSUED, issued.getState());
        assertNull(issued.getFirstInstallTime());
    }

    @Test
    void paidIssueIncludesTwelveMonthMaintenanceAndPerpetualRuntime() {
        MarketPluginLicenseMapper mapper = mock(MarketPluginLicenseMapper.class);
        PluginLicenseService service = new PluginLicenseService(mapper);

        MarketPluginLicense issued = service.issue(PLUGIN, 1L, INSTANCE, "PAID", 365);

        assertNull(issued.getExpireTime(), "维护到期不能停止已交付版本");
        assertNotNull(issued.getMaintenanceUntil());
        assertTrue(issued.getMaintenanceUntil().isAfter(LocalDateTime.now().plusMonths(11)));
        assertEquals(PluginLicenseService.STATE_ISSUED, issued.getState());
    }

    @Test
    void markFirstInstallStartsTrialClock() {
        MarketPluginLicense stored = license("TRIAL", null);
        List<MarketPluginLicense> updated = new ArrayList<>();
        MarketPluginLicenseMapper mapper = mapperReturning(stored);
        when(mapper.updateById(any(MarketPluginLicense.class))).thenAnswer(invocation -> {
            updated.add(invocation.getArgument(0));
            return 1;
        });
        PluginLicenseService service = new PluginLicenseService(mapper);

        LocalDateTime before = LocalDateTime.now();
        MarketPluginLicense result = service.markFirstInstall(PLUGIN, INSTANCE, 15);

        assertNotNull(result.getFirstInstallTime());
        assertEquals(PluginLicenseService.STATE_ACTIVATED, result.getState());
        assertNotNull(result.getBindTime());
        assertNotNull(result.getExpireTime(), "首次安装后必须回填过期时间");
        assertTrue(!result.getExpireTime().isBefore(before.plusDays(14)), "应为首次安装 + 15 天");
        assertTrue(!result.getExpireTime().isAfter(before.plusDays(16)));
        assertEquals(1, updated.size());
    }

    @Test
    void reinstallDoesNotResetTrialClock() {
        MarketPluginLicense stored = license("TRIAL", null);
        MarketPluginLicenseMapper mapper = mapperReturning(stored);
        PluginLicenseService service = new PluginLicenseService(mapper);
        service.markFirstInstall(PLUGIN, INSTANCE, 15);
        LocalDateTime firstInstall = stored.getFirstInstallTime();
        LocalDateTime expire = stored.getExpireTime();

        // 模拟「卸载 → 重装」：十天后再次调用，起算点与过期时间都不能动
        MarketPluginLicense again = service.markFirstInstall(PLUGIN, INSTANCE, 15);

        assertSame(stored, again);
        assertEquals(firstInstall, again.getFirstInstallTime(), "卸载重装不能重置试用期，否则等于无限续期");
        assertEquals(expire, again.getExpireTime());
    }

    @Test
    void markFirstInstallLeavesPaidExpiryUntouched() {
        MarketPluginLicense stored = license("PAID", 365);
        LocalDateTime originalExpiry = stored.getExpireTime();
        MarketPluginLicenseMapper mapper = mapperReturning(stored);
        PluginLicenseService service = new PluginLicenseService(mapper);

        MarketPluginLicense result = service.markFirstInstall(PLUGIN, INSTANCE, 15);

        assertNotNull(result.getFirstInstallTime(), "买断授权也要记录首次安装时间（用于统计与排障）");
        assertEquals(originalExpiry, result.getExpireTime(), "买断授权的过期时间不由试用天数改写");
    }

    @Test
    void markFirstInstallRequiresInstanceAndValidLicense() {
        MarketPluginLicenseMapper mapper = mapperReturning(license("TRIAL", null));
        PluginLicenseService service = new PluginLicenseService(mapper);

        assertThrows(IllegalArgumentException.class,
                () -> service.markFirstInstall(PLUGIN, "", 15), "没有站点身份就不该起算");
        assertThrows(IllegalArgumentException.class,
                () -> service.markFirstInstall("", INSTANCE, 15));

        MarketPluginLicenseMapper emptyMapper = mock(MarketPluginLicenseMapper.class);
        when(emptyMapper.selectOne(any(Wrapper.class))).thenReturn(null);
        PluginLicenseService emptyService = new PluginLicenseService(emptyMapper);
        assertThrows(IllegalStateException.class,
                () -> emptyService.markFirstInstall(PLUGIN, INSTANCE, 15), "无有效授权不允许起算");
    }

    @Test
    void migrateExpiredTransitionsActivatedLicenses() {
        MarketPluginLicense expired = license("TRIAL", -1);
        expired.setState(PluginLicenseService.STATE_ACTIVATED);
        MarketPluginLicense stillValid = license("TRIAL", 5);
        stillValid.setState(PluginLicenseService.STATE_ACTIVATED);
        MarketPluginLicense notActivated = license("TRIAL", -1);
        notActivated.setState(PluginLicenseService.STATE_ISSUED);

        // SQL 负责筛出「ACTIVATED 且已过期」的行；这里把三类行一起交给服务，
        // 验证即使查询条件被改坏，服务层也不会误伤未到期 / 未激活的授权。
        List<MarketPluginLicense> rows = new ArrayList<>(List.of(expired, stillValid, notActivated));
        MarketPluginLicenseMapper mapper = mock(MarketPluginLicenseMapper.class);
        when(mapper.selectList(any(Wrapper.class))).thenReturn(rows);
        PluginLicenseService service = new PluginLicenseService(mapper);

        int migrated = service.migrateExpired(LocalDateTime.now(), 100);

        assertEquals(1, migrated);
        assertEquals(PluginLicenseService.STATE_EXPIRED, expired.getState());
        assertEquals(PluginLicenseService.STATE_ACTIVATED, stillValid.getState(), "未到期不能被迁移");
        assertEquals(PluginLicenseService.STATE_ISSUED, notActivated.getState(), "未激活不能被迁移");
    }

    @Test
    void migrateExpiredReturnsZeroWhenNothingExpired() {
        MarketPluginLicenseMapper mapper = mock(MarketPluginLicenseMapper.class);
        when(mapper.selectList(any(Wrapper.class))).thenReturn(List.of());
        PluginLicenseService service = new PluginLicenseService(mapper);

        assertEquals(0, service.migrateExpired(LocalDateTime.now(), 100));
        assertEquals(0, service.migrateExpired(LocalDateTime.now(), 0), "limit<=0 应视为不迁移");
    }

    @Test
    void unbindAndRevokeUpdateStateMachine() {
        MarketPluginLicense stored = license("PAID", 365);
        MarketPluginLicenseMapper mapper = mock(MarketPluginLicenseMapper.class);
        when(mapper.selectOne(any(Wrapper.class))).thenReturn(stored);
        PluginLicenseService service = new PluginLicenseService(mapper);

        service.unbind("HOU-TEST-KEY", INSTANCE);
        assertEquals(PluginLicenseService.STATE_UNBOUND, stored.getState(), "解绑不等于作废，授权仍可再用");
        assertNull(stored.getInstanceId());

        service.revoke("HOU-TEST-KEY");
        assertEquals(PluginLicenseService.STATE_REVOKED, stored.getState());
        assertEquals(0, stored.getStatus());
    }

    @Test
    void activateMovesIssuedToBound() {
        MarketPluginLicense stored = license("PAID", 365);
        stored.setState(PluginLicenseService.STATE_ISSUED);
        stored.setBindTime(null);
        MarketPluginLicenseMapper mapper = mock(MarketPluginLicenseMapper.class);
        when(mapper.selectOne(any(Wrapper.class))).thenReturn(stored);
        PluginLicenseService service = new PluginLicenseService(mapper);

        MarketPluginLicense result = service.activate(PLUGIN, "HOU-TEST-KEY", INSTANCE);

        assertEquals(PluginLicenseService.STATE_BOUND, result.getState(),
                "绑定实例只是生命周期一步，ACTIVATED 要等首次安装成功");
        assertNotNull(result.getBindTime());
    }

    /**
     * {@code findAny} 的价值：<b>不校验有效性</b>，因此能查到 {@code findValid} 查不到的行。
     *
     * <p>注意：{@code findValid} 的过期过滤由 SQL 的 {@code expire_time > now} 条件完成，
     * 在纯单测里被 mock 掉了，这里只钉 {@code findAny} 的语义与 fail-closed 边界。
     */
    @Test
    void findAnyDistinguishesExpiredFromNotPurchased() {
        MarketPluginLicense stored = license("PAID", -1);
        MarketPluginLicenseMapper mapper = mock(MarketPluginLicenseMapper.class);
        when(mapper.selectOne(any(Wrapper.class))).thenReturn(stored);
        PluginLicenseService service = new PluginLicenseService(mapper);

        assertNotNull(service.findAny(PLUGIN, INSTANCE),
                "已过期的行也必须能查到，目录页才能区分「过期（续费）」与「未购买（购买）」");
        assertNull(service.findAny(PLUGIN, ""), "instanceId 为空时拒绝查询（fail-closed）");
        assertNull(service.findAny("", INSTANCE));
    }
}
