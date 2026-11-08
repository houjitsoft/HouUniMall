package com.houjiang.plugin.engine.artifact;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * 插件安装包完整性校验（MD5）。
 *
 * <p>对应市场侧 {@code hou_market_plugin_version.jar_md5}。校验口径：
 * <ul>
 *   <li>市场声明了摘要 → 必须一致，否则视为安装包被篡改/传输损坏</li>
 *   <li>市场未声明摘要 → 放行（仅记录，不做阻断）</li>
 * </ul>
 *
 * <p><b>边界说明</b>：MD5 只能证明「传输一致」，不能证明「来源可信」；
 * 真正的来源可信由签名验签（{@code PluginSignatureVerifier}）承担，尚未启用。
 *
 * @author HouCloud
 */
public class PluginChecksumVerifier {

    private static final String ALGORITHM = "MD5";

    private static final int BUFFER_SIZE = 8192;

    public String md5(byte[] content) {
        if (content == null) {
            throw new IllegalArgumentException("安装包内容不能为空");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            return HexFormat.of().formatHex(digest.digest(content));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("当前 JVM 不支持 " + ALGORITHM, e);
        }
    }

    /**
     * 计算磁盘文件摘要。
     *
     * <p>按 8KB 缓冲流式更新，不把整个制品读进内存（制品上限默认 64MB）。
     */
    public String md5(Path file) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("安装包路径不能为空");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            try (InputStream in = Files.newInputStream(file)) {
                byte[] buffer = new byte[BUFFER_SIZE];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    digest.update(buffer, 0, read);
                }
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("当前 JVM 不支持 " + ALGORITHM, e);
        }
    }

    /**
     * 校验字节内容摘要。
     *
     * @param expectedMd5 市场声明的摘要，空值表示不校验
     */
    public boolean verify(byte[] content, String expectedMd5) {
        return verify(content, expectedMd5, false);
    }

    /**
     * 校验磁盘文件摘要。
     */
    public boolean verify(Path file, String expectedMd5) throws IOException {
        return verify(file, expectedMd5, false);
    }

    /**
     * 校验字节内容摘要（可强制要求声明）。
     *
     * <p><b>为什么需要 {@code requireDeclared}</b>：宽松模式下「市场没声明摘要」直接放行，
     * 一旦市场侧漏填字段，整条校验链等于不存在。安装/升级这类高危动作应当传
     * {@code true}——没有声明摘要就按校验失败处理，宁可装不上，也不能把来源不明的包放进来。
     *
     * @param expectedMd5     市场声明的摘要
     * @param requireDeclared 为 {@code true} 时，摘要为空视为校验失败
     */
    public boolean verify(byte[] content, String expectedMd5, boolean requireDeclared) {
        if (content == null) {
            throw new IllegalArgumentException("安装包内容不能为空");
        }
        if (expectedMd5 == null || expectedMd5.isBlank()) {
            return !requireDeclared;
        }
        return md5(content).equalsIgnoreCase(expectedMd5.trim());
    }

    /**
     * 校验磁盘文件摘要（可强制要求声明）。
     *
     * @see #verify(byte[], String, boolean)
     */
    public boolean verify(Path file, String expectedMd5, boolean requireDeclared) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("安装包路径不能为空");
        }
        if (!Files.isRegularFile(file)) {
            throw new IllegalArgumentException("安装包不存在或不是普通文件：" + file);
        }
        if (expectedMd5 == null || expectedMd5.isBlank()) {
            return !requireDeclared;
        }
        return md5(file).equalsIgnoreCase(expectedMd5.trim());
    }
}
