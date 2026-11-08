package com.houjiang.commons.version;

import java.util.Objects;

/**
 * 一条插件依赖声明：{@code code} 或 {@code code@minVersion}（M5b）。
 *
 * <p><b>为什么这个类在 commons</b>：同一份 {@code dependencies_json} 要在两个地方判定——
 * 升级前的兼容性预检（{@code plugin-update}）和目录「有更新」判定（{@code plugin-market}）。
 * 而 <b>plugin-market 依赖不到 plugin-update</b>（依赖方向是 {@code plugin-update → plugin-market}，
 * 反向成环），各自解析一份的后果就是「目录说能升、预检说缺依赖」——
 * 与 {@link VersionComparison}、{@link PluginVersionComparator} 下沉是同一类问题、同一套解法。
 *
 * <p><b>只描述声明，不描述状态</b>：本类只回答「这条依赖要求什么」，
 * 「被依赖的插件装没装、启没启用」由调用方按自己的数据源回答。
 * 这样预检（直查安装表）和目录判定（批量查一次建 Map）能各用自己的取数方式，
 * 判定口径仍然完全一致——口径收敛在 {@link #satisfiedBy(String)} 这一个方法里。
 *
 * <p><b>弱校验口径（与 M1 清单校验一致）</b>：没声明 {@code @minVersion} 就只要求「装了且启用」，
 * 不臆测版本要求；反过来，声明了就必须满足。
 *
 * @author Amos
 * @since M5b
 */
public final class PluginDependencyExpression {

    /**
     * 依赖声明里的版本分隔符：{@code erp@1.2.0} 表示至少需要 erp 1.2.0。
     *
     * <p>与预检原实现里的 {@code DEPENDENCY_VERSION_SEPARATOR} 同值；
     * 下沉后以本常量为唯一来源。
     */
    public static final String VERSION_SEPARATOR = "@";

    private final String code;

    private final String minVersion;

    /**
     * 构造一条依赖声明。
     *
     * @param code       被依赖的插件编码（不能为空）
     * @param minVersion 最低版本要求，可为 null / 空白
     */
    private PluginDependencyExpression(String code, String minVersion) {
        this.code = code == null ? "" : code.trim();
        this.minVersion = minVersion == null || minVersion.isBlank() ? null : minVersion.trim();
    }

    /**
     * 构造一条依赖声明。
     *
     * @param code       被依赖的插件编码
     * @param minVersion 最低版本要求，可为 null / 空白
     * @return 依赖声明
     */
    public static PluginDependencyExpression of(String code, String minVersion) {
        return new PluginDependencyExpression(code, minVersion);
    }

    /**
     * 被依赖的插件编码。
     *
     * @return 插件编码
     */
    public String code() {
        return code;
    }

    /**
     * 声明的最低版本。
     *
     * @return 最低版本；未声明返回 null
     */
    public String minVersion() {
        return minVersion;
    }

    /**
     * 是否声明了最低版本。
     *
     * @return true 表示声明了 {@code code@minVersion}
     */
    public boolean requiresMin() {
        return minVersion != null;
    }

    /**
     * 已装版本是否满足本条依赖的<b>版本</b>要求（不判断装没装、启没启用）。
     *
     * <p><b>fail-closed</b>：{@code installedVersion} 为 null 或空白时一律返回 false。
     * 「拿不到版本」不等于「版本满足」——装了但版本未知的依赖若按满足处理，
     * 就会放行一个升级后必崩的组合，代价远高于多提示一次「请先确认依赖版本」。
     *
     * <p>比较走 {@link VersionComparison#atLeast(String, String)}（宽松口径）：
     * 依赖声明面对的是历史数据，库里存在 {@code 1.8} 这类两段版本号，
     * 用严格 SemVer 解析会把「能满足」误判成「不满足」。
     *
     * @param installedVersion 被依赖插件的已装版本，可为 null
     * @return true 表示版本满足（未声明最低版本时恒为 true）
     */
    public boolean satisfiedBy(String installedVersion) {
        if (!requiresMin()) {
            return true;
        }
        if (installedVersion == null || installedVersion.isBlank()) {
            return false;
        }
        return VersionComparison.atLeast(installedVersion.trim(), minVersion);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof PluginDependencyExpression that)) {
            return false;
        }
        return Objects.equals(code, that.code) && Objects.equals(minVersion, that.minVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, minVersion);
    }

    @Override
    public String toString() {
        return requiresMin() ? code + VERSION_SEPARATOR + minVersion : code;
    }
}
