package com.houjiang.mall.service.plugin;

import com.baomidou.mybatisplus.annotation.TableName;
import com.houjiang.mall.entity.market.MarketPlugin;
import com.houjiang.mall.entity.market.MarketPluginLicense;
import com.houjiang.mall.entity.plugin.AddonPlugin;
import com.houjiang.mall.entity.plugin.MallPluginInstall;
import com.houjiang.mall.mapper.market.MarketPluginMapper;
import com.houjiang.mall.mapper.plugin.AddonCateMapper;
import com.houjiang.mall.mapper.plugin.AddonPluginMapper;
import com.houjiang.mall.mapper.plugin.MallPluginInstallMapper;
import com.houjiang.mall.model.plugin.PluginCatalogItem;
import com.houjiang.mall.model.plugin.PluginCenterItem;
import com.houjiang.mall.service.market.PluginLicenseService;
import com.houjiang.plugin.engine.config.PluginEngineProperties;
import com.houjiang.plugin.engine.core.PluginLifecycleManager;
import com.houjiang.plugin.engine.spi.PluginMenuPort;
import org.springframework.test.util.ReflectionTestUtils;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class PluginCenterServiceTest {

    @Test
    void pluginCatalogComesFromUnifiedAddonRepository() {
        boolean hasAddonCatalogMapper = Arrays.stream(PluginCenterService.class.getDeclaredFields())
                .anyMatch(field -> "AddonPluginMapper".equals(field.getType().getSimpleName()));

        assertTrue(hasAddonCatalogMapper, "插件目录必须从 hou_addon_* 统一数据域读取");
    }

    @Test
    void installStateUsesUnifiedAddonTable() {
        TableName tableName = MallPluginInstall.class.getAnnotation(TableName.class);

        assertNotNull(tableName);
        assertEquals("hou_addon_plugin_install", tableName.value());
    }

    @Test
    void buildItemsMarksCatalogPluginsAsNotInstalledWhenNoInstallStateExists() {
        PluginCenterService service = new PluginCenterService(null);

        List<PluginCenterItem> items = service.buildItems(List.of(), Map.of());

        // 38 = 25 个既有目录项 + 13 个 activity 拆分插件（2026-10-19 并入静态 CATALOG）。
        // 数量断言用于防止目录项被误删；新增插件时同步更新此数。
        assertEquals(38, items.size());
        PluginCenterItem points = find(items, "mall-points");
        assertFalse(points.getInstalled());
        assertFalse(points.getEnabled());
        assertEquals("积分商城", points.getName());
        assertEquals("营销", points.getCategory());
        PluginCenterItem advertising = find(items, "mall-advertising");
        assertFalse(advertising.getInstalled());
        assertFalse(advertising.getEnabled());
        assertEquals("广告管理", advertising.getName());
    }

    @Test
    void buildItemsMergesInstallStateAndConfig() {
        PluginCenterService service = new PluginCenterService(null);
        MallPluginInstall install = new MallPluginInstall();
        install.setPluginCode("mall-live");
        install.setInstalled(1);
        install.setEnabled(1);
        install.setVersion("1.0.0");
        install.setConfigJson("{\"autoAudit\":false}");

        List<PluginCenterItem> items = service.buildItems(List.of(install), Map.of());

        PluginCenterItem live = find(items, "mall-live");
        assertTrue(live.getInstalled());
        assertTrue(live.getEnabled());
        assertEquals("{\"autoAudit\":false}", live.getConfigJson());
    }

    @Test
    void pluginCenterReadsPriceAndPurchaseStateFromMarketData() {
        MallPluginInstallMapper installMapper = mock(MallPluginInstallMapper.class);
        MarketPluginMapper marketMapper = mock(MarketPluginMapper.class);
        PluginLicenseService licenseService = mock(PluginLicenseService.class);

        MarketPlugin marketPlugin = new MarketPlugin();
        marketPlugin.setPluginCode("erp");
        marketPlugin.setLicenseType("PAID");
        marketPlugin.setPriceCents(3000000);
        marketPlugin.setStatus(2);
        when(installMapper.selectList(null)).thenReturn(List.of());
        when(marketMapper.selectList(null)).thenReturn(List.of(marketPlugin));
        when(licenseService.listValid()).thenReturn(List.of());

        PluginCenterService service = new PluginCenterService(
                installMapper, null, null, null, null, marketMapper, licenseService);

        PluginCenterItem item = service.listPlugins(Map.of()).stream()
                .filter(candidate -> "erp".equals(candidate.getCode()))
                .findFirst()
                .orElseThrow();

        assertEquals("PAID", item.getLicenseType());
        assertEquals(3000000, item.getPriceCents());
        assertEquals("￥30000.00", item.getPriceText());
        assertEquals("NOT_PURCHASED", item.getPurchaseStatus());
        assertFalse(item.getLicensed());
    }

    @Test
    void pluginCenterMarksLicensedMarketPluginAsPurchased() {
        MallPluginInstallMapper installMapper = mock(MallPluginInstallMapper.class);
        MarketPluginMapper marketMapper = mock(MarketPluginMapper.class);
        PluginLicenseService licenseService = mock(PluginLicenseService.class);

        MarketPlugin marketPlugin = new MarketPlugin();
        marketPlugin.setPluginCode("erp");
        marketPlugin.setLicenseType("PAID");
        marketPlugin.setPriceCents(3000000);
        marketPlugin.setStatus(2);
        MarketPluginLicense license = new MarketPluginLicense();
        license.setPluginCode("erp");
        license.setLicenseType("PAID");
        when(installMapper.selectList(null)).thenReturn(List.of());
        when(marketMapper.selectList(null)).thenReturn(List.of(marketPlugin));
        when(licenseService.listValid()).thenReturn(List.of(license));

        PluginCenterService service = new PluginCenterService(
                installMapper, null, null, null, null, marketMapper, licenseService);

        PluginCenterItem item = service.listPlugins(Map.of()).stream()
                .filter(candidate -> "erp".equals(candidate.getCode()))
                .findFirst()
                .orElseThrow();

        assertEquals("PURCHASED", item.getPurchaseStatus());
        assertTrue(item.getLicensed());
    }

    @Test
    void buildItemsFiltersByKeywordCategoryAndStatus() {
        PluginCenterService service = new PluginCenterService(null);
        MallPluginInstall install = new MallPluginInstall();
        install.setPluginCode("mall-distribution");
        install.setInstalled(1);
        install.setEnabled(1);

        List<PluginCenterItem> items = service.buildItems(
                List.of(install),
                Map.of("keyword", "分销", "category", "营销", "status", "enabled")
        );

        assertEquals(1, items.size());
        assertEquals("mall-distribution", items.get(0).getCode());
    }

    @Test
    void requireCatalogItemRejectsUnknownPluginCode() {
        PluginCenterService service = new PluginCenterService(null);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.requireCatalogItem("unknown-plugin")
        );

        assertEquals("插件不存在: unknown-plugin", ex.getMessage());
    }

    @Test
    void saveConfigRejectsInvalidJsonBeforeUpdatingInstallState() {
        MallPluginInstallMapper mapper = mock(MallPluginInstallMapper.class);
        MallPluginInstall install = new MallPluginInstall();
        install.setId(1L);
        install.setPluginCode("mall-points");
        install.setInstalled(1);
        install.setEnabled(1);
        when(mapper.selectOne(any(Wrapper.class))).thenReturn(install);
        PluginCenterService service = new PluginCenterService(mapper);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.saveConfig("mall-points", "{invalid")
        );

        assertEquals("插件配置必须是合法 JSON", ex.getMessage());
        verify(mapper, never()).updateById(any(MallPluginInstall.class));
    }

    @Test
    void resolveFrontendUrlBuildsVersionedAssetUrlForEsmEntry() {
        assertEquals(
                "/web/plugin-assets/mall-coupon/1.2.0/index.mjs",
                PluginCenterService.resolveFrontendUrl("mall-coupon", "1.2.0", "index.mjs")
        );
        assertEquals(
                "/web/plugin-assets/mall-coupon/1.2.0/dist/main.js",
                PluginCenterService.resolveFrontendUrl("mall-coupon", "1.2.0", "/dist/main.js")
        );
    }

    @Test
    void resolveFrontendUrlAddsSafeRevisionQuery() {
        assertEquals(
                "/web/plugin-assets/mall-coupon/1.2.0/index.mjs?rev=abc123",
                PluginCenterService.resolveFrontendUrl("mall-coupon", "1.2.0", "index.mjs", "abc123")
        );
        assertEquals(
                "/web/plugin-assets/mall-coupon/1.2.0/index.mjs",
                PluginCenterService.resolveFrontendUrl("mall-coupon", "1.2.0", "index.mjs", "bad&rev=old")
        );
    }

    @Test
    void installedPluginUsesChecksumAsFrontendRevision(@TempDir Path pluginsDir) throws Exception {
        MallPluginInstall install = new MallPluginInstall();
        install.setPluginCode("pm");
        install.setInstalled(1);
        install.setEnabled(1);
        install.setVersion("1.0.0");
        install.setChecksum("ABCDEF0123456789");

        PluginCenterItem item = find(
                serviceWithArtifact(pluginsDir, "pm", "1.0.0").buildItems(List.of(install), Map.of()), "pm");

        assertEquals("ABCDEF0123456789", item.getFrontendRevision());
        assertEquals("/web/plugin-assets/pm/1.0.0/index.mjs?rev=ABCDEF0123456789", item.getFrontendUrl());
    }

    @Test
    void installedPluginFallsBackToUpdateTimeRevision(@TempDir Path pluginsDir) throws Exception {
        MallPluginInstall install = new MallPluginInstall();
        install.setPluginCode("pm");
        install.setInstalled(1);
        install.setEnabled(1);
        install.setVersion("1.0.0");
        install.setUpdateTime(java.time.LocalDateTime.of(2026, 9, 15, 12, 34, 56, 789000000));

        PluginCenterItem item = find(
                serviceWithArtifact(pluginsDir, "pm", "1.0.0").buildItems(List.of(install), Map.of()), "pm");

        assertEquals("20260915123456789", item.getFrontendRevision());
        assertEquals("/web/plugin-assets/pm/1.0.0/index.mjs?rev=20260915123456789", item.getFrontendUrl());
    }

    @Test
    void installedPluginWithoutArtifactDropsUrlAndRevision() {
        // 已安装 + 声明入口，但产物没落盘（13 个 activity 拆分插件的现状）：
        // URL 与 rev 都必须为空，否则后台拿一个 404 地址去 import。
        MallPluginInstall install = new MallPluginInstall();
        install.setPluginCode("goods-discount");
        install.setInstalled(1);
        install.setEnabled(1);
        install.setVersion("1.0.0");
        install.setChecksum("ABCDEF0123456789");

        PluginCenterItem item = find(
                new PluginCenterService(null).buildItems(List.of(install), Map.of()), "goods-discount");

        assertTrue(item.getInstalled());
        assertNull(item.getFrontendUrl(), "产物不存在时不得下发 frontendUrl");
        assertNull(item.getFrontendRevision(), "产物不存在时也不带 rev 指纹");
    }

    @Test
    void resolveFrontendUrlSkipsBuiltInPageEntryAndUnsafePaths() {
        assertNull(PluginCenterService.resolveFrontendUrl("mall-points", "1.0.0", "/points/index"));
        assertNull(PluginCenterService.resolveFrontendUrl("mall-points", "1.0.0", "../../etc/passwd.mjs"));
        assertNull(PluginCenterService.resolveFrontendUrl(null, "1.0.0", "index.mjs"));
    }

    @Test
    void catalogPluginsExposeFrontendUrlOnlyForEsmEntries(@TempDir Path pluginsDir) throws Exception {
        // frontendUrl 需要两个条件同时成立：目录里声明了 ESM 入口 + 产物真的落盘。
        // 只声明不落盘的插件（13 个 activity 拆分插件的现状）必须不下发，
        // 否则后台每次加载都去 import 一个 404 的 index.mjs。
        Files.createDirectories(pluginsDir.resolve("erp").resolve("1.0.0").resolve("frontend"));
        Files.writeString(pluginsDir.resolve("erp").resolve("1.0.0").resolve("frontend").resolve("index.mjs"),
                "export default {}");
        Files.createDirectories(pluginsDir.resolve("geo").resolve("1.0.0"));
        Files.writeString(pluginsDir.resolve("geo").resolve("1.0.0").resolve("index.mjs"), "export default {}");

        PluginCenterService service = new PluginCenterService(null);
        PluginEngineProperties properties = new PluginEngineProperties();
        properties.setPluginsDir(pluginsDir.toString());
        ReflectionTestUtils.setField(service, "pluginEngineProperties", properties);

        List<PluginCenterItem> items = service.buildItems(List.of(), Map.of());

        assertNotNull(find(items, "erp").getFrontendUrl(), "产物在 <ver>/frontend/ 下：应下发版本化 ESM 入口");
        assertNotNull(find(items, "geo").getFrontendUrl(), "产物与版本目录同层：应下发版本化 ESM 入口");
        assertNull(find(items, "logging").getFrontendUrl(),
                "声明了入口但版本目录下无产物：不得下发（否则后台 import 404）");
        assertNull(find(items, "supplier").getFrontendUrl(), "同上：无产物不下发");
        assertNull(find(items, "aicode").getFrontendUrl(), "同上：无产物不下发");
        assertNull(find(items, "mall-points").getFrontendUrl(),
                "内置目录插件（entryPath 为页面路径）不应返回前端产物 URL");
    }

    @Test
    void frontendArtifactOnDiskReadsFrontendEntryFromMarketJar(@TempDir Path pluginsDir) throws Exception {
        // 市场安装落盘的是 JAR 形态：<code>-<ver>.jar!/frontend/index.mjs
        Path versionDir = Files.createDirectories(pluginsDir.resolve("pm").resolve("1.0.0"));
        Path jar = versionDir.resolve("pm-1.0.0.jar");
        try (java.util.zip.ZipOutputStream out = new java.util.zip.ZipOutputStream(Files.newOutputStream(jar))) {
            out.putNextEntry(new java.util.zip.ZipEntry("frontend/index.mjs"));
            out.write("export default {}".getBytes(java.nio.charset.StandardCharsets.UTF_8));
            out.closeEntry();
        }

        PluginCenterService service = new PluginCenterService(null);
        PluginEngineProperties properties = new PluginEngineProperties();
        properties.setPluginsDir(pluginsDir.toString());
        ReflectionTestUtils.setField(service, "pluginEngineProperties", properties);

        assertTrue(service.frontendArtifactOnDisk("pm", "1.0.0", "index.mjs"), "JAR 内含 frontend/index.mjs 应判为已交付");
        assertFalse(service.frontendArtifactOnDisk("pm", "1.0.0", "other.mjs"), "JAR 内没有该条目应判为未交付");
        assertFalse(service.frontendArtifactOnDisk("goods-discount", "1.0.0", "index.mjs"), "整目录缺失应判为未交付");
        assertFalse(service.frontendArtifactOnDisk("mall-thousandGuestsPrice", "1.0.0", "index.mjs"),
                "编码含大写不符合产物端点规则，按未交付处理（否则后台拿到 400）");
    }

    @Test
    void installStateComesOnlyFromInstallTable() {
        // 安装状态唯一以 hou_addon_plugin_install 为准：即使运行健康，没有登记也不显示已安装
        //（本地插件的登记由 PluginRegistryReconciler 在启动时补齐）
        MallPluginInstallMapper installMapper = mock(MallPluginInstallMapper.class);
        PluginLifecycleManager lifecycleManager = mock(PluginLifecycleManager.class);
        when(installMapper.selectList(null)).thenReturn(List.of());
        when(lifecycleManager.isHealthy("crm")).thenReturn(true);

        PluginCenterService service = new PluginCenterService(
                installMapper, null, lifecycleManager, null, null, null, null);

        List<PluginCenterItem> items = service.listPlugins(Map.of("status", "enabled"));

        assertTrue(items.isEmpty(), "无安装记录的插件不得再靠运行健康假装已安装");
        verify(lifecycleManager, never()).isHealthy(any());
    }

    @Test
    void persistedDisabledStateWinsOverHealthyRuntimeState() {
        PluginLifecycleManager lifecycleManager = mock(PluginLifecycleManager.class);
        when(lifecycleManager.isHealthy("crm")).thenReturn(true);
        MallPluginInstall install = new MallPluginInstall();
        install.setPluginCode("crm");
        install.setInstalled(1);
        install.setEnabled(0);

        PluginCenterService service = new PluginCenterService(
                null, null, lifecycleManager, null, null, null, null);

        PluginCenterItem item = find(service.buildItems(List.of(install), Map.of()), "crm");

        assertTrue(item.getInstalled());
        assertFalse(item.getEnabled());
        verify(lifecycleManager, never()).isHealthy("crm");
    }

    @Test
    void catalogPrefersAddonTableRowsWhenPresent() {
        AddonPluginMapper addonMapper = mock(AddonPluginMapper.class);
        AddonPlugin row = new AddonPlugin();
        row.setServiceCode("mall");
        row.setPluginCode("erp");
        row.setName("ERP 企业资源计划");
        row.setCategory("Enterprise");
        row.setVersion("2.0.0");
        row.setStatus(1); // Only enabled registry rows override static metadata.
        when(addonMapper.selectList(any())).thenReturn(List.of(row));
        PluginCenterService service = new PluginCenterService(null, addonMapper, null, null, null);

        List<PluginCatalogItem> items = service.catalog();

        assertEquals(38, items.size(), "DB row overrides metadata without hiding static plugins");
        PluginCatalogItem erp = items.stream()
                .filter(item -> "erp".equals(item.getCode()))
                .findFirst()
                .orElseThrow();
        assertEquals("2.0.0", erp.getVersion());
        assertTrue(items.stream().anyMatch(item -> "geo".equals(item.getCode())));
        assertTrue(items.stream().anyMatch(item -> "mall-advertising".equals(item.getCode())));
    }

    @Test
    void catalogFallsBackToStaticWhenAddonTableQueryIsEmpty() {
        AddonPluginMapper addonMapper = mock(AddonPluginMapper.class);
        when(addonMapper.selectList(any())).thenReturn(List.of());
        PluginCenterService service = new PluginCenterService(null, addonMapper, null, null, null);

        List<PluginCatalogItem> items = service.catalog();

        assertEquals(38, items.size(), "DB 连通但查询为空时回落静态目录，避免插件中心空白");
        assertTrue(items.stream().anyMatch(item -> "erp".equals(item.getCode())));
        assertTrue(items.stream().anyMatch(item -> "mall-advertising".equals(item.getCode())));
    }

    @Test
    void uninstallKeepsPublishedAddonCatalogRow() {
        MallPluginInstallMapper installMapper = mock(MallPluginInstallMapper.class);
        MallPluginInstall install = new MallPluginInstall();
        install.setId(1L);
        install.setPluginCode("mall-points");
        install.setInstalled(1);
        install.setEnabled(1);
        when(installMapper.selectOne(any(Wrapper.class))).thenReturn(install);
        when(installMapper.updateById(any(MallPluginInstall.class))).thenReturn(1);
        AddonPluginSyncService syncService = mock(AddonPluginSyncService.class);
        PluginCenterService service = new PluginCenterService(installMapper, null, null, null, syncService);

        boolean result = service.uninstall("mall-points");

        assertTrue(result);
        assertEquals(0, install.getInstalled().intValue());
        assertEquals(0, install.getEnabled().intValue());
        verifyNoInteractions(syncService);
    }

    @Test
    void uninstallRemovesMenusWhenPluginIsInstalledButNotLoaded() {
        MallPluginInstallMapper installMapper = mock(MallPluginInstallMapper.class);
        MallPluginInstall install = new MallPluginInstall();
        install.setId(2L);
        install.setPluginCode("mall-points");
        install.setInstalled(1);
        install.setEnabled(1);
        when(installMapper.selectOne(any(Wrapper.class))).thenReturn(install);
        when(installMapper.updateById(any(MallPluginInstall.class))).thenReturn(1);
        PluginMenuPort menuPort = mock(PluginMenuPort.class);
        PluginCenterService service = new PluginCenterService(installMapper, null, null, null, null);
        ReflectionTestUtils.setField(service, "pluginMenuPort", menuPort);

        assertTrue(service.uninstall("mall-points"));
        verify(menuPort).remove("mall-points");
    }

    @Test
    void uninstallStillReclaimsMenusWhenEngineUnloadFails() {
        MallPluginInstallMapper installMapper = mock(MallPluginInstallMapper.class);
        MallPluginInstall install = new MallPluginInstall();
        install.setId(3L);
        install.setPluginCode("mall-points");
        install.setInstalled(1);
        install.setEnabled(1);
        when(installMapper.selectOne(any(Wrapper.class))).thenReturn(install);
        when(installMapper.updateById(any(MallPluginInstall.class))).thenReturn(1);
        PluginLifecycleManager lifecycleManager = mock(PluginLifecycleManager.class);
        when(lifecycleManager.isLoaded("mall-points")).thenReturn(true);
        doThrow(new IllegalStateException("engine unload failed")).when(lifecycleManager).uninstall("mall-points");
        PluginMenuPort menuPort = mock(PluginMenuPort.class);
        PluginCenterService service = new PluginCenterService(installMapper, null, lifecycleManager, null, null);
        ReflectionTestUtils.setField(service, "pluginMenuPort", menuPort);

        // 引擎卸载抛异常时：菜单回收仍必须执行（幂等必达，方案 §8.3 修缺口 G2），且卸载整体返回成功
        assertTrue(service.uninstall("mall-points"));
        verify(menuPort).remove("mall-points");
        assertEquals(0, install.getInstalled().intValue());
    }

    @Test
    void uninstallReclaimsMenusEvenWhenEnginePathWasTaken() {
        MallPluginInstallMapper installMapper = mock(MallPluginInstallMapper.class);
        MallPluginInstall install = new MallPluginInstall();
        install.setId(4L);
        install.setPluginCode("mall-points");
        install.setInstalled(1);
        install.setEnabled(1);
        when(installMapper.selectOne(any(Wrapper.class))).thenReturn(install);
        when(installMapper.updateById(any(MallPluginInstall.class))).thenReturn(1);
        PluginLifecycleManager lifecycleManager = mock(PluginLifecycleManager.class);
        when(lifecycleManager.isLoaded("mall-points")).thenReturn(true);
        PluginMenuPort menuPort = mock(PluginMenuPort.class);
        PluginCenterService service = new PluginCenterService(installMapper, null, lifecycleManager, null, null);
        ReflectionTestUtils.setField(service, "pluginMenuPort", menuPort);

        // 走了引擎卸载路径后，服务层仍要再做一次幂等 remove（收尾必达，不与引擎路径互斥）
        assertTrue(service.uninstall("mall-points"));
        verify(menuPort).remove("mall-points");
    }

    @Test
    void standaloneServicesNeverEnterEmbeddedLifecycle() {
        MallPluginInstallMapper installs = mock(MallPluginInstallMapper.class);
        PluginLifecycleManager engine = mock(PluginLifecycleManager.class);
        PluginCenterService service = new PluginCenterService(installs, null, engine, null, null);
        for (String code : List.of("erp", "oa", "crm", "mes")) {
            assertThrows(IllegalArgumentException.class, () -> service.uninstall(code));
            assertThrows(IllegalArgumentException.class, () -> service.toggle(code, true));
        }
        verifyNoInteractions(installs, engine);
    }

    // ---------- 分类数据来源：hou_addon_cate ----------

    private static Map<String, Object> cateRow(int id, String name) {
        Map<String, Object> row = new java.util.LinkedHashMap<>();
        row.put("id", id);
        row.put("name", name);
        return row;
    }

    private static Map<String, Object> menuRow(String path, int cateId) {
        Map<String, Object> row = new java.util.LinkedHashMap<>();
        row.put("paths", path);
        row.put("cate_id", cateId);
        return row;
    }

    private PluginCenterService serviceWithCate(List<Map<String, Object>> cateRows,
                                                List<Map<String, Object>> menuRows) {
        MallPluginInstallMapper installMapper = mock(MallPluginInstallMapper.class);
        when(installMapper.selectList(null)).thenReturn(List.of());
        AddonCateMapper cateMapper = mock(AddonCateMapper.class);
        when(cateMapper.selectAll()).thenReturn(cateRows);
        when(cateMapper.selectPluginCateIds(anyList())).thenReturn(menuRows);
        return new PluginCenterService(installMapper, null, null, null, null, null, null, cateMapper);
    }

    @Test
    void categoriesComeOnlyFromAddonCateTable() {
        PluginCenterService service = serviceWithCate(
                List.of(cateRow(1, "营销"), cateRow(4, "商城"), cateRow(10, "生产")),
                List.of());

        List<String> categories = service.categories();

        // 分类唯一来源是 hou_addon_cate：顺序跟表 id，目录自带、表里没有的分类文案不再混入
        assertTrue(categories.indexOf("营销") < categories.indexOf("商城"), "分类顺序跟随 hou_addon_cate.id");
        assertTrue(categories.indexOf("商城") < categories.indexOf("生产"));
        assertFalse(categories.contains("营销工具"), "表外分类不得混入: " + categories);
    }

    @Test
    void uncategorizedAppendsWithFilterableLabel() {
        PluginCenterService service = serviceWithCate(
                List.of(cateRow(4, "商城")),
                List.of(menuRow("/plugin/webpos", 4)));

        List<String> categories = service.categories();

        // aicode / supplier 等未挂分类的插件 → 末尾补「未分类」且可作为筛选项
        assertTrue(categories.contains("未分类"));
        List<PluginCenterItem> uncategorized = service.listPlugins(Map.of("category", "未分类"));
        assertTrue(uncategorized.stream().anyMatch(item -> "aicode".equals(item.getCode())));
        assertTrue(uncategorized.stream().noneMatch(item -> "webpos".equals(item.getCode())));
    }

    @Test
    void pluginCategoryFollowsPluginMenuCateId() {
        PluginCenterService service = serviceWithCate(
                List.of(cateRow(4, "商城"), cateRow(10, "生产")),
                List.of(menuRow("/plugin/webpos", 4), menuRow("/plugin/mes", 10)));

        PluginCenterItem webpos = find(service.listPlugins(Map.of()), "webpos");
        PluginCenterItem mes = find(service.listPlugins(Map.of()), "mes");

        assertEquals("商城", webpos.getCategory());
        assertEquals(4, webpos.getCateId());
        assertEquals("生产", mes.getCategory());
        assertEquals(10, mes.getCateId());
    }

    @Test
    void pluginListFiltersByBothCategoryNameAndId() {
        PluginCenterService service = serviceWithCate(
                List.of(cateRow(4, "商城")),
                List.of(menuRow("/plugin/webpos", 4)));

        // webpos（菜单关联）、orderfood 与 mall-gather（目录声明同名）都归「商城」
        assertEquals(3, service.listPlugins(Map.of("category", "商城")).size(), "按分类名过滤");
        // 按 ID 过滤只认菜单 cate_id 绑定：orderfood/mall-gather 无菜单关联，cateId 为空
        assertEquals(1, service.listPlugins(Map.of("category", "4")).size(), "按分类 ID 过滤");
        assertTrue(service.listPlugins(Map.of("category", "不存在的分类")).isEmpty());
    }

    @Test
    void categoriesFallbackToPluginOwnCategoryWhenCateTableUnavailable() {
        MallPluginInstallMapper installMapper = mock(MallPluginInstallMapper.class);
        when(installMapper.selectList(null)).thenReturn(List.of());
        AddonCateMapper cateMapper = mock(AddonCateMapper.class);
        when(cateMapper.selectAll()).thenThrow(new RuntimeException("Table 'hou_addon_cate' doesn't exist"));
        PluginCenterService service = new PluginCenterService(installMapper, null, null, null, null, null, null, cateMapper);

        List<String> categories = service.categories();

        assertFalse(categories.isEmpty(), "分类表不可用时必须退化而不是抛错");
        assertTrue(categories.contains("营销"));
        // 无分类上下文时，插件分类维持目录原值
        assertEquals("商城", find(service.listPlugins(Map.of()), "webpos").getCategory());
    }

    @Test
    void pluginOwnCateIdOverridesLegacyMenuBinding() {
        // hou_addon_plugin.cate_id 与插件位菜单的 cate_id 故意给冲突值：以插件自己的外键为准
        AddonPluginMapper pluginMapper = mock(AddonPluginMapper.class);
        when(pluginMapper.selectList(any())).thenReturn(List.of(addonPlugin("webpos", "商城", 10)));
        PluginCenterService service = serviceWithCateTable(pluginMapper,
                List.of(cateRow(4, "商城"), cateRow(10, "生产")),
                List.of(menuRow("/plugin/webpos", 4)));

        PluginCenterItem webpos = find(service.listPlugins(Map.of()), "webpos");

        assertEquals("生产", webpos.getCategory(), "迁移后插件表的外键优先于菜单绑定");
        assertEquals(10, webpos.getCateId());
    }

    @Test
    void menuFallbackOnlyFillsPluginsWithoutCateId() {
        AddonPluginMapper pluginMapper = mock(AddonPluginMapper.class);
        when(pluginMapper.selectList(any())).thenReturn(List.of(
                addonPlugin("webpos", "商城", 4), addonPlugin("mes", "生产", 10)));
        PluginCenterService service = serviceWithCateTable(pluginMapper,
                List.of(cateRow(4, "商城"), cateRow(10, "生产")),
                List.of(menuRow("/plugin/webpos", 4), menuRow("/plugin/mes", 10)));

        service.listPlugins(Map.of());

        // 已带 cate_id 的插件不该再进菜单回落查询；还没回填的静态目录项才查
        ArgumentCaptor<List<String>> paths = ArgumentCaptor.forClass(List.class);
        verify(cateMapperOf(service)).selectPluginCateIds(paths.capture());
        assertFalse(paths.getValue().contains("/plugin/webpos"), "已有外键的插件不再走菜单回落: " + paths.getValue());
        assertFalse(paths.getValue().contains("/plugin/mes"));
    }

    private AddonCateMapper cateMapperOf(PluginCenterService service) {
        return (AddonCateMapper) ReflectionTestUtils.getField(service, "addonCateMapper");
    }

    /** 带 hou_addon_cate 与插件表数据的服务；pluginMapper 为 null 时目录回落到静态 CATALOG。 */
    private PluginCenterService serviceWithCateTable(AddonPluginMapper pluginMapper,
                                                     List<Map<String, Object>> cateRows,
                                                     List<Map<String, Object>> menuRows) {
        MallPluginInstallMapper installMapper = mock(MallPluginInstallMapper.class);
        when(installMapper.selectList(null)).thenReturn(List.of());
        AddonCateMapper cateMapper = mock(AddonCateMapper.class);
        when(cateMapper.selectAll()).thenReturn(cateRows);
        when(cateMapper.selectPluginCateIds(anyList())).thenReturn(menuRows);
        return new PluginCenterService(installMapper, pluginMapper, null, null, null, null, null, cateMapper);
    }

    private static AddonPlugin addonPlugin(String code, String category, Integer cateId) {
        AddonPlugin row = new AddonPlugin();
        row.setServiceCode("mall");
        row.setPluginCode(code);
        row.setName(code);
        row.setCategory(category);
        row.setCateId(cateId);
        row.setVersion("1.0.0");
        row.setStatus(1);
        return row;
    }

    private PluginCenterItem find(List<PluginCenterItem> items, String code) {
        return items.stream()
                .filter(item -> code.equals(item.getCode()))
                .findFirst()
                .orElseThrow();
    }
    @Test
    void deploymentOutageDoesNotUninstallOrDisableDeliveredPlugin() {
        var service = new PluginCenterService(null);
        ReflectionTestUtils.setField(service, "unavailablePluginCodes", " mall-points, unknown ");
        var install = new MallPluginInstall();
        install.setPluginCode("mall-points");
        install.setInstalled(1);
        install.setEnabled(1);
        var item = find(service.buildItems(List.of(install), Map.of()), "mall-points");
        assertTrue(item.getInstalled());
        assertTrue(item.getEnabled());
        var serialized = new com.fasterxml.jackson.databind.ObjectMapper().valueToTree(item);
        assertEquals("UNAVAILABLE", serialized.path("healthStatus").asText());
        ReflectionTestUtils.setField(service, "unavailablePluginCodes", "");
        var recovered = new com.fasterxml.jackson.databind.ObjectMapper().valueToTree(
                find(service.buildItems(List.of(install), Map.of()), "mall-points"));
        assertTrue(recovered.path("healthStatus").isNull());
    }

    @Test
    void standaloneInstallFallsBackToAddonTableWhenServiceReceiptMissing() {
        PluginCenterService service = new PluginCenterService(null);
        org.springframework.jdbc.core.JdbcTemplate jdbc = mock(org.springframework.jdbc.core.JdbcTemplate.class);
        ReflectionTestUtils.setField(service, "serviceInstallJdbc", jdbc);
        when(jdbc.queryForList(contains("hou_service_installation"), eq("site-1")))
                .thenReturn(List.of(Map.of(
                        "plugin_code", "oa",
                        "version", "1.0.0",
                        "status", 1,
                        "updated_at", "2026-09-23")));

        MallPluginInstall erp = install("erp", 1, 1);
        MallPluginInstall points = install("mall-points", 1, 1);

        @SuppressWarnings("unchecked")
        List<MallPluginInstall> merged = (List<MallPluginInstall>) ReflectionTestUtils.invokeMethod(
                service, "serviceInstallations", List.of(erp, points), "site-1");

        Map<String, MallPluginInstall> byCode = merged.stream()
                .collect(java.util.stream.Collectors.toMap(MallPluginInstall::getPluginCode, item -> item));
        assertEquals(1, byCode.get("erp").getInstalled(), "无容器回执时独立服务应回落到安装表");
        assertEquals(1, byCode.get("mall-points").getInstalled());
        assertEquals(1, byCode.get("oa").getInstalled(), "有回执的独立服务仍以回执为准");
    }

    @Test
    void serviceReceiptOverridesLegacyStandaloneInstall() {
        PluginCenterService service = new PluginCenterService(null);
        org.springframework.jdbc.core.JdbcTemplate jdbc = mock(org.springframework.jdbc.core.JdbcTemplate.class);
        ReflectionTestUtils.setField(service, "serviceInstallJdbc", jdbc);
        when(jdbc.queryForList(contains("hou_service_installation"), eq("site-1")))
                .thenReturn(List.of(Map.of(
                        "plugin_code", "erp",
                        "version", "2.0.0",
                        "status", 0,
                        "updated_at", "2026-09-23")));

        @SuppressWarnings("unchecked")
        List<MallPluginInstall> merged = (List<MallPluginInstall>) ReflectionTestUtils.invokeMethod(
                service, "serviceInstallations", List.of(install("erp", 1, 1)), "site-1");

        assertEquals(1, merged.size());
        assertEquals(0, merged.get(0).getInstalled(), "容器回执 status=0 必须覆盖安装表已装状态");
        assertEquals(0, merged.get(0).getEnabled());
        assertEquals("2.0.0", merged.get(0).getVersion());
    }

    private static MallPluginInstall install(String code, int installed, int enabled) {
        MallPluginInstall row = new MallPluginInstall();
        row.setPluginCode(code);
        row.setInstalled(installed);
        row.setEnabled(enabled);
        row.setVersion("1.0.0");
        return row;
    }

    /** 造一个「产物真的落盘」的插件目录（{@code <code>/<ver>/frontend/index.mjs}），返回指向它的服务实例。 */
    private static PluginCenterService serviceWithArtifact(Path pluginsDir, String code, String version) throws Exception {
        Path frontendDir = Files.createDirectories(
                pluginsDir.resolve(code).resolve(version).resolve("frontend"));
        Files.writeString(frontendDir.resolve("index.mjs"), "export default {}");
        PluginCenterService service = new PluginCenterService(null);
        PluginEngineProperties properties = new PluginEngineProperties();
        properties.setPluginsDir(pluginsDir.toString());
        ReflectionTestUtils.setField(service, "pluginEngineProperties", properties);
        return service;
    }

}
