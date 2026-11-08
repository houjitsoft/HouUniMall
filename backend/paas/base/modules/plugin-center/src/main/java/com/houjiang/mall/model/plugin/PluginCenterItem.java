package com.houjiang.mall.model.plugin;

import lombok.Data;

import java.util.List;

/**
 * 插件中心列表项，合并目录信息和安装状态。
 * @author Amos
 */
@Data
public class PluginCenterItem {

    private String code;

    private String name;

    private String category;

    /**
     * 插件所属分类 ID（hou_addon_cate.id），由插件位一级菜单的 cate_id 关联得出。
     * 未能关联到分类时为 null，语义等价于「未分类」；此时仍以 {@link #category} 文案兜底展示。
     */
    private Integer cateId;

    private String version;

    /** 市场上最新已审核版本（未上架时为空）。 */
    private String latestVersion;

    private String description;

    private List<String> terminals;

    private List<String> dependencies;

    private String entryPath;

    /**
     * 插件前端产物（ESM）地址，含版本号，供宿主运行时 import。
     * 仅当插件声明了 ESM 入口（如 index.mjs）时非空；老插件为 null，前端不加载。
     */
    private String frontendUrl;

    /**
     * 前端制品修订号。优先使用安装包 checksum，缺失时使用安装记录更新时间；
     * 宿主将其编码到 frontendUrl 的 rev 查询参数，避免同一插件版本复用旧缓存。
     */
    private String frontendRevision;

    private String configSchema;

    /** 市场数据库中的收费类型：FREE / PAID / TRIAL。 */
    private String licenseType;

    /** 市场数据库中的价格，单位为分。 */
    private Integer priceCents;

    /** 面向管理后台的价格文案，由市场数据库字段计算。 */
    private String priceText;

    /** 本站点是否持有有效授权；购买状态的最终判断来自 hou_market_plugin_license。 */
    private Boolean licensed;

    /** FREE / PURCHASED / NOT_PURCHASED / UNAVAILABLE。 */
    private String purchaseStatus;

    /** 有效授权到期时间；null 且 licensed=true 表示永久授权。 */
    private String licenseExpireTime;

    private Boolean installed;

    private Boolean enabled;

    /** Known deployment outage; independent of installation, entitlement and maintenance. */
    private String healthStatus;

    private String configJson;
}
