package com.houjiang.mall.service.market;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 站点侧：插件下载次数与平台（unimall.houjit.com）对齐。
 *
 * <p><b>为什么要有它</b>：下载次数是「这个插件有多少人装过」的公共信号，必须由<b>平台</b>
 * 统一持有——每个站点各算各的，别的安装人看到的永远只是自己那一次。所以：</p>
 * <ul>
 *   <li><b>装完就上报</b>：{@link #reportAsync} 把「本站点装了某插件某版本」告诉平台，
 *       平台落明细并重算次数（同站点同版本幂等，不刷量）；</li>
 *   <li><b>列表就读平台</b>：{@link #remoteCounts} 从平台批量取真实次数，覆盖本地展示值；
 *       这样任何安装人（含开源自部署）看到的都是全网真实数字。</li>
 * </ul>
 *
 * <p><b>铁律（别改反）</b>：</p>
 * <ul>
 *   <li>上报是旁路：失败<b>不阻断</b>安装、不让用户看到报错，站内明细已经落好了；</li>
 *   <li>未配置 {@code houcloud.platform.url} 时<b>静默跳过</b>——单机部署下本地明细就是权威，
 *       平台自身（unimall.houjit.com）也不配这个地址，因此不会自打自；</li>
 *   <li>读平台失败一律<b>回退本地值</b>，绝不因为平台不可用把市场页打成空白。</li>
 * </ul>
 *
 * <p>配置项与站点协议回传共用同一组：{@code houcloud.platform.url / site-code / site-name /
 * token / report-enabled}。</p>
 *
 * @author Amos
 */
@Slf4j
@Service
public class PlatformDownloadReporter {

    /** 本地就是平台时使用的站点标识。 */
    private static final String SITE_PLATFORM = PluginDownloadStatsService.SITE_PLATFORM;

    /** 平台计数缓存有效期：60s 内同一批插件不重复请求。 */
    private static final long CACHE_TTL_MS = 60_000L;

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

    private final RestTemplate http;

    private final ExecutorService pool = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "platform-download-report");
        thread.setDaemon(true);
        return thread;
    });

    /** 平台计数本地缓存：code → 次数。 */
    private volatile Map<String, Integer> cachedCounts = Map.of();

    private volatile long cachedAt = 0L;

    public PlatformDownloadReporter() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        // 短超时：市场页不能因为平台慢而卡住
        factory.setConnectTimeout(800);
        factory.setReadTimeout(1500);
        this.http = new RestTemplate(factory);
    }

    /**
     * 本站点标识：未配置时视为平台自身。
     *
     * @return 站点标识，永不为 null
     */
    public String localSiteCode() {
        return StringUtils.hasText(siteCode) ? siteCode.trim() : SITE_PLATFORM;
    }

    /**
     * 本站点名称。
     *
     * @return 站点名称，未配置返回空串
     */
    public String localSiteName() {
        return siteName == null ? "" : siteName.trim();
    }

    /**
     * 是否对接了平台（配了平台地址即视为对接）。
     *
     * @return true = 计数以平台为准
     */
    public boolean platformLinked() {
        return StringUtils.hasText(platformUrl);
    }

    /**
     * 异步上报一次下载 / 安装到平台。
     *
     * @param pluginCode 插件编码
     * @param version    版本号
     * @param source     install / download
     */
    public void reportAsync(String pluginCode, String version, String source) {
        if (!reportEnabled || !StringUtils.hasText(pluginCode) || !platformLinked()) {
            return;
        }
        if (!StringUtils.hasText(platformToken)) {
            log.debug("插件下载上报未配置 houcloud.platform.token，跳过");
            return;
        }
        String url = trimSlash(platformUrl) + "/web/plugin-center/market/download/report";
        String site = localSiteCode();
        String name = localSiteName();
        String token = platformToken.trim();
        String code = pluginCode.trim();
        String ver = version == null ? "" : version.trim();
        String src = StringUtils.hasText(source) ? source.trim() : "install";
        pool.submit(() -> {
            try {
                Map<String, Object> body = new HashMap<>(8);
                body.put("pluginCode", code);
                body.put("version", ver);
                body.put("siteCode", site);
                body.put("siteName", name);
                body.put("source", src);
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("X-Platform-Token", token);
                ResponseEntity<String> resp = http.postForEntity(url, new HttpEntity<>(body, headers), String.class);
                if (!resp.getStatusCode().is2xxSuccessful()) {
                    log.warn("插件下载上报平台失败：HTTP {} code={} site={}", resp.getStatusCode(), code, site);
                    return;
                }
                log.info("插件下载已上报平台：code={} version={} site={}", code, ver, site);
                // 计数变了，让下次列表重新取
                cachedAt = 0L;
            } catch (Exception e) {
                // 旁路能力：上报失败不影响本站安装结果
                log.warn("插件下载上报平台异常（本站安装不受影响）: {}", e.getMessage());
            }
        });
    }

    /**
     * 从平台批量取真实下载次数。
     *
     * <p>60s 进程内缓存（整批）：市场页滚动 / 切分类不会把平台打成压力源。
     * 任何异常都返回空 Map，调用方据此回退本地计数。
     *
     * @param codes 插件编码
     * @return code → 平台真实次数；未对接平台或失败时为空 Map
     */
    public Map<String, Integer> remoteCounts(List<String> codes) {
        if (!platformLinked() || codes == null || codes.isEmpty()) {
            return Map.of();
        }
        Map<String, Integer> cached = cachedCounts;
        if (System.currentTimeMillis() - cachedAt < CACHE_TTL_MS && !cached.isEmpty()) {
            return cached;
        }
        String url = trimSlash(platformUrl) + "/web/plugin-center/market/download/stats";
        try {
            Map<String, Object> body = new HashMap<>(2);
            body.put("codes", new ArrayList<>(codes));
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (StringUtils.hasText(platformToken)) {
                headers.set("X-Platform-Token", platformToken.trim());
            }
            ResponseEntity<String> resp = http.postForEntity(url, new HttpEntity<>(body, headers), String.class);
            if (!resp.getStatusCode().is2xxSuccessful() || !StringUtils.hasText(resp.getBody())) {
                return Map.of();
            }
            JSONObject json = JSON.parseObject(resp.getBody());
            Object data = json == null ? null : json.get("data");
            if (!(data instanceof JSONObject dataObj)) {
                return Map.of();
            }
            Object counts = dataObj.get("counts");
            if (!(counts instanceof JSONObject countsObj)) {
                return Map.of();
            }
            Map<String, Integer> result = new LinkedHashMap<>();
            for (Map.Entry<String, Object> entry : countsObj.entrySet()) {
                Object value = entry.getValue();
                if (value instanceof Number number) {
                    result.put(entry.getKey(), number.intValue());
                }
            }
            cachedCounts = Collections.unmodifiableMap(result);
            cachedAt = System.currentTimeMillis();
            return cachedCounts;
        } catch (Exception e) {
            log.debug("读取平台插件下载次数失败（回退本地计数）: {}", e.getMessage());
            return Map.of();
        }
    }

    private static String trimSlash(String url) {
        String value = url == null ? "" : url.trim();
        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }
}
