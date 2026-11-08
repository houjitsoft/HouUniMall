package com.houjiang.mall.service.plugin;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.houjiang.mall.entity.market.MarketPlugin;
import com.houjiang.mall.entity.market.MarketPluginLicense;
import com.houjiang.mall.entity.plugin.MallPluginInstall;
import com.houjiang.mall.entity.plugin.AddonPlugin;
import com.houjiang.mall.mapper.market.MarketPluginMapper;
import com.houjiang.mall.mapper.plugin.AddonCateMapper;
import com.houjiang.mall.mapper.plugin.AddonPluginMapper;
import com.houjiang.mall.mapper.plugin.MallPluginInstallMapper;
import com.houjiang.mall.model.plugin.PluginCatalogItem;
import com.houjiang.mall.model.plugin.PluginCenterItem;
import com.houjiang.mall.service.market.PluginLicenseService;
import com.houjiang.mall.service.market.PluginLifecycleSettlementService;
import com.houjiang.mall.service.market.PluginMarketService;
import com.houjiang.plugin.engine.config.PluginEngineProperties;
import com.houjiang.plugin.engine.core.PluginLifecycleManager;
import com.houjiang.plugin.engine.spi.PluginMenuPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 平台插件中心服务。
 * @author Amos
 */
@Slf4j
@Service
public class PluginCenterService {

    /** 未归入 hou_addon_cate 任何分类的插件统一落到该文案（与顶栏「插件应用」口径一致）。 */
    public static final String UNCATEGORIZED_CATEGORY = "未分类";

    /**
     * 走「市场下载 + PF4J 装载」的插件（其余内嵌插件没有可下载制品）。
     *
     * <p>🔴 2026-10-27 修 P1：这份名单原来与 {@code architecture/services.json} 漂移 ——
     * 少了 ocr / lowcode / mall-live / mall-points / mall-distribution /
     * mall-rebate / unilogin / demo-coupon 八个（它们确实是内嵌插件、也有制品）。
     * unipay 不在此列：支付中心是 payment 独立服务，2026-11-02 起不再作为插件安装。
     * 于是点安装会掉进「非市场分支」，只写一行 installed=1 就算装好了，
     * 前端显示已安装但功能根本不存在（假安装）。
     *
     * <p>⚠️ **2026-10-28 口径更正**：上文原写「由 {@code PluginCatalogConsistencyTest} 守着」，
     * 但该测试**在仓库中并不存在**（全仓无此文件），没有任何自动化校验把本名单与
     * {@code architecture/services.json} 对齐 —— 也就是说**漂移风险仍然存在，只能靠人肉同步**。
     * 补 blaming 之前不要相信「改了会被测试拦住」。
     *
     * <p>截至 2026-10-28 实测差异（手工比对，无自动化）：
     * <ul>
     *   <li>{@code activity} —— 有意不合入：**不是插件**，只是「遗留 URL 前缀 → order 服务」的路由映射
     *       （承载方就是 order，不存在 activity 服务 / activity 插件；删它会让 C 端 22 个活动页接口 404）。
     *       将来补自动化校验时必须把这一项排除。</li>
     *   <li>{@code hello-world} —— ⚠️ **疑似遗漏（待确认）**：它在 {@code services.json} 里是 {@code runtime=embedded}
     *       的真实插件，但不在本名单中，点安装会掉进非市场分支（只置 installed=1 的「假安装」）。</li>
     *   <li>本名单多出的 cms/crm/erp/finance/geo/im/logging/mes/oa/orderfood/pm/rider/supplier/webpos 共 14 个 ——
     *       有意不合入：它们属服务型产品黑名单，走独立服务安装，不得作为 PF4J 插件安装。</li>
     * </ul>
     */
    private static final Set<String> MARKET_BACKED_PLUGINS = Set.of(
            "erp", "webpos", "oa", "mes", "finance", "cms",
            "geo", "orderfood", "pm", "crm", "im", "logging", "supplier", "rider", "aicode",
            "mall-advertising", "national-subsidy",
            "order-seckill", "order-voucher", "order-reduction", "order-pintuan", "order-combination",
            "order-activity-hub", "order-marketing", "goods-discount", "userms-redpacket",
            "userms-lucky-draw", "cms-live", "cms-carousel",
            // 下面八个是漏登记的既有内嵌插件（有制品、走市场下载）。unipay 已回归 payment 服务。
            "ocr", "lowcode", "mall-live", "mall-points", "mall-distribution",
            "mall-rebate", "unilogin", "demo-coupon"
        );

        // 下面 12 个 activity 域插件目录项已合并进文件后段的 CATALOG（catalog() 的静态兜底）。
        // ⚠️ 2026-10-29：userms-points-mall 已从名单与 CATALOG 摘除 —— 它与内嵌插件
        // mall-points（真正的积分商品兑换，表 hou_mall_points_tool）同名同描述「积分商城」，
        // 但只有后端 CRUD 骨架（表 activity_points_mall_tool 生产/评估库均不存在）、无前端产物、
        // 无菜单、上线 72h 零调用，属 activity 拆分时的重复产物。仓库源码暂留，不再对外露出。


    // 独立服务编码统一取 StandalonePluginCodes（单一事实来源），不再在本类另存一份。
    private static final Set<String> STANDALONE_SERVICES = StandalonePluginCodes.CODES;

    @Autowired(required=false)
    private org.springframework.jdbc.core.JdbcTemplate serviceInstallJdbc;

    /** Operator-confirmed outages until the owning service is deployed or recovered. */
    @org.springframework.beans.factory.annotation.Value("${houcloud.plugin.unavailable-codes:}")
    private String unavailablePluginCodes = "";

    private final MallPluginInstallMapper pluginInstallMapper;
    private final AddonPluginMapper addonPluginMapper;
    private final AddonCateMapper addonCateMapper;
    private final PluginLifecycleManager lifecycleManager;
    private final PluginMarketService marketService;
    private final AddonPluginSyncService addonPluginSyncService;
    private final MarketPluginMapper marketPluginMapper;
    private final PluginLicenseService licenseService;

    /**
     * Optional direct menu lifecycle hook. The engine invokes this hook for
     * loaded plugins; the center also needs it when an install record exists
     * but the plugin was not loaded after a restart.
     */
    @Autowired(required = false)
    private PluginMenuPort pluginMenuPort;

    /**
     * 商业侧清算（卸载后回收授权与支付凭证）。可选装配：
     * 未装配时卸载行为与改动前完全一致，单测直接 new 出来的场景就是这个状态。
     */
    @Autowired(required = false)
    private PluginLifecycleSettlementService lifecycleSettlementService;

    /** 插件 JAR 落盘目录，用于定位 plugin.yaml 做菜单兜底注册。 */
    @Autowired(required = false)
    private PluginEngineProperties pluginEngineProperties;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter FRONTEND_REVISION_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    public PluginCenterService(MallPluginInstallMapper pluginInstallMapper) {
        this(pluginInstallMapper, null, null, null, null, null, null, null);
    }

    public PluginCenterService(MallPluginInstallMapper pluginInstallMapper,
                               AddonPluginMapper addonPluginMapper,
                               PluginLifecycleManager lifecycleManager,
                               PluginMarketService marketService,
                               AddonPluginSyncService addonPluginSyncService) {
        this(pluginInstallMapper, addonPluginMapper, lifecycleManager, marketService,
                addonPluginSyncService, null, null, null);
    }

    /**
     * 无分类表读取能力的装配形式（保留：既有单测与「只关心安装态」的调用）。
     */
    public PluginCenterService(MallPluginInstallMapper pluginInstallMapper,
                               AddonPluginMapper addonPluginMapper,
                               PluginLifecycleManager lifecycleManager,
                               PluginMarketService marketService,
                               AddonPluginSyncService addonPluginSyncService,
                               MarketPluginMapper marketPluginMapper,
                               PluginLicenseService licenseService) {
        this(pluginInstallMapper, addonPluginMapper, lifecycleManager, marketService,
                addonPluginSyncService, marketPluginMapper, licenseService, null);
    }

    /**
     * Spring 注入入口。其余构造器只保留给既有单测，因此不再标注 {@code @Autowired}，
     * 避免多构造器注入的歧义。
     */
    @Autowired
    public PluginCenterService(MallPluginInstallMapper pluginInstallMapper,
                               AddonPluginMapper addonPluginMapper,
                               PluginLifecycleManager lifecycleManager,
                               PluginMarketService marketService,
                               AddonPluginSyncService addonPluginSyncService,
                               MarketPluginMapper marketPluginMapper,
                               PluginLicenseService licenseService,
                               AddonCateMapper addonCateMapper) {
        this.pluginInstallMapper = pluginInstallMapper;
        this.addonPluginMapper = addonPluginMapper;
        this.lifecycleManager = lifecycleManager;
        this.marketService = marketService;
        this.addonPluginSyncService = addonPluginSyncService;
        this.marketPluginMapper = marketPluginMapper;
        this.licenseService = licenseService;
        this.addonCateMapper = addonCateMapper;
    }

    /** 原样返回 base 拥有的分类字典，顺序由 sort/id 决定。 */
    public List<Map<String, Object>> listCategories() {
        if (addonCateMapper == null) throw new IllegalStateException("分类目录不可用");
        return nullSafe(addonCateMapper.selectAll());
    }

    /** 插件目录与市场分类在 base 的同一事务中更新。0 表示未分类。 */
    @Transactional
    public void updateCategory(String pluginCode, int cateId) {
        if (!StringUtils.hasText(pluginCode) || !pluginCode.matches("[A-Za-z0-9][A-Za-z0-9_-]*") || cateId < 0) {
            throw new IllegalArgumentException("插件编码或分类ID不合法");
        }
        if (addonCateMapper == null) throw new IllegalStateException("分类目录不可用");
        String category = "";
        if (cateId > 0) {
            category = listCategories().stream().filter(row -> toInt(row.get("id")) == cateId)
                    .map(row -> String.valueOf(row.get("name"))).findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("插件分类不存在: " + cateId));
        }
        int updated = addonCateMapper.updateCatalogCategory(pluginCode, cateId, category);
        if (updated == 0 && addonCateMapper.countCatalogPlugin(pluginCode) == 0) {
            throw new IllegalArgumentException("插件目录不存在: " + pluginCode);
        }
        addonCateMapper.updateMarketCategory(pluginCode, cateId, category);
    }

    /** 插件前端产物统一前缀（BFF 泛化转发入口，转发规则见 T11） */
    private static final String FRONTEND_ASSET_PREFIX = "/web/plugin-assets";

    private static final List<PluginCatalogItem> CATALOG = List.of(
            new PluginCatalogItem(
                    "erp",
                    "ERP 企业资源计划",
                    "ERP",
                    "1.0.0",
                    "商品、销售、库存、采购、生产与财务一体化管理",
                    List.of("平台端"),
                    List.of("mall"),
                    "index.mjs",
                    "{}"
            ),
            new PluginCatalogItem(
                    "webpos",
                    "WebPOS",
                    "商城",
                    "1.0.0",
                    "门店收银、称重结算与收银台操作",
                    List.of("平台端", "收银端"),
                    List.of(),
                    "index.mjs",
                    "{}"
            ),
            new PluginCatalogItem(
                    "oa",
                    "OA",
                    "OA办公",
                    "1.0.0",
                    "合同、考勤与人事等办公自动化管理",
                    List.of("平台端"),
                    List.of(),
                    "index.mjs",
                    "{}"
            ),
            new PluginCatalogItem(
                    "mes",
                    "MES",
                    "生产",
                    "1.0.0",
                    "制造执行与生产运营管理",
                    List.of("平台端"),
                    List.of(),
                    "index.mjs",
                    "{}"
            ),
            new PluginCatalogItem(
                    "finance",
                    "财务中心",
                    "财务",
                    "1.0.0",
                    "总账、凭证、应收应付与财务报表",
                    List.of("平台端"),
                    List.of(),
                    "index.mjs",
                    "{}"
            ),
            new PluginCatalogItem(
                    "geo",
                    "GEO",
                    "",
                    "1.0.0",
                    "生成式引擎优化与内容智能",
                    List.of("平台端"),
                    List.of(),
                    "index.mjs",
                    "{}"
            ),
            new PluginCatalogItem(
                    "orderfood",
                    "餐饮管理",
                    "商城",
                    "1.0.0",
                    "餐厅点餐与外卖配送运营",
                    List.of("平台端"),
                    List.of(),
                    "index.mjs",
                    "{}"
            ),
            new PluginCatalogItem(
                    "pm",
                    "项目管理",
                    "项目管理",
                    "1.0.0",
                    "项目、任务、测试与交付流程管理",
                    List.of("平台端"),
                    List.of(),
                    "index.mjs",
                    "{}"
            ),
            new PluginCatalogItem(
                    "activity",
                    "营销活动",
                    "营销",
                    "1.0.0",
                    "优惠券、活动、促销与营销管理",
                    List.of("平台端", "商家端", "移动端"),
                    List.of(),
                    "index.mjs",
                    "{}"
            ),
            new PluginCatalogItem(
                    "mall-advertising",
                    "广告管理",
                    "营销与客户",
                    "1.0.0",
                    "平台广告位管理、商家投放申请与商城登录页广告轮播",
                    List.of("平台端", "商家端", "PC端"),
                    List.of(),
                    "index.mjs",
                    "{}"
            ),
            new PluginCatalogItem(
                    "national-subsidy",
                    "order",
                    "国家补贴",
                    "营销与客户",
                    null,
                    "1.0.0",
                    "国家补贴（国补/以旧换新）规则配置、资格校验、算价与逆向回收",
                    List.of("平台端", "PC端"),
                    List.of(),
                    "index.mjs",
                    "{}"
            ),
            new PluginCatalogItem(
                    "cms",
                    "CMS",
                    "",
                    "1.0.0",
                    "站点、渠道、文章与内容运营",
                    List.of("平台端"),
                    List.of(),
                    "index.mjs",
                    "{}"
            ),
            new PluginCatalogItem(
                    "crm",
                    "CRM",
                    "CRM",
                    "1.0.0",
                    "客户、联系人、商机与服务工单",
                    List.of("平台端"),
                    List.of(),
                    "index.mjs",
                    "{}"
            ),
            new PluginCatalogItem(
                    "im",
                    "IM",
                    "",
                    "1.0.0",
                    "实时客服、会话与工单处理",
                    List.of("平台端"),
                    List.of(),
                    "index.mjs",
                    "{}"
            ),
            new PluginCatalogItem(
                    "logging",
                    "日志管理",
                    "",
                    "1.0.0",
                    "运行、访问、业务与 ELK 日志管理",
                    List.of("平台端"),
                    List.of(),
                    "index.mjs",
                    "{}"
            ),
            new PluginCatalogItem(
                    "supplier",
                    "供应商管理",
                    "",
                    "1.0.0",
                    "供应商、采购寻源、考核与协同",
                    List.of("平台端"),
                    List.of(),
                    "index.mjs",
                    "{}"
            ),
            new PluginCatalogItem(
                    "aicode",
                    "AI 开发",
                    "",
                    "1.0.0",
                    "AI 辅助二次开发工作台",
                    List.of("平台端"),
                    List.of(),
                    "index.mjs",
                    "{}"
            ),
            new PluginCatalogItem(
                    "mall-points",
                    "积分商城",
                    "营销",
                    "1.0.0",
                    "基于现有积分工具能力，支持积分商品与积分兑换场景。",
                    List.of("平台端", "移动端", "PC端"),
                    List.of("积分工具", "会员中心"),
                    "/points/index",
                    "{\"exchangeLimit\":{\"type\":\"number\",\"label\":\"每人限兑数量\"},\"autoPublish\":{\"type\":\"boolean\",\"label\":\"自动上架\"}}"
            ),
            new PluginCatalogItem(
                    "mall-live",
                    "直播带货",
                    "营销",
                    "1.0.0",
                    "整合直播账号、直播活动和直播商品审核能力。",
                    List.of("平台端", "移动端"),
                    List.of("直播账号", "商品管理"),
                    "/live/activity",
                    "{\"autoAudit\":{\"type\":\"boolean\",\"label\":\"商品自动审核\"},\"noticeEnabled\":{\"type\":\"boolean\",\"label\":\"开播提醒\"}}"
            ),
            new PluginCatalogItem(
                    "mall-gather",
                    "商品采集",
                    "商城",
                    "1.0.0",
                    "对接商品采集配置、采集商品和采集费用管理。",
                    List.of("平台端", "商家端"),
                    List.of("商品管理", "采集配置"),
                    "/application/gather/set",
                    "{\"dailyLimit\":{\"type\":\"number\",\"label\":\"每日采集上限\"},\"auditRequired\":{\"type\":\"boolean\",\"label\":\"采集后需审核\"}}"
            ),
            new PluginCatalogItem(
                    "mall-distribution",
                    "分销推广",
                    "营销",
                    "1.0.0",
                    "对接分销商、分销商品、分销订单和佣金结算能力。",
                    List.of("平台端", "商家端", "移动端", "PC端"),
                    List.of("分销体系", "财务中心"),
                    "/distribute/overView",
                    "{\"defaultRate\":{\"type\":\"number\",\"label\":\"默认佣金比例\"},\"autoSettle\":{\"type\":\"boolean\",\"label\":\"自动结算\"}}"
            ),
            new PluginCatalogItem(
                    "mall-risk",
                    "商城风控",
                    "合规",
                    "1.0.0",
                    "整合商城风控规则、监控和告警处理。",
                    List.of("平台端"),
                    List.of("风控规则", "支付风控"),
                    "/risk/alert",
                    "{\"alertThreshold\":{\"type\":\"number\",\"label\":\"告警阈值\"},\"notifyAdmin\":{\"type\":\"boolean\",\"label\":\"通知管理员\"}}"
            ),
            new PluginCatalogItem(
                    "mall-rebate",
                    "返利结算",
                    "财务",
                    "1.0.0",
                    "整合返利规则、返利记录、返利付款和调整审核。",
                    List.of("平台端"),
                    List.of("财务中心", "返利管理"),
                    "/rebate/record",
                    "{\"settleCycle\":{\"type\":\"string\",\"label\":\"结算周期\"},\"auditRequired\":{\"type\":\"boolean\",\"label\":\"付款需审核\"}}"
            ),
                new PluginCatalogItem(
                    "order-seckill",
                    "order",
                    "限时秒杀",
                    "营销与客户",
                    null,
                    "1.0.0",
                    "秒杀活动：场次、秒杀商品、限购与库存扣减",
                    List.of("平台端", "移动端"),
                    List.of(),
                    "index.mjs",
                    "{}"
            ),
            new PluginCatalogItem(
                    "order-voucher",
                    "order",
                    "优惠券",
                    "营销与客户",
                    null,
                    "1.0.0",
                    "代金券模板、发放规则与核销",
                    List.of("平台端", "移动端"),
                    List.of(),
                    "index.mjs",
                    "{}"
            ),
            new PluginCatalogItem(
                    "order-reduction",
                    "order",
                    "满减",
                    "营销与客户",
                    null,
                    "1.0.0",
                    "满减活动：门槛、优惠与叠加规则",
                    List.of("平台端", "移动端"),
                    List.of(),
                    "index.mjs",
                    "{}"
            ),
            new PluginCatalogItem(
                    "order-pintuan",
                    "order",
                    "拼团",
                    "营销与客户",
                    null,
                    "1.0.0",
                    "拼团活动：开团、参团、成团与团长优惠",
                    List.of("平台端", "移动端"),
                    List.of(),
                    "index.mjs",
                    "{}"
            ),
            new PluginCatalogItem(
                    "order-combination",
                    "order",
                    "组合促销",
                    "营销与客户",
                    null,
                    "1.0.0",
                    "组合促销：多商品打包与组合价",
                    List.of("平台端", "移动端"),
                    List.of(),
                    "index.mjs",
                    "{}"
            ),
            new PluginCatalogItem(
                    "order-activity-hub",
                    "order",
                    "活动管理中枢",
                    "营销与客户",
                    null,
                    "1.0.0",
                    "活动统一管理入口：列表、统计与操作编排",
                    List.of("平台端", "移动端"),
                    List.of(),
                    "index.mjs",
                    "{}"
            ),
            new PluginCatalogItem(
                    "order-marketing",
                    "order",
                    "营销总览",
                    "营销与客户",
                    null,
                    "1.0.0",
                    "营销活动总览看板",
                    List.of("平台端", "移动端"),
                    List.of(),
                    "index.mjs",
                    "{}"
            ),
            new PluginCatalogItem(
                    "goods-discount",
                    "goods",
                    "商品折扣",
                    "营销与客户",
                    null,
                    "1.0.0",
                    "限时折扣：折扣价、上限与限购校验",
                    List.of("平台端", "移动端"),
                    List.of(),
                    "index.mjs",
                    "{}"
            ),
            new PluginCatalogItem(
                    "userms-redpacket",
                    "userms",
                    "红包",
                    "营销与客户",
                    null,
                    "1.0.0",
                    "红包模板、发放与状态流转",
                    List.of("平台端", "移动端"),
                    List.of(),
                    "index.mjs",
                    "{}"
            ),
            new PluginCatalogItem(
                    "userms-lucky-draw",
                    "userms",
                    "抽奖转盘",
                    "营销与客户",
                    null,
                    "1.0.0",
                    "幸运转盘：奖品、抽奖记录与中奖发奖",
                    List.of("平台端", "移动端"),
                    List.of(),
                    "index.mjs",
                    "{}"
            ),
            new PluginCatalogItem(
                    "cms-live",
                    "cms",
                    "直播带货",
                    "营销与客户",
                    null,
                    "1.0.0",
                    "直播间、弹幕与直播商品审核",
                    List.of("平台端", "移动端"),
                    List.of(),
                    "index.mjs",
                    "{}"
            ),
            new PluginCatalogItem(
                    "cms-carousel",
                    "cms",
                    "轮播图",
                    "营销与客户",
                    null,
                    "1.0.0",
                    "商城轮播位与轮播商品管理",
                    List.of("平台端", "移动端"),
                    List.of(),
                    "index.mjs",
                    "{}"
            ),
            new PluginCatalogItem(
                    "ocr",
                    "tool",
                    "OCR 识别",
                    "AI 工具",
                    null,
                    "1.0.0",
                    "OCR 识别行为统计小工具（ocr.houjit.com 对外识别服务的后端）：记录登录时间、识别使用的文件列表、识别内容、来源 IP",
                    List.of("平台端"),
                    List.of(),
                    "index.mjs",
                    "{}"
            )
    );

    public List<PluginCatalogItem> catalog() {
        if (addonPluginMapper != null) {
            // 一次查全（@TableLogic 会自动追加 deleted=0），再按 status 分流。
            // 注意：不能靠查 deleted=1 判断「已下架」——@TableLogic 会把它过滤掉，永远查不到。
            List<AddonPlugin> rows = addonPluginMapper.selectList(new LambdaQueryWrapper<AddonPlugin>()
                    .orderByAsc(AddonPlugin::getSort, AddonPlugin::getId));
            // 目录里显式停用（status=0）的插件：静态目录不得把它补回来。
            // buildItems 是以 catalog() 为准遍历的，install 表只补充状态位，
            // 因此「停用」若不在目录层生效，面板会继续显示点不开的卡片
            // （2026-09-20 mall-risk / mall-gather 即如此）。
            java.util.Set<String> retired = rows.stream()
                    .filter(row -> !Integer.valueOf(1).equals(row.getStatus()))
                    .map(AddonPlugin::getPluginCode)
                    .collect(java.util.stream.Collectors.toSet());
            List<PluginCatalogItem> fromDb = rows.stream()
                    .filter(row -> Integer.valueOf(1).equals(row.getStatus()))
                    .map(this::toCatalogItem)
                    .toList();
            // Database rows override static metadata, while static plugins fill catalog gaps
            // during migration before every plugin.yaml has been synchronized.
            if (!fromDb.isEmpty()) {
                Map<String, PluginCatalogItem> merged = new LinkedHashMap<>();
                fromDb.forEach(item -> merged.put(item.getCode(), item));
                CATALOG.forEach(item -> merged.putIfAbsent(item.getCode(), item));
                retired.forEach(merged.keySet()::remove);
                return List.copyOf(merged.values());
            }
        }
        return CATALOG;
    }

    public PluginCatalogItem requireCatalogItem(String pluginCode) {
        return catalog().stream()
                .filter(item -> item.getCode().equals(pluginCode))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("插件不存在: " + pluginCode));
    }

    public List<PluginCenterItem> listPlugins(Map<String, Object> filters) {
        // 下架语义由 @TableLogic 承担：两张表都是 deleted=0 可见，下架即 deleted=1，
        // MyBatis-Plus 会自动追加该条件，这里不需要再手写过滤。
        String instance=com.houjiang.commons.util.SiteInstanceResolver.resolveOrNull();
        List<MallPluginInstall> installs = serviceInstallations(pluginInstallMapper.selectList(null),instance);
        Map<String, Object> safeFilters = filters == null ? Map.of() : filters;
        return buildItems(installs, safeFilters, marketMap(), licenseMap(instance));
    }

    /**
     * 插件列表 + 可选分类。
     *
     * <p>分类取自 hou_addon_cate（与顶栏「插件应用」同源），目录自带分类作为兜底补充，
     * 见 {@link #categories(List)}。
     */
    public Map<String, Object> listPluginsWithCategories(Map<String, Object> filters) {
        List<PluginCenterItem> items = listPlugins(filters);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", items);
        result.put("categories", categories(items));
        return result;
    }

    /** 全站可用分类（不含插件过滤条件）。 */
    public List<String> categories() {
        return categories(listPlugins(Map.of()));
    }

    /**
     * 分类下拉：<b>唯一数据源是 hou_addon_cate</b>（id 升序），与顶栏「插件应用」完全同源。
     *
     * <p>存在未归入任何分类的插件时，末尾补一个「未分类」项（可筛选项，与顶栏一致）。
     * 分类表缺失 / 查询异常 / 表为空时才退化为「从插件目录分类推导」（改动前行为），
     * 只记一条 warn，不让分类问题连带打挂插件列表。
     */
    private List<String> categories(List<PluginCenterItem> items) {
        Set<String> result = new LinkedHashSet<>();
        CategoryContext context = loadCategoryContext();
        boolean fromTable = !context.cateNames().isEmpty();
        if (fromTable) {
            for (String name : context.cateNames().values()) {
                if (StringUtils.hasText(name)) {
                    result.add(name.trim());
                }
            }
        } else {
            log.warn("[PluginCenter] hou_addon_cate 无可用分类记录，退化使用插件目录自带分类推导");
        }
        boolean hasUncategorized = false;
        for (PluginCenterItem item : items) {
            String resolved = StringUtils.hasText(item.getCategory()) ? item.getCategory().trim() : null;
            if (!StringUtils.hasText(resolved)) {
                hasUncategorized = true;
                continue;
            }
            if (!fromTable) {
                // 仅在分类表不可用时兜底展示目录自带分类，避免插件被筛不出来
                result.add(resolved);
            }
        }
        if (fromTable && hasUncategorized) {
            result.add(UNCATEGORIZED_CATEGORY);
        }
        return List.copyOf(result);
    }

    /**
     * 分类上下文：hou_addon_cate 快照 + 插件位菜单的 cate_id 关联。
     *
     * @param cateNames    分类 ID → 名称（id 升序）
     * @param pluginCateId 插件编码 → 分类 ID
     */
    private record CategoryContext(Map<Integer, String> cateNames, Map<String, Integer> pluginCateId) {
    }

    /** 读取分类上下文；任何异常都退化为「无分类表」，由调用方继续兜底。 */
    private CategoryContext loadCategoryContext() {
        if (addonCateMapper == null) {
            return new CategoryContext(Map.of(), Map.of());
        }
        Map<Integer, String> cateNames = new LinkedHashMap<>();
        try {
            for (Map<String, Object> row : nullSafe(addonCateMapper.selectAll())) {
                Object id = row.get("id");
                Object name = row.get("name");
                if (id == null || !StringUtils.hasText(String.valueOf(name))) {
                    continue;
                }
                cateNames.put(toInt(id), String.valueOf(name).trim());
            }
        } catch (Exception e) {
            log.warn("[PluginCenter] 读取 hou_addon_cate 失败，分类退化为插件自带分类: {}", e.getMessage());
            return new CategoryContext(Map.of(), Map.of());
        }
        // 保留 id 升序：Map.copyOf 不保证迭代顺序，分类下拉的顺序会随哈希漂
        return new CategoryContext(Collections.unmodifiableMap(cateNames), pluginCateIdMap());
    }

    /**
     * 插件编码 → 分类 ID。
     *
     * <p>权威来源是 <b>hou_addon_plugin.cate_id</b>（2026-09-18 起的外键列，随 catalog 一起
     * 由 MyBatis-Plus 带出，因此这里不再额外查表）。插件位菜单（hou_permission_menu.cate_id）
     * 降级为<b>兼容回落</b>：未加列、或加列后尚未回填的实例，仍靠它维持迁移前的行为。
     */
    private Map<String, Integer> pluginCateIdMap() {
        Map<String, Integer> mapping = new LinkedHashMap<>();
        List<String> pendingPaths = new ArrayList<>();
        for (PluginCatalogItem item : catalog()) {
            String code = item.getCode();
            if (!StringUtils.hasText(code)) {
                continue;
            }
            int declared = toInt(item.getCateId());
            if (declared > 0) {
                mapping.put(code, declared);
            } else {
                pendingPaths.add("/plugin/" + code);
            }
        }
        if (pendingPaths.isEmpty() || addonCateMapper == null) {
            return Collections.unmodifiableMap(mapping);
        }
        try {
            for (Map<String, Object> row : nullSafe(addonCateMapper.selectPluginCateIds(pendingPaths))) {
                Object rawPath = row.get("paths");
                Object rawCateId = row.get("cate_id");
                int cateId = toInt(rawCateId);
                if (!(rawPath instanceof String path) || cateId <= 0 || !path.startsWith("/plugin/")) {
                    continue;
                }
                // putIfAbsent：只补 hou_addon_plugin.cate_id 为空的插件，菜单不得覆盖已有外键
                mapping.putIfAbsent(path.substring("/plugin/".length()), cateId);
            }
        } catch (Exception e) {
            log.warn("[PluginCenter] 读取插件分类关联失败，仅保留 hou_addon_plugin.cate_id 维度: {}", e.getMessage());
        }
        return Collections.unmodifiableMap(mapping);
    }

    /**
     * 解析插件最终展示的分类（<b>只允许 hou_addon_cate 里的分类</b>）。
     *
     * <ol>
     *   <li>hou_addon_plugin.cate_id（未回填时回落插件位一级菜单的 cate_id）优先 ——
     *       与顶栏「插件应用」的分组口径必须一致；</li>
     *   <li>其次认目录自带分类，但仅当它与分类表同名（忽略大小写与首尾空格）时采用，
     *       并统一成表里的写法；</li>
     *   <li>目录分类未登记到分类表时返回 null（前端与筛选项统一显示为「未分类」）。</li>
     * </ol>
     *
     * <p>分类表不可用（缺失/为空）时退化为原样返回目录分类，避免分类故障放大成列表故障。
     */
    private String resolveCategory(String code, String declared, CategoryContext context) {
        Integer menuCateId = context.pluginCateId().get(code);
        if (menuCateId != null && StringUtils.hasText(context.cateNames().get(menuCateId))) {
            return context.cateNames().get(menuCateId);
        }
        if (!StringUtils.hasText(declared)) {
            return null;
        }
        String trimmed = declared.trim();
        for (String name : context.cateNames().values()) {
            if (name != null && name.equalsIgnoreCase(trimmed)) {
                return name;
            }
        }
        return context.cateNames().isEmpty() ? trimmed : null;
    }

    /** 插件所属分类 ID；未关联到分类返回 null。 */
    private Integer resolveCateId(String code, CategoryContext context) {
        Integer cateId = context.pluginCateId().get(code);
        return cateId != null && context.cateNames().containsKey(cateId) ? cateId : null;
    }

    /**
     * 目录全量插件的规范分类（code → hou_addon_cate 名称；未归类为空串）。
     *
     * <p>给启动对账器（PluginRegistryReconciler）回写 hou_addon_plugin.category 用，
     * 保证目录表里的分类文案与分类表、顶栏分组三处始终一致。
     */
    public Map<String, String> canonicalCategories() {
        CategoryContext context = loadCategoryContext();
        Map<String, String> result = new LinkedHashMap<>();
        for (PluginCatalogItem item : catalog()) {
            String resolved = resolveCategory(item.getCode(), item.getCategory(), context);
            result.put(item.getCode(), resolved == null ? "" : resolved);
        }
        return result;
    }

    public PluginCenterItem detail(String pluginCode) {
        return detail(pluginCode, null);
    }

    /** 插件详情：按站点实例合并有效授权状态。 */
    public PluginCenterItem detail(String pluginCode, String instanceId) {
        requireCatalogItem(pluginCode);
        MallPluginInstall install = findInstall(pluginCode);
        return buildItems(serviceInstallations(install == null ? List.of() : List.of(install),instanceId), Map.of("code", pluginCode),
                marketMap(), licenseMap(instanceId)).get(0);
    }

    /**
     * 合并安装表与容器安装回执。
     *
     * <p>独立服务的权威运行态来自 {@code hou_service_installation}；但若某编码尚无回执
     * （例如站点仍以 Compose/历史登记方式装配），必须保留 {@code hou_addon_plugin_install}
     * 的已安装记录，否则右上角应用面板会把「已装且有权限」的独立服务全部滤掉。
     * 有回执时以回执为准（含 status=0 的停用）。
     */
    private List<MallPluginInstall> serviceInstallations(List<MallPluginInstall> legacy, String instance) {
        Map<String, MallPluginInstall> byCode = new LinkedHashMap<>();
        if (legacy != null) {
            for (MallPluginInstall row : legacy) {
                if (row == null || !StringUtils.hasText(row.getPluginCode())) {
                    continue;
                }
                byCode.put(row.getPluginCode(), row);
            }
        }
        if (serviceInstallJdbc == null || !StringUtils.hasText(instance)) {
            return new ArrayList<>(byCode.values());
        }
        for (var row : serviceInstallJdbc.queryForList(
                "SELECT plugin_code,version,status,updated_at FROM hou_service_installation WHERE instance_id=?",
                instance)) {
            String code = String.valueOf(row.get("plugin_code"));
            if (!StringUtils.hasText(code) || "null".equals(code)) {
                continue;
            }
            MallPluginInstall install = new MallPluginInstall();
            install.setPluginCode(code);
            Object version = row.get("version");
            if (version != null) {
                install.setVersion(String.valueOf(version));
            }
            boolean running = row.get("status") instanceof Number status && status.intValue() == 1;
            install.setInstalled(running ? 1 : 0);
            install.setEnabled(running ? 1 : 0);
            // 前端产物修订号：service 安装记录会覆盖 legacy 记录，若不带上 updateTime，
            // resolveFrontendRevision 会返回 null → frontendUrl 不带 ?rev= 指纹 →
            // 浏览器对 immutable 的 /web/plugin-assets/** 缓存一年 → 发版后用户仍跑旧包
            // （2026-10-20 的 GEO 改版不生效即此因）。这里用 updated_at 作为回退指纹。
            Object updatedAt = row.get("updated_at");
            LocalDateTime revisionTime = toLocalDateTime(updatedAt);
            if (revisionTime != null) {
                install.setUpdateTime(revisionTime);
            }
            byCode.put(code, install);
        }
        return new ArrayList<>(byCode.values());
    }

    /**
     * 独立服务的运行态：以容器安装回执为准（{@code hou_service_installation} 的 status=1）。
     *
     * <p>独立服务不跑 PF4J，健康与否由安装代理写入的回执承载；没有回执就没有运行态证据，
     * 此时返回 false（未知不猜成健康）。
     */
    public boolean isServiceRunning(String pluginCode) {
        if (serviceInstallJdbc == null || !StringUtils.hasText(pluginCode)) {
            return false;
        }
        try {
            List<Map<String, Object>> rows = serviceInstallJdbc.queryForList(
                    "SELECT status FROM hou_service_installation WHERE plugin_code=?",
                    pluginCode.trim());
            return rows.stream().anyMatch(row -> row.get("status") instanceof Number status
                    && status.intValue() == 1);
        } catch (RuntimeException ex) {
            log.warn("读取独立服务安装回执失败: {} - {}", pluginCode, ex.getMessage());
            return false;
        }
    }

    /** JDBC 时间列（Timestamp/LocalDateTime/字符串）统一转 LocalDateTime；无法解析返回 null。 */
    private static LocalDateTime toLocalDateTime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDateTime dateTime) {
            return dateTime;
        }
        if (value instanceof java.sql.Timestamp timestamp) {
            return timestamp.toLocalDateTime();
        }
        if (value instanceof java.util.Date date) {
            return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        }
        if (value instanceof Number number) {
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(number.longValue()), ZoneId.systemDefault());
        }
        String text = String.valueOf(value).trim();
        if (text.isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(text.replace(' ', 'T'));
        } catch (Exception ignored) {
            return null;
        }
    }

    public Map<String, Object> summary() {
        List<PluginCenterItem> items = listPlugins(Map.of());
        long installed = items.stream().filter(PluginCenterItem::getInstalled).count();
        long enabled = items.stream().filter(PluginCenterItem::getEnabled).count();
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("total", items.size());
        summary.put("installed", installed);
        summary.put("enabled", enabled);
        summary.put("available", items.size() - installed);
        return summary;
    }

    public List<PluginCenterItem> buildItems(List<MallPluginInstall> installs, Map<String, Object> filters) {
        return buildItems(installs, filters, marketMap(), licenseMap(null));
    }

    private List<PluginCenterItem> buildItems(List<MallPluginInstall> installs,
                                              Map<String, Object> filters,
                                              Map<String, MarketPlugin> marketMap,
                                              Map<String, MarketPluginLicense> licenseMap) {
        Map<String, MallPluginInstall> installMap = installs == null ? Map.of() : installs.stream()
                .filter(item -> item.getPluginCode() != null)
                .collect(Collectors.toMap(MallPluginInstall::getPluginCode, Function.identity(), (left, right) -> left));

        CategoryContext categoryContext = loadCategoryContext();
        Set<String> unavailable = java.util.Arrays.stream(unavailablePluginCodes.split(","))
                .map(String::trim).filter(code -> !code.isEmpty()).collect(Collectors.toSet());
        return catalog().stream()
                .map(catalogItem -> toCenterItem(catalogItem, installMap.get(catalogItem.getCode()),
                        marketMap.get(catalogItem.getCode()), licenseMap.get(catalogItem.getCode()),
                        categoryContext))
                .peek(item -> { if (unavailable.contains(item.getCode())) item.setHealthStatus("UNAVAILABLE"); })
                .filter(item -> matchFilters(item, filters == null ? Map.of() : filters))
                .collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean install(String pluginCode) {
        return install(pluginCode, null);
    }

    /** 安装插件：面向站点的调用应传入当前实例标识。 */
    @Transactional(rollbackFor = Exception.class)
    public boolean install(String pluginCode, String instanceId) {
        PluginCatalogItem catalogItem = requireCatalogItem(pluginCode);
        if (STANDALONE_SERVICES.contains(pluginCode)) {
            throw new IllegalStateException("服务型产品必须从 /base/plugin-center/service/install 以独立容器启动");
        }
        // Embedded extensions use the market download, validation and PF4J lifecycle.
        if (MARKET_BACKED_PLUGINS.contains(pluginCode) && marketService != null) {
            marketService.installFromMarket(pluginCode, catalogItem.getVersion(), "LIGHTWEIGHT", instanceId);
            ensureMenusRegistered(pluginCode);
            return true;
        }
        // 🔴 2026-10-27：非市场分支以前直接写 installed=1，只要插件「不在市场名单里」
        // 就能绕开付费授权校验（审计 P2）。市场里存在、但这套名单没收录的付费插件
        // 正是最容易漏掉的一类，所以这里同样过一遍授权门（免费/非市场插件是 no-op）。
        assertEnableLicensed(pluginCode, instanceId);
        // 🔴 2026-10-27：走到这里说明该插件「没有可下载制品」。以前直接写 installed=1，
        // 前端就显示「已安装」，实际什么都没装载 —— 靠 30 分钟心跳兜底掩盖问题。
        // 现在先确认引擎里真的装载了它，否则明确拒绝，让调用方去走正确入口。
        if (lifecycleManager != null && !lifecycleManager.isLoaded(pluginCode)) {
            throw new IllegalStateException("该插件没有可下载的安装制品，且引擎未装载它，无法标记为已安装: "
                    + pluginCode + "。内嵌插件请从市场安装（/market/install）；"
                    + "服务型插件请走 /base/plugin-center/service/install");
        }
        MallPluginInstall install = findInstall(pluginCode);
        LocalDateTime now = LocalDateTime.now();
        if (install == null) {
            install = new MallPluginInstall();
            install.setPluginCode(pluginCode);
            install.setInstalled(1);
            install.setEnabled(1);
            install.setVersion(catalogItem.getVersion());
            install.setConfigJson("{}");
            install.setInstallTime(now);
            install.setUpdateTime(now);
            install.setDeleted(0);
            boolean inserted = pluginInstallMapper.insert(install) > 0;
            ensureMenusRegistered(pluginCode);
            return inserted;
        }
        install.setInstalled(1);
        install.setEnabled(1);
        install.setVersion(catalogItem.getVersion());
        install.setUpdateTime(now);
        boolean updated = pluginInstallMapper.updateById(install) > 0;
        ensureMenusRegistered(pluginCode);
        return updated;
    }

    /**
     * 安装收尾保证菜单权限已注册，与卸载时的 {@link PluginMenuPort#remove(String)} 成对。
     *
     * <p>只依据「插件声明 → 菜单权限」的写入来管理，不涉及任何名单。
     * 市场插件在引擎装载时已经同步过菜单（isLoaded 为真），这里直接返回、不重复同步；
     * 其余情况（不走市场的插件、或重启后插件尚未加载）按插件目录下的 plugin.yaml
     * 兜底注册，保证「装了就有菜单」。找不到声明文件只告警，不阻塞安装。
     */
    private void ensureMenusRegistered(String pluginCode) {
        if (pluginMenuPort == null) {
            return;
        }
        if (lifecycleManager != null && lifecycleManager.isLoaded(pluginCode)) {
            return;
        }
        Path declaration = resolvePluginDeclaration(pluginCode);
        if (declaration == null) {
            log.warn("插件 {} 未找到 plugin.yaml，跳过菜单兜底注册（安装记录已写入）", pluginCode);
            return;
        }
        try {
            pluginMenuPort.sync(pluginCode, declaration);
        } catch (RuntimeException e) {
            log.warn("插件 {} 菜单注册失败，安装记录仍视为成功：{}", pluginCode, e.getMessage());
        }
    }

    /** 插件产物根目录（与 {@code PluginAssetController} 同一配置键）。 */
    private Path pluginsRoot() {
        String dir = pluginEngineProperties == null || !StringUtils.hasText(pluginEngineProperties.getPluginsDir())
                ? "plugins" : pluginEngineProperties.getPluginsDir();
        return Path.of(dir).toAbsolutePath().normalize();
    }

    /**
     * 前端产物是否真的落在宿主磁盘上（目录形态 {@code <code>/<version>/frontend/}，
     * 或市场安装落盘的 JAR 形态 {@code <code>-<version>.jar!/frontend/}）。
     *
     * <p>插件目录里的 {@code entryPath} 只表达「这个插件会有前端页面」，不等于产物已交付。
     * 13 个 activity 拆分插件（秒杀 / 拼团 / 满减 / 限购 / 商品折扣 / 抽奖 / 红包 / 积分商城 /
     * 直播 / 轮播 …）目前只有后端接口、没有前端产物；若照样下发 {@code frontendUrl}，
     * 后台每次加载都会去 import 一个 404 的 index.mjs，控制台刷一片
     * {@code Failed to fetch dynamically imported module}。
     *
     * <p>产物端点（{@link com.houjiang.mall.controller.plugin.PluginAssetController}）
     * 本来就以「插件产物不存在」回 404，这里提前与它对同一事实：
     * 「已安装但前端还没做出来」的插件安静地不注册路由，而不是每次开页面都报错。
     * 产物一旦落盘（补发前端 / 市场安装解包），下一次取列表就会自动带出 frontendUrl。
     */
    boolean frontendArtifactOnDisk(String code, String version, String entryPath) {
        if (!StringUtils.hasText(code) || !StringUtils.hasText(version) || !StringUtils.hasText(entryPath)) {
            return false;
        }
        try {
            String entry = PluginAssetResolver.normalizeEntry(entryPath);
            String safeVersion = version.trim();
            Path root = pluginsRoot();
            for (Path pluginDir : PluginSourceLayout.productDirectories(root, code)) {
                Path versionDir = pluginDir.resolve(safeVersion).normalize();
                if (!versionDir.startsWith(pluginDir)) {
                    continue;
                }
                for (Path dir : List.of(versionDir, pluginDir)) {
                    for (String prefix : List.of(PluginAssetResolver.FRONTEND_DIR + "/", "")) {
                        Path candidate = dir.resolve(prefix + entry).normalize();
                        if (candidate.startsWith(dir) && Files.isRegularFile(candidate)) {
                            return true;
                        }
                    }
                }
                Path jar = versionDir.resolve(code + "-" + safeVersion + ".jar");
                if (Files.isRegularFile(jar)) {
                    try (java.util.zip.ZipFile zip = new java.util.zip.ZipFile(jar.toFile())) {
                        if (zip.getEntry(PluginAssetResolver.FRONTEND_DIR + "/" + entry) != null) {
                            return true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            // 路径非法（如含大写编码）或读包失败一律按「产物不可用」处理：
            // 宁可少一个入口，也不让后台每次加载都抛一次 import 失败。
            log.debug("插件前端产物探测失败：code={}, version={}, entry={}, reason={}",
                    code, version, entryPath, e.getMessage());
            return false;
        }
        return false;
    }

    /** 在插件目录下定位声明文件：优先 <code>.jar，其次 <code>/plugin.yaml。 */
    private Path resolvePluginDeclaration(String pluginCode) {
        Path root = pluginsRoot();
        try {
            Path jar = root.resolve(pluginCode + ".jar");
            if (Files.isRegularFile(jar)) {
                return jar;
            }
            for (Path folder : PluginSourceLayout.productDirectories(root, pluginCode)) {
                if (Files.isRegularFile(folder.resolve("plugin.yaml"))) return folder;
            }
        } catch (RuntimeException e) {
            log.warn("定位插件 {} 声明文件失败：{}", pluginCode, e.getMessage());
        }
        return null;
    }

    /**
     * 卸载插件：引擎卸载 → 菜单回收（幂等必达）→ 安装记录置空 → 目录行软删。
     *
     * <p>菜单回收是收尾硬步骤（方案 §8.3，修缺口 G2）：无论引擎卸载走没走、成没成，
     * 最后都再执行一次 {@link PluginMenuPort#remove(String)}——remove 是纯 DELETE，天然幂等，
     * 两次执行第二次影响行数为 0。这样引擎卸载中途异常时菜单也不会残留，
     * 「重启后未加载 + 卸载」的兜底场景也由同一步覆盖，不再互斥二选一。
     */
    /**
     * 卸载（无站点身份）：站点实例由服务端解析，解析不到时跳过商业清算。
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean uninstall(String pluginCode) {
        return uninstall(pluginCode, com.houjiang.commons.util.SiteInstanceResolver.resolveOrNull());
    }

    /**
     * 卸载（带站点实例）：在卸载收尾后做商业清算（授权回收 + 支付凭证清理）。
     *
     * <p>清算失败<b>不阻断卸载</b>——卸载是租户的显式意图，「卸不掉」比「清理不干净」严重得多；
     * 清算内部已自行兜异常并留日志等人工补偿。
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean uninstall(String pluginCode, String instanceId) {
        if(pluginCode!=null && STANDALONE_SERVICES.contains(pluginCode.trim()))throw new IllegalArgumentException("独立服务必须由容器安装代理管理");
        requireCatalogItem(pluginCode);
        MallPluginInstall install = requireInstalledRecord(pluginCode);
        RuntimeException lifecycleFailure = null;
        if (lifecycleManager != null && lifecycleManager.isLoaded(pluginCode)) {
            try {
                lifecycleManager.uninstall(pluginCode);
            } catch (RuntimeException e) {
                // 记录但不在此刻抛出：菜单回收必须继续执行，避免「记录回滚了、菜单却残留/或相反」
                lifecycleFailure = e;
            }
        }
        if (pluginMenuPort != null) {
            pluginMenuPort.remove(pluginCode);
        }
        if (lifecycleFailure != null) {
            log.warn("插件 {} 引擎卸载报错，已单独完成菜单回收，安装记录按未安装收尾", pluginCode, lifecycleFailure);
        }
        install.setInstalled(0);
        install.setEnabled(0);
        install.setUpdateTime(LocalDateTime.now());
        boolean updated = pluginInstallMapper.updateById(install) > 0;
        // hou_addon_plugin 是正式发布目录，不是安装态。卸载只更新 install、回收菜单和运行制品，
        // 目录行必须保留，确保未安装商品仍能在插件中心展示并允许再次购买/安装。
        // 商业清算：授权回收（释放实例占用）+ 订单支付凭证与回调流水清理。
        // 必须放在 installed=0 之后——授权解绑守卫要求插件不在安装态。
        if (updated && lifecycleSettlementService != null) {
            try {
                lifecycleSettlementService.onUninstalled(pluginCode, instanceId);
            } catch (RuntimeException e) {
                log.error("插件 {} 卸载后商业清算失败（需人工补偿）: {}", pluginCode, e.getMessage());
            }
        }
        return updated;
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean toggle(String pluginCode, boolean enabled) {
        return toggle(pluginCode, enabled, null);
    }

    /**
     * 启用/停用插件。
     *
     * <p><b>启用必须持有本站点有效授权</b>：安装时校验过授权，但授权过期或被吊销后
     * 原实现仍可直接启用，等于绕开车控（授权只挡安装、不挡运行）。
     * 停用方向不设门控——欠费或授权失效时应允许停用与卸载。
     *
     * @param instanceId 站点实例标识；传入时按实例收敛授权校验，为空时按本站点库内授权校验
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean toggle(String pluginCode, boolean enabled, String instanceId) {
        if(pluginCode!=null && STANDALONE_SERVICES.contains(pluginCode.trim()))throw new IllegalArgumentException("独立服务必须由容器安装代理管理");
        requireCatalogItem(pluginCode);
        MallPluginInstall install = requireInstalledRecord(pluginCode);
        if (enabled) {
            assertEnableLicensed(pluginCode, instanceId);
        }
        if (lifecycleManager != null && lifecycleManager.isLoaded(pluginCode)) {
            if (enabled) {
                lifecycleManager.enable(pluginCode);
            } else {
                lifecycleManager.disable(pluginCode);
            }
        }
        install.setEnabled(enabled ? 1 : 0);
        install.setUpdateTime(LocalDateTime.now());
        return pluginInstallMapper.updateById(install) > 0;
    }

    /**
     * 启用前授权门控：仅对市场侧的付费/试用插件生效；免费、未上架或非市场插件不受影响。
     */
    private void assertEnableLicensed(String pluginCode, String instanceId) {
        MarketPlugin marketPlugin = marketPlugin(pluginCode);
        if (marketPlugin == null || !PluginLicenseService.requiresLicense(marketPlugin.getLicenseType())) {
            return;
        }
        if (licenseService == null) {
            throw new IllegalStateException("插件授权能力未装配，无法启用付费插件: " + pluginCode);
        }
        if (StringUtils.hasText(instanceId)) {
            licenseService.assertInstallable(pluginCode, instanceId.trim());
        } else {
            licenseService.assertInstallable(pluginCode);
        }
    }

    private MarketPlugin marketPlugin(String pluginCode) {
        if (marketPluginMapper == null || !StringUtils.hasText(pluginCode)) {
            return null;
        }
        return marketPluginMapper.selectOne(new LambdaQueryWrapper<MarketPlugin>()
                .eq(MarketPlugin::getPluginCode, pluginCode)
                .last("limit 1"));
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean saveConfig(String pluginCode, String configJson) {
        requireCatalogItem(pluginCode);
        MallPluginInstall install = requireInstalledRecord(pluginCode);
        install.setConfigJson(normalizeConfigJson(configJson));
        install.setUpdateTime(LocalDateTime.now());
        return pluginInstallMapper.updateById(install) > 0;
    }

    private String normalizeConfigJson(String configJson) {
        String normalized = StringUtils.hasText(configJson) ? configJson.trim() : "{}";
        try {
            JSON.parse(normalized);
        } catch (Exception e) {
            throw new IllegalArgumentException("插件配置必须是合法 JSON");
        }
        return normalized;
    }

    private PluginCenterItem toCenterItem(PluginCatalogItem catalogItem,
                                          MallPluginInstall install,
                                          MarketPlugin marketPlugin,
                                          MarketPluginLicense license,
                                          CategoryContext categoryContext) {
        PluginCenterItem item = new PluginCenterItem();
        item.setCode(catalogItem.getCode());
        item.setName(catalogItem.getName());
        item.setCategory(resolveCategory(catalogItem.getCode(), catalogItem.getCategory(), categoryContext));
        item.setCateId(resolveCateId(catalogItem.getCode(), categoryContext));
        String version = install != null && StringUtils.hasText(install.getVersion())
                ? install.getVersion() : catalogItem.getVersion();
        item.setVersion(version);
        item.setDescription(catalogItem.getDescription());
        item.setTerminals(catalogItem.getTerminals());
        item.setDependencies(catalogItem.getDependencies());
        item.setEntryPath(catalogItem.getEntryPath());
        String frontendRevision = resolveFrontendRevision(install);
        String frontendUrl = resolveFrontendUrl(catalogItem.getCode(), version, catalogItem.getEntryPath(), frontendRevision);
        // 声明了入口但产物没落盘：不下发 URL（否则后台 import 一个 404 的 index.mjs，每次开页面报错）。
        if (frontendUrl != null && !frontendArtifactOnDisk(catalogItem.getCode(), version, catalogItem.getEntryPath())) {
            log.debug("插件 {} 声明了前端入口 {}，但 {} 下无对应产物，本次不下发 frontendUrl",
                    catalogItem.getCode(), catalogItem.getEntryPath(), pluginsRoot());
            frontendUrl = null;
            frontendRevision = null;
        }
        item.setFrontendRevision(frontendRevision);
        item.setFrontendUrl(frontendUrl);
        item.setConfigSchema(catalogItem.getConfigSchema());
        applyMarketState(item, catalogItem.getCode(), marketPlugin, license);
        // 独立服务来自实例容器回执；内嵌扩展来自插件安装表，不以目录存在假装已安装。
        boolean installed = install != null && Integer.valueOf(1).equals(install.getInstalled());
        item.setInstalled(installed);
        item.setEnabled(installed && Integer.valueOf(1).equals(install.getEnabled()));
        item.setConfigJson(install != null && StringUtils.hasText(install.getConfigJson()) ? install.getConfigJson() : "{}");
        return item;
    }

    private void applyMarketState(PluginCenterItem item,
                                  String pluginCode,
                                  MarketPlugin marketPlugin,
                                  MarketPluginLicense license) {
        if (marketPlugin == null || !Integer.valueOf(2).equals(marketPlugin.getStatus())) {
            if (MARKET_BACKED_PLUGINS.contains(pluginCode)) {
                // 🔴 文案不能写「未上架」：市场侧的失败提示（沙箱闸门 3002、令牌过期等）
                //    与商品上架状态无关，笼统说成「未上架」会让用户误判商品没上架并反复刷新。
                //    这里只描述本字段的真实含义 —— 该插件当前未在市场销售。
                item.setLicenseType("UNLISTED");
                item.setPriceText("暂未销售");
                item.setLicensed(false);
                item.setPurchaseStatus("UNAVAILABLE");
            } else {
                item.setLicenseType("FREE");
                item.setPriceCents(0);
                item.setPriceText("免费");
                item.setLicensed(true);
                item.setPurchaseStatus("FREE");
            }
            return;
        }

        item.setLicenseType(marketPlugin.getLicenseType());
        item.setPriceCents(marketPlugin.getPriceCents());
        item.setPriceText(priceText(marketPlugin));
        boolean licensed = !PluginLicenseService.requiresLicense(marketPlugin.getLicenseType()) || license != null;
        item.setLicensed(licensed);
        item.setPurchaseStatus(purchaseStatus(marketPlugin.getLicenseType(), license));
        item.setLicenseExpireTime(license == null || license.getExpireTime() == null
                ? null : license.getExpireTime().format(TIME_FORMATTER));
        if (StringUtils.hasText(marketPlugin.getLatestVersion())) {
            item.setLatestVersion(marketPlugin.getLatestVersion());
        }
    }

    private Map<String, MarketPlugin> marketMap() {
        if (marketPluginMapper == null) {
            return Map.of();
        }
        List<MarketPlugin> plugins = marketPluginMapper.selectList(null);
        if (plugins == null) {
            return Map.of();
        }
        return plugins.stream()
                .filter(plugin -> StringUtils.hasText(plugin.getPluginCode()))
                .collect(Collectors.toMap(MarketPlugin::getPluginCode, Function.identity(), (left, right) -> left));
    }

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
                .filter(license -> StringUtils.hasText(license.getPluginCode()))
                .collect(Collectors.toMap(MarketPluginLicense::getPluginCode, Function.identity(), (left, right) -> left));
    }

    private String purchaseStatus(String licenseType, MarketPluginLicense license) {
        if (!PluginLicenseService.requiresLicense(licenseType)) {
            return "FREE";
        }
        return license == null ? "NOT_PURCHASED" : "PURCHASED";
    }

    private String priceText(MarketPlugin plugin) {
        String type = plugin.getLicenseType() == null ? "FREE" : plugin.getLicenseType().toUpperCase(Locale.ROOT);
        return switch (type) {
            case "PAID" -> "￥" + String.format(Locale.ROOT, "%.2f",
                    (plugin.getPriceCents() == null ? 0 : plugin.getPriceCents()) / 100.0);
            case "TRIAL" -> "试用 " + (plugin.getTrialDays() == null ? 0 : plugin.getTrialDays()) + " 天";
            default -> "免费";
        };
    }

    /**
     * 由插件目录的 entryPath 推导前端产物地址（契约见《开源插件化架构》8.2）。
     *
     * <p>约定：entryPath 以 {@code .mjs} / {@code .js} 结尾时视为 ESM 产物入口，
     * 返回带版本号的 {@code /web/plugin-assets/{code}/{version}/{entry}}；
     * 其余（如 {@code /points/index} 这类内置页面路径）返回 null，表示无独立前端产物，
     * 宿主前端加载器会跳过该插件，不影响既有页面。
     *
     * @param code 插件编码
     * @param version 生效版本
     * @param entryPath 目录中的前端入口路径
     * @return 前端产物 URL，无则 null
     */
    static String resolveFrontendUrl(String code, String version, String entryPath) {
        return resolveFrontendUrl(code, version, entryPath, null);
    }

    /**
     * 构造带制品修订号的前端入口。版本目录仍是主缓存键，rev 用于处理开发期或补发时
     * 同一版本内容被替换的情况；修订号只接受 URL 安全字符，避免把元数据拼成可控查询串。
     */
    static String resolveFrontendUrl(String code, String version, String entryPath, String revision) {
        if (!StringUtils.hasText(code) || !StringUtils.hasText(entryPath)) {
            return null;
        }
        String entry = entryPath.trim();
        if (!entry.endsWith(".mjs") && !entry.endsWith(".js")) {
            return null;
        }
        String normalizedEntry = entry.replaceAll("^/+", "");
        if (!StringUtils.hasText(normalizedEntry) || normalizedEntry.contains("..")) {
            return null;
        }
        String safeVersion = StringUtils.hasText(version) ? version.trim() : "0.0.0";
        String url = FRONTEND_ASSET_PREFIX + "/" + code + "/" + safeVersion + "/" + normalizedEntry;
        String safeRevision = normalizeFrontendRevision(revision);
        return StringUtils.hasText(safeRevision) ? url + "?rev=" + safeRevision : url;
    }

    /** 从安装记录生成前端制品修订号，不引入新的数据库字段。 */
    private String resolveFrontendRevision(MallPluginInstall install) {
        if (install == null) {
            return null;
        }
        String checksum = normalizeFrontendRevision(install.getChecksum());
        if (StringUtils.hasText(checksum)) {
            return checksum;
        }
        if (install.getUpdateTime() == null) {
            return null;
        }
        return install.getUpdateTime().format(FRONTEND_REVISION_TIME_FORMATTER);
    }

    private static String normalizeFrontendRevision(String revision) {
        if (!StringUtils.hasText(revision)) {
            return null;
        }
        String value = revision.trim();
        return value.matches("[A-Za-z0-9._-]{1,128}") ? value : null;
    }

    private PluginCatalogItem toCatalogItem(AddonPlugin plugin) {
        PluginCatalogItem item = new PluginCatalogItem(
                plugin.getPluginCode(), plugin.getName(), plugin.getCategory(), plugin.getCateId(),
                plugin.getVersion(), plugin.getDescription(), parseStringList(plugin.getTerminalsJson()),
                parseStringList(plugin.getDependenciesJson()), plugin.getEntryPath(), plugin.getConfigSchema()
        );
        item.setHostService(plugin.getServiceCode());
        return item;
    }

    private List<String> parseStringList(String json) {
        if (!StringUtils.hasText(json)) {
            return List.of();
        }
        try {
            return JSON.parseArray(json, String.class);
        } catch (Exception e) {
            throw new IllegalStateException("插件目录 JSON 配置错误", e);
        }
    }

    private boolean matchFilters(PluginCenterItem item, Map<String, Object> filters) {
        String code = stringValue(filters.get("code"));
        if (StringUtils.hasText(code) && !item.getCode().equals(code)) {
            return false;
        }
        String keyword = stringValue(filters.get("keyword"));
        if (StringUtils.hasText(keyword)
                && !item.getName().contains(keyword)
                && !item.getCode().contains(keyword)
                && !item.getDescription().contains(keyword)) {
            return false;
        }
        String category = stringValue(filters.get("category"));
        if (StringUtils.hasText(category)) {
            if (UNCATEGORIZED_CATEGORY.equals(category)) {
                // 「未分类」是可筛选项：命中没有归入任何 hou_addon_cate 分类的插件
                if (StringUtils.hasText(item.getCategory())) {
                    return false;
                }
            } else {
                // 分类既可能按名称传（现有前端口径），也可能按 hou_addon_cate.id 传；两种都认。
                boolean sameName = category.equals(item.getCategory());
                boolean sameId = item.getCateId() != null && category.equals(String.valueOf(item.getCateId()));
                if (!sameName && !sameId) {
                    return false;
                }
            }
        }
        String status = stringValue(filters.get("status"));
        if (!StringUtils.hasText(status)) {
            return true;
        }
        return switch (status) {
            case "installed" -> item.getInstalled();
            case "enabled" -> item.getEnabled();
            case "disabled" -> item.getInstalled() && !item.getEnabled();
            case "available" -> !item.getInstalled();
            default -> true;
        };
    }

    private MallPluginInstall requireInstalledRecord(String pluginCode) {
        MallPluginInstall install = findInstall(pluginCode);
        if (install == null || !Integer.valueOf(1).equals(install.getInstalled())) {
            throw new IllegalStateException("插件未安装: " + pluginCode);
        }
        return install;
    }

    private MallPluginInstall findInstall(String pluginCode) {
        return pluginInstallMapper.selectOne(new LambdaQueryWrapper<MallPluginInstall>()
                .eq(MallPluginInstall::getPluginCode, pluginCode)
                .last("limit 1"));
    }

    private String stringValue(Object value) {
        return value == null ? "" : value.toString().trim();
    }

    private int toInt(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /** 分类表 / 菜单表可能不存在，DAO 返回 null 时统一当空集合处理。 */
    private List<Map<String, Object>> nullSafe(List<Map<String, Object>> rows) {
        return rows == null ? List.of() : rows;
    }
}
