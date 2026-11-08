package com.houjiang.plugin.artifact.key;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 制品存储键（storage key）契约工具类。
 *
 * <p>统一契约：{@code {code}/{version}/{code}-{version}.jar}
 *
 * <pre>
 *   示例：coupon-demo/1.0.0/coupon-demo-1.0.0.jar
 * </pre>
 *
 * <p>该契约与引擎侧 {@code PluginArtifactStore} 的目录约定一致，保证「本地安装目录」与
 * 「分发存储目录」可以一一映射，回滚时按目录整体恢复。
 *
 * <p>所有 key 都必须是相对路径。本工具类在生成与解析时都会做合法性校验，
 * 严禁出现 {@code ..}、绝对路径、反斜杠，防止目录穿越（pluginCode / version 来自用户输入）。
 *
 * @author Amos
 * @since M0
 */
public final class ArtifactKeys {

    /**
     * 制品文件扩展名。
     */
    public static final String JAR_SUFFIX = ".jar";

    /**
     * key 段分隔符。
     */
    public static final String SEPARATOR = "/";

    /**
     * 合法的分段字符（字母、数字、点、下划线、中划线）。
     */
    private static final Pattern SAFE_SEGMENT = Pattern.compile("[A-Za-z0-9._-]+");

    /**
     * 商业双制品在对象存储上的逻辑前缀（见《系统架构》§4.2）。
     */
    public static final String COMMERCIAL_RUNTIME_PREFIX = "commercial-runtime/";

    /**
     * 商业源码包前缀。
     */
    public static final String COMMERCIAL_SOURCE_PREFIX = "commercial-source/";

    /**
     * 私有构造，工具类不允许实例化。
     */
    private ArtifactKeys() {
    }

    /**
     * 商业运行制品键：{@code commercial-runtime/{code}/{version}/{code}-{version}-runtime.tar}。
     */
    public static String commercialRuntimeKey(String pluginCode, String version) {
        checkSegment("pluginCode", pluginCode);
        checkSegment("version", version);
        return COMMERCIAL_RUNTIME_PREFIX + pluginCode + SEPARATOR + version + SEPARATOR
                + pluginCode + "-" + version + "-runtime.tar";
    }

    /**
     * 商业源码包键：{@code commercial-source/{code}/{version}/{code}-{version}-source.tar.gz}。
     */
    public static String commercialSourceKey(String pluginCode, String version) {
        checkSegment("pluginCode", pluginCode);
        checkSegment("version", version);
        return COMMERCIAL_SOURCE_PREFIX + pluginCode + SEPARATOR + version + SEPARATOR
                + pluginCode + "-" + version + "-source.tar.gz";
    }

    /**
     * 生成制品存储键。
     *
     * @param pluginCode 插件编码
     * @param version    插件版本
     * @return 存储键 {@code {code}/{version}/{code}-{version}.jar}
     * @throws IllegalArgumentException 编码或版本为空、含非法字符时抛出
     */
    public static String jarKey(String pluginCode, String version) {
        checkSegment("pluginCode", pluginCode);
        checkSegment("version", version);
        return pluginCode + SEPARATOR + version + SEPARATOR + pluginCode + "-" + version + JAR_SUFFIX;
    }

    /**
     * 生成版本目录键（删除/列出版本下所有文件时用）。
     *
     * @param pluginCode 插件编码
     * @param version    插件版本
     * @return 目录键 {@code {code}/{version}}
     */
    public static String versionDir(String pluginCode, String version) {
        checkSegment("pluginCode", pluginCode);
        checkSegment("version", version);
        return pluginCode + SEPARATOR + version;
    }

    /**
     * 生成插件目录键。
     *
     * @param pluginCode 插件编码
     * @return 目录键 {@code {code}}
     */
    public static String pluginDir(String pluginCode) {
        checkSegment("pluginCode", pluginCode);
        return pluginCode;
    }

    /**
     * 解析存储键，得到插件编码与版本。
     *
     * @param key 存储键
     * @return 解析结果
     * @throws IllegalArgumentException key 不符合契约时抛出
     */
    public static ArtifactCoordinate parse(String key) {
        validate(key);
        String[] segments = stripCommercialPrefix(key).split(SEPARATOR);
        return new ArtifactCoordinate(segments[0], segments[1], segments[2]);
    }

    /**
     * 校验存储键合法性（防目录穿越）。
     *
     * @param key 存储键
     * @throws IllegalArgumentException 非法时抛出
     */
    public static void validate(String key) {
        Objects.requireNonNull(key, "制品存储键不能为空");
        if (key.isBlank()) {
            throw new IllegalArgumentException("制品存储键不能为空白");
        }
        if (key.contains("..")) {
            throw new IllegalArgumentException("制品存储键不允许包含 '..'：key=" + key);
        }
        if (key.contains("\\")) {
            throw new IllegalArgumentException("制品存储键不允许包含反斜杠：key=" + key);
        }
        if (key.startsWith(SEPARATOR)) {
            throw new IllegalArgumentException("制品存储键必须是相对路径：key=" + key);
        }
        String normalized = stripCommercialPrefix(key);
        String[] segments = normalized.split(SEPARATOR);
        if (segments.length != 3) {
            throw new IllegalArgumentException(
                    "制品存储键必须为 [commercial-runtime|commercial-source/]{code}/{version}/{file}，实际 "
                            + segments.length + " 段：key=" + key);
        }
        checkSegment("pluginCode", segments[0]);
        checkSegment("version", segments[1]);
        String fileName = segments[2];
        int stemLen = fileName.length() - suffixLength(fileName);
        if (stemLen <= 0) {
            throw new IllegalArgumentException("制品文件名缺少有效主干：key=" + key);
        }
        checkSegment("fileName", fileName.substring(0, stemLen));
        if (!fileName.toLowerCase(Locale.ROOT).endsWith(JAR_SUFFIX)
                && !fileName.endsWith("-source.tar.gz") && !fileName.endsWith("-runtime.tar")) {
            throw new IllegalArgumentException(
                    "制品文件名必须以 .jar / -source.tar.gz / -runtime.tar 结尾：key=" + key);
        }
    }

    /**
     * 去掉商业双制品逻辑前缀，便于与三段契约对齐。
     */
    public static String stripCommercialPrefix(String key) {
        if (key.startsWith(COMMERCIAL_RUNTIME_PREFIX)) {
            return key.substring(COMMERCIAL_RUNTIME_PREFIX.length());
        }
        if (key.startsWith(COMMERCIAL_SOURCE_PREFIX)) {
            return key.substring(COMMERCIAL_SOURCE_PREFIX.length());
        }
        return key;
    }

    private static int suffixLength(String fileName) {
        if (fileName.endsWith("-source.tar.gz")) {
            return "-source.tar.gz".length();
        }
        if (fileName.endsWith("-runtime.tar")) {
            return "-runtime.tar".length();
        }
        if (fileName.toLowerCase(Locale.ROOT).endsWith(JAR_SUFFIX)) {
            return JAR_SUFFIX.length();
        }
        return 0;
    }

    private static void checkSegment(String name, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " 不能为空");
        }
        if (!SAFE_SEGMENT.matcher(value).matches()) {
            throw new IllegalArgumentException(
                    name + " 只允许字母、数字、点、下划线、中划线，实际值：" + value);
        }
    }

    /**
     * 存储键解析结果。
     */
    public static final class ArtifactCoordinate {

        private final String pluginCode;

        private final String version;

        private final String fileName;

        private ArtifactCoordinate(String pluginCode, String version, String fileName) {
            this.pluginCode = pluginCode;
            this.version = version;
            this.fileName = fileName;
        }

        /**
         * @return 插件编码
         */
        public String getPluginCode() {
            return pluginCode;
        }

        /**
         * @return 插件版本
         */
        public String getVersion() {
            return version;
        }

        /**
         * @return 制品文件名
         */
        public String getFileName() {
            return fileName;
        }
    }
}
