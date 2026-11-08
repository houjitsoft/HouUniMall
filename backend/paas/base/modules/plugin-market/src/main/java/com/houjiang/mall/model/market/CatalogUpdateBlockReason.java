package com.houjiang.mall.model.market;

import com.houjiang.commons.constant.PluginDistErrorCode;
import lombok.Data;

/**
 * 「有更新」判定被拦下的原因（P0-10）。
 *
 * <p><b>为什么是枚举而不是文案</b>：前端要按原因分支（未购买跳购买、过期跳续费、
 * 框架低显示「请先升级系统」），靠字符串文案判断必然随文案调整而崩。
 * 枚举 {@code name()} 直接作为接口的 {@code blockReason} 输出，
 * 与 {@link PluginDistErrorCode} 一一对应，文案只用于展示。
 *
 * @author Amos
 * @since M5
 */
public enum CatalogUpdateBlockReason {

    /**
     * 已装的就是最新版（不是异常，只是「没有更新」）。
     */
    UP_TO_DATE(0, "已是最新版本"),

    /**
     * 该插件不在市场（编码未上架或已删除）。
     */
    NOT_ON_MARKET(PluginDistErrorCode.PLUGIN_OR_VERSION_OFFLINE, "该插件不在插件市场"),

    /**
     * 商品未处于上架态（草稿 / 已下架）。
     */
    PLUGIN_NOT_PUBLISHED(PluginDistErrorCode.PLUGIN_OR_VERSION_OFFLINE, "该插件已下架，暂不提供升级"),

    /**
     * 商品上架但没有任何已发布版本（全部 offline）。
     */
    NO_PUBLISHED_VERSION(PluginDistErrorCode.VERSION_NOT_PUBLISHED, "该插件暂无可用版本"),

    /**
     * 上报的当前版本不是合法 SemVer，无法比较。
     */
    CURRENT_VERSION_INVALID(PluginDistErrorCode.MANIFEST_INVALID, "当前版本号格式非法，无法判断是否有更新"),

    /**
     * 既没上报版本、也没有安装记录，无从判断。
     */
    CURRENT_VERSION_UNKNOWN(PluginDistErrorCode.MANIFEST_INVALID, "无法确认当前版本，暂不提示更新"),

    /**
     * 宿主框架版本低于插件要求的最低框架版本。
     */
    FRAMEWORK_INCOMPATIBLE(PluginDistErrorCode.INCOMPATIBLE_FRAMEWORK, "当前系统版本低于插件要求，升级前请先升级系统"),

    /**
     * 依赖插件未安装 / 未启用 / 已装版本低于声明的最低版本。
     *
     * <p>M5b 新增：与升级前 {@code /precheck} 的 3015 对齐，共用 commons 的
     * {@code PluginDependencyParser} 解析，避免「目录说能升、预检说缺依赖」。
     */
    DEPENDENCY_MISSING(PluginDistErrorCode.DEPENDENCY_MISSING, "缺少依赖插件，请先安装依赖后再升级"),

    /**
     * 付费插件未持有本站点有效授权（含未购买 / 已过期 / 已吊销之外的情形）。
     */
    NOT_PURCHASED(PluginDistErrorCode.NOT_PURCHASED, "该插件需先购买授权才能升级"),

    /**
     * 授权已过期。
     */
    LICENSE_EXPIRED(PluginDistErrorCode.LICENSE_EXPIRED, "授权已过期，续费后可升级"),

    /**
     * 授权已被吊销。
     */
    LICENSE_REVOKED(PluginDistErrorCode.LICENSE_REVOKED, "授权已被吊销，无法升级"),

    /**
     * 授权能力未装配（部署问题，不该出现在生产）。
     */
    LICENSE_UNAVAILABLE(PluginDistErrorCode.NOT_PURCHASED, "授权能力未装配，无法校验升级资格");

    private final int errorCode;

    private final String message;

    CatalogUpdateBlockReason(int errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }

    /**
     * 对应的插件分发错误码（0 表示非错误，仅「没有更新」）。
     *
     * @return 错误码
     */
    public int getErrorCode() {
        return errorCode;
    }

    /**
     * 面向用户的中文说明。
     *
     * @return 说明文案
     */
    public String getMessage() {
        return message;
    }
}
