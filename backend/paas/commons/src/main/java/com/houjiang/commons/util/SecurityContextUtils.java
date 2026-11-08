package com.houjiang.commons.util;

import com.houjiang.commons.vo.user.UserInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * SecurityContext工具类
 * 
 * 【重要说明】获取当前登录用户：
 * 1. JWT过滤器（JwtAuthenticationTokenFilter）已经验证了token并设置了认证信息
 * 2. Controller中直接使用此工具类获取用户信息，无需再次验证token
 * 3. 使用方式：
 *    ```java
 *    UserInfoVO user = SecurityContextUtils.getCurrentUser();
 *    String userId = user.getUserId();
 *    String orgId = user.getOrgId();
 *    ```
 * 
 * 【关键点】：
 * - 不要手动验证token，JWT过滤器已经处理
 * - 不要手动调用ParseTokenUtils.getUserInfoByToken()
 * - 如果getCurrentUser()抛出异常，说明用户未登录（JWT过滤器应该已经拦截）
 * 
 * 【Token验证流程】：
 * 前端 → BFF层（转发token） → PAAS层（JWT过滤器验证） → Controller（使用本工具类获取用户）
 * 
 * @author Amos
 * @since 2025-01-29
 */
@Slf4j
public class SecurityContextUtils {

    /**
     * 从SecurityContext获取当前登录用户信息
     * 支持两种类型的Principal：
     * 1. LoginUser对象（包含UserInfoVO和权限列表）
     * 2. UserInfoVO对象（直接就是用户信息）
     * 
     * @return 当前登录用户信息
     * @throws RuntimeException 如果用户未登录或登录已过期
     */
    public static UserInfoVO getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || authentication.getPrincipal() == null) {
            log.error("SecurityContextUtils - SecurityContext中没有认证信息，用户未登录！" +
                "authentication: {}, principal: {}", 
                authentication != null, 
                authentication != null && authentication.getPrincipal() != null);
            throw new RuntimeException("用户未登录或登录已过期");
        }
        
        Object principal = authentication.getPrincipal();
        log.debug("SecurityContextUtils - Principal类型: {}, 类名: {}", 
            principal.getClass().getSimpleName(), 
            principal.getClass().getName());
        
        // 如果是LoginUser类型（OA服务等使用）
        if (principal.getClass().getName().contains("LoginUser")) {
            try {
                // 使用反射获取user字段
                java.lang.reflect.Method getUserMethod = principal.getClass().getMethod("getUser");
                Object user = getUserMethod.invoke(principal);
                if (user instanceof UserInfoVO) {
                    log.debug("SecurityContextUtils - 成功从LoginUser提取UserInfoVO，用户ID: {}", 
                        ((UserInfoVO) user).getUserId());
                    return (UserInfoVO) user;
                } else {
                    log.error("SecurityContextUtils - LoginUser.getUser()返回的不是UserInfoVO类型: {}", 
                        user != null ? user.getClass().getName() : "null");
                }
            } catch (Exception e) {
                log.error("SecurityContextUtils - 从LoginUser获取UserInfoVO失败: {}", e.getMessage(), e);
            }
        }
        
        // 如果是UserInfoVO类型（PM服务等使用）
        if (principal instanceof UserInfoVO) {
            log.debug("SecurityContextUtils - Principal直接是UserInfoVO类型，用户ID: {}", 
                ((UserInfoVO) principal).getUserId());
            return (UserInfoVO) principal;
        }
        
        // 如果是String类型（可能是userId，简化过滤器设置）
        if (principal instanceof String) {
            log.warn("SecurityContextUtils - SecurityContext中的Principal是String类型（userId），无法获取完整用户信息");
            throw new RuntimeException("无法获取完整用户信息，请使用JwtAuthenticationTokenFilter");
        }
        
        log.error("SecurityContextUtils - SecurityContext中的Principal类型不支持: {}, 完整类名: {}", 
            principal.getClass().getSimpleName(), 
            principal.getClass().getName());
        throw new RuntimeException("用户信息类型不支持: " + principal.getClass().getName());
    }

    /**
     * 从SecurityContext获取当前登录用户信息（不抛异常，返回null）
     * 
     * @return 当前登录用户信息，如果未登录返回null
     */
    public static UserInfoVO getCurrentUserOrNull() {
        try {
            return getCurrentUser();
        } catch (Exception e) {
            log.debug("获取当前用户信息失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 检查当前是否有用户登录
     * 
     * @return true表示已登录，false表示未登录
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null 
            && authentication.getPrincipal() != null 
            && !"anonymousUser".equals(authentication.getPrincipal());
    }

    /**
     * 从SecurityContext获取当前登录用户的ID
     * 
     * @return 用户ID，如果未登录返回null
     */
    public static String getCurrentUserId() {
        UserInfoVO user = getCurrentUserOrNull();
        return user != null ? user.getUserId() : null;
    }

    /**
     * 读取当前会话的精确权限码。权限判断不做父级权限兜底，避免授予菜单权限后意外放开按钮。
     */
    public static Set<String> getCurrentPermissions() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            return Set.of();
        }
        Set<String> permissions = new LinkedHashSet<>();
        Object principal = authentication.getPrincipal();
        collectPermissionProperty(principal, permissions);
        if (authentication.getAuthorities() != null) {
            for (GrantedAuthority authority : authentication.getAuthorities()) {
                if (authority != null && authority.getAuthority() != null) {
                    addPermission(permissions, authority.getAuthority());
                }
            }
        }
        if (principal instanceof UserInfoVO user) {
            collectUserInfoPermissions(user, permissions);
        } else {
            try {
                Object user = principal.getClass().getMethod("getUser").invoke(principal);
                if (user instanceof UserInfoVO userInfo) {
                    collectUserInfoPermissions(userInfo, permissions);
                }
            } catch (Exception ignored) {
                // Principal does not expose a UserInfoVO; the explicit permission list is authoritative.
            }
        }
        return permissions;
    }

    public static boolean hasPermission(String permission) {
        return permission != null && getCurrentPermissions().contains(permission.trim());
    }

    /**
     * 兼容 base 未安装 JWT 过滤器时由转发令牌解析出的 UserInfoVO。
     */
    public static boolean hasPermission(UserInfoVO user, String permission) {
        if (user == null || permission == null || permission.isBlank()) return false;
        Set<String> permissions = new LinkedHashSet<>();
        collectUserInfoPermissions(user, permissions);
        return permissions.contains(permission.trim());
    }

    private static void collectPermissionProperty(Object principal, Set<String> target) {
        try {
            Object value = principal.getClass().getMethod("getPermissions").invoke(principal);
            if (value instanceof Collection<?> collection) {
                collection.forEach(item -> addPermission(target, item));
            } else if (value instanceof String text) {
                splitPermissions(text, target);
            }
        } catch (Exception ignored) {
            // A UserInfoVO-only principal has no permissions property.
        }
    }

    private static void collectUserInfoPermissions(UserInfoVO user, Set<String> target) {
        splitPermissions(user.getLimits(), target);
        splitPermissions(user.getRightsLimitsAll(), target);
    }

    private static void splitPermissions(String raw, Set<String> target) {
        if (raw == null || raw.isBlank()) return;
        for (String value : raw.split("[,;\\s]+")) addPermission(target, value);
    }

    private static void addPermission(Set<String> target, Object value) {
        if (value != null && !value.toString().isBlank()) target.add(value.toString().trim());
    }
}
