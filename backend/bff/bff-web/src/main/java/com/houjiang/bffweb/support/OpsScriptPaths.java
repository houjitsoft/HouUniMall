package com.houjiang.bffweb.support;

import java.io.File;
import java.util.Locale;

/**
 * 运维脚本路径解析：避免 Linux 上把 {@code D:/wwwroot/...} 当成相对路径，
/**
 * 拼成 {@code /www/wwwroot/houcloud/D:/wwwroot/...}。
 * @author Amos
 */
public final class OpsScriptPaths {

    public static final String WINDOWS_PROJECT_ROOT = "D:/wwwroot/houcloud";
    public static final String LINUX_PROJECT_ROOT = "/www/wwwroot/houcloud";

    private OpsScriptPaths() {
    }

    public static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
    }

    /**
     * @param configuredPath 配置项（可为空、Linux 绝对路径或 Windows 盘符路径）
/**
     * @param relativePath   相对项目根的路径，如 {@code jar/service-manage.ps1}
     */
    public static String resolve(String configuredPath, String relativePath) {
        String relative = normalizeSlashes(relativePath).replaceFirst("^/+", "");
        String fallback = projectRoot() + "/" + relative;
        if (configuredPath == null || configuredPath.isBlank()) {
            return fallback;
        }

        String raw = normalizeSlashes(configuredPath.trim());
        if (isWindows()) {
            if (isAbsolutePath(raw)) {
                return raw;
            }
            return new File(System.getProperty("user.dir"), raw).getAbsolutePath();
        }

        if (looksLikeWindowsDrivePath(raw)) {
            raw = mapWindowsProjectPathToLinux(raw);
        }
        if (isAbsolutePath(raw)) {
            return raw;
        }
        return new File(System.getProperty("user.dir"), raw).getAbsolutePath();
    }

    public static String projectRoot() {
        return isWindows() ? WINDOWS_PROJECT_ROOT : LINUX_PROJECT_ROOT;
    }

    static String mapWindowsProjectPathToLinux(String windowsPath) {
        String normalized = normalizeSlashes(windowsPath);
        String lower = normalized.toLowerCase(Locale.ROOT);
        String winRoot = normalizeSlashes(WINDOWS_PROJECT_ROOT).toLowerCase(Locale.ROOT);
        if (lower.startsWith(winRoot)) {
            return LINUX_PROJECT_ROOT + normalized.substring(winRoot.length());
        }
        if (lower.matches("^[a-z]:/wwwroot/houcloud.*")) {
            int colon = normalized.indexOf(':');
            return LINUX_PROJECT_ROOT + normalized.substring(colon + "/wwwroot/houcloud".length() + 1);
        }
        return normalized;
    }

    static boolean looksLikeWindowsDrivePath(String path) {
        return path != null && path.matches("(?i)^[a-z]:/.*");
    }

    static boolean isAbsolutePath(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }
        if (path.startsWith("/")) {
            return true;
        }
        if (looksLikeWindowsDrivePath(path)) {
            return true;
        }
        return path.startsWith("\\\\");
    }

    private static String normalizeSlashes(String path) {
        return path.replace('\\', '/');
    }
}
