package com.houjiang.commons.event;

import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 插件版本下架事件（M5b，方案 §7.4 紧急下架联动）。
 *
 * <p><b>为什么事件类放在 commons</b>：
 * 发布侧（{@code plugin-publish}）负责发事件，授权侧（{@code plugin-license}）负责监听并吊销，
 * 而 <b>publish 不能依赖 license</b>（依赖图上是
 * {@code mall → plugin-publish → plugin-market → plugin-license}，反向即成环）。
 * 把事件类下沉到 commons，两边都只看得见 commons，不需要新增任何一条依赖边——
 * 这与 {@code PluginDistErrorCode}、{@code PluginVersionComparator} 的下沉是同一套做法。
 *
 * <p><b>为什么用 Spring 事件而不是直接调用</b>：下架是发布域的动作，吊销是授权域的动作，
 * 直接调用会把「谁被下架了」和「谁的授权要作废」焊死在一处；
 * 用事件可以让将来新增的监听方（如通知租户、冻结订单）零改动接入。
 *
 * @author Amos
 * @since M5b
 */
public class PluginVersionOfflineEvent extends ApplicationEvent {

    /**
     * 下架原因常量：紧急下架（安全 / 合规 / 重大缺陷）。
     */
    public static final String REASON_EMERGENCY = "EMERGENCY_OFFLINE";

    private static final long serialVersionUID = 1L;

    private final String pluginCode;

    private final String version;

    private final String reason;

    private final LocalDateTime offlineTime;

    /**
     * 构造事件。
     *
     * @param source      事件源（通常是发布服务）
     * @param pluginCode  插件编码
     * @param version     被下架的版本号
     * @param reason      下架原因，可为 null
     * @param offlineTime 下架时间
     */
    public PluginVersionOfflineEvent(Object source, String pluginCode, String version,
                                     String reason, LocalDateTime offlineTime) {
        super(source);
        this.pluginCode = pluginCode == null ? null : pluginCode.trim();
        this.version = version == null ? null : version.trim();
        this.reason = reason;
        this.offlineTime = offlineTime == null ? LocalDateTime.now() : offlineTime;
    }

    /**
     * 插件编码。
     *
     * @return 插件编码
     */
    public String getPluginCode() {
        return pluginCode;
    }

    /**
     * 被下架的版本号。
     *
     * @return 版本号
     */
    public String getVersion() {
        return version;
    }

    /**
     * 下架原因。
     *
     * @return 原因，可为 null
     */
    public String getReason() {
        return reason;
    }

    /**
     * 下架时间。
     *
     * @return 时间
     */
    public LocalDateTime getOfflineTime() {
        return offlineTime;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof PluginVersionOfflineEvent event)) {
            return false;
        }
        return Objects.equals(pluginCode, event.pluginCode)
                && Objects.equals(version, event.version)
                && Objects.equals(reason, event.reason)
                && Objects.equals(offlineTime, event.offlineTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pluginCode, version, reason, offlineTime);
    }

    @Override
    public String toString() {
        return "PluginVersionOfflineEvent{pluginCode='" + pluginCode + "', version='" + version
                + "', reason='" + reason + "', offlineTime=" + offlineTime + "}";
    }
}
