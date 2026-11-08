package com.houjiang.mall.service.market;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.houjiang.commons.version.PluginDependencyExpression;
import com.houjiang.commons.version.PluginDependencyParser;
import com.houjiang.commons.version.PluginVersionComparator;
import com.houjiang.commons.version.VersionComparison;
import com.houjiang.mall.entity.market.MarketPlugin;
import com.houjiang.mall.entity.market.MarketPluginLicense;
import com.houjiang.mall.entity.market.MarketPluginVersion;
import com.houjiang.mall.entity.plugin.MallPluginInstall;
import com.houjiang.mall.mapper.market.MarketPluginMapper;
import com.houjiang.mall.mapper.market.MarketPluginVersionMapper;
import com.houjiang.mall.mapper.plugin.MallPluginInstallMapper;
import com.houjiang.mall.model.market.CatalogUpdateBlockReason;
import com.houjiang.mall.model.market.CatalogUpdateItem;
import com.houjiang.mall.model.market.CatalogUpdateQuery;
import com.houjiang.mall.service.market.cache.PluginCatalogCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 目录「有更新」判定（P0-10）：插件中心拿本地已装清单问一次，就能知道哪些能升、哪些被拦。
 *
 * <p><b>为什么单独一个服务</b>：{@link PluginMarketService} 的构造器已经为了兼容
 * 各种装配场景膨胀到 5 个重载，再往里塞「缓存 + 框架版本 + 授权门控」三样依赖
 * 只会让它更难维护；而这三样本就是「更新判定」这一件事的内聚依赖。
 *
 * <p><b>三条硬性口径</b>：
 * <ol>
 *   <li><b>SemVer 比较，不是字符串比较</b>：字符串比较会得出 {@code 1.10.0 < 1.9.0}，
 *       表现为「反复提示升级到旧版本」。与发布侧共用
 *       {@link PluginVersionComparator}（同一份实现，不复制）；</li>
 *   <li><b>instanceId 必填（fail-closed）</b>：授权必须按站点收敛，
 *       漏传就拒绝，绝不退化成「不限实例」的宽松查询；</li>
 *   <li><b>框架版本约束</b>：市场最新版本的 {@code min_framework_version} 高于宿主版本时
 *       判定为「不可升级」并给出原因，而不是让用户点了升级才在预检里失败。</li>
 * </ol>
 *
 * <p><b>边界（诚实说明）</b>：
 * <ul>
 *   <li>{@code hou_addon_plugin_install} 是<b>站点本地表、没有 instance_id 列</b>，
 *       本服务的 instanceId 收敛体现在「入参强制 + 授权按 instance_id 查询」两处；
 *       安装态回退只按 pluginCode 取本库记录，不存在跨站点读取的可能；</li>
 *   <li>{@code upgradable_version} 回写是<b>尽力而为</b>：它只是目录页的缓存列，
 *       写失败不能让「有更新」判定本身失败，因此异常只记日志；</li>
 *   <li><b>依赖插件（{@code dependencies_json}）</b>：与升级前 {@code /precheck} 共用
 *       commons 的 {@link PluginDependencyParser} 与 {@link VersionComparison}，
 *       保证「目录说能升」与「预检说能装」是同一个结论——
 *       两段各判一套的后果是用户点了升级才在预检里被 3015 打回。</li>
 * </ul>
 *
 * <p><b>已知限制</b>：依赖声明是<b>商品级</b>的（挂在 {@code hou_market_plugin} 上，
 * 不是版本级）。因此 v2.0 新增的依赖会影响 v1.x 用户看到的升级提示——
 * 判定用的是商品行当前的依赖声明，而不是目标版本当时声明的依赖。
 *
 * @author Amos
 * @since M5
 */
@Slf4j
@Service
public class PluginCatalogUpdateService {

    /**
     * 商品已上架。
     */
    private static final int STATUS_PUBLISHED = 2;

    /**
     * 版本已发布（与 PluginMarketService.VERSION_APPROVED 同义）。
     */
    private static final int VERSION_PUBLISHED = 1;

    private final MarketPluginMapper marketPluginMapper;

    private final MarketPluginVersionMapper marketPluginVersionMapper;

    private final MallPluginInstallMapper pluginInstallMapper;

    /**
     * 授权门控；为 null 表示只读场景（单测），此时付费插件一律判定为未授权。
     */
    private final PluginLicenseService licenseService;

    /**
     * 目录索引缓存；为 null 表示未启用，全部回源。
     */
    private final PluginCatalogCache catalogCache;

    /**
     * 宿主框架版本：与 plugin-update 的 {@code houcloud.update.framework-version}
     * 共用同一个配置键，保证「预检」与「有更新判定」看到的是同一个值。
     */
    private final String frameworkVersion;

    /**
     * 构造服务。
     *
     * @param marketPluginMapper        市场商品 Mapper
     * @param marketPluginVersionMapper 市场版本 Mapper
     * @param pluginInstallMapper       站点安装态 Mapper
     * @param licenseService            授权服务，可为 null
     * @param catalogCache              目录缓存，可为 null
     * @param frameworkVersion          宿主框架版本
     */
    @Autowired
    public PluginCatalogUpdateService(MarketPluginMapper marketPluginMapper,
                                      MarketPluginVersionMapper marketPluginVersionMapper,
                                      MallPluginInstallMapper pluginInstallMapper,
                                      PluginLicenseService licenseService,
                                      PluginCatalogCache catalogCache,
                                      @Value("${houcloud.update.framework-version:1.0.0}") String frameworkVersion) {
        this.marketPluginMapper = Objects.requireNonNull(marketPluginMapper, "marketPluginMapper 不能为空");
        this.marketPluginVersionMapper = Objects.requireNonNull(marketPluginVersionMapper,
                "marketPluginVersionMapper 不能为空");
        this.pluginInstallMapper = Objects.requireNonNull(pluginInstallMapper, "pluginInstallMapper 不能为空");
        this.licenseService = licenseService;
        this.catalogCache = catalogCache;
        this.frameworkVersion = frameworkVersion == null ? "" : frameworkVersion.trim();
    }

    /**
     * 批量判定「已装插件是否有更新」。
     *
     * @param instanceId 站点实例标识（必填，fail-closed）
     * @param queries    已装清单（{@code code + version}），version 可为空
     * @return 与入参同序的判定结果
     */
    public List<CatalogUpdateItem> catalogUpdates(String instanceId, List<CatalogUpdateQuery> queries) {
        if (!StringUtils.hasText(instanceId)) {
            throw new IllegalArgumentException(
                    "站点实例标识不能为空：目录更新判定必须按站点收敛，无法确认站点身份时拒绝判定");
        }
        List<CatalogUpdateQuery> normalized = normalize(queries);
        List<CatalogUpdateItem> items = new ArrayList<>();
        if (normalized.isEmpty()) {
            return items;
        }
        Map<String, MarketPlugin> pluginMap = pluginMap(normalized);
        Map<String, MallPluginInstall> installMap = installMap(normalized);
        // 依赖态走「延迟批量」：被依赖的插件大多不在本次入参里（用户只上报自己关心的那几个），
        // 逐条查会退化成 N+1——列表页一次几十个插件，每条再查若干依赖，数据库连接先被打满。
        // 延迟是因为判定顺序是「框架 → 授权 → 依赖」：被授权拦下的插件根本不该触发依赖查询。
        DependencyLookup dependencyLookup = new DependencyLookup(pluginMap, installMap);
        for (CatalogUpdateQuery query : normalized) {
            String code = query.getCode();
            MarketPlugin plugin = pluginMap.get(code);
            MallPluginInstall install = installMap.get(code);
            String currentVersion = resolveCurrentVersion(query, install);
            CatalogUpdateItem item = new CatalogUpdateItem(code, currentVersion, null);
            if (plugin == null) {
                items.add(item.blocked(CatalogUpdateBlockReason.NOT_ON_MARKET));
                continue;
            }
            if (!Integer.valueOf(STATUS_PUBLISHED).equals(plugin.getStatus())) {
                items.add(item.blocked(CatalogUpdateBlockReason.PLUGIN_NOT_PUBLISHED));
                continue;
            }
            String latest = latestVersion(code);
            item.setLatestVersion(latest);
            if (latest == null) {
                items.add(item.blocked(CatalogUpdateBlockReason.NO_PUBLISHED_VERSION));
                continue;
            }
            if (currentVersion == null || !PluginVersionComparator.isValid(currentVersion)) {
                CatalogUpdateBlockReason reason = currentVersion == null
                        ? CatalogUpdateBlockReason.CURRENT_VERSION_UNKNOWN
                        : CatalogUpdateBlockReason.CURRENT_VERSION_INVALID;
                items.add(item.blocked(reason));
                continue;
            }
            if (PluginVersionComparator.compare(latest, currentVersion) <= 0) {
                items.add(item.blocked(CatalogUpdateBlockReason.UP_TO_DATE));
                continue;
            }
            CatalogUpdateBlockReason blockReason = blockReason(plugin, instanceId, dependencyLookup);
            items.add(blockReason == null ? item.upgradable() : item.blocked(blockReason));
        }
        writeBackUpgradableVersion(items, installMap);
        return items;
    }

    /**
     * 判定前置条件：框架版本 → 授权 → 依赖。
     *
     * <p><b>为什么授权排在依赖前面</b>：两者是不同层级的前提——
     * <b>授权是「能不能用这个插件」的前提，依赖是「装上之后能不能跑」的前提</b>。
     * 一个未购买（或已过期 / 已吊销）的用户，真正该做的动作是购买或续费；
     * 若因为「依赖恰好也没装」就先弹出「缺少依赖插件」，会把续费这个转化入口
     * 盖成「去装个依赖」，等于把最该给他的 CTA 换成了次级动作。
     * 早期版本为了「省一次授权查询」把依赖放在前面——那是微优化，
     * 收益远小于误导成本，因此顺序定为：框架 → 授权 → 依赖。
     *
     * <p>框架版本仍排第一：它是宿主侧的硬约束，与插件本身无关，
     * 框架都不满足时谈授权和依赖都没有意义。
     *
     * @param plugin               商品行
     * @param instanceId           站点实例标识
     * @param installMap           本次入参命中到的安装态（优先用它判依赖，同一批请求里可能就带着）
     * @param dependencyInstallMap 依赖的批量查询结果（{@code installMap} 里没有时才回退到这里）
     * @return null 表示放行；否则为拦截原因
     */
    private CatalogUpdateBlockReason blockReason(MarketPlugin plugin, String instanceId,
                                                DependencyLookup dependencyLookup) {
        if (!VersionComparison.atLeast(frameworkVersion, plugin.getMinFrameworkVersion())) {
            return CatalogUpdateBlockReason.FRAMEWORK_INCOMPATIBLE;
        }
        if (PluginLicenseService.requiresLicense(plugin.getLicenseType())) {
            if (licenseService == null) {
                return CatalogUpdateBlockReason.LICENSE_UNAVAILABLE;
            }
            if (licenseService.findValid(plugin.getPluginCode(), instanceId) == null) {
                return licenseBlockReason(plugin.getPluginCode(), instanceId);
            }
        }
        if (dependencyMissing(plugin, dependencyLookup)) {
            return CatalogUpdateBlockReason.DEPENDENCY_MISSING;
        }
        return null;
    }

    /**
     * 依赖判定：与升级前 {@code /precheck} 的 3015 完全同口径。
     *
     * <p>三条拦截：没装（{@code installed != 1}）、装了没启用（{@code enabled != 1}）、
     * 声明了 {@code code@minVersion} 但已装版本不满足。
     * <b>未声明或声明非法一律放行</b>（弱校验，见 {@link PluginDependencyParser}）。
     *
     * @param plugin          商品行
     * @param dependencyLookup 依赖安装态查询
     * @return true 表示缺少依赖，应拦截
     */
    private boolean dependencyMissing(MarketPlugin plugin, DependencyLookup dependencyLookup) {
        List<PluginDependencyExpression> dependencies =
                PluginDependencyParser.parse(plugin.getDependenciesJson());
        if (dependencies.isEmpty()) {
            return false;
        }
        for (PluginDependencyExpression dependency : dependencies) {
            MallPluginInstall installed = dependencyLookup.find(dependency.code());
            if (installed == null || !Integer.valueOf(1).equals(installed.getInstalled())) {
                return true;
            }
            if (!Integer.valueOf(1).equals(installed.getEnabled())) {
                return true;
            }
            if (dependency.requiresMin() && !dependency.satisfiedBy(installed.getVersion())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 依赖安装态的<b>延迟批量</b>查询（禁止 N+1，也禁止「还没轮到判依赖就先查」）。
     *
     * <p><b>为什么要延迟</b>：判定顺序是「框架 → 授权 → 依赖」。列表页里被授权拦下的插件占多数时，
     * 提前把依赖查出来等于白跑一次 SQL；而一次性预查又会让「顺序」这件事在代码里失去约束力——
     * 只看调用次数根本看不出依赖到底是不是排在授权之后。
     *
     * <p><b>为什么不直接延迟到每条判定里查</b>：那就是 N+1 了。
     * 这里折中是「汇总所有待查编码，但只在第一次真正需要时发那一次查询」：
     * 全程最多一次 SQL，且没有被用到的场景一次都不发。
     *
     * <p>注意：本对象是<b>方法内局部实例</b>，不做成字段——服务是单例，
     * 把「本次请求的查询结果」挂到字段上会有并发串号风险。
     */
    private final class DependencyLookup {

        private final Map<String, MallPluginInstall> fromRequest;

        private final List<String> codes;

        private Map<String, MallPluginInstall> batch;

        /**
         * 构造查询器（此时只算编码，不查库）。
         *
         * @param pluginMap  商品映射
         * @param installMap 本次入参命中到的安装态
         */
        DependencyLookup(Map<String, MarketPlugin> pluginMap, Map<String, MallPluginInstall> installMap) {
            this.fromRequest = installMap;
            Set<String> pending = new LinkedHashSet<>();
            for (MarketPlugin plugin : pluginMap.values()) {
                if (plugin == null) {
                    continue;
                }
                for (PluginDependencyExpression dependency
                        : PluginDependencyParser.parse(plugin.getDependenciesJson())) {
                    String code = dependency.code();
                    // 已经在本次入参里的编码不必再查——它们本来就是站点上报的安装态
                    if (StringUtils.hasText(code) && !installMap.containsKey(code)) {
                        pending.add(code);
                    }
                }
            }
            this.codes = new ArrayList<>(pending);
        }

        /**
         * 取被依赖插件的安装态：优先用本次入参那一批（同一次请求里可能就带着，最贴近站点现状），
         * 缺失再回落到批量查询结果。
         *
         * @param code 被依赖的插件编码
         * @return 安装态；查不到返回 null
         */
        MallPluginInstall find(String code) {
            MallPluginInstall hit = fromRequest.get(code);
            if (hit != null) {
                return hit;
            }
            if (batch == null) {
                batch = queryBatch();
            }
            return batch.get(code);
        }

        private Map<String, MallPluginInstall> queryBatch() {
            if (codes.isEmpty()) {
                return Map.of();
            }
            List<MallPluginInstall> installs = pluginInstallMapper.selectList(
                    new LambdaQueryWrapper<MallPluginInstall>()
                            .in(MallPluginInstall::getPluginCode, codes));
            return toMap(installs, MallPluginInstall::getPluginCode);
        }
    }

    /**
     * 未持有有效授权时进一步区分「没买 / 过期 / 吊销」，让前端能分流到购买或续费。
     *
     * @param pluginCode 插件编码
     * @param instanceId 站点实例标识
     * @return 拦截原因
     */
    private CatalogUpdateBlockReason licenseBlockReason(String pluginCode, String instanceId) {
        MarketPluginLicense any = licenseService.findAny(pluginCode, instanceId);
        if (any == null) {
            return CatalogUpdateBlockReason.NOT_PURCHASED;
        }
        if (!Integer.valueOf(1).equals(any.getStatus())) {
            return CatalogUpdateBlockReason.LICENSE_REVOKED;
        }
        return CatalogUpdateBlockReason.LICENSE_EXPIRED;
    }

    /**
     * 取市场最新已发布版本：走 {@code hou:idx:latest:{code}} 缓存，未装配缓存则回源。
     *
     * @param pluginCode 插件编码
     * @return 最新版本号；无已发布版本返回 null
     */
    private String latestVersion(String pluginCode) {
        if (catalogCache == null) {
            return queryLatestVersion(pluginCode);
        }
        return catalogCache.latestVersion(pluginCode, () -> queryLatestVersion(pluginCode));
    }

    /**
     * 回源：已发布版本里的 SemVer 最大值（不是「最后发布的那条」）。
     *
     * @param pluginCode 插件编码
     * @return 最新版本号；无已发布版本返回 null
     */
    private String queryLatestVersion(String pluginCode) {
        List<MarketPluginVersion> versions = marketPluginVersionMapper.selectList(
                new LambdaQueryWrapper<MarketPluginVersion>()
                        .eq(MarketPluginVersion::getPluginCode, pluginCode)
                        .eq(MarketPluginVersion::getStatus, VERSION_PUBLISHED));
        if (versions == null || versions.isEmpty()) {
            return null;
        }
        List<String> candidates = new ArrayList<>();
        for (MarketPluginVersion row : versions) {
            // 状态已在 SQL 里过滤，这里再判一次：下架版本若被算进候选，
            // 「有更新」会指向一个装不上的版本（offline 不可下载），比不提示更糟。
            if (row.getVersion() != null
                    && Integer.valueOf(VERSION_PUBLISHED).equals(row.getStatus())) {
                candidates.add(row.getVersion().trim());
            }
        }
        return PluginVersionComparator.maxOf(candidates);
    }

    /**
     * 当前版本：优先信入参（站点自报），缺失时用安装态回退。
     *
     * @param query   入参项
     * @param install 安装态
     * @return 当前版本；都拿不到返回 null
     */
    private String resolveCurrentVersion(CatalogUpdateQuery query, MallPluginInstall install) {
        String reported = query == null ? null : query.getVersion();
        if (StringUtils.hasText(reported)) {
            return reported.trim();
        }
        if (install != null && StringUtils.hasText(install.getVersion())) {
            return install.getVersion().trim();
        }
        return null;
    }

    /**
     * 回写 {@code hou_addon_plugin_install.upgradable_version}（目录页缓存列）。
     *
     * <p><b>幂等</b>：只在值真的变化时才写库——同样的入参重复调用不会产生第二条 UPDATE，
     * 也就不会把 {@code update_time} 无意义地刷新、更不会触发任何乐观锁冲突。
     *
     * @param items      判定结果
     * @param installMap 安装态映射
     */
    private void writeBackUpgradableVersion(List<CatalogUpdateItem> items,
                                            Map<String, MallPluginInstall> installMap) {
        if (installMap.isEmpty()) {
            return;
        }
        for (CatalogUpdateItem item : items) {
            MallPluginInstall install = installMap.get(item.getCode());
            if (install == null) {
                continue;
            }
            String expected = item.isUpgradable() ? item.getLatestVersion() : null;
            if (Objects.equals(expected, install.getUpgradableVersion())) {
                continue;
            }
            install.setUpgradableVersion(expected);
            install.setUpdateTime(java.time.LocalDateTime.now());
            try {
                pluginInstallMapper.updateById(install);
            } catch (RuntimeException e) {
                // 尽力而为：缓存列写失败不能让「有更新」判定失败
                log.warn("回写 upgradable_version 失败，目录页将回源计算: code={} msg={}",
                        item.getCode(), e.getMessage());
            }
        }
    }

    private List<CatalogUpdateQuery> normalize(List<CatalogUpdateQuery> queries) {
        List<CatalogUpdateQuery> result = new ArrayList<>();
        if (queries == null || queries.isEmpty()) {
            return result;
        }
        Set<String> seen = new LinkedHashSet<>();
        for (CatalogUpdateQuery query : queries) {
            if (query == null || !StringUtils.hasText(query.getCode())) {
                continue;
            }
            if (seen.add(query.getCode().trim())) {
                result.add(query);
            }
        }
        return result;
    }

    private Map<String, MarketPlugin> pluginMap(List<CatalogUpdateQuery> queries) {
        List<String> codes = codesOf(queries);
        if (codes.isEmpty()) {
            return Map.of();
        }
        List<MarketPlugin> plugins = marketPluginMapper.selectList(new LambdaQueryWrapper<MarketPlugin>()
                .in(MarketPlugin::getPluginCode, codes));
        return toMap(plugins, MarketPlugin::getPluginCode);
    }

    private Map<String, MallPluginInstall> installMap(List<CatalogUpdateQuery> queries) {
        List<String> codes = codesOf(queries);
        if (codes.isEmpty()) {
            return Map.of();
        }
        List<MallPluginInstall> installs = pluginInstallMapper.selectList(
                new LambdaQueryWrapper<MallPluginInstall>()
                        .in(MallPluginInstall::getPluginCode, codes));
        return toMap(installs, MallPluginInstall::getPluginCode);
    }

    private List<String> codesOf(List<CatalogUpdateQuery> queries) {
        return queries.stream()
                .map(CatalogUpdateQuery::getCode)
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());
    }

    private <T> Map<String, T> toMap(Collection<T> rows, Function<T, String> keyExtractor) {
        if (rows == null || rows.isEmpty()) {
            return Map.of();
        }
        Map<String, T> map = new LinkedHashMap<>();
        for (T row : rows) {
            String key = keyExtractor.apply(row);
            if (StringUtils.hasText(key) && !map.containsKey(key)) {
                map.put(key.trim(), row);
            }
        }
        return map;
    }
}
