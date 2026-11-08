package com.houjiang.plugin.publish.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.houjiang.commons.constant.PluginDistErrorCode;
import com.houjiang.plugin.publish.model.ValidationIssue;
import com.houjiang.plugin.publish.model.ValidationResult;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link PluginManifestValidator} 单测：7 条 P0-2 校验项逐条覆盖，外加 1 条防御校验。
 *
 * <p>所有用例都在测试内用 {@link JarOutputStream} 现打最小 jar，不依赖外部 fixture。
 *
 * @author Amos
 * @since M1
 */
class PluginManifestValidatorTest {

    private static final String CODE = "coupon-demo";

    private static final long MAX_BYTES = 64L * 1024 * 1024;

    private final PluginManifestValidator validator = new PluginManifestValidator();

    @Test
    @DisplayName("① 正常清单全部通过")
    void happyPath() {
        byte[] jar = jar(manifest(CODE, "1.0.0", "1.0.0", null));
        ValidationResult result = validator.validate(jar, context(CODE, "1.0.0", null));
        assertTrue(result.isPass(), () -> "应通过，实际：" + describe(result));
        assertTrue(result.getIssues().isEmpty());
    }

    @Test
    @DisplayName("①-a 制品不是合法 zip → 3013 MANIFEST_INVALID")
    void brokenJar() {
        byte[] notAZip = "this-is-not-a-jar".getBytes(StandardCharsets.UTF_8);
        ValidationResult result = validator.validate(notAZip, context(CODE, "1.0.0", null));
        assertFalse(result.isPass());
        assertEquals(PluginDistErrorCode.MANIFEST_INVALID, result.firstErrorCode());
        assertTrue(hasField(result, "manifest"));
    }

    @Test
    @DisplayName("①-b 缺少 plugin.yaml → 3013")
    void missingManifest() {
        byte[] jar = jar(null);
        ValidationResult result = validator.validate(jar, context(CODE, "1.0.0", null));
        assertFalse(result.isPass());
        assertEquals(PluginDistErrorCode.MANIFEST_INVALID, result.firstErrorCode());
        assertTrue(hasField(result, "manifest"));
    }

    @Test
    @DisplayName("①-c plugin.yaml 不是合法 YAML → 3013")
    void brokenYaml() {
        byte[] jar = jar("code: [unclosed\n  - bad indent\n\t\ttab");
        ValidationResult result = validator.validate(jar, context(CODE, "1.0.0", null));
        assertFalse(result.isPass());
        assertEquals(PluginDistErrorCode.MANIFEST_INVALID, result.firstErrorCode());
        assertTrue(hasField(result, "manifest"));
    }

    @Test
    @DisplayName("② 清单 code 与入参不一致 → 3013 field=code")
    void codeMismatch() {
        byte[] jar = jar(manifest("other-plugin", "1.0.0", "1.0.0", null));
        ValidationResult result = validator.validate(jar, context(CODE, "1.0.0", null));
        assertFalse(result.isPass());
        assertEquals(PluginDistErrorCode.MANIFEST_INVALID, result.firstErrorCode());
        assertTrue(hasField(result, "code"));
    }

    @Test
    @DisplayName("③ 版本号不是合法 SemVer → 3013 field=version")
    void versionNotSemVer() {
        byte[] jar = jar(manifest(CODE, "1.0", "1.0", null));
        ValidationResult result = validator.validate(jar, context(CODE, "1.0", null));
        assertFalse(result.isPass());
        assertEquals(PluginDistErrorCode.MANIFEST_INVALID, result.firstErrorCode());
        assertTrue(hasField(result, "version"));
    }

    @Test
    @DisplayName("④ 版本号未超过历史最大版本 → 3012 VERSION_CONFLICT（SemVer 口径）")
    void versionNotIncreasing() {
        byte[] jar = jar(manifest(CODE, "1.9.0", "1.0.0", null));
        // 历史最大 1.10.0：字符串比较会误判 1.9.0 > 1.10.0，SemVer 比较必须拦下
        ValidationResult result = validator.validate(jar, context(CODE, "1.9.0", "1.10.0"));
        assertFalse(result.isPass());
        assertEquals(PluginDistErrorCode.VERSION_CONFLICT, result.firstErrorCode());
        assertTrue(hasField(result, "version"));
    }

    @Test
    @DisplayName("④-b 版本号确实更大时放行（1.10.0 > 1.9.0）")
    void versionIncreasingPasses() {
        byte[] jar = jar(manifest(CODE, "1.10.0", "1.0.0", null));
        ValidationResult result = validator.validate(jar, context(CODE, "1.10.0", "1.9.0"));
        assertTrue(result.isPass(), () -> "应通过，实际：" + describe(result));
    }

    @Test
    @DisplayName("⑤ minFrameworkVersion 为空 → 3013")
    void missingMinFrameworkVersion() {
        byte[] jar = jar(manifest(CODE, "1.0.0", "", null));
        ValidationResult result = validator.validate(jar, context(CODE, "1.0.0", null));
        assertFalse(result.isPass());
        assertEquals(PluginDistErrorCode.MANIFEST_INVALID, result.firstErrorCode());
        assertTrue(hasField(result, "minFrameworkVersion"));
    }

    @Test
    @DisplayName("⑥ 依赖未上架 → 3015 DEPENDENCY_MISSING")
    void dependencyMissing() {
        byte[] jar = jar(manifest(CODE, "1.0.0", "1.0.0", "['erp']"));
        ValidationContext context = context(CODE, "1.0.0", null);
        context.setDependencyPublished(code -> false);
        ValidationResult result = validator.validate(jar, context);
        assertFalse(result.isPass());
        assertEquals(PluginDistErrorCode.DEPENDENCY_MISSING, result.firstErrorCode());
        assertTrue(hasField(result, "dependencies"));
    }

    @Test
    @DisplayName("⑥-b 依赖已上架 → 通过")
    void dependencyPublished() {
        byte[] jar = jar(manifest(CODE, "1.0.0", "1.0.0", "['erp']"));
        ValidationContext context = context(CODE, "1.0.0", null);
        context.setDependencyPublished(code -> true);
        assertTrue(validator.validate(jar, context).isPass());
    }

    @Test
    @DisplayName("⑥-c 未声明 dependencies 段 → 跳过校验（不打挂现有 13 个官方 SKU）")
    void dependenciesSectionAbsent() {
        byte[] jar = jar(manifest(CODE, "1.0.0", "1.0.0", null));
        ValidationContext context = context(CODE, "1.0.0", null);
        context.setDependencyPublished(code -> false);
        assertTrue(validator.validate(jar, context).isPass());
    }

    @Test
    @DisplayName("⑥-d 声明为空数组 → 通过")
    void dependenciesEmptyArray() {
        byte[] jar = jar(manifest(CODE, "1.0.0", "1.0.0", "[]"));
        ValidationContext context = context(CODE, "1.0.0", null);
        context.setDependencyPublished(code -> false);
        assertTrue(validator.validate(jar, context).isPass());
    }

    @Test
    @DisplayName("⑦ 体积超出上限 → 3017 SIZE_EXCEEDED")
    void sizeExceeded() {
        byte[] jar = jar(manifest(CODE, "1.0.0", "1.0.0", null));
        // 上限调成 10 字节：bytes 版校验以实际字节数为准，不必真的造一个 64MB 的 jar
        ValidationContext context = context(CODE, "1.0.0", null, 10L);
        ValidationResult result = validator.validate(jar, context);
        assertFalse(result.isPass());
        assertEquals(PluginDistErrorCode.SIZE_EXCEEDED, result.firstErrorCode());
        assertTrue(hasField(result, "size"));
    }

    @Test
    @DisplayName("⑦-b 文件版校验以磁盘实际大小为准（无需把制品读进内存）")
    void sizeExceededByFile() throws Exception {
        Path jarFile = Files.createTempFile("oversize-", ".jar");
        try {
            Files.write(jarFile, jar(manifest(CODE, "1.0.0", "1.0.0", null)));
            ValidationContext context = context(CODE, "1.0.0", null, 10L);
            ValidationResult result = validator.validate(jarFile, context);
            assertFalse(result.isPass());
            assertEquals(PluginDistErrorCode.SIZE_EXCEEDED, result.firstErrorCode());
        } finally {
            Files.deleteIfExists(jarFile);
        }
    }

    @Test
    @DisplayName("⑦-c 恰好等于上限时放行（边界值）")
    void sizeAtLimitPasses() throws Exception {
        Path jarFile = Files.createTempFile("atlimit-", ".jar");
        try {
            byte[] content = jar(manifest(CODE, "1.0.0", "1.0.0", null));
            Files.write(jarFile, content);
            ValidationContext context = context(CODE, "1.0.0", null, content.length);
            assertTrue(validator.validate(jarFile, context).isPass());
        } finally {
            Files.deleteIfExists(jarFile);
        }
    }

    @Test
    @DisplayName("⑧ 补充防御：清单 version 与入参 version 不一致 → 3013")
    void versionParamMismatch() {
        byte[] jar = jar(manifest(CODE, "1.1.0", "1.0.0", null));
        ValidationResult result = validator.validate(jar, context(CODE, "1.0.0", null));
        assertFalse(result.isPass());
        assertEquals(PluginDistErrorCode.MANIFEST_INVALID, result.firstErrorCode());
        assertTrue(hasField(result, "version"));
    }

    private static ValidationContext context(String code, String version, String maxExisting) {
        return context(code, version, maxExisting, MAX_BYTES);
    }

    private static ValidationContext context(String code, String version, String maxExisting, long maxBytes) {
        ValidationContext context = new ValidationContext(code, version, 1024L, maxExisting, maxBytes);
        context.setDependencyPublished(dep -> true);
        return context;
    }

    private static boolean hasField(ValidationResult result, String field) {
        return result.getIssues().stream().anyMatch(issue -> field.equals(issue.getField()));
    }

    private static String describe(ValidationResult result) {
        return result.getIssues().stream()
                .map(issue -> issue.getLevel() + "/" + issue.getField() + "/" + issue.getMsg())
                .collect(Collectors.joining("; "));
    }

    private static String manifest(String code, String version, String minFramework, String dependencies) {
        StringBuilder yaml = new StringBuilder();
        yaml.append("code: ").append(code).append('\n');
        yaml.append("version: ").append(version).append('\n');
        yaml.append("minFrameworkVersion: '").append(minFramework).append("'\n");
        if (dependencies != null) {
            yaml.append("dependencies: ").append(dependencies).append('\n');
        }
        return yaml.toString();
    }

    private static byte[] jar(String yaml) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (JarOutputStream jos = new JarOutputStream(buffer)) {
            if (yaml != null) {
                jos.putNextEntry(new JarEntry("plugin.yaml"));
                jos.write(yaml.getBytes(StandardCharsets.UTF_8));
                jos.closeEntry();
            }
            jos.putNextEntry(new JarEntry("com/demo/Main.class"));
            jos.write(new byte[] {1, 2, 3, 4});
            jos.closeEntry();
        } catch (IOException e) {
            throw new IllegalStateException("构造测试 jar 失败", e);
        }
        return buffer.toByteArray();
    }

    @Test
    @DisplayName("错误级别只有 ERROR / WARN，且 WARN 不阻断")
    void warnDoesNotBlock() {
        ValidationResult result = ValidationResult.ok();
        result.addWarn("changelog", 0, "建议填写更新说明");
        assertTrue(result.isPass());
        Set<String> levels = result.getIssues().stream()
                .map(ValidationIssue::getLevel)
                .collect(Collectors.toSet());
        assertTrue(levels.contains(ValidationIssue.LEVEL_WARN));
    }
}
