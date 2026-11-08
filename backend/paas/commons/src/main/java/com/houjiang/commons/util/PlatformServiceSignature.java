package com.houjiang.commons.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Locale;

/** Mall 平台财务服务间调用签名。 */
public final class PlatformServiceSignature {

    public static final String TIMESTAMP_HEADER = "X-Hou-Service-Timestamp";
    public static final String NONCE_HEADER = "X-Hou-Service-Nonce";
    public static final String SIGNATURE_HEADER = "X-Hou-Service-Signature";
    private static final long MAX_CLOCK_SKEW_SECONDS = 60L;

    private PlatformServiceSignature() {
    }

    public static String sign(String secret, long timestamp, String nonce, String authorization,
                              String method, String path, byte[] body) {
        requireSecret(secret);
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal(payload(timestamp, nonce, authorization, method, path, body)
                    .getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new IllegalStateException("无法生成服务调用签名", e);
        }
    }

    public static boolean verify(String secret, long now, String timestampValue, String nonce,
                                 String authorization, String method, String path, byte[] body,
                                 String actualSignature) {
        if (secret == null || secret.isBlank() || timestampValue == null || nonce == null
                || nonce.isBlank() || actualSignature == null) {
            return false;
        }
        try {
            long timestamp = Long.parseLong(timestampValue);
            if (Math.abs(now - timestamp) > MAX_CLOCK_SKEW_SECONDS) {
                return false;
            }
            byte[] expected = sign(secret, timestamp, nonce, authorization, method, path, body)
                    .getBytes(StandardCharsets.US_ASCII);
            byte[] actual = actualSignature.getBytes(StandardCharsets.US_ASCII);
            return MessageDigest.isEqual(expected, actual);
        } catch (RuntimeException e) {
            return false;
        }
    }

    private static String payload(long timestamp, String nonce, String authorization,
                                  String method, String path, byte[] body) {
        return timestamp + "\n"
                + nonce.trim() + "\n"
                + (authorization == null ? "" : authorization.trim()) + "\n"
                + (method == null ? "" : method.trim().toUpperCase(Locale.ROOT)) + "\n"
                + (path == null ? "" : path.trim()) + "\n"
                + sha256(body == null ? new byte[0] : body);
    }

    private static String sha256(byte[] value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value));
        } catch (Exception e) {
            throw new IllegalStateException("无法计算服务调用请求摘要", e);
        }
    }

    private static void requireSecret(String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("mall.platform-finance.service-secret 未配置");
        }
    }
}
