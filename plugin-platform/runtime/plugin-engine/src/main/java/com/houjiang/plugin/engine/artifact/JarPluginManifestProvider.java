package com.houjiang.plugin.engine.artifact;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.houjiang.plugin.engine.spi.PluginManifestProvider;
import com.houjiang.plugin.sdk.model.PluginDependency;
import com.houjiang.plugin.sdk.model.PluginManifest;
import com.houjiang.plugin.sdk.model.PluginServiceOwnership;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

/** Reads the authoritative {@code plugin.yaml} entry from a plugin JAR. */
public final class JarPluginManifestProvider implements PluginManifestProvider {

    private static final String[] MANIFEST_ENTRIES = {"plugin.yaml", "META-INF/plugin.yaml"};

    /** 合法的插件 / 依赖编码。 */
    private static final Pattern SAFE_CODE = Pattern.compile("[A-Za-z0-9_-]+");

    /** 三段式 SemVer（允许 pre-release 后缀，如 1.0.0-beta）。 */
    private static final Pattern VERSION_PATTERN = Pattern.compile("\\d+\\.\\d+\\.\\d+(-[A-Za-z0-9.]+)?");

    private static final Set<String> OWNERS = Set.of("official", "thirdparty", "custom");

    private static final Set<String> RUNTIMES = Set.of("embedded", "standalone");

    private final ObjectMapper yamlMapper;
    private final boolean allowLegacyHost;

    public JarPluginManifestProvider() {
        this(false);
    }

    public JarPluginManifestProvider(boolean allowLegacyHost) {
        this(new ObjectMapper(new YAMLFactory())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false), allowLegacyHost);
    }

    JarPluginManifestProvider(ObjectMapper yamlMapper) {
        this(yamlMapper, false);
    }

    private JarPluginManifestProvider(ObjectMapper yamlMapper, boolean allowLegacyHost) {
        this.yamlMapper = yamlMapper;
        this.allowLegacyHost = allowLegacyHost;
    }

    @Override
    public PluginManifest load(String code, Path jarPath) {
        if (jarPath == null || !Files.isRegularFile(jarPath)) {
            throw new IllegalArgumentException("插件安装包不存在: " + jarPath);
        }
        try (JarFile jar = new JarFile(jarPath.toFile())) {
            for (String entryName : MANIFEST_ENTRIES) {
                JarEntry entry = jar.getJarEntry(entryName);
                if (entry == null) {
                    continue;
                }
                try (InputStream input = jar.getInputStream(entry)) {
                    PluginManifest manifest = yamlMapper.readValue(input, PluginManifest.class);
                    validate(code, manifest, jarPath);
                    return manifest;
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("读取插件 plugin.yaml 失败: " + jarPath, e);
        }
        throw new IllegalStateException("插件安装包缺少 plugin.yaml: " + jarPath);
    }

    /**
     * 清单校验。
     *
     * <p>早期只校验 code / version / owner 三项，其余字段写了没人管——
     * {@code minFrameworkVersion}、{@code dependencies}、{@code terminals} 写错了也照装不误，
     * 故障全部推迟到运行期才炸，且现象（排序错乱、依赖静默不满足、终端类型不匹配）
     * 离根因很远。这里把能静态判定的都判掉，装载前就拒绝。
     *
     * <p><b>为什么版本必须严格 SemVer</b>：升级方向、版本排序、兼容性比较全部依赖它。
     * 一个 {@code 1.0} 或 {@code v1.0} 会让字符串排序把 {@code 1.10.0} 排到 {@code 1.9.0} 前面，
     * 用户会看到「最新版」其实是旧版。
     */
    private void validate(String code, PluginManifest manifest, Path jarPath) {
        if (manifest == null || manifest.getCode() == null || manifest.getCode().isBlank()) {
            throw new IllegalStateException("插件 plugin.yaml 缺少 code: " + jarPath);
        }
        manifest.setCode(manifest.getCode().trim());
        if (!SAFE_CODE.matcher(manifest.getCode()).matches()) {
            throw new IllegalStateException("插件 code 只允许字母、数字、中划线、下划线，实际值："
                    + manifest.getCode() + "（" + jarPath + "）");
        }
        if (code == null || !code.equals(manifest.getCode())) {
            throw new IllegalStateException("插件清单 code 与安装编码不一致: expected="
                    + code + ", actual=" + manifest.getCode());
        }
        if (manifest.getVersion() == null || manifest.getVersion().isBlank()) {
            throw new IllegalStateException("插件 plugin.yaml 缺少 version: " + jarPath);
        }
        String version = manifest.getVersion().trim();
        if (!VERSION_PATTERN.matcher(version).matches()) {
            throw new IllegalStateException("插件 version 必须是三段式 SemVer（如 1.0.0），实际值："
                    + version + "（" + jarPath + "）");
        }
        manifest.setVersion(version);

        if (manifest.getOwner() == null || manifest.getOwner().isBlank()) {
            manifest.setOwner("official");
        } else {
            manifest.setOwner(manifest.getOwner().trim().toLowerCase(java.util.Locale.ROOT));
        }
        if (!OWNERS.contains(manifest.getOwner())) {
            throw new IllegalStateException("插件 owner 不受支持（可选 official/thirdparty/custom）: "
                    + manifest.getOwner());
        }
        String runtime = manifest.getRuntime() == null ? "embedded" : manifest.getRuntime().trim();
        if (!RUNTIMES.contains(runtime)) {
            throw new IllegalStateException("插件 runtime 不受支持（可选 embedded/standalone）: " + runtime);
        }
        manifest.setRuntime(runtime);

        // minFrameworkVersion 允许缺省（缺省表示不限制），但写了就必须合法：
        // 非法值会让兼容性比较静默失效，等于「声明了要求但没人执行」。
        if (manifest.getMinFrameworkVersion() != null && !manifest.getMinFrameworkVersion().isBlank()) {
            String minFramework = manifest.getMinFrameworkVersion().trim();
            if (!VERSION_PATTERN.matcher(minFramework).matches()) {
                throw new IllegalStateException("插件 minFrameworkVersion 必须是三段式 SemVer，实际值："
                        + minFramework + "（" + jarPath + "）");
            }
            manifest.setMinFrameworkVersion(minFramework);
        }
        validateDependencies(manifest, jarPath);
        manifest.setHostService(PluginServiceOwnership.resolve(manifest, allowLegacyHost));
    }

    /**
     * 依赖声明校验：编码非空、不重复、不自依赖、版本范围若写了就必须是合法 SemVer。
     *
     * <p>不在这里做「依赖是否已安装」的判定——那是安装编排的职责
     * （{@code PluginCompatibilityPrecheckService}），装载期只管声明本身是否写得对。
     */
    private void validateDependencies(PluginManifest manifest, Path jarPath) {
        if (manifest.getDependencies() == null || manifest.getDependencies().isEmpty()) {
            return;
        }
        Set<String> seen = new LinkedHashSet<>();
        for (PluginDependency dependency : manifest.getDependencies()) {
            if (dependency == null || dependency.getCode() == null || dependency.getCode().isBlank()) {
                throw new IllegalStateException("插件依赖声明缺少 code: " + jarPath);
            }
            String depCode = dependency.getCode().trim();
            if (!SAFE_CODE.matcher(depCode).matches()) {
                throw new IllegalStateException("插件依赖 code 只允许字母、数字、中划线、下划线，实际值："
                        + depCode + "（" + jarPath + "）");
            }
            if (depCode.equals(manifest.getCode())) {
                throw new IllegalStateException("插件不能声明依赖自己: " + depCode + "（" + jarPath + "）");
            }
            if (!seen.add(depCode)) {
                throw new IllegalStateException("插件依赖声明重复: " + depCode + "（" + jarPath + "）");
            }
            if (dependency.getVersionRange() != null && !dependency.getVersionRange().isBlank()) {
                String range = dependency.getVersionRange().trim();
                // 版本范围形如 ">=1.0.0" / "1.2.0"；取末尾裸露的版本号做格式校验
                String bare = range.replaceAll("^[<>=~^ ]+", "").trim();
                if (!bare.isEmpty() && !VERSION_PATTERN.matcher(bare).matches()) {
                    throw new IllegalStateException("插件依赖版本范围不合法: " + range
                            + "（依赖 " + depCode + "，" + jarPath + "）");
                }
            }
        }
    }
}
