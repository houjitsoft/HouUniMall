package com.houjiang.mall.service.market;

import com.houjiang.mall.mapper.market.MarketPluginDownloadMapper;
import com.houjiang.mall.service.market.cache.PluginCatalogCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 插件下载次数统计（明细为准，计数回写）。
 *
 * <p><b>口径（别改反）</b>：</p>
 * <ul>
 *   <li>明细表 {@code hou_market_plugin_download} 是<b>唯一权威</b>，一行 = 一个站点的一次真实下载；</li>
 *   <li>{@code hou_market_plugin.download_count} 是派生字段，每次明细变化后按插件<b>重算回写</b>，
 *       不做自增——自增在重复上报 / 安装回滚 / 手工修数后必然与明细对不上；</li>
 *   <li>唯一键 {@code (site_code, plugin_code, version)}：同一站点同一版本重复安装不会刷量。</li>
 * </ul>
 *
 * <p><b>失败不阻断安装</b>：统计是旁路能力，写失败只记 warn，绝不能把一次成功安装变成失败——
 * 调用方（{@link PluginMarketService#installFromMarket}）在事务里调用，这里的异常一律吞掉。</p>
 *
 * @author Amos
 */
@Slf4j
@Service
public class PluginDownloadStatsService {

    /** 平台自身的站点标识（unimall.houjit.com）。 */
    public static final String SITE_PLATFORM = "platform";

    private final MarketPluginDownloadMapper downloadMapper;

    /**
     * 目录索引缓存（可选）：有新下载时整体失效列表缓存，否则平台页最多要等 5 分钟才看到新数字。
     */
    private PluginCatalogCache catalogCache;

    public PluginDownloadStatsService(MarketPluginDownloadMapper downloadMapper) {
        this.downloadMapper = downloadMapper;
    }

    @Autowired(required = false)
    public void setCatalogCache(PluginCatalogCache catalogCache) {
        this.catalogCache = catalogCache;
    }

    /**
     * 记录一次下载 / 安装。
     *
     * @param pluginCode 插件编码
     * @param version    版本号（可空）
     * @param siteCode   下载方站点标识（平台自身传 {@link #SITE_PLATFORM}）
     * @param siteName   站点名称（可空）
     * @param source     来源：install / download / platform
     * @param clientIp   客户端 IP（可空）
     * @return true = 这是一次真实新增（非重复上报）
     */
    public boolean record(String pluginCode, String version, String siteCode, String siteName,
                          String source, String clientIp) {
        if (!StringUtils.hasText(pluginCode)) {
            return false;
        }
        String code = pluginCode.trim();
        String ver = version == null ? "" : version.trim();
        String site = StringUtils.hasText(siteCode) ? siteCode.trim() : SITE_PLATFORM;
        try {
            int inserted = downloadMapper.insertIgnore(code, ver, site,
                    siteName == null ? "" : siteName.trim(),
                    StringUtils.hasText(source) ? source.trim() : "install",
                    clientIp == null ? "" : clientIp.trim());
            if (inserted <= 0) {
                // 重复上报（同站点同版本）：不刷量，也不回写
                return false;
            }
            downloadMapper.syncDownloadCount(code);
            if (catalogCache != null) {
                try {
                    catalogCache.bumpRevision();
                } catch (RuntimeException cacheError) {
                    log.debug("下载计数回写后失效目录缓存失败（不影响计数）: {}", cacheError.getMessage());
                }
            }
            return true;
        } catch (RuntimeException e) {
            // 明细表缺失（老库未执行迁移）时也不能让安装失败
            log.warn("记录插件下载明细失败（不影响安装）: code={} site={} err={}", code, site, e.getMessage());
            return false;
        }
    }

    /**
     * 批量取真实下载次数。
     *
     * @param codes 插件编码
     * @return code → 次数；空入参返回空 Map
     */
    public Map<String, Integer> counts(List<String> codes) {
        if (codes == null || codes.isEmpty()) {
            return Map.of();
        }
        try {
            List<Map<String, Object>> rows = downloadMapper.countByCodes(codes);
            Map<String, Integer> result = new LinkedHashMap<>();
            if (rows == null) {
                return result;
            }
            for (Map<String, Object> row : rows) {
                Object code = row.get("code");
                Object cnt = row.get("cnt");
                if (code == null) {
                    continue;
                }
                result.put(String.valueOf(code), cnt == null ? 0 : ((Number) cnt).intValue());
            }
            return result;
        } catch (RuntimeException e) {
            log.warn("聚合插件下载次数失败（回退计数器字段）: {}", e.getMessage());
            return Map.of();
        }
    }

    /**
     * 单插件统计。
     *
     * @param pluginCode 插件编码
     * @return {downloadCount, siteCount}
     */
    public Map<String, Object> statsOf(String pluginCode) {
        Map<String, Object> result = new LinkedHashMap<>();
        int downloadCount = counts(List.of(pluginCode)).getOrDefault(pluginCode, 0);
        result.put("code", pluginCode);
        result.put("downloadCount", downloadCount);
        try {
            result.put("siteCount", downloadMapper.countSites(pluginCode));
        } catch (RuntimeException e) {
            result.put("siteCount", 0);
        }
        return result;
    }
}
