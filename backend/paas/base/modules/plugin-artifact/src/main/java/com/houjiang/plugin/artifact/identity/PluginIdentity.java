package com.houjiang.plugin.artifact.identity;

import java.util.Objects;

/**
 * 下载请求的调用方身份：站点实例（租户）+ 用户。
 *
 * <p><b>必须由服务端从登录态解析</b>，绝不接收前端传值——
 * 一旦前端可以传 instanceId，任何人都能把别人的付费授权「借」过来下载。
 *
 * @author Amos
 * @since M2
 */
public final class PluginIdentity {

    private final String instanceId;

    private final Long userId;

    /**
     * 构造调用方身份。
     *
     * @param instanceId 站点实例（域名/机器码），无值时传空串
     * @param userId     用户 ID，可为 null
     */
    public PluginIdentity(String instanceId, Long userId) {
        this.instanceId = instanceId == null ? "" : instanceId.trim();
        this.userId = userId;
    }

    /**
     * @return 站点实例，无值时为空串
     */
    public String getInstanceId() {
        return instanceId;
    }

    /**
     * @return 用户 ID，可为 null
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * @return 是否携带了站点实例
     */
    public boolean hasInstance() {
        return !instanceId.isEmpty();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof PluginIdentity that)) {
            return false;
        }
        return Objects.equals(instanceId, that.instanceId) && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(instanceId, userId);
    }

    @Override
    public String toString() {
        return "PluginIdentity{instance=" + instanceId + ", user=" + userId + "}";
    }
}
