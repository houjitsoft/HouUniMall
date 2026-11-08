package com.houjiang.plugin.publish.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.houjiang.commons.constant.PluginDistErrorCode;
import com.houjiang.mall.entity.market.MarketPlugin;
import com.houjiang.mall.entity.market.MarketPluginVersion;
import com.houjiang.mall.mapper.market.MarketPluginMapper;
import com.houjiang.mall.mapper.market.MarketPluginVersionMapper;
import com.houjiang.mall.service.plugin.AddonPluginSyncService;
import com.houjiang.plugin.publish.exception.PluginPublishException;
import com.houjiang.plugin.publish.model.VersionListItem;
import com.houjiang.plugin.publish.state.PluginProductStatus;
import com.houjiang.plugin.publish.state.PluginVersionStateMachine;
import com.houjiang.plugin.publish.state.PluginVersionStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link PluginPublishService} 单测：审核 / 下架 / 重新上架的跃迁合法性，
 * 以及审核通过时的商品联动（§5.1 最后一步）。
 *
 * @author Amos
 * @since M1
 */
class PluginPublishServiceTest {

    private static final String CODE = "coupon-demo";

    private MarketPluginVersionMapper versionMapper;

    private MarketPluginMapper pluginMapper;

    private PluginPublishService service;

    private AddonPluginSyncService addonPluginSyncService;

    /**
     * 捕获 updateById 的行。
     */
    private final List<MarketPluginVersion> updatedVersions = new ArrayList<>();

    private final List<MarketPlugin> updatedPlugins = new ArrayList<>();

    @BeforeEach
    void setUp() {
        versionMapper = mock(MarketPluginVersionMapper.class);
        pluginMapper = mock(MarketPluginMapper.class);
        addonPluginSyncService = mock(AddonPluginSyncService.class);
        service = new PluginPublishService(versionMapper, pluginMapper, null, null, addonPluginSyncService);

        when(versionMapper.updateById(any(MarketPluginVersion.class))).thenAnswer(invocation -> {
            updatedVersions.add(invocation.getArgument(0));
            return 1;
        });
        when(pluginMapper.updateById(any(MarketPlugin.class))).thenAnswer(invocation -> {
            updatedPlugins.add(invocation.getArgument(0));
            return 1;
        });
        when(addonPluginSyncService.syncFromPluginYaml(anyString())).thenReturn(true);
    }

    @Test
    @DisplayName("submit：draft → pending")
    void submitDraftToPending() {
        when(versionMapper.selectOne(any())).thenReturn(version(101L, "1.0.0", PluginVersionStatus.DRAFT));

        Map<String, Object> snapshot = service.submit(CODE, "1.0.0");

        assertEquals(PluginVersionStatus.PENDING.getCode(), snapshot.get("status"));
        assertEquals(PluginVersionStatus.PENDING.getCode(), updatedVersions.get(0).getStatus());
        assertNotNull(updatedVersions.get(0).getSubmitTime());
    }

    @Test
    @DisplayName("submit：rejected → pending（驳回后可修改重提交）")
    void submitRejectedToPending() {
        when(versionMapper.selectOne(any())).thenReturn(version(102L, "1.0.0", PluginVersionStatus.REJECTED));
        Map<String, Object> snapshot = service.submit(CODE, "1.0.0");
        assertEquals(PluginVersionStatus.PENDING.getCode(), snapshot.get("status"));
    }

    @Test
    @DisplayName("review APPROVE：版本 status=1 + 商品 status=2 + latest_version 取 SemVer 最大")
    void reviewApproveTriggersPluginLinkage() {
        MarketPluginVersion pending = version(103L, "1.10.0", PluginVersionStatus.PENDING);
        pending.setPluginYaml("code: coupon-demo\nversion: 1.10.0\nhostService: order\n");
        when(versionMapper.selectById(103L)).thenReturn(pending);
        when(pluginMapper.selectOne(any())).thenReturn(plugin(1));
        // 已发布版本里有一个 1.9.0：字符串比较会选错，必须按 SemVer 选出 1.10.0
        when(versionMapper.selectList(any())).thenReturn(List.of(
                version(104L, "1.9.0", PluginVersionStatus.PUBLISHED),
                version(103L, "1.10.0", PluginVersionStatus.PUBLISHED)));

        Map<String, Object> snapshot = service.review(103L, "APPROVE", "ok", 9L);

        assertEquals(PluginVersionStatus.PUBLISHED.getCode(), snapshot.get("status"));
        MarketPluginVersion saved = updatedVersions.get(0);
        assertEquals(PluginVersionStatus.PUBLISHED.getCode(), saved.getStatus());
        assertNotNull(saved.getPublishTime());
        assertEquals(9L, saved.getReviewerId());

        MarketPlugin savedPlugin = updatedPlugins.get(0);
        assertEquals(PluginProductStatus.PUBLISHED.getCode(), savedPlugin.getStatus());
        assertEquals("1.10.0", savedPlugin.getLatestVersion(), "latest_version 必须是 SemVer 最大值");
        verify(addonPluginSyncService).syncFromPluginYaml(pending.getPluginYaml());
    }

    @Test
    @DisplayName("review REJECT：pending → rejected")
    void reviewReject() {
        when(versionMapper.selectById(105L)).thenReturn(version(105L, "1.0.0", PluginVersionStatus.PENDING));
        Map<String, Object> snapshot = service.review(105L, "REJECT", "缺少更新说明", 9L);
        assertEquals(PluginVersionStatus.REJECTED.getCode(), snapshot.get("status"));
        assertEquals("缺少更新说明", updatedVersions.get(0).getReviewNote());
    }

    @Test
    @DisplayName("review APPROVE：正式目录同步失败时拒绝发布")
    void reviewApproveRejectsWhenPublishedCatalogCannotBeRegistered() {
        MarketPluginVersion pending = version(112L, "1.0.0", PluginVersionStatus.PENDING);
        pending.setPluginYaml("code: coupon-demo\nversion: 1.0.0\n");
        when(versionMapper.selectById(112L)).thenReturn(pending);
        when(pluginMapper.selectOne(any())).thenReturn(plugin(1));
        when(versionMapper.selectList(any())).thenReturn(List.of(pending));
        when(addonPluginSyncService.syncFromPluginYaml(anyString())).thenReturn(false);

        PluginPublishException error = assertThrows(PluginPublishException.class,
                () -> service.review(112L, "APPROVE", "ok", 9L));

        assertEquals(PluginDistErrorCode.MANIFEST_INVALID, error.getCode());
    }

    @Test
    @DisplayName("review：非 pending 状态不允许审核（draft 直接审核被拒）")
    void reviewFromDraftRejected() {
        when(versionMapper.selectById(106L)).thenReturn(version(106L, "1.0.0", PluginVersionStatus.DRAFT));
        assertThrows(PluginVersionStateMachine.PluginStateTransitionException.class,
                () -> service.review(106L, "APPROVE", null, 9L));
    }

    @Test
    @DisplayName("review：决策值非法直接拒绝")
    void reviewWithBadDecision() {
        when(versionMapper.selectById(107L)).thenReturn(version(107L, "1.0.0", PluginVersionStatus.PENDING));
        PluginPublishException e = assertThrows(PluginPublishException.class,
                () -> service.review(107L, "MAYBE", null, 9L));
        assertEquals(PluginDistErrorCode.MANIFEST_INVALID, e.getCode());
    }

    @Test
    @DisplayName("offline：published → offline，并记录原因与时间")
    void offlinePublished() {
        MarketPluginVersion published = version(108L, "1.0.0", PluginVersionStatus.PUBLISHED);
        when(versionMapper.selectOne(any())).thenReturn(published);
        when(pluginMapper.selectOne(any())).thenReturn(plugin(1));
        when(versionMapper.selectList(any())).thenReturn(List.of());

        Map<String, Object> snapshot = service.offline(CODE, "1.0.0", "发现严重缺陷");

        assertEquals(PluginVersionStatus.OFFLINE.getCode(), snapshot.get("status"));
        assertEquals("发现严重缺陷", updatedVersions.get(0).getOfflineReason());
        assertNotNull(updatedVersions.get(0).getOfflineTime());
    }

    @Test
    @DisplayName("relist：offline → published（原版本重新上架）")
    void relistOffline() {
        MarketPluginVersion offline = version(109L, "1.0.0", PluginVersionStatus.OFFLINE);
        offline.setPluginYaml("code: coupon-demo\nversion: 1.0.0\nhostService: order\n");
        when(versionMapper.selectOne(any())).thenReturn(offline);
        when(pluginMapper.selectOne(any())).thenReturn(plugin(1));
        when(versionMapper.selectList(any())).thenReturn(List.of(
                version(109L, "1.0.0", PluginVersionStatus.PUBLISHED)));

        Map<String, Object> snapshot = service.relist(CODE, "1.0.0");

        assertEquals(PluginVersionStatus.PUBLISHED.getCode(), snapshot.get("status"));
        assertNull(updatedVersions.get(0).getOfflineReason());
        assertEquals("1.0.0", updatedPlugins.get(0).getLatestVersion());
        verify(addonPluginSyncService).syncFromPluginYaml(offline.getPluginYaml());
    }

    @Test
    @DisplayName("list：返回字段齐备且不含 jar_url")
    void listWithoutJarUrl() {
        MarketPluginVersion row = version(110L, "1.0.0", PluginVersionStatus.PUBLISHED);
        row.setJarMd5("md5-value");
        row.setJarSize(2048L);
        row.setJarSignature("sig");
        row.setSignatureKid("k2026");
        row.setJarUrl("/base/plugin-center/artifact/raw?aid=should-not-be-exposed");
        when(versionMapper.selectList(any())).thenReturn(List.of(row));

        List<VersionListItem> items = service.list(CODE, null);

        assertEquals(1, items.size());
        VersionListItem item = items.get(0);
        assertEquals("1.0.0", item.getVersion());
        assertEquals(PluginVersionStatus.PUBLISHED.getCode(), item.getStatus());
        assertEquals("已发布", item.getStatusName());
        assertEquals("md5-value", item.getJarMd5());
        assertEquals(2048L, item.getJarSize());
        assertEquals("sig", item.getSignature());
        assertEquals("k2026", item.getKid());
        // VersionListItem 结构上就没有 jarUrl 字段，从类型层面杜绝裸地址外泄
        assertFalse(hasJarUrlField(), "VersionListItem 不应存在 jarUrl 字段");
    }

    @Test
    @DisplayName("offline：published 之外的状态不允许下架")
    void offlineFromDraftRejected() {
        when(versionMapper.selectOne(any())).thenReturn(version(111L, "1.0.0", PluginVersionStatus.DRAFT));
        assertThrows(PluginVersionStateMachine.PluginStateTransitionException.class,
                () -> service.offline(CODE, "1.0.0", "x"));
    }

    private static boolean hasJarUrlField() {
        try {
            VersionListItem.class.getDeclaredField("jarUrl");
            return true;
        } catch (NoSuchFieldException e) {
            return false;
        }
    }

    private static MarketPluginVersion version(Long id, String version, PluginVersionStatus status) {
        MarketPluginVersion row = new MarketPluginVersion();
        row.setId(id);
        row.setPluginCode(CODE);
        row.setVersion(version);
        row.setStatus(status.getCode());
        return row;
    }

    private static MarketPlugin plugin(int status) {
        MarketPlugin plugin = new MarketPlugin();
        plugin.setId(1L);
        plugin.setPluginCode(CODE);
        plugin.setStatus(status);
        return plugin;
    }
}
