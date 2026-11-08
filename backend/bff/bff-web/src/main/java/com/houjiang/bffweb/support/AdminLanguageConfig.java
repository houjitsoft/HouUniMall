package com.houjiang.bffweb.support;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 后台语言配置。可选语言与前端 adminLanguageCatalog 保持同一份编码。
 */
public final class AdminLanguageConfig {

    public static final String CONFIG_KEY = "admin_language_config";

    private static final List<String> CATALOG = List.of(
            "zh-CN", "zh-TW", "en", "ja", "ko", "fr", "de", "es", "ru", "ar", "ms"
    );
    private static final List<String> DEFAULT_SUPPORTED = List.of("zh-CN", "en");
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private AdminLanguageConfig() {
    }

    public static Map<String, Object> defaults() {
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("systemLanguage", "zh-CN");
        config.put("defaultLanguage", "zh-CN");
        config.put("multiLanguage", true);
        config.put("autoDetect", true);
        config.put("supportedLanguages", new ArrayList<>(DEFAULT_SUPPORTED));
        config.put("rtlSupport", false);
        return config;
    }

    public static Map<String, Object> parseStored(String json) {
        if (json == null || json.isBlank()) {
            return defaults();
        }
        try {
            Map<String, Object> raw = MAPPER.readValue(json, MAP_TYPE);
            Map<String, Object> normalized = normalize(raw, true);
            return normalized == null ? defaults() : normalized;
        } catch (Exception ignored) {
            return defaults();
        }
    }

    public static String toJson(Map<String, Object> config) {
        try {
            return MAPPER.writeValueAsString(config);
        } catch (Exception exception) {
            throw new IllegalStateException("语言配置无法序列化", exception);
        }
    }

    /**
     * @return 规范化后的配置；显式传入的语言都不合法时返回 null
     */
    public static Map<String, Object> normalize(Map<String, Object> raw) {
        return normalize(raw, false);
    }

    private static Map<String, Object> normalize(Map<String, Object> raw, boolean fillWhenMissing) {
        Map<String, Object> source = raw == null ? Map.of() : raw;
        boolean explicit = source.containsKey("supportedLanguages");
        List<String> supported = ordered(source.get("supportedLanguages"), !explicit && fillWhenMissing);
        if (supported.isEmpty()) {
            if (fillWhenMissing && !explicit) {
                return defaults();
            }
            return null;
        }

        String systemLanguage = choose(source.get("systemLanguage"), supported);
        String defaultLanguage = choose(source.get("defaultLanguage"), supported);
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("systemLanguage", systemLanguage);
        config.put("defaultLanguage", defaultLanguage);
        config.put("multiLanguage", asBoolean(source.get("multiLanguage"), true));
        config.put("autoDetect", asBoolean(source.get("autoDetect"), true));
        config.put("supportedLanguages", supported);
        config.put("rtlSupport", asBoolean(source.get("rtlSupport"), false));
        return config;
    }

    private static String choose(Object value, List<String> supported) {
        String code = normalizeCode(value);
        if (code != null && supported.contains(code)) {
            return code;
        }
        return supported.get(0);
    }

    private static List<String> ordered(Object raw, boolean useDefaultWhenEmpty) {
        List<String> picked = new ArrayList<>();
        if (raw instanceof Collection<?> values) {
            for (Object value : values) {
                String code = normalizeCode(value);
                if (code != null && !picked.contains(code)) {
                    picked.add(code);
                }
            }
        } else if (useDefaultWhenEmpty) {
            picked.addAll(DEFAULT_SUPPORTED);
        }
        if (picked.isEmpty() && useDefaultWhenEmpty) {
            picked.addAll(DEFAULT_SUPPORTED);
        }
        List<String> ordered = new ArrayList<>();
        for (String code : CATALOG) {
            if (picked.contains(code)) {
                ordered.add(code);
            }
        }
        return ordered;
    }

    static String normalizeCode(Object value) {
        if (value == null) {
            return null;
        }
        String key = value.toString().trim().toLowerCase(Locale.ROOT).replace('_', '-');
        if (key.isEmpty()) {
            return null;
        }
        return switch (key) {
            case "zh", "zh-cn", "zh-hans" -> "zh-CN";
            case "zh-tw", "zh-hk", "zh-hant" -> "zh-TW";
            case "en", "en-us", "en-gb" -> "en";
            case "ja", "ja-jp" -> "ja";
            case "ko", "ko-kr" -> "ko";
            case "fr", "fr-fr" -> "fr";
            case "de", "de-de" -> "de";
            case "es", "es-es", "es-mx" -> "es";
            case "ru", "ru-ru" -> "ru";
            case "ar", "ar-sa" -> "ar";
            case "ms", "ms-my" -> "ms";
            default -> prefix(key);
        };
    }

    private static String prefix(String key) {
        if (key.startsWith("zh-tw") || key.startsWith("zh-hk") || key.startsWith("zh-hant")) {
            return "zh-TW";
        }
        if (key.equals("zh") || key.startsWith("zh-")) {
            return "zh-CN";
        }
        String head = key.split("-", 2)[0];
        return switch (head) {
            case "en" -> "en";
            case "ja" -> "ja";
            case "ko" -> "ko";
            case "fr" -> "fr";
            case "de" -> "de";
            case "es" -> "es";
            case "ru" -> "ru";
            case "ar" -> "ar";
            case "ms" -> "ms";
            default -> null;
        };
    }

    private static boolean asBoolean(Object value, boolean fallback) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value == null) {
            return fallback;
        }
        String text = value.toString().trim().toLowerCase(Locale.ROOT);
        if (text.isEmpty()) {
            return fallback;
        }
        if ("1".equals(text) || "true".equals(text) || "yes".equals(text)) {
            return true;
        }
        if ("0".equals(text) || "false".equals(text) || "no".equals(text)) {
            return false;
        }
        return fallback;
    }
}
