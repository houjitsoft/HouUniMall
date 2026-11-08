package com.houjiang.commons.util;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 支付渠道凭证加解密工具
 * - 算法 AES/CBC/PKCS5Padding，128 位
 * - 字段级加解密，仅敏感字段加密（白名单）
 * - 已加密值带固定前缀 "ENC::"，避免重复加密
 * - KEY/IV 默认硬编码，生产建议在启动时通过 setKeyIv 注入 Nacos 配置
 * @author Amos
 */
@Slf4j
public final class PayCredentialCryptoUtil {

    /** 加密前缀标识，用于识别已加密字段 */
    public static final String ENC_PREFIX = "ENC::";

    /** 默认 16 字节 KEY（AES-128），生产请从 Nacos payment.crypto.key 注入 */
    private static volatile String AES_KEY;

    /** 默认 16 字节 IV，生产请从 Nacos payment.crypto.iv 注入 */
    private static volatile String AES_IV;

    /** 敏感字段白名单（仅这些字段进行加密落库） */
    private static final Set<String> SENSITIVE_FIELDS = new HashSet<>(Arrays.asList(
            "privateKey",
            "apiKeySecret",
            "apiV3Key",
            "appSecret",
            "certPath",
            "mchKey",
            "signKey",
            "alipayPublicKey"
    ));

    private PayCredentialCryptoUtil() {
    }

    /**
     * 启动时注入 KEY/IV（建议在 paas/payment 启动 Bean 中调用）
     */
    public static void setKeyIv(String key, String iv) {
        if (key == null || key.getBytes(StandardCharsets.UTF_8).length != 16) {
            throw new IllegalArgumentException("payment.crypto.key must be exactly 16 UTF-8 bytes");
        }
        if (iv == null || iv.getBytes(StandardCharsets.UTF_8).length != 16) {
            throw new IllegalArgumentException("payment.crypto.iv must be exactly 16 UTF-8 bytes");
        }
        AES_KEY = key;
        AES_IV = iv;
    }

    /**
     * 加密字段，已加密则原样返回
     */
    public static String encryptField(String plain) {
        if (plain == null || plain.isEmpty()) {
            return plain;
        }
        if (plain.startsWith(ENC_PREFIX)) {
            return plain;
        }
        requireConfigured();
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec keySpec = new SecretKeySpec(AES_KEY.getBytes(StandardCharsets.UTF_8), "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(AES_IV.getBytes(StandardCharsets.UTF_8));
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            byte[] encrypted = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));
            return ENC_PREFIX + Base64.encodeBase64String(encrypted);
        } catch (Exception e) {
            throw new IllegalStateException("Payment credential encryption failed", e);
        }
    }

    /**
     * 解密字段，未加密则原样返回
     */
    public static String decryptField(String cipherText) {
        if (cipherText == null || cipherText.isEmpty()) {
            return cipherText;
        }
        if (!cipherText.startsWith(ENC_PREFIX)) {
            return cipherText;
        }
        requireConfigured();
        try {
            String body = cipherText.substring(ENC_PREFIX.length());
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec keySpec = new SecretKeySpec(AES_KEY.getBytes(StandardCharsets.UTF_8), "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(AES_IV.getBytes(StandardCharsets.UTF_8));
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            byte[] original = cipher.doFinal(Base64.decodeBase64(body));
            return new String(original, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Payment credential decryption failed", e);
        }
    }

    /**
     * 加密 configJson 中的敏感字段（按白名单），返回 JSON 字符串
     */
    public static String encryptConfigJson(Map<String, Object> configMap) {
        if (configMap == null || configMap.isEmpty()) {
            return "{}";
        }
        for (Map.Entry<String, Object> entry : configMap.entrySet()) {
            if (SENSITIVE_FIELDS.contains(entry.getKey()) && entry.getValue() != null) {
                configMap.put(entry.getKey(), encryptField(String.valueOf(entry.getValue())));
            }
        }
        return JSONObject.toJSONString(configMap);
    }

    /**
     * 解密 configJson 中的敏感字段，返回 Map
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> decryptConfigJson(String configJson) {
        if (configJson == null || configJson.isEmpty()) {
            return JSONObject.parseObject("{}").getInnerMap();
        }
        Map<String, Object> map = JSONObject.parseObject(configJson).getInnerMap();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (SENSITIVE_FIELDS.contains(entry.getKey()) && entry.getValue() != null) {
                String val = String.valueOf(entry.getValue());
                if (val.startsWith(ENC_PREFIX)) {
                    map.put(entry.getKey(), decryptField(val));
                }
            }
        }
        return map;
    }

    /**
     * 脱敏 configJson 用于列表展示，敏感字段统一替换为 "***"
     */
    public static String desensitizeConfigJson(String configJson) {
        if (configJson == null || configJson.isEmpty()) {
            return "{}";
        }
        Map<String, Object> map = JSONObject.parseObject(configJson).getInnerMap();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (SENSITIVE_FIELDS.contains(entry.getKey()) && entry.getValue() != null) {
                map.put(entry.getKey(), "***");
            }
        }
        return JSONObject.toJSONString(map);
    }

    /**
     * 判断字段是否敏感
     */
    public static boolean isSensitive(String fieldName) {
        return SENSITIVE_FIELDS.contains(fieldName);
    }

    private static void requireConfigured() {
        if (AES_KEY == null || AES_IV == null) {
            throw new IllegalStateException("Payment credential encryption is not configured");
        }
    }
}
