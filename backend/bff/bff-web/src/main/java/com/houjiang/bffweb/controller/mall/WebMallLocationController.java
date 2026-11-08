package com.houjiang.bffweb.controller.mall;

import com.alibaba.fastjson2.JSONObject;
import com.houjiang.bffweb.util.ResponseResult;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 商城前台访问者地理位置（IP 粗定位）
 * <p>用于商品详情「配送至」自动带出国家/省市区；内网/失败由前端回落中国·北京。</p>
 */
@Slf4j
@RestController
@RequestMapping("/web/mall/front/location")
public class WebMallLocationController {

    private static final String IP_JSON_URL = "https://whois.pconline.com.cn/ipJson.jsp";
    /** 免费 IP 库，lang=zh-CN 返回中文国名，便于匹配商城国家表 */
    private static final String IP_API_URL = "http://ip-api.com/json/";
    private static final Charset GBK = Charset.forName("GBK");

    @GetMapping("/ip")
    public ResponseResult locateByIp(HttpServletRequest request) {
        String ip = resolveClientIp(request);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("ip", ip);
        data.put("country", "");
        data.put("countryCode", "");
        data.put("province", "");
        data.put("city", "");
        data.put("region", "");
        data.put("proCode", "");
        data.put("cityCode", "");
        data.put("addr", "");
        data.put("internal", isInternalIp(ip));

        if (isInternalIp(ip)) {
            data.put("source", "internal");
            return ResponseResult.success(data);
        }

        String source = "";
        try {
            // 1) 国家：ip-api（中文国名 + ISO 码）
            JSONObject geo = fetchIpApi(ip);
            if (geo != null) {
                data.put("country", blankToEmpty(geo.getString("country")));
                data.put("countryCode", blankToEmpty(geo.getString("countryCode")).toUpperCase());
                // 非中国时用 region/city 作参考（前端仅中国展示省市区）
                if (!"CN".equals(data.get("countryCode"))) {
                    data.put("province", blankToEmpty(geo.getString("regionName")));
                    data.put("city", blankToEmpty(geo.getString("city")));
                }
                source = "ip-api";
            }

            // 2) 中国省市区：太平洋 IP 库更细
            boolean maybeChina = "CN".equals(data.get("countryCode"))
                    || blankToEmpty((String) data.get("country")).contains("中国")
                    || blankToEmpty((String) data.get("countryCode")).isEmpty();
            if (maybeChina) {
                fillFromPconline(ip, data);
                if ("pconline".equals(data.get("pconlineSource"))) {
                    if (blankToEmpty((String) data.get("countryCode")).isEmpty()
                            && hasText((String) data.get("province"))) {
                        data.put("country", "中国");
                        data.put("countryCode", "CN");
                    }
                    source = source.isEmpty() ? "pconline" : source + "+pconline";
                }
            }
            data.put("source", source.isEmpty() ? "empty" : source);
        } catch (Exception e) {
            log.warn("IP 定位失败 ip={}, err={}", ip, e.toString());
            data.put("source", "error");
            data.put("error", e.getMessage());
        }
        data.remove("pconlineSource");
        return ResponseResult.success(data);
    }

    private static void fillFromPconline(String ip, Map<String, Object> data) {
        try {
            String raw = httpGet(IP_JSON_URL + "?ip=" + ip + "&json=true", 2500, GBK);
            if (raw == null || raw.isBlank()) {
                return;
            }
            JSONObject obj = JSONObject.parseObject(raw.trim());
            if (obj == null) {
                return;
            }
            String pro = blankToEmpty(obj.getString("pro"));
            String city = blankToEmpty(obj.getString("city"));
            String region = blankToEmpty(obj.getString("region"));
            if (!pro.isEmpty()) {
                data.put("province", pro);
            }
            if (!city.isEmpty()) {
                data.put("city", city);
            }
            if (!region.isEmpty()) {
                data.put("region", region);
            }
            data.put("proCode", blankToEmpty(obj.getString("proCode")));
            data.put("cityCode", blankToEmpty(obj.getString("cityCode")));
            data.put("addr", blankToEmpty(obj.getString("addr")));
            data.put("pconlineSource", "pconline");
        } catch (Exception e) {
            log.warn("pconline 定位失败 ip={}, err={}", ip, e.toString());
        }
    }

    private static JSONObject fetchIpApi(String ip) {
        try {
            String url = IP_API_URL + ip + "?lang=zh-CN&fields=status,country,countryCode,regionName,city,query";
            String raw = httpGet(url, 2500, StandardCharsets.UTF_8);
            if (raw == null || raw.isBlank()) {
                return null;
            }
            JSONObject obj = JSONObject.parseObject(raw.trim());
            if (obj == null || !"success".equalsIgnoreCase(obj.getString("status"))) {
                return null;
            }
            return obj;
        } catch (Exception e) {
            log.warn("ip-api 定位失败 ip={}, err={}", ip, e.toString());
            return null;
        }
    }

    private static boolean hasText(String v) {
        return v != null && !v.isBlank();
    }

    private static String blankToEmpty(String v) {
        return v == null ? "" : v.trim();
    }

    private static String resolveClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip.split(",")[0].trim();
        }
        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }
        ip = request.getRemoteAddr();
        if ("https://example.net/id/garnet".equals(ip) || "::1".equals(ip)) {
            return "127.0.0.1";
        }
        return ip == null ? "" : ip;
    }

    private static boolean isInternalIp(String ip) {
        if (ip == null || ip.isBlank()) return true;
        if ("127.0.0.1".equals(ip) || "localhost".equalsIgnoreCase(ip)) return true;
        if (ip.startsWith("10.") || ip.startsWith("192.168.") || ip.startsWith("127.")) return true;
        if (ip.startsWith("172.")) {
            try {
                int second = Integer.parseInt(ip.split("\\.")[1]);
                return second >= 16 && second <= 31;
            } catch (Exception ignored) {
                return false;
            }
        }
        return false;
    }

    private static String httpGet(String url, int timeoutMs, Charset charset) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) URI.create(url).toURL().openConnection();
        conn.setConnectTimeout(timeoutMs);
        conn.setReadTimeout(timeoutMs);
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", "houcloud-mall/1.0");
        conn.setRequestProperty("Accept", "*/*");
        int code = conn.getResponseCode();
        if (code < 200 || code >= 300) {
            throw new IllegalStateException("HTTP " + code);
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), charset))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } finally {
            conn.disconnect();
        }
    }
}
