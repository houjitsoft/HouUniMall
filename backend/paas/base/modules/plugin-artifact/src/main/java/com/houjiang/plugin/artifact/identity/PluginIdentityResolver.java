package com.houjiang.plugin.artifact.identity;

import com.houjiang.commons.util.SecurityContextUtils;
import com.houjiang.commons.util.SiteInstanceResolver;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.Map;
import com.houjiang.commons.vo.user.UserInfoVO;
import org.springframework.util.StringUtils;

/**
 * 从登录态解析下载调用方身份。
 *
 * <p><b>为什么必须服务端解析（而不是前端传）</b>：
 * {@code instanceId} 是授权归属的唯一凭证，前端一旦能传，
 * 任何登录用户都能填上别人的站点实例把付费插件的制品拉走，授权体系直接失效。
 *
 * <p><b>instanceId 取 {@code orgId}</b>：登录态里可稳定拿到的站点级标识只有组织 ID
 * （{@link UserInfoVO#getOrgId()}），它与「一个组织 = 一个站点实例」的授权模型对齐。
 * 组织为空时退化为 {@code u-{userId}}（用户级兜底），保证实例标识永远非空——
 * 空标识会让付费插件的授权查询退化成「不限实例」，那是越权口子，必须 fail-closed。
 *
 * @author Amos
 * @since M2
 */
public final class PluginIdentityResolver {

    /**
     * 用户级实例标识前缀。
     */
    private static final String USER_INSTANCE_PREFIX = "u-";

    /**
     * 私有构造，工具类不允许实例化。
     */
    private PluginIdentityResolver() {
    }

    /**
     * 从登录态解析身份；未登录时返回 null（由上层决定报 3001）。
     *
     * @return 调用方身份，未登录返回 null
     */
    public static PluginIdentity fromCurrentContext() {
        UserInfoVO user = SecurityContextUtils.getCurrentUserOrNull();
        if (user != null) return from(user);
        String instance = SiteInstanceResolver.resolveOrNull();
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (instance != null && auth != null && auth.getPrincipal() instanceof Map<?, ?> map) {
            return new PluginIdentity(instance, parseUserId(String.valueOf(map.get("id"))));
        }
        return null;
    }

    /**
     * 从用户信息解析身份。
     *
     * @param user 登录用户信息
     * @return 调用方身份
     * @throws IllegalArgumentException user 为 null 时抛出
     */
    public static PluginIdentity from(UserInfoVO user) {
        if (user == null) {
            throw new IllegalArgumentException("登录用户信息不能为空");
        }
        Long userId = parseUserId(user.getUserId());
        String instanceId = StringUtils.hasText(user.getOrgId())
                ? user.getOrgId().trim()
                : (userId == null ? "" : USER_INSTANCE_PREFIX + userId);
        return new PluginIdentity(instanceId, userId);
    }

    /**
     * 解析用户 ID；非数字或空值时返回 null（不抛异常，userId 只是留痕字段）。
     *
     * @param rawUserId 原始用户 ID
     * @return 用户 ID，解析失败返回 null
     */
    private static Long parseUserId(String rawUserId) {
        if (!StringUtils.hasText(rawUserId)) {
            return null;
        }
        try {
            return Long.parseLong(rawUserId.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
