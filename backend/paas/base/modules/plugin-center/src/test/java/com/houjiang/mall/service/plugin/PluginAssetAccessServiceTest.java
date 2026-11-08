package com.houjiang.mall.service.plugin;

import com.houjiang.mall.entity.plugin.MallPluginInstall;
import com.houjiang.mall.mapper.plugin.MallPluginInstallMapper;
import com.houjiang.mall.model.plugin.PluginCatalogItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link PluginAssetAccessService} 单测（T11）：产物公开只读，准入由「宿主登记 + 安装态」两道门控把关。
 *
 * @author Amos
 */
class PluginAssetAccessServiceTest {

    private PluginCenterService pluginCenterService;

    private MallPluginInstallMapper installMapper;

    private PluginAssetAccessService accessService;

    @BeforeEach
    void setUp() {
        pluginCenterService = mock(PluginCenterService.class);
        installMapper = mock(MallPluginInstallMapper.class);
        when(pluginCenterService.catalog()).thenReturn(List.of(catalogItem("mall-points"), catalogItem("mall-live")));
        accessService = new PluginAssetAccessService(pluginCenterService, installMapper);
    }

    @Test
    @DisplayName("未登记插件：拒绝（随机探测拿不到任何字节）")
    void notRegistered() {
        assertEquals(PluginAssetAccessService.Access.NOT_REGISTERED, accessService.check("mall-hacker"));
    }

    @Test
    @DisplayName("无安装记录（官方内置/演示产物）：放行，便于未安装预览")
    void registeredWithoutInstallRecord() {
        when(installMapper.selectOne(any())).thenReturn(null);
        assertEquals(PluginAssetAccessService.Access.ALLOWED, accessService.check("mall-live"));
    }

    @Test
    @DisplayName("已安装且启用：放行")
    void installedAndEnabled() {
        when(installMapper.selectOne(any())).thenReturn(install("mall-points", 1, 1));
        assertEquals(PluginAssetAccessService.Access.ALLOWED, accessService.check("mall-points"));
    }

    @Test
    @DisplayName("已卸载（installed=0）：拒绝 → 404，满足「卸载后访问其页面不可用」验收")
    void uninstalled() {
        when(installMapper.selectOne(any())).thenReturn(install("mall-points", 0, 0));
        assertEquals(PluginAssetAccessService.Access.UNINSTALLED, accessService.check("mall-points"));
    }

    @Test
    @DisplayName("已停用（enabled=0）：拒绝 → 423")
    void disabled() {
        when(installMapper.selectOne(any())).thenReturn(install("mall-points", 1, 0));
        assertEquals(PluginAssetAccessService.Access.DISABLED, accessService.check("mall-points"));
    }

    @Test
    @DisplayName("安装态查询异常：降级为放行，避免库抖动导致官方产物整体不可读")
    void installQueryFailureDegradesToAllowed() {
        when(installMapper.selectOne(any())).thenThrow(new RuntimeException("db down"));
        assertEquals(PluginAssetAccessService.Access.ALLOWED, accessService.check("mall-live"));
    }

    private PluginCatalogItem catalogItem(String code) {
        PluginCatalogItem item = new PluginCatalogItem();
        item.setCode(code);
        item.setName(code);
        return item;
    }

    private MallPluginInstall install(String code, int installed, int enabled) {
        MallPluginInstall install = new MallPluginInstall();
        install.setPluginCode(code);
        install.setInstalled(installed);
        install.setEnabled(enabled);
        return install;
    }
}
