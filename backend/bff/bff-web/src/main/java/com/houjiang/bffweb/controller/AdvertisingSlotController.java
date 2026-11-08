package com.houjiang.bffweb.controller;

import com.houjiang.bffweb.service.PluginApiClient;
import com.houjiang.bffweb.service.PluginApiClient.PluginCallResult;
import com.houjiang.bffweb.util.ResponseResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/web/advertising")
public class AdvertisingSlotController {

    private static final Pattern SLOT_CODE = Pattern.compile("[a-z0-9]+(?:-[a-z0-9]+)*");
    private final PluginApiClient pluginApiClient;

    public AdvertisingSlotController(PluginApiClient pluginApiClient) {
        this.pluginApiClient = pluginApiClient;
    }

    @GetMapping("/slots/{code}")
    public ResponseResult<List<AdvertisingSlotItem>> slot(@PathVariable String code) {
        String normalized = code == null ? "" : code.trim().toLowerCase(Locale.ROOT);
        if (!SLOT_CODE.matcher(normalized).matches()) {
            return ResponseResult.fail(400, "Invalid advertising slot code", List.of());
        }
        PluginCallResult upstream = pluginApiClient.internalGet(
                "mall-advertising", "/public/slots/" + normalized);
        if (upstream.status() != 200) {
            return ResponseResult.success(List.of());
        }
        return ResponseResult.success(sanitize(upstream.body()));
    }

    private static List<AdvertisingSlotItem> sanitize(Object body) {
        if (!(body instanceof List<?> list)) {
            return List.of();
        }
        List<AdvertisingSlotItem> result = new ArrayList<>();
        for (Object raw : list) {
            if (!(raw instanceof Map<?, ?> item)) continue;
            Long id = longValue(item.get("id"));
            String title = text(item.get("title"), 120);
            String imageUrl = safeImage(item.get("imageUrl"));
            if (id == null || imageUrl == null) continue;
            result.add(new AdvertisingSlotItem(id, title, imageUrl, safeTarget(item.get("targetUrl")),
                    text(item.get("startTime"), 40), text(item.get("endTime"), 40)));
        }
        return List.copyOf(result);
    }

    private static Long longValue(Object value) {
        if (value instanceof Number number) return number.longValue();
        if (value instanceof String text) {
            try {
                return Long.valueOf(text);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private static String text(Object value, int maxLength) {
        if (!(value instanceof String string)) return null;
        String normalized = string.trim();
        return normalized.isEmpty() || normalized.length() > maxLength ? null : normalized;
    }

    private static String safeImage(Object value) {
        String url = text(value, 2048);
        if (url == null || url.startsWith("//")) return null;
        if (url.startsWith("/")) return safeSitePath(url) ? url : null;
        return safeAbsolute(url, true) ? url : null;
    }

    private static String safeTarget(Object value) {
        String url = text(value, 2048);
        if (url == null || url.startsWith("//")) return null;
        if (url.startsWith("/")) return safeSitePath(url) ? url : null;
        return safeAbsolute(url, false) ? url : null;
    }

    private static boolean safeSitePath(String value) {
        try {
            URI uri = new URI(value);
            String path = uri.getPath();
            return path != null && !path.contains("/../") && !path.endsWith("/..")
                    && !path.contains("/./") && !path.endsWith("/.");
        } catch (URISyntaxException exception) {
            return false;
        }
    }

    private static boolean safeAbsolute(String value, boolean httpsOnly) {
        try {
            URI uri = new URI(value);
            String scheme = uri.getScheme();
            return uri.getHost() != null && ("https".equalsIgnoreCase(scheme)
                    || (!httpsOnly && "http".equalsIgnoreCase(scheme)));
        } catch (URISyntaxException exception) {
            return false;
        }
    }

    public record AdvertisingSlotItem(Long id, String title, String imageUrl, String targetUrl,
                                      String startTime, String endTime) {
    }
}
