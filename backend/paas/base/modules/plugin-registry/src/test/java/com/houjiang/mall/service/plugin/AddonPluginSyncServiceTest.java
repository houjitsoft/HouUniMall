package com.houjiang.mall.service.plugin;

import com.houjiang.mall.entity.plugin.AddonPlugin;
import com.houjiang.mall.mapper.plugin.AddonPluginMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * {@link AddonPluginSyncService} 单测（P0-3：plugin.yaml → hou_addon_plugin 单向同步）。
 *
 * <p>覆盖三条关键路径：①完整声明映射为目录行并幂等 upsert；②入口归一化（剥版本化前缀 / 保留裸入口）；
 * ③守卫与软删（yaml 为空、缺 code、无 mapper 时跳过；卸载软删委派 mapper）。
 *
 * @author Amos
 */
class AddonPluginSyncServiceTest {

    /** 典型市场插件声明：含 terminals / 对象数组 dependencies / 版本化 frontend.entry / configSchema */
    private static final String FULL_YAML = """
            code: erp
            name: ERP 企业资源计划
            category: Enterprise
            version: 1.0.0
            description: 商品、销售、库存、采购、生产与财务管理
            terminals:
              - admin
            dependencies:
              - code: mall
                version: 1.0.0
            frontend:
              entry: /plugin-assets/erp/1.0.0/index.mjs
            configSchema:
              warehouseLimit:
                type: number
                label: 仓库上限
            """;

    @Test
    void syncFromPluginYamlMapsFullDeclarationToAddonRowAndUpserts() {
        AddonPluginMapper mapper = mock(AddonPluginMapper.class);
        when(mapper.upsertFromManifest(any(AddonPlugin.class))).thenReturn(1);
        AddonPluginSyncService service = new AddonPluginSyncService(mapper);

        boolean synced = service.syncFromPluginYaml("mall", FULL_YAML);

        assertTrue(synced, "合法声明应同步成功");
        AddonPlugin row = captureUpsert(mapper);
        assertEquals("mall", row.getServiceCode());
        assertEquals("erp", row.getPluginCode());
        assertEquals("ERP 企业资源计划", row.getName());
        assertEquals("Enterprise", row.getCategory());
        assertEquals("1.0.0", row.getVersion());
        assertEquals("商品、销售、库存、采购、生产与财务管理", row.getDescription());
        assertEquals("[\"admin\"]", row.getTerminalsJson());
        // 对象数组 dependencies 压成编码字符串数组，与静态目录 JSON 形状一致
        assertEquals("[\"mall\"]", row.getDependenciesJson());
        // frontend.entry 剥掉 /plugin-assets/{code}/{version}/ 前缀 → 裸入口，避免前缀重复
        assertEquals("index.mjs", row.getEntryPath());
        assertTrue(row.getConfigSchema().contains("warehouseLimit"), "configSchema 应序列化为 JSON");
        assertEquals(1, row.getStatus().intValue());
        assertEquals(0, row.getSort().intValue());
        assertEquals(0, row.getDeleted().intValue());
    }

    @Test
    void syncFromPluginYamlUsesDeclaredHostServiceForPublishedCatalogEntry() {
        AddonPluginMapper mapper = mock(AddonPluginMapper.class);
        AddonPluginSyncService service = new AddonPluginSyncService(mapper);

        boolean synced = service.syncFromPluginYaml("""
                code: mall-advertising
                name: 广告管理
                version: 1.0.0
                hostService: order
                category: 营销与客户
                """);

        assertTrue(synced);
        AddonPlugin row = captureUpsert(mapper);
        assertEquals("order", row.getServiceCode(), "正式目录必须采用 plugin.yaml 的权威归属服务");
        assertEquals("mall-advertising", row.getPluginCode());
        verify(mapper).softDeleteOtherServiceRows("order", "mall-advertising");
    }

    @Test
    void syncFromPluginYamlDefaultsNameAndEmptyEntriesForMinimalDeclaration() {
        AddonPluginMapper mapper = mock(AddonPluginMapper.class);
        AddonPluginSyncService service = new AddonPluginSyncService(mapper);

        boolean synced = service.syncFromPluginYaml("mall", "code: mall-points\n");

        assertTrue(synced);
        AddonPlugin row = captureUpsert(mapper);
        assertEquals("mall-points", row.getPluginCode());
        assertEquals("mall-points", row.getName(), "缺 name 时以 code 兜底");
        assertEquals("[]", row.getTerminalsJson());
        assertEquals("[]", row.getDependenciesJson());
        assertEquals("{}", row.getConfigSchema());
        assertNull(row.getEntryPath());
    }

    @Test
    void syncFromPluginYamlKeepsTopLevelEntryPathVerbatim() {
        AddonPluginMapper mapper = mock(AddonPluginMapper.class);
        AddonPluginSyncService service = new AddonPluginSyncService(mapper);

        service.syncFromPluginYaml("mall", "code: mall-points\nentryPath: /points/index\n");

        assertEquals("/points/index", captureUpsert(mapper).getEntryPath(), "内置页面路径原样保留");
    }

    @Test
    void syncFromPluginYamlKeepsNonAssetFrontendEntryAsIs() {
        AddonPluginMapper mapper = mock(AddonPluginMapper.class);
        AddonPluginSyncService service = new AddonPluginSyncService(mapper);

        service.syncFromPluginYaml("mall", "code: mall-coupon\nfrontend:\n  entry: dist/main.js\n");

        assertEquals("dist/main.js", captureUpsert(mapper).getEntryPath(), "非 /plugin-assets/ 前缀入口不剥离");
    }

    @Test
    void syncFromPluginYamlSkipsWhenCodeMissing() {
        AddonPluginMapper mapper = mock(AddonPluginMapper.class);
        AddonPluginSyncService service = new AddonPluginSyncService(mapper);

        boolean synced = service.syncFromPluginYaml("mall", "name: 无编码插件\n");

        assertFalse(synced, "缺 code 应跳过而非抛异常");
        verify(mapper, never()).upsertFromManifest(any(AddonPlugin.class));
    }

    @Test
    void syncFromPluginYamlSkipsWhenArgsBlank() {
        AddonPluginMapper mapper = mock(AddonPluginMapper.class);
        AddonPluginSyncService service = new AddonPluginSyncService(mapper);

        assertFalse(service.syncFromPluginYaml("mall", "  "));
        assertFalse(service.syncFromPluginYaml(null, FULL_YAML));
        verify(mapper, never()).upsertFromManifest(any(AddonPlugin.class));
    }

    @Test
    void syncFromPluginYamlSkipsWhenMapperMissing() {
        AddonPluginSyncService service = new AddonPluginSyncService(null);

        assertFalse(service.syncFromPluginYaml("mall", FULL_YAML), "只读 / 无库场景不阻断安装");
    }

    @Test
    void softDeleteByCodeDelegatesToMapperAndReportsHit() {
        AddonPluginMapper mapper = mock(AddonPluginMapper.class);
        when(mapper.softDeleteByCode("mall", "erp")).thenReturn(1);
        AddonPluginSyncService service = new AddonPluginSyncService(mapper);

        assertTrue(service.softDeleteByCode("mall", "erp"));
        verify(mapper).softDeleteByCode("mall", "erp");
    }

    @Test
    void softDeleteByCodeReturnsFalseWhenNoRowAffectedOrArgsInvalid() {
        AddonPluginMapper mapper = mock(AddonPluginMapper.class);
        when(mapper.softDeleteByCode("mall", "ghost")).thenReturn(0);
        AddonPluginSyncService service = new AddonPluginSyncService(mapper);

        assertFalse(service.softDeleteByCode("mall", "ghost"), "未命中行返回 false");
        assertFalse(service.softDeleteByCode("mall", "  "));
        assertFalse(new AddonPluginSyncService(null).softDeleteByCode("mall", "erp"));
        verify(mapper, times(1)).softDeleteByCode(anyString(), anyString());
    }

    private AddonPlugin captureUpsert(AddonPluginMapper mapper) {
        ArgumentCaptor<AddonPlugin> captor = ArgumentCaptor.forClass(AddonPlugin.class);
        verify(mapper).upsertFromManifest(captor.capture());
        return captor.getValue();
    }
}
