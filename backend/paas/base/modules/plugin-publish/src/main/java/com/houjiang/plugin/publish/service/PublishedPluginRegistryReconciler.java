package com.houjiang.plugin.publish.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.houjiang.commons.version.PluginVersionComparator;
import com.houjiang.mall.entity.market.MarketPluginVersion;
import com.houjiang.mall.mapper.market.MarketPluginVersionMapper;
import com.houjiang.mall.service.plugin.AddonPluginSyncService;
import com.houjiang.plugin.publish.state.PluginVersionStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 启动时把已发布版本的 plugin.yaml 幂等补入站点插件目录。 */
@Slf4j
@Component
public class PublishedPluginRegistryReconciler {

    private final MarketPluginVersionMapper versionMapper;
    private final AddonPluginSyncService syncService;

    public PublishedPluginRegistryReconciler(MarketPluginVersionMapper versionMapper,
                                             AddonPluginSyncService syncService) {
        this.versionMapper = versionMapper;
        this.syncService = syncService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void reconcile() {
        try {
            doReconcile();
        } catch (RuntimeException e) {
            log.error("已发布插件目录补账失败，跳过本轮（不影响 base 启动）", e);
        }
    }

    void doReconcile() {
        List<MarketPluginVersion> published = versionMapper.selectList(
                new LambdaQueryWrapper<MarketPluginVersion>()
                        .eq(MarketPluginVersion::getStatus, PluginVersionStatus.PUBLISHED.getCode()));
        Map<String, MarketPluginVersion> latestByCode = new LinkedHashMap<>();
        for (MarketPluginVersion row : published == null ? List.<MarketPluginVersion>of() : published) {
            if (row == null || !StringUtils.hasText(row.getPluginCode())
                    || !StringUtils.hasText(row.getPluginYaml())
                    || !PluginVersionComparator.isValid(row.getVersion())) {
                continue;
            }
            MarketPluginVersion current = latestByCode.get(row.getPluginCode());
            if (current == null || isNewer(row.getVersion(), current.getVersion())) {
                latestByCode.put(row.getPluginCode(), row);
            }
        }
        int synced = 0;
        for (MarketPluginVersion row : latestByCode.values()) {
            try {
                if (syncService.syncFromPluginYaml(row.getPluginYaml())) {
                    synced++;
                }
            } catch (RuntimeException e) {
                log.error("已发布插件目录补账失败: plugin={} version={}",
                        row.getPluginCode(), row.getVersion(), e);
            }
        }
        log.info("已发布插件目录补账完成: published={} synced={}", latestByCode.size(), synced);
    }

    private boolean isNewer(String candidate, String current) {
        if (!PluginVersionComparator.isValid(candidate)) {
            return false;
        }
        return !PluginVersionComparator.isValid(current)
                || PluginVersionComparator.compare(candidate, current) > 0;
    }
}
