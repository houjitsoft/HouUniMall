package com.houjiang.mall.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 装修组件 content 字段归一化：兼容历史库中 style/per_line 等旧字段名，
 * 输出 uniapp 组件期望的 image_type / type / show_nums 等字段。
 * @author Amos
 */
public final class DecorateContentNormalizer {

    private DecorateContentNormalizer() {
    }

    @SuppressWarnings("unchecked")
    public static List<Object> normalizePageData(List<Object> data) {
        if (data == null || data.isEmpty()) {
            return data;
        }
        List<Object> result = new ArrayList<>(data.size());
        for (Object item : data) {
            if (item instanceof List) {
                List<Object> group = new ArrayList<>();
                for (Object sub : (List<Object>) item) {
                    if (sub instanceof Map) {
                        group.add(normalizeComponent((Map<String, Object>) sub));
                    } else {
                        group.add(sub);
                    }
                }
                result.add(group);
            } else if (item instanceof Map) {
                result.add(normalizeComponent((Map<String, Object>) item));
            } else {
                result.add(item);
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> normalizeComponent(Map<String, Object> item) {
        if (item == null) {
            return new LinkedHashMap<>();
        }
        Map<String, Object> copy = new LinkedHashMap<>(item);
        Object contentObj = copy.get("content");
        if (!(contentObj instanceof Map)) {
            return copy;
        }
        Map<String, Object> content = new LinkedHashMap<>((Map<String, Object>) contentObj);
        String name = String.valueOf(copy.getOrDefault("name", ""));

        switch (name) {
            case "banner" -> normalizeBanner(content);
            case "nav" -> normalizeNav(content);
            case "search" -> normalizeSearch(content);
            case "middle-banner", "user-banner", "hot-banner" -> ensureEnabled(content);
            default -> { /* no-op */ }
        }
        copy.put("content", content);
        return copy;
    }

    private static void normalizeBanner(Map<String, Object> content) {
        ensureEnabled(content);
        if (!content.containsKey("image_type")) {
            Object style = content.get("style");
            content.put("image_type", style != null ? toInt(style, 1) : 1);
        }
    }

    private static void normalizeNav(Map<String, Object> content) {
        ensureEnabled(content);
        if (!content.containsKey("type")) {
            content.put("type", 1);
        }
        if (!content.containsKey("show_type")) {
            content.put("show_type", 1);
        }
        if (!content.containsKey("show_nums")) {
            int perLine = toInt(content.get("per_line"), 5);
            content.put("show_nums", perLine <= 3 ? 1 : perLine == 4 ? 2 : 3);
        }
        if (!content.containsKey("show_rows")) {
            content.put("show_rows", toInt(content.get("show_line"), 2));
        }
    }

    private static void normalizeSearch(Map<String, Object> content) {
        if (content.isEmpty()) {
            content.put("enabled", 1);
            content.put("placeholder", "请输入关键词");
        }
    }

    private static void ensureEnabled(Map<String, Object> content) {
        if (!content.containsKey("enabled")) {
            content.put("enabled", 1);
        }
    }

    private static int toInt(Object value, int defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
