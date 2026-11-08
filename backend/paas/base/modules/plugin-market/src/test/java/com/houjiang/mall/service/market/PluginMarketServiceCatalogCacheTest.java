package com.houjiang.mall.service.market;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.houjiang.mall.entity.market.MarketPlugin;
import com.houjiang.mall.mapper.market.MarketPluginMapper;
import com.houjiang.mall.mapper.market.MarketPluginVersionMapper;
import com.houjiang.mall.mapper.plugin.MallPluginInstallMapper;
import com.houjiang.mall.model.market.MarketPluginItem;
import com.houjiang.mall.service.market.cache.PluginCatalogCache;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 目录列表缓存的租户隔离与回源行为（M5）。
 *
 * <p>钉死两件事：
 * <ol>
 *   <li><b>缓存命中时不再查商品表</b>，但安装态与授权仍然每请求现算；</li>
 *   <li><b>租户字段不进缓存</b>：A 站点命中缓存后看到的必须是<b>自己</b>的安装态，
 *       而不是写入缓存那次的安装态——就地修改缓存对象会造成跨站点数据泄漏。</li>
 * </ol>
 *
 * @author Amos
 * @since M5
 */
class PluginMarketServiceCatalogCacheTest {

    /**
     * 内存版存储后端（与 PluginCatalogCacheTest 同款，独立一份避免测试间耦合）。
     */
    private static final class MemoryStore implements com.houjiang.mall.service.market.cache.CatalogCacheStore {

        private final Map<String, String> data = new HashMap<>();

        @Override
        public String get(String key) {
            return data.get(key);
        }

        @Override
        public void set(String key, String value, int ttlSeconds) {
            data.put(key, value);
        }

        @Override
        public Long incr(String key) {
            long next = Long.parseLong(data.getOrDefault(key, "0")) + 1L;
            data.put(key, String.valueOf(next));
            return next;
        }

        @Override
        public void del(String key) {
            data.remove(key);
        }
    }

    /**
     * 统计商品表查询次数。
     *
     * <p>注意：每次 {@code listPlugins} 会查两次——一次公开基线、一次 {@code categories}，
     * 因此「命中缓存」的判据是次数<b>只增加 1</b>，而不是不增加。
     *
     * @param pluginMapper 商品 Mapper
     * @return 查询计数
     */
    private AtomicInteger countingPluginMapper(MarketPluginMapper pluginMapper) {
        AtomicInteger queryCount = new AtomicInteger();
        when(pluginMapper.selectList(any(Wrapper.class)))
                .thenAnswer(invocation -> {
                    queryCount.incrementAndGet();
                    return List.of(plugin("mall-live", "直播带货"));
                });
        return queryCount;
    }

    private MarketPlugin plugin(String code, String name) {
        MarketPlugin plugin = new MarketPlugin();
        plugin.setPluginCode(code);
        plugin.setName(name);
        plugin.setCategory("营销工具");
        plugin.setLicenseType("FREE");
        plugin.setStatus(2);
        plugin.setPriceCents(0);
        plugin.setTrialDays(0);
        return plugin;
    }

    @SuppressWarnings("unchecked")
    private List<MarketPluginItem> listOf(Map<String, Object> result) {
        return (List<MarketPluginItem>) result.get("list");
    }

    @Test
    void cachedBaselineSkipsPluginQueryButKeepsTenantFieldsFresh() {
        MarketPluginMapper pluginMapper = mock(MarketPluginMapper.class);
        MarketPluginVersionMapper versionMapper = mock(MarketPluginVersionMapper.class);
        MallPluginInstallMapper installMapper = mock(MallPluginInstallMapper.class);
        AtomicInteger queryCount = countingPluginMapper(pluginMapper);

        PluginMarketService service = new PluginMarketService(pluginMapper, versionMapper, installMapper);
        PluginCatalogCache cache = new PluginCatalogCache(new MemoryStore(), 300, true);
        service.setCatalogCache(cache);

        // 第一次：未安装（本次触发两次 selectList：基线 + categories）
        when(installMapper.selectList(any(Wrapper.class))).thenReturn(List.of());
        List<MarketPluginItem> first = listOf(service.listPlugins(Map.of("instanceId", "site-a")));
        assertEquals(1, first.size());
        assertFalse(first.get(0).getInstalled());
        assertEquals(2, queryCount.get(), "首次调用应回源一次商品基线（另有一次 categories）");

        // 第二次：命中缓存（商品表不再被查），但站点 B 已安装 → 必须看到「已安装」
        com.houjiang.mall.entity.plugin.MallPluginInstall install =
                new com.houjiang.mall.entity.plugin.MallPluginInstall();
        install.setPluginCode("mall-live");
        install.setInstalled(1);
        install.setEnabled(1);
        install.setVersion("1.0.0");
        when(installMapper.selectList(any(Wrapper.class))).thenReturn(List.of(install));

        List<MarketPluginItem> second = listOf(service.listPlugins(Map.of("instanceId", "site-b")));

        assertEquals(1, second.size());
        assertTrue(second.get(0).getInstalled(), "缓存命中也必须叠加本站点的安装态");
        assertFalse(first.get(0).getInstalled(), "上一次结果不能被缓存对象就地改写（否则跨站点泄漏）");
        assertEquals(3, queryCount.get(), "第二次应命中缓存，只多出一次 categories 查询");
        assertTrue(cache.hitCount() >= 1, "第二次应命中缓存");
    }

    @Test
    void tenantFilteredQueryIsNeverCached() {
        MarketPluginMapper pluginMapper = mock(MarketPluginMapper.class);
        MarketPluginVersionMapper versionMapper = mock(MarketPluginVersionMapper.class);
        MallPluginInstallMapper installMapper = mock(MallPluginInstallMapper.class);
        AtomicInteger queryCount = countingPluginMapper(pluginMapper);
        when(installMapper.selectList(any(Wrapper.class))).thenReturn(List.of());

        PluginMarketService service = new PluginMarketService(pluginMapper, versionMapper, installMapper);
        PluginCatalogCache cache = new PluginCatalogCache(new MemoryStore(), 300, true);
        service.setCatalogCache(cache);

        service.listPlugins(Map.of("instanceId", "site-a", "status", "installed"));
        service.listPlugins(Map.of("instanceId", "site-a", "status", "installed"));

        // 「已装/未装」依赖本站点安装态，属于租户维度，绝不进缓存
        assertEquals(4, queryCount.get(), "两次调用各回源一次基线（另有一次 categories），说明没有被缓存");
        assertEquals(0L, cache.hitCount());
    }

    @Test
    void revisionBumpRefreshesCatalog() {
        MarketPluginMapper pluginMapper = mock(MarketPluginMapper.class);
        MarketPluginVersionMapper versionMapper = mock(MarketPluginVersionMapper.class);
        MallPluginInstallMapper installMapper = mock(MallPluginInstallMapper.class);
        AtomicInteger queryCount = countingPluginMapper(pluginMapper);
        when(installMapper.selectList(any(Wrapper.class))).thenReturn(List.of());

        PluginMarketService service = new PluginMarketService(pluginMapper, versionMapper, installMapper);
        PluginCatalogCache cache = new PluginCatalogCache(new MemoryStore(), 300, true);
        service.setCatalogCache(cache);

        service.listPlugins(Map.of("instanceId", "site-a"));
        when(pluginMapper.selectList(any(Wrapper.class)))
                .thenReturn(List.of(plugin("mall-live", "直播带货"), plugin("mall-points", "积分商城")));
        service.listPlugins(Map.of("instanceId", "site-a"));

        assertEquals(1, listOf(service.listPlugins(Map.of("instanceId", "site-a"))).size(),
                "未 bump 前仍是缓存里的旧列表");

        cache.bumpRevision();
        assertEquals(2, listOf(service.listPlugins(Map.of("instanceId", "site-a"))).size(),
                "发布侧 bump revision 后目录必须刷新，否则上架后列表不更新");
    }

    @Test
    void detailStillReturnsFullItemWithoutCache() {
        MarketPluginMapper pluginMapper = mock(MarketPluginMapper.class);
        MarketPluginVersionMapper versionMapper = mock(MarketPluginVersionMapper.class);
        MallPluginInstallMapper installMapper = mock(MallPluginInstallMapper.class);
        when(pluginMapper.selectOne(any(Wrapper.class))).thenReturn(plugin("mall-live", "直播带货"));
        when(versionMapper.selectList(any(Wrapper.class))).thenReturn(List.of());
        when(installMapper.selectList(any(Wrapper.class))).thenReturn(List.of());

        PluginMarketService service = new PluginMarketService(pluginMapper, versionMapper, installMapper);
        Map<String, Object> detail = service.detail("mall-live", "site-a");

        assertNotNull(detail.get("plugin"));
        assertEquals("mall-live", ((MarketPluginItem) detail.get("plugin")).getCode());
    }
}
