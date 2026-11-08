package com.houjiang.mall.model.market;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 插件市场浏览项：合并市场元数据、定价与站点安装状态。
 *
 * @author Amos
 */
@Data
public class MarketPluginItem {

    private String code;

    private String name;

    /**
     * 分类外键（hou_addon_cate.id）；0 = 未分类。
     *
     * <p>与 {@link #category} 的关系：cateId 是<b>归属</b>（权威，可 JOIN、可用于筛选），
     * category 只是<b>展示名</b>（由 cateId 从字典派生）。分类字典里查不到的 id、
     * 以及 0，一律归一到「未分类」桶，保证前端永远拿得到一个可渲染的分类。
     */
    private Integer cateId;

    /**
     * 分类展示名。
     *
     * <p>由 cateId 从 hou_addon_cate 派生；仅在分类字典不可用（表缺失 / 查询异常）时
     * 退化为库里的原始文本，绝不返回 null —— 前端分类角标与 Tab 都依赖它。
     */
    private String category;

    private String description;

    private String shortDesc;

    private String iconUrl;

    private String latestVersion;

    /**
     * 清单声明的运行方式（embedded / standalone）。
     *
     * <p>运行方式在插件发布时就由 plugin.yaml 定死，安装时不可选——2026-10-29 前市场接口
     * 不返回该字段，前端安装弹窗只能兜底把内嵌/独立两个都放出来让用户「选」，
     * 而后端只认内嵌（LIGHTWEIGHT），选「独立服务」必然被拒。返回该字段后弹窗按清单锁定。
     * 来源优先级：最新已审版本 plugin_yaml 声明 → {@code StandalonePluginCodes} 兜底。
     */
    private String runtime;

    /** FREE / PAID / TRIAL */
    private String licenseType;

    /** 价格（分） */
    private Integer priceCents;

    /** 价格展示文案：免费 / ¥99.00 / 试用 15 天 */
    private String priceText;

    private Integer trialDays;

    private Integer downloadCount;

    private Integer installCount;

    private BigDecimal ratingAvg;

    private Integer ratingCount;

    private List<String> tags;

    private List<String> terminals;

    private List<String> dependencies;

    /** 该站点是否已安装 */
    private Boolean installed;

    /** 本站点已安装版本（未安装时为 null） */
    private String installedVersion;

    /** 该站点是否已启用 */
    private Boolean enabled;

    /** 该站点是否已持有有效授权（PAID / TRIAL 才有意义；FREE 恒为 true） */
    private Boolean licensed;

    /** 授权到期时间文案，null 且 licensed=true 表示永久授权 */
    private String licenseExpireTime;

    /** 购买状态：FREE / PURCHASED / NOT_PURCHASED。 */
    private String purchaseStatus;

    /**
     * 插件声明的宿主服务（平台目录从最新已审版本的 plugin_yaml 解析；本地快照模式可能为空）。
     *
     * <p>2026-10-30：市场页要能提前告知「这个插件装到哪个服务」，以及该宿主在本站是否存在，
     * 而不是等用户点完安装才在派发环节报「注册中心未找到 xx 服务的可用实例」。</p>
     */
    private String hostService;

    /**
     * 本站是否具备该插件的安装条件（宿主在线或本机装载）。
     *
     * <p>{@code null} = 平台目录未提供宿主信息（旧平台 + 新站点），前端不应拦截。</p>
     */
    private Boolean hostAvailable;

    /** hostAvailable=false 时给前端展示的原因（面向管理员的完整句子）。 */
    private String hostUnavailableReason;
}
