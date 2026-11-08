package com.houjiang.commons.version;

/**
 * 框架版本号比较（宽松口径）。
 *
 * <p><b>为什么不用发布侧的 {@code PluginVersionComparator}</b>：
 * 那个类是<b>严格 SemVer</b>（三段数字 + 可选预发布），用于「发布时拒绝脏版本号」；
 * 而这里是运行时兼容性判定，面对的是历史数据——库里存在 {@code 1.8}、{@code 2.0}
 * 这类两段版本号，严格解析会直接抛异常，把「能装」判成「不能装」。
 * 因此本类按「逐段比较：都是数字比数值，否则比字典序，缺位补 0」的宽松规则处理，
 * 既能识别 {@code 1.8 < 2.0}，也不会因为格式不严谨把可安装的插件拦在门外。
 *
 * <p><b>为什么放在 commons</b>：M3 时它只被安装链路（plugin-update）使用；M5 的目录
 * 「有更新」判定（plugin-market）也要用同一条 {@code min_framework_version} 规则，
 * 而 market <b>不能</b>依赖 update（依赖方向是 {@code plugin-update → plugin-market}，反向成环）。
 * 各留一份宽松比较器的后果是「预检说兼容、目录说被拦」，与 SemVer 比较器的分裂是同一类事故，
 * 因此把两个口径的比较器并排放在 commons，各自唯一。
 *
 * <p><b>与 {@link PluginVersionComparator} 的分工不要混淆</b>：
 * 那一个是严格 SemVer（发布时拒绝脏版本号 + 目录判定「有更新」），
 * 这一个是宽松口径（运行时兼容性判定，容忍 {@code 1.8} 这类历史两段版本号）。
 *
 * @author Amos
 * @since M3
 */
public final class VersionComparison {

    /**
     * 版本段分隔符（点、短横线、加号都拆，覆盖 {@code 1.0.0-rc.1} / {@code 1.0.0+build}）。
     */
    private static final String SEGMENT_SPLIT = "[.\\-+]";

    /**
     * 私有构造，工具类不允许实例化。
     */
    private VersionComparison() {
    }

    /**
     * 判断 {@code current} 是否满足 {@code required}（即 current &gt;= required）。
     *
     * <p>{@code required} 为空表示插件未声明最低框架版本，按「不限制」放行——
     * 与依赖段的弱校验口径一致：声明缺失不该让已上架的存量插件整体装不上。
     *
     * @param current  宿主框架版本
     * @param required 插件要求的最低框架版本，可为 null / 空白
     * @return true 表示满足
     */
    public static boolean atLeast(String current, String required) {
        if (required == null || required.isBlank()) {
            return true;
        }
        String currentVersion = current == null ? "" : current.trim();
        if (currentVersion.isEmpty()) {
            // 宿主版本未知时无法自证兼容，按不兼容处理（fail-closed）
            return false;
        }
        return compare(currentVersion, required.trim()) >= 0;
    }

    /**
     * 比较两个版本号。
     *
     * @param left  左侧版本
     * @param right 右侧版本
     * @return 负数表示 left &lt; right，0 表示相等，正数表示 left &gt; right
     */
    public static int compare(String left, String right) {
        String[] leftSegments = left.trim().split(SEGMENT_SPLIT);
        String[] rightSegments = right.trim().split(SEGMENT_SPLIT);
        int max = Math.max(leftSegments.length, rightSegments.length);
        for (int i = 0; i < max; i++) {
            boolean leftMissing = i >= leftSegments.length;
            boolean rightMissing = i >= rightSegments.length;
            if (leftMissing && rightMissing) {
                break;
            }
            String leftSegment = leftMissing ? "0" : leftSegments[i];
            String rightSegment = rightMissing ? "0" : rightSegments[i];
            // 段数不等且多出来的是非数字段（如 -rc.1）：多出来的那侧是预发布版，正式版更高。
            // 直接补 0 比较会得出「1.0.0 < 1.0.0-rc.1」的相反结论（0 是数字，排在字母前）。
            if (leftMissing) {
                return isNumeric(rightSegment) ? compareSegment("0", rightSegment) : 1;
            }
            if (rightMissing) {
                return isNumeric(leftSegment) ? compareSegment(leftSegment, "0") : -1;
            }
            int result = compareSegment(leftSegment, rightSegment);
            if (result != 0) {
                return result;
            }
        }
        return 0;
    }

    /**
     * 比较单个版本段：都是纯数字比数值，否则按字典序（数字段排在字母段之前，
     * 与 {@code 1.0.0-rc.1 < 1.0.0} 的直觉一致）。
     *
     * @param left  左段
     * @param right 右段
     * @return 比较结果
     */
    private static int compareSegment(String left, String right) {
        boolean leftNumeric = isNumeric(left);
        boolean rightNumeric = isNumeric(right);
        if (leftNumeric && rightNumeric) {
            try {
                return Long.compare(Long.parseLong(left), Long.parseLong(right));
            } catch (NumberFormatException e) {
                // 超长数字段退化为字典序，不因单段异常中断整体判定
                return left.compareTo(right);
            }
        }
        if (leftNumeric) {
            return -1;
        }
        if (rightNumeric) {
            return 1;
        }
        return left.compareTo(right);
    }

    /**
     * 判断字符串是否全为数字（空串视为非数字）。
     *
     * @param value 待判断字符串
     * @return true 表示纯数字
     */
    private static boolean isNumeric(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            if (!Character.isDigit(value.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
