package com.houjiang.mall.service.market;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.houjiang.commons.version.VersionComparison;
import com.houjiang.mall.entity.market.MarketPlugin;
import com.houjiang.mall.entity.market.MarketPluginLicense;
import com.houjiang.mall.entity.market.MarketPluginVersion;
import com.houjiang.mall.entity.plugin.MallPluginInstall;
import com.houjiang.mall.mapper.market.MarketAddonCateMapper;
import com.houjiang.mall.mapper.market.MarketPluginMapper;
import com.houjiang.mall.mapper.market.MarketPluginVersionMapper;
import com.houjiang.mall.mapper.plugin.MallPluginInstallMapper;
import com.houjiang.mall.model.market.CatalogBaseline;
import com.houjiang.mall.model.market.MarketPluginItem;
import com.houjiang.mall.service.market.cache.PluginCatalogCache;
import com.houjiang.mall.service.plugin.AddonPluginSyncService;
import com.houjiang.mall.service.plugin.StandalonePluginCodes;
import com.houjiang.plugin.engine.artifact.PluginArtifactStore;
import com.houjiang.plugin.engine.artifact.PluginChecksumVerifier;
import com.houjiang.plugin.engine.core.PluginLifecycleManager;
import com.houjiang.plugin.sdk.model.PluginManifest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.Locale;

import org.yaml.snakeyaml.Yaml;

/**
 * 插件市场服务（形态 A：并入 mall）。
 *
 * <p>两刀能力：
 * <ol>
 *   <li><b>第一刀（浏览层）</b>：市场浏览 + 定价 + 版本。按上架状态（status=2）列出市场插件，
 *       合并本站点安装态；FREE / PAID / TRIAL 展示为 {@code priceText}。</li>
 *   <li><b>第二刀（安装层）</b>：在线安装链路 —— 下载 → MD5 校验 → 落盘 → 登记安装态。
 *       落盘使用 plugin-engine 的 {@link PluginArtifactStore}，与引擎装载共用同一目录契约。</li>
 * </ol>
 *
 * <p><b>边界（诚实说明）</b>：
 * <ul>
 *   <li>付费授权校验已接入：PAID / TRIAL 插件必须持有本站点有效 License
 *       （{@link PluginLicenseService}）才允许安装；签发 / 吊销属平台侧动作，不在本类职责内；</li>
 *   <li>订单与在线支付（{@code hou_market_plugin_order}）仍未接入——当前收费链路为
 *       「线下收款 → 平台签发授权码 → 租户激活」，系统内不落订单；</li>
 *   <li>引擎真实装载（PF4J load/start）由宿主导入 {@code PluginEngineAutoConfiguration} 后生效；
 *       Mall 宿主在落盘后调用 PF4J 生命周期管理器完成装载、启动和路由注册；测试或只读构造器未提供引擎时仍可仅验证落盘链路。</li>
 * </ul>
 *
 * @author Amos
 */
@Slf4j
@Service
public class PluginMarketService {

    /** 已上架 */
    private static final int STATUS_PUBLISHED = 2;

    /** 版本审核通过 */
    private static final int VERSION_APPROVED = 1;

    /**
     * 跨宿主安装被拒（与 plugin-update 的 3020 HOST_SERVICE_MISMATCH 同码）。
     *
     * <p>本类不能直接引 {@code PluginDistErrorCode.HOST_SERVICE_MISMATCH}：该常量在
     * commons 里是后加的，老基线的 commons 制品没有它，引了会让热修包在老镜像上跑不起来。
     */
    private static final int ERR_HOST_SERVICE_MISMATCH = 3020;

    /** base 宿主标识：本进程只装载声明为 base 的嵌入插件。 */
    private static final String HOST_SERVICE_BASE = "base";

    /** 「未分类」桶的分类 ID：与前端 menuDisplay.UNCATEGORIZED_CATE_ID 保持一致。 */
    private static final int UNCATEGORIZED_CATE_ID = 0;

    /** 「未分类」桶的展示名：与插件中心、顶栏「插件应用」面板三处文案保持一致。 */
    private static final String UNCATEGORIZED_LABEL = "未分类";

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final MarketPluginMapper marketPluginMapper;
    private final MarketPluginVersionMapper marketPluginVersionMapper;
    private final MallPluginInstallMapper pluginInstallMapper;
    private final PluginArtifactDownloader artifactDownloader;
    private final PluginArtifactStore artifactStore;
    private final PluginChecksumVerifier checksumVerifier;
    private final PluginLicenseService licenseService;
    private final PluginLifecycleManager lifecycleManager;
    private final AddonPluginSyncService addonPluginSyncService;

    /**
     * 商业侧清算（安装前核对订单 / 卸载后回收授权与凭证）。
     *
     * <p>用可选字段注入而不是构造器参数：本类已有多个构造器重载（只读 / 安装 / 完整），
     * 新增必填参数会牵动所有既有调用点与单测；且缺失时只是「不做购买订单核对」，
     * 不改变安装主流程——单测直接 new 出来的场景就是这个状态。
     */
    @Autowired(required = false)
    private PluginLifecycleSettlementService lifecycleSettlementService;

    /**
     * 安装包下载地址换票器（可选）。
     *
     * <p><b>为什么必须有它</b>：{@code hou_market_plugin_version.jar_url} 存的是
     * {@code /base/plugin-center/artifact/raw?aid=xxx} —— 那是<b>规范指针</b>，不是可直接下载的
     * 链接；{@code raw} 端点只认 {@code token} 参数，把指针直接交给下载器只会拿到 401。
     * 必须先按当前登录身份换取「10 分钟 + 限次 + 绑定本站点」的令牌地址
     * （2026-10-28 修复：此前在线安装 100% 失败在「不支持的安装包地址协议」，就是漏了这一步）。
     *
     * <p><b>为什么是可选字段注入</b>：接口定义在本模块，实现 {@code ArtifactUrlResolverImpl}
     * 由 plugin-artifact 提供（artifact 依赖 market，不能反向依赖），故只能按类型取 Bean；
     * 本类已有 5 个构造器重载，再加必填参数会再造重载并牵动所有调用点与单测。
     * 缺失时回退「直接把 jar_url 交给下载器」，仅对 http/file 形态的地址有效。
     */
    @Autowired(required = false)
    private ArtifactUrlResolver artifactUrlResolver;

    /**
     * 跨宿主安装派发器（可选）。
     *
     * <p><b>为什么必须有它</b>：嵌入式插件由声明了 {@code hostService} 的宿主进程内的 PF4J 引擎装载
     * （OCR 声明 tool，就只能在 tool 进程里装）。base 自己的引擎只认识 hostService=base 的插件，
     * 若把 OCR 的制品落盘到 base 的 plugins 目录，tool 永远扫不到，base 也只能炸在
     * 「Unknown hostService」（2026-10-28 实测）。所以跨宿主时 base 只做编排：下载 → 校验 →
     * 派发给宿主 → 登记安装态。
     *
     * <p>与 {@link #artifactUrlResolver} 同样的取舍：可选字段注入，避免再增构造器重载。
     */
    @Autowired(required = false)
    private PluginHostInstallDispatcher hostInstallDispatcher;

    /**
     * 下载次数明细统计（可选）：{@code hou_market_plugin_download} 是下载次数的权威来源。
     *
     * <p>与其它可选依赖同样的取舍（本类已有 5 个构造器重载）：缺失时退化为旧的
     * {@code download_count} 自增，安装主流程不受影响。</p>
     */
    @Autowired(required = false)
    private PluginDownloadStatsService downloadStatsService;

    /**
     * 站点 ↔ 平台下载计数对齐（可选）：装完上报平台、列表读取平台真实次数。
     *
     * <p>未配置 {@code houcloud.platform.url} 时它自身静默跳过，平台站点（unimall.houjit.com）
     * 因此天然以本地明细为权威。</p>
     */
    @Autowired(required = false)
    private PlatformDownloadReporter platformDownloadReporter;

    /**
     * 平台市场目录客户端（可选）：站点打开插件中心时，插件商品实时从平台读。
     *
     * <p>2026-10-29 负责人确认的架构口径：<b>插件商品的唯一权威源是平台</b>，
     * 开源包本地只有已安装插件的表、不带插件商品快照；未配置 {@code houcloud.platform.url}
     * 或平台不可达时自动回落本地表（平台自身正是这种形态）。</p>
     */
    @Autowired(required = false)
    private PlatformMarketClient platformMarketClient;

    /**
     * 价格套餐路由（可选）：平台目录端点要把每个插件商品的套餐一起带出，
     * 否则站点端「购买」没有可选项。与 {@code PluginMarketController} 注入的是同一 Bean。
     */
    @Autowired(required = false)
    private com.houjiang.mall.pay.plugin.PluginPayChannelRouter channelRouter;

    /**
     * 环境配置（可选）：宿主可用性预检要用它读 {@code houcloud.plugin.host-endpoints.<host>}。
     *
     * <p>2026-10-30：开源站点只有部分宿主服务（评估形态 11 服务），市场目录里却能看到
     * 平台全部在架插件。此前用户点安装要等下载完成、派发环节才报
     * 「注册中心未找到 cms 服务的可用实例」——既浪费一次下载，报错文案还是运维向的。
     * 预检把「这个插件本站装不装得了」提前到列表展示与安装入口。</p>
     *
     * <p>与其它可选依赖同样的取舍（本类已有 5 个构造器重载）：缺失时预检退化为
     * 「不拦截」，与旧行为一致，不影响主流程。</p>
     */
    @Autowired(required = false)
    private Environment environment;

    /**
     * 服务发现（可选）：宿主可用性预检的第二判定源。
     *
     * <p>与 {@link PluginHostInstallDispatcher#install} 的地址解析同口径：
     * host-endpoints 显式配置优先（非注册中心部署的运维兜底），注册中心实例其次。
     * 预检结论与派发解析共享同一优先级，才能保证「列表说能装 → 安装就真的能派发」。</p>
     */
    @Autowired(required = false)
    private DiscoveryClient discoveryClient;

    /**
     * 宿主框架版本：用于在详情的版本列表上逐版本标出 {@code compatible}。
     *
     * <p>与 {@code PluginMarketController} 的 {@code frameworkVersion} 同源同口径
     * （{@code houcloud.update.framework-version}）；市场模块不依赖 plugin-update，
     * 这里直接用配置读取，避免为了一个字符串引入模块耦合。
     *
     * <p>用字段注入而不是再加一个构造器参数：本类已有 5 个构造器重载，再添必填参数会
     * 再造出一批重载并牵动所有调用点；且它缺失时只是 {@code compatible} 不做判定
     * （返回 null），不影响主流程——测试里直接 new 出来就是这个情况。
     */
    @Value("${houcloud.update.framework-version:1.0.0}")
    private String frameworkVersion;

    /**
     * 目录索引缓存（M5）：可选能力，为 null 时列表全部回源。
     *
     * <p><b>为什么用 setter 注入而不是构造器</b>：本类为了兼容「只读 / 安装 / 完整」三种装配
     * 已经有 5 个构造器重载，再加一个必填参数会再造出两个重载、并且要改动所有既有调用点。
     * 缓存是纯加速手段、缺失时行为完全正确，因此按可选依赖装配更合适。
     */
    private PluginCatalogCache catalogCache;

    /**
     * 可选装配目录索引缓存。
     *
     * @param catalogCache 缓存组件，可为 null
     */
    @Autowired(required = false)
    public void setCatalogCache(PluginCatalogCache catalogCache) {
        this.catalogCache = catalogCache;
    }

    /**
     * 可选装配插件分类表读取器（hou_addon_cate）。
     *
     * <p>与 {@link #catalogCache} 同样的取舍：本类构造器重载已经够多，
     * 分类表是「有则更准、没有也能用」的可选依赖，故用 setter 注入。
     */
    @Autowired(required = false)
    public void setAddonCateMapper(MarketAddonCateMapper addonCateMapper) {
        this.addonCateMapper = addonCateMapper;
    }

    private MarketAddonCateMapper addonCateMapper;

    /**
     * 市场安装后把 plugin.yaml 的 menus 同步进权限表（右上角「插件应用」依赖它）。
     * 跨宿主安装时 jar 不在 base 本机，必须用换票带回的 yaml 文本落临时文件再 sync。
     */
    @Autowired(required = false)
    private com.houjiang.plugin.engine.spi.PluginMenuPort pluginMenuPort;

    /**
     * 仅浏览能力（保留：单测与只读场景）。
     */
    public PluginMarketService(MarketPluginMapper marketPluginMapper,
                               MarketPluginVersionMapper marketPluginVersionMapper,
                               MallPluginInstallMapper pluginInstallMapper) {
        // 委派 9 参主构造器（安装能力全 null）：避免与两个 8 参构造器（@Autowired 的 String pluginsDir 版、
        // 包内 PluginArtifactStore 版）产生「引用不明确」编译歧义；只读场景本就不装配任何安装能力。
        this(marketPluginMapper, marketPluginVersionMapper, pluginInstallMapper, null, null, null, null, null, null);
    }

    /**
     * Spring 装配：安装产物仓库与插件引擎使用同一 pluginsRoot（目录契约）。
     */
    @Autowired
    public PluginMarketService(MarketPluginMapper marketPluginMapper,
                               MarketPluginVersionMapper marketPluginVersionMapper,
                               MallPluginInstallMapper pluginInstallMapper,
                               PluginArtifactDownloader artifactDownloader,
                               @Value("${houcloud.plugin.plugins-dir:plugins}") String pluginsDir,
                               PluginLicenseService licenseService,
                               PluginLifecycleManager lifecycleManager,
                               AddonPluginSyncService addonPluginSyncService) {
        this(marketPluginMapper, marketPluginVersionMapper, pluginInstallMapper, artifactDownloader,
                new PluginArtifactStore(Path.of(pluginsDir)), new PluginChecksumVerifier(), licenseService,
                lifecycleManager, addonPluginSyncService);
    }

    PluginMarketService(MarketPluginMapper marketPluginMapper,
                        MarketPluginVersionMapper marketPluginVersionMapper,
                        MallPluginInstallMapper pluginInstallMapper,
                        PluginArtifactDownloader artifactDownloader,
                         PluginArtifactStore artifactStore,
                         PluginChecksumVerifier checksumVerifier,
                         PluginLicenseService licenseService) {
        this(marketPluginMapper, marketPluginVersionMapper, pluginInstallMapper, artifactDownloader,
                artifactStore, checksumVerifier, licenseService, null);
    }

    PluginMarketService(MarketPluginMapper marketPluginMapper,
                        MarketPluginVersionMapper marketPluginVersionMapper,
                        MallPluginInstallMapper pluginInstallMapper,
                        PluginArtifactDownloader artifactDownloader,
                        PluginArtifactStore artifactStore,
                        PluginChecksumVerifier checksumVerifier,
                        PluginLicenseService licenseService,
                        PluginLifecycleManager lifecycleManager) {
        this(marketPluginMapper, marketPluginVersionMapper, pluginInstallMapper, artifactDownloader,
                artifactStore, checksumVerifier, licenseService, lifecycleManager, null);
    }

    PluginMarketService(MarketPluginMapper marketPluginMapper,
                        MarketPluginVersionMapper marketPluginVersionMapper,
                        MallPluginInstallMapper pluginInstallMapper,
                        PluginArtifactDownloader artifactDownloader,
                        PluginArtifactStore artifactStore,
                        PluginChecksumVerifier checksumVerifier,
                        PluginLicenseService licenseService,
                        PluginLifecycleManager lifecycleManager,
                        AddonPluginSyncService addonPluginSyncService) {
        this.marketPluginMapper = marketPluginMapper;
        this.marketPluginVersionMapper = marketPluginVersionMapper;
        this.pluginInstallMapper = pluginInstallMapper;
        this.artifactDownloader = artifactDownloader;
        this.artifactStore = artifactStore;
        this.checksumVerifier = checksumVerifier;
        this.licenseService = licenseService;
        this.lifecycleManager = lifecycleManager;
        this.addonPluginSyncService = addonPluginSyncService;
    }

    /**
     * 市场列表：分页前的全量（数据量小）+ 可选分类 + 过滤。
     */
    public Map<String, Object> listPlugins(Map<String, Object> filters) {
        Map<String, Object> filtersSafe = filters == null ? Map.of() : filters;
        // 2026-10-29 架构口径：插件商品的唯一权威源是平台。配了平台地址的站点，
        // 市场页的基线（有什么插件/版本/价格/真实下载次数）实时从平台目录读，
        // 本地只叠加自己的安装态与授权。拉不到（未对接/平台不可达）回落本地表。
        PlatformMarketClient.RemoteCatalog remote = remoteCatalogOrNull();
        if (remote != null) {
            return listFromRemoteCatalog(remote, filtersSafe);
        }
        // 分类字典（id → 名称）一次取齐：既用于筛选「未分类」的判定，也用于列表项展示名归并。
        // 放进循环里取会退化成「每条目一次查询」。
        Map<Integer, String> cateNames = cateNameById();
        LambdaQueryWrapper<MarketPlugin> wrapper = new LambdaQueryWrapper<MarketPlugin>()
                .eq(MarketPlugin::getStatus, STATUS_PUBLISHED)
                // 分类维度排序：按分类外键（= 字典 sort 顺序）再按 id，保证同分类聚在一起
                .orderByAsc(MarketPlugin::getCateId)
                .orderByAsc(MarketPlugin::getId);
        String keyword = stringValue(filtersSafe.get("keyword"));
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(MarketPlugin::getName, keyword)
                    .or().like(MarketPlugin::getPluginCode, keyword)
                    .or().like(MarketPlugin::getDescription, keyword));
        }
        // 分类筛选：优先按 cateId（权威外键）；category 文本仍支持，供旧调用方兼容。
        // 两个都不传 = 不过滤。
        Integer cateId = intValue(filtersSafe.get("cateId"));
        if (cateId != null && cateId >= 0) {
            applyCateIdFilter(wrapper, cateId, cateNames.keySet());
        }
        String category = stringValue(filtersSafe.get("category"));
        if (StringUtils.hasText(category)) {
            wrapper.eq(MarketPlugin::getCategory, category);
        }
        String licenseType = stringValue(filtersSafe.get("licenseType"));
        if (StringUtils.hasText(licenseType)) {
            wrapper.eq(MarketPlugin::getLicenseType, licenseType.toUpperCase(Locale.ROOT));
        }
        String installStatus = stringValue(filtersSafe.get("status"));
        if ("installed".equalsIgnoreCase(installStatus)) {
            wrapper.exists("SELECT 1 FROM hou_addon_plugin_install i WHERE i.plugin_code = hou_market_plugin.plugin_code AND i.installed = 1 AND i.deleted = 0");
        } else if ("available".equalsIgnoreCase(installStatus)) {
            wrapper.notExists("SELECT 1 FROM hou_addon_plugin_install i WHERE i.plugin_code = hou_market_plugin.plugin_code AND i.installed = 1 AND i.deleted = 0");
        }
        int pageNo = positiveInt(filtersSafe.get("page"), positiveInt(filtersSafe.get("current"), 1));
        int pageSize = Math.min(100, positiveInt(filtersSafe.get("pageSize"), positiveInt(filtersSafe.get("size"), 20)));
        // 缓存只装「与租户无关」的公开基线；安装态与授权每请求现算，不进缓存
        // （否则缓存规模 = 站点数 × 分页 × 关键字，且任何一次安装都要失效全站）。
        CatalogBaseline baseline = loadBaseline(wrapper, pageNo, pageSize,
                baselineCacheKey(keyword, category, cateId, licenseType, pageNo, pageSize,
                        StringUtils.hasText(installStatus)));
        // 租户维度一次查齐再合并：放进 stream 里会退化成「每条目一次查询」

        Map<String, MallPluginInstall> installMap = installMapOf(codesOf(baseline));
        Map<String, MarketPluginLicense> licenseMap = licenseMap(stringValue(filtersSafe.get("instanceId")));
        List<MarketPluginItem> items = baseline.getItems().stream()
                .map(item -> decorate(item, installMap.get(item.getCode()), licenseMap.get(item.getCode()), cateNames))
                .filter(item -> matchFilters(item, filtersSafe))
                .collect(Collectors.toList());
        // 下载次数以平台（unimall.houjit.com）为准：本站点只是全网数据的一个消费者。
        // 未对接平台 / 平台不可达时保持本地值，不把页面打成 0。
        applyRemoteCounts(items);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", items);
        result.put("categories", categoryOptions());
        result.put("page", pageNo);
        result.put("pageSize", pageSize);
        result.put("total", baseline.getTotal());
        result.put("pages", baseline.getPages());
        return result;
    }

    /**
     * 取平台市场目录；未对接 / 平台不可达时返回 null（调用方回落本地表）。
     *
     * <p>独立成方法而不是直接调 client：① {@link #listPlugins} 与 {@link #categoryOptions()}
     * 与 {@link #detail(String, String)} 三处共用；② 这里是「平台目录优先、本地表兜底」
     * 的唯一口径点，避免三处各写一套降级判断漂移。</p>
     */
    private PlatformMarketClient.RemoteCatalog remoteCatalogOrNull() {
        return platformMarketClient == null ? null : platformMarketClient.catalog();
    }

    /**
     * 站点视角的市场列表：平台目录为基线 + 本站安装态/授权合并。
     *
     * <p>与本地版 {@link #listPlugins} 的关键差异：</p>
     * <ul>
     *   <li>基线不查本地 {@code hou_market_plugin}——本地没有插件商品快照（2026-10-29 口径）；
     *       平台目录里的 downloadCount 已是全网真实值，无需再叠加 {@link #applyRemoteCounts}；</li>
     *   <li>分类字典用平台目录里带回的（站点本地 {@code hou_addon_cate} 的 id 语义
     *       与平台不对齐，混用会「点 Tab 筛不出东西」）；</li>
     *   <li>cateId / installed / available 过滤与分页在内存做——平台目录是全量小数据
     *       （当前 33 个插件商品），没有为它做服务端分页的必要。</li>
     * </ul>
     *
     * @param catalog 平台目录
     * @param filters 站点请求的筛选与分页参数
     * @return 与本地版结构一致的结果（list/categories/page/pageSize/total/pages）
     */
    private Map<String, Object> listFromRemoteCatalog(PlatformMarketClient.RemoteCatalog catalog,
                                                       Map<String, Object> filters) {
        Map<Integer, String> cateNames = remoteCateNames(catalog);
        List<String> codes = catalog.getPlugins().stream()
                .map(MarketPluginItem::getCode)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());
        Map<String, MallPluginInstall> installMap = installMapOf(codes);
        Map<String, MarketPluginLicense> licenseMap = licenseMap(stringValue(filters.get("instanceId")));
        List<MarketPluginItem> items = catalog.getPlugins().stream()
                .map(item -> decorate(item, installMap.get(item.getCode()), licenseMap.get(item.getCode()), cateNames))
                // 站点视角宿主可用性（2026-10-30）：开源/精简部署只带部分宿主服务，
                // 市场页要在列表层就把「装不了的原因」讲清楚，而不是让用户点完安装
                // 才在派发环节报「注册中心未找到 xx 服务的可用实例」。公开目录只带
                // hostService 声明，这里的 true/false 是本站自己的结论。
                .peek(this::applyHostAvailability)
                .filter(item -> matchFilters(item, filters))
                .filter(item -> matchRemoteCateId(item, filters, catalog))
                .filter(item -> matchRemoteInstallStatus(item, filters))
                .collect(Collectors.toList());
        int pageNo = positiveInt(filters.get("page"), positiveInt(filters.get("current"), 1));
        int pageSize = Math.min(100, positiveInt(filters.get("pageSize"), positiveInt(filters.get("size"), 20)));
        int total = items.size();
        int pages = pageSize <= 0 ? 0 : (total + pageSize - 1) / pageSize;
        int from = Math.min((pageNo - 1) * pageSize, total);
        int to = Math.min(from + pageSize, total);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", items.subList(from, to));
        result.put("categories", catalog.getCategories());
        result.put("page", pageNo);
        result.put("pageSize", pageSize);
        result.put("total", total);
        result.put("pages", pages);
        return result;
    }

    /** 远端目录的 cateId 过滤（内存版，语义与本地 applyCateIdFilter 对齐）。 */
    private boolean matchRemoteCateId(MarketPluginItem item, Map<String, Object> filters,
                                      PlatformMarketClient.RemoteCatalog catalog) {
        Integer cateId = intValue(filters.get("cateId"));
        if (cateId == null || cateId < 0) {
            return true;
        }
        boolean known = remoteCateNames(catalog).containsKey(cateId);
        int itemCate = item.getCateId() == null ? 0 : item.getCateId();
        if (cateId == 0) {
            // 「未分类」桶：平台目录里的分类字典查不到该 id 视为未分类
            return !known || itemCate == 0;
        }
        return known && itemCate == cateId;
    }

    /** 远端目录的 installed / available 过滤（本站安装态，内存版）。 */
    private boolean matchRemoteInstallStatus(MarketPluginItem item, Map<String, Object> filters) {
        String installStatus = stringValue(filters.get("status"));
        if ("installed".equalsIgnoreCase(installStatus)) {
            return Boolean.TRUE.equals(item.getInstalled());
        }
        if ("available".equalsIgnoreCase(installStatus)) {
            return !Boolean.TRUE.equals(item.getInstalled());
        }
        return true;
    }

    /**
     * 平台目录的分类字典 → id→名称映射。
     *
     * @param catalog 平台目录
     * @return id → 名称；目录没带分类时返回空 Map（展示名回退 category 文本）
     */
    private Map<Integer, String> remoteCateNames(PlatformMarketClient.RemoteCatalog catalog) {
        Map<Integer, String> result = new LinkedHashMap<>();
        if (catalog.getCategories() == null) {
            return result;
        }
        for (Map<String, Object> row : catalog.getCategories()) {
            Object id = row == null ? null : row.get("id");
            Object name = row == null ? null : row.get("name");
            if (id instanceof Number number && name != null) {
                result.put(number.intValue(), String.valueOf(name));
            }
        }
        return result;
    }

    /**
     * 分页缓存键：关键字 / 分类 / 授权类型 / 分页共同参与。
     *
     * <p>取 MD5 摘要而不直接拼原始串，是因为关键字可能很长且含中文与特殊字符，
     * 直接拼会让 key 长度不可控、也不便于在 Redis 里肉眼比对。
     *
     * @param category       分类名（旧口径，可为 null）
     * @param cateId         分类 ID（新口径，null 表示未传）
     * @param tenantFiltered true 表示本次带租户维度过滤（已装/未装），不可缓存
     * @return 缓存键；不可缓存时返回 null
     */
    private String baselineCacheKey(String keyword, String category, Integer cateId, String licenseType,
                                    int pageNo, int pageSize, boolean tenantFiltered) {
        if (tenantFiltered || catalogCache == null || !catalogCache.isEnabled()) {
            return null;
        }
        String raw = keyword + '|' + category + '|' + (cateId == null ? "" : cateId) + '|'
                + licenseType + '|' + pageNo + '|' + pageSize;
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            StringBuilder builder = new StringBuilder(32);
            for (byte item : digest.digest(raw.getBytes(java.nio.charset.StandardCharsets.UTF_8))) {
                builder.append(Character.forDigit((item >> 4) & 0xF, 16))
                        .append(Character.forDigit(item & 0xF, 16));
            }
            return builder.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            // MD5 是 JDK 强制实现的算法，走到这里说明运行环境异常；退化为哈希码，
            // 最坏情况是命中率下降，不能让列表接口失败。
            return Integer.toHexString(raw.hashCode());
        }
    }

    /**
     * 加载公开基线：命中缓存直接返回，否则查库并回填。
     *
     * @param wrapper   查询条件
     * @param pageNo    页码
     * @param pageSize  每页条数
     * @param cacheKey  缓存键；null 表示本次不缓存
     * @return 公开基线
     */
    private CatalogBaseline loadBaseline(LambdaQueryWrapper<MarketPlugin> wrapper, int pageNo, int pageSize,
                                         String cacheKey) {
        if (cacheKey == null) {
            return queryBaseline(wrapper, pageNo, pageSize);
        }
        CatalogBaseline cached = catalogCache.readThrough(
                PluginCatalogCache.NAMESPACE_CATALOG,
                cacheKey, CatalogBaseline.class, () -> queryBaseline(wrapper, pageNo, pageSize));
        return cached == null ? queryBaseline(wrapper, pageNo, pageSize) : cached;
    }

    /**
     * 回源查询公开基线（只含市场元数据，不含安装态 / 授权）。
     *
     * @param wrapper  查询条件
     * @param pageNo   页码
     * @param pageSize 每页条数
     * @return 公开基线
     */
    private CatalogBaseline queryBaseline(LambdaQueryWrapper<MarketPlugin> wrapper, int pageNo, int pageSize) {
        IPage<MarketPlugin> page = marketPluginMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
        List<MarketPlugin> plugins = page == null ? marketPluginMapper.selectList(wrapper) : page.getRecords();
        List<MarketPluginItem> items = plugins == null
                ? List.of()
                : plugins.stream().map(this::baselineItem).collect(Collectors.toList());
        CatalogBaseline baseline = new CatalogBaseline();
        baseline.setItems(items);
        baseline.setTotal(page == null ? items.size() : page.getTotal());
        baseline.setPages(page == null ? (items.isEmpty() ? 0 : 1) : page.getPages());
        return baseline;
    }

    /**
     * 取出基线里的插件编码（用于合并安装态与授权）。
     *
     * @param baseline 公开基线
     * @return 插件编码列表
     */
    private List<String> codesOf(CatalogBaseline baseline) {
        if (baseline == null || baseline.getItems() == null) {
            return List.of();
        }
        return baseline.getItems().stream()
                .map(MarketPluginItem::getCode)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());
    }

    /**
     * 市场分类名（旧口径，字符串数组）。
     *
     * <p>2026-09-19 起与 {@link #categoryOptions()} 同源同序，只是剥掉 id / count，
     * 供只需要展示名的调用方使用。行为变化：<b>不再</b>把已上架插件的自由文本 category
     * 追加进来——那正是分类漂移的来源（市场侧「营销工具」与分类表「营销」各说各话）。
     * 未归类插件统一落到「未分类」桶。
     */
    public List<String> categories() {
        return categoryOptions().stream()
                .map(row -> String.valueOf(row.get("name")))
                .collect(Collectors.toList());
    }

    /**
     * 分类选项：<b>唯一数据源是 hou_addon_cate</b>，带 id、名称与已上架插件数量。
     *
     * <p>为什么要有这个方法：市场页此前按「分类名」字符串筛选与分组，而分类名在
     * 2026-09-19 之前与市场数据里的自由文本<b>零重叠</b>（营销工具 / 内容营销 / … vs
     * 营销 / 工具 / …），于是 Tab 上出现的分类和列表里的分类对不上。改为按 id 之后：
     * 重命名分类不必同步改插件数据，筛选、分组、计数三者天然一致。
     *
     * <p>规则：
     * <ol>
     *   <li>分类表全量输出（sort 升序、同 sort 按 id），<b>空分类也保留</b>，
     *       保证「分类下拉」与顶栏「插件应用」面板、插件中心三处列表完全一致；</li>
     *   <li>count = 该分类下<b>已上架</b>插件数；</li>
     *   <li>存在未归类插件（cate_id 为 0 / NULL / 指向已删除分类）时，
     *       末尾补一个 id=0 的「未分类」桶；</li>
     *   <li>分类表不可用（表缺失 / 查询异常）时不抛错，全部插件归入「未分类」，
     *       只记 warn —— 分类问题不能连带打挂市场列表。</li>
     * </ol>
     *
     * @return {@code [{id, name, count}]}，可直接供前端渲染分类 Tab
     */
    public List<Map<String, Object>> categoryOptions() {
        // 2026-10-29：对接平台的站点，分类字典与插件商品一起从平台读——
        // 站点本地 hou_addon_cate 的 id 语义与平台不对齐，本地查只会「Tab 与列表对不上」。
        PlatformMarketClient.RemoteCatalog remote = remoteCatalogOrNull();
        if (remote != null && remote.getCategories() != null && !remote.getCategories().isEmpty()) {
            return remote.getCategories();
        }
        List<CateRef> cates = readCates();
        Map<Integer, Long> counts = countPublishedByCate(cates.stream().map(CateRef::id)
                .collect(Collectors.toSet()));
        List<Map<String, Object>> result = new ArrayList<>();
        for (CateRef cate : cates) {
            result.add(cateOption(cate.id(), cate.name(), counts.getOrDefault(cate.id(), 0L)));
        }
        long uncategorized = counts.getOrDefault(UNCATEGORIZED_CATE_ID, 0L);
        if (uncategorized > 0) {
            result.add(cateOption(UNCATEGORIZED_CATE_ID, UNCATEGORIZED_LABEL, uncategorized));
        }
        return result;
    }

    private static Map<String, Object> cateOption(int id, String name, long count) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", id);
        row.put("name", name);
        row.put("count", count);
        return row;
    }

    /** 读取分类表（sort 升序）；任何异常都当作「无分类表」，不向上抛。 */
    private List<CateRef> readCates() {
        if (addonCateMapper == null) {
            return List.of();
        }
        try {
            List<Map<String, Object>> rows = addonCateMapper.selectAll();
            if (rows == null) {
                return List.of();
            }
            List<CateRef> result = new ArrayList<>();
            for (Map<String, Object> row : rows) {
                if (row == null) {
                    continue;
                }
                int id = intOf(row.get("id"), -1);
                String name = row.get("name") == null ? null : String.valueOf(row.get("name")).trim();
                if (id <= 0 || !StringUtils.hasText(name)) {
                    continue;
                }
                result.add(new CateRef(id, name, intOf(row.get("sort"), 0)));
            }
            result.sort((a, b) -> Integer.compare(a.sort(), b.sort()) == 0
                    ? Integer.compare(a.id(), b.id())
                    : Integer.compare(a.sort(), b.sort()));
            return result;
        } catch (Exception e) {
            log.warn("[PluginMarket] 读取 hou_addon_cate 失败，市场分类退化为未分类: {}", e.getMessage());
            return List.of();
        }
    }

    /** 分类 id → 规范名；用于把列表项的展示名统一到字典口径。 */
    private Map<Integer, String> cateNameById() {
        Map<Integer, String> map = new LinkedHashMap<>();
        for (CateRef cate : readCates()) {
            map.put(cate.id(), cate.name());
        }
        return map;
    }

    /**
     * 按分类统计已上架插件数。
     *
     * <p>不属于任何已知分类的（0 / NULL / 指向已删除分类）统一计入 {@link #UNCATEGORIZED_CATE_ID}，
     * 与前端的「未分类」桶口径一致 —— 否则会出现「列表里显示未分类，Tab 上却没有这一项」。
     */
    private Map<Integer, Long> countPublishedByCate(Set<Integer> knownIds) {
        Map<Integer, Long> counts = new LinkedHashMap<>();
        List<MarketPlugin> published = marketPluginMapper.selectList(
                new LambdaQueryWrapper<MarketPlugin>().eq(MarketPlugin::getStatus, STATUS_PUBLISHED));
        for (MarketPlugin plugin : published == null ? List.<MarketPlugin>of() : published) {
            int id = normalizeCateId(plugin.getCateId(), knownIds);
            counts.merge(id, 1L, Long::sum);
        }
        return counts;
    }

    /** 归一分类 id：未知 / 空 / 非正 → 未分类（0）。 */
    private static int normalizeCateId(Integer raw, Set<Integer> knownIds) {
        if (raw == null || raw <= 0) {
            return UNCATEGORIZED_CATE_ID;
        }
        return knownIds != null && !knownIds.isEmpty() && !knownIds.contains(raw)
                ? UNCATEGORIZED_CATE_ID
                : raw;
    }

    /** 分类筛选：id&gt;0 精确匹配；id=0 命中「未分类」（含 NULL 与指向已删除分类的行）。 */
    private static void applyCateIdFilter(LambdaQueryWrapper<MarketPlugin> wrapper, int cateId,
                                          Set<Integer> knownIds) {
        if (cateId > 0) {
            wrapper.eq(MarketPlugin::getCateId, cateId);
            return;
        }
        if (knownIds == null || knownIds.isEmpty()) {
            // 分类字典不可用：所有插件在展示上都算未分类，此时不加条件等价于"全选"
            wrapper.and(w -> w.isNull(MarketPlugin::getCateId).or().eq(MarketPlugin::getCateId, 0));
            return;
        }
        wrapper.and(w -> w.isNull(MarketPlugin::getCateId)
                .or().eq(MarketPlugin::getCateId, 0)
                .or().notIn(MarketPlugin::getCateId, knownIds));
    }

    /** Map 里的数字取值（MyBatis 可能返回 Integer / Long / BigDecimal）。 */
    private static int intOf(Object value, int fallback) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value != null && StringUtils.hasText(String.valueOf(value))) {
            try {
                return Integer.parseInt(String.valueOf(value).trim());
            } catch (NumberFormatException ignored) {
                return fallback;
            }
        }
        return fallback;
    }

    /** 筛选参数里的整数取值；null / 非数字一律视为「未传」。 */
    private static Integer intValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        String text = String.valueOf(value).trim();
        if (!StringUtils.hasText(text)) {
            return null;
        }
        try {
            return Integer.valueOf(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** 分类字典的一行（内部用）。 */
    private record CateRef(int id, String name, int sort) {}

    /**
     * 插件详情：市场元数据 + 可用版本列表。
     *
     * <p><b>版本列表是裁剪过的，不是实体直出</b>：本接口对任意已登录用户开放，而
     * {@link MarketPluginVersion} 实体上挂着 {@code jarUrl}、{@code schemaSql}、
     * {@code rollbackSql} 与制品指纹，整实体序列化等于把这些一起敞开。裁剪清单见
     * {@link #toVersionItem(MarketPluginVersion, String)}。
     */
    public Map<String, Object> detail(String pluginCode) {
        return detail(pluginCode, null);
    }

    public Map<String, Object> detail(String pluginCode, String instanceId) {
        // 2026-10-29：对接平台的站点，插件商品详情（含版本列表）同样以平台目录为准；
        // 本地版本表在开源包里没有制品数据（jar_url 为空是诚实状态，不是缺陷）。
        PlatformMarketClient.RemoteCatalog remote = remoteCatalogOrNull();
        if (remote != null) {
            Map<String, Object> remoteDetail = detailFromRemoteCatalog(remote, pluginCode, instanceId);
            if (remoteDetail != null) {
                return remoteDetail;
            }
            // 平台目录里没有这个插件 = 平台已下架；本地也不该再有，直接报不存在
            throw new IllegalArgumentException("插件不存在: " + pluginCode);
        }
        MarketPlugin plugin = marketPluginMapper.selectOne(new LambdaQueryWrapper<MarketPlugin>()
                .eq(MarketPlugin::getPluginCode, pluginCode)
                .last("limit 1"));
        if (plugin == null) {
            throw new IllegalArgumentException("插件不存在: " + pluginCode);
        }
        List<MarketPluginVersion> versions = marketPluginVersionMapper.selectList(
                new LambdaQueryWrapper<MarketPluginVersion>()
                        .eq(MarketPluginVersion::getPluginCode, pluginCode)
                        .eq(MarketPluginVersion::getStatus, VERSION_APPROVED)
                        .orderByDesc(MarketPluginVersion::getPublishTime));

        String pluginMinFramework = plugin.getMinFrameworkVersion();
        List<Map<String, Object>> versionItems = new ArrayList<>(versions.size());
        for (MarketPluginVersion row : versions) {
            versionItems.add(toVersionItem(row, pluginMinFramework));
        }

        Map<String, Object> result = new LinkedHashMap<>();
        MarketPluginItem pluginItem = toItem(plugin, installMap(List.of(plugin)).get(pluginCode), licenseMap(instanceId).get(pluginCode));
        applyRemoteCounts(List.of(pluginItem));
        result.put("plugin", pluginItem);
        result.put("versions", versionItems);
        return result;
    }

    /**
     * 站点视角的商品详情：平台目录里的插件商品基线 + 本站安装态/授权合并。
     *
     * @param catalog    平台目录
     * @param pluginCode 插件编码
     * @param instanceId 站点实例
     * @return 详情（plugin/versions/plans）；平台目录里没有该插件时返回 null
     */
    private Map<String, Object> detailFromRemoteCatalog(PlatformMarketClient.RemoteCatalog catalog,
                                                        String pluginCode, String instanceId) {
        MarketPluginItem base = catalog.getPlugins().stream()
                .filter(item -> pluginCode.equals(item.getCode()))
                .findFirst()
                .orElse(null);
        if (base == null) {
            return null;
        }
        MarketPluginItem item = decorate(base, installMapOf(List.of(pluginCode)).get(pluginCode),
                licenseMap(instanceId).get(pluginCode), remoteCateNames(catalog));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("plugin", item);
        result.put("versions", catalog.getVersions().getOrDefault(pluginCode, List.of()));
        // 套餐必须带：站点的「购买」入口靠它给出价格选项，缺了就只能是「暂不可购买」
        result.put("plans", catalog.getPlans().getOrDefault(pluginCode, List.of()));
        return result;
    }

    /**
     * 平台侧对外目录：全部在架插件商品 + 分类字典 + 每商品套餐与版本摘要 + 真实下载次数。
     *
     * <p>2026-10-29 架构口径：插件商品的唯一权威源是平台。开源包本地没有插件商品快照，
     * 「别的安装人」的站点打开插件中心时，市场页从这里实时拉目录。返回的只是
     * 公开插件商品信息——不含任何站点的安装态与授权（那些由站点自己合并）。</p>
     *
     * <p>数据量：当前 33 个在架插件商品 + 38 个版本摘要 + 套餐，序列化后约几十 KB，
     * 一次全量返回；站点侧做进程内缓存（默认 300s），不会把平台打成热路径。</p>
     *
     * @return {plugins, categories, plans, versions, generatedAt}
     */
    public Map<String, Object> remoteCatalog() {
        Map<Integer, String> cateNames = cateNameById();
        List<MarketPlugin> plugins = marketPluginMapper.selectList(new LambdaQueryWrapper<MarketPlugin>()
                .eq(MarketPlugin::getStatus, STATUS_PUBLISHED)
                .orderByAsc(MarketPlugin::getCateId)
                .orderByAsc(MarketPlugin::getId));
        List<MarketPluginItem> items = new ArrayList<>(plugins == null ? 0 : plugins.size());
        Map<String, List<Map<String, Object>>> plans = new LinkedHashMap<>();
        Map<String, List<Map<String, Object>>> versions = new LinkedHashMap<>();
        if (plugins != null) {
            for (MarketPlugin plugin : plugins) {
                String code = plugin.getPluginCode();
                if (!StringUtils.hasText(code)) {
                    continue;
                }
                List<MarketPluginVersion> rows = marketPluginVersionMapper.selectList(
                        new LambdaQueryWrapper<MarketPluginVersion>()
                                .eq(MarketPluginVersion::getPluginCode, code)
                                .eq(MarketPluginVersion::getStatus, VERSION_APPROVED)
                                .orderByDesc(MarketPluginVersion::getPublishTime));
                List<Map<String, Object>> versionItems = new ArrayList<>(rows == null ? 0 : rows.size());
                if (rows != null) {
                    for (MarketPluginVersion row : rows) {
                        versionItems.add(toVersionItem(row, plugin.getMinFrameworkVersion()));
                    }
                }
                versions.put(code, versionItems);
                MarketPluginItem item = baselineItem(plugin);
                // 宿主声明（2026-10-30）：站点拿到 hostService 才能在市场页提前判
                // 「本站有没有这个宿主」，而不是点完安装、下载完制品才在派发环节报
                // 「注册中心未找到 xx 服务的可用实例」。以最新已审版本（rows[0]，与
                // orderByDesc 的安装链路取值口径一致）的 plugin_yaml 为准；解析失败
                // 留空（null = 站点不拦截），不让展示数据拖着安装链路一起炸。
                if (rows != null && !rows.isEmpty()) {
                    String declaredHost = declaredHostService(code, rows.get(0));
                    if (StringUtils.hasText(declaredHost)) {
                        item.setHostService(declaredHost);
                    }
                }
                // 展示名归并与本地列表同口径，站点端拿到即可用、不用二次处理
                items.add(decorate(item, null, null, cateNames));
                plans.put(code, channelRouter == null ? List.of() : channelRouter.plansOf(code));
            }
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("plugins", items);
        result.put("categories", categoryOptions());
        result.put("plans", plans);
        result.put("versions", versions);
        result.put("generatedAt", java.time.LocalDateTime.now().toString());
        return result;
    }

    /**
     * 用平台真实下载次数覆盖本地展示值。
     *
     * <p>只在「本站点对接了平台」时生效；平台自身（{@code houcloud.platform.url} 未配置）与
     * 平台不可达的情况都保持本地值——下载次数宁可显示「本站视角的旧值」，
     * 也不能在平台上打个盹时把数字清成 0。
     *
     * @param items 列表项；为空直接返回
     */
    private void applyRemoteCounts(List<MarketPluginItem> items) {
        if (items == null || items.isEmpty() || platformDownloadReporter == null
                || !platformDownloadReporter.platformLinked()) {
            return;
        }
        List<String> codes = items.stream()
                .map(MarketPluginItem::getCode)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());
        if (codes.isEmpty()) {
            return;
        }
        Map<String, Integer> remote = platformDownloadReporter.remoteCounts(codes);
        if (remote.isEmpty()) {
            return;
        }
        for (MarketPluginItem item : items) {
            Integer value = remote.get(item.getCode());
            if (value != null && value > 0) {
                item.setDownloadCount(value);
            }
        }
    }

    /**
     * 在线安装（第二刀核心链路）：下载 → MD5 校验 → 落盘 → 登记本站点安装态。
     *
     * <p>任一步失败都抛异常且不写安装态（{@code @Transactional} 保证库内一致），
     * 已落盘的半成品不会登记为「已安装」。
     *
     * @param pluginCode 插件编码
     * @param version    版本号；为空表示取最新已审核版本
     * @return 安装结果（含落盘路径与摘要，供前端与排障使用）
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> installFromMarket(String pluginCode, String version) {
        return installFromMarket(pluginCode, version, "LIGHTWEIGHT");
    }

    /**
     * Install a market plugin using an explicitly selected runtime profile.
     * Only the embedded lightweight profile is available in this deployment.
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> installFromMarket(String pluginCode, String version, String installProfile) {
        return installFromMarket(pluginCode, version, installProfile, null);
    }

    /**
     * 按站点实例安装市场插件。instanceId 用于把授权校验收敛到当前站点，
     * 避免跨站点复用同一插件的购买记录。
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> installFromMarket(String pluginCode, String version,
                                                 String installProfile, String instanceId) {
        String profile = normalizeInstallProfile(installProfile);
        // 服务型产品守卫：收口到 StandalonePluginCodes（2026-10-27 建立的单一事实源），
        // 此前这里手写 14 个编码，aigateway 晋升独立服务后就没跟上——第三份手抄名单又漂移了。
        if (StandalonePluginCodes.isStandalone(pluginCode)) {
            throw new IllegalStateException("服务型产品不得作为 PF4J 插件安装，请使用插件中心的独立服务安装");
        }
        requireInstallReady();
        // 平台分发模式（2026-10-30 补齐 10-29 口径最后一环）：配了 houcloud.platform.url 的站点，
        // 市场列表实时来自平台目录，而本地市场版本快照恒为空（开源包不带插件商品数据），
        // 继续查本地表只会把「列表里能看到的插件」全部装死在
        // 「插件版本不存在或未通过审核」。插件商品 / 版本审核 / 制品存在的权威判定
        // 全部交给平台 acquire（X-Platform-Token 换票），本地只负责下载、校验、落盘、装载。
        if (platformMarketClient != null && platformMarketClient.platformLinked()) {
            return installFromPlatform(pluginCode, version, profile, instanceId);
        }
        MarketPlugin plugin = marketPluginMapper.selectOne(new LambdaQueryWrapper<MarketPlugin>()
                .eq(MarketPlugin::getPluginCode, pluginCode)
                .last("limit 1"));
        if (plugin == null) {
            throw new IllegalArgumentException("插件不存在: " + pluginCode);
        }
        if (!Integer.valueOf(STATUS_PUBLISHED).equals(plugin.getStatus())) {
            throw new IllegalStateException("插件未上架，无法安装: " + pluginCode);
        }
        // 付费门控：PAID / TRIAL 必须持有本站点有效授权，未授权直接拒绝（不下载、不落盘、不登记）
        assertLicensed(plugin, instanceId);
        // 商业侧校验：这一笔购买是否干净（已支付、非沙箱授权）。
        // 放在下载之前：不干净就别浪费带宽把包拉下来再回滚。
        if (lifecycleSettlementService != null && PluginLicenseService.requiresLicense(plugin.getLicenseType())) {
            lifecycleSettlementService.onInstalled(pluginCode, instanceId);
        }

        MarketPluginVersion target = resolveVersion(pluginCode, version);
        assertSupportedInstallProfile(profile, pluginCode, target);
        if (!StringUtils.hasText(target.getJarUrl())) {
            throw new IllegalStateException("该版本未提供安装包地址: " + pluginCode + "@" + target.getVersion());
        }
        // 跨宿主预检（3020 口径）：本进程只能装载 hostService=base 的嵌入插件。
        // 不预检的话会先下载+落盘，再在 PF4J 引擎里炸成 500「Unknown hostService」——
        // 用户既看不懂，也白拉一次 20MB 制品（2026-10-28 实测：装 OCR 就是这个表现）。
        // 宿主归属：base 只装载声明为 base 的嵌入插件，其余派发给声明的宿主进程。
        // 早于此判定：不能先下载+落盘再炸「Unknown hostService」——白拉一次制品、还留下脏产物。
        String declaredHost = declaredHostService(pluginCode, target);
        String hostService = StringUtils.hasText(declaredHost) ? declaredHost.trim() : HOST_SERVICE_BASE;
        boolean localInstall = HOST_SERVICE_BASE.equalsIgnoreCase(hostService);
        // 宿主可用性预检（2026-10-30）：跨宿主插件先确认宿主在线再换票下载，
        // 避免拉完制品才在派发环节报「注册中心未找到 xx 服务的可用实例」。
        requireHostResolvable(hostService);

        // 1. 换票 → 下载安装包
        //    jar_url 是 ?aid= 规范指针，raw 端点只认令牌；先换票再下载，
        //    换票本身复用下载端点的授权判定，安装与手动下载的口径因此完全一致。
        String downloadUrl = target.getJarUrl();
        if (artifactUrlResolver != null) {
            try {
                String tokenized = artifactUrlResolver.resolveTokenizedUrl(pluginCode, target.getVersion());
                if (StringUtils.hasText(tokenized)) {
                    downloadUrl = tokenized;
                }
            } catch (RuntimeException e) {
                // 回退裸 jar_url 只会得到 401，报错更难懂；换不到票就当场失败
                throw new IllegalStateException("换取安装包下载令牌失败: " + e.getMessage(), e);
            }
        } else {
            log.warn("未装配 ArtifactUrlResolver，按原始 jar_url 下载（仅 http/file 形态可用）：{}/{}",
                    pluginCode, target.getVersion());
        }
        PluginArtifactDownloader.Artifact artifact;
        try {
            artifact = artifactDownloader.download(downloadUrl);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("安装包地址不可用: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new IllegalStateException("下载插件安装包失败: " + e.getMessage(), e);
        }

        // 2. 完整性校验（市场声明了摘要才强校验）
        if (!checksumVerifier.verify(artifact.getContent(), target.getJarMd5())) {
            throw new IllegalStateException("安装包校验失败（MD5 不一致）: " + pluginCode + "@" + target.getVersion());
        }

        // 3. 落盘与装载
        //    base 宿主：落盘到本进程 pluginsRoot 并交给本地 PF4J 引擎装载（与引擎共用目录契约）。
        //    其它宿主：base 绝不落盘（落了也没人扫，还污染 base 目录），改为把制品派发给宿主进程，
        //             由宿主的引擎按自己的 pluginsRoot 落盘并装载。
        Path jarPath;
        if (localInstall) {
            try {
                jarPath = artifactStore.store(pluginCode, target.getVersion(), artifact.getContent());
            } catch (IOException e) {
                throw new IllegalStateException("插件安装包落盘失败: " + e.getMessage(), e);
            }
            // 4. 交给 PF4J 宿主真实装载并注册路由；引擎失败时不登记为已安装
            if (lifecycleManager != null) {
                try {
                    lifecycleManager.install(pluginCode, jarPath, manifestFromDeclaration(
                            pluginCode, target.getVersion(), target.getPluginYaml()));
                } catch (RuntimeException e) {
                    try {
                        artifactStore.delete(pluginCode);
                    } catch (IOException cleanupError) {
                        e.addSuppressed(cleanupError);
                    }
                    throw e;
                }
            }
        } else {
            if (hostInstallDispatcher == null) {
                throw new IllegalStateException("插件归属 " + hostService + " 宿主，"
                        + "但 base 未装配跨宿主安装派发器（错误码 " + ERR_HOST_SERVICE_MISMATCH + "）");
            }
            Map<String, Object> remote = hostInstallDispatcher.install(
                    hostService, pluginCode, target.getVersion(), artifact.getContent(), target.getJarMd5());
            Object remotePath = remote.get("jarPath");
            // 宿主侧路径只作留痕：base 不访问该文件系统，落盘与装载都已由宿主完成
            jarPath = remotePath == null || !StringUtils.hasText(String.valueOf(remotePath))
                    ? Path.of(hostService + ":" + pluginCode + "/" + target.getVersion())
                    : Path.of(String.valueOf(remotePath));
        }

        // 5. 登记本站点安装态
        String checksum = StringUtils.hasText(target.getJarMd5())
                ? target.getJarMd5().trim().toLowerCase()
                : artifact.md5();
        try {
            upsertInstall(pluginCode, target.getVersion(), jarPath.toString(), checksum);
            // 5.1 plugin.yaml → hou_addon_plugin 单向同步（P0-3）：以包内声明刷新站点目录行，
            //     与安装态同一事务；失败则整体回滚并由下方 catch 撤销 PF4J 装载与落盘产物。
            if (addonPluginSyncService != null) {
                addonPluginSyncService.syncFromPluginYaml(target.getPluginYaml());
            }
            syncMenusFromPluginYaml(pluginCode, target.getPluginYaml());
            bumpCounters(plugin);
            // 5.1 下载明细（下载次数的权威来源）：一行 = 本站点对某版本的一次真实下载。
            //     明细失败不能拖垮安装——它内部吞异常，只记 warn。
            recordDownload(pluginCode, target.getVersion());
            // 5.2 试用期从「首次成功安装」起算（M5，方案 §7.3）：
            //     TRIAL 授权签发时不扣时间，到这里才回填 expire_time = now + trial_days。
            //     与安装态同一事务：起算失败则整笔安装回滚，避免「已装但试用期没起算」。
            if (licenseService != null
                    && PluginLicenseService.requiresLicense(plugin.getLicenseType())
                    && StringUtils.hasText(instanceId)) {
                licenseService.markFirstInstall(pluginCode, instanceId.trim(), plugin.getTrialDays());
            }
        } catch (RuntimeException persistenceError) {
            // 文件系统和 PF4J 不受数据库事务回滚约束，持久化失败时主动撤销运行态和产物。
            if (localInstall) {
                if (lifecycleManager != null && lifecycleManager.isLoaded(pluginCode)) {
                    lifecycleManager.uninstall(pluginCode);
                }
                try {
                    artifactStore.delete(pluginCode);
                } catch (IOException cleanupError) {
                    persistenceError.addSuppressed(cleanupError);
                }
            } else if (hostInstallDispatcher != null) {
                // 跨宿主：宿主侧已装载成功但本站安装态没落库，必须回头卸载，避免「宿主在跑、市场显示未装」。
                try {
                    hostInstallDispatcher.uninstall(hostService, pluginCode);
                } catch (RuntimeException cleanupError) {
                    persistenceError.addSuppressed(cleanupError);
                }
            }
            throw persistenceError;
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("code", pluginCode);
        result.put("version", target.getVersion());
        result.put("licenseType", plugin.getLicenseType());
        result.put("jarPath", jarPath.toString());
        result.put("hostService", hostService);
        result.put("installedBy", localInstall ? HOST_SERVICE_BASE : hostService);
        result.put("size", artifact.size());
        result.put("checksum", checksum);
        result.put("protocol", artifact.getProtocol());
        result.put("sourceUrl", artifact.getSourceUrl());
        result.put("installed", true);
        result.put("enabled", true);
        result.put("installProfile", profile);
        return result;
    }

    /**
     * 平台分发安装（2026-10-30）：换票 → 下载 → 指纹校验 → 落盘 → 装载 → 登记安装态。
     *
     * <p>与本地安装链路的主要差异：</p>
     * <ul>
     *   <li>商品 / 版本 / 制品的权威判定全在平台侧（acquire 返回指纹与插件清单），
     *       本地不再查市场快照——开源包本地版本表恒为空；</li>
     *   <li>付费门控也由平台 acquire 判（免费插件才放行，付费插件平台直接拒绝），
     *       本地授权表没有平台购买记录，不能在此兜底；</li>
     *   <li>本地商品快照计数不 bump（列表展示值以平台真实计数为准）。</li>
     * </ul>
     *
     * <p>失败语义与本地链路一致：任一步失败抛异常、不写安装态，
     * 已落盘的产物与运行态主动撤销。</p>
     */
    private Map<String, Object> installFromPlatform(String pluginCode, String version,
                                                     String profile, String instanceId) {
        Map<String, Object> acquired = platformMarketClient.acquire(pluginCode, version);
        String resolvedVersion = textOf(acquired, "version");
        String jarMd5 = textOf(acquired, "jarMd5");
        String pluginYaml = textOf(acquired, "pluginYaml");
        String declaredHost = textOf(acquired, "hostService");
        String downloadUrl = textOf(acquired, "downloadUrl");
        if (!StringUtils.hasText(resolvedVersion)) {
            throw new IllegalStateException("平台换票未返回版本号: " + pluginCode);
        }
        if (!StringUtils.hasText(downloadUrl)) {
            throw new IllegalStateException("平台换票未返回下载地址: " + pluginCode + "@" + resolvedVersion);
        }
        String hostService = StringUtils.hasText(declaredHost) ? declaredHost.trim() : HOST_SERVICE_BASE;
        boolean localInstall = HOST_SERVICE_BASE.equalsIgnoreCase(hostService);
        // 宿主可用性预检（2026-10-30）：acquire 已拿到宿主声明，下载前确认宿主在线；
        // 开源/精简站点未部署目标宿主时，在这里就把原因讲清楚（与列表层
        // applyHostAvailability 同口径），不再等下载完才报注册中心找不到实例。
        requireHostResolvable(hostService);

        // 1. 下载（平台 raw 端点，令牌在 acquire 时签发：10 分钟 + 限次 + 绑站点）
        PluginArtifactDownloader.Artifact artifact;
        try {
            artifact = artifactDownloader.download(downloadUrl);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("安装包地址不可用: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new IllegalStateException("下载插件安装包失败: " + e.getMessage(), e);
        }

        // 2. 完整性校验（与平台制品指纹比对；平台未带回指纹时不强校验，同本地口径）
        if (StringUtils.hasText(jarMd5) && !checksumVerifier.verify(artifact.getContent(), jarMd5)) {
            throw new IllegalStateException("安装包校验失败（MD5 与平台指纹不一致）: "
                    + pluginCode + "@" + resolvedVersion);
        }

        // 3. 落盘与装载（与本地安装共用目录契约与跨宿主派发器）
        Path jarPath;
        if (localInstall) {
            try {
                jarPath = artifactStore.store(pluginCode, resolvedVersion, artifact.getContent());
            } catch (IOException e) {
                throw new IllegalStateException("插件安装包落盘失败: " + e.getMessage(), e);
            }
            if (lifecycleManager != null) {
                try {
                    lifecycleManager.install(pluginCode, jarPath,
                            manifestFromDeclaration(pluginCode, resolvedVersion, pluginYaml));
                } catch (RuntimeException e) {
                    try {
                        artifactStore.delete(pluginCode);
                    } catch (IOException cleanupError) {
                        e.addSuppressed(cleanupError);
                    }
                    throw e;
                }
            }
        } else {
            if (hostInstallDispatcher == null) {
                throw new IllegalStateException("插件归属 " + hostService + " 宿主，"
                        + "但 base 未装配跨宿主安装派发器（错误码 " + ERR_HOST_SERVICE_MISMATCH + "）");
            }
            Map<String, Object> remote = hostInstallDispatcher.install(
                    hostService, pluginCode, resolvedVersion, artifact.getContent(), jarMd5);
            Object remotePath = remote.get("jarPath");
            jarPath = remotePath == null || !StringUtils.hasText(String.valueOf(remotePath))
                    ? Path.of(hostService + ":" + pluginCode + "/" + resolvedVersion)
                    : Path.of(String.valueOf(remotePath));
        }

        // 4. 登记本站点安装态（失败时撤销运行态与产物，与本地链路同语义）
        String checksum = StringUtils.hasText(jarMd5) ? jarMd5.trim().toLowerCase() : artifact.md5();
        try {
            upsertInstall(pluginCode, resolvedVersion, jarPath.toString(), checksum);
            if (addonPluginSyncService != null) {
                addonPluginSyncService.syncFromPluginYaml(pluginYaml);
            }
            // 目录行同步 ≠ 权限菜单同步：漏掉这一步右上角「插件应用」永远看不到已装插件
            syncMenusFromPluginYaml(pluginCode, pluginYaml);
            // 下载明细会同时上报平台真实计数（未对接平台时静默保持本地明细）
            recordDownload(pluginCode, resolvedVersion);
        } catch (RuntimeException persistenceError) {
            if (localInstall) {
                if (lifecycleManager != null && lifecycleManager.isLoaded(pluginCode)) {
                    lifecycleManager.uninstall(pluginCode);
                }
                try {
                    artifactStore.delete(pluginCode);
                } catch (IOException cleanupError) {
                    persistenceError.addSuppressed(cleanupError);
                }
            } else if (hostInstallDispatcher != null) {
                try {
                    hostInstallDispatcher.uninstall(hostService, pluginCode);
                } catch (RuntimeException cleanupError) {
                    persistenceError.addSuppressed(cleanupError);
                }
            }
            throw persistenceError;
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("code", pluginCode);
        result.put("version", resolvedVersion);
        // acquire 只对免费插件放行；付费插件平台侧已拒绝，这里恒为 FREE
        result.put("licenseType", "FREE");
        result.put("jarPath", jarPath.toString());
        result.put("hostService", hostService);
        result.put("installedBy", localInstall ? HOST_SERVICE_BASE : hostService);
        result.put("size", artifact.size());
        result.put("checksum", checksum);
        result.put("protocol", artifact.getProtocol());
        result.put("sourceUrl", artifact.getSourceUrl());
        result.put("installed", true);
        result.put("enabled", true);
        result.put("installProfile", profile);
        result.put("source", "platform");
        return result;
    }

    private static String textOf(Map<String, Object> map, String key) {
        Object value = map == null ? null : map.get(key);
        return value == null ? null : String.valueOf(value).trim();
    }

    private String normalizeInstallProfile(String installProfile) {
        if (!StringUtils.hasText(installProfile)) {
            return "LIGHTWEIGHT";
        }
        String profile = installProfile.trim().toUpperCase(Locale.ROOT);
        if ("HEAVY".equals(profile)) {
            throw new IllegalStateException("当前仅支持 LIGHTWEIGHT 插件模式，HEAVY 尚未部署独立服务");
        }
        if ("CORE".equals(profile)) {
            throw new IllegalStateException("CORE 为平台核心模式，不允许站点安装");
        }
        if (!"LIGHTWEIGHT".equals(profile)) {
            throw new IllegalArgumentException("不支持的插件安装模式: " + installProfile);
        }
        return profile;
    }

    private void assertSupportedInstallProfile(String profile, String pluginCode, MarketPluginVersion target) {
        if (!"LIGHTWEIGHT".equals(profile) || !StringUtils.hasText(target.getPluginYaml())) {
            return;
        }
        Map<String, Object> declaration = parsePluginDeclaration(target.getPluginYaml());
        Object runtime = declaration.get("runtime");
        if (runtime instanceof Map<?, ?> runtimeMap) {
            Object supportedRuntime = runtimeMap.get("supported");
            if (supportedRuntime != null && !containsProfile(supportedRuntime, "embedded")) {
                throw new IllegalStateException("插件 " + pluginCode + " 未声明 LIGHTWEIGHT/embedded 运行模式");
            }
        } else if (runtime != null && !"embedded".equalsIgnoreCase(String.valueOf(runtime).trim())) {
            throw new IllegalStateException("插件 " + pluginCode + " 未声明 LIGHTWEIGHT/embedded 运行模式");
        }
        Object supported = declaration.get("supportedInstallProfiles");
        if (supported == null) {
            supported = declaration.get("installProfiles");
        }
        if (supported instanceof Map<?, ?> runtimeMap) {
            supported = runtimeMap.get("supported");
        }
        if (supported != null && !containsProfile(supported, "LIGHTWEIGHT")) {
            throw new IllegalStateException("插件 " + pluginCode + " 未声明支持 LIGHTWEIGHT 安装模式");
        }
    }

    private Map<String, Object> parsePluginDeclaration(String yaml) {
        if (!StringUtils.hasText(yaml)) {
            return Map.of();
        }
        // 历史写入（mysql -B 批处理等）把换行存成字面 \n 转义、整段挤成一行：
        // SnakeYAML 只能解出第一个键（code），hostService / runtime / owner 全部丢失
        // —— 目录项宿主预检会因此拿到 null（2026-10-30 实锤：userms-redpacket 的
        // plugin_yaml 便是这种形态）。仅在「无真实换行且带字面转义」时反转义，
        // 真多行文本与含 \n 字符串值的正常声明不受影响。
        String text = yaml;
        if (!text.contains("\n") && text.contains("\\n")) {
            text = text.replace("\\n", "\n");
        }
        try {
            Object value = new Yaml().load(text);
            if (value instanceof Map<?, ?> raw) {
                Map<String, Object> result = new LinkedHashMap<>();
                raw.forEach((key, item) -> result.put(String.valueOf(key), item));
                return result;
            }
        } catch (RuntimeException ignored) {
            // Older market rows may contain a JSON fragment or invalid optional metadata.
            try {
                Map<String, Object> json = JSON.parseObject(yaml);
                return json == null ? Map.of() : json;
            } catch (RuntimeException ignoredJson) {
                // Missing/invalid optional declaration keeps the legacy lightweight default.
            }
        }
        return Map.of();
    }

    private PluginManifest manifestFromDeclaration(String pluginCode, String version, String yaml) {
        Map<String, Object> declaration = parsePluginDeclaration(yaml);
        PluginManifest manifest = new PluginManifest();
        manifest.setCode(pluginCode);
        manifest.setVersion(version);
        Object owner = declaration.get("owner");
        String ownerText = owner == null ? null : String.valueOf(owner).trim();
        manifest.setOwner(StringUtils.hasText(ownerText) ? ownerText : "official");
        Object runtime = declaration.get("runtime");
        if (runtime != null && !(runtime instanceof Map<?, ?>)) {
            manifest.setRuntime(String.valueOf(runtime).trim());
        }
        // 与 PluginUpdateService.manifestOf 同理：引擎会校验 supplied 清单的归属，
        // 不回填 hostService/host 会在 resolve 处抛 "Unknown hostService: null"。
        Object hostService = declaration.get("hostService");
        if (hostService != null) {
            manifest.setHostService(String.valueOf(hostService).trim());
        }
        Object host = declaration.get("host");
        if (host != null) {
            manifest.setHost(String.valueOf(host).trim());
        }
        return manifest;
    }

    /**
     * 读取版本清单里声明的 {@code hostService}。
     *
     * <p>解析失败返回 {@code null}：预检只是提前给出可读的错误，清单格式问题不该把安装
     * 整体拒掉，交给后面的引擎与兼容性预检处理更有上下文。
     *
     * @param pluginCode 插件编码
     * @param target     目标版本
     * @return 声明的宿主服务名；未声明或解析失败为 null
     */
    private String declaredHostService(String pluginCode, MarketPluginVersion target) {
        try {
            PluginManifest manifest = manifestFromDeclaration(
                    pluginCode, target.getVersion(), target.getPluginYaml());
            String host = manifest.getHostService();
            return StringUtils.hasText(host) ? host.trim() : null;
        } catch (RuntimeException e) {
            log.warn("解析插件清单失败，跳过跨宿主预检：{}@{} err={}",
                    pluginCode, target.getVersion(), e.getMessage());
            return null;
        }
    }

    /**
     * 站点视角宿主可用性：写入列表项的 hostAvailable / hostUnavailableReason。
     *
     * <p>判定口径与 {@link PluginHostInstallDispatcher} 的地址解析完全一致
     * （host-endpoints 显式配置优先，注册中心实例其次）——预检结论和派发解析共享
     * 同一优先级，才能保证「列表说能装 → 安装就真的能派发」。未声明宿主（null，
     * 旧平台目录兼容）与 base 本机装载不拦截，保持与既有行为一致。</p>
     *
     * <p>只写 false 或 true：留 null 表示「未知，不拦截」给前端（旧平台 + 新站点的
     * 过渡形态）。</p>
     */
    private void applyHostAvailability(MarketPluginItem item) {
        String host = item.getHostService();
        if (!StringUtils.hasText(host) || HOST_SERVICE_BASE.equalsIgnoreCase(host.trim())) {
            return;
        }
        if (hostEndpointResolvable(host.trim())) {
            item.setHostAvailable(true);
        } else {
            item.setHostAvailable(false);
            item.setHostUnavailableReason("插件归属 " + host.trim()
                    + " 宿主服务，当前站点未部署该服务，无法安装");
        }
    }

    /**
     * 宿主地址能否解析（与派发器 resolveBaseUrl 同口径，但不抛异常）：
     * host-endpoints 显式配置优先，注册中心实例其次。
     */
    private boolean hostEndpointResolvable(String hostService) {
        if (environment != null) {
            String configured = environment.getProperty(
                    "houcloud.plugin.host-endpoints." + hostService);
            if (configured != null && !configured.isBlank()) {
                return true;
            }
        }
        if (discoveryClient != null) {
            try {
                var instances = discoveryClient.getInstances(hostService);
                if (instances != null && !instances.isEmpty()) {
                    return true;
                }
            } catch (RuntimeException ignored) {
                // 注册中心查询异常按不可用处理：与派发器行为一致（那邊会抛，这边预检
                // 不能因目录页把列表拖挂）。
            }
        }
        return false;
    }

    /**
     * 安装链路宿主预检（2026-10-30）：宿主不在线就别浪费一次下载，直接把
     * 「这个站点为什么装不了」说清楚——替代派发环节的
     * 「注册中心未找到 xx 服务的可用实例」报错（那是运维向文案，用户看不懂）。
     *
     * <p>未声明宿主与 base 本机装载直接放行；判定口径与列表展示一致，
     * 避免列表说能装、安装又拒绝的割裂。</p>
     */
    private void requireHostResolvable(String hostService) {
        if (!StringUtils.hasText(hostService) || HOST_SERVICE_BASE.equalsIgnoreCase(hostService.trim())) {
            return;
        }
        String host = hostService.trim();
        if (hostEndpointResolvable(host)) {
            return;
        }
        throw new IllegalStateException("插件归属 " + host + " 宿主服务，当前站点未部署该服务，无法安装；"
                + "部署该服务后即可安装，或由运维配置 houcloud.plugin.host-endpoints." + host
                + " 显式指定宿主地址");
    }

    private boolean containsProfile(Object value, String expected) {
        if (value instanceof Iterable<?> iterable) {
            for (Object item : iterable) {
                if (expected.equalsIgnoreCase(String.valueOf(item).trim())) {
                    return true;
                }
            }
            return false;
        }
        String text = String.valueOf(value);
        for (String item : text.split(",")) {
            if (expected.equalsIgnoreCase(item.trim())) {
                return true;
            }
        }
        return false;
    }

    private void requireInstallReady() {
        if (artifactDownloader == null || artifactStore == null || checksumVerifier == null) {
            throw new IllegalStateException("插件安装能力未装配（缺少下载器 / 落盘仓库 / 校验器）");
        }
    }

    /**
     * 付费门控：PAID / TRIAL 插件必须已持有本站点有效授权，否则抛异常。
     *
     * <p>FREE 插件直接放行；授权能力未装配时按「不可安装」处理，避免静默放行付费插件。
     */
    /**
     * 付费门控：PAID / TRIAL 必须持有<b>本站点</b>有效授权。
     *
     * <p><b>fail-closed</b>：instanceId 为空时直接拒绝，不再退化成「不限实例」的查询——
     * 否则站点侧漏传实例标识就能复用其它站点的购买记录安装付费插件。
     * 平台内部调用若确实无站点身份，应显式走 {@code instanceId} 参数而不是省略它。
     */
    private void assertLicensed(MarketPlugin plugin, String instanceId) {
        if (!PluginLicenseService.requiresLicense(plugin.getLicenseType())) {
            return;
        }
        if (licenseService == null) {
            throw new IllegalStateException("插件授权能力未装配，无法安装付费插件: " + plugin.getPluginCode());
        }
        if (!StringUtils.hasText(instanceId)) {
            throw new IllegalStateException(
                    "安装付费插件必须携带站点实例标识（instanceId），无法确认授权归属: " + plugin.getPluginCode());
        }
        licenseService.assertInstallable(plugin.getPluginCode(), instanceId.trim());
    }

    private MarketPluginVersion resolveVersion(String pluginCode, String version) {
        LambdaQueryWrapper<MarketPluginVersion> wrapper = new LambdaQueryWrapper<MarketPluginVersion>()
                .eq(MarketPluginVersion::getPluginCode, pluginCode)
                .eq(MarketPluginVersion::getStatus, VERSION_APPROVED);
        if (StringUtils.hasText(version)) {
            wrapper.eq(MarketPluginVersion::getVersion, version.trim());
        } else {
            wrapper.orderByDesc(MarketPluginVersion::getPublishTime)
                    .orderByDesc(MarketPluginVersion::getId);
        }
        wrapper.last("limit 1");
        MarketPluginVersion target = marketPluginVersionMapper.selectOne(wrapper);
        if (target == null) {
            throw new IllegalStateException("插件版本不存在或未通过审核: " + pluginCode
                    + "@" + (StringUtils.hasText(version) ? version.trim() : "latest"));
        }
        return target;
    }

    /**
     * 用换票/版本表带回的 plugin.yaml 文本同步权限菜单。
     * 失败只记日志：安装态已写入，菜单可后续对账补齐，不能把整次安装打成失败。
     */
    private void syncMenusFromPluginYaml(String pluginCode, String pluginYaml) {
        if (pluginMenuPort == null || !StringUtils.hasText(pluginCode) || !StringUtils.hasText(pluginYaml)) {
            return;
        }
        Path dir = null;
        try {
            dir = Files.createTempDirectory("plugin-menu-" + pluginCode + "-");
            Path yaml = dir.resolve("plugin.yaml");
            Files.writeString(yaml, pluginYaml, StandardCharsets.UTF_8);
            pluginMenuPort.sync(pluginCode, dir);
        } catch (Exception e) {
            log.warn("插件 {} 安装后菜单同步失败（安装态已保留，右上角可能暂不可见）: {}",
                    pluginCode, e.getMessage());
        } finally {
            if (dir != null) {
                try {
                    Files.deleteIfExists(dir.resolve("plugin.yaml"));
                    Files.deleteIfExists(dir);
                } catch (IOException ignored) {
                    // temp cleanup best-effort
                }
            }
        }
    }

    private void upsertInstall(String pluginCode, String version, String jarPath, String checksum) {
        MallPluginInstall install = findInstall(pluginCode);
        LocalDateTime now = LocalDateTime.now();
        if (install == null) {
            install = new MallPluginInstall();
            install.setPluginCode(pluginCode);
            install.setInstalled(1);
            install.setEnabled(1);
            install.setVersion(version);
            install.setJarPath(jarPath);
            install.setChecksum(checksum);
            install.setConfigJson("{}");
            install.setInstallTime(now);
            install.setUpdateTime(now);
            install.setDeleted(0);
            pluginInstallMapper.insert(install);
            return;
        }
        install.setInstalled(1);
        install.setEnabled(1);
        install.setVersion(version);
        install.setJarPath(jarPath);
        install.setChecksum(checksum);
        install.setUpdateTime(now);
        pluginInstallMapper.updateById(install);
    }

    private void bumpCounters(MarketPlugin plugin) {
        plugin.setInstallCount((plugin.getInstallCount() == null ? 0 : plugin.getInstallCount()) + 1);
        // 下载次数由明细表回写（PluginDownloadStatsService#record → syncDownloadCount），
        // 这里不再自增：两条写入路径同时改同一个字段，必然和明细对不上。
        // 只有没装配统计服务（单测 / 精简装配）时才退回自增。
        if (downloadStatsService == null) {
            plugin.setDownloadCount((plugin.getDownloadCount() == null ? 0 : plugin.getDownloadCount()) + 1);
        }
        marketPluginMapper.updateById(plugin);
    }

    /**
     * 记录一次真实下载，并把本站点这次安装上报平台。
     *
     * <p>为什么是「先落本地明细，再异步上报」：本地明细是本站点自己的账（离线也要有数），
     * 平台那份是所有安装人的公共账。两者都写失败也只是「这次没记上」，不能影响插件装上。
     *
     * @param pluginCode 插件编码
     * @param version    版本号
     */
    private void recordDownload(String pluginCode, String version) {
        String siteCode = platformDownloadReporter == null
                ? PluginDownloadStatsService.SITE_PLATFORM
                : platformDownloadReporter.localSiteCode();
        String siteName = platformDownloadReporter == null ? "" : platformDownloadReporter.localSiteName();
        if (downloadStatsService != null) {
            downloadStatsService.record(pluginCode, version, siteCode, siteName, "install", null);
        }
        if (platformDownloadReporter != null) {
            platformDownloadReporter.reportAsync(pluginCode, version, "install");
        }
    }

    private Map<String, MallPluginInstall> installMap(List<MarketPlugin> plugins) {
        if (plugins == null || plugins.isEmpty()) {
            return Map.of();
        }
        List<String> codes = plugins.stream()
                .map(MarketPlugin::getPluginCode)
                .filter(StringUtils::hasText)
                .toList();
        List<MallPluginInstall> installs = pluginInstallMapper.selectList(
                new LambdaQueryWrapper<MallPluginInstall>()
                        .in(MallPluginInstall::getPluginCode, codes));
        return installs.stream()
                .filter(item -> item.getPluginCode() != null)
                .collect(Collectors.toMap(MallPluginInstall::getPluginCode, Function.identity(), (a, b) -> a));
    }

    /**
     * 按编码批量查安装态（一次查询，避免逐条查库）。
     *
     * @param codes 插件编码
     * @return 编码 -&gt; 安装态
     */
    private Map<String, MallPluginInstall> installMapOf(List<String> codes) {
        if (codes == null || codes.isEmpty()) {
            return Map.of();
        }
        List<MallPluginInstall> installs = pluginInstallMapper.selectList(
                new LambdaQueryWrapper<MallPluginInstall>()
                        .in(MallPluginInstall::getPluginCode, codes));
        return installs.stream()
                .filter(item -> item.getPluginCode() != null)
                .collect(Collectors.toMap(MallPluginInstall::getPluginCode, Function.identity(), (a, b) -> a));
    }

    private int positiveInt(Object value, int fallback) {
        try {
            int parsed = Integer.parseInt(String.valueOf(value));
            return parsed > 0 ? parsed : fallback;
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private MallPluginInstall findInstall(String pluginCode) {
        return pluginInstallMapper.selectOne(new LambdaQueryWrapper<MallPluginInstall>()
                .eq(MallPluginInstall::getPluginCode, pluginCode)
                .last("limit 1"));
    }

    /**
     * 本站点有效授权映射：pluginCode -&gt; license（授权能力未装配时为空）。
     */
    private Map<String, MarketPluginLicense> licenseMap(String instanceId) {
        if (licenseService == null) {
            return Map.of();
        }
        List<MarketPluginLicense> licenses = StringUtils.hasText(instanceId)
                ? licenseService.listMine(instanceId)
                : licenseService.listValid();
        if (licenses == null) {
            return Map.of();
        }
        return licenses.stream()
                .filter(item -> StringUtils.hasText(item.getPluginCode()))
                .collect(Collectors.toMap(MarketPluginLicense::getPluginCode, Function.identity(), (a, b) -> a));
    }

    private MarketPluginItem toItem(MarketPlugin plugin, MallPluginInstall install, MarketPluginLicense license) {
        return decorate(baselineItem(plugin), install, license);
    }

    /**
     * 版本实体 → 租户可见的展示项（<b>白名单挑字段，不是整实体直出</b>）。
     *
     * <p>为什么必须挑：{@link MarketPluginVersion} 是按「发布与分发」建模的库表实体，
     * 上面除了版本号还挂着三类不该给租户的东西：
     * <ul>
     *   <li><b>制品直链</b> {@code jarUrl}——下载必须走 {@code /base/plugin-center/artifact/download-url} 换票，
     *       直链外泄等于绕过换票与授权；</li>
     *   <li><b>建表与回滚 SQL</b> {@code schemaSql} / {@code rollbackSql}——库结构直接暴露，
     *       回滚 SQL 更是把「怎么把库改回去」写给了攻击者；</li>
     *   <li><b>制品指纹</b> {@code jarMd5} / {@code jarSha256} / {@code jarSize}——不买也能
     *       枚举全部版本的指纹，可用于确认目标版本、比对供应链投毒、做变更监控。
     *       指纹的归属是换票成功后随链接下发（见 {@code DownloadDeniedException}），这里不重复暴露。</li>
     * </ul>
     * 另有 {@code jarSignature} / {@code storageKey} / {@code pluginYaml} / {@code reviewNote}
     * 等分发与审核内部字段，同样不在租户视野内。
     *
     * <p><b>为什么是白名单而不是「拉黑这几个字段」</b>：拉黑会随实体加列而失效——
     * 下次有人往 {@code MarketPluginVersion} 上加一个敏感列，它会默认出现在本接口的返回里。
     * 白名单反过来：新列默认不可见，要暴露必须显式加进来并说明理由。
     *
     * @param row                版本实体
     * @param pluginMinFramework 插件级最低框架版本；为空则 {@code compatible} 不做判定
     * @return 租户可见字段：{@code id} / {@code version} / {@code changelog} /
     *         {@code publishTime} / {@code minFrameworkVersion} / {@code compatible}
     */
    private Map<String, Object> toVersionItem(MarketPluginVersion row, String pluginMinFramework) {
        Map<String, Object> item = new LinkedHashMap<>();
        // 前端版本表格 row-key="id" 依赖它，缺了 el-table 的展开行与行状态会错乱
        item.put("id", row.getId());
        item.put("version", row.getVersion() == null ? null : row.getVersion().trim());
        item.put("changelog", row.getChangelog());
        item.put("publishTime", row.getPublishTime() == null ? null
                : row.getPublishTime().format(TIME_FORMATTER));
        String required = StringUtils.hasText(pluginMinFramework) ? pluginMinFramework.trim() : null;
        Boolean compatible = null;
        if (StringUtils.hasText(frameworkVersion) && StringUtils.hasText(required)) {
            compatible = VersionComparison.atLeast(frameworkVersion.trim(), required);
        }
        item.put("minFrameworkVersion", required);
        item.put("compatible", compatible);
        return item;
    }

    /**
     * 公开基线项：<b>只包含市场元数据</b>，不含安装态 / 授权等租户维度字段。
     *
     * <p>为什么要拆出来：目录索引缓存只能缓存与租户无关的部分（方案 §6.3），
     * 租户字段若进了缓存，缓存规模会随站点数膨胀，且任何一次安装都要失效全站。
     *
     * @param plugin 插件商品行
     * @return 公开基线项
     */
    private MarketPluginItem baselineItem(MarketPlugin plugin) {
        MarketPluginItem item = new MarketPluginItem();
        item.setCode(plugin.getPluginCode());
        item.setName(plugin.getName());
        // cateId 是归属（权威），category 只是展示名；真正的规范名归并在 decorate 里做
        // （那里才拿得到分类字典，且要处理"字典里查不到 → 未分类"的兜底）。
        item.setCateId(plugin.getCateId() == null || plugin.getCateId() < 0 ? 0 : plugin.getCateId());
        item.setCategory(plugin.getCategory());
        item.setDescription(plugin.getDescription());
        item.setShortDesc(plugin.getShortDesc());
        item.setIconUrl(plugin.getIconUrl());
        item.setLatestVersion(plugin.getLatestVersion());
        item.setLicenseType(plugin.getLicenseType());
        item.setPriceCents(plugin.getPriceCents());
        item.setPriceText(priceText(plugin));
        item.setTrialDays(plugin.getTrialDays());
        item.setDownloadCount(plugin.getDownloadCount());
        item.setInstallCount(plugin.getInstallCount());
        item.setRatingAvg(plugin.getRatingAvg());
        item.setRatingCount(plugin.getRatingCount());
        item.setTags(parseStringList(plugin.getTagsJson()));
        item.setTerminals(parseStringList(plugin.getTerminalsJson()));
        item.setDependencies(parseStringList(plugin.getDependenciesJson()));
        // 运行方式是发布期元数据（公开、与租户无关），进基线缓存。
        // 前端安装弹窗靠它把「选运行方式」收敛成「按清单确认」——缺了它弹窗只能两个都放出来让用户选。
        item.setRuntime(declaredRuntimeOf(plugin.getPluginCode()));
        return item;
    }

    /**
     * 读取插件清单声明的运行方式（embedded / standalone）。
     *
     * <p>优先级：最新已审版本 {@code plugin_yaml} 的 {@code runtime} 声明 →
     * {@link StandalonePluginCodes} 兜底（快照市场没有版本行时仍能给出稳定答案）。
     * 解析失败不抛异常：运行方式只是展示与弹窗锁定用，不拦截安装链路。
     *
     * @param pluginCode 插件编码
     * @return 小写的 runtime；无法判定时按内嵌处理（与后端安装只认 LIGHTWEIGHT 一致）
     */
    private String declaredRuntimeOf(String pluginCode) {
        if (!StringUtils.hasText(pluginCode)) {
            return "embedded";
        }
        try {
            MarketPluginVersion latest = marketPluginVersionMapper.selectOne(
                    new LambdaQueryWrapper<MarketPluginVersion>()
                            .eq(MarketPluginVersion::getPluginCode, pluginCode)
                            .eq(MarketPluginVersion::getStatus, VERSION_APPROVED)
                            .orderByDesc(MarketPluginVersion::getPublishTime)
                            .last("limit 1"));
            if (latest != null && StringUtils.hasText(latest.getPluginYaml())) {
                PluginManifest manifest = manifestFromDeclaration(
                        pluginCode, latest.getVersion(), latest.getPluginYaml());
                if (StringUtils.hasText(manifest.getRuntime())) {
                    return manifest.getRuntime().trim().toLowerCase(Locale.ROOT);
                }
            }
        } catch (RuntimeException ignored) {
            // 清单缺失/格式异常：走独立服务名单兜底，不把市场列表拖挂
        }
        return StandalonePluginCodes.isStandalone(pluginCode) ? "standalone" : "embedded";
    }

    /**
     * 给公开基线叠加租户维度字段（安装态 / 授权 / 购买状态）。
     *
     * <p>返回<b>新对象</b>而不是就地修改入参：缓存反序列化出来的对象可能被复用，
     * 就地写租户字段会把 A 站点的安装态泄漏给 B 站点——这是缓存租户数据的典型事故，
     * 因此这里强制复制一份再叠加。
     *
     * @param baseline 公开基线项
     * @param install  本站点安装态，可为 null
     * @param license  本站点有效授权，可为 null
     * @return 叠加后的完整展示项
     */
    /** 不传分类字典的重载：内部自行取一次（单次详情场景，不是列表循环）。 */
    private MarketPluginItem decorate(MarketPluginItem baseline, MallPluginInstall install,
                                      MarketPluginLicense license) {
        return decorate(baseline, install, license, null);
    }

    private MarketPluginItem decorate(MarketPluginItem baseline, MallPluginInstall install,
                                      MarketPluginLicense license, Map<Integer, String> cateNames) {
        Map<Integer, String> cates = cateNames == null ? cateNameById() : cateNames;
        MarketPluginItem item = new MarketPluginItem();
        item.setCode(baseline.getCode());
        item.setName(baseline.getName());
        // 分类：以 cateId（外键）为归属，展示名由分类字典派生 —— 这样重命名分类
        // 不需要同步改插件数据。字典查不到 / 未归类 → 落到「未分类」，绝不返回空。
        Integer resolvedCateId = normalizeCateId(baseline.getCateId(), cates.keySet());
        item.setCateId(resolvedCateId);
        if (!cates.isEmpty()) {
            item.setCategory(cates.getOrDefault(resolvedCateId, UNCATEGORIZED_LABEL));
        } else {
            // 分类字典不可用（表缺失 / 查询异常）：保留库里原始文案兜底展示，
            // 不能因为读不到分类表就让市场页的分类角标整片空白。
            item.setCategory(StringUtils.hasText(baseline.getCategory())
                    ? baseline.getCategory()
                    : UNCATEGORIZED_LABEL);
        }
        item.setDescription(baseline.getDescription());
        item.setShortDesc(baseline.getShortDesc());
        item.setIconUrl(baseline.getIconUrl());
        item.setLatestVersion(baseline.getLatestVersion());
        // 运行方式随基线透传（2026-10-29 新增字段，此处白名单漏了它会静默丢字段——接口仍返回 null）
        item.setRuntime(baseline.getRuntime());
        item.setLicenseType(baseline.getLicenseType());
        item.setPriceCents(baseline.getPriceCents());
        item.setPriceText(baseline.getPriceText());
        item.setTrialDays(baseline.getTrialDays());
        item.setDownloadCount(baseline.getDownloadCount());
        item.setInstallCount(baseline.getInstallCount());
        item.setRatingAvg(baseline.getRatingAvg());
        item.setRatingCount(baseline.getRatingCount());
        item.setTags(baseline.getTags());
        item.setTerminals(baseline.getTerminals());
        item.setDependencies(baseline.getDependencies());
        // 宿主声明（2026-10-30）随基线透传：公开数据、无站点语义；
        // 站点视角的 hostAvailable 判定由站点列表组装后置（applyHostAvailability），
        // 不能在共用 decorate 里做——平台目录的 items 是给全部站点消费的公开基线，
        // 平台自己的宿主可用性结论混进去会误导缺宿主的站点。
        item.setHostService(baseline.getHostService());
        item.setInstalled(install != null && Integer.valueOf(1).equals(install.getInstalled()));
        item.setInstalledVersion(install != null && Integer.valueOf(1).equals(install.getInstalled())
                ? install.getVersion() : null);
        item.setEnabled(install != null && Integer.valueOf(1).equals(install.getEnabled()));
        // 授权状态：FREE 恒为已授权；PAID / TRIAL 以是否命中有效授权为准
        item.setLicensed(!PluginLicenseService.requiresLicense(baseline.getLicenseType()) || license != null);
        item.setLicenseExpireTime(license == null || license.getExpireTime() == null
                ? null
                : license.getExpireTime().format(TIME_FORMATTER));
        item.setPurchaseStatus(purchaseStatus(baseline.getLicenseType(), license));
        return item;
    }

    private String purchaseStatus(String licenseType, MarketPluginLicense license) {
        if (!PluginLicenseService.requiresLicense(licenseType)) {
            return "FREE";
        }
        return license == null ? "NOT_PURCHASED" : "PURCHASED";
    }

    /**
     * 定价展示文案。
     */
    private String priceText(MarketPlugin plugin) {
        String type = plugin.getLicenseType() == null ? "FREE" : plugin.getLicenseType().toUpperCase(Locale.ROOT);
        return switch (type) {
            case "PAID" -> "¥" + String.format(Locale.ROOT, "%.2f", (plugin.getPriceCents() == null ? 0 : plugin.getPriceCents()) / 100.0);
            case "TRIAL" -> "试用 " + (plugin.getTrialDays() == null ? 0 : plugin.getTrialDays()) + " 天";
            default -> "免费";
        };
    }

    private List<String> parseStringList(String json) {
        if (!StringUtils.hasText(json)) {
            return List.of();
        }
        try {
            return JSON.parseArray(json, String.class);
        } catch (Exception e) {
            return List.of();
        }
    }

    private boolean matchFilters(MarketPluginItem item, Map<String, Object> filters) {
        String keyword = stringValue(filters.get("keyword"));
        if (StringUtils.hasText(keyword)
                && !item.getName().contains(keyword)
                && !item.getCode().contains(keyword)
                && (item.getDescription() == null || !item.getDescription().contains(keyword))) {
            return false;
        }
        String category = stringValue(filters.get("category"));
        if (StringUtils.hasText(category) && !category.equals(item.getCategory())) {
            return false;
        }
        String licenseType = stringValue(filters.get("licenseType"));
        if (StringUtils.hasText(licenseType) && !licenseType.equalsIgnoreCase(item.getLicenseType())) {
            return false;
        }
        return true;
    }

    private String stringValue(Object value) {
        return value == null ? "" : value.toString().trim();
    }
}
