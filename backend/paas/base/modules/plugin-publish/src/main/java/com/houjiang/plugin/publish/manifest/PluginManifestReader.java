package com.houjiang.plugin.publish.manifest;

import com.houjiang.plugin.artifact.util.ArtifactDigests;
import com.houjiang.plugin.publish.model.PluginManifest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

/**
 * 从插件 jar 中读取并解析 {@code plugin.yaml}。
 *
 * <p>取值兼容三种写法，避免历史 SKU 与新 SKU 之间的字段名漂移：
 * <ul>
 *   <li>{@code code} / {@code pluginCode} / {@code plugin_code}</li>
 *   <li>{@code minFrameworkVersion} / {@code min_framework_version}</li>
 *   <li>{@code dependencies}：元素可以是字符串，也可以是含 {@code code} 键的对象</li>
 * </ul>
 *
 * <p>清单文件名同时接受 {@code plugin.yaml} 与 {@code plugin.yml}，优先根目录，
 * 找不到时退化为「任意路径下最后一个同名文件」（兼容少数插件把清单打进了子目录）。
 *
 * @author Amos
 * @since M1
 */
public final class PluginManifestReader {

    /**
     * 候选清单文件名（按优先级）。
     */
    private static final String[] CANDIDATES = {"plugin.yaml", "plugin.yml"};

    /**
     * 私有构造，工具类不允许实例化。
     */
    private PluginManifestReader() {
    }

    /**
     * 从 jar 文件中读取清单（<b>只读取清单条目，不把整个 jar 读进内存</b>，
     * 64MB 制品走这条路径，避免上传瞬时占用大量堆内存）。
     *
     * @param jarFile jar 文件路径
     * @return 解析后的清单；jar 中不含清单文件时返回 null
     * @throws IOException   jar 不是合法 zip / 读取失败
     * @throws YAMLException 清单内容不是合法 YAML
     */
    public static PluginManifest read(Path jarFile) throws IOException {
        String yaml = readManifestText(jarFile);
        return yaml == null ? null : parse(yaml);
    }

    /**
     * 从 jar 字节中读取清单（单测与内存场景使用）。
     *
     * @param jarBytes jar 完整字节
     * @return 解析后的清单；jar 中不含清单文件时返回 null
     * @throws IOException   jar 不是合法 zip / 读取失败
     * @throws YAMLException 清单内容不是合法 YAML
     */
    public static PluginManifest read(byte[] jarBytes) throws IOException {
        String yaml = readManifestText(jarBytes);
        if (yaml == null) {
            return null;
        }
        return parse(yaml);
    }

    /**
     * 解析清单文本。
     *
     * @param yaml 清单原文
     * @return 解析后的清单
     * @throws YAMLException 内容不是合法 YAML，或顶层不是键值结构
     */
    @SuppressWarnings("unchecked")
    public static PluginManifest parse(String yaml) {
        Object loaded;
        try {
            loaded = new Yaml().load(yaml);
        } catch (YAMLException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new YAMLException("plugin.yaml 解析失败：" + e.getMessage());
        }
        if (!(loaded instanceof Map)) {
            throw new YAMLException("plugin.yaml 顶层必须是键值结构");
        }
        Map<String, Object> map = (Map<String, Object>) loaded;

        PluginManifest manifest = new PluginManifest();
        manifest.setCode(firstNonBlank(map, "code", "pluginCode", "plugin_code"));
        manifest.setVersion(firstNonBlank(map, "version", "pluginVersion", "plugin_version"));
        manifest.setMinFrameworkVersion(
                firstNonBlank(map, "minFrameworkVersion", "min_framework_version"));
        manifest.setDependencies(readDependencies(map.get("dependencies")));
        manifest.setRawYaml(yaml);
        manifest.setManifestSha256(ArtifactDigests.sha256Hex(yaml.getBytes(StandardCharsets.UTF_8)));
        return manifest;
    }

    /**
     * 读取 jar 文件中清单条目的文本内容（只读取该条目，不加载整个 jar）。
     *
     * @param jarFile jar 文件路径
     * @return 清单文本；不存在时返回 null
     * @throws IOException jar 不是合法 zip / 读取失败
     */
    static String readManifestText(Path jarFile) throws IOException {
        try (ZipFile zipFile = new ZipFile(jarFile.toFile())) {
            for (String candidate : CANDIDATES) {
                ZipEntry entry = zipFile.getEntry(candidate);
                if (entry != null && !entry.isDirectory()) {
                    try (InputStream in = zipFile.getInputStream(entry)) {
                        return new String(in.readAllBytes(), StandardCharsets.UTF_8);
                    }
                }
            }
            // 退化为任意路径下的同名文件，兼容少数插件把清单打进了子目录
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.isDirectory()) {
                    continue;
                }
                String name = entry.getName();
                for (String candidate : CANDIDATES) {
                    if (name.endsWith("/" + candidate)) {
                        try (InputStream in = zipFile.getInputStream(entry)) {
                            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * 读取 jar 字节中清单条目的文本内容。
     *
     * @param jarBytes jar 完整字节
     * @return 清单文本；不存在时返回 null
     * @throws IOException jar 不是合法 zip / 读取失败
     */
    static String readManifestText(byte[] jarBytes) throws IOException {
        String fallback = null;
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(jarBytes))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                String name = entry.getName();
                for (String candidate : CANDIDATES) {
                    if (candidate.equals(name)) {
                        // 根目录清单：优先级最高，直接返回
                        return new String(zis.readAllBytes(), StandardCharsets.UTF_8);
                    }
                    if (name.endsWith("/" + candidate)) {
                        fallback = new String(zis.readAllBytes(), StandardCharsets.UTF_8);
                    }
                }
            }
        }
        return fallback;
    }

    private static String firstNonBlank(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            Object value = map.get(key);
            if (value == null) {
                continue;
            }
            String text = String.valueOf(value).trim();
            if (!text.isEmpty()) {
                return text;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static List<String> readDependencies(Object raw) {
        if (raw == null) {
            // 清单未声明 dependencies 段：与「声明为空数组」区分开，交给校验器决定是否强校验
            return null;
        }
        if (!(raw instanceof List)) {
            return new ArrayList<>();
        }
        List<String> codes = new ArrayList<>();
        for (Object item : (List<Object>) raw) {
            if (item == null) {
                continue;
            }
            if (item instanceof String text) {
                if (!text.trim().isEmpty()) {
                    codes.add(text.trim());
                }
                continue;
            }
            if (item instanceof Map<?, ?> itemMap) {
                Object code = itemMap.get("code");
                if (code == null) {
                    code = itemMap.get("pluginCode");
                }
                if (code == null) {
                    code = itemMap.get("plugin_code");
                }
                if (code != null && !String.valueOf(code).trim().isEmpty()) {
                    codes.add(String.valueOf(code).trim());
                }
            }
        }
        return codes;
    }
}
