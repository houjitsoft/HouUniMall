package com.houjiang.plugin.sdk.util;

import com.houjiang.plugin.sdk.api.PluginContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 插件日志工具。
 *
 * <p>统一使用 <code>plugin.{pluginCode}</code> 作为 logger 名，便于按插件隔离日志级别与排查。
 *
 * <p>示例：
 * <pre>{@code
 * private static final Logger log = PluginLogger.get(context);
 * }</pre>
 *
 * @author HouCloud
 */
public final class PluginLogger {

    private PluginLogger() {
    }

    /**
     * 依据插件上下文获取 logger，名称为 {@code plugin.{pluginCode}}。
     */
    public static Logger get(PluginContext context) {
        return LoggerFactory.getLogger(context == null ? "plugin.unknown" : context.getLoggerName());
    }

    /**
     * 指定插件编码获取 logger。
     */
    public static Logger get(String pluginCode) {
        return LoggerFactory.getLogger("plugin." + pluginCode);
    }
}
