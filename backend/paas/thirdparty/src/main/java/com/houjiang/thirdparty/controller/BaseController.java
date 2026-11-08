package com.houjiang.thirdparty.controller;

import com.houjiang.commons.util.SecurityContextUtils;
import com.houjiang.commons.vo.user.UserInfoVO;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 基础Controller
 * 
 * 【重要说明】统一使用SecurityContextUtils获取用户信息
 * 1. JWT过滤器已经验证了token并设置了SecurityContext
 * 2. 直接使用SecurityContextUtils.getCurrentUser()获取当前用户
 * 3. 不要手动验证token或调用ParseTokenUtils
 * 
 * @author Amos
 * @since 2025-01-07
 */
public class BaseController {

    /**
     * 从请求头中获取token（保留用于特殊情况）
     *
     * @return token字符串
     */
    public String getTokenFromRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String authorization = request.getHeader("Authorization");
            if (authorization != null && authorization.startsWith("Bearer ")) {
                return authorization.substring(7);
            }
        }
        return null;
    }

    /**
     * 获取当前登录用户信息
     * 【推荐使用】统一使用SecurityContextUtils，JWT过滤器已经验证了token
     *
     * @return 当前登录用户信息，如果未登录返回null
     */
    public UserInfoVO getCurrentUser() {
        return SecurityContextUtils.getCurrentUser();
    }

    /**
     * 验证token并获取用户信息（已废弃，请使用getCurrentUser）
     * 
     * @deprecated 请使用 getCurrentUser() 方法，JWT过滤器已经验证了token
     * @return 用户信息
     */
    @Deprecated
    public UserInfoVO validateTokenAndGetUser() {
        UserInfoVO user = getCurrentUser();
        if (user == null) {
            throw new RuntimeException("用户未登录");
        }
        return user;
    }
}
