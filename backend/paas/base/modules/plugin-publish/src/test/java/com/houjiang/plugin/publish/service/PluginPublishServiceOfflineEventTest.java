package com.houjiang.plugin.publish.service;

import com.houjiang.commons.event.PluginVersionOfflineEvent;
import com.houjiang.mall.entity.market.MarketPlugin;
import com.houjiang.mall.entity.market.MarketPluginVersion;
import com.houjiang.mall.mapper.market.MarketPluginMapper;
import com.houjiang.mall.mapper.market.MarketPluginVersionMapper;
import com.houjiang.plugin.publish.state.PluginProductStatus;
import com.houjiang.plugin.publish.state.PluginVersionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 下架联动：事件发布 + 商品回落草稿（M5b，方案 §7.4）。
 *
 * <p>钉死三件事：
 * <ol>
 *   <li>下架必须广播 {@link PluginVersionOfflineEvent}，且带全 code / version / reason；</li>
 *   <li>没有发布器、或发布器抛异常时，<b>下架本身不能失败</b>（联动是附加动作）；</li>
 *   <li>最后一个已发布版本下架后，商品要回落 draft——这条是 M1 既有行为，
 *       本次核实它是否真的生效，避免 M5b 又实现一遍。</li>
 * </ol>
 *
 * @author Amos
 * @since M5b
 */
class PluginPublishServiceOfflineEventTest {

    private static final String CODE = "coupon-demo";

    private MarketPluginVersionMapper versionMapper;

    private MarketPluginMapper pluginMapper;

    private final List<Object> publishedEvents = new ArrayList<>();

    @BeforeEach
    void setUp() {
        versionMapper = mock(MarketPluginVersionMapper.class);
        pluginMapper = mock(MarketPluginMapper.class);
        when(versionMapper.updateById(any(MarketPluginVersion.class))).thenReturn(1);
        when(pluginMapper.updateById(any(MarketPlugin.class))).thenReturn(1);
    }

    private MarketPluginVersion version(Long id, String version, PluginVersionStatus status) {
        MarketPluginVersion row = new MarketPluginVersion();
        row.setId(id);
        row.setPluginCode(CODE);
        row.setVersion(version);
        row.setStatus(status.getCode());
        return row;
    }

    private MarketPlugin plugin() {
        MarketPlugin plugin = new MarketPlugin();
        plugin.setId(1L);
        plugin.setPluginCode(CODE);
        plugin.setStatus(PluginProductStatus.PUBLISHED.getCode());
        return plugin;
    }

    @Test
    @DisplayName("下架广播事件：code / version / reason 齐全")
    void offlinePublishesEvent() {
        ApplicationEventPublisher publisher = publishedEvents::add;
        when(versionMapper.selectOne(any())).thenReturn(version(1L, "1.0.0", PluginVersionStatus.PUBLISHED));
        when(pluginMapper.selectOne(any())).thenReturn(plugin());
        when(versionMapper.selectList(any())).thenReturn(List.of());

        new PluginPublishService(versionMapper, pluginMapper, null, publisher)
                .offline(CODE, "1.0.0", "安全漏洞");

        assertEquals(1, publishedEvents.size());
        PluginVersionOfflineEvent event = (PluginVersionOfflineEvent) publishedEvents.get(0);
        assertEquals(CODE, event.getPluginCode());
        assertEquals("1.0.0", event.getVersion());
        assertEquals("安全漏洞", event.getReason());
        assertNotNull(event.getOfflineTime());
    }

    @Test
    @DisplayName("未装配发布器时下架照常成功（联动是附加动作）")
    void offlineWorksWithoutPublisher() {
        when(versionMapper.selectOne(any())).thenReturn(version(1L, "1.0.0", PluginVersionStatus.PUBLISHED));
        when(pluginMapper.selectOne(any())).thenReturn(plugin());
        when(versionMapper.selectList(any())).thenReturn(List.of());

        PluginPublishService service = new PluginPublishService(versionMapper, pluginMapper);
        assertEquals(PluginVersionStatus.OFFLINE.getCode(),
                service.offline(CODE, "1.0.0", "x").get("status"));
    }

    @Test
    @DisplayName("发布器抛异常不能让下架失败")
    void offlineSurvivesPublisherFailure() {
        ApplicationEventPublisher publisher = event -> {
            throw new IllegalStateException("模拟事件总线故障");
        };
        when(versionMapper.selectOne(any())).thenReturn(version(1L, "1.0.0", PluginVersionStatus.PUBLISHED));
        when(pluginMapper.selectOne(any())).thenReturn(plugin());
        when(versionMapper.selectList(any())).thenReturn(List.of());

        PluginPublishService service = new PluginPublishService(versionMapper, pluginMapper, null, publisher);
        assertEquals(PluginVersionStatus.OFFLINE.getCode(),
                service.offline(CODE, "1.0.0", "x").get("status"));
    }

    @Test
    @DisplayName("核实 M1：最后一个已发布版本下架后商品回落 draft（§7.4，无需 M5b 重复实现）")
    void offlineFallsBackToDraftWhenNoPublishedVersionLeft() {
        when(versionMapper.selectOne(any())).thenReturn(version(1L, "1.0.0", PluginVersionStatus.PUBLISHED));
        MarketPlugin plugin = plugin();
        when(pluginMapper.selectOne(any())).thenReturn(plugin);
        // 已发布版本集合为空 → latestVersion=null → 商品回落 draft
        when(versionMapper.selectList(any())).thenReturn(List.of());

        new PluginPublishService(versionMapper, pluginMapper, null, publishedEvents::add)
                .offline(CODE, "1.0.0", "违规");

        assertEquals(PluginProductStatus.DRAFT.getCode(), plugin.getStatus());
    }

    @Test
    @DisplayName("仍有其它已发布版本时商品保持上架（下架单个版本不该让整个商品消失）")
    void offlineKeepsProductPublishedWhenOtherVersionsRemain() {
        when(versionMapper.selectOne(any())).thenReturn(version(1L, "1.0.0", PluginVersionStatus.PUBLISHED));
        MarketPlugin plugin = plugin();
        when(pluginMapper.selectOne(any())).thenReturn(plugin);
        when(versionMapper.selectList(any()))
                .thenReturn(List.of(version(2L, "1.1.0", PluginVersionStatus.PUBLISHED)));

        new PluginPublishService(versionMapper, pluginMapper, null, publishedEvents::add)
                .offline(CODE, "1.0.0", "违规");

        assertEquals(PluginProductStatus.PUBLISHED.getCode(), plugin.getStatus());
        assertEquals("1.1.0", plugin.getLatestVersion());
    }
}
