package com.houjiang.plugin.engine.core;

import com.houjiang.plugin.sdk.api.PluginContext;
import com.houjiang.plugin.sdk.model.PluginManifest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Collections;
import java.util.Map;

/**
 * {@link PluginContext} 的宿主侧实现。
 *
 * <p>每个插件实例持有一个 {@code PluginContextImpl}：配置来自运营者在插件中心填写的值，
 * 数据目录按 <code>plugin-data/{code}/</code> 隔离，服务获取走宿主 ApplicationContext。
 *
 * @author HouCloud
 */
public class PluginContextImpl implements PluginContext {

    private final PluginManifest manifest;
    private final Map<String, Object> config;
    private final String dataDir;
    private final String resourceDir;
    private final ApplicationContext applicationContext;

    public PluginContextImpl(PluginManifest manifest, Map<String, Object> config,
                             String dataDir, String resourceDir, ApplicationContext applicationContext) {
        this.manifest = manifest;
        this.config = config == null ? Collections.emptyMap() : config;
        this.dataDir = dataDir;
        this.resourceDir = resourceDir;
        this.applicationContext = applicationContext;
    }

    @Override
    public String getPluginCode() {
        return manifest.getCode();
    }

    @Override
    public String getPluginVersion() {
        return manifest.getVersion();
    }

    @Override
    public Map<String, Object> getConfig() {
        return config;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getConfig(String key, T defaultValue) {
        Object v = config.get(key);
        return v == null ? defaultValue : (T) v;
    }

    @Override
    public <T> T getService(Class<T> clazz) {
        try {
            return applicationContext.getBean(clazz);
        } catch (Throwable t) {
            return null;
        }
    }

    @Override
    public void publishEvent(String topic, Object payload) {
        if (applicationContext instanceof ApplicationEventPublisher publisher) {
            publisher.publishEvent(new PluginBusEvent(topic, payload, manifest.getCode()));
        }
    }

    @Override
    public String getDataDir() {
        return dataDir;
    }

    @Override
    public String getResourceDir() {
        return resourceDir;
    }

    /**
     * 引擎内部插件事件载体，供宿主 {@code @EventListener} 监听。
     */
    public static class PluginBusEvent extends ApplicationEvent {

        private final String topic;
        private final String pluginCode;

        public PluginBusEvent(String topic, Object payload, String pluginCode) {
            super(payload);
            this.topic = topic;
            this.pluginCode = pluginCode;
        }

        public String getTopic() {
            return topic;
        }

        public String getPluginCode() {
            return pluginCode;
        }
    }
}
