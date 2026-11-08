package com.houjiang.mall.util;

import org.springframework.util.StringUtils;

/**
 * Mall 管理员 Token 工具
 * @author Amos
 */
public final class MallAdminTokenHelper {

    private MallAdminTokenHelper() {
    }

    public static String normalize(String token) {
        if (!StringUtils.hasText(token)) {
            return token;
        }
        String value = token.trim();
        if (value.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return value.substring(7).trim();
        }
        return value;
    }
}
