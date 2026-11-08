package com.houjiang.plugin.publish.service;

import com.houjiang.commons.version.PluginVersionComparator;
import com.houjiang.mall.entity.market.MarketPlugin;
import com.houjiang.mall.entity.market.MarketPluginVersion;
import com.houjiang.mall.mapper.market.MarketPluginMapper;
import com.houjiang.mall.mapper.market.MarketPluginVersionMapper;
import com.houjiang.mall.service.market.cache.PluginCatalogCache;
import com.houjiang.plugin.publish.state.PluginVersionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 发布侧与目录缓存的联动（M5）：审核通过 / 下架 / 重新上架都要让目录缓存失效。
 *
 * <p>为什么必须钉死：M1 在 {@code syncPluginOnPublish} 里留了 TODO(M5)，
 * 如果这一步漏了，表现是「市场里有这个插件、但租户点了升级说没更新」——
 * 目录侧与发布侧对「最新版本」的认知不一致，且这类不一致只能靠 TTL 等 5 分钟才自愈。
 *
 * @author Amos
 * @since M5
 */
class PluginPublishServiceCatalogInvalidationTest {

    private static final String CODE = "coupon-demo";

    private MarketPluginVersionMapper versionMapper;

    private MarketPluginMapper pluginMapper;

    private PluginCatalogCache catalogCache;

    private PluginPublishService service;

    @BeforeEach
    void setUp() {
        versionMapper = mock(MarketPluginVersionMapper.class);
        pluginMapper = mock(MarketPluginMapper.class);
        catalogCache = mock(PluginCatalogCache.class);
        service = new PluginPublishService(versionMapper, pluginMapper, catalogCache);
        when(pluginMapper.selectOne(any())).thenReturn(plugin());
        when(versionMapper.selectList(any())).thenReturn(List.of());
    }

    private MarketPlugin plugin() {
        MarketPlugin plugin = new MarketPlugin();
        plugin.setId(1L);
        plugin.setPluginCode(CODE);
        plugin.setStatus(2);
        return plugin;
    }

    private MarketPluginVersion version(Long id, String version, PluginVersionStatus status) {
        MarketPluginVersion row = new MarketPluginVersion();
        row.setId(id);
        row.setPluginCode(CODE);
        row.setVersion(version);
        row.setStatus(status.getCode());
        return row;
    }

    /**
     * 发布侧与目录侧必须共用 commons 里那一份 SemVer 比较器。
     */
    @Test
    @DisplayName("发布侧引用的是 commons 里的同一份 SemVer 比较器")
    void usesSharedComparatorFromCommons() {
        assertEquals("com.houjiang.commons.version.PluginVersionComparator",
                PluginVersionComparator.class.getName(),
                "不允许在 plugin-publish 里再留一份实现，否则「发布说合法、目录说没更新」");
    }

    @Test
    @DisplayName("审核通过：bump revision + 失效该插件的 latest 缓存")
    void approveInvalidatesCatalog() {
        when(versionMapper.selectById(103L))
                .thenReturn(version(103L, "1.10.0", PluginVersionStatus.PENDING));
        when(versionMapper.selectList(any())).thenReturn(List.of(
                version(104L, "1.9.0", PluginVersionStatus.PUBLISHED),
                version(103L, "1.10.0", PluginVersionStatus.PUBLISHED)));

        service.review(103L, "APPROVE", "ok", 9L);

        // 两件事缺一不可：整体失效分页缓存 + 单独删 latest（latest 不进分页缓存）
        verify(catalogCache, times(1)).bumpRevision();
        verify(catalogCache, times(1)).invalidateLatest(CODE);
    }

    @Test
    @DisplayName("下架：同样要让目录缓存失效（否则已下架版本仍被当作可升级）")
    void offlineInvalidatesCatalog() {
        when(versionMapper.selectOne(any()))
                .thenReturn(version(103L, "1.10.0", PluginVersionStatus.PUBLISHED));

        service.offline(CODE, "1.10.0", "安全原因");

        verify(catalogCache, times(1)).bumpRevision();
        verify(catalogCache, times(1)).invalidateLatest(CODE);
    }

    @Test
    @DisplayName("重新上架：目录要重新可见")
    void relistInvalidatesCatalog() {
        when(versionMapper.selectOne(any()))
                .thenReturn(version(103L, "1.10.0", PluginVersionStatus.OFFLINE));

        service.relist(CODE, "1.10.0");

        verify(catalogCache, times(1)).bumpRevision();
        verify(catalogCache, times(1)).invalidateLatest(CODE);
    }

    @Test
    @DisplayName("未装配目录缓存时动作是空操作，不抛异常")
    void worksWithoutCache() {
        PluginPublishService noCache = new PluginPublishService(versionMapper, pluginMapper);
        when(versionMapper.selectOne(any()))
                .thenReturn(version(103L, "1.10.0", PluginVersionStatus.PUBLISHED));

        noCache.offline(CODE, "1.10.0", "安全原因");

        // 缓存缺失时行为完全正确（TTL 兜底），不应影响发布流程
        verify(catalogCache, times(0)).bumpRevision();
    }
}
