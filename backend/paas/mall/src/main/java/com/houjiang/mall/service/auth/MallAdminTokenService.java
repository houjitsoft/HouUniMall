package com.houjiang.mall.service.auth;

import com.houjiang.mall.util.MallAdminTokenHelper;
import com.houjiang.mall.mapper.auth.MallSystemMenuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Mall 管理员 Token 解析（MALL_ADMIN_TOKEN Redis）
 * @author Amos
 */
@Service
@RequiredArgsConstructor
public class MallAdminTokenService {

    private static final String MALL_ADMIN_TOKEN_KEY = "MALL_ADMIN_TOKEN:";

    private final RedisTemplate<String, Object> redisTemplate;
    private final MallSystemMenuMapper mallSystemMenuMapper;

    @SuppressWarnings("unchecked")
    public Map<String, Object> getAdminInfo(String rawToken) {
        if (!StringUtils.hasText(rawToken)) {
            return null;
        }
        String token = MallAdminTokenHelper.normalize(rawToken);
        Object value = redisTemplate.opsForValue().get(MALL_ADMIN_TOKEN_KEY + token);
        if (value == null) {
            return null;
        }
        return (Map<String, Object>) value;
    }

    public Integer getAdminId(String rawToken) {
        Map<String, Object> adminInfo = getAdminInfo(rawToken);
        if (adminInfo == null) {
            return null;
        }
        Object id = adminInfo.get("id");
        return id == null ? null : Integer.parseInt(String.valueOf(id));
    }

    /**
     * 是否已登录：商城管理员 token 或 Cloud 内嵌商城的云后台 token。
     */
    public boolean isAuthenticated(String rawToken) {
        if (getAdminInfo(rawToken) != null) {
            return true;
        }
        String token = MallAdminTokenHelper.normalize(rawToken);
        if (!StringUtils.hasText(token)) {
            return false;
        }
        Object cloudUser = redisTemplate.opsForValue().get(
                com.houjiang.commons.config.HouConfig.HOU_USER_TOKEN_USER + token);
        return cloudUser != null && StringUtils.hasText(String.valueOf(cloudUser));
    }

    public boolean hasFinanceAccess(String rawToken) {
        return !getFinancePermissions(rawToken).isEmpty();
    }

    public Set<String> getFinancePermissions(String rawToken) {
        Map<String, Object> adminInfo = getAdminInfo(rawToken);
        if (adminInfo == null) {
            return Set.of();
        }
        if (Integer.parseInt(String.valueOf(adminInfo.getOrDefault("root", 0))) == 1) {
            return Set.of("*");
        }
        Object id = adminInfo.get("id");
        if (id == null) {
            return Set.of();
        }
        int adminId = Integer.parseInt(String.valueOf(id));
        Stream<String> routePermissions = emptyIfNull(mallSystemMenuMapper.selectRouteMenusByAdminId(adminId)).stream()
                .map(menu -> menu.getPerms())
                .filter(StringUtils::hasText);
        Stream<String> actionPermissions = emptyIfNull(mallSystemMenuMapper.selectPermsByAdminId(adminId)).stream()
                .filter(StringUtils::hasText);
        return Stream.concat(routePermissions, actionPermissions)
                .filter(MallAdminTokenService::isFinancePermission)
                .collect(Collectors.toUnmodifiableSet());
    }

    public static boolean isFinancePermission(String permission) {
        return "*".equals(permission)
                || "mall:finance".equals(permission)
                || permission.startsWith("mall:finance:")
                || "shell:mall:finance".equals(permission)
                || permission.startsWith("shell:mall:finance:");
    }

    private static <T> java.util.List<T> emptyIfNull(java.util.List<T> values) {
        return values == null ? java.util.List.of() : values;
    }
}
