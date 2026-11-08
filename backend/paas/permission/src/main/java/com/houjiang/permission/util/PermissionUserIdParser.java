package com.houjiang.permission.util;

/**
 * 解析会员 userId（hou_user_info.user_id 为雪花 Long，禁止经 JSON Number 中转以免精度丢失）
 * @author Amos
 */
public final class PermissionUserIdParser {

    private PermissionUserIdParser() {
    }

    public static Long parse(Object userIdObj) {
        if (userIdObj == null) {
            return null;
        }
        String raw = userIdObj.toString().trim();
        if (raw.isEmpty()) {
            return null;
        }
        return Long.parseLong(raw);
    }
}
