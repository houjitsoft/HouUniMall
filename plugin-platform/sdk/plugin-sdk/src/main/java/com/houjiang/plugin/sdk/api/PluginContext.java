package com.houjiang.plugin.sdk.api;

import java.util.Map;
import java.util.function.Consumer;

/**
 * 插件上下文 - 插件与框架交互的核心 API。
 * 每个插件实例持有一个 PluginContext，可获取配置、发布事件、访问框架服务。
 *
 * @author HouCloud
 */
public interface PluginContext {

    /**
     * 获取插件编码
     *
     * @return 插件唯一编码
     */
    String getPluginCode();

    /**
     * 获取插件版本
     *
     * @return 当前版本号
     */
    String getPluginVersion();

    /**
     * 获取插件配置（来自 plugin.yaml 中 configSchema 对应的用户填写值）
     *
     * @return 配置 Map
     */
    Map<String, Object> getConfig();

    /**
     * 获取指定配置项
     *
     * @param key          配置键
     * @param defaultValue 默认值
     * @param <T>          值类型
     * @return 配置值
     */
    <T> T getConfig(String key, T defaultValue);

    /**
     * 从框架获取服务 Bean
     *
     * @param clazz Bean 类型
     * @param <T>   Bean 泛型
     * @return Bean 实例，不存在则返回 null
     */
    <T> T getService(Class<T> clazz);

    /**
     * 发布事件到框架事件总线
     *
     * @param topic   事件主题
     * @param payload 事件数据
     */
    void publishEvent(String topic, Object payload);

    /**
     * 获取插件数据目录路径（可读写）
     *
     * @return 数据目录绝对路径
     */
    String getDataDir();

    /**
     * 获取插件资源目录路径（只读）
     *
     * @return 资源目录绝对路径
     */
    String getResourceDir();

    /**
     * 获取插件日志记录器名称前缀
     *
     * @return 日志前缀，格式：plugin.{pluginCode}
     */
    default String getLoggerName() {
        return "plugin." + getPluginCode();
    }

    /** 获取宿主当前语言，例如 zh-CN、en、ja。 */
    default String getLocale() {
        return "zh-CN";
    }

    /**
     * 翻译插件命名空间内的文本。宿主未提供翻译时返回 fallback 或 key。
     */
    default String translate(String key, String fallback) {
        return fallback != null ? fallback : key;
    }

    /** 订阅宿主语言变化；默认实现为空，便于旧插件平滑升级。 */
    default AutoCloseable onLocaleChange(Consumer<String> listener) {
        return () -> { };
    }
}
