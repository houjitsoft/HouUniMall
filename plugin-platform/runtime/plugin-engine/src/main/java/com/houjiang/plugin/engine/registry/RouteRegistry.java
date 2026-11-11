package com.houjiang.plugin.engine.registry;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 插件路由表：{@code (code, path, httpMethod) → (bean, Method, permission)}。
 *
 * <p>由加载流程扫描插件类上的 {@code @HouRoute} 填充；{@link com.houjiang.plugin.engine.core.PluginDispatcher}
 * 据此分发。卸载时按 code 整批清除。
 *
 * @author HouCloud
 */
public class RouteRegistry {

    public static class RouteEntry {
        public String code;
        public String path;
        public String httpMethod;
        public Object bean;
        public Method method;
        public String permission;

        /**
         * 允许的插件调用方白名单（{@code null} 或空 = 不做调用方限制）。
         *
         * <p>{@code "host"} 表示宿主侧（浏览器登录用户），{@code "*"} 表示任意插件。
         * 由 {@code @HouRoute(allowCallers = ...)} 填充，判定见
         * {@link com.houjiang.plugin.engine.core.PluginDispatcher}。
         */
        public java.util.Set<String> allowCallers;

        /**
         * 是否允许游客（未登录）调用，由 {@code @HouRoute(anonymous = true)} 填充。
         *
         * <p>默认 {@code false}：所有插件路由都要求登录态（历史行为）。
         * 置 {@code true} 后分发器跳过登录校验与权限校验，只用于纯只读展示接口。
         */
        public boolean anonymous;
    }

    /**
     * Writes are rare (plugin lifecycle events), while lookups happen on every
     * plugin request. Keep a small mutable index for writes and publish an
     * immutable read snapshot after each change.
     */
    private final Map<String, RouteEntry> mutableRoutes = new HashMap<>();
    private volatile RouteSnapshot snapshot = RouteSnapshot.empty();

    public synchronized void register(RouteEntry entry) {
        if (entry == null || entry.code == null || entry.path == null || entry.httpMethod == null) {
            throw new IllegalArgumentException("route entry/code/path/httpMethod must not be null");
        }
        mutableRoutes.put(key(entry.code, entry.path, entry.httpMethod), copy(entry));
        publishSnapshot();
    }

    public RouteEntry lookup(String code, String path, String httpMethod) {
        String method = httpMethod == null ? null : httpMethod.toUpperCase();
        String requestedPath = path == null ? "" : path;
        String normalizedPath = normalizeLookupPath(requestedPath);
        RouteSnapshot current = snapshot;
        RouteEntry exact = current.exact().get(key(code, requestedPath, method));
        if (exact == null && !normalizedPath.equals(requestedPath)) {
            exact = current.exact().get(key(code, normalizedPath, method));
        }
        if (exact == null && !normalizedPath.equals("/") && !normalizedPath.isEmpty()) {
            exact = current.exact().get(key(code, normalizedPath + "/", method));
        }
        if (exact != null) {
            return exact;
        }
        return current.templates().getOrDefault(key(code, "", method), List.of()).stream()
                .filter(entry -> entry.pattern().matcher(normalizedPath).matches())
                .findFirst()
                .map(CompiledRoute::entry)
                .orElse(null);
    }

    /**
     * Spring accepts both "/resource" and "/resource/" for an empty method
     * mapping, while the scanner intentionally preserves the latter in the
     * registry for backwards-compatible route metadata.  Normalize only for
     * lookup so either form reaches the same plugin handler without changing
     * scanner output or persisted route descriptions.
     */
    private static String normalizeLookupPath(String path) {
        if (path == null || path.isBlank()) {
            return "/";
        }
        String normalized = path.startsWith("/") ? path : "/" + path;
        while (normalized.length() > 1 && normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    public synchronized void removeByPlugin(String code) {
        if (code == null) {
            return;
        }
        mutableRoutes.values().removeIf(e -> code.equals(e.code));
        publishSnapshot();
    }

    private String key(String code, String path, String method) {
        return code + "#" + path + "#" + (method == null ? "" : method.toUpperCase());
    }

    private void publishSnapshot() {
        Map<String, RouteEntry> exact = new HashMap<>();
        Map<String, List<CompiledRoute>> templates = new HashMap<>();
        mutableRoutes.forEach((routeKey, entry) -> {
            exact.put(routeKey, entry);
            if (entry.path.contains("{")) {
                String templateKey = key(entry.code, "", entry.httpMethod);
                templates.computeIfAbsent(templateKey, ignored -> new ArrayList<>())
                        .add(new CompiledRoute(entry, compile(entry.path)));
            }
        });
        templates.values().forEach(routes -> routes.sort((left, right) ->
                Integer.compare(right.entry().path.length(), left.entry().path.length())));
        Map<String, List<CompiledRoute>> immutableTemplates = new HashMap<>();
        templates.forEach((key, value) -> immutableTemplates.put(key, List.copyOf(value)));
        snapshot = new RouteSnapshot(Map.copyOf(exact), Map.copyOf(immutableTemplates));
    }

    private Pattern compile(String template) {
        String regex = template.replaceAll("([\\.\\+\\(\\)\\[\\]\\{\\}\\^\\$\\|])", "\\\\$1")
                .replaceAll("\\\\\\{[^/]+\\\\\\}", "[^/]+");
        return Pattern.compile(regex);
    }

    private RouteEntry copy(RouteEntry source) {
        RouteEntry target = new RouteEntry();
        target.code = source.code;
        target.path = source.path;
        target.httpMethod = source.httpMethod.toUpperCase();
        target.bean = source.bean;
        target.method = source.method;
        target.permission = source.permission;
        target.allowCallers = source.allowCallers == null || source.allowCallers.isEmpty()
                ? null
                : java.util.Set.copyOf(source.allowCallers);
        target.anonymous = source.anonymous;
        return target;
    }

    private record CompiledRoute(RouteEntry entry, Pattern pattern) {
    }

    private record RouteSnapshot(Map<String, RouteEntry> exact,
                                 Map<String, List<CompiledRoute>> templates) {
        private static RouteSnapshot empty() {
            return new RouteSnapshot(Collections.emptyMap(), Collections.emptyMap());
        }
    }
}
