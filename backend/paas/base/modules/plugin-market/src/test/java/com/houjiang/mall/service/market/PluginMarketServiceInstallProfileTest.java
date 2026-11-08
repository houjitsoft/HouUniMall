package com.houjiang.mall.service.market;

import com.houjiang.mall.mapper.market.MarketPluginMapper;
import com.houjiang.mall.mapper.market.MarketPluginVersionMapper;
import com.houjiang.mall.mapper.plugin.MallPluginInstallMapper;
import com.houjiang.mall.entity.market.MarketPlugin;
import com.houjiang.mall.entity.market.MarketPluginVersion;
import com.houjiang.plugin.engine.artifact.PluginArtifactStore;
import com.houjiang.plugin.engine.artifact.PluginChecksumVerifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PluginMarketServiceInstallProfileTest {

    @TempDir
    Path tempDir;

    private PluginMarketService service() {
        return new PluginMarketService(
                mock(MarketPluginMapper.class), mock(MarketPluginVersionMapper.class),
                mock(MallPluginInstallMapper.class), mock(PluginArtifactDownloader.class),
                new PluginArtifactStore(tempDir), new PluginChecksumVerifier(), null);
    }

    @Test
    void rejectsHeavyProfileBeforeMarketLookup() {
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service().installFromMarket("mall-live", null, "HEAVY"));

        assertTrue(ex.getMessage().contains("HEAVY"));
    }

    @Test
    void rejectsCoreProfileBeforeMarketLookup() {
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service().installFromMarket("mall-live", null, "CORE"));

        assertTrue(ex.getMessage().contains("CORE"));
    }

    @Test
    void rejectsLightweightWhenManifestOnlyDeclaresStandaloneRuntime() throws Exception {
        MarketPluginMapper pluginMapper = mock(MarketPluginMapper.class);
        MarketPluginVersionMapper versionMapper = mock(MarketPluginVersionMapper.class);
        PluginArtifactDownloader downloader = mock(PluginArtifactDownloader.class);
        MarketPlugin plugin = new MarketPlugin();
        plugin.setPluginCode("erp");
        plugin.setStatus(2);
        plugin.setLicenseType("FREE");
        MarketPluginVersion version = new MarketPluginVersion();
        version.setPluginCode("erp");
        version.setVersion("1.0.0");
        version.setStatus(1);
        version.setPluginYaml("runtime: standalone");
        version.setJarUrl("file:///erp.jar");
        when(pluginMapper.selectOne(any())).thenReturn(plugin);
        when(versionMapper.selectOne(any())).thenReturn(version);

        PluginMarketService service = new PluginMarketService(pluginMapper, versionMapper,
                mock(MallPluginInstallMapper.class), downloader, new PluginArtifactStore(tempDir),
                new PluginChecksumVerifier(), null);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.installFromMarket("erp", "1.0.0", "LIGHTWEIGHT"));

        assertTrue(ex.getMessage().contains("独立服务"));
        verify(downloader, never()).download(any());
    }
}
