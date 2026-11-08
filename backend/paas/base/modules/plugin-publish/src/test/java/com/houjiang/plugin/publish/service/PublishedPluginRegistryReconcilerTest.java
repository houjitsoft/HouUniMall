package com.houjiang.plugin.publish.service;

import com.houjiang.mall.entity.market.MarketPluginVersion;
import com.houjiang.mall.mapper.market.MarketPluginVersionMapper;
import com.houjiang.mall.service.plugin.AddonPluginSyncService;
import com.houjiang.plugin.publish.state.PluginVersionStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class PublishedPluginRegistryReconcilerTest {

    @Test
    void startupCatalogQueryFailureDoesNotAbortBaseStartup() {
        MarketPluginVersionMapper versionMapper = mock(MarketPluginVersionMapper.class);
        when(versionMapper.selectList(any())).thenThrow(new IllegalStateException("database unavailable"));
        AddonPluginSyncService syncService = mock(AddonPluginSyncService.class);

        assertDoesNotThrow(() -> new PublishedPluginRegistryReconciler(versionMapper, syncService).reconcile());
    }

    @Test
    void startupSkipsPublishedRowsWithInvalidSemanticVersion() {
        MarketPluginVersion version = new MarketPluginVersion();
        version.setPluginCode("mall-advertising");
        version.setVersion("latest");
        version.setStatus(PluginVersionStatus.PUBLISHED.getCode());
        version.setPluginYaml("code: mall-advertising\nhostService: order\n");
        MarketPluginVersionMapper versionMapper = mock(MarketPluginVersionMapper.class);
        when(versionMapper.selectList(any())).thenReturn(List.of(version));
        AddonPluginSyncService syncService = mock(AddonPluginSyncService.class);

        new PublishedPluginRegistryReconciler(versionMapper, syncService).reconcile();

        verifyNoInteractions(syncService);
    }

    @Test
    void startupBackfillsPublishedPluginIntoAddonCatalogWithoutCreatingInstallState() {
        MarketPluginVersion version = new MarketPluginVersion();
        version.setPluginCode("mall-advertising");
        version.setVersion("1.0.0");
        version.setStatus(PluginVersionStatus.PUBLISHED.getCode());
        version.setPluginYaml("""
                code: mall-advertising
                name: 广告管理
                version: 1.0.0
                hostService: order
                category: 营销与客户
                """);

        MarketPluginVersionMapper versionMapper = mock(MarketPluginVersionMapper.class);
        when(versionMapper.selectList(any())).thenReturn(List.of(version));
        AddonPluginSyncService syncService = mock(AddonPluginSyncService.class);

        new PublishedPluginRegistryReconciler(versionMapper, syncService).reconcile();

        verify(syncService).syncFromPluginYaml(version.getPluginYaml());
    }
}
