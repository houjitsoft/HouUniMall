package com.houjiang.aiagent.support;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 分页与参数解析工具。
 *
 * <p>前端统一读取 {@code res.data.list} / {@code res.data.total}，因此分页结果
 * 必须固定为 {@code {list, total, page, limit}} 四键，不能换成 MyBatis-Plus 的 IPage 原样序列化
 * （IPage 序列化出来是 records，前端取不到 list）。
 */
public final class AiPageSupport {

    private AiPageSupport() {
    }

    public static long pageNo(Map<String, Object> params) {
        return toLong(params, "page", 1L);
    }

    public static long pageSize(Map<String, Object> params) {
        long size = toLong(params, "limit", 20L);
        if (size <= 0) {
            size = 20L;
        }
        return Math.min(size, 200L);
    }

    public static <T> Page<T> page(Map<String, Object> params) {
        return new Page<>(pageNo(params), pageSize(params));
    }

    public static Map<String, Object> pageResult(IPage<?> page, List<?> list) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", list);
        result.put("total", page.getTotal());
        result.put("page", page.getCurrent());
        result.put("limit", page.getSize());
        return result;
    }

    public static String str(Map<String, Object> params, String key) {
        Object value = params == null ? null : params.get(key);
        return value == null ? null : String.valueOf(value).trim();
    }

    public static String str(Map<String, Object> params, String key, String fallback) {
        String value = str(params, key);
        return value == null || value.isEmpty() ? fallback : value;
    }

    public static Long toLong(Map<String, Object> params, String key) {
        Object value = params == null ? null : params.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        String text = String.valueOf(value).trim();
        if (text.isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static long toLong(Map<String, Object> params, String key, long fallback) {
        Long value = toLong(params, key);
        return value == null ? fallback : value;
    }

    public static Integer toInt(Map<String, Object> params, String key) {
        Long value = toLong(params, key);
        return value == null ? null : value.intValue();
    }
}
