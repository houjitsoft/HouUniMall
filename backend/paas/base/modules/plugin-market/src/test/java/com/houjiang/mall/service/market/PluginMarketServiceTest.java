package com.houjiang.mall.service.market;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.houjiang.mall.entity.market.MarketPlugin;
import com.houjiang.mall.entity.market.MarketPluginVersion;
import com.houjiang.mall.entity.plugin.MallPluginInstall;
import com.houjiang.mall.mapper.market.MarketAddonCateMapper;
import com.houjiang.mall.mapper.market.MarketPluginMapper;
import com.houjiang.mall.mapper.market.MarketPluginVersionMapper;
import com.houjiang.mall.mapper.plugin.MallPluginInstallMapper;
import com.houjiang.mall.model.market.MarketPluginItem;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 插件市场服务单测：验证「浏览 + 定价 + 分类 + 版本」的接口逻辑。
 *
 * <p>与 T0 的 {@code PluginCenterServiceTest} 同一验收口径——生产登录态不可得时，
 * 以 Service 层逻辑 + 线上链路（{@code code=11006}）+ 库内数据三者共同佐证。
 *
 * @author Amos
 */
class PluginMarketServiceTest {

    private MarketPlugin plugin(String code, String name, String category, String license, int price, int trial) {
        return plugin(code, name, category, null, license, price, trial);
    }

    private MarketPlugin plugin(String code, String name, String category, Integer cateId,
                                String license, int price, int trial) {
        MarketPlugin p = new MarketPlugin();
        p.setPluginCode(code);
        p.setName(name);
        p.setCategory(category);
        p.setCateId(cateId);
        p.setLicenseType(license);
        p.setPriceCents(price);
        p.setTrialDays(trial);
        p.setStatus(2);
        return p;
    }

    @SuppressWarnings("unchecked")
    @Test
    void listPluginsReturnsPricingAndCategories() {
        MarketPluginMapper pluginMapper = mock(MarketPluginMapper.class);
        MarketPluginVersionMapper versionMapper = mock(MarketPluginVersionMapper.class);
        MallPluginInstallMapper installMapper = mock(MallPluginInstallMapper.class);

        when(pluginMapper.selectList(any(Wrapper.class))).thenReturn(List.of(
                plugin("mall-points", "积分商城", "营销工具", "TRIAL", 0, 15),
                plugin("mall-live", "直播带货", "内容营销", "PAID", 9900, 0),
                plugin("mall-gather", "商品采集", "商品工具", "FREE", 0, 0)));
        when(installMapper.selectList(any())).thenReturn(List.of());

        PluginMarketService service = new PluginMarketService(pluginMapper, versionMapper, installMapper);
        Map<String, Object> result = service.listPlugins(Map.of());

        List<MarketPluginItem> list = (List<MarketPluginItem>) result.get("list");
        assertEquals(3, list.size());
        assertEquals("试用 15 天", byCode(list, "mall-points").getPriceText());
        assertEquals("¥99.00", byCode(list, "mall-live").getPriceText());
        assertEquals("免费", byCode(list, "mall-gather").getPriceText());

        // 授权状态：付费/试用未授权标注为 false；免费恒为 true
        assertFalse(byCode(list, "mall-live").getLicensed(), "付费插件未授权时应为 false");
        assertFalse(byCode(list, "mall-points").getLicensed(), "试用插件未授权时应为 false");
        assertTrue(byCode(list, "mall-gather").getLicensed(), "免费插件恒为已授权");

        // 分类：未配置分类字典时插件统一落「未分类」桶，分类列表不得为空
        List<Map<String, Object>> categories = (List<Map<String, Object>>) result.get("categories");
        assertFalse(categories.isEmpty(), "分类列表不得为空");
        assertTrue(categories.stream().anyMatch(c -> "未分类".equals(c.get("name"))),
                "未配置分类字典时插件统一落「未分类」: " + categories);
    }

    @SuppressWarnings("unchecked")
    @Test
    void listPluginsMergesInstallState() {
        MarketPluginMapper pluginMapper = mock(MarketPluginMapper.class);
        MarketPluginVersionMapper versionMapper = mock(MarketPluginVersionMapper.class);
        MallPluginInstallMapper installMapper = mock(MallPluginInstallMapper.class);

        when(pluginMapper.selectList(any(Wrapper.class))).thenReturn(List.of(
                plugin("mall-live", "直播带货", "内容营销", "PAID", 9900, 0)));
        MallPluginInstall install = new MallPluginInstall();
        install.setPluginCode("mall-live");
        install.setInstalled(1);
        install.setEnabled(1);
        when(installMapper.selectList(any())).thenReturn(List.of(install));

        PluginMarketService service = new PluginMarketService(pluginMapper, versionMapper, installMapper);
        Map<String, Object> result = service.listPlugins(Map.of());

        List<MarketPluginItem> list = (List<MarketPluginItem>) result.get("list");
        MarketPluginItem live = byCode(list, "mall-live");
        assertTrue(live.getInstalled());
        assertTrue(live.getEnabled());
    }

    @SuppressWarnings("unchecked")
    @Test
    void detailReturnsPluginAndVersions() {
        MarketPluginMapper pluginMapper = mock(MarketPluginMapper.class);
        MarketPluginVersionMapper versionMapper = mock(MarketPluginVersionMapper.class);
        MallPluginInstallMapper installMapper = mock(MallPluginInstallMapper.class);

        when(pluginMapper.selectOne(any(Wrapper.class)))
                .thenReturn(plugin("mall-distribution", "分销推广", "增长工具", "PAID", 19900, 0));
        MarketPluginVersion version = new MarketPluginVersion();
        version.setPluginCode("mall-distribution");
        version.setVersion("1.0.0");
        version.setJarUrl("https://market.houjit.com/jars/mall-distribution-1.0.0.jar");
        version.setJarMd5("d41d8cd98f00b204e9800998ecf8427e");
        version.setSchemaSql("CREATE TABLE demo (id BIGINT);");
        when(versionMapper.selectList(any(Wrapper.class))).thenReturn(List.of(version));
        when(installMapper.selectList(any())).thenReturn(List.of());

        PluginMarketService service = new PluginMarketService(pluginMapper, versionMapper, installMapper);
        Map<String, Object> result = service.detail("mall-distribution");

        MarketPluginItem item = (MarketPluginItem) result.get("plugin");
        assertEquals("mall-distribution", item.getCode());
        assertEquals("¥199.00", item.getPriceText());
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> versions = (List<Map<String, Object>>) result.get("versions");
        assertEquals(1, versions.size());
        assertEquals("1.0.0", versions.get(0).get("version"));
    }

    /**
     * 详情的版本列表是租户可见的：制品直链、建表/回滚 SQL、制品指纹一律不得出现。
     *
     * <p>这条断言是回归闸门——裁剪靠的是 {@code toVersionItem} 的白名单，
     * 一旦有人改成整实体直出（或往白名单里误加了敏感列），这里必须红。
     */
    @SuppressWarnings("unchecked")
    @Test
    void detailVersionsExcludeArtifactAndSchemaFields() {
        MarketPluginMapper pluginMapper = mock(MarketPluginMapper.class);
        MarketPluginVersionMapper versionMapper = mock(MarketPluginVersionMapper.class);
        MallPluginInstallMapper installMapper = mock(MallPluginInstallMapper.class);

        when(pluginMapper.selectOne(any(Wrapper.class)))
                .thenReturn(plugin("mall-distribution", "分销推广", "增长工具", "PAID", 19900, 0));
        MarketPluginVersion version = new MarketPluginVersion();
        version.setPluginCode("mall-distribution");
        version.setVersion("1.0.0");
        version.setJarUrl("https://market.houjit.com/jars/mall-distribution-1.0.0.jar");
        version.setJarMd5("d41d8cd98f00b204e9800998ecf8427e");
        version.setJarSha256("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");
        version.setJarSize(4096L);
        version.setJarSignature("MEUCIQ...");
        version.setStorageKey("mall/mall-distribution/1.0.0.jar");
        version.setSchemaSql("CREATE TABLE demo (id BIGINT);");
        version.setRollbackSql("DROP TABLE demo;");
        version.setPluginYaml("id: mall-distribution");
        version.setReviewNote("内部审核意见");
        when(versionMapper.selectList(any(Wrapper.class))).thenReturn(List.of(version));
        when(installMapper.selectList(any())).thenReturn(List.of());

        PluginMarketService service = new PluginMarketService(pluginMapper, versionMapper, installMapper);
        Map<String, Object> result = service.detail("mall-distribution");

        List<Map<String, Object>> versions = (List<Map<String, Object>>) result.get("versions");
        assertEquals(1, versions.size());
        Map<String, Object> item = versions.get(0);
        for (String forbidden : List.of("jarUrl", "jarMd5", "jarSha256", "jarSize", "jarSignature",
                "storageKey", "schemaSql", "rollbackSql", "pluginYaml", "reviewNote")) {
            assertFalse(item.containsKey(forbidden), "租户可见的版本列表不得包含 " + forbidden);
        }
        // 白名单保留项仍要在，别裁过头
        assertTrue(item.containsKey("id"));
        assertTrue(item.containsKey("version"));
        assertTrue(item.containsKey("changelog"));
        assertTrue(item.containsKey("publishTime"));
        assertTrue(item.containsKey("compatible"));
    }

    // ---------- 分类数据来源：hou_addon_cate ----------

    @Test
    void categoriesTakeAddonCateTableAsSourceOfTruth() {
        MarketPluginMapper pluginMapper = mock(MarketPluginMapper.class);
        when(pluginMapper.selectList(any(Wrapper.class))).thenReturn(List.of(
                plugin("mall-points", "积分商城", "营销工具", "FREE", 0, 0)));
        MarketAddonCateMapper cateMapper = mock(MarketAddonCateMapper.class);
        when(cateMapper.selectAll()).thenReturn(List.of(
                Map.of("id", 1, "name", "营销"),
                Map.of("id", 4, "name", "商城")));

        PluginMarketService service = new PluginMarketService(pluginMapper, null, null);
        service.setAddonCateMapper(cateMapper);

        List<String> categories = service.categories();

        assertTrue(categories.contains("营销"), "分类必须来自 hou_addon_cate: " + categories);
        assertTrue(categories.contains("商城"), "表里没有插件的分类也要出现: " + categories);
        assertTrue(categories.indexOf("营销") < categories.indexOf("商城"), "顺序跟随 sort/id 升序");
        // 2026-09-19：分类漂移的根源就是「把市场插件的自由文本分类并进分类下拉」。
        // 现在分类归属走 cate_id，展示名由字典派生，自由文本不再泄漏到分类列表。
        assertFalse(categories.contains("营销工具"),
                "市场插件自带的自由文本分类不得再混入分类下拉: " + categories);
    }

    @Test
    void categoryOptionsCarryIdCountAndUncategorizedBucket() {
        MarketPluginMapper pluginMapper = mock(MarketPluginMapper.class);
        when(pluginMapper.selectList(any(Wrapper.class))).thenReturn(List.of(
                plugin("mall-points", "积分商城", "营销", 1, "FREE", 0, 0),
                plugin("mall-live", "直播带货", "营销", 1, "FREE", 0, 0),
                // cate_id 指向已被删除的分类：展示与统计都要落进「未分类」桶，
                // 否则会出现「列表里显示未分类，Tab 上却没有这一项」。
                plugin("mall-ghost", "幽灵插件", "已删除分类", 99, "FREE", 0, 0)));
        MarketAddonCateMapper cateMapper = mock(MarketAddonCateMapper.class);
        when(cateMapper.selectAll()).thenReturn(List.of(
                Map.of("id", 1, "name", "营销", "sort", 2),
                Map.of("id", 4, "name", "商城", "sort", 1)));

        PluginMarketService service = new PluginMarketService(pluginMapper, null, null);
        service.setAddonCateMapper(cateMapper);

        List<Map<String, Object>> options = service.categoryOptions();

        assertEquals(List.of(4, 1, 0), options.stream().map(o -> o.get("id")).toList(),
                "分类按 sort 升序，未分类桶固定在末尾");
        assertEquals(List.of("商城", "营销", "未分类"), options.stream().map(o -> o.get("name")).toList());
        assertEquals(List.of(0L, 2L, 1L), options.stream().map(o -> o.get("count")).toList(),
                "count 为该分类下已上架插件数，未知分类计入未分类");
    }

    @Test
    void listItemsCarryCateIdAndCanonicalCategoryName() {
        MarketPluginMapper pluginMapper = mock(MarketPluginMapper.class);
        MallPluginInstallMapper installMapper = mock(MallPluginInstallMapper.class);
        when(pluginMapper.selectList(any(Wrapper.class))).thenReturn(List.of(
                plugin("mall-points", "积分商城", "营销", 1, "FREE", 0, 0),
                plugin("mall-orphan", "无归属插件", null, null, "FREE", 0, 0)));
        when(installMapper.selectList(any())).thenReturn(List.of());
        MarketAddonCateMapper cateMapper = mock(MarketAddonCateMapper.class);
        when(cateMapper.selectAll()).thenReturn(List.of(Map.of("id", 1, "name", "营销")));

        PluginMarketService service = new PluginMarketService(pluginMapper, null, installMapper);
        service.setAddonCateMapper(cateMapper);

        Map<String, Object> result = service.listPlugins(Map.of());

        @SuppressWarnings("unchecked")
        List<MarketPluginItem> items = (List<MarketPluginItem>) result.get("list");
        assertEquals(1, byCode(items, "mall-points").getCateId());
        assertEquals("营销", byCode(items, "mall-points").getCategory());
        assertEquals(0, byCode(items, "mall-orphan").getCateId(), "未归类插件的 cateId 归一为 0");
        assertEquals("未分类", byCode(items, "mall-orphan").getCategory(),
                "分类展示名永不为空，未归类落「未分类」");
    }

    @Test
    void categoriesFallBackWhenCateTableReadFails() {
        MarketPluginMapper pluginMapper = mock(MarketPluginMapper.class);
        when(pluginMapper.selectList(any(Wrapper.class))).thenReturn(List.of(
                plugin("mall-points", "积分商城", "营销工具", "FREE", 0, 0)));
        MarketAddonCateMapper cateMapper = mock(MarketAddonCateMapper.class);
        when(cateMapper.selectAll()).thenThrow(new RuntimeException("Table 'hou_addon_cate' doesn't exist"));

        PluginMarketService service = new PluginMarketService(pluginMapper, null, null);
        service.setAddonCateMapper(cateMapper);

        List<String> categories = service.categories();

        assertFalse(categories.isEmpty(), "分类表不可用时必须给出可用的分类，不能整排消失");
        // 分类字典读不到时，所有插件归入「未分类」桶：分类问题不能连带打挂市场列表。
        assertEquals(List.of("未分类"), categories);
    }

    private MarketPluginItem byCode(List<MarketPluginItem> items, String code) {
        return items.stream()
                .filter(item -> code.equals(item.getCode()))
                .findFirst()
                .orElseThrow();
    }
}
