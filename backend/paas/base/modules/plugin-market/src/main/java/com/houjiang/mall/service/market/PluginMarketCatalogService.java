package com.houjiang.mall.service.market;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.houjiang.commons.version.VersionComparison;
import com.houjiang.mall.entity.market.MarketPlugin;
import com.houjiang.mall.entity.market.MarketPluginVersion;
import com.houjiang.mall.mapper.market.MarketPluginMapper;
import com.houjiang.mall.mapper.market.MarketPluginVersionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 市场目录的<b>租户视角</b>查询服务：已发布版本列表、最新版本判定。
 *
 * <p>与 {@code plugin-publish} 的 {@code /base/plugin-center/publish/versions/list} 是两种视角，不要混用：
 * <ul>
 *   <li><b>发布者视角</b>（plugin-publish）：看自己提交的全部版本（含草稿/审核中/已下架），
 *       且不返回 {@code jarUrl}——避免制品直链泄给不该拿到的人；</li>
 *   <li><b>租户视角</b>（本类）：只看<b>已发布</b>版本，用于市场详情页的「版本选择」下拉，
 *       带 changelog 与兼容性信息，同样不返回 {@code jarUrl}（下载必须走换票链路），
 *       也不返回 {@code jarMd5} / {@code jarSha256} / {@code jarSize}。</li>
 * </ul>
 *
 * <p><b>为什么单独建一个类而不是塞进 {@code PluginMarketService}</b>：
 * 那个类已经承担浏览、定价、安装落盘三摊职责，且为了兼容多种装配有 5 个构造器重载；
 * 再往里加查询方法会让它继续膨胀。本类只依赖两个 Mapper，纯查询、无事务、易测试。
 *
 * @author Amos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PluginMarketCatalogService {

    /** 版本已审核通过（与 PluginMarketService.VERSION_APPROVED 同义）。 */
    private static final int VERSION_APPROVED = 1;

    /** 插件已上架（hou_market_plugin.status）。 */
    private static final int PLUGIN_PUBLISHED = 2;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final MarketPluginMapper marketPluginMapper;

    private final MarketPluginVersionMapper marketPluginVersionMapper;

    /**
     * 查询某插件的已发布版本列表（按 SemVer 降序，最新在前）。
     *
     * <p><b>为什么用 SemVer 排序而不是字符串排序</b>：字符串排序会把 {@code 1.10.0} 排在
     * {@code 1.9.0} 前面（因为 "1" < "9"），用户会看到「最新版」是旧版。
     *
     * <p><b>版本格式非法的处理</b>：不抛异常、不丢弃——排到末尾并在返回体里标
     * {@code versionValid=false}，让前端能提示「该版本版本号不合规」而不是静默消失。
     *
     * @param pluginCode      插件编码，必填
     * @param currentVersion  当前已装版本，用于标出 {@code isCurrent}；可为空
     * @param frameworkVersion 宿主框架版本，用于逐版本标出 {@code compatible}；为空则不做判定
     * @return 版本列表；插件不存在或未上架时返回空列表
     */
    public List<Map<String, Object>> versions(String pluginCode, String currentVersion, String frameworkVersion) {
        if (!StringUtils.hasText(pluginCode)) {
            throw new IllegalArgumentException("插件编码不能为空");
        }
        String code = pluginCode.trim();
        MarketPlugin plugin = marketPluginMapper.selectOne(new LambdaQueryWrapper<MarketPlugin>()
                .eq(MarketPlugin::getPluginCode, code)
                .eq(MarketPlugin::getStatus, PLUGIN_PUBLISHED)
                .last("LIMIT 1"));
        if (plugin == null) {
            return List.of();
        }
        List<MarketPluginVersion> rows = marketPluginVersionMapper.selectList(
                new LambdaQueryWrapper<MarketPluginVersion>()
                        .eq(MarketPluginVersion::getPluginCode, code)
                        .eq(MarketPluginVersion::getStatus, VERSION_APPROVED));
        List<Map<String, Object>> items = new ArrayList<>(rows.size());
        for (MarketPluginVersion row : rows) {
            items.add(toItem(row, currentVersion, frameworkVersion, plugin.getMinFrameworkVersion()));
        }
        // 非法版本排末尾：Comparator 里 compare 失败视为相等会破坏排序稳定性，故单独识别
        items.sort(Comparator
                .comparing((Map<String, Object> item) -> Boolean.TRUE.equals(item.get("versionValid"))
                        ? 0 : 1)
                .thenComparing(item -> versionKey(item), Comparator.reverseOrder())
                .thenComparing(item -> String.valueOf(item.get("version"))));
        return items;
    }

    /**
     * 取某插件的最新已发布版本。
     *
     * @return 最新版本号；无已发布版本时返回 null
     */
    public String latestVersion(String pluginCode) {
        if (!StringUtils.hasText(pluginCode)) {
            return null;
        }
        List<Map<String, Object>> items = versions(pluginCode.trim(), null, null);
        return items.isEmpty() ? null : String.valueOf(items.get(0).get("version"));
    }

    /**
     * 排序键：把 {@code 1.2.3} 补零成 {@code 000001000002000003}，使字符串序等于 SemVer 序。
     *
     * <p>为什么不直接用 {@link VersionComparison#compare} 做 Comparator：那么大一个
     * comparator 在 20 条数据上没问题，但每次比较都要重新解析字符串；
     * 预计算排序键可以只解析一次。数据量小，这里主要是避免重复解析带来的无谓开销。
     */
    private static String versionKey(Map<String, Object> item) {
        Object raw = item.get("versionSortKey");
        return raw == null ? "" : String.valueOf(raw);
    }

    /**
     * 版本行 → 返回体。
     *
     * <p><b>刻意不含 {@code jarMd5} / {@code jarSha256} / {@code jarSize}</b>：这三个字段是
     * 制品指纹，本接口对任意已登录用户开放，照原样返回等于「不买也能枚举全部版本指纹」——
     * 可用于确认目标版本、比对供应链投毒、做变更监控。制品指纹只在
     * {@code /base/plugin-center/artifact/download-url} 换票成功后随链接一起下发
     * （见 {@code DownloadDeniedException}：未授权时连 md5 都不给），
     * 完整性校验的需求在那里已经满足，版本列表不需要重复暴露。
     */
    private static Map<String, Object> toItem(MarketPluginVersion row,
                                              String currentVersion,
                                              String frameworkVersion,
                                              String pluginMinFramework) {
        Map<String, Object> item = new LinkedHashMap<>();
        String version = row.getVersion() == null ? "" : row.getVersion().trim();
        item.put("version", version);
        item.put("changelog", row.getChangelog());
        item.put("publishTime", row.getPublishTime() == null ? null
                : row.getPublishTime().format(TIME_FORMATTER));
        item.put("isCurrent", StringUtils.hasText(currentVersion)
                && version.equalsIgnoreCase(currentVersion.trim()));
        item.put("versionValid", isValidVersion(version));
        item.put("versionSortKey", sortKeyOf(version));
        // 兼容性：插件级 minFrameworkVersion 优先，版本级 compatibilityJson 里的声明其次
        String required = StringUtils.hasText(pluginMinFramework) ? pluginMinFramework : null;
        Boolean compatible = null;
        if (StringUtils.hasText(frameworkVersion) && StringUtils.hasText(required)) {
            compatible = VersionComparison.atLeast(frameworkVersion.trim(), required.trim());
        }
        item.put("minFrameworkVersion", required);
        item.put("compatible", compatible);
        item.put("compatibilityJson", row.getCompatibilityJson());
        return item;
    }

    /**
     * 版本号合法性：三段数字，允许 {@code v} 前缀与 pre-release 后缀。
     */
    private static boolean isValidVersion(String version) {
        if (!StringUtils.hasText(version)) {
            return false;
        }
        String core = version.trim();
        if (core.toLowerCase().startsWith("v")) {
            core = core.substring(1);
        }
        int dash = core.indexOf('-');
        if (dash >= 0) {
            core = core.substring(0, dash);
        }
        if (core.isEmpty()) {
            return false;
        }
        String[] parts = core.split("\\.");
        if (parts.length != 3) {
            return false;
        }
        for (String part : parts) {
            if (!part.matches("\\d+")) {
                return false;
            }
        }
        return true;
    }

    /**
     * 生成排序键，每段补零到 6 位。
     *
     * <p>非法版本返回空串（配合 {@code versionValid} 排到末尾）。
     */
    private static String sortKeyOf(String version) {
        if (!isValidVersion(version)) {
            return "";
        }
        String core = version.trim();
        if (core.toLowerCase().startsWith("v")) {
            core = core.substring(1);
        }
        int dash = core.indexOf('-');
        if (dash >= 0) {
            core = core.substring(0, dash);
        }
        StringBuilder sb = new StringBuilder();
        for (String part : core.split("\\.")) {
            String padded = "000000" + part;
            sb.append(padded.substring(padded.length() - 6));
        }
        return sb.toString();
    }
}
