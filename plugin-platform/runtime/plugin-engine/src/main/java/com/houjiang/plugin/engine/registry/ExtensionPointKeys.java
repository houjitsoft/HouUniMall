package com.houjiang.plugin.engine.registry;

import java.util.Locale;

/**
 * 扩展点键工具（引擎内部统一约定，§7）。
 *
 * <p>扩展点短名：去掉接口名末尾的 {@code Extension}，如 {@code PriceCalcExtension} → {@code PriceCalc}。
 * 规范键（canonical）：短名去 {@code -_ 空格} 后转小写，使配置书写更宽容。
 * 实现键：{@code source#implClass}，用于运行期禁用 / 熔断摘除，精确到单个实现。
 *
 * @author HouCloud
 */
public final class ExtensionPointKeys {

    private ExtensionPointKeys() {
    }

    /** 扩展点短名：{@code PriceCalcExtension} → {@code PriceCalc}。 */
    public static String shortName(Class<?> type) {
        if (type == null) {
            return "";
        }
        String simpleName = type.getSimpleName();
        if (simpleName.endsWith("Extension") && simpleName.length() > "Extension".length()) {
            return simpleName.substring(0, simpleName.length() - "Extension".length());
        }
        return simpleName;
    }

    /** 规范键：去 {@code -_ 空格} 并转小写；{@code PriceCalc} / {@code price-calc} 等价。 */
    public static String canonical(String point) {
        if (point == null || point.isBlank()) {
            throw new IllegalArgumentException("扩展点短名不能为空");
        }
        String trimmed = point.trim();
        if (trimmed.endsWith("Extension")) {
            trimmed = trimmed.substring(0, trimmed.length() - "Extension".length());
        }
        StringBuilder sb = new StringBuilder(trimmed.length());
        for (int i = 0; i < trimmed.length(); i++) {
            char c = trimmed.charAt(i);
            if (c == '-' || c == '_' || c == ' ') {
                continue;
            }
            sb.append(Character.toLowerCase(c));
        }
        if (sb.length() == 0) {
            throw new IllegalArgumentException("扩展点短名不能为空");
        }
        return sb.toString();
    }

    /** 规范键（由扩展点 Class 计算）。 */
    public static String canonicalOf(Class<?> type) {
        return canonical(shortName(type));
    }

    /** 实现键：{@code source#implClass}。 */
    public static String implementationKey(String source, String implementationClass) {
        String src = source == null ? "" : source;
        String cls = implementationClass == null ? "" : implementationClass;
        return src + "#" + cls;
    }

    /** 实现键（由来源与实例计算）。 */
    public static String implementationKey(String source, Object instance) {
        return implementationKey(source, instance == null ? "" : instance.getClass().getName());
    }

    /** 取实现键中的来源段；无分隔符时返回原值。 */
    public static String sourceOf(String implementationKey) {
        if (implementationKey == null) {
            return "";
        }
        int idx = implementationKey.indexOf('#');
        return idx < 0 ? implementationKey : implementationKey.substring(0, idx);
    }

    /** 判断规范短名是否 {@code .toLowerCase} 前缀匹配（诊断用）。 */
    static boolean matchesLowerCase(String canonical, String candidate) {
        return candidate != null && canonical.equals(candidate.toLowerCase(Locale.ROOT));
    }
}
