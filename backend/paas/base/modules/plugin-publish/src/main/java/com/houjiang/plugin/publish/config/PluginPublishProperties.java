package com.houjiang.plugin.publish.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 插件发布侧配置，前缀 {@code houcloud.plugin.*}。
 *
 * <pre>
 * houcloud:
 *   plugin:
 *     max-artifact-bytes: 67108864   # 64MB
 * </pre>
 *
 * @author Amos
 * @since M1
 */
@Component
@ConfigurationProperties(prefix = "houcloud.plugin")
public class PluginPublishProperties {

    /**
     * 单包体积上限（字节），默认 64MB（67108864）。超出报 3017 SIZE_EXCEEDED。
     */
    private long maxArtifactBytes = 64L * 1024 * 1024;

    /**
     * 发布类写接口是否只允许平台管理员调用，默认 true。
     *
     * <p>发布/审核/上下架是<b>平台级动作</b>，一旦放开给普通租户，
     * 任何人都能把自己的制品推成「已发布」并出现在所有人的市场里。
     * 因此默认收口到平台管理员；只有「第三方开发者自助发布」这套
     * （含开发者实名、审核流、责任主体）真正落地后，才把它配成 false。
     *
     * <p>判定复用现有约定：mall 的 {@code MallAdminTokenAuthenticationFilter}
     * 在 bbc 后台登录时写入 {@code ROLE_MALL_ADMIN} 权限，
     * 本模块只做一次 {@code SecurityContextUtils.hasPermission("ROLE_MALL_ADMIN")} 校验，
     * <b>不新造管理员判断工具</b>。
     */
    private boolean adminOnly = true;

    /**
     * @return 单包体积上限（字节）
     */
    public long getMaxArtifactBytes() {
        return maxArtifactBytes;
    }

    /**
     * @param maxArtifactBytes 单包体积上限（字节）
     */
    public void setMaxArtifactBytes(long maxArtifactBytes) {
        this.maxArtifactBytes = maxArtifactBytes;
    }

    /**
     * @return 是否只允许平台管理员调用发布类接口
     */
    public boolean isAdminOnly() {
        return adminOnly;
    }

    /**
     * @param adminOnly 是否只允许平台管理员调用发布类接口
     */
    public void setAdminOnly(boolean adminOnly) {
        this.adminOnly = adminOnly;
    }
}
