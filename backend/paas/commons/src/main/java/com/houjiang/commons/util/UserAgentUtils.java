package com.houjiang.commons.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User-Agent 解析工具：提取设备类型、浏览器与操作系统。
 *
 * <p>仅做轻量正则匹配，不引入第三方解析库；解析不出时回退为「未知」，
 * 保证同意记录落库不因 UA 异常而失败。</p>
 *
 * @author Amos
 */
public class UserAgentUtils {

    /** 设备类型：电脑 */
    public static final String DEVICE_PC = "PC";
    /** 设备类型：手机 */
    public static final String DEVICE_MOBILE = "Mobile";
    /** 设备类型：平板 */
    public static final String DEVICE_TABLET = "Tablet";

    /** 未知 */
    private static final String UNKNOWN = "未知";

    private static final Pattern BROWSER_PATTERNS_EDGE = Pattern.compile("Edg(?:e|A|iOS)?/([\\d.]+)");
    private static final Pattern BROWSER_PATTERNS_CHROME = Pattern.compile("(?:Chrome|CriOS)/([\\d.]+)");
    private static final Pattern BROWSER_PATTERNS_FIREFOX = Pattern.compile("(?:Firefox|FxiOS)/([\\d.]+)");
    private static final Pattern BROWSER_PATTERNS_SAFARI = Pattern.compile("Version/([\\d.]+).*Safari");
    private static final Pattern BROWSER_PATTERNS_IE = Pattern.compile("(?:MSIE |Trident.*rv:)([\\d.]+)");

    private static final Pattern OS_WINDOWS = Pattern.compile("Windows NT ([\\d.]+)");
    private static final Pattern OS_MAC = Pattern.compile("Mac OS X ([\\d_.]+)");
    private static final Pattern OS_ANDROID = Pattern.compile("Android\\s*([\\d.]+)?");
    private static final Pattern OS_IOS = Pattern.compile("(?:iPhone|iPad|iPod).*OS\\s*([\\d_]+)");
    private static final Pattern OS_HARMONY = Pattern.compile("HarmonyOS|OpenHarmony");
    private static final Pattern OS_LINUX = Pattern.compile("Linux");

    private UserAgentUtils() {
    }

    /**
     * 解析 UA，返回 device / browser / os 三个字段。
     *
     * @param userAgent User-Agent 原文，可为空
     * @return 解析结果，key 固定为 device、browser、os
     */
    public static Map<String, String> parse(String userAgent) {
        Map<String, String> result = new LinkedHashMap<>(4);
        if (userAgent == null || userAgent.trim().isEmpty()) {
            result.put("device", UNKNOWN);
            result.put("browser", UNKNOWN);
            result.put("os", UNKNOWN);
            return result;
        }
        String ua = userAgent.trim();
        result.put("device", resolveDevice(ua));
        result.put("browser", resolveBrowser(ua));
        result.put("os", resolveOs(ua));
        return result;
    }

    /**
     * 设备类型判定
     */
    public static String resolveDevice(String ua) {
        if (ua == null || ua.isEmpty()) {
            return UNKNOWN;
        }
        String lower = ua.toLowerCase();
        // 平板优先判定：iPad 及 Android 平板（无 Mobile 标识）
        if (lower.contains("ipad") || lower.contains("tablet") || lower.contains("playbook")
                || lower.contains("kindle") || (lower.contains("android") && !lower.contains("mobile"))) {
            return DEVICE_TABLET;
        }
        if (lower.contains("mobile") || lower.contains("iphone") || lower.contains("ipod")
                || lower.contains("android") || lower.contains("windows phone")) {
            return DEVICE_MOBILE;
        }
        return DEVICE_PC;
    }

    /**
     * 浏览器名称及主版本号，如 Chrome 128
     */
    public static String resolveBrowser(String ua) {
        if (ua == null || ua.isEmpty()) {
            return UNKNOWN;
        }
        if (BROWSER_PATTERNS_EDGE.matcher(ua).find()) {
            return withVersion("Edge", BROWSER_PATTERNS_EDGE, ua);
        }
        if (ua.contains("OPR/") || ua.contains("Opera")) {
            return "Opera";
        }
        if (ua.contains("MicroMessenger")) {
            return "微信内置浏览器";
        }
        if (ua.contains("QQBrowser")) {
            return "QQ浏览器";
        }
        if (ua.contains("UCBrowser")) {
            return "UC浏览器";
        }
        if (ua.contains("TheWorld")) {
            return "世界之窗";
        }
        if (BROWSER_PATTERNS_CHROME.matcher(ua).find()) {
            return withVersion("Chrome", BROWSER_PATTERNS_CHROME, ua);
        }
        if (BROWSER_PATTERNS_FIREFOX.matcher(ua).find()) {
            return withVersion("Firefox", BROWSER_PATTERNS_FIREFOX, ua);
        }
        if (BROWSER_PATTERNS_SAFARI.matcher(ua).find()) {
            return withVersion("Safari", BROWSER_PATTERNS_SAFARI, ua);
        }
        if (BROWSER_PATTERNS_IE.matcher(ua).find()) {
            return withVersion("IE", BROWSER_PATTERNS_IE, ua);
        }
        if (ua.contains("curl") || ua.contains("python") || ua.contains("Java")) {
            return "程序客户端";
        }
        return UNKNOWN;
    }

    /**
     * 操作系统名称及版本
     */
    public static String resolveOs(String ua) {
        if (ua == null || ua.isEmpty()) {
            return UNKNOWN;
        }
        if (OS_HARMONY.matcher(ua).find()) {
            return "HarmonyOS";
        }
        Matcher iphone = OS_IOS.matcher(ua);
        if (iphone.find()) {
            return "iOS " + normalizeVersion(iphone.group(1));
        }
        Matcher android = OS_ANDROID.matcher(ua);
        if (android.find()) {
            String v = android.group(1);
            return v == null || v.isEmpty() ? "Android" : "Android " + v;
        }
        Matcher windows = OS_WINDOWS.matcher(ua);
        if (windows.find()) {
            return "Windows " + mapWindowsVersion(windows.group(1));
        }
        if (ua.contains("Windows")) {
            return "Windows";
        }
        Matcher mac = OS_MAC.matcher(ua);
        if (mac.find()) {
            return "macOS " + normalizeVersion(mac.group(1));
        }
        if (ua.contains("Macintosh")) {
            return "macOS";
        }
        if (OS_LINUX.matcher(ua).find()) {
            return "Linux";
        }
        return UNKNOWN;
    }

    /**
     * 仅取主版本号，避免记录过细的浏览器小版本
     */
    private static String withVersion(String name, Pattern pattern, String ua) {
        Matcher m = pattern.matcher(ua);
        if (m.find()) {
            String version = m.group(1);
            int dot = version.indexOf('.');
            return dot > 0 ? name + " " + version.substring(0, dot) : name + " " + version;
        }
        return name;
    }

    /**
     * Windows NT 版本号 → 通行版本名
     */
    private static String mapWindowsVersion(String nt) {
        switch (nt) {
            case "10.0":
                return "10/11";
            case "6.3":
                return "8.1";
            case "6.2":
                return "8";
            case "6.1":
                return "7";
            case "6.0":
                return "Vista";
            case "5.1":
                return "XP";
            default:
                return nt;
        }
    }

    private static String normalizeVersion(String raw) {
        return raw == null ? "" : raw.replace('_', '.');
    }
}
