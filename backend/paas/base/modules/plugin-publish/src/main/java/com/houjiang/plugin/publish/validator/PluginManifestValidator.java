package com.houjiang.plugin.publish.validator;

import com.houjiang.commons.constant.PluginDistErrorCode;
import com.houjiang.commons.version.PluginVersionComparator;
import com.houjiang.plugin.publish.manifest.PluginManifestReader;
import com.houjiang.plugin.publish.model.PluginManifest;
import com.houjiang.plugin.publish.model.ValidationResult;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.error.YAMLException;

/**
 * 插件清单校验器（P0-2）。
 *
 * <p><b>不抛异常、不中断</b>：一次性把所有字段级问题收集完返回，让发布者一次改完，
 * 而不是「改一个字段重试一次」。只有 ERROR 级问题会阻断上传。
 *
 * <p>校验项（与方案 P0-2 逐条对应）：
 * <ol>
 *   <li>jar 可读、{@code plugin.yaml} 存在且可解析 → 3013 MANIFEST_INVALID</li>
 *   <li>清单 code 与入参 pluginCode 一致 → 3013</li>
 *   <li>版本号为合法 SemVer（允许 {@code -rc.N} 预发布段）→ 3013</li>
 *   <li>版本号必须<b>大于</b>该 pluginCode 的历史最大版本（SemVer 比较，不是字符串比较）→ 3012</li>
 *   <li>{@code minFrameworkVersion} 非空 → 3013</li>
 *   <li>{@code dependencies} 每个依赖在市场存在且已上架 → 3015 DEPENDENCY_MISSING；
 *       <b>清单未声明 dependencies 段则跳过，不做强校验</b>（避免打挂现有 13 个官方 SKU）</li>
 *   <li>制品体积 ≤ 上限（默认 64MB）→ 3017 SIZE_EXCEEDED</li>
 * </ol>
 *
 * <p>补充的第 8 项防御校验：清单 version 必须与接口入参 version 一致 → 3013。
 * 存储键 {@code {code}/{version}/...} 与签名串都绑定 version，两者不一致会让制品身份自相矛盾。
 *
 * @author Amos
 * @since M1
 */
@Component
public class PluginManifestValidator {

    /**
     * 执行清单校验（内存字节版，单测与内存场景使用）。
     *
     * @param jarBytes 制品完整字节
     * @param context  校验上下文
     * @return 结构化校验结果
     */
    public ValidationResult validate(byte[] jarBytes, ValidationContext context) {
        if (jarBytes == null || jarBytes.length == 0) {
            ValidationResult empty = ValidationResult.ok();
            empty.addError("file", PluginDistErrorCode.MANIFEST_INVALID, "制品内容为空");
            return empty;
        }
        PluginManifest manifest;
        try {
            manifest = PluginManifestReader.read(jarBytes);
        } catch (IOException e) {
            ValidationResult result = ValidationResult.ok();
            result.addError("manifest", PluginDistErrorCode.MANIFEST_INVALID,
                    "制品不是合法 jar/zip，无法读取：" + e.getMessage());
            return result;
        } catch (YAMLException e) {
            ValidationResult result = ValidationResult.ok();
            result.addError("manifest", PluginDistErrorCode.MANIFEST_INVALID, "plugin.yaml 解析失败：" + e.getMessage());
            return result;
        }
        return validate(manifest, jarBytes.length, context);
    }

    /**
     * 执行清单校验（文件版，<b>只读取 jar 内清单条目</b>，用于真实上传链路，避免把 64MB 读进堆）。
     *
     * @param jarFile 已落盘的制品文件
     * @param context 校验上下文
     * @return 结构化校验结果
     */
    public ValidationResult validate(Path jarFile, ValidationContext context) {
        ValidationResult result = ValidationResult.ok();
        if (jarFile == null || !Files.isRegularFile(jarFile)) {
            result.addError("file", PluginDistErrorCode.MANIFEST_INVALID, "制品文件不存在");
            return result;
        }
        long sizeBytes;
        try {
            sizeBytes = Files.size(jarFile);
        } catch (IOException e) {
            result.addError("file", PluginDistErrorCode.MANIFEST_INVALID, "无法读取制品大小：" + e.getMessage());
            return result;
        }
        PluginManifest manifest;
        try {
            manifest = PluginManifestReader.read(jarFile);
        } catch (IOException e) {
            result.addError("manifest", PluginDistErrorCode.MANIFEST_INVALID,
                    "制品不是合法 jar/zip，无法读取：" + e.getMessage());
            return result;
        } catch (YAMLException e) {
            result.addError("manifest", PluginDistErrorCode.MANIFEST_INVALID, "plugin.yaml 解析失败：" + e.getMessage());
            return result;
        }
        ValidationResult checked = validate(manifest, sizeBytes, context);
        return checked;
    }

    /**
     * 校验主体（清单已解析、体积已知）。
     *
     * @param manifest  解析后的清单，可为 null（表示缺少 plugin.yaml）
     * @param sizeBytes 制品字节数
     * @param context   校验上下文
     * @return 结构化校验结果
     */
    public ValidationResult validate(PluginManifest manifest, long sizeBytes, ValidationContext context) {
        ValidationResult result = ValidationResult.ok();

        // ⑦ 体积校验放在最前面：超大制品没必要再看清单
        if (!checkSize(sizeBytes, context, result)) {
            return result;
        }

        if (manifest == null) {
            result.addError("manifest", PluginDistErrorCode.MANIFEST_INVALID,
                    "制品内缺少 plugin.yaml / plugin.yml");
            return result;
        }

        // ② code 一致性
        if (isBlank(manifest.getCode())) {
            result.addError("code", PluginDistErrorCode.MANIFEST_INVALID, "plugin.yaml 缺少 code 字段");
        } else if (!manifest.getCode().equals(context.getPluginCode())) {
            result.addError("code", PluginDistErrorCode.MANIFEST_INVALID,
                    "plugin.yaml 的 code(" + manifest.getCode() + ") 与入参 pluginCode(" + context.getPluginCode() + ") 不一致");
        }

        // ③ 版本号合法 SemVer
        String manifestVersion = manifest.getVersion();
        if (isBlank(manifestVersion)) {
            result.addError("version", PluginDistErrorCode.MANIFEST_INVALID, "plugin.yaml 缺少 version 字段");
        } else if (!PluginVersionComparator.isValid(manifestVersion)) {
            result.addError("version", PluginDistErrorCode.MANIFEST_INVALID,
                    "版本号不是合法 SemVer（应为 major.minor.patch[-预发布]）：" + manifestVersion);
        }

        // ⑧ 清单 version 与入参 version 一致
        if (!isBlank(manifestVersion) && !isBlank(context.getVersion())
                && !manifestVersion.equals(context.getVersion())) {
            result.addError("version", PluginDistErrorCode.MANIFEST_INVALID,
                    "plugin.yaml 的 version(" + manifestVersion + ") 与入参 version(" + context.getVersion() + ") 不一致");
        }

        // ④ 版本号必须大于历史最大版本（SemVer 比较）
        String effectiveVersion = !isBlank(manifestVersion) ? manifestVersion : context.getVersion();
        if (!isBlank(effectiveVersion) && PluginVersionComparator.isValid(effectiveVersion)
                && !isBlank(context.getMaxExistingVersion())) {
            String max = context.getMaxExistingVersion();
            if (PluginVersionComparator.isValid(max)
                    && PluginVersionComparator.compare(effectiveVersion, max) <= 0) {
                result.addError("version", PluginDistErrorCode.VERSION_CONFLICT,
                        "版本号必须大于历史最大版本 " + max + "，当前 " + effectiveVersion);
            }
        }

        // ⑤ minFrameworkVersion 非空
        if (isBlank(manifest.getMinFrameworkVersion())) {
            result.addError("minFrameworkVersion", PluginDistErrorCode.MANIFEST_INVALID,
                    "plugin.yaml 的 minFrameworkVersion 不能为空（客户端据此做兼容性预检）");
        }

        // ⑥ 依赖校验：未声明 dependencies 段则跳过
        checkDependencies(manifest, context, result);

        return result;
    }

    private boolean checkSize(long actualBytes, ValidationContext context, ValidationResult result) {
        long limit = context.getMaxArtifactBytes();
        if (limit <= 0) {
            return true;
        }
        if (actualBytes > limit) {
            result.addError("size", PluginDistErrorCode.SIZE_EXCEEDED,
                    "制品体积 " + actualBytes + " 字节超过上限 " + limit + " 字节（" + (limit / 1024 / 1024) + "MB）");
            return false;
        }
        return true;
    }

    private void checkDependencies(PluginManifest manifest, ValidationContext context, ValidationResult result) {
        if (!manifest.hasDependencies()) {
            // 清单未声明依赖段：不做强校验（历史 SKU 可能没有该段）
            return;
        }
        List<String> dependencies = manifest.safeDependencies();
        for (String dependency : dependencies) {
            if (isBlank(dependency)) {
                continue;
            }
            boolean published;
            try {
                published = context.getDependencyPublished().test(dependency);
            } catch (RuntimeException e) {
                // 判定器（查库）抛异常时按「未上架」处理：问题照常报出，但不让数据库抖动打挂上传接口
                published = false;
            }
            if (!published) {
                result.addError("dependencies", PluginDistErrorCode.DEPENDENCY_MISSING,
                        "依赖插件 " + dependency + " 在插件市场中不存在或未上架");
            }
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
