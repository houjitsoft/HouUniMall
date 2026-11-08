package com.houjiang.commons.version;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 插件版本号比较器（严格 SemVer）。
 *
 * <p><b>为什么必须做 SemVer 比较而不是字符串比较</b>：字符串比较会得出
 * {@code "1.10.0" < "1.9.0"} 的错误结论，直接导致「有更新」判定失效、
 * 客户端反复提示升级到旧版本。目录索引（P0-10）、升级预检、回滚锚点全部依赖本类。
 *
 * <p><b>为什么放在 commons</b>：发布侧（plugin-publish，拒绝脏版本号）与目录侧
 * （plugin-market，判定「有更新」）都要用它，而 market <b>不能</b>依赖 publish
 * （依赖方向是 {@code mall → plugin-publish → plugin-market}，反向即成环）。
 * 全项目只允许存在这一份实现——各复制一份的后果是「同一件事有两种判定」，
 * 这类分歧在生产上表现为「发布时说合法、目录里说没更新」，极难排查。
 *
 * <p>支持的格式：{@code major.minor.patch} 三段数字，可选预发布后缀
 * （{@code -alpha.1} / {@code -beta} / {@code -rc.2}）。
 * 比较规则（与 SemVer 2.0.0 一致的核心部分）：
 * <ol>
 *   <li>依次比较 major / minor / patch 的数值；</li>
 *   <li>正式版 &gt; 预发布版（同为 1.0.0 时，1.0.0 &gt; 1.0.0-rc.1）；</li>
 *   <li>预发布段按「纯数字段比数值、其余按字典序、数字段优先级低于非数字段」逐段比较。</li>
 * </ol>
 *
 * <p>不符合 SemVer 的版本（如 {@code v1.0}）会被判定为非法，
 * 由发布侧的清单校验拦截，避免脏版本进入目录。
 *
 * <p><b>注意</b>：本类是<b>严格</b>口径。安装链路面对历史数据（库里存在 {@code 1.8} 这类
 * 两段版本号）时用的是 plugin-update 的 {@code VersionComparison}（宽松口径），
 * 两者分工不同，不要互换。
 *
 * @author Amos
 * @since M0
 */
public final class PluginVersionComparator {

    /**
     * SemVer 版本格式。
     */
    private static final Pattern SEMVER =
            Pattern.compile("^(\\d+)\\.(\\d+)\\.(\\d+)(?:-([0-9A-Za-z.-]+))?$");

    /**
     * 私有构造，工具类不允许实例化。
     */
    private PluginVersionComparator() {
    }

    /**
     * 比较两个版本号。
     *
     * @param left  左侧版本
     * @param right 右侧版本
     * @return 负数表示 left &lt; right，0 表示相等，正数表示 left &gt; right
     * @throws IllegalArgumentException 任一版本号不是合法 SemVer 时抛出
     */
    public static int compare(String left, String right) {
        SemVer a = parse(left);
        SemVer b = parse(right);
        int result = Integer.compare(a.major, b.major);
        if (result != 0) {
            return result;
        }
        result = Integer.compare(a.minor, b.minor);
        if (result != 0) {
            return result;
        }
        result = Integer.compare(a.patch, b.patch);
        if (result != 0) {
            return result;
        }
        return comparePreRelease(a.preRelease, b.preRelease);
    }

    /**
     * 判断 candidate 是否是相对 current 的更高版本（严格大于）。
     *
     * @param current   当前已安装版本，为 null 或空白时视为「未安装」，任何合法版本都是升级
     * @param candidate 候选版本
     * @return true 表示 candidate 更高
     */
    public static boolean isUpgrade(String current, String candidate) {
        if (current == null || current.isBlank()) {
            return isValid(candidate);
        }
        return compare(candidate, current) > 0;
    }

    /**
     * 判断版本号是否为合法 SemVer。
     *
     * @param version 版本号
     * @return true 表示合法
     */
    public static boolean isValid(String version) {
        return version != null && SEMVER.matcher(version.trim()).matches();
    }

    /**
     * 从一组版本里挑出最大的那个（严格 SemVer 口径）。
     *
     * <p>与 {@link #isUpgrade} 的区别：这里用于「刷新商品 latest_version」这类
     * 需要在一批版本里取最大值的场景；非法版本直接跳过而不是抛异常——
     * 一条历史脏数据不该让整个刷新动作失败。
     *
     * @param versions 候选版本，可为 null
     * @return 最大版本，全部非法或为空时返回 null
     */
    public static String maxOf(java.util.Collection<String> versions) {
        if (versions == null || versions.isEmpty()) {
            return null;
        }
        String best = null;
        for (String candidate : versions) {
            if (!isValid(candidate)) {
                continue;
            }
            if (best == null || compare(candidate, best) > 0) {
                best = candidate.trim();
            }
        }
        return best;
    }

    /**
     * 解析版本号。
     *
     * @param version 版本号
     * @return 解析结果
     * @throws IllegalArgumentException 版本为空或不符合 SemVer 时抛出
     */
    public static SemVer parse(String version) {
        if (version == null || version.isBlank()) {
            throw new IllegalArgumentException("插件版本号不能为空");
        }
        Matcher matcher = SEMVER.matcher(version.trim());
        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                    "插件版本号不是合法 SemVer（应为 major.minor.patch[-preRelease]）：" + version);
        }
        return new SemVer(
                Integer.parseInt(matcher.group(1)),
                Integer.parseInt(matcher.group(2)),
                Integer.parseInt(matcher.group(3)),
                matcher.group(4));
    }

    private static int comparePreRelease(String left, String right) {
        if (left == null && right == null) {
            return 0;
        }
        // 正式版（无预发布段）高于预发布版
        if (left == null) {
            return 1;
        }
        if (right == null) {
            return -1;
        }
        String[] leftParts = left.split("\\.");
        String[] rightParts = right.split("\\.");
        int max = Math.max(leftParts.length, rightParts.length);
        for (int i = 0; i < max; i++) {
            String l = i < leftParts.length ? leftParts[i] : null;
            String r = i < rightParts.length ? rightParts[i] : null;
            if (l == null) {
                return -1;
            }
            if (r == null) {
                return 1;
            }
            int result = comparePreReleasePart(l, r);
            if (result != 0) {
                return result;
            }
        }
        return 0;
    }

    private static int comparePreReleasePart(String left, String right) {
        boolean leftNumeric = left.chars().allMatch(Character::isDigit);
        boolean rightNumeric = right.chars().allMatch(Character::isDigit);
        if (leftNumeric && rightNumeric) {
            return Long.compare(Long.parseLong(left), Long.parseLong(right));
        }
        if (leftNumeric) {
            // SemVer：数字段的优先级低于非数字段
            return -1;
        }
        if (rightNumeric) {
            return 1;
        }
        return left.compareTo(right);
    }

    /**
     * SemVer 解析结果。
     */
    public static final class SemVer {

        private final int major;

        private final int minor;

        private final int patch;

        private final String preRelease;

        private SemVer(int major, int minor, int patch, String preRelease) {
            this.major = major;
            this.minor = minor;
            this.patch = patch;
            this.preRelease = preRelease;
        }

        /**
         * @return 主版本号
         */
        public int getMajor() {
            return major;
        }

        /**
         * @return 次版本号
         */
        public int getMinor() {
            return minor;
        }

        /**
         * @return 修订号
         */
        public int getPatch() {
            return patch;
        }

        /**
         * @return 预发布段，正式版为 null
         */
        public String getPreRelease() {
            return preRelease;
        }

        /**
         * @return 是否为正式版（无预发布段）
         */
        public boolean isRelease() {
            return preRelease == null;
        }

        @Override
        public String toString() {
            return major + "." + minor + "." + patch + (preRelease == null ? "" : "-" + preRelease);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof SemVer other)) {
                return false;
            }
            return major == other.major && minor == other.minor && patch == other.patch
                    && Objects.equals(preRelease, other.preRelease);
        }

        @Override
        public int hashCode() {
            return Objects.hash(major, minor, patch, preRelease);
        }
    }
}
