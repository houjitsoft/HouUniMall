package com.houjiang.mall.pay.plugin;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 回调签名工具：HMAC-SHA256 签名 / 验签 + 时间戳防重放 + 常量时间比较。
 *
 * <p><b>为什么自己实现而不是接各渠道 SDK</b>：插件市场只用到「下单 + 回调验签」两个动作，
 * 引入微信/支付宝 SDK 会带进大量与本站依赖冲突的传递依赖；且 CASHIER（内部收银台）本身
 * 就没有官方 SDK。统一成一种签名契约，新增渠道时只需复用本类。
 *
 * <p><b>签名契约</b>（下单与回调两侧共用）：
 * <pre>
 *   sign = HMAC_SHA256(secret, 参数名升序拼接的 k=v&amp;k=v... + "&amp;timestamp=" + ts + "&amp;nonce=" + nonce)
 * </pre>
 *
 * @author Amos
 */
@Slf4j
@Component
public class PluginPaySigner {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    /**
     * 生成签名。
     *
     * @param params   参与签名的业务参数（null 值跳过）
     * @param secret   商户密钥
     * @param timestamp 秒级时间戳
     * @param nonce    随机串
     */
    public String sign(Map<String, String> params, String secret, long timestamp, String nonce) {
        return hmacSha256(canonical(params, timestamp, nonce), secret);
    }

    /**
     * 验签 + 时间戳校验。
     *
     * @param params       回调参数（含 sign / timestamp / nonce）
     * @param secret       商户密钥
     * @param toleranceSec 允许的时钟偏移（秒）
     * @throws PayNotifyException 验签失败或时间戳超窗（均 fatal）
     */
    public void verify(Map<String, String> params, String secret, long toleranceSec) {
        String sign = params.get("sign");
        if (sign == null || sign.isBlank()) {
            throw new PayNotifyException("回调缺少签名字段 sign，拒绝处理");
        }
        String timestamp = params.get("timestamp");
        if (timestamp == null || timestamp.isBlank()) {
            throw new PayNotifyException("回调缺少时间戳 timestamp，无法防重放，拒绝处理");
        }
        long ts;
        try {
            ts = Long.parseLong(timestamp.trim());
        } catch (NumberFormatException e) {
            throw new PayNotifyException("回调 timestamp 格式非法: " + timestamp);
        }
        long now = Instant.now().getEpochSecond();
        if (Math.abs(now - ts) > toleranceSec) {
            throw new PayNotifyException("回调时间戳超出容忍窗口（" + toleranceSec + "秒），疑似重放攻击，拒绝处理");
        }
        String expected = sign(excluding(params, "sign"), secret, ts, params.get("nonce"));
        if (!constantTimeEquals(expected, sign.trim())) {
            throw new PayNotifyException("回调验签失败，签名不匹配（请检查渠道密钥配置是否被改动）");
        }
    }

    /**
     * 校验支付结果回调的金额是否与订单一致（防「改小金额」攻击）。
     *
     * <p>🔴 2026-10-27 修 P0：原实现遇到 null 直接 return —— 等于「回调不传金额就放行」，
     * 攻击者把 amount 字段删掉即可用任意低价（或 0 元）结算出全价授权。
     * 现在缺任一侧金额都视为不可核对，一律拒收。
     */
    public static void assertAmountMatches(java.math.BigDecimal expected, java.math.BigDecimal actual) {
        if (expected == null) {
            throw new PayNotifyException("订单金额为空，无法校验实收金额，拒绝结算");
        }
        if (actual == null) {
            throw new PayNotifyException("回调缺少金额字段 amount，无法确认实收金额，拒绝结算"
                    + "（订单金额 " + expected + "）");
        }
        if (expected.compareTo(actual) != 0) {
            throw new PayNotifyException("回调金额与订单金额不一致（订单 " + expected + " / 回调 " + actual
                    + "），拒绝结算并等待人工核对");
        }
    }

    /** 参数按名升序拼串；null / 空值跳过，sign 不参与。 */
    static String canonical(Map<String, String> params, long timestamp, String nonce) {
        Map<String, String> sorted = new TreeMap<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (key == null || "sign".equals(key) || value == null || value.isBlank()) {
                continue;
            }
            sorted.put(key, value.trim());
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : sorted.entrySet()) {
            sb.append(entry.getKey()).append('=').append(entry.getValue()).append('&');
        }
        sb.append("timestamp=").append(timestamp);
        if (nonce != null && !nonce.isBlank()) {
            sb.append("&nonce=").append(nonce.trim());
        }
        return sb.toString();
    }

    static Map<String, String> excluding(Map<String, String> params, String... keys) {
        List<String> excluded = new ArrayList<>();
        for (String key : keys) {
            if (key != null) {
                excluded.add(key);
            }
        }
        Map<String, String> copy = new java.util.LinkedHashMap<>();
        params.forEach((k, v) -> {
            if (k != null && !excluded.contains(k)) {
                copy.put(k, v);
            }
        });
        return copy;
    }

    public static String hmacSha256(String data, String secret) {
        if (secret == null || secret.isEmpty()) {
            throw new PayNotifyException("签名密钥为空，无法签名（请配置渠道 apiKey）");
        }
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            byte[] bytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                sb.append(Character.forDigit((b >> 4) & 0xF, 16));
                sb.append(Character.forDigit(b & 0xF, 16));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new PayNotifyException("签名计算失败: " + e.getMessage());
        }
    }

    /** 常量时间比较，避免时序侧信道。 */
    public static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        byte[] left = a.getBytes(StandardCharsets.UTF_8);
        byte[] right = b.getBytes(StandardCharsets.UTF_8);
        if (left.length != right.length) {
            return false;
        }
        int diff = 0;
        for (int i = 0; i < left.length; i++) {
            diff |= left[i] ^ right[i];
        }
        return diff == 0;
    }

    /** 简单摘要，用于生成回调幂等 notifyId（渠道未提供时）。 */
    public static String digest(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 8; i++) {
                sb.append(Character.forDigit((bytes[i] >> 4) & 0xF, 16));
                sb.append(Character.forDigit(bytes[i] & 0xF, 16));
            }
            return sb.toString();
        } catch (Exception e) {
            log.warn("摘要计算失败，退回随机串: {}", e.getMessage());
            return String.valueOf(System.nanoTime());
        }
    }
}
