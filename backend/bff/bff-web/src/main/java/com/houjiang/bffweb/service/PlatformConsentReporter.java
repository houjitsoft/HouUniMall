package com.houjiang.bffweb.service;

import com.houjiang.bffweb.controller.plugin.ProxyRestTemplateFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 站点侧：把协议签署记录回传到平台（unimall.houjit.com）。
 *
 * <p><b>为什么要回传</b>：协议由平台统一发布，站点里用户同意后记录落在站内
 * {@code hou_user_agreement_consent}；平台侧还需要一份各站点、各用户的签署凭据，
 * 用于合规举证与统计。所以站内写成功后，站点把同一条记录异步上报一份给平台。</p>
 *
 * <p><b>铁律（别改反）</b>：</p>
 * <ul>
 *   <li>回传是<b>副本</b>，不替代站内记录；</li>
 *   <li>回传失败<b>不阻断</b>主流程、不让用户看到报错——合规主记录已经在站内写好了，
 *       平台侧缺一条只是留痕不完整，不能反过来让「同意协议」这个动作失败；</li>
 *   <li>未配置平台地址/站点标识/令牌时<b>静默跳过</b>（开源自部署默认不回传）。</li>
 * </ul>
 *
 * <p>开关：{@code houcloud.platform.report-enabled}（默认 false），
 * 平台地址 {@code houcloud.platform.url}、站点标识 {@code houcloud.platform.site-code}、
 * 站点名称 {@code houcloud.platform.site-name}、共享令牌 {@code houcloud.platform.token}。</p>
 *
 * @author Amos
 */
@Service
public class PlatformConsentReporter {

    private static final Logger log = LoggerFactory.getLogger(PlatformConsentReporter.class);

    @Value("${houcloud.platform.report-enabled:false}")
    private boolean reportEnabled;

    @Value("${houcloud.platform.url:}")
    private String platformUrl;

    @Value("${houcloud.platform.site-code:}")
    private String siteCode;

    @Value("${houcloud.platform.site-name:}")
    private String siteName;

    @Value("${houcloud.platform.token:}")
    private String platformToken;

    private final RestTemplate http = ProxyRestTemplateFactory.create(5000, 15000);

    private final ExecutorService pool = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "platform-consent-report");
        t.setDaemon(true);
        return t;
    });

    /**
     * 异步回传（失败只记日志）。
     *
     * @param userId   站内用户ID
     * @param username 登录名/手机号
     * @param consents 站内刚写入的同意记录（元素含 code / agreementName / versionNo）
     * @param scene    场景：register / reagree / plugin_purchase
     * @param ip       客户端IP
     * @param ua       UA
     * @param source   来源端
     */
    public void reportAsync(String userId, String username, List<Map<String, Object>> consents,
                            String scene, String ip, String ua, String source) {
        if (!reportEnabled) {
            return;
        }
        if (!StringUtils.hasText(platformUrl) || !StringUtils.hasText(siteCode)
                || !StringUtils.hasText(platformToken)) {
            log.debug("协议回传未配置完整（url/siteCode/token），跳过本次回传");
            return;
        }
        if (consents == null || consents.isEmpty()) {
            return;
        }
        List<Map<String, Object>> items = new ArrayList<>(consents.size());
        String now = Instant.now().toString();
        for (Map<String, Object> consent : consents) {
            if (consent == null) {
                continue;
            }
            String code = firstNonBlank(consent.get("code"), consent.get("agreementCode"),
                    consent.get("agreement_code"));
            String versionNo = firstNonBlank(consent.get("versionNo"), consent.get("version"),
                    consent.get("version_no"));
            if (!StringUtils.hasText(code) || !StringUtils.hasText(versionNo)) {
                continue;
            }
            Map<String, Object> item = new HashMap<>(12);
            item.put("userId", userId);
            item.put("username", username);
            item.put("agreementCode", code);
            item.put("agreementName", firstNonBlank(consent.get("agreementName"), consent.get("name")));
            item.put("versionNo", versionNo);
            item.put("scene", scene);
            item.put("source", source);
            item.put("ip", ip);
            item.put("userAgent", ua);
            item.put("consentTime", now);
            items.add(item);
        }
        if (items.isEmpty()) {
            return;
        }
        String url = platformUrl.trim();
        while (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        url = url + "/web/platform/consent/report";
        String finalUrl = url;
        pool.submit(() -> {
            try {
                Map<String, Object> body = new HashMap<>(4);
                body.put("siteCode", siteCode.trim());
                body.put("siteName", siteName == null ? "" : siteName.trim());
                body.put("items", items);
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("X-Platform-Token", platformToken.trim());
                ResponseEntity<String> resp = http.postForEntity(
                        finalUrl, new HttpEntity<>(body, headers), String.class);
                if (!resp.getStatusCode().is2xxSuccessful()) {
                    log.warn("协议签署记录回传平台失败：HTTP {} siteCode={}", resp.getStatusCode(), siteCode);
                    return;
                }
                log.info("协议签署记录已回传平台：siteCode={} 条数={}", siteCode, items.size());
            } catch (Exception e) {
                // 回传失败不影响站内留痕，也不向用户暴露
                log.warn("协议签署记录回传平台异常（站内记录已落库，不影响用户）: {}", e.getMessage());
            }
        });
    }

    private static String firstNonBlank(Object... values) {
        if (values == null) {
            return "";
        }
        for (Object v : values) {
            if (v != null && StringUtils.hasText(String.valueOf(v))) {
                return String.valueOf(v).trim();
            }
        }
        return "";
    }
}
