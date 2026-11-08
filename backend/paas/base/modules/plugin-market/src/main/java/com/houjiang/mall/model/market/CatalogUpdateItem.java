package com.houjiang.mall.model.market;

import lombok.Data;

/**
 * 目录更新判定结果项（P0-10）。
 *
 * <p>字段与方案 §4.2 一致：{@code code / currentVersion / latestVersion / upgradable / blockReason}。
 * {@code blockReason} 为 {@link CatalogUpdateBlockReason} 的 {@code name()}，
 * 前端按它分支（购买 / 续费 / 升级系统），不要用文案判断。
 *
 * @author Amos
 * @since M5
 */
@Data
public class CatalogUpdateItem {

    /**
     * 插件编码。
     */
    private String code;

    /**
     * 本站点当前版本（优先取入参，缺失时用安装态回退）。
     */
    private String currentVersion;

    /**
     * 市场最新已发布版本（SemVer 最大值，非「最后发布」）。
     */
    private String latestVersion;

    /**
     * 是否可升级（有更高版本且所有前置条件都满足）。
     */
    private boolean upgradable;

    /**
     * 被拦下的原因；可升级或已是最新时为 null / UP_TO_DATE。
     */
    private String blockReason;

    /**
     * 构造空结果项（供 JSON 反序列化使用）。
     */
    public CatalogUpdateItem() {
    }

    /**
     * 构造结果项。
     *
     * @param code           插件编码
     * @param currentVersion 当前版本
     * @param latestVersion  最新版本
     */
    public CatalogUpdateItem(String code, String currentVersion, String latestVersion) {
        this.code = code;
        this.currentVersion = currentVersion;
        this.latestVersion = latestVersion;
        this.upgradable = false;
        this.blockReason = null;
    }

    /**
     * 标记「可升级」。
     *
     * @return 当前对象，便于链式构造
     */
    public CatalogUpdateItem upgradable() {
        this.upgradable = true;
        this.blockReason = null;
        return this;
    }

    /**
     * 标记「不可升级」并给出原因。
     *
     * @param reason 原因枚举
     * @return 当前对象，便于链式构造
     */
    public CatalogUpdateItem blocked(CatalogUpdateBlockReason reason) {
        this.upgradable = false;
        this.blockReason = reason == null ? null : reason.name();
        return this;
    }
}
