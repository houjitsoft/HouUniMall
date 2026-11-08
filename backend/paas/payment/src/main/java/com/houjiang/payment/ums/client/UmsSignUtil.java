package com.houjiang.payment.ums.client;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * 银联商务企业订单报文签名：参数字典序拼接后 SHA256withRSA。
 */
public final class UmsSignUtil {

    private UmsSignUtil() {}

    public static String buildSignContent(Map<String, ?> params) {
        TreeMap<String, String> sorted = new TreeMap<>();
        for (Map.Entry<String, ?> e : params.entrySet()) {
            if (e.getKey() == null || "signature".equals(e.getKey())) continue;
            if (e.getValue() == null) continue;
            String v = String.valueOf(e.getValue());
            if (v.isEmpty()) continue;
            sorted.put(e.getKey(), v);
        }
        return sorted.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));
    }

    public static String sign(Map<String, ?> params, String privateKeyPem) {
        try {
            String content = buildSignContent(params);
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(loadPrivateKey(privateKeyPem));
            signature.update(content.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(signature.sign());
        } catch (Exception ex) {
            throw new IllegalStateException("UMS 加签失败: " + ex.getMessage(), ex);
        }
    }

    public static boolean verify(Map<String, ?> params, String publicKeyPem) {
        try {
            Object sig = params.get("signature");
            if (sig == null || String.valueOf(sig).isBlank()) return false;
            String content = buildSignContent(params);
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(loadPublicKey(publicKeyPem));
            signature.update(content.getBytes(StandardCharsets.UTF_8));
            return signature.verify(Base64.getDecoder().decode(String.valueOf(sig)));
        } catch (Exception ex) {
            return false;
        }
    }

    /** 分账卡号：SHA-256 十六进制小写 */
    public static String sha256Hex(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] dig = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(dig.length * 2);
            for (byte b : dig) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception ex) {
            throw new IllegalStateException("SHA-256 失败", ex);
        }
    }

    private static PrivateKey loadPrivateKey(String pem) throws Exception {
        String normalized = stripPem(pem);
        byte[] keyBytes = Base64.getDecoder().decode(normalized);
        return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
    }

    private static PublicKey loadPublicKey(String pem) throws Exception {
        String normalized = stripPem(pem);
        byte[] keyBytes = Base64.getDecoder().decode(normalized);
        return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(keyBytes));
    }

    private static String stripPem(String pem) {
        if (pem == null || pem.isBlank()) throw new IllegalArgumentException("密钥为空");
        return pem.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", "")
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
    }
}
