package com.houjiang.payment.ums.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.houjiang.payment.ums.config.UmsConfigStore;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 银联商务企业订单 HTTPS 报文客户端。
 */
public class UmsClient {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HHmmss");

    private final RestTemplate restTemplate;

    public UmsClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Map<String, Object> invoke(UmsConfigStore.Config cfg, String transCode, Map<String, Object> body) {
        if (cfg == null || !cfg.enabled()) {
            throw new IllegalStateException("银联企业订单渠道未启用，请先在插件配置中开启");
        }
        if (isBlank(cfg.mchPrivateKey())) {
            throw new IllegalStateException("未配置商户私钥，无法加签");
        }
        LocalDateTime now = LocalDateTime.now();
        Map<String, Object> payload = new LinkedHashMap<>();
        if (body != null) payload.putAll(body);
        payload.put("transCode", transCode);
        payload.put("verNo", blankTo(cfg.verNo(), "100"));
        payload.put("srcReqDate", DATE.format(now));
        payload.put("srcReqTime", TIME.format(now));
        payload.put("srcReqId", newReqId());
        payload.put("channelId", blankTo(cfg.channelId(), "043"));
        payload.put("groupId", blankTo(cfg.groupId(), ""));
        payload.put("signature", UmsSignUtil.sign(payload, cfg.mchPrivateKey()));

        String url = blankTo(cfg.baseUrl(),
                "https://mobl-test.chinaums.com/channel/Business/UnifyMulti") + "/" + transCode;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String json = MAPPER.writeValueAsString(payload);
            ResponseEntity<String> resp = restTemplate.postForEntity(url, new HttpEntity<>(json, headers), String.class);
            String respBody = resp.getBody() == null ? "{}" : resp.getBody();
            Map<String, Object> result = MAPPER.readValue(respBody, new TypeReference<>() {});
            if (!isBlank(cfg.umsPublicKey()) && result.containsKey("signature")
                    && !UmsSignUtil.verify(result, cfg.umsPublicKey())) {
                throw new IllegalStateException("银商响应验签失败");
            }
            result.put("_req", payload);
            result.put("_raw", respBody);
            return result;
        } catch (IllegalStateException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("调用银联接口失败(" + transCode + "): " + ex.getMessage(), ex);
        }
    }

    public static String newReqId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static String blankTo(String s, String def) {
        return isBlank(s) ? def : s.trim();
    }
}
