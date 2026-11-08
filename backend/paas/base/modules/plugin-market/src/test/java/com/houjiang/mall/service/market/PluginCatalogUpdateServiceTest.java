package com.houjiang.mall.service.market;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.houjiang.commons.version.PluginVersionComparator;
import com.houjiang.mall.entity.market.MarketPlugin;
import com.houjiang.mall.entity.market.MarketPluginLicense;
import com.houjiang.mall.entity.market.MarketPluginVersion;
import com.houjiang.mall.entity.plugin.MallPluginInstall;
import com.houjiang.mall.mapper.market.MarketPluginMapper;
import com.houjiang.mall.mapper.market.MarketPluginVersionMapper;
import com.houjiang.mall.mapper.plugin.MallPluginInstallMapper;
import com.houjiang.mall.model.market.CatalogUpdateItem;
import com.houjiang.mall.model.market.CatalogUpdateQuery;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 目录「有更新」判定测试（P0-10 / M5）。
 *
 * <p>重点钉死三件事：
 * <ol>
 *   <li><b>SemVer 比较</b>：{@code 1.9.0 → 1.10.0} 必须判为「有更新」；
 *       字符串比较会得出 {@code "1.10.0" < "1.9.0"}，表现为反复提示升级到旧版本；</li>
 *   <li><b>instanceId 必填</b>：漏传直接拒绝，不允许退化成「不限实例」的宽松查询；</li>
 *   <li><b>回写幂等</b>：{@code upgradable_version} 只在值变化时写库，
 *       重复调用不应产生多余的 UPDATE。</li>
 * </ol>
 *
 * @author Amos
 * @since M5
 */
class PluginCatalogUpdateServiceTest {

    private static final String INSTANCE = "cloud.houjit.com";

    private MarketPlugin plugin(String code, String licenseType, int status) {
        MarketPlugin plugin = new MarketPlugin();
        plugin.setPluginCode(code);
        plugin.setName(code);
        plugin.setLicenseType(licenseType);
        plugin.setStatus(status);
        plugin.setTrialDays(15);
        return plugin;
    }

    private MarketPluginVersion version(String pluginCode, String version, int status) {
        MarketPluginVersion row = new MarketPluginVersion();
        row.setPluginCode(pluginCode);
        row.setVersion(version);
        row.setStatus(status);
        return row;
    }

    private MallPluginInstall install(String code, String version) {
        MallPluginInstall install = new MallPluginInstall();
        install.setPluginCode(code);
        install.setInstalled(1);
        install.setEnabled(1);
        install.setVersion(version);
        return install;
    }

    private PluginCatalogUpdateService service(MarketPluginMapper pluginMapper,
                                               MarketPluginVersionMapper versionMapper,
                                               MallPluginInstallMapper installMapper,
                                               PluginLicenseService licenseService) {
        return new PluginCatalogUpdateService(pluginMapper, versionMapper, installMapper,
                licenseService, null, "9.9.9");
    }

    /**
     * 「有更新」判定与发布侧必须共用同一个 SemVer 比较器——
     * 各复制一份的后果是「发布时说合法、目录里说没更新」，这类分歧极难排查。
     */
    @Test
    void usesTheSharedSemVerComparatorFromCommons() {
        assertEquals("com.houjiang.commons.version.PluginVersionComparator",
                PluginVersionComparator.class.getName(),
                "目录侧必须引用 commons 里的同一份实现，不允许再复制一份");
        assertTrue(PluginVersionComparator.compare("1.10.0", "1.9.0") > 0);
    }

    @Test
    void upgradeDetectedBySemVerNotByStringCompare() {
        MarketPluginMapper pluginMapper = mock(MarketPluginMapper.class);
        MarketPluginVersionMapper versionMapper = mock(MarketPluginVersionMapper.class);
        MallPluginInstallMapper installMapper = mock(MallPluginInstallMapper.class);
        when(pluginMapper.selectList(any(Wrapper.class)))
                .thenReturn(List.of(plugin("mall-live", "FREE", 2)));
        when(versionMapper.selectList(any(Wrapper.class))).thenReturn(List.of(
                version("mall-live", "1.9.0", 1),
                version("mall-live", "1.10.0", 1)));
        when(installMapper.selectList(any(Wrapper.class))).thenReturn(List.of());

        List<CatalogUpdateItem> items = service(pluginMapper, versionMapper, installMapper, null)
                .catalogUpdates(INSTANCE, List.of(new CatalogUpdateQuery("mall-live", "1.9.0")));

        assertEquals(1, items.size());
        CatalogUpdateItem item = items.get(0);
        assertEquals("1.10.0", item.getLatestVersion(), "必须取 SemVer 最大值，不是「最后发布」或字符串最大");
        assertTrue(item.isUpgradable());
        assertNull(item.getBlockReason());
    }

    @Test
    void alreadyLatestIsNotUpgradable() {
        MarketPluginMapper pluginMapper = mock(MarketPluginMapper.class);
        MarketPluginVersionMapper versionMapper = mock(MarketPluginVersionMapper.class);
        MallPluginInstallMapper installMapper = mock(MallPluginInstallMapper.class);
        when(pluginMapper.selectList(any(Wrapper.class)))
                .thenReturn(List.of(plugin("mall-live", "FREE", 2)));
        when(versionMapper.selectList(any(Wrapper.class))).thenReturn(List.of(
                version("mall-live", "1.10.0", 1)));
        when(installMapper.selectList(any(Wrapper.class))).thenReturn(List.of());

        List<CatalogUpdateItem> items = service(pluginMapper, versionMapper, installMapper, null)
                .catalogUpdates(INSTANCE, List.of(new CatalogUpdateQuery("mall-live", "1.10.0")));

        assertFalse(items.get(0).isUpgradable());
        assertEquals("UP_TO_DATE", items.get(0).getBlockReason());
    }

    @Test
    void missingPluginIsReportedAsNotOnMarket() {
        MarketPluginMapper pluginMapper = mock(MarketPluginMapper.class);
        MarketPluginVersionMapper versionMapper = mock(MarketPluginVersionMapper.class);
        MallPluginInstallMapper installMapper = mock(MallPluginInstallMapper.class);
        when(pluginMapper.selectList(any(Wrapper.class))).thenReturn(List.of());
        when(installMapper.selectList(any(Wrapper.class))).thenReturn(List.of());

        List<CatalogUpdateItem> items = service(pluginMapper, versionMapper, installMapper, null)
                .catalogUpdates(INSTANCE, List.of(new CatalogUpdateQuery("ghost", "1.0.0")));

        assertEquals("NOT_ON_MARKET", items.get(0).getBlockReason());
        assertFalse(items.get(0).isUpgradable());
    }

    @Test
    void offlineProductBlocksUpgrade() {
        MarketPluginMapper pluginMapper = mock(MarketPluginMapper.class);
        MarketPluginVersionMapper versionMapper = mock(MarketPluginVersionMapper.class);
        MallPluginInstallMapper installMapper = mock(MallPluginInstallMapper.class);
        when(pluginMapper.selectList(any(Wrapper.class)))
                .thenReturn(List.of(plugin("mall-live", "FREE", 3)));
        when(installMapper.selectList(any(Wrapper.class))).thenReturn(List.of());

        List<CatalogUpdateItem> items = service(pluginMapper, versionMapper, installMapper, null)
                .catalogUpdates(INSTANCE, List.of(new CatalogUpdateQuery("mall-live", "1.0.0")));

        assertEquals("PLUGIN_NOT_PUBLISHED", items.get(0).getBlockReason());
    }

    @Test
    void noPublishedVersionBlocksUpgrade() {
        MarketPluginMapper pluginMapper = mock(MarketPluginMapper.class);
        MarketPluginVersionMapper versionMapper = mock(MarketPluginVersionMapper.class);
        MallPluginInstallMapper installMapper = mock(MallPluginInstallMapper.class);
        when(pluginMapper.selectList(any(Wrapper.class)))
                .thenReturn(List.of(plugin("mall-live", "FREE", 2)));
        // 全部 offline（status=5），已发布集合为空
        when(versionMapper.selectList(any(Wrapper.class)))
                .thenReturn(List.of(version("mall-live", "1.1.0", 5)));
        when(installMapper.selectList(any(Wrapper.class))).thenReturn(List.of());

        List<CatalogUpdateItem> items = service(pluginMapper, versionMapper, installMapper, null)
                .catalogUpdates(INSTANCE, List.of(new CatalogUpdateQuery("mall-live", "1.0.0")));

        assertEquals("NO_PUBLISHED_VERSION", items.get(0).getBlockReason());
        assertNull(items.get(0).getLatestVersion());
    }

    @Test
    void oldFrameworkBlocksUpgrade() {
        MarketPluginMapper pluginMapper = mock(MarketPluginMapper.class);
        MarketPluginVersionMapper versionMapper = mock(MarketPluginVersionMapper.class);
        MallPluginInstallMapper installMapper = mock(MallPluginInstallMapper.class);
        MarketPlugin plugin = plugin("mall-live", "FREE", 2);
        plugin.setMinFrameworkVersion("2.0");
        when(pluginMapper.selectList(any(Wrapper.class))).thenReturn(List.of(plugin));
        when(versionMapper.selectList(any(Wrapper.class)))
                .thenReturn(List.of(version("mall-live", "1.1.0", 1)));
        when(installMapper.selectList(any(Wrapper.class))).thenReturn(List.of());

        PluginCatalogUpdateService service = new PluginCatalogUpdateService(
                pluginMapper, versionMapper, installMapper, null, null, "1.0.0");

        List<CatalogUpdateItem> items = service.catalogUpdates(INSTANCE,
                List.of(new CatalogUpdateQuery("mall-live", "1.0.0")));

        assertEquals("FRAMEWORK_INCOMPATIBLE", items.get(0).getBlockReason(),
                "框架版本低于要求时必须拦下，而不是让用户点了升级才在预检里失败");
        assertFalse(items.get(0).isUpgradable());
    }

    @Test
    void paidPluginWithoutLicenseIsNotPurchased() {
        MarketPluginMapper pluginMapper = mock(MarketPluginMapper.class);
        MarketPluginVersionMapper versionMapper = mock(MarketPluginVersionMapper.class);
        MallPluginInstallMapper installMapper = mock(MallPluginInstallMapper.class);
        PluginLicenseService licenseService = mock(PluginLicenseService.class);
        when(pluginMapper.selectList(any(Wrapper.class)))
                .thenReturn(List.of(plugin("mall-live", "PAID", 2)));
        when(versionMapper.selectList(any(Wrapper.class)))
                .thenReturn(List.of(version("mall-live", "1.1.0", 1)));
        when(installMapper.selectList(any(Wrapper.class))).thenReturn(List.of());
        when(licenseService.findValid(anyString(), anyString())).thenReturn(null);
        when(licenseService.findAny(anyString(), anyString())).thenReturn(null);

        List<CatalogUpdateItem> items = service(pluginMapper, versionMapper, installMapper, licenseService)
                .catalogUpdates(INSTANCE, List.of(new CatalogUpdateQuery("mall-live", "1.0.0")));

        assertEquals("NOT_PURCHASED", items.get(0).getBlockReason());
        verify(licenseService).findValid("mall-live", INSTANCE);
    }

    @Test
    void expiredAndRevokedLicensesAreDistinguished() {
        MarketPluginMapper pluginMapper = mock(MarketPluginMapper.class);
        MarketPluginVersionMapper versionMapper = mock(MarketPluginVersionMapper.class);
        MallPluginInstallMapper installMapper = mock(MallPluginInstallMapper.class);
        when(pluginMapper.selectList(any(Wrapper.class)))
                .thenReturn(List.of(plugin("mall-live", "PAID", 2)));
        when(versionMapper.selectList(any(Wrapper.class)))
                .thenReturn(List.of(version("mall-live", "1.1.0", 1)));
        when(installMapper.selectList(any(Wrapper.class))).thenReturn(List.of());

        PluginLicenseService expiredCase = mock(PluginLicenseService.class);
        MarketPluginLicense expired = new MarketPluginLicense();
        expired.setStatus(1);
        when(expiredCase.findValid(anyString(), anyString())).thenReturn(null);
        when(expiredCase.findAny(anyString(), anyString())).thenReturn(expired);
        assertEquals("LICENSE_EXPIRED", service(pluginMapper, versionMapper, installMapper, expiredCase)
                .catalogUpdates(INSTANCE, List.of(new CatalogUpdateQuery("mall-live", "1.0.0")))
                .get(0).getBlockReason(), "过期要能分流到「续费」，不能一律显示「未购买」");

        PluginLicenseService revokedCase = mock(PluginLicenseService.class);
        MarketPluginLicense revoked = new MarketPluginLicense();
        revoked.setStatus(0);
        when(revokedCase.findValid(anyString(), anyString())).thenReturn(null);
        when(revokedCase.findAny(anyString(), anyString())).thenReturn(revoked);
        assertEquals("LICENSE_REVOKED", service(pluginMapper, versionMapper, installMapper, revokedCase)
                .catalogUpdates(INSTANCE, List.of(new CatalogUpdateQuery("mall-live", "1.0.0")))
                .get(0).getBlockReason());
    }

    @Test
    void freePluginSkipsLicenseGate() {
        MarketPluginMapper pluginMapper = mock(MarketPluginMapper.class);
        MarketPluginVersionMapper versionMapper = mock(MarketPluginVersionMapper.class);
        MallPluginInstallMapper installMapper = mock(MallPluginInstallMapper.class);
        when(pluginMapper.selectList(any(Wrapper.class)))
                .thenReturn(List.of(plugin("mall-live", "FREE", 2)));
        when(versionMapper.selectList(any(Wrapper.class)))
                .thenReturn(List.of(version("mall-live", "1.1.0", 1)));
        when(installMapper.selectList(any(Wrapper.class))).thenReturn(List.of());

        // 授权能力未装配也不应影响免费插件
        List<CatalogUpdateItem> items = service(pluginMapper, versionMapper, installMapper, null)
                .catalogUpdates(INSTANCE, List.of(new CatalogUpdateQuery("mall-live", "1.0.0")));

        assertTrue(items.get(0).isUpgradable());
    }

    @Test
    void instanceIdIsRequired() {
        MarketPluginMapper pluginMapper = mock(MarketPluginMapper.class);
        MarketPluginVersionMapper versionMapper = mock(MarketPluginVersionMapper.class);
        MallPluginInstallMapper installMapper = mock(MallPluginInstallMapper.class);
        PluginCatalogUpdateService service = service(pluginMapper, versionMapper, installMapper, null);

        assertThrows(IllegalArgumentException.class,
                () -> service.catalogUpdates("", List.of(new CatalogUpdateQuery("mall-live", "1.0.0"))));
        assertThrows(IllegalArgumentException.class,
                () -> service.catalogUpdates(null, List.of(new CatalogUpdateQuery("mall-live", "1.0.0"))));
    }

    @Test
    void currentVersionFallsBackToInstallRow() {
        MarketPluginMapper pluginMapper = mock(MarketPluginMapper.class);
        MarketPluginVersionMapper versionMapper = mock(MarketPluginVersionMapper.class);
        MallPluginInstallMapper installMapper = mock(MallPluginInstallMapper.class);
        when(pluginMapper.selectList(any(Wrapper.class)))
                .thenReturn(List.of(plugin("mall-live", "FREE", 2)));
        when(versionMapper.selectList(any(Wrapper.class)))
                .thenReturn(List.of(version("mall-live", "1.1.0", 1)));
        when(installMapper.selectList(any(Wrapper.class)))
                .thenReturn(List.of(install("mall-live", "1.0.0")));

        List<CatalogUpdateItem> items = service(pluginMapper, versionMapper, installMapper, null)
                .catalogUpdates(INSTANCE, List.of(new CatalogUpdateQuery("mall-live", null)));

        assertEquals("1.0.0", items.get(0).getCurrentVersion(), "入参没带版本时用本地安装态回退");
        assertTrue(items.get(0).isUpgradable());
    }

    @Test
    void invalidOrUnknownCurrentVersionIsReported() {
        MarketPluginMapper pluginMapper = mock(MarketPluginMapper.class);
        MarketPluginVersionMapper versionMapper = mock(MarketPluginVersionMapper.class);
        MallPluginInstallMapper installMapper = mock(MallPluginInstallMapper.class);
        when(pluginMapper.selectList(any(Wrapper.class)))
                .thenReturn(List.of(plugin("mall-live", "FREE", 2)));
        when(versionMapper.selectList(any(Wrapper.class)))
                .thenReturn(List.of(version("mall-live", "1.1.0", 1)));
        when(installMapper.selectList(any(Wrapper.class))).thenReturn(List.of());

        assertEquals("CURRENT_VERSION_INVALID", service(pluginMapper, versionMapper, installMapper, null)
                .catalogUpdates(INSTANCE, List.of(new CatalogUpdateQuery("mall-live", "v1")))
                .get(0).getBlockReason());
        assertEquals("CURRENT_VERSION_UNKNOWN", service(pluginMapper, versionMapper, installMapper, null)
                .catalogUpdates(INSTANCE, List.of(new CatalogUpdateQuery("mall-live", null)))
                .get(0).getBlockReason());
    }

    @Test
    void writeBackIsIdempotent() {
        MarketPluginMapper pluginMapper = mock(MarketPluginMapper.class);
        MarketPluginVersionMapper versionMapper = mock(MarketPluginVersionMapper.class);
        MallPluginInstallMapper installMapper = mock(MallPluginInstallMapper.class);
        when(pluginMapper.selectList(any(Wrapper.class)))
                .thenReturn(List.of(plugin("mall-live", "FREE", 2)));
        when(versionMapper.selectList(any(Wrapper.class)))
                .thenReturn(List.of(version("mall-live", "1.1.0", 1)));
        MallPluginInstall row = install("mall-live", "1.0.0");
        when(installMapper.selectList(any(Wrapper.class))).thenReturn(List.of(row));

        PluginCatalogUpdateService service = service(pluginMapper, versionMapper, installMapper, null);
        List<CatalogUpdateQuery> queries = List.of(new CatalogUpdateQuery("mall-live", "1.0.0"));
        service.catalogUpdates(INSTANCE, queries);
        service.catalogUpdates(INSTANCE, queries);
        service.catalogUpdates(INSTANCE, queries);

        // 三次调用只写一次：值没变化就不产生 UPDATE（幂等，不会刷新 update_time）
        verify(installMapper, times(1)).updateById(any(MallPluginInstall.class));
        assertEquals("1.1.0", row.getUpgradableVersion());
    }

    @Test
    void noUpdateClearsCachedUpgradableVersion() {
        MarketPluginMapper pluginMapper = mock(MarketPluginMapper.class);
        MarketPluginVersionMapper versionMapper = mock(MarketPluginVersionMapper.class);
        MallPluginInstallMapper installMapper = mock(MallPluginInstallMapper.class);
        when(pluginMapper.selectList(any(Wrapper.class)))
                .thenReturn(List.of(plugin("mall-live", "FREE", 2)));
        when(versionMapper.selectList(any(Wrapper.class)))
                .thenReturn(List.of(version("mall-live", "1.1.0", 1)));
        MallPluginInstall row = install("mall-live", "1.1.0");
        row.setUpgradableVersion("1.1.0");
        when(installMapper.selectList(any(Wrapper.class))).thenReturn(List.of(row));

        service(pluginMapper, versionMapper, installMapper, null)
                .catalogUpdates(INSTANCE, List.of(new CatalogUpdateQuery("mall-live", "1.1.0")));

        assertNull(row.getUpgradableVersion(), "已是最新时必须把缓存列清空，否则目录页一直显示「有更新」");
        verify(installMapper, times(1)).updateById(any(MallPluginInstall.class));
    }

    @Test
    void emptyInputReturnsEmptyResult() {
        MarketPluginMapper pluginMapper = mock(MarketPluginMapper.class);
        MarketPluginVersionMapper versionMapper = mock(MarketPluginVersionMapper.class);
        MallPluginInstallMapper installMapper = mock(MallPluginInstallMapper.class);

        List<CatalogUpdateItem> items = service(pluginMapper, versionMapper, installMapper, null)
                .catalogUpdates(INSTANCE, List.of());

        assertTrue(items.isEmpty());
        verify(pluginMapper, never()).selectList(any(Wrapper.class));
    }

    // ------------------------------------------------------------------
    // 依赖校验（M5b）：与升级前 /precheck 的 3015 同口径
    // ------------------------------------------------------------------

    private MallPluginInstall installState(String code, String version, int installed, int enabled) {
        MallPluginInstall install = new MallPluginInstall();
        install.setPluginCode(code);
        install.setInstalled(installed);
        install.setEnabled(enabled);
        install.setVersion(version);
        return install;
    }

    /**
     * 安装态 Mapper 会被查两次：第一次是本次入参的安装态，第二次是依赖的批量查询。
     * 这里用 Mockito 的连续打桩固定顺序（实现里就是先 installMap 再 dependencyInstallMap）。
     */
    private void givenInstallQueries(MallPluginInstallMapper installMapper,
                                     List<MallPluginInstall> requested,
                                     List<MallPluginInstall> dependencies) {
        when(installMapper.selectList(any(Wrapper.class))).thenReturn(requested, dependencies);
    }

    private void givenUpgradablePlugin(MarketPluginMapper pluginMapper,
                                       MarketPluginVersionMapper versionMapper,
                                       MarketPlugin plugin) {
        when(pluginMapper.selectList(any(Wrapper.class))).thenReturn(List.of(plugin));
        when(versionMapper.selectList(any(Wrapper.class)))
                .thenReturn(List.of(version(plugin.getPluginCode(), "1.1.0", 1)));
    }

    @Test
    void missingDependencyBlocksUpgrade() {
        MarketPluginMapper pluginMapper = mock(MarketPluginMapper.class);
        MarketPluginVersionMapper versionMapper = mock(MarketPluginVersionMapper.class);
        MallPluginInstallMapper installMapper = mock(MallPluginInstallMapper.class);
        MarketPlugin plugin = plugin("mall-live", "FREE", 2);
        plugin.setDependenciesJson("[\"erp\"]");
        givenUpgradablePlugin(pluginMapper, versionMapper, plugin);
        // erp 既不在本次入参里、库里也没有安装记录
        givenInstallQueries(installMapper, List.of(install("mall-live", "1.0.0")), List.of());

        List<CatalogUpdateItem> items = service(pluginMapper, versionMapper, installMapper, null)
                .catalogUpdates(INSTANCE, List.of(new CatalogUpdateQuery("mall-live", "1.0.0")));

        assertEquals("DEPENDENCY_MISSING", items.get(0).getBlockReason(),
                "缺依赖时不能报「可升级」，否则用户点了升级会被预检的 3015 打回");
        assertFalse(items.get(0).isUpgradable());
    }

    @Test
    void dependencyInstalledButDisabledBlocksUpgrade() {
        MarketPluginMapper pluginMapper = mock(MarketPluginMapper.class);
        MarketPluginVersionMapper versionMapper = mock(MarketPluginVersionMapper.class);
        MallPluginInstallMapper installMapper = mock(MallPluginInstallMapper.class);
        MarketPlugin plugin = plugin("mall-live", "FREE", 2);
        plugin.setDependenciesJson("[\"erp\"]");
        givenUpgradablePlugin(pluginMapper, versionMapper, plugin);
        givenInstallQueries(installMapper, List.of(install("mall-live", "1.0.0")),
                List.of(installState("erp", "1.0.0", 1, 0)));

        List<CatalogUpdateItem> items = service(pluginMapper, versionMapper, installMapper, null)
                .catalogUpdates(INSTANCE, List.of(new CatalogUpdateQuery("mall-live", "1.0.0")));

        assertEquals("DEPENDENCY_MISSING", items.get(0).getBlockReason(), "装了但停用与没装等价");
    }

    @Test
    void dependencyVersionBelowRequiredBlocksUpgrade() {
        MarketPluginMapper pluginMapper = mock(MarketPluginMapper.class);
        MarketPluginVersionMapper versionMapper = mock(MarketPluginVersionMapper.class);
        MallPluginInstallMapper installMapper = mock(MallPluginInstallMapper.class);
        MarketPlugin plugin = plugin("mall-live", "FREE", 2);
        plugin.setDependenciesJson("[\"erp@2.0.0\"]");
        givenUpgradablePlugin(pluginMapper, versionMapper, plugin);
        givenInstallQueries(installMapper, List.of(install("mall-live", "1.0.0")),
                List.of(installState("erp", "1.9.0", 1, 1)));

        List<CatalogUpdateItem> items = service(pluginMapper, versionMapper, installMapper, null)
                .catalogUpdates(INSTANCE, List.of(new CatalogUpdateQuery("mall-live", "1.0.0")));

        assertEquals("DEPENDENCY_MISSING", items.get(0).getBlockReason());
    }

    @Test
    void dependencyVersionSatisfiedAllowsUpgrade() {
        MarketPluginMapper pluginMapper = mock(MarketPluginMapper.class);
        MarketPluginVersionMapper versionMapper = mock(MarketPluginVersionMapper.class);
        MallPluginInstallMapper installMapper = mock(MallPluginInstallMapper.class);
        MarketPlugin plugin = plugin("mall-live", "FREE", 2);
        plugin.setDependenciesJson("[\"erp@2.0.0\"]");
        givenUpgradablePlugin(pluginMapper, versionMapper, plugin);
        givenInstallQueries(installMapper, List.of(install("mall-live", "1.0.0")),
                List.of(installState("erp", "2.1.0", 1, 1)));

        List<CatalogUpdateItem> items = service(pluginMapper, versionMapper, installMapper, null)
                .catalogUpdates(INSTANCE, List.of(new CatalogUpdateQuery("mall-live", "1.0.0")));

        assertTrue(items.get(0).isUpgradable(), "依赖满足时不能误拦");
        assertNull(items.get(0).getBlockReason());
    }

    @Test
    void invalidDependenciesJsonIsTreatedAsNoDependency() {
        MarketPluginMapper pluginMapper = mock(MarketPluginMapper.class);
        MarketPluginVersionMapper versionMapper = mock(MarketPluginVersionMapper.class);
        MallPluginInstallMapper installMapper = mock(MallPluginInstallMapper.class);
        MarketPlugin plugin = plugin("mall-live", "FREE", 2);
        plugin.setDependenciesJson("{不是数组");
        givenUpgradablePlugin(pluginMapper, versionMapper, plugin);
        when(installMapper.selectList(any(Wrapper.class))).thenReturn(List.of(install("mall-live", "1.0.0")));

        List<CatalogUpdateItem> items = service(pluginMapper, versionMapper, installMapper, null)
                .catalogUpdates(INSTANCE, List.of(new CatalogUpdateQuery("mall-live", "1.0.0")));

        assertTrue(items.get(0).isUpgradable(), "声明写错不等于缺依赖（弱校验口径）");
        // 解析不出依赖时不应再多查一次库
        verify(installMapper, times(1)).selectList(any(Wrapper.class));
    }

    /**
     * 判定顺序是「框架 → 授权 → 依赖」：授权是「能不能用」的前提，依赖是「装上后能不能跑」的前提。
     * 未购买却先弹「缺依赖」，会把续费这个转化入口盖掉。
     */
    @Test
    void licenseIsJudgedBeforeDependency() {
        MarketPluginMapper pluginMapper = mock(MarketPluginMapper.class);
        MarketPluginVersionMapper versionMapper = mock(MarketPluginVersionMapper.class);
        MallPluginInstallMapper installMapper = mock(MallPluginInstallMapper.class);
        PluginLicenseService licenseService = mock(PluginLicenseService.class);
        MarketPlugin plugin = plugin("mall-live", "PAID", 2);
        plugin.setDependenciesJson("[\"erp\"]");
        givenUpgradablePlugin(pluginMapper, versionMapper, plugin);
        givenInstallQueries(installMapper, List.of(install("mall-live", "1.0.0")), List.of());
        when(licenseService.findValid(anyString(), anyString())).thenReturn(null);
        when(licenseService.findAny(anyString(), anyString())).thenReturn(null);

        List<CatalogUpdateItem> items = service(pluginMapper, versionMapper, installMapper, licenseService)
                .catalogUpdates(INSTANCE, List.of(new CatalogUpdateQuery("mall-live", "1.0.0")));

        assertEquals("NOT_PURCHASED", items.get(0).getBlockReason(),
                "未购买必须报「未购买」而不是「缺依赖」，否则续费入口被盖掉");
        // 被授权拦下时根本不该去查依赖：installMapper.selectList 只有「入参安装态」那一次
        verify(installMapper, times(1)).selectList(any(Wrapper.class));
    }

    @Test
    void dependencyIsStillCheckedWhenLicenseIsValid() {
        MarketPluginMapper pluginMapper = mock(MarketPluginMapper.class);
        MarketPluginVersionMapper versionMapper = mock(MarketPluginVersionMapper.class);
        MallPluginInstallMapper installMapper = mock(MallPluginInstallMapper.class);
        PluginLicenseService licenseService = mock(PluginLicenseService.class);
        MarketPlugin plugin = plugin("mall-live", "PAID", 2);
        plugin.setDependenciesJson("[\"erp\"]");
        givenUpgradablePlugin(pluginMapper, versionMapper, plugin);
        givenInstallQueries(installMapper, List.of(install("mall-live", "1.0.0")), List.of());
        when(licenseService.findValid(anyString(), anyString())).thenReturn(new MarketPluginLicense());

        List<CatalogUpdateItem> items = service(pluginMapper, versionMapper, installMapper, licenseService)
                .catalogUpdates(INSTANCE, List.of(new CatalogUpdateQuery("mall-live", "1.0.0")));

        assertEquals("DEPENDENCY_MISSING", items.get(0).getBlockReason(),
                "授权有效后依赖仍然要判，顺序调整不等于把依赖判定砍掉");
        verify(installMapper, times(2)).selectList(any(Wrapper.class));
    }

    @Test
    void dependenciesAreQueriedInOneBatchNotPerItem() {
        MarketPluginMapper pluginMapper = mock(MarketPluginMapper.class);
        MarketPluginVersionMapper versionMapper = mock(MarketPluginVersionMapper.class);
        MallPluginInstallMapper installMapper = mock(MallPluginInstallMapper.class);
        MarketPlugin first = plugin("plugin-a", "FREE", 2);
        first.setDependenciesJson("[\"erp\"]");
        MarketPlugin second = plugin("plugin-b", "FREE", 2);
        second.setDependenciesJson("[\"erp\"]");
        MarketPlugin third = plugin("plugin-c", "FREE", 2);
        third.setDependenciesJson("[\"erp\"]");
        when(pluginMapper.selectList(any(Wrapper.class))).thenReturn(List.of(first, second, third));
        when(versionMapper.selectList(any(Wrapper.class)))
                .thenReturn(List.of(version("plugin-a", "1.1.0", 1)));
        givenInstallQueries(installMapper,
                List.of(install("plugin-a", "1.0.0"), install("plugin-b", "1.0.0"),
                        install("plugin-c", "1.0.0")),
                List.of());

        service(pluginMapper, versionMapper, installMapper, null).catalogUpdates(INSTANCE, List.of(
                new CatalogUpdateQuery("plugin-a", "1.0.0"),
                new CatalogUpdateQuery("plugin-b", "1.0.0"),
                new CatalogUpdateQuery("plugin-c", "1.0.0")));

        // 依赖必须一次批量查完：入参安装态 1 次 + 依赖批量 1 次，与插件个数无关
        verify(installMapper, times(2)).selectList(any(Wrapper.class));
    }

    @Test
    void dependencyStatePrefersTheOneFromRequestBatch() {
        MarketPluginMapper pluginMapper = mock(MarketPluginMapper.class);
        MarketPluginVersionMapper versionMapper = mock(MarketPluginVersionMapper.class);
        MallPluginInstallMapper installMapper = mock(MallPluginInstallMapper.class);
        MarketPlugin plugin = plugin("mall-live", "FREE", 2);
        plugin.setDependenciesJson("[\"erp@2.0.0\"]");
        givenUpgradablePlugin(pluginMapper, versionMapper, plugin);
        // erp 就在本次入参里（站点把它也上报了），已装 2.5.0 满足要求
        when(installMapper.selectList(any(Wrapper.class)))
                .thenReturn(List.of(install("mall-live", "1.0.0"), install("erp", "2.5.0")));

        List<CatalogUpdateItem> items = service(pluginMapper, versionMapper, installMapper, null)
                .catalogUpdates(INSTANCE, List.of(
                        new CatalogUpdateQuery("mall-live", "1.0.0"),
                        new CatalogUpdateQuery("erp", "2.5.0")));

        assertEquals("1.1.0", items.get(0).getLatestVersion());
        assertTrue(items.get(0).isUpgradable(), "同一批请求里带着的依赖态优先，不需要再查一次");
        verify(installMapper, times(1)).selectList(any(Wrapper.class));
    }
}
