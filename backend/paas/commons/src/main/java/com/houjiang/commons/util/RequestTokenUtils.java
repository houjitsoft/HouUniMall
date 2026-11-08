package com.houjiang.commons.util;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 请求Token工具类
 * 用于从HttpServletRequest中获取Authorization header并解析用户信息
 * 
 * @author Amos
 * @date 2025-01-06
 */
public class RequestTokenUtils {

    /**
     * 从当前请求的Authorization header中解析出userId
     * 
     * @return userId 如果解析成功，否则返回null
     */
    public static String getUserIdFromRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String authorization = request.getHeader("Authorization");
                if (authorization != null && authorization.startsWith("Bearer ")) {
                    String token = authorization.substring(7);
                    Claims claims = JwtUtil.parseJWT(token);
                    return claims.getSubject();
                }
            }
        } catch (Exception e) {
            // token解析失败，降级到参数获取
        }
        return null;
    }

    /**
     * 从当前请求的Authorization header中获取完整的token
     * 
     * @return token 如果存在，否则返回null
     */
    public static String getTokenFromRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String authorization = request.getHeader("Authorization");
                if (authorization != null && authorization.startsWith("Bearer ")) {
                    return authorization.substring(7);
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    /**
     * 从指定的HttpServletRequest中解析出userId
     * 
     * @param request HttpServletRequest对象
     * @return userId 如果解析成功，否则返回null
     */
    public static String getUserIdFromRequest(HttpServletRequest request) {
        try {
            String authorization = request.getHeader("Authorization");
            if (authorization != null && authorization.startsWith("Bearer ")) {
                String token = authorization.substring(7);
                Claims claims = JwtUtil.parseJWT(token);
                return claims.getSubject();
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    /**
     * 从指定的HttpServletRequest中获取完整的token
     * 
     * @param request HttpServletRequest对象
     * @return token 如果存在，否则返回null
     */
    public static String getTokenFromRequest(HttpServletRequest request) {
        try {
            String authorization = request.getHeader("Authorization");
            if (authorization != null && authorization.startsWith("Bearer ")) {
                return authorization.substring(7);
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }
}
