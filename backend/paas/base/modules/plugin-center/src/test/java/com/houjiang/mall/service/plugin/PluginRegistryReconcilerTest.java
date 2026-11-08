package com.houjiang.mall.service.plugin;

import com.houjiang.mall.entity.plugin.AddonPlugin;
import com.houjiang.mall.entity.plugin.MallPluginInstall;
import com.houjiang.mall.mapper.plugin.AddonCateMapper;
import com.houjiang.mall.mapper.plugin.AddonPluginMapper;
import com.houjiang.mall.mapper.plugin.MallPluginInstallMapper;
import com.houjiang.mall.model.plugin.PluginCatalogItem;
import com.houjiang.plugin.engine.core.PluginLifecycleManager;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class PluginRegistryReconcilerTest {

    private static PluginCatalogItem item(String code, String name, String category) {
        return new PluginCatalogItem(code, name, category, "1.0.0", "desc",
                List.of("平台端"), List.of(), "index.mjs", "{}");
    }

    private static PluginCatalogItem item(String code, String name, String category, String hostService) {
        PluginCatalogItem item = item(code, name, category);
        item.setHostService(hostService);
        return item;
    }

    private static Map<String, Object> menuRow(String path, int cateId) {
        return Map.of("paths", path, "cate_id", cateId);
    }

    private static Map<String, Object> cateRow(int id, String name) {
        return Map.of("id", id, "name", name);
    }

    @Test
    void backfillsCatalogRowAndInstallRecordForMenuLinkedLocalPlugin() {
        PluginCenterService centerService = mock(PluginCenterService.class);
        when(centerService.catalog()).thenReturn(List.of(item("webpos", "WebPOS", "商城")));
        when(centerService.canonicalCategories()).thenReturn(Map.of("webpos", "商城"));

        MallPluginInstallMapper installMapper = mock(MallPluginInstallMapper.class);
        when(installMapper.selectList(null)).thenReturn(List.of());
        AddonPluginMapper addonMapper = mock(AddonPluginMapper.class);
        AddonCateMapper cateMapper = mock(AddonCateMapper.class);
        when(cateMapper.selectAllPluginMenus()).thenReturn(List.of(menuRow("/plugin/webpos", 4), menuRow("/plugin/aicloud", 0)));
        when(cateMapper.selectAll()).thenReturn(List.of(cateRow(4, "商城")));
        when(addonMapper.selectSoftDeletedCodes()).thenReturn(List.of());
        PluginLifecycleManager lifecycle = mock(PluginLifecycleManager.class);

        PluginRegistryReconciler reconciler = new PluginRegistryReconciler(
                installMapper, addonMapper, cateMapper, centerService, lifecycle);
        reconciler.doReconcile();

        // 目录补全：webpos 落一行，category 用规范分类
        ArgumentCaptor<AddonPlugin> rowCaptor = ArgumentCaptor.forClass(AddonPlugin.class);
        verify(addonMapper).upsertFromManifest(rowCaptor.capture());
        assertEquals("webpos", rowCaptor.getValue().getPluginCode());
        assertEquals("商城", rowCaptor.getValue().getCategory());

        // 安装登记补全：菜单存在 ⇒ installed=1
        ArgumentCaptor<MallPluginInstall> installCaptor = ArgumentCaptor.forClass(MallPluginInstall.class);
        verify(installMapper).insert(installCaptor.capture());
        assertEquals("webpos", installCaptor.getValue().getPluginCode());
        assertEquals(1, installCaptor.getValue().getInstalled());
        assertEquals(1, installCaptor.getValue().getEnabled());
    }

    @Test
    void legacyRouteAliasMapsAicloudMenuToAicode() {
        PluginCenterService centerService = mock(PluginCenterService.class);
        when(centerService.catalog()).thenReturn(List.of(item("aicode", "AI 开发", "")));
        when(centerService.canonicalCategories()).thenReturn(Map.of("aicode", ""));

        MallPluginInstallMapper installMapper = mock(MallPluginInstallMapper.class);
        when(installMapper.selectList(null)).thenReturn(List.of());
        AddonPluginMapper addonMapper = mock(AddonPluginMapper.class);
        AddonCateMapper cateMapper = mock(AddonCateMapper.class);
        when(cateMapper.selectAllPluginMenus()).thenReturn(List.of(menuRow("/plugin/aicloud", 0)));
        when(cateMapper.selectAll()).thenReturn(List.of());
        when(addonMapper.selectSoftDeletedCodes()).thenReturn(List.of());

        new PluginRegistryReconciler(installMapper, addonMapper, cateMapper, centerService, null)
                .doReconcile();

        // /plugin/aicloud 经别名解析为 aicode ⇒ 视为本地已安装
        ArgumentCaptor<MallPluginInstall> installCaptor = ArgumentCaptor.forClass(MallPluginInstall.class);
        verify(installMapper).insert(installCaptor.capture());
        assertEquals("aicode", installCaptor.getValue().getPluginCode());
    }

    @Test
    void nestedMenuPathResolvesToFirstSegment() {
        PluginCenterService centerService = mock(PluginCenterService.class);
        when(centerService.catalog()).thenReturn(List.of(item("oa", "OA", "OA办公")));
        when(centerService.canonicalCategories()).thenReturn(Map.of("oa", "OA办公"));

        MallPluginInstallMapper installMapper = mock(MallPluginInstallMapper.class);
        when(installMapper.selectList(null)).thenReturn(List.of());
        AddonPluginMapper addonMapper = mock(AddonPluginMapper.class);
        AddonCateMapper cateMapper = mock(AddonCateMapper.class);
        when(cateMapper.selectAllPluginMenus()).thenReturn(List.of(menuRow("/plugin/oa/apps", 6)));
        when(cateMapper.selectAll()).thenReturn(List.of());
        when(addonMapper.selectSoftDeletedCodes()).thenReturn(List.of());

        new PluginRegistryReconciler(installMapper, addonMapper, cateMapper, centerService, null)
                .doReconcile();

        verify(installMapper).insert(any(MallPluginInstall.class));
    }

    @Test
    void explicitlyUninstalledPluginIsNotResurrected() {
        PluginCenterService centerService = mock(PluginCenterService.class);
        when(centerService.catalog()).thenReturn(List.of(
                item("webpos", "WebPOS", "商城"),
                item("mall-points", "积分商城", "营销")));
        when(centerService.canonicalCategories()).thenReturn(Map.of(
                "webpos", "商城", "mall-points", "营销"));

        MallPluginInstall uninstalled = new MallPluginInstall();
        uninstalled.setPluginCode("mall-points");
        uninstalled.setInstalled(0);
        uninstalled.setEnabled(0);
        MallPluginInstallMapper installMapper = mock(MallPluginInstallMapper.class);
        when(installMapper.selectList(null)).thenReturn(List.of(uninstalled));

        AddonPluginMapper addonMapper = mock(AddonPluginMapper.class);
        AddonCateMapper cateMapper = mock(AddonCateMapper.class);
        when(cateMapper.selectAllPluginMenus()).thenReturn(List.of(menuRow("/plugin/webpos", 4)));
        when(cateMapper.selectAll()).thenReturn(List.of());
        when(addonMapper.selectSoftDeletedCodes()).thenReturn(List.of("mall-points"));

        new PluginRegistryReconciler(installMapper, addonMapper, cateMapper, centerService, null)
                .doReconcile();

        // mall-points 已显式卸载（软删 + installed=0 + 无菜单/健康信号）：目录不复活、登记不重写；
        // webpos 有菜单在用，正常补目录 + 补登记
        ArgumentCaptor<AddonPlugin> rowCaptor = ArgumentCaptor.forClass(AddonPlugin.class);
        verify(addonMapper, times(1)).upsertFromManifest(rowCaptor.capture());
        assertEquals("webpos", rowCaptor.getValue().getPluginCode());
        ArgumentCaptor<MallPluginInstall> installCaptor = ArgumentCaptor.forClass(MallPluginInstall.class);
        verify(installMapper, times(1)).insert(installCaptor.capture());
        assertEquals("webpos", installCaptor.getValue().getPluginCode());
        verify(installMapper, never()).updateById(any(MallPluginInstall.class));
    }

    @Test
    void uninstalledMarketProductIsNotRegisteredAsLocalPlugin() {
        PluginCenterService centerService = mock(PluginCenterService.class);
        when(centerService.catalog()).thenReturn(List.of(
                item("mall-advertising", "广告管理", "营销与客户")));
        when(centerService.canonicalCategories()).thenReturn(Map.of(
                "mall-advertising", "营销与客户"));

        MallPluginInstallMapper installMapper = mock(MallPluginInstallMapper.class);
        when(installMapper.selectList(null)).thenReturn(List.of());
        AddonPluginMapper addonMapper = mock(AddonPluginMapper.class);
        when(addonMapper.selectList(null)).thenReturn(List.of());
        when(addonMapper.selectSoftDeletedCodes()).thenReturn(List.of());
        AddonCateMapper cateMapper = mock(AddonCateMapper.class);
        when(cateMapper.selectAllPluginMenus()).thenReturn(List.of());
        PluginLifecycleManager lifecycle = mock(PluginLifecycleManager.class);
        when(lifecycle.isHealthy("mall-advertising")).thenReturn(false);

        new PluginRegistryReconciler(installMapper, addonMapper, cateMapper, centerService, lifecycle)
                .doReconcile();

        verify(addonMapper, never()).upsertFromManifest(any(AddonPlugin.class));
        verify(installMapper, never()).insert(any(MallPluginInstall.class));
    }

    @Test
    void publishedCatalogRowOwnedByOrderIsNotDuplicatedUnderLegacyMallHost() {
        PluginCenterService centerService = mock(PluginCenterService.class);
        when(centerService.catalog()).thenReturn(List.of(
                item("mall-advertising", "广告管理", "营销与客户")));
        when(centerService.canonicalCategories()).thenReturn(Map.of(
                "mall-advertising", "营销与客户"));

        MallPluginInstallMapper installMapper = mock(MallPluginInstallMapper.class);
        when(installMapper.selectList(null)).thenReturn(List.of());
        AddonPlugin existing = new AddonPlugin();
        existing.setServiceCode("order");
        existing.setPluginCode("mall-advertising");
        AddonPluginMapper addonMapper = mock(AddonPluginMapper.class);
        when(addonMapper.selectList(null)).thenReturn(List.of(existing));
        when(addonMapper.selectSoftDeletedCodes()).thenReturn(List.of());
        AddonCateMapper cateMapper = mock(AddonCateMapper.class);
        when(cateMapper.selectAllPluginMenus()).thenReturn(List.of());

        new PluginRegistryReconciler(installMapper, addonMapper, cateMapper, centerService, null)
                .doReconcile();

        ArgumentCaptor<AddonPlugin> catalogCaptor = ArgumentCaptor.forClass(AddonPlugin.class);
        verify(addonMapper).upsertFromManifest(catalogCaptor.capture());
        assertEquals("order", catalogCaptor.getValue().getServiceCode());
        verify(installMapper, never()).insert(any(MallPluginInstall.class));
    }

    @Test
    void newUnipayCatalogRowUsesManifestHostService() {
        PluginCenterService centerService = mock(PluginCenterService.class);
        when(centerService.catalog()).thenReturn(List.of(
                item("unipay", "支付中心", "办公与财务", "payment")));
        when(centerService.canonicalCategories()).thenReturn(Map.of("unipay", "办公与财务"));

        MallPluginInstallMapper installMapper = mock(MallPluginInstallMapper.class);
        when(installMapper.selectList(null)).thenReturn(List.of());
        AddonPluginMapper addonMapper = mock(AddonPluginMapper.class);
        when(addonMapper.selectList(null)).thenReturn(List.of());
        when(addonMapper.selectSoftDeletedCodes()).thenReturn(List.of());
        AddonCateMapper cateMapper = mock(AddonCateMapper.class);
        when(cateMapper.selectAllPluginMenus()).thenReturn(List.of(menuRow("/plugin/unipay", 30)));

        new PluginRegistryReconciler(installMapper, addonMapper, cateMapper, centerService, null)
                .doReconcile();

        ArgumentCaptor<AddonPlugin> catalogCaptor = ArgumentCaptor.forClass(AddonPlugin.class);
        verify(addonMapper).upsertFromManifest(catalogCaptor.capture());
        assertEquals("payment", catalogCaptor.getValue().getServiceCode());
    }

    @Test
    void resetsInstallFlagWhenInUseSignalExistsButRecordSaysUninstalled() {
        PluginCenterService centerService = mock(PluginCenterService.class);
        when(centerService.catalog()).thenReturn(List.of(item("crm", "CRM", "CRM")));
        when(centerService.canonicalCategories()).thenReturn(Map.of("crm", "CRM"));

        MallPluginInstall record = new MallPluginInstall();
        record.setPluginCode("crm");
        record.setInstalled(0);
        record.setEnabled(1);
        MallPluginInstallMapper installMapper = mock(MallPluginInstallMapper.class);
        when(installMapper.selectList(null)).thenReturn(List.of(record));

        AddonPluginMapper addonMapper = mock(AddonPluginMapper.class);
        AddonCateMapper cateMapper = mock(AddonCateMapper.class);
        when(cateMapper.selectAllPluginMenus()).thenReturn(List.of());
        when(cateMapper.selectAll()).thenReturn(List.of());
        when(addonMapper.selectSoftDeletedCodes()).thenReturn(List.of());
        PluginLifecycleManager lifecycle = mock(PluginLifecycleManager.class);
        when(lifecycle.isHealthy("crm")).thenReturn(true);

        new PluginRegistryReconciler(installMapper, addonMapper, cateMapper, centerService, lifecycle)
                .doReconcile();

        // 运行健康 ⇒ 在用 ⇒ 复位 installed=1
        ArgumentCaptor<MallPluginInstall> captor = ArgumentCaptor.forClass(MallPluginInstall.class);
        verify(installMapper).updateById(captor.capture());
        assertEquals(1, captor.getValue().getInstalled());
        verify(installMapper, never()).insert(any(MallPluginInstall.class));
    }
}
