package com.houjiang.plugin.publish.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.houjiang.commons.constant.PluginDistErrorCode;
import com.houjiang.commons.event.PluginVersionOfflineEvent;
import com.houjiang.mall.entity.market.MarketPlugin;
import com.houjiang.mall.entity.market.MarketPluginVersion;
import com.houjiang.mall.mapper.market.MarketPluginMapper;
import com.houjiang.mall.mapper.market.MarketPluginVersionMapper;
import com.houjiang.commons.version.PluginVersionComparator;
import com.houjiang.mall.service.market.cache.PluginCatalogCache;
import com.houjiang.mall.service.plugin.AddonPluginSyncService;
import com.houjiang.plugin.publish.exception.PluginPublishException;
import com.houjiang.plugin.publish.model.VersionListItem;
import com.houjiang.plugin.publish.state.PluginProductStatus;
import com.houjiang.plugin.publish.state.PluginVersionStateMachine;
import com.houjiang.plugin.publish.state.PluginVersionStatus;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 版本发布流程服务：提交审核 / 审核 / 下架 / 重新上架 / 列表。
 *
 * <p>所有状态跃迁<b>必须</b>经 {@link PluginVersionStateMachine} 收口，
 * 不允许在业务代码里直接 setStatus。
 *
 * @author Amos
 * @since M1
 */
@Slf4j
@Service
public class PluginPublishService {

    /**
     * 审核决策：通过。
     */
    public static final String DECISION_APPROVE = "APPROVE";

    /**
     * 审核决策：驳回。
     */
    public static final String DECISION_REJECT = "REJECT";

    private final MarketPluginVersionMapper versionMapper;

    private final MarketPluginMapper pluginMapper;

    /**
     * 目录索引缓存（M5）：发布侧变更后负责让目录缓存失效。
     *
     * <p>为 null 表示未装配（单测 / 未引入缓存的场景），此时所有失效动作是空操作——
     * 缓存有 TTL 兜底，最坏情况是目录最多脏 5 分钟，不会错到「永远刷新不了」。
     */
    private final PluginCatalogCache catalogCache;

    /**
     * 事件发布器（M5b）：下架时广播 {@link PluginVersionOfflineEvent}。
     *
     * <p>为 null 表示未装配（单测），此时只下架、不发事件——
     * 事件是「联动」不是「主流程」，缺少发布器不该让下架失败。
     */
    private final ApplicationEventPublisher eventPublisher;

    /** 已发布清单 → hou_addon_plugin 正式目录同步。 */
    private final AddonPluginSyncService addonPluginSyncService;

    /**
     * 构造服务（不装配目录缓存与事件发布器）。
     *
     * @param versionMapper 版本 Mapper
     * @param pluginMapper  商品 Mapper
     */
    public PluginPublishService(MarketPluginVersionMapper versionMapper, MarketPluginMapper pluginMapper) {
        this(versionMapper, pluginMapper, null, null, null);
    }

    /**
     * 构造服务（M5：带目录缓存）。
     *
     * @param versionMapper 版本 Mapper
     * @param pluginMapper  商品 Mapper
     * @param catalogCache  目录索引缓存，可为 null
     */
    public PluginPublishService(MarketPluginVersionMapper versionMapper, MarketPluginMapper pluginMapper,
                                PluginCatalogCache catalogCache) {
        this(versionMapper, pluginMapper, catalogCache, null, null);
    }

    /**
     * 构造服务。
     *
     * @param versionMapper   版本 Mapper
     * @param pluginMapper    商品 Mapper
     * @param catalogCache    目录索引缓存，可为 null
     * @param eventPublisher  事件发布器，可为 null
     */
    public PluginPublishService(MarketPluginVersionMapper versionMapper, MarketPluginMapper pluginMapper,
                                PluginCatalogCache catalogCache, ApplicationEventPublisher eventPublisher) {
        this(versionMapper, pluginMapper, catalogCache, eventPublisher, null);
    }

    @Autowired
    public PluginPublishService(MarketPluginVersionMapper versionMapper, MarketPluginMapper pluginMapper,
                                PluginCatalogCache catalogCache, ApplicationEventPublisher eventPublisher,
                                AddonPluginSyncService addonPluginSyncService) {
        this.versionMapper = Objects.requireNonNull(versionMapper, "versionMapper 不能为空");
        this.pluginMapper = Objects.requireNonNull(pluginMapper, "pluginMapper 不能为空");
        this.catalogCache = catalogCache;
        this.eventPublisher = eventPublisher;
        this.addonPluginSyncService = addonPluginSyncService;
    }

    /**
     * 提交审核：draft(0)/rejected(4) → pending(3)。
     *
     * @param pluginCode 插件编码
     * @param version    版本号
     * @return 状态快照
     */
    public Map<String, Object> submit(String pluginCode, String version) {
        MarketPluginVersion row = requireVersion(pluginCode, version);
        PluginVersionStatus from = PluginVersionStatus.fromCode(row.getStatus());
        PluginVersionStateMachine.assertTransition(from, PluginVersionStatus.PENDING);
        row.setStatus(PluginVersionStatus.PENDING.getCode());
        row.setSubmitTime(LocalDateTime.now());
        row.setReviewNote(null);
        versionMapper.updateById(row);
        return snapshot(row);
    }

    /**
     * 审核：pending(3) → published(1) / rejected(4)。
     *
     * <p>通过时必须做四件联动（方案 §5.1 最后一步）：
     * ① 版本 status=1 ② 商品 latest_version 刷为 SemVer 最大值
     * ③ 商品 status=2 ④ 写 publish_time。
     *
     * @param versionId 版本记录 ID
     * @param decision  APPROVE / REJECT
     * @param note      审核意见
     * @param reviewerId 审核人
     * @return 状态快照
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> review(Long versionId, String decision, String note, Long reviewerId) {
        if (versionId == null) {
            throw new PluginPublishException(PluginDistErrorCode.MANIFEST_INVALID, "versionId 不能为空");
        }
        MarketPluginVersion row = versionMapper.selectById(versionId);
        if (row == null) {
            throw new PluginPublishException(PluginDistErrorCode.ARTIFACT_NOT_FOUND,
                    "版本记录不存在：versionId=" + versionId);
        }
        PluginVersionStatus from = PluginVersionStatus.fromCode(row.getStatus());
        boolean approve = DECISION_APPROVE.equalsIgnoreCase(decision);
        boolean reject = DECISION_REJECT.equalsIgnoreCase(decision);
        if (!approve && !reject) {
            throw new PluginPublishException(PluginDistErrorCode.MANIFEST_INVALID,
                    "审核决策只能是 APPROVE / REJECT，实际：" + decision);
        }

        PluginVersionStatus target = approve ? PluginVersionStatus.PUBLISHED : PluginVersionStatus.REJECTED;
        PluginVersionStateMachine.assertTransition(from, target);

        row.setReviewerId(reviewerId);
        row.setReviewNote(note);
        if (approve) {
            row.setStatus(PluginVersionStatus.PUBLISHED.getCode());
            row.setPublishTime(LocalDateTime.now());
        } else {
            row.setStatus(PluginVersionStatus.REJECTED.getCode());
        }
        versionMapper.updateById(row);

        if (approve) {
            syncPluginOnPublish(row.getPluginCode());
            syncPublishedManifest(row);
        }
        return snapshot(row);
    }

    /**
     * 下架：published(1) → offline(5)。
     *
     * @param pluginCode 插件编码
     * @param version    版本号
     * @param reason     下架原因
     * @return 状态快照
     */
    public Map<String, Object> offline(String pluginCode, String version, String reason) {
        MarketPluginVersion row = requireVersion(pluginCode, version);
        PluginVersionStatus from = PluginVersionStatus.fromCode(row.getStatus());
        PluginVersionStateMachine.assertTransition(from, PluginVersionStatus.OFFLINE);
        row.setStatus(PluginVersionStatus.OFFLINE.getCode());
        row.setOfflineTime(LocalDateTime.now());
        row.setOfflineReason(reason);
        versionMapper.updateById(row);
        // 重新计算商品的最新版本：下架的可能正是最新那个
        refreshLatestVersion(pluginCode);
        // 下架会让该版本从目录消失（列表与「有更新」都要立刻反映）
        invalidateCatalog(pluginCode);
        // 广播下架事件：由授权侧监听并吊销该插件的全部有效授权（方案 §7.4）。
        // 放在最后——事件只做联动，前两步（版本状态、目录失效）才是下架本身。
        publishOfflineEvent(pluginCode, version, reason);
        return snapshot(row);
    }

    /**
     * 广播版本下架事件（M5b）。
     *
     * <p>发布失败不能让下架失败：事件是联动动作，授权侧还有心跳兜底（扫描到「无有效授权」也会软停）。
     * 因此这里只记日志，不抛异常。
     *
     * @param pluginCode 插件编码
     * @param version    版本号
     * @param reason     下架原因
     */
    private void publishOfflineEvent(String pluginCode, String version, String reason) {
        if (eventPublisher == null) {
            return;
        }
        try {
            eventPublisher.publishEvent(new PluginVersionOfflineEvent(
                    this, pluginCode, version, reason, LocalDateTime.now()));
        } catch (RuntimeException e) {
            log.error("版本下架事件发布失败（下架本身已生效，授权侧由心跳兜底）: plugin={} version={} msg={}",
                    pluginCode, version, e.getMessage());
        }
    }

    /**
     * 重新上架：offline(5) → published(1)。
     *
     * <p>只允许「原版本」重新上架，<b>不允许换制品</b>：制品由上传链路决定且 published/offline 不可覆盖，
     * 因此这里只改状态、不动 storage_key / md5 / 签名。
     *
     * @param pluginCode 插件编码
     * @param version    版本号
     * @return 状态快照
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> relist(String pluginCode, String version) {
        MarketPluginVersion row = requireVersion(pluginCode, version);
        PluginVersionStatus from = PluginVersionStatus.fromCode(row.getStatus());
        PluginVersionStateMachine.assertTransition(from, PluginVersionStatus.PUBLISHED);
        row.setStatus(PluginVersionStatus.PUBLISHED.getCode());
        row.setOfflineReason(null);
        row.setOfflineTime(null);
        versionMapper.updateById(row);
        refreshLatestVersion(pluginCode);
        syncPublishedManifest(row);
        // 重新上架：该版本重新进入目录，「有更新」判定要重新可见
        invalidateCatalog(pluginCode);
        return snapshot(row);
    }

    private void syncPublishedManifest(MarketPluginVersion row) {
        if (addonPluginSyncService == null || row == null) {
            return;
        }
        if (!addonPluginSyncService.syncFromPluginYaml(row.getPluginYaml())) {
            throw new PluginPublishException(PluginDistErrorCode.MANIFEST_INVALID,
                    "已发布插件必须提供包含 code 与 hostService 的有效 plugin.yaml："
                            + row.getPluginCode() + "@" + row.getVersion());
        }
    }

    /**
     * 版本列表。
     *
     * <p><b>不返回 jar_url</b>（方案 §4.1）：制品地址只能经签名下载链接获取。
     *
     * @param pluginCode 插件编码
     * @param status     状态过滤，可为 null
     * @return 版本列表项
     */
    public List<VersionListItem> list(String pluginCode, Integer status) {
        if (pluginCode == null || pluginCode.isBlank()) {
            throw new PluginPublishException(PluginDistErrorCode.MANIFEST_INVALID, "pluginCode 不能为空");
        }
        LambdaQueryWrapper<MarketPluginVersion> wrapper =
                new LambdaQueryWrapper<MarketPluginVersion>()
                        .eq(MarketPluginVersion::getPluginCode, pluginCode.trim());
        if (status != null) {
            wrapper.eq(MarketPluginVersion::getStatus, status);
        }
        wrapper.orderByDesc(MarketPluginVersion::getCreateTime);
        List<MarketPluginVersion> rows = versionMapper.selectList(wrapper);
        List<VersionListItem> items = new ArrayList<>();
        if (rows == null) {
            return items;
        }
        for (MarketPluginVersion row : rows) {
            VersionListItem item = new VersionListItem();
            item.setVersion(row.getVersion());
            item.setStatus(row.getStatus());
            item.setStatusName(safeStatusName(row.getStatus()));
            item.setJarMd5(row.getJarMd5());
            item.setJarSize(row.getJarSize());
            item.setSignature(row.getJarSignature());
            item.setKid(row.getSignatureKid());
            item.setPublishTime(row.getPublishTime());
            items.add(item);
        }
        return items;
    }

    private void syncPluginOnPublish(String pluginCode) {
        MarketPlugin plugin = findPlugin(pluginCode);
        if (plugin == null) {
            log.warn("审核通过但未找到插件商品记录，跳过商品联动：pluginCode={}", pluginCode);
            return;
        }
        plugin.setStatus(PluginProductStatus.PUBLISHED.getCode());
        refreshLatestVersion(pluginCode);
        // M1 曾在这里留 TODO(M5)：Redis 目录缓存失效。M5 补上——
        // 审核通过后商品进目录、latest_version 变化，目录列表与「有更新」都必须立刻刷新，
        // 否则租户看到的是「市场里有、点了却说没更新」的分裂状态。
        invalidateCatalog(pluginCode);
    }

    /**
     * 让目录缓存反映本次变更（M5）。
     *
     * <p>两件事都要做，缺一不可：
     * <ol>
     *   <li>{@code bumpRevision()} —— 一次 INCR 让所有分页缓存整体失效（不逐个删 key，
     *       否则关键字 × 分页组合的 key 数量没有上界，会造成雪崩与 key 爆炸）；</li>
     *   <li>{@code invalidateLatest()} —— 单插件最新版本是<b>独立</b>缓存
     *       （{@code hou:idx:latest:{code}}，不进分页缓存、revision 不参与 key），
     *       必须单独删，否则「有更新」判定会一直用旧的 latest_version。</li>
     * </ol>
     *
     * @param pluginCode 插件编码
     */
    private void invalidateCatalog(String pluginCode) {
        if (catalogCache == null) {
            return;
        }
        catalogCache.bumpRevision();
        catalogCache.invalidateLatest(pluginCode);
    }

    /**
     * 把商品的最新版本刷为「已发布版本中的 SemVer 最大值」。
     *
     * @param pluginCode 插件编码
     */
    private void refreshLatestVersion(String pluginCode) {
        MarketPlugin plugin = findPlugin(pluginCode);
        if (plugin == null) {
            return;
        }
        List<MarketPluginVersion> published = versionMapper.selectList(
                new LambdaQueryWrapper<MarketPluginVersion>()
                        .eq(MarketPluginVersion::getPluginCode, pluginCode)
                        .eq(MarketPluginVersion::getStatus, PluginVersionStatus.PUBLISHED.getCode()));
        // 与目录侧「有更新」判定共用同一个 maxOf（同一份 SemVer 实现）：
        // 若两边各写一遍取最大值，1.10.0 vs 1.9.0 的结果可能不一致，
        // 表现就是「发布时说最新是 1.10.0、目录里却说没更新」。
        List<String> candidates = new ArrayList<>();
        if (published != null) {
            for (MarketPluginVersion item : published) {
                if (item.getVersion() != null) {
                    candidates.add(item.getVersion());
                }
            }
        }
        String latest = PluginVersionComparator.maxOf(candidates);
        plugin.setLatestVersion(latest);
        if (latest == null && Integer.valueOf(PluginProductStatus.PUBLISHED.getCode()).equals(plugin.getStatus())) {
            // 已发布版本被全部下架：商品回落到草稿态，避免市场列表出现「有商品无版本」的脏数据
            plugin.setStatus(PluginProductStatus.DRAFT.getCode());
        }
        plugin.setUpdateTime(LocalDateTime.now());
        pluginMapper.updateById(plugin);
    }

    private MarketPluginVersion requireVersion(String pluginCode, String version) {
        if (pluginCode == null || pluginCode.isBlank() || version == null || version.isBlank()) {
            throw new PluginPublishException(PluginDistErrorCode.MANIFEST_INVALID, "pluginCode 与 version 均不能为空");
        }
        MarketPluginVersion row = versionMapper.selectOne(new LambdaQueryWrapper<MarketPluginVersion>()
                .eq(MarketPluginVersion::getPluginCode, pluginCode.trim())
                .eq(MarketPluginVersion::getVersion, version.trim())
                .last("LIMIT 1"));
        if (row == null) {
            throw new PluginPublishException(PluginDistErrorCode.ARTIFACT_NOT_FOUND,
                    "版本不存在：" + pluginCode + "@" + version);
        }
        return row;
    }

    private MarketPlugin findPlugin(String pluginCode) {
        return pluginMapper.selectOne(new LambdaQueryWrapper<MarketPlugin>()
                .eq(MarketPlugin::getPluginCode, pluginCode)
                .last("LIMIT 1"));
    }

    private static String safeStatusName(Integer status) {
        try {
            return PluginVersionStatus.fromCode(status).getDescription();
        } catch (IllegalArgumentException e) {
            return "未知(" + status + ")";
        }
    }

    private static Map<String, Object> snapshot(MarketPluginVersion row) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("versionId", row.getId());
        snapshot.put("pluginCode", row.getPluginCode());
        snapshot.put("version", row.getVersion());
        snapshot.put("status", row.getStatus());
        snapshot.put("statusName", safeStatusName(row.getStatus()));
        snapshot.put("publishTime", row.getPublishTime());
        return snapshot;
    }
}
