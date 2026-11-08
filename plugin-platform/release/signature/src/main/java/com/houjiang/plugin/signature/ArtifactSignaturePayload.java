package com.houjiang.plugin.signature;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

/**
 * 制品签名规范化串（canonical payload）构建与解析。
 *
 * <p><b>为什么签摘要串而不是直接签 jar 字节</b>：直接签 jar 字节只能证明「这段字节没被改」，
 * 无法阻止「把 1.0.0 的合法签名贴到 1.1.0 的 jar 上」（签名与版本元数据分离存放时尤其容易）。
 * 因此把 pluginCode / version / md5 / size 一起纳入签名，任何一个维度被替换都会导致验签失败。
 *
 * <p>格式（固定 5 段，竖线分隔）：
 * <pre>
 * sha256(jarBytes) | pluginCode | version | md5 | sizeBytes
 * </pre>
 *
 * <p>sha256 与 md5 均为小写 32/64 位十六进制，size 为十进制字节数；解析时严格校验段数与类型。
 *
 * @author HouCloud Plugin Platform
 * @since M0
 */
public final class ArtifactSignaturePayload {

    /**
     * 字段分隔符。
     */
    private static final String SEPARATOR = "|";

    /**
     * 规范化串的段数。
     */
    private static final int SEGMENT_COUNT = 5;

    /**
     * 私有构造，工具类不允许实例化。
     */
    private ArtifactSignaturePayload() {
    }

    /**
     * 由已计算好的摘要信息构建规范化签名串。
     *
     * @param sha256Hex  制品 sha256（小写十六进制，64 位）
     * @param pluginCode 插件编码
     * @param version    插件版本
     * @param md5Hex     制品 md5（小写十六进制，32 位）
     * @param sizeBytes  制品字节数
     * @return 规范化签名串
     */
    public static String build(String sha256Hex, String pluginCode, String version, String md5Hex, long sizeBytes) {
        Objects.requireNonNull(sha256Hex, "sha256Hex 不能为空");
        Objects.requireNonNull(pluginCode, "pluginCode 不能为空");
        Objects.requireNonNull(version, "version 不能为空");
        Objects.requireNonNull(md5Hex, "md5Hex 不能为空");
        return sha256Hex + SEPARATOR
                + pluginCode + SEPARATOR
                + version + SEPARATOR
                + md5Hex + SEPARATOR
                + sizeBytes;
    }

    /**
     * 直接由制品字节构建规范化签名串（内部计算 sha256 / md5 / size）。
     *
     * @param jarBytes   制品（jar）完整字节
     * @param pluginCode 插件编码
     * @param version    插件版本
     * @return 规范化签名串
     */
    public static String build(byte[] jarBytes, String pluginCode, String version) {
        Objects.requireNonNull(jarBytes, "jarBytes 不能为空");
        return build(sha256Hex(jarBytes), pluginCode, version, md5Hex(jarBytes), jarBytes.length);
    }

    /**
     * 解析规范化签名串，校验段数与类型。
     *
     * @param payload 规范化签名串
     * @return 解析结果
     * @throws IllegalArgumentException 段数不足或 size 无法解析时抛出
     */
    public static ParsedPayload parse(String payload) {
        if (payload == null || payload.isEmpty()) {
            throw new IllegalArgumentException("签名规范化串为空，无法解析");
        }
        String[] segments = payload.split("\\" + SEPARATOR, -1);
        if (segments.length != SEGMENT_COUNT) {
            throw new IllegalArgumentException(
                    "签名规范化串格式非法，期望 " + SEGMENT_COUNT + " 段，实际 " + segments.length + " 段");
        }
        long sizeBytes;
        try {
            sizeBytes = Long.parseLong(segments[4]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("签名规范化串第 5 段（size）不是合法数字：" + segments[4], e);
        }
        return new ParsedPayload(segments[0], segments[1], segments[2], segments[3], sizeBytes);
    }

    /**
     * 计算字节数组的 sha256 十六进制串（小写）。
     *
     * @param data 原始字节
     * @return 小写十六进制 sha256
     */
    public static String sha256Hex(byte[] data) {
        return toHex(digest("SHA-256", data));
    }

    /**
     * 计算字节数组的 md5 十六进制串（小写）。
     *
     * @param data 原始字节
     * @return 小写十六进制 md5
     */
    public static String md5Hex(byte[] data) {
        return toHex(digest("MD5", data));
    }

    private static byte[] digest(String algorithm, byte[] data) {
        try {
            return MessageDigest.getInstance(algorithm).digest(data);
        } catch (NoSuchAlgorithmException e) {
            throw new ArtifactSignatureException("当前 JDK 不支持摘要算法 " + algorithm, e);
        }
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(Character.forDigit((b >> 4) & 0xF, 16));
            sb.append(Character.forDigit(b & 0xF, 16));
        }
        return sb.toString();
    }

    /**
     * 规范化签名串的解析结果。
     */
    public static final class ParsedPayload {

        private final String sha256Hex;

        private final String pluginCode;

        private final String version;

        private final String md5Hex;

        private final long sizeBytes;

        private ParsedPayload(String sha256Hex, String pluginCode, String version, String md5Hex, long sizeBytes) {
            this.sha256Hex = sha256Hex;
            this.pluginCode = pluginCode;
            this.version = version;
            this.md5Hex = md5Hex;
            this.sizeBytes = sizeBytes;
        }

        /**
         * @return 制品 sha256（小写十六进制）
         */
        public String getSha256Hex() {
            return sha256Hex;
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
         * @return 制品 md5（小写十六进制）
         */
        public String getMd5Hex() {
            return md5Hex;
        }

        /**
         * @return 制品字节数
         */
        public long getSizeBytes() {
            return sizeBytes;
        }
    }

    /**
     * 将规范化签名串转为签名/验签用的字节（UTF-8），保证签名端与验签端编码一致。
     *
     * @param payload 规范化签名串
     * @return UTF-8 字节
     */
    public static byte[] toSignBytes(String payload) {
        return payload.getBytes(StandardCharsets.UTF_8);
    }
}
