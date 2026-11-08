package com.houjiang.plugin.artifact.util;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 制品摘要计算工具。
 *
 * <p>提供「一次读流、同时算 md5 + sha256 + 字节数」的能力，
 * 避免为两个摘要各读一遍 64MB 制品。
 *
 * @author Amos
 * @since M0
 */
public final class ArtifactDigests {

    /**
     * 读取缓冲区大小（64KB）。
     */
    private static final int BUFFER_SIZE = 64 * 1024;

    /**
     * 私有构造，工具类不允许实例化。
     */
    private ArtifactDigests() {
    }

    /**
     * 把已算好的摘要字节转成小写十六进制串。
     *
     * <p>注意与 {@link #md5Hex(byte[])} / {@link #sha256Hex(byte[])} 的区别：
     * 那两个是「对原始数据算摘要」，本方法是「把已算好的摘要转成字符串」，<b>不会二次哈希</b>。
     * 落存储时摘要是在 drain 过程中增量算好的，必须用本方法，否则会得到 md5(md5) 的错误值。
     *
     * @param bytes 摘要字节
     * @return 小写十六进制串
     */
    public static String toHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(Character.forDigit((b >> 4) & 0xF, 16));
            sb.append(Character.forDigit(b & 0xF, 16));
        }
        return sb.toString();
    }

    /**
     * 计算字节数组的 md5（小写十六进制）。
     *
     * @param data 原始字节
     * @return 小写十六进制 md5
     */
    public static String md5Hex(byte[] data) {
        return toHexString(digest("MD5", data));
    }

    /**
     * 计算字节数组的 sha256（小写十六进制）。
     *
     * @param data 原始字节
     * @return 小写十六进制 sha256
     */
    public static String sha256Hex(byte[] data) {
        return toHexString(digest("SHA-256", data));
    }

    /**
     * 一次遍历完成「拷贝流 + 计算 md5 + 计算 sha256 + 统计字节数」。
     *
     * @param in        源流
     * @param md5       用于累计 md5 的 {@link MessageDigest}
     * @param sha256    用于累计 sha256 的 {@link MessageDigest}
     * @param byteSink  每块数据的消费动作（写文件 / 写响应），参数依次为 buffer、offset、length
     * @return 实际读取的字节数
     * @throws IOException 读写失败时抛出
     */
    public static long drain(InputStream in, MessageDigest md5, MessageDigest sha256, ByteSink byteSink)
            throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        long total = 0L;
        int read;
        while ((read = in.read(buffer)) > 0) {
            md5.update(buffer, 0, read);
            sha256.update(buffer, 0, read);
            byteSink.accept(buffer, 0, read);
            total += read;
        }
        return total;
    }

    /**
     * 创建指定算法的摘要器。
     *
     * @param algorithm 摘要算法名（MD5 / SHA-256）
     * @return 摘要器
     */
    public static MessageDigest newDigest(String algorithm) {
        try {
            return MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("当前 JDK 不支持摘要算法 " + algorithm, e);
        }
    }

    private static byte[] digest(String algorithm, byte[] data) {
        return newDigest(algorithm).digest(data);
    }

    /**
     * 分块数据消费动作（对应 {@link ArtifactDigests#drain}）。
     */
    @FunctionalInterface
    public interface ByteSink {

        /**
         * 消费一块数据。
         *
         * @param buffer 缓冲区
         * @param offset 起始偏移
         * @param length 有效长度
         * @throws IOException 写入失败时抛出
         */
        void accept(byte[] buffer, int offset, int length) throws IOException;
    }
}
