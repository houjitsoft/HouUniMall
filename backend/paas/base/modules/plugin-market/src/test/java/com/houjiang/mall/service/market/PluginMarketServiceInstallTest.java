package com.houjiang.mall.service.market;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.houjiang.mall.entity.market.MarketPlugin;
import com.houjiang.mall.entity.market.MarketPluginLicense;
import com.houjiang.mall.entity.market.MarketPluginVersion;
import com.houjiang.mall.entity.plugin.MallPluginInstall;
import com.houjiang.mall.mapper.market.MarketPluginLicenseMapper;
import com.houjiang.mall.mapper.market.MarketPluginMapper;
import com.houjiang.mall.mapper.market.MarketPluginVersionMapper;
import com.houjiang.mall.mapper.plugin.MallPluginInstallMapper;
import com.houjiang.plugin.engine.artifact.PluginArtifactStore;
import com.houjiang.plugin.engine.artifact.PluginChecksumVerifier;
import com.houjiang.plugin.engine.core.PluginLifecycleManager;
import com.houjiang.plugin.engine.core.PluginState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 市场在线安装链路单测（T7 第二刀核心）：下载 → MD5 校验 → 落盘 → 登记安装态。
 *
 * <p>断言重点：成功链路完整落库落盘；任一步失败都不写安装态、不留半成品；
 * <b>付费门控</b>——PAID / TRIAL 未持有本站点有效授权时必须拒绝安装，且不下载、不落盘、不登记；
 * 缺站点实例标识（instanceId）时同样拒绝，避免跨站点复用购买记录。
 *
 * @author Amos
 */
class PluginMarketServiceInstallTest {

    @TempDir
    Path tempDir;

    private final PluginChecksumVerifier verifier = new PluginChecksumVerifier();

    /** 站点实例标识：付费插件安装必须携带，否则视为无法确认授权归属而拒绝。 */
    private static final String SITE = "shop.example.com";

    private MarketPlugin plugin(String code, String licenseType, int status) {
        MarketPlugin p = new MarketPlugin();
        p.setPluginCode(code);
        p.setName("直播带货");
        p.setLicenseType(licenseType);
        p.setStatus(status);
        p.setPriceCents(9900);
        p.setInstallCount(0);
        p.setDownloadCount(0);
        return p;
    }

    private MarketPluginVersion version(String code, String ver, String jarUrl, String md5) {
        MarketPluginVersion v = new MarketPluginVersion();
        v.setPluginCode(code);
        v.setVersion(ver);
        v.setJarUrl(jarUrl);
        v.setJarMd5(md5);
        v.setStatus(1);
        return v;
    }

    /** 已授权场景：PAID / TRIAL 插件的安装门控放行（按站点实例校验）。 */
    private PluginLicenseService licensed(String pluginCode) {
        PluginLicenseService licenseService = mock(PluginLicenseService.class);
        when(licenseService.assertInstallable(pluginCode)).thenReturn(new MarketPluginLicense());
        when(licenseService.assertInstallable(eq(pluginCode), anyString())).thenReturn(new MarketPluginLicense());
        return licenseService;
    }

    /** 未授权场景：真实授权服务 + 空授权表（查不到有效授权）。 */
    private PluginLicenseService unlicensed() {
        MarketPluginLicenseMapper licenseMapper = mock(MarketPluginLicenseMapper.class);
        when(licenseMapper.selectOne(any(Wrapper.class))).thenReturn(null);
        return new PluginLicenseService(licenseMapper);
    }

    @Test
    void installDownloadsVerifiesStoresAndRegisters() throws Exception {
        MarketPluginMapper pluginMapper = mock(MarketPluginMapper.class);
        MarketPluginVersionMapper versionMapper = mock(MarketPluginVersionMapper.class);
        MallPluginInstallMapper installMapper = mock(MallPluginInstallMapper.class);
        PluginArtifactDownloader downloader = mock(PluginArtifactDownloader.class);

        byte[] content = "mall-live-jar-bytes".getBytes();
        String md5 = verifier.md5(content);
        when(pluginMapper.selectOne(any(Wrapper.class))).thenReturn(plugin("mall-live", "PAID", 2));
        when(versionMapper.selectOne(any(Wrapper.class)))
                .thenReturn(version("mall-live", "1.0.0", "file:///repo/mall-live-1.0.0.jar", md5));
        when(downloader.download(anyString()))
                .thenReturn(new PluginArtifactDownloader.Artifact(content, "file:///repo/mall-live-1.0.0.jar", "file"));
        when(installMapper.selectOne(any(Wrapper.class))).thenReturn(null);
        PluginLifecycleManager lifecycleManager = mock(PluginLifecycleManager.class);
        when(lifecycleManager.install(eq("mall-live"), any(Path.class), any()))
                .thenReturn(PluginState.ACTIVE);

        PluginMarketService service = new PluginMarketService(pluginMapper, versionMapper, installMapper,
                downloader, new PluginArtifactStore(tempDir), verifier, licensed("mall-live"), lifecycleManager);

        Map<String, Object> result = service.installFromMarket("mall-live", null, "LIGHTWEIGHT", SITE);

        assertEquals("mall-live", result.get("code"));
        assertEquals("1.0.0", result.get("version"));
        assertEquals("PAID", result.get("licenseType"));
        assertEquals(md5, result.get("checksum"));
        assertEquals(Boolean.TRUE, result.get("installed"));
        assertTrue(new PluginArtifactStore(tempDir).exists("mall-live", "1.0.0"), "安装包应已落盘");
        verify(installMapper).insert(any(MallPluginInstall.class));
        verify(pluginMapper).updateById(any(MarketPlugin.class));
        verify(lifecycleManager).install(eq("mall-live"), any(Path.class), any());
    }

    @Test
    void installRejectsChecksumMismatchWithoutStoringOrRegistering() throws Exception {
        MarketPluginMapper pluginMapper = mock(MarketPluginMapper.class);
        MarketPluginVersionMapper versionMapper = mock(MarketPluginVersionMapper.class);
        MallPluginInstallMapper installMapper = mock(MallPluginInstallMapper.class);
        PluginArtifactDownloader downloader = mock(PluginArtifactDownloader.class);

        byte[] content = "tampered".getBytes();
        when(pluginMapper.selectOne(any(Wrapper.class))).thenReturn(plugin("mall-live", "PAID", 2));
        when(versionMapper.selectOne(any(Wrapper.class)))
                .thenReturn(version("mall-live", "1.0.0", "file:///repo/a.jar", "deadbeefdeadbeefdeadbeefdeadbeef"));
        when(downloader.download(anyString()))
                .thenReturn(new PluginArtifactDownloader.Artifact(content, "file:///repo/a.jar", "file"));

        PluginMarketService service = new PluginMarketService(pluginMapper, versionMapper, installMapper,
                downloader, new PluginArtifactStore(tempDir), verifier, licensed("mall-live"));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.installFromMarket("mall-live", "1.0.0", "LIGHTWEIGHT", SITE));

        assertTrue(ex.getMessage().contains("MD5 不一致"), "实际=" + ex.getMessage());
        assertFalse(new PluginArtifactStore(tempDir).exists("mall-live", "1.0.0"), "校验失败不得落盘");
        verify(installMapper, never()).insert(any(MallPluginInstall.class));
        verify(installMapper, never()).updateById(any(MallPluginInstall.class));
    }

    @Test
    void installRejectsUnpublishedPlugin() {
        MarketPluginMapper pluginMapper = mock(MarketPluginMapper.class);
        when(pluginMapper.selectOne(any(Wrapper.class))).thenReturn(plugin("mall-live", "PAID", 1));

        PluginMarketService service = new PluginMarketService(pluginMapper, mock(MarketPluginVersionMapper.class),
                mock(MallPluginInstallMapper.class), mock(PluginArtifactDownloader.class),
                new PluginArtifactStore(tempDir), verifier, licensed("mall-live"));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.installFromMarket("mall-live", null));

        assertEquals("插件未上架，无法安装: mall-live", ex.getMessage());
    }

    @Test
    void installRejectsUnknownPlugin() {
        MarketPluginMapper pluginMapper = mock(MarketPluginMapper.class);
        when(pluginMapper.selectOne(any(Wrapper.class))).thenReturn(null);

        PluginMarketService service = new PluginMarketService(pluginMapper, mock(MarketPluginVersionMapper.class),
                mock(MallPluginInstallMapper.class), mock(PluginArtifactDownloader.class),
                new PluginArtifactStore(tempDir), verifier, licensed("mall-unknown"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.installFromMarket("mall-unknown", null));

        assertEquals("插件不存在: mall-unknown", ex.getMessage());
    }

    @Test
    void installRejectsVersionWithoutJarUrl() {
        MarketPluginMapper pluginMapper = mock(MarketPluginMapper.class);
        MarketPluginVersionMapper versionMapper = mock(MarketPluginVersionMapper.class);
        when(pluginMapper.selectOne(any(Wrapper.class))).thenReturn(plugin("mall-points", "TRIAL", 2));
        when(versionMapper.selectOne(any(Wrapper.class)))
                .thenReturn(version("mall-points", "1.0.0", "", null));

        PluginMarketService service = new PluginMarketService(pluginMapper, versionMapper,
                mock(MallPluginInstallMapper.class), mock(PluginArtifactDownloader.class),
                new PluginArtifactStore(tempDir), verifier, licensed("mall-points"));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.installFromMarket("mall-points", "1.0.0", "LIGHTWEIGHT", SITE));

        assertEquals("该版本未提供安装包地址: mall-points@1.0.0", ex.getMessage());
    }

    @Test
    void installRequiresInstallCapability() {
        PluginMarketService service = new PluginMarketService(
                mock(MarketPluginMapper.class), mock(MarketPluginVersionMapper.class), mock(MallPluginInstallMapper.class));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.installFromMarket("mall-live", null));

        assertEquals("插件安装能力未装配（缺少下载器 / 落盘仓库 / 校验器）", ex.getMessage());
    }

    @Test
    void installReusesExistingInstallRow() throws Exception {
        MarketPluginMapper pluginMapper = mock(MarketPluginMapper.class);
        MarketPluginVersionMapper versionMapper = mock(MarketPluginVersionMapper.class);
        MallPluginInstallMapper installMapper = mock(MallPluginInstallMapper.class);
        PluginArtifactDownloader downloader = mock(PluginArtifactDownloader.class);

        byte[] content = "upgrade-jar".getBytes();
        String md5 = verifier.md5(content);
        when(pluginMapper.selectOne(any(Wrapper.class))).thenReturn(plugin("mall-points", "FREE", 2));
        when(versionMapper.selectOne(any(Wrapper.class)))
                .thenReturn(version("mall-points", "1.1.0", "file:///repo/mall-points-1.1.0.jar", md5));
        when(downloader.download(anyString()))
                .thenReturn(new PluginArtifactDownloader.Artifact(content, "file:///repo/mall-points-1.1.0.jar", "file"));
        MallPluginInstall existing = new MallPluginInstall();
        existing.setId(7L);
        existing.setPluginCode("mall-points");
        existing.setInstalled(1);
        when(installMapper.selectOne(any(Wrapper.class))).thenReturn(existing);

        PluginMarketService service = new PluginMarketService(pluginMapper, versionMapper, installMapper,
                downloader, new PluginArtifactStore(tempDir), verifier, null);

        Map<String, Object> result = service.installFromMarket("mall-points", "1.1.0");

        assertEquals("1.1.0", result.get("version"));
        verify(installMapper, never()).insert(any(MallPluginInstall.class));
        verify(installMapper).updateById(any(MallPluginInstall.class));
        assertEquals("1.1.0", existing.getVersion());
        assertTrue(existing.getJarPath().endsWith("mall-points-1.1.0.jar"), "实际=" + existing.getJarPath());
    }

    @Test
    void installRejectsPaidPluginWithoutLicense() {
        MarketPluginMapper pluginMapper = mock(MarketPluginMapper.class);
        when(pluginMapper.selectOne(any(Wrapper.class))).thenReturn(plugin("mall-webpos", "PAID", 2));

        PluginMarketService service = new PluginMarketService(pluginMapper, mock(MarketPluginVersionMapper.class),
                mock(MallPluginInstallMapper.class), mock(PluginArtifactDownloader.class),
                new PluginArtifactStore(tempDir), verifier, unlicensed());

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.installFromMarket("mall-webpos", null, "LIGHTWEIGHT", SITE));

        assertTrue(ex.getMessage().contains("需先购买并激活授权"), "实际=" + ex.getMessage());
        assertFalse(new PluginArtifactStore(tempDir).exists("mall-webpos", "1.0.0"), "未授权不得落盘");
    }

    @Test
    void installRejectsPaidPluginWithoutSiteInstance() {
        MarketPluginMapper pluginMapper = mock(MarketPluginMapper.class);
        when(pluginMapper.selectOne(any(Wrapper.class))).thenReturn(plugin("mall-webpos", "PAID", 2));

        PluginMarketService service = new PluginMarketService(pluginMapper, mock(MarketPluginVersionMapper.class),
                mock(MallPluginInstallMapper.class), mock(PluginArtifactDownloader.class),
                new PluginArtifactStore(tempDir), verifier, licensed("mall-webpos"));

        // fail-closed：即便授权表里有有效授权，缺站点实例标识也不得安装——
        // 否则站点侧漏传 instanceId 就能复用其它站点的购买记录。
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.installFromMarket("mall-webpos", null));

        assertTrue(ex.getMessage().contains("站点实例标识"), "实际=" + ex.getMessage());
        assertFalse(new PluginArtifactStore(tempDir).exists("mall-webpos", "1.0.0"), "缺实例标识不得落盘");
    }

    @Test
    void installRejectsTrialPluginWithoutLicense() {
        MarketPluginMapper pluginMapper = mock(MarketPluginMapper.class);
        when(pluginMapper.selectOne(any(Wrapper.class))).thenReturn(plugin("mall-activity", "TRIAL", 2));

        PluginMarketService service = new PluginMarketService(pluginMapper, mock(MarketPluginVersionMapper.class),
                mock(MallPluginInstallMapper.class), mock(PluginArtifactDownloader.class),
                new PluginArtifactStore(tempDir), verifier, unlicensed());

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.installFromMarket("mall-activity", null, "LIGHTWEIGHT", SITE));

        assertTrue(ex.getMessage().contains("需先购买并激活授权"), "实际=" + ex.getMessage());
    }

    @Test
    void installRejectsPaidPluginWhenLicenseCapabilityMissing() {
        MarketPluginMapper pluginMapper = mock(MarketPluginMapper.class);
        when(pluginMapper.selectOne(any(Wrapper.class))).thenReturn(plugin("mall-orderfood", "PAID", 2));

        PluginMarketService service = new PluginMarketService(pluginMapper, mock(MarketPluginVersionMapper.class),
                mock(MallPluginInstallMapper.class), mock(PluginArtifactDownloader.class),
                new PluginArtifactStore(tempDir), verifier, null);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.installFromMarket("mall-orderfood", null));

        assertTrue(ex.getMessage().contains("插件授权能力未装配"), "实际=" + ex.getMessage());
    }
}
