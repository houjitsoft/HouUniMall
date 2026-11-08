package com.houjiang.payment.util;

import com.houjiang.commons.util.ParseTokenUtils;
import com.houjiang.commons.vo.user.UserInfoVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

/**
 * 支付 api 子包共享的 token 工具
 * 从 Header Authorization / token 中提取当前登录用户 userId
 *
 * @author Amos
 */
@Slf4j
public final class ApiTokenUtils {

    private ApiTokenUtils() {}

    /**
     * 从请求中提取 userId，无法解析时返回 null（不抛异常）
     */
    public static String resolveUserId(HttpServletRequest http) {
        if (http == null) {
            return null;
        }
        String token = http.getHeader("Authorization");
        if (!StringUtils.hasText(token)) {
            token = http.getHeader("token");
        }
        if (!StringUtils.hasText(token)) {
            return null;
        }
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        try {
            UserInfoVO user = ParseTokenUtils.getUserInfoByToken(token);
            return user == null ? null : user.getUserId();
        } catch (Exception e) {
            log.warn("[api-token] 解析 token 失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从请求中提取 userId，失败抛 IllegalStateException
     */
    public static String requireUserId(HttpServletRequest http) {
        String userId = resolveUserId(http);
        if (!StringUtils.hasText(userId)) {
            throw new IllegalStateException("缺少有效的登录凭证");
        }
        return userId;
    }
}
