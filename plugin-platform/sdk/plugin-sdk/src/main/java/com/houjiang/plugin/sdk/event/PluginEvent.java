package com.houjiang.plugin.sdk.event;

import java.io.Serializable;
import java.time.Instant;

/**
 * 插件事件。
 *
 * <p>插件通过 {@link com.houjiang.plugin.sdk.api.PluginContext#publishEvent(String, Object)} 发布，
 * 宿主事件总线负责分发给订阅方。事件是插件与宿主、插件与插件之间解耦通信的推荐方式。
 *
 * @author HouCloud
 */
public class PluginEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 事件主题，建议 {@code 模块.动作}，如 {@code order.created} */
    private String topic;

    /** 事件负载 */
    private Object payload;

    /** 发布方插件编码，宿主事件为 {@code host} */
    private String source;

    /** 发生时间 */
    private Instant occurredAt = Instant.now();

    public PluginEvent() {
    }

    public PluginEvent(String topic, Object payload, String source) {
        this.topic = topic;
        this.payload = payload;
        this.source = source;
    }

    public static PluginEvent of(String topic, Object payload) {
        return new PluginEvent(topic, payload, null);
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }

    @Override
    public String toString() {
        return "PluginEvent{topic='" + topic + "', source='" + source + "', at=" + occurredAt + '}';
    }
}
