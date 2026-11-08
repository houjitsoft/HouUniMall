package com.houjiang.plugin.engine.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Uses the host request's Spring Security context. The plugin engine does not
 * resolve tokens itself; the host authentication filter remains authoritative.
 *
 * <p><b>fail-closed</b>: {@link #hasPermission(String)} 对空权限一律返回 {@code false}。
 * 原实现把「空权限」判为通过，等于让任何未声明权限的插件路由对所有登录用户开放
 * （买家账号也能调 ERP／收银／财务插件的管理接口）。未登录与空权限都必须是拒绝。
 *
 * <p>「未声明权限 = 仅校验登录态」属于路由层的历史兼容策略，由
 * {@code PluginDispatcher} 显式判断并可由
 * {@code houcloud.plugin.require-declared-route-permission} 收敛为拒绝，
 * 不再由本求值器隐式放行。
 */
public class SpringSecurityPluginPermissionEvaluator implements PluginPermissionEvaluator {

    @Override
    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal());
    }

    @Override
    public boolean hasPermission(String permission) {
        if (!isAuthenticated()) {
            return false;
        }
        if (permission == null || permission.isBlank()) {
            // fail-closed：没有权限标识就没有可校验的授权，不得默认放行
            return false;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String granted = authority.getAuthority();
            if ("*".equals(granted) || permission.equals(granted)) {
                return true;
            }
        }
        // Cloud's menu permission is the application-level capability. Older
        // sessions may carry shell:{plugin}:apps without every fine-grained
        // plugin permission, so keep the plugin route usable after login while
        // still requiring the correct application scope (never any shell app).
        int separator = permission.indexOf(':');
        if (separator > 0) {
            String pluginCode = permission.substring(0, separator).trim();
            if (!pluginCode.isEmpty()) {
                // Cloud sessions have existed with both the shell-app
                // capability and the dashboard-module capability.  Treat
                // either as the plugin-level grant; fine-grained route
                // permissions remain preferred and are still checked first.
                String applicationPermission = "shell:" + pluginCode + ":apps";
                String dashboardPermission = "dashboard:module:" + pluginCode;
                String shellPermission = "shell:" + pluginCode;
                for (GrantedAuthority authority : authentication.getAuthorities()) {
                    String granted = authority.getAuthority();
                    if (applicationPermission.equals(granted)
                            || dashboardPermission.equals(granted)
                            || shellPermission.equals(granted)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
