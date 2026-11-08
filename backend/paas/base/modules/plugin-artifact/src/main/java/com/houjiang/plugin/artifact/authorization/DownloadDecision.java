package com.houjiang.plugin.artifact.authorization;

import com.houjiang.mall.entity.market.MarketPlugin;
import com.houjiang.mall.entity.market.MarketPluginVersion;

/**
 * 下载授权判定结果。
 *
 * <p>不抛异常也是有意的：授权判定是「业务分支」而不是「系统错误」，
 * 未授权时上层直接把 {@code code} 塞进 {@code ResponseResult.fail}，data 保持 null。
 *
 * @author Amos
 * @since M2
 */
public final class DownloadDecision {

    private final boolean allowed;

    private final int code;

    private final String message;

    private final MarketPluginVersion version;

    private final MarketPlugin plugin;

    private DownloadDecision(boolean allowed,
                             int code,
                             String message,
                             MarketPluginVersion version,
                             MarketPlugin plugin) {
        this.allowed = allowed;
        this.code = code;
        this.message = message;
        this.version = version;
        this.plugin = plugin;
    }

    /**
     * 构造放行判定。
     *
     * @param version 命中的版本行
     * @param plugin  命中的商品行，可为 null
     * @return 放行判定
     */
    public static DownloadDecision allow(MarketPluginVersion version, MarketPlugin plugin) {
        return new DownloadDecision(true, 0, "ok", version, plugin);
    }

    /**
     * 构造拒绝判定。
     *
     * @param code    插件分发错误码
     * @param message 描述
     * @return 拒绝判定
     */
    public static DownloadDecision deny(int code, String message) {
        return new DownloadDecision(false, code, message, null, null);
    }

    /**
     * 构造拒绝判定（携带命中的版本行，便于日志与后续扩展）。
     *
     * @param code    插件分发错误码
     * @param message 描述
     * @param version 命中的版本行，可为 null
     * @return 拒绝判定
     */
    public static DownloadDecision deny(int code, String message, MarketPluginVersion version) {
        return new DownloadDecision(false, code, message, version, null);
    }

    /**
     * @return 是否放行
     */
    public boolean isAllowed() {
        return allowed;
    }

    /**
     * @return 错误码（放行时为 0）
     */
    public int getCode() {
        return code;
    }

    /**
     * @return 描述
     */
    public String getMessage() {
        return message;
    }

    /**
     * @return 命中的版本行
     */
    public MarketPluginVersion getVersion() {
        return version;
    }

    /**
     * @return 命中的商品行，可能为 null
     */
    public MarketPlugin getPlugin() {
        return plugin;
    }
}
