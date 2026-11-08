package com.houjiang.plugin.sdk.model;

import java.io.Serializable;

/**
 * 插件依赖声明。
 *
 * <p>对应 <code>plugin.yaml</code> 中 <code>dependencies</code> 的条目：
 * <pre>{@code
 * dependencies:
 *   - code: mall-points
 *     versionRange: ">=1.0.0"
 * }</pre>
 *
 * @author HouCloud
 */
public class PluginDependency implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 被依赖插件的编码 */
    private String code;

    /**
     * 版本范围约束，如 {@code >=1.0.0}、{@code 1.x}；为空表示任意版本。
     */
    private String versionRange;

    /** 是否可选：可选依赖缺失时不阻止安装 */
    private boolean optional;

    public PluginDependency() {
    }

    public PluginDependency(String code, String versionRange, boolean optional) {
        this.code = code;
        this.versionRange = versionRange;
        this.optional = optional;
    }

    public static PluginDependency of(String code) {
        return new PluginDependency(code, null, false);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getVersionRange() {
        return versionRange;
    }

    public void setVersionRange(String versionRange) {
        this.versionRange = versionRange;
    }

    public boolean isOptional() {
        return optional;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    @Override
    public String toString() {
        return code + (versionRange == null ? "" : " " + versionRange) + (optional ? " (optional)" : "");
    }
}
