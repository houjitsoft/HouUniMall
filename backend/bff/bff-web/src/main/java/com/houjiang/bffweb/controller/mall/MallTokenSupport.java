package com.houjiang.bffweb.controller.mall;

import jakarta.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Mall商城前台接口 token 注入工具
 *
 * paas 的 order / userms 等服务通过请求体 map.get("token") 取 token
 * （ParseTokenUtils.getUserInfoByToken），而前端把 token 放在
 * Authorization: Bearer xxx / token 请求头里，因此 BFF 转发前需要统一注入。
 *
 * @author Amos
 */
public final class MallTokenSupport {

    private MallTokenSupport() {
    }

    /**
     * 从请求头解析 token：优先 Authorization（剥 Bearer 前缀），其次 token 头
     */
    public static String resolveToken(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String authorization = request.getHeader("Authorization");
        if (authorization != null && !authorization.isBlank()) {
            return authorization.startsWith("Bearer ") ? authorization.substring(7) : authorization;
        }
        String token = request.getHeader("token");
        return (token != null && !token.isBlank()) ? token : null;
    }

    /**
     * 若请求体中没有 token，则从请求头解析并注入；已有 token 时保持不动
     */
    public static Map<String, Object> withToken(Map<String, Object> param, HttpServletRequest request) {
        if (param == null) {
            param = new HashMap<>();
        }
        Object existing = param.get("token");
        if (existing == null || existing.toString().isBlank()) {
            String token = resolveToken(request);
            if (token != null) {
                param.put("token", token);
            }
        }
        return param;
    }

    /**
     * 字段别名映射：下游期望 targetKey，前端传的是 sourceKey 时补一份
     * （不覆盖已有 targetKey）
     */
    public static void putIfAbsentFrom(Map<String, Object> param, String targetKey, String sourceKey) {
        if (param.get(targetKey) == null && param.get(sourceKey) != null) {
            param.put(targetKey, param.get(sourceKey));
        }
    }
}
