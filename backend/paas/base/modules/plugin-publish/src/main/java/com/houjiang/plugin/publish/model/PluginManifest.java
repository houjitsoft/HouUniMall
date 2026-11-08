package com.houjiang.plugin.publish.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Data;

/**
 * 插件清单（jar 内 {@code plugin.yaml}）解析结果。
 *
 * <p>字段命名沿用仓库内现有 13 个官方 SKU 的 manifest 写法（camelCase）：
 * {@code code} / {@code version} / {@code minFrameworkVersion} / {@code dependencies}。
 *
 * @author Amos
 * @since M1
 */
@Data
public class PluginManifest {

    /**
     * 插件编码。
     */
    private String code;

    /**
     * 版本号（应为 SemVer）。
     */
    private String version;

    /**
     * 最低宿主框架版本，为空表示未声明。
     */
    private String minFrameworkVersion;

    /**
     * 依赖的插件编码列表；清单未声明该段时为 null（区分「未声明」与「声明为空数组」）。
     */
    private List<String> dependencies;

    /**
     * manifest 原文，落库用（供引擎侧同步站点目录）。
     */
    private String rawYaml;

    /**
     * manifest 原文的 sha256（小写十六进制），防清单被掉包。
     */
    private String manifestSha256;

    /**
     * 构造空清单。
     */
    public PluginManifest() {
        this.dependencies = new ArrayList<>();
    }

    /**
     * @return 依赖列表，永远非 null
     */
    public List<String> safeDependencies() {
        return dependencies == null ? Collections.emptyList() : dependencies;
    }

    /**
     * @return 是否声明了 dependencies 段（未声明与声明为空数组语义不同：未声明不做强校验）
     */
    public boolean hasDependencies() {
        return dependencies != null;
    }
}
