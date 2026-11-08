package com.houjiang.payment.util;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * 支付模块敏感字段 AES 加解密工具
 * <p>
 * 复用 commons {@code AesEncryptUtil} 的 KEY/IV，但对纯字符串加解密
 * （不经过 JSON 序列化，避免给字符串自动加引号），用于
 * {@code hou_pay_merchant_qual.app_secret} / {@code api_key} 字段加解密。
 *
 * @author Amos
 * @since 2026-05-13
 */
public class AesPayUtil {

    /** 加密密钥（与 commons.AesEncryptUtil 保持一致，便于跨模块解密） */
    private static final String KEY = "2y0u2n5a0f1e2n4g";
    /** 加密向量 */
    private static final String IV  = "y2u0a2n5f0e1n2g4";
    /** 算法 */
    private static final String ALGORITHM = "AES/CBC/NoPadding";

    private AesPayUtil() {
    }

    /**
     * AES 加密
     *
     * @param plain 明文（null 或空返回原值）
     * @return Base64 后的密文
     */
    public static String encrypt(String plain) {
        if (plain == null || plain.isEmpty()) {
            return plain;
        }
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            int blockSize = cipher.getBlockSize();
            byte[] dataBytes = plain.getBytes("UTF-8");
            int paddedLen = dataBytes.length;
            if (paddedLen % blockSize != 0) {
                paddedLen += blockSize - (paddedLen % blockSize);
            }
            byte[] plainPadded = new byte[paddedLen];
            System.arraycopy(dataBytes, 0, plainPadded, 0, dataBytes.length);
            SecretKeySpec keySpec = new SecretKeySpec(KEY.getBytes("UTF-8"), "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(IV.getBytes("UTF-8"));
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            byte[] encrypted = cipher.doFinal(plainPadded);
            return new Base64().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("AesPayUtil encrypt fail", e);
        }
    }

    /**
     * AES 解密
     *
     * @param cipherText Base64 密文（null 或空返回原值；非密文格式抛异常前先尝试原样返回，便于兼容历史明文）
     * @return 明文
     */
    public static String decrypt(String cipherText) {
        if (cipherText == null || cipherText.isEmpty()) {
            return cipherText;
        }
        try {
            byte[] encrypted = new Base64().decode(cipherText);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(KEY.getBytes("UTF-8"), "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(IV.getBytes("UTF-8"));
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            byte[] original = cipher.doFinal(encrypted);
            return new String(original, "UTF-8").trim();
        } catch (Exception e) {
            // 兼容历史明文：解密失败时原样返回
            return cipherText;
        }
    }
}
